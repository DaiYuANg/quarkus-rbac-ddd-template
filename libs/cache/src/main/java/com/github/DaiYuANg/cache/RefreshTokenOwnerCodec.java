package com.github.DaiYuANg.cache;

import com.google.common.base.Strings;
import java.util.Objects;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
class RefreshTokenOwnerCodec {
  private static final String OWNER_SEPARATOR = "\t";

  String encode(Long userId, String username) {
    return Strings.nullToEmpty(Objects.toString(userId, null))
        + OWNER_SEPARATOR
        + Strings.nullToEmpty(normalize(username));
  }

  Optional<RefreshTokenStore.RefreshTokenOwner> decode(String rawValue) {
    val normalizedValue = normalize(rawValue);
    if (normalizedValue == null) {
      return Optional.empty();
    }
    if (!normalizedValue.contains(OWNER_SEPARATOR)) {
      return Optional.of(new RefreshTokenStore.RefreshTokenOwner(null, normalizedValue));
    }
    val parts = normalizedValue.split(OWNER_SEPARATOR, 2);
    val userId = parseUserId(parts[0]);
    val username = parts.length < 2 ? null : normalize(parts[1]);
    if (userId == null && username == null) {
      return Optional.empty();
    }
    return Optional.of(new RefreshTokenStore.RefreshTokenOwner(userId, username));
  }

  String normalize(String value) {
    return StringUtils.trimToNull(value);
  }

  private Long parseUserId(String rawValue) {
    val normalizedValue = normalize(rawValue);
    if (normalizedValue == null) {
      return null;
    }
    try {
      return Long.parseLong(normalizedValue);
    } catch (NumberFormatException ignored) {
      return null;
    }
  }
}
