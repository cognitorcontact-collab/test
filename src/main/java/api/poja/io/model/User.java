package api.poja.io.model;

import static api.poja.io.endpoint.rest.model.User.StatusEnum.SUSPENDED;
import static java.time.Duration.ZERO;
import static java.time.Instant.now;

import api.poja.io.endpoint.rest.model.User.StatusEnum;
import api.poja.io.endpoint.rest.security.model.UserRole;
import api.poja.io.service.pricing.PricingMethod;
import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Slf4j
public class User {
  private String id;

  private String firstName;

  private String lastName;

  private String username;

  private String email;

  private UserRole[] roles;

  private String githubId;

  private String avatar;

  private String stripeId;

  private PricingMethod pricingMethod;
  private StatusEnum status;
  private String statusReason;

  private boolean betaTester;
  private boolean isEndToEndTestUser;
  private String mainOrgId;
  private boolean archived;

  private Instant joinedAt;
  private Instant statusUpdatedAt;
  private String activeSubscriptionId;
  private String latestSubscriptionId;

  public Duration suspensionDurationInSeconds() {
    if (!SUSPENDED.equals(status) || null == statusUpdatedAt) {
      return ZERO;
    }
    return Duration.between(now(), statusUpdatedAt);
  }
}
