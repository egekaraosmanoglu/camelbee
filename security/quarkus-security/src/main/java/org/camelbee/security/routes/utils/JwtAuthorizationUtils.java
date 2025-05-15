package org.camelbee.security.routes.utils;

import java.util.Arrays;
import java.util.List;
import org.apache.camel.Exchange;
import org.camelbee.security.routes.exception.InsufficientPrivilegesException;

/**
 * Utility class for JWT authorization operations.
 * Provides methods to check roles and scopes from JWT tokens in Camel exchanges.
 */
public class JwtAuthorizationUtils {

  private JwtAuthorizationUtils() {
    // Private constructor to prevent instantiation
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
   * Bean method to check for a required role and throw an exception if missing.
   * Designed for use with Camel bean component.
   *
   * @param exchange the Camel exchange
   * @param role     the required role
   */
  public static void requireRole(Exchange exchange, String role) {
    if (!hasRole(exchange, role)) {
      throw new InsufficientPrivilegesException(
          "ERROR-AUTH010", "Insufficient privileges - missing role: " + role
      );
    }
  }

  /**
   * Bean method to check for a required scope and throw an exception if missing.
   * Designed for use with Camel bean component.
   *
   * @param exchange the Camel exchange
   * @param scope    the required scope
   */
  public static void requireScope(Exchange exchange, String scope) {
    if (!hasScope(exchange, scope)) {
      throw new InsufficientPrivilegesException(
          "ERROR-AUTH011", "Insufficient privileges - missing scope: " + scope
      );
    }
  }

  /**
   * Bean method to check for both a required role and scope.
   * Designed for use with Camel bean component.
   *
   * @param exchange the Camel exchange
   * @param role     the required role
   * @param scope    the required scope
   */
  public static void requireRoleAndScope(Exchange exchange, String role, String scope) {
    if (!hasRole(exchange, role) || !hasScope(exchange, scope)) {
      throw new InsufficientPrivilegesException(
          "ERROR-AUTH012", "Insufficient privileges - requires role and scope"
      );
    }
  }
}