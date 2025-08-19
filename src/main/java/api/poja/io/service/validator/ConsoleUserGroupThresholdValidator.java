package api.poja.io.service.validator;

import api.poja.io.model.exception.BadRequestException;
import api.poja.io.repository.jpa.ConsoleUserGroupRepository;
import api.poja.io.repository.model.UserSubscription;
import api.poja.io.service.UserService;
import api.poja.io.service.UserSubscriptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConsoleUserGroupThresholdValidator {
  public ConsoleUserGroupThresholdValidator(
      @Value("${max.groups.per.org}") long maxConsoleGroupsNbPerOrg,
      @Value("${e2e.max.groups.per.user}") long endToEndTestMaxConsoleGroupNbPerOrg,
      @Value("${e2e.user.main.org.id}") String endToEndTestUserMainOrgId,
      @Value("${beta.user.console.threshold}") long betaUserConsoleThreshold,
      ConsoleUserGroupRepository consoleUserGroupRepository,
      UserSubscriptionService userSubscriptionService,
      UserService userService) {
    this.normalOrgsThreshold = maxConsoleGroupsNbPerOrg;
    this.endToEndTestThreshold = endToEndTestMaxConsoleGroupNbPerOrg;
    this.endToEndTestUserMainOrg = endToEndTestUserMainOrgId;
    this.betaUserConsoleThreshold = betaUserConsoleThreshold;
    this.consoleUserGroupRepository = consoleUserGroupRepository;
    this.userSubscriptionService = userSubscriptionService;
    this.userService = userService;
  }

  private final long normalOrgsThreshold;
  private final long endToEndTestThreshold;
  private final String endToEndTestUserMainOrg;
  private final long betaUserConsoleThreshold;
  private final ConsoleUserGroupRepository consoleUserGroupRepository;
  private final UserSubscriptionService userSubscriptionService;
  private final UserService userService;

  public void accept(String orgId, String orgOwnerId) {
    if (endToEndTestUserMainOrg.equals(orgId)) {
      checkThreshold(orgId, endToEndTestThreshold);
    } else {
      var optionalActiveSubscription =
          userSubscriptionService.findActiveSubscriptionByUserId(orgOwnerId);
      if (optionalActiveSubscription.isPresent()) {
        UserSubscription orgOwnerActiveSubscription = optionalActiveSubscription.get();
        checkThresholdBySubscription(
            orgOwnerActiveSubscription.getUserId(),
            orgOwnerActiveSubscription.getOffer().getMaxUserGroups());
        return;
      }
      var orgOwner = userService.getUserById(orgOwnerId);
      if (orgOwner.isBetaTester()) {
        checkThreshold(orgId, betaUserConsoleThreshold);
        return;
      }
      checkThreshold(orgId, normalOrgsThreshold);
    }
  }

  private void checkThreshold(String orgId, long maxThreshold) {
    long userGroupsNb = consoleUserGroupRepository.countByOrgId(orgId).orElse(0L);
    if (userGroupsNb + 1 > maxThreshold) {
      throw new BadRequestException("cannot create new console user group");
    }
  }

  private void checkThresholdBySubscription(String userId, long maxThreshold) {
    long userGroupsNb = consoleUserGroupRepository.countByUserId(userId).orElse(0L);
    if (userGroupsNb + 1 > maxThreshold) {
      throw new BadRequestException("cannot create new console user group");
    }
  }
}
