package com.github.DaiYuANg.security.identity;

/**
 * Stable string keys shared across JWT custom claims, {@link io.quarkus.security.identity
 * .SecurityIdentity} attributes, Valkey authority snapshots, and serialized principal maps.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
public final class PrincipalAttributeKeys {

  private PrincipalAttributeKeys() {}

  /** Serialized principal subject; distinct from JWT {@code sub} in some maps. */
  public static final String SUBJECT = "subject";

  public static final String USERNAME = "username";

  /** Login / identity provider id (e.g. config-user, db-user). */
  public static final String PROVIDER_ID = "providerId";

  /**
   * Raw nickname from the account backing store (DB user nickname); optional extra on top of {@link
   * #DISPLAY_NAME}.
   */
  public static final String NICKNAME = "nickname";

  public static final String DISPLAY_NAME = "displayName";
  public static final String USER_TYPE = "userType";
  public static final String ROLES = "roles";
  public static final String PERMISSIONS = "permissions";
  public static final String AUTHORITY_VERSION = "authorityVersion";

  /** Provenance, e.g. {@code db} vs {@code config}. */
  public static final String SOURCE = "source";
}
