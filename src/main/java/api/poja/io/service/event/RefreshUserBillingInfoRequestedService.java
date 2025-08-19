package api.poja.io.service.event;

import static api.poja.io.repository.model.enums.BillingInfoComputeStatus.FINISHED;
import static java.math.BigDecimal.ZERO;
import static java.time.Instant.now;

import api.poja.io.endpoint.event.EventProducer;
import api.poja.io.endpoint.event.model.RefreshOrgBillingInfoRequested;
import api.poja.io.endpoint.event.model.RefreshUserBillingInfoRequested;
import api.poja.io.repository.model.BillingInfo;
import api.poja.io.repository.model.Organization;
import api.poja.io.service.BillingInfoService;
import api.poja.io.service.UserStatusEventProducerService;
import api.poja.io.service.organization.OrganizationService;
import java.time.Instant;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RefreshUserBillingInfoRequestedService
    implements Consumer<RefreshUserBillingInfoRequested> {
  private final EventProducer<RefreshOrgBillingInfoRequested> eventProducer;
  private final OrganizationService organizationService;
  private final BillingInfoService billingInfoService;
  private final UserStatusEventProducerService statusEventProducerService;

  @Override
  public void accept(RefreshUserBillingInfoRequested refreshUserBillingInfoRequested) {
    String userId = refreshUserBillingInfoRequested.getUserId();
    var orgs = organizationService.findAllByOwnerId(userId);
    if (orgs.isEmpty()) {
      Instant computeDatetime = now();
      billingInfoService.crupdateBillingInfo(
          BillingInfo.builder()
              .id(refreshUserBillingInfoRequested.getId().toString())
              .userId(userId)
              .computationIntervalEnd(
                  refreshUserBillingInfoRequested.getPricingCalculationRequestEndTime())
              .computeDatetime(computeDatetime)
              .computedPriceInUsd(ZERO)
              .appId(null)
              .envId(null)
              .orgId(null)
              .queryId(null)
              .status(FINISHED)
              .pricingMethod(refreshUserBillingInfoRequested.getPricingMethod())
              .build());
      statusEventProducerService.fireUserStatusRefreshEvent(computeDatetime, userId);
      return;
    }
    eventProducer.accept(
        orgs.stream()
            .map(o -> toRefreshOrgBillingInfoRequested(o, refreshUserBillingInfoRequested))
            .toList());
  }

  private static RefreshOrgBillingInfoRequested toRefreshOrgBillingInfoRequested(
      Organization organization, RefreshUserBillingInfoRequested parent) {
    return new RefreshOrgBillingInfoRequested(
        organization.getOwnerId(), organization.getId(), parent);
  }
}
