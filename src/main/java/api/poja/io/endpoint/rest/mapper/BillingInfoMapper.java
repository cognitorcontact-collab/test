package api.poja.io.endpoint.rest.mapper;

import static api.poja.io.endpoint.rest.model.DurationUnit.MINUTES;

import api.poja.io.endpoint.rest.model.Duration;
import api.poja.io.endpoint.rest.model.EnvBillingInfo;
import api.poja.io.endpoint.rest.model.OrgBillingInfo;
import api.poja.io.endpoint.rest.model.UserBillingInfo;
import api.poja.io.repository.model.BillingInfo;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class BillingInfoMapper {
  public EnvBillingInfo toEnvRest(BillingInfo domain, Instant startTime, Instant endTime) {
    var duration = new Duration().amount(domain.getComputedDurationInMinutes()).unit(MINUTES);
    return new EnvBillingInfo()
        .startTime(startTime)
        .endTime(endTime)
        .appId(domain.getAppId())
        .envId(domain.getEnvId())
        .computedPrice(domain.getComputedPriceInUsd())
        .pricingMethod(domain.getPricingMethod().getName())
        .computeTime(domain.getComputeDatetime())
        .resourceInvocationTotalDuration(duration);
  }

  public UserBillingInfo toUserRest(BillingInfo domain, Instant startTime, Instant endTime) {
    var duration = new Duration().amount(domain.getComputedDurationInMinutes()).unit(MINUTES);
    return new UserBillingInfo()
        .startTime(startTime)
        .endTime(endTime)
        .userId(domain.getUserId())
        .computedPrice(domain.getComputedPriceInUsd())
        .pricingMethod(domain.getPricingMethod().getName())
        .computeTime(domain.getComputeDatetime())
        .resourceInvocationTotalDuration(duration);
  }

  public OrgBillingInfo toOrgRest(BillingInfo domain, Instant startTime, Instant endTime) {
    var duration = new Duration().amount(domain.getComputedDurationInMinutes()).unit(MINUTES);
    return new OrgBillingInfo()
        .startTime(startTime)
        .endTime(endTime)
        .userId(domain.getUserId())
        .orgId(domain.getOrgId())
        .computedPrice(domain.getComputedPriceInUsd())
        .pricingMethod(domain.getPricingMethod().getName())
        .computeTime(domain.getComputeDatetime())
        .resourceInvocationTotalDuration(duration);
  }
}
