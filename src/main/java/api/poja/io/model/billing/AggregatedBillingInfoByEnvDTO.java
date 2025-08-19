package api.poja.io.model.billing;

import java.math.BigDecimal;
import java.time.Instant;

public record AggregatedBillingInfoByEnvDTO(
    BigDecimal amount,
    Instant maxComputedatetime,
    Double computedDurationInMinutes,
    String userId,
    String appId,
    String envId)
    implements AggregatedBillingInfoByEnvDTOProjection {
  public AggregatedBillingInfoByEnvDTO(
      BigDecimal amount,
      Instant maxComputedatetime,
      Double computedDurationInMinutes,
      String userId,
      String appId,
      String envId) {
    this.amount = amount == null ? BigDecimal.ZERO : amount;
    this.maxComputedatetime = maxComputedatetime;
    this.computedDurationInMinutes = computedDurationInMinutes;
    this.userId = userId;
    this.envId = envId;
    this.appId = appId;
  }

  @Override
  public BigDecimal getAmount() {
    return amount;
  }

  @Override
  public Instant getMaxComputeDatetime() {
    return maxComputedatetime;
  }

  @Override
  public Double getComputedDurationInMinutes() {
    return computedDurationInMinutes;
  }

  @Override
  public String getUserId() {
    return userId;
  }

  @Override
  public String getEnvId() {
    return envId;
  }

  @Override
  public String getAppId() {
    return appId;
  }
}
