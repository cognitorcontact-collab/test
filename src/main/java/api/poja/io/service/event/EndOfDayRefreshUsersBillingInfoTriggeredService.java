package api.poja.io.service.event;

import api.poja.io.endpoint.event.model.EndOfDayRefreshUsersBillingInfoTriggered;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EndOfDayRefreshUsersBillingInfoTriggeredService
    implements Consumer<EndOfDayRefreshUsersBillingInfoTriggered> {
  private final RefreshUsersBillingInfoTriggeredService refreshUserBillingInfoRequestedService;

  @Override
  public void accept(
      EndOfDayRefreshUsersBillingInfoTriggered endOfDayRefreshUsersBillingInfoTriggered) {
    refreshUserBillingInfoRequestedService.accept(
        endOfDayRefreshUsersBillingInfoTriggered.asRefreshUsersBillingInfoTriggered());
  }
}
