package org.camelbee.security.routes.routes;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.camelbee.security.routes.exception.InvalidRequestException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Camel route for JWT validation.
 * Validates JWTs using JWKS and extracts roles and scopes based on configured claim paths.
 */
@ApplicationScoped
@IfBuildProperty(name = "camelbee.security.enabled", stringValue = "true")
public class JwtValidationRoute extends RouteBuilder {

  private static final Logger log = LoggerFactory.getLogger(JwtValidationRoute.class);

  @ConfigProperty(name = "camelbee.security.issuer", defaultValue = "test-issuer")
  String jwkIssuer;

  @ConfigProperty(name = "camelbee.security.audience", defaultValue = "test-audience")
  String jwkAudience;

  @ConfigProperty(name = "camelbee.security.role-claims")
  List<String> roleClaimPaths;

  @ConfigProperty(name = "camelbee.security.scope-claims", defaultValue = "scope,scp,scopes")
  List<String> scopeClaimPaths;

  @ConfigProperty(name = "camelbee.security.algorithm", defaultValue = "RS256")
  String algorithm;

  @ConfigProperty(name = "camelbee.security.clock-skew", defaultValue = "30")
  private int clockSkewSeconds;

  /**
   * Configures the JWT validation route.
   * This route expects a JWT token in the Authorization header and validates it against
   * configured JWKS. After successful validation, it extracts roles and scopes from the
   * token claims and stores them in exchange properties for downstream processing.
   *
   * @throws Exception if route configuration fails
   */
  @Override
  public void configure() throws Exception {
    from("direct:validateJWT")
        .id("jwt-validation")
        .errorHandler(noErrorHandler())
        .to("direct:fetchJWKS").id("fetchJWKSEndpoint")
        .process(exchange -> {
          String token = extractToken(exchange);
          JWKSet jwkSet = exchange.getProperty("jwkSet", JWKSet.class);
          if (jwkSet == null) {
            throw new InvalidRequestException("ERROR-AUTH002", "JWKS not available");
          }

          // Validate token using Nimbus
          ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
          JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
              JWSAlgorithm.parse(algorithm),
              new ImmutableJWKSet<>(jwkSet)
          );
          jwtProcessor.setJWSKeySelector(keySelector);

          JWTClaimsSet claims = jwtProcessor.process(token, null);
          validateClaims(claims);

          // Store claims
          exchange.setProperty("jwt.claims", claims);
          exchange.setProperty("jwt.iss", claims.getIssuer());
          exchange.setProperty("jwt.sub", claims.getSubject());
          exchange.setProperty("jwt.exp", claims.getExpirationTime());
          exchange.setProperty("jwt.iat", claims.getIssueTime());
          exchange.setProperty("jwt.nbf", claims.getNotBeforeTime());
          exchange.setProperty("jwt.jti", claims.getJWTID());

          // Extract roles and scopes
          List<String> roles = extractRoles(claims);
          List<String> scopes = extractScopes(claims);

          exchange.setProperty("jwt.roles", roles);
          exchange.setProperty("jwt.scopes", scopes);
          exchange.setProperty("jwt.validated", true);

          log.debug("JWT validated successfully. Subject: {}, Roles: {}, Scopes: {}",
              claims.getSubject(), roles, scopes);

          // Optionally return a success flag
          exchange.getIn().setBody(true);
        });
  }

  private String extractToken(Exchange exchange) {
    String token = exchange.getIn().getHeader("Authorization", String.class);
    if (token != null && token.startsWith("Bearer ")) {
      return token.substring(7);
    }
    throw new InvalidRequestException("ERROR-AUTH001", "Authorization header is missing or malformed");
  }

  private void validateClaims(JWTClaimsSet claims) {
    long now = System.currentTimeMillis();
    long skewMillis = clockSkewSeconds * 1000L;

    // Validate issuer
    if (!jwkIssuer.equals(claims.getIssuer())) {
      throw new InvalidRequestException("ERROR-AUTH003", "Invalid token issuer");
    }

    // Validate audience
    List<String> audience = claims.getAudience();
    if (audience == null || audience.isEmpty() || !jwkAudience.equals(audience.getFirst())) {
      throw new InvalidRequestException("ERROR-AUTH004", "Invalid token audience");
    }

    // Validate expiration with skew
    Date exp = claims.getExpirationTime();
    if (exp != null && now > (exp.getTime() + skewMillis)) {
      throw new InvalidRequestException("ERROR-AUTH005", "Token has expired");
    }

    // Validate not-before with skew
    Date nbf = claims.getNotBeforeTime();
    if (nbf != null && now < (nbf.getTime() - skewMillis)) {
      throw new InvalidRequestException("ERROR-AUTH006", "Token not yet valid");
    }
  }

  private List<String> extractRoles(JWTClaimsSet claims) {
    Map<String, Object> allClaims = claims.getClaims();
    Set<String> roles = new HashSet<>();

    log.debug("Extracting roles from claims with paths: {}", roleClaimPaths);

    for (String path : roleClaimPaths) {
      Object value = getNestedClaimValue(allClaims, path);

      if (value != null) {
        log.debug("Found value for role path '{}': {}", path, value);

        if (value instanceof List<?>) {
          for (Object item : (List<?>) value) {
            roles.add(String.valueOf(item));
          }
        } else if (value instanceof String) {
          roles.addAll(Arrays.asList(((String) value).split(" ")));
        } else if (value instanceof Map<?, ?> map) {
          Object nested = map.get("roles");
          if (nested instanceof List<?>) {
            for (Object r : (List<?>) nested) {
              roles.add(String.valueOf(r));
            }
          }
        }
      }
    }

    log.debug("Extracted roles: {}", roles);
    return new ArrayList<>(roles);
  }

  private List<String> extractScopes(JWTClaimsSet claims) {
    Map<String, Object> allClaims = claims.getClaims();
    Set<String> scopes = new HashSet<>();

    log.debug("Extracting scopes from claims with paths: {}", scopeClaimPaths);

    for (String path : scopeClaimPaths) {
      Object value = getNestedClaimValue(allClaims, path);

      if (value != null) {
        log.debug("Found value for scope path '{}': {}", path, value);

        if (value instanceof String str) {
          scopes.addAll(Arrays.asList(str.split(" ")));
        } else if (value instanceof List<?>) {
          for (Object item : (List<?>) value) {
            scopes.add(String.valueOf(item));
          }
        }
      }
    }

    log.debug("Extracted scopes: {}", scopes);
    return new ArrayList<>(scopes);
  }

  private Object getNestedClaimValue(Map<String, Object> claims, String path) {
    String[] parts = path.split("\\.");
    Object current = claims;

    for (String part : parts) {
      if (!(current instanceof Map<?, ?> map)) {
        return null;
      }
      current = map.get(part);
      if (current == null) {
        return null;
      }
    }

    return current;
  }

  /**
   * Checks if the JWT token in the exchange has a specific role.
   *
   * @param exchange     the Camel exchange containing JWT properties
   * @param requiredRole the role to check for
   * @return true if the token has the specified role, false otherwise
   */
  public static boolean hasRole(Exchange exchange, String requiredRole) {
    @SuppressWarnings("unchecked")
    List<String> roles = exchange.getProperty("jwt.roles", List.class);
    return roles != null && roles.contains(requiredRole);
  }

  /**
   * Checks if the JWT token in the exchange has any of the specified roles.
   *
   * @param exchange      the Camel exchange containing JWT properties
   * @param requiredRoles array of roles to check for
   * @return true if the token has at least one of the specified roles, false otherwise
   */
  public static boolean hasAnyRole(Exchange exchange, String... requiredRoles) {
    @SuppressWarnings("unchecked")
    List<String> roles = exchange.getProperty("jwt.roles", List.class);
    if (roles == null) {
      return false;
    }
    for (String requiredRole : requiredRoles) {
      if (roles.contains(requiredRole)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the JWT token in the exchange has all of the specified roles.
   *
   * @param exchange      the Camel exchange containing JWT properties
   * @param requiredRoles array of roles to check for
   * @return true if the token has all of the specified roles, false otherwise
   */
  public static boolean hasAllRoles(Exchange exchange, String... requiredRoles) {
    @SuppressWarnings("unchecked")
    List<String> roles = exchange.getProperty("jwt.roles", List.class);
    if (roles == null) {
      return false;
    }
    return Arrays.stream(requiredRoles).allMatch(roles::contains);
  }

  /**
   * Checks if the JWT token in the exchange has a specific scope.
   *
   * @param exchange      the Camel exchange containing JWT properties
   * @param requiredScope the scope to check for
   * @return true if the token has the specified scope, false otherwise
   */
  public static boolean hasScope(Exchange exchange, String requiredScope) {
    @SuppressWarnings("unchecked")
    List<String> scopes = exchange.getProperty("jwt.scopes", List.class);
    return scopes != null && scopes.contains(requiredScope);
  }

  /**
   * Checks if the JWT token in the exchange has any of the specified scopes.
   *
   * @param exchange       the Camel exchange containing JWT properties
   * @param requiredScopes array of scopes to check for
   * @return true if the token has at least one of the specified scopes, false otherwise
   */
  public static boolean hasAnyScope(Exchange exchange, String... requiredScopes) {
    @SuppressWarnings("unchecked")
    List<String> scopes = exchange.getProperty("jwt.scopes", List.class);
    if (scopes == null) {
      return false;
    }
    for (String requiredScope : requiredScopes) {
      if (scopes.contains(requiredScope)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the JWT token in the exchange has all of the specified scopes.
   *
   * @param exchange       the Camel exchange containing JWT properties
   * @param requiredScopes array of scopes to check for
   * @return true if the token has all of the specified scopes, false otherwise
   */
  public static boolean hasAllScopes(Exchange exchange, String... requiredScopes) {
    @SuppressWarnings("unchecked")
    List<String> scopes = exchange.getProperty("jwt.scopes", List.class);
    if (scopes == null) {
      return false;
    }
    return Arrays.stream(requiredScopes).allMatch(scopes::contains);
  }

  /**
   * Retrieves all JWT-related properties from the exchange.
   * Properties are identified by the "jwt." prefix in their names.
   *
   * @param exchange the Camel exchange to extract JWT properties from
   * @return a map containing all JWT properties (without the "jwt." prefix as keys)
   */
  public static Map<String, Object> getJwtProperties(Exchange exchange) {
    Map<String, Object> jwtProps = new HashMap<>();
    exchange.getProperties().forEach((key, value) -> {
      if (key.startsWith("jwt.")) {
        jwtProps.put(key, value);
      }
    });
    return jwtProps;
  }
}