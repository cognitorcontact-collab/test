package api.poja.io.service.userStatusUpdate;

import static api.poja.io.endpoint.event.model.ConsoleUserStatusUpdateRequested.StatusAlteration.ACTIVATE;
import static api.poja.io.endpoint.rest.model.User.StatusEnum.ACTIVE;

import api.poja.io.endpoint.event.EventProducer;
import api.poja.io.endpoint.event.model.AppStatusUpdateRequested;
import api.poja.io.endpoint.event.model.ConsoleUserStatusUpdateRequested;
import api.poja.io.endpoint.event.model.PojaEvent;
import api.poja.io.endpoint.event.model.UserStatusUpdateRequested;
import api.poja.io.repository.model.Application;
import api.poja.io.service.ApplicationService;
import api.poja.io.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserActivationRequestedService implements Consumer<UserStatusUpdateRequested> {
  private final ApplicationService applicationService;
  private final EventProducer<PojaEvent> eventProducer;
  private final UserService userService;

  private static List<PojaEvent> getActivationEvents(
      UserStatusUpdateRequested userStatusUpdateRequested, List<Application> apps) {
    var appSuspensionEvents = getAppActivationEvents(userStatusUpdateRequested, apps);
    var consoleUserActivationEvent =
        getConsoleUserActivationEvent(userStatusUpdateRequested.getUserId());

    List<PojaEvent> suspensionEvents = new ArrayList<>(appSuspensionEvents);
    suspensionEvents.add(consoleUserActivationEvent);
    return suspensionEvents;
  }

  private static PojaEvent getConsoleUserActivationEvent(String userId) {
    return ConsoleUserStatusUpdateRequested.builder().userId(userId).status(ACTIVATE).build();
  }

  private static List<PojaEvent> getAppActivationEvents(
      UserStatusUpdateRequested userStatusUpdateRequested, List<Application> apps) {
    return apps.stream()
        .map(app -> toAppStatusUpdateRequested(userStatusUpdateRequested.getUserId(), app))
        .toList();
  }

  private static PojaEvent toAppStatusUpdateRequested(String userId, Application app) {
    return AppStatusUpdateRequested.builder()
        .userId(userId)
        .appId(app.getId())
        .status(AppStatusUpdateRequested.StatusAlteration.ACTIVATE)
        .build();
  }

  @Override
  public void accept(UserStatusUpdateRequested userStatusUpdateRequested) {
    var user = userService.getUserById(userStatusUpdateRequested.getUserId());
    if (ACTIVE.equals(user.getStatus())) {
      return;
    }
    assert UserStatusUpdateRequested.StatusAlteration.ACTIVATE.equals(
        userStatusUpdateRequested.getStatus());
    userService.updateUserStatus(
        userStatusUpdateRequested.getUserId(),
        ACTIVE,
        userStatusUpdateRequested.getStatusReason(),
        userStatusUpdateRequested.getRequestedAt());
    List<Application> apps =
        applicationService.findAllNotArchivedAndSuspendedByUserId(
            userStatusUpdateRequested.getUserId());
    eventProducer.accept(getActivationEvents(userStatusUpdateRequested, apps));
  }
}
