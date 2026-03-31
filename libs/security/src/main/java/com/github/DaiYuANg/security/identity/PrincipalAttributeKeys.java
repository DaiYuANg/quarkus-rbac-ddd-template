package com.github.DaiYuANg.security.identity;

import lombok.experimental.UtilityClass;

/**
 * Stable string keys shared across JWT custom claims, {@link io.quarkus.security.identity
 * .SecurityIdentity} attributes, Valkey authority snapshots, and serialized principal maps.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@UtilityClass
public class PrincipalAttributeKeys {

  /** Serialized principal subject; distinct from JWT {@code sub} in some maps. */
  public final String SUBJECT = "subject";

  public final String USERNAME = "username";
  public final String USER_ID = "userId";

  /** Login / identity provider id (e.g. super-admin, db-user). */
  public final String PROVIDER_ID = "providerId";

  /**
   * Raw nickname from the account backing store (DB user nickname); optional extra on top of {@link
   * #DISPLAY_NAME}.
   */
  public final String NICKNAME = "nickname";

  public final String DISPLAY_NAME = "displayName";
  public final String USER_TYPE = "userType";
  public final String ROLES = "roles";
  public final String PERMISSIONS = "permissions";
  public final String AUTHORITY_VERSION = "authorityVersion";

  /** Provenance, e.g. {@code db} vs {@code config}. */
  public final String SOURCE = "source";
}
