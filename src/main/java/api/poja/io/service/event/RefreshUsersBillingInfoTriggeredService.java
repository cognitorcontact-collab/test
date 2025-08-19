package api.poja.io.service.event;

import api.poja.io.endpoint.event.EventProducer;
import api.poja.io.endpoint.event.model.RefreshUserBillingInfoRequested;
import api.poja.io.endpoint.event.model.RefreshUsersBillingInfoTriggered;
import api.poja.io.model.User;
import api.poja.io.service.UserService;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class RefreshUsersBillingInfoTriggeredService
    implements Consumer<RefreshUsersBillingInfoTriggered> {
  private final UserService userService;
  private final EventProducer<RefreshUserBillingInfoRequested> eventProducer;

  @Override
  public void accept(RefreshUsersBillingInfoTriggered refreshUsersBillingInfoTriggered) {
    List<User> allUsers =
        userService.findAllToComputeBillingFor(
            refreshUsersBillingInfoTriggered.getPricingCalculationRequestEndTime(),
            refreshUsersBillingInfoTriggered.getUtcLocalDate().minusDays(1));
    log.info(
        "initiating billings refresh for {} users for date {}",
        allUsers.size(),
        refreshUsersBillingInfoTriggered.getUtcLocalDate());
    eventProducer.accept(
        allUsers.stream()
            .map(u -> toRefreshUserBillingInfoRequested(u, refreshUsersBillingInfoTriggered))
            .toList());
  }

  private static RefreshUserBillingInfoRequested toRefreshUserBillingInfoRequested(
      User user, RefreshUsersBillingInfoTriggered parent) {
    return new RefreshUserBillingInfoRequested(user.getId(), parent, user.getPricingMethod());
  }
}
