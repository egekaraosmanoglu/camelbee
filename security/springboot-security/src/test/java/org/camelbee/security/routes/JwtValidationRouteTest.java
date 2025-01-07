package org.camelbee.security.routes;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.camelbee.security.routes.cache.JwksCache;
import org.camelbee.security.routes.routes.FetchJwksRoute;
import org.camelbee.security.routes.routes.JwtValidationRoute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

/**
 * Unit Tester For Camel Routes.
 */
@CamelSpringBootTest
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@SpringBootTest(
    properties = {
        "camelbee.security.jwksUrl=http://test-auth-server/.well-known/jwks.json",
        "camelbee.security.issuer=test-issuer",
        "camelbee.security.audience=test-audience",
        "camelbee.security.jwks.cache-duration:2000"
    },
    classes = {
        JwksCache.class,
        FetchJwksRoute.class,
        JwtValidationRoute.class
    }
)
@UseAdviceWith
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class JwtValidationRouteTest {

  @Produce("direct:validateJWT")
  protected ProducerTemplate producerTemplate;

  @EndpointInject("mock:result")
  protected MockEndpoint resultEndpoint;

  @EndpointInject("mock:fetchJWKS")
  protected MockEndpoint mockFetchJWKS;

  @Autowired
  private JwksCache jwksCache;

  @Autowired
  private CamelContext camelContext;

  private Exchange exchange;
  private Map<String, Object> headers;
  private RSAKey rsaKey;
  private String validToken;
  private JWKSet jwkSet;
  private static final String TEST_ISSUER = "test-issuer";
  private static final String TEST_AUDIENCE = "test-audience";

  @BeforeEach
  public void setup() throws Exception {
    // Generate RSA key pair for testing
    rsaKey = new RSAKeyGenerator(2048)
        .keyID("123")
        .generate();

    // Create JWKS and cache it
    jwkSet = new JWKSet(rsaKey.toPublicJWK());
    jwksCache.updateCache(jwkSet);

    // Mock the JWKS fetch route
    AdviceWith.adviceWith(camelContext, "jwt-validation",
        advice -> {
          advice.weaveById("fetchJWKSEndpoint")
              .replace()
              .to("mock:fetchJWKS");
          advice.weaveAddLast().to("mock:result");
        });

    // Setup the mock JWKS response
    mockFetchJWKS.whenAnyExchangeReceived(exchange -> {
      exchange.setProperty("jwkSet", jwkSet);
    });

    // Create a valid JWT token
    JWSSigner signer = new RSASSASigner(rsaKey);

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("user123")
        .issuer(TEST_ISSUER)
        .audience(TEST_AUDIENCE)
        .expirationTime(new Date(new Date().getTime() + 60 * 1000)) // 1 minute expiry
        .claim("scope", "read write")
        .build();

    SignedJWT signedJWT = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
        claims
    );

    signedJWT.sign(signer);
    validToken = signedJWT.serialize();

    // Setup exchange and headers
    headers = new HashMap<>();
    headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + validToken);

    exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);

    // Setup result endpoint
    resultEndpoint.reset();

    camelContext.start();

  }

  @Test
  @Order(1)
  void testValidJwtToken() throws Exception {
    // Setup expectations
    resultEndpoint.expectedMessageCount(1);
    resultEndpoint.expectedHeaderReceived("jwt.validated", true);
    mockFetchJWKS.expectedMessageCount(1);

    // Send exchange
    producerTemplate.send(exchange);

    // Verify results
    mockFetchJWKS.assertIsSatisfied();
    resultEndpoint.assertIsSatisfied();

    // Verify JWT claims are correctly extracted
    String subject = exchange.getIn().getHeader("jwt.sub", String.class);
    String scope = exchange.getIn().getHeader("jwt.scope", String.class);
    assertThat(subject).isEqualTo("user123");
    assertThat(scope).isEqualTo("read write");
  }

  @Test
  @Order(2)
  void testInvalidJwtToken() throws Exception {
    // Setup invalid token
    headers.put(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here");
    exchange.getIn().setHeaders(headers);

    // Setup expectations
    mockFetchJWKS.expectedMessageCount(1);

    Exchange response = producerTemplate.send(exchange);

    // Verify the exception
    assertThat(response.getException()).hasMessageContaining("Invalid unsecured/JWS/JWE header: Invalid JSON object");

    mockFetchJWKS.assertIsSatisfied();
  }

  @Test
  @Order(3)
  void testMissingAuthorizationHeader() throws Exception {
    // Remove authorization header
    headers.remove(HttpHeaders.AUTHORIZATION);
    exchange.getIn().setHeaders(headers);

    // Setup expectations
    mockFetchJWKS.expectedMessageCount(1);

    Exchange response = producerTemplate.send(exchange);

    // Verify the exception
    assertThat(response.getException()).hasMessageContaining("Authorization header is missing");

    mockFetchJWKS.assertIsSatisfied();
  }

  @Test
  @Order(4)
  void testExpiredToken() throws Exception {
    // Create expired token
    JWSSigner signer = new RSASSASigner(rsaKey);

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("user123")
        .issuer(TEST_ISSUER)
        .audience(TEST_AUDIENCE)
        .expirationTime(new Date(new Date().getTime() - 60 * 1000)) // expired 1 minute ago
        .build();

    SignedJWT signedJWT = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
        claims
    );

    signedJWT.sign(signer);
    String expiredToken = signedJWT.serialize();

    // Setup exchange with expired token
    headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken);
    exchange.getIn().setHeaders(headers);

    // Setup expectations
    mockFetchJWKS.expectedMessageCount(1);

    Exchange response = producerTemplate.send(exchange);
    // Verify the exception
    assertThat(response.getException()).hasMessageContaining("Expired JWT");
    mockFetchJWKS.assertIsSatisfied();
  }

  @Test
  @Order(5)
  void testInvalidIssuer() throws Exception {
    // Create token with invalid issuer
    JWSSigner signer = new RSASSASigner(rsaKey);

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("user123")
        .issuer("wrong-issuer")
        .audience(TEST_AUDIENCE)
        .expirationTime(new Date(new Date().getTime() + 60 * 1000))
        .build();

    SignedJWT signedJWT = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
        claims
    );

    signedJWT.sign(signer);
    String tokenWithInvalidIssuer = signedJWT.serialize();

    // Setup exchange with invalid issuer token
    headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithInvalidIssuer);
    exchange.getIn().setHeaders(headers);

    // Setup expectations
    mockFetchJWKS.expectedMessageCount(1);

    Exchange response = producerTemplate.send(exchange);

    // Verify the exception
    assertThat(response.getException()).hasMessageContaining("Invalid token issuer");

    mockFetchJWKS.assertIsSatisfied();
  }

  @Test
  @Order(6)
  void testInvalidAudience() throws Exception {
    // Create token with invalid audience
    JWSSigner signer = new RSASSASigner(rsaKey);

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject("user123")
        .issuer(TEST_ISSUER)
        .audience("wrong-audience")
        .expirationTime(new Date(new Date().getTime() + 60 * 1000))
        .build();

    SignedJWT signedJWT = new SignedJWT(
        new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
        claims
    );

    signedJWT.sign(signer);
    String tokenWithInvalidAudience = signedJWT.serialize();

    // Setup exchange with invalid audience token
    headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + tokenWithInvalidAudience);
    exchange.getIn().setHeaders(headers);

    // Setup expectations
    mockFetchJWKS.expectedMessageCount(1);

    Exchange response = producerTemplate.send(exchange);
    // Verify the exception
    assertThat(response.getException()).hasMessageContaining("Invalid token audience");

    mockFetchJWKS.assertIsSatisfied();
  }

  @Test
  @Order(7)
  void testMissingJwks() throws Exception {
    // Setup mock to return null JWKS
    mockFetchJWKS.whenAnyExchangeReceived(exchange -> {
      exchange.setProperty("jwkSet", null);
    });

    // Setup expectations
    mockFetchJWKS.expectedMessageCount(1);

    Exchange response = producerTemplate.send(exchange);

    // Verify the exception
    assertThat(response.getException()).hasMessageContaining("JWKS not available");

    mockFetchJWKS.assertIsSatisfied();
  }
}