package org.camelbee.security.routes.routes;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.security.routes.exception.InvalidRequestException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Camel route for JWT validation.
 * This route handles the validation of JWT tokens using JWKS (JSON Web Key Sets).
 * It performs various validation steps including signature verification, issuer and audience validation.
 */
@ApplicationScoped
public class JwtValidationRoute extends RouteBuilder {

  @ConfigProperty(name = "camelbee.security.issuer", defaultValue = "test-issuer")
  String jwkIssuer;

  @ConfigProperty(name = "camelbee.security.audience", defaultValue = "test-audience")
  String jwkAudience;

  /**
   * Configures the JWT validation route.
   * The route performs the following steps:
   *
   * @throws Exception if route configuration fails or during token validation
   */
  @Override
  public void configure() throws Exception {
    from("direct:validateJWT")
        .id("jwt-validation")
        .errorHandler(noErrorHandler())
        .to("direct:fetchJWKS").id("fetchJWKSEndpoint")
        .process(exchange -> {
          String token = exchange.getIn().getHeader("Authorization", String.class);
          if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
          } else {
            throw new InvalidRequestException("ERROR-AUTH001", "Authorization header is missing");
          }

          JWKSet jwkSet = exchange.getProperty("jwkSet", JWKSet.class);
          if (jwkSet == null) {
            throw new InvalidRequestException("ERROR-AUTH002", "JWKS not available");
          }

          // Configure JWT processor
          ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
          JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
              JWSAlgorithm.RS256,
              new ImmutableJWKSet<>(jwkSet)
          );
          jwtProcessor.setJWSKeySelector(keySelector);

          // Validate token
          var claims = jwtProcessor.process(token, null);

          // Validate issuer
          String tokenIssuer = claims.getIssuer();
          if (!jwkIssuer.equals(tokenIssuer)) {
            throw new InvalidRequestException("ERROR-AUTH003", "Invalid token issuer");
          }

          // Validate audience
          String tokenAudience = claims.getAudience().get(0);
          if (!jwkAudience.equals(tokenAudience)) {
            throw new InvalidRequestException("ERROR-AUTH004", "Invalid token audience");
          }

          // Set validated claims in headers
          exchange.getIn().setHeader("jwt.sub", claims.getSubject());
          exchange.getIn().setHeader("jwt.scope", claims.getStringClaim("scope"));
          exchange.getIn().setHeader("jwt.validated", true);

          // Set success response
          exchange.getIn().setBody(true);
        });
  }
}