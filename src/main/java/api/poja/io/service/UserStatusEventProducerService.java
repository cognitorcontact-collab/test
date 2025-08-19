package api.poja.io.service;

import static api.poja.io.endpoint.rest.model.User.StatusEnum.UNDER_MODIFICATION;
import static api.poja.io.service.event.RefreshUserStatusRequestedService.THIRTY_DAYS;
import static java.time.ZoneOffset.UTC;

import api.poja.io.endpoint.event.EventProducer;
import api.poja.io.endpoint.event.model.RefreshUserStatusRequested;
import api.poja.io.model.User;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserStatusEventProducerService {

  private final EventProducer<RefreshUserStatusRequested> eventProducer;
  private final UserService userService;

  public void fireUserStatusRefreshEvent(Instant computationRequestEndDatetime, String userId) {
    User user = userService.getUserById(userId);
    if (UNDER_MODIFICATION.equals(user.getStatus())) {
      return;
    }
    // Set user status to UNDER_MODIFICATION — a transitional state, not a final status.
    // We keep the existing status_updated_at to avoid breaking duration tracking for the current
    // status.
    // For example, if the user is currently ACTIVE and remains ACTIVE after the transition,
    // we want the ACTIVE duration to remain accurate, as it will be computed from this timestamp.
    // Only when the final (non-transitional) status changes should status_updated_at be updated.
    userService.updateUserStatus(
        userId, UNDER_MODIFICATION, user.getStatusReason(), user.getStatusUpdatedAt());
    log.info("fire user status refresh event for {}", userId);
    eventProducer.accept(
        List.of(
            RefreshUserStatusRequested.builder()
                .pricingCalculationRequestStartTime(
                    computationRequestEndDatetime.atZone(UTC).minus(THIRTY_DAYS).toInstant())
                .userId(userId)
                .pricingCalculationRequestEndTime(computationRequestEndDatetime)
                .build()));
  }
}
