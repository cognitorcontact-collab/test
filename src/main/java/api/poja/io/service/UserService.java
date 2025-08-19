package api.poja.io.service;

import static api.poja.io.endpoint.event.model.UserStatusUpdateRequested.StatusAlteration.ACTIVATE;
import static api.poja.io.endpoint.event.model.UserStatusUpdateRequested.StatusAlteration.SUSPEND;
import static api.poja.io.endpoint.rest.model.User.StatusEnum.SUSPENDED;
import static api.poja.io.endpoint.rest.model.User.StatusEnum.UNDER_MODIFICATION;
import static java.time.Instant.now;

import api.poja.io.endpoint.event.EventProducer;
import api.poja.io.endpoint.event.model.PojaEvent;
import api.poja.io.endpoint.event.model.UserArchivalRequested;
import api.poja.io.endpoint.event.model.UserStatusUpdateRequested;
import api.poja.io.endpoint.event.model.UserUpserted;
import api.poja.io.endpoint.rest.model.CreateUser;
import api.poja.io.endpoint.rest.model.User.StatusEnum;
import api.poja.io.endpoint.rest.model.UserStatistics;
import api.poja.io.endpoint.rest.security.github.GithubComponent;
import api.poja.io.model.BoundedPageSize;
import api.poja.io.model.PageFromOne;
import api.poja.io.model.User;
import api.poja.io.model.UserWithLatestOrgInviteDTO;
import api.poja.io.model.exception.BadRequestException;
import api.poja.io.model.exception.ForbiddenException;
import api.poja.io.model.exception.NotFoundException;
import api.poja.io.model.page.Page;
import api.poja.io.repository.UserRepository;
import api.poja.io.repository.model.UserSuspension;
import api.poja.io.repository.model.mapper.UserMapper;
import api.poja.io.service.stripe.StripeService;
import api.poja.io.service.user.UserConf;
import api.poja.io.service.validator.UserSuspensionValidator;
import api.poja.io.service.validator.UsersThresholdValidator;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.kohsuke.github.GHMyself;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserRepository repository;
  private final UserConf userConf;
  private final GithubComponent githubComponent;
  private final UserMapper mapper;
  private final EventProducer<PojaEvent> eventProducer;
  private final StripeService stripeService;
  private final UsersThresholdValidator thresholdValidator;
  private final UserSuspensionValidator userSuspensionValidator;
  private final UserSuspensionService userSuspensionService;

  private static PojaEvent getUserUpserted(User u) {
    return new UserUpserted(u);
  }

  public List<User> createUsers(List<CreateUser> toCreate) {
    thresholdValidator.accept(toCreate.size());
    List<User> toSave = toCreate.stream().map(this::createUserFrom).toList();
    var saved = repository.saveAll(toSave);
    eventProducer.accept(saved.stream().map(UserService::getUserUpserted).toList());
    return saved;
  }

  public List<User> findAllToBill(YearMonth yearMonth) {
    return repository.findAllToBillFor(yearMonth);
  }

  public User getUserById(String userId) {
    return findById(userId)
        .orElseThrow(
            () -> new NotFoundException("The user identified by id " + userId + " is not found"));
  }

  public List<User> findAllToComputeBillingFor(Instant computeDatetime, LocalDate dateIntervalEnd) {
    return repository.findAllToComputeBilling(computeDatetime, dateIntervalEnd);
  }

  public Optional<User> findById(String userId) {
    return repository.findById(userId, now());
  }

  private User createUserFrom(CreateUser createUser) {
    String stripeName = createUser.getFirstName() + " " + createUser.getLastName();
    GHMyself githubUser = getUserByToken(createUser.getToken());
    String customerId = stripeService.createCustomer(stripeName, createUser.getEmail()).getId();
    User user = mapper.toModel(createUser, githubUser, customerId);
    if (repository.existsByEmail(user.getEmail()))
      throw new BadRequestException("An account with the same email already exists");
    if (repository.existsByGithubId(user.getGithubId()))
      throw new BadRequestException("An account with the same github id already exists");
    return user;
  }

  private GHMyself getUserByToken(String token) {
    return githubComponent
        .getCurrentUserByToken(token)
        .orElseThrow(() -> new ForbiddenException("Invalid token"));
  }

  public User findByGithubUserId(String githubUserId) {
    return repository
        .findByGithubId(githubUserId, now())
        .orElseThrow(
            () ->
                new NotFoundException(
                    "The user identified by the github id " + githubUserId + " is not found"));
  }

  public void updateMainOrgId(String userId, String mainOrgId) {
    repository.updateMainOrgId(userId, mainOrgId);
  }

  public Page<User> getUsers(
      String username, PageFromOne pageFromOne, BoundedPageSize boundedPageSize) {
    return repository.getUsersByUsername(username, now(), pageFromOne, boundedPageSize);
  }

  public Page<UserWithLatestOrgInviteDTO> getUsers(
      String orgId, String username, PageFromOne pageFromOne, BoundedPageSize boundedPageSize) {
    return repository.getUsersByUsernameWithLastOrgInvite(
        orgId, username, now(), pageFromOne, boundedPageSize);
  }

  public void updateUserStatus(
      String userId, StatusEnum status, String statusReason, Instant statusUpdatedAt) {
    repository.updateStatus(userId, status, statusReason, statusUpdatedAt);
    saveUserSuspension(userId, status, statusReason, statusUpdatedAt);
  }

  private void saveUserSuspension(
      String userId, StatusEnum status, String reason, Instant statusUpdatedAt) {
    if (!SUSPENDED.equals(status)) {
      return;
    }
    userSuspensionService.save(
        UserSuspension.builder()
            .userId(userId)
            .suspensionReason(reason)
            .suspendedAt(statusUpdatedAt)
            .build());
  }

  public void updateUserLatestSubscriptionId(String userId, String subscriptionId) {
    repository.updateLatestSubscriptionId(userId, subscriptionId);
  }

  public UserStatistics getUserStats() {
    var userStatistics = repository.getUserStatistics();
    return new UserStatistics()
        .maxUsersNb(userConf.maxUsersNb())
        .archivedUsersNb(userStatistics.archivedCount())
        .suspendedUsersNb(userStatistics.suspendedCount())
        .usersCount(userStatistics.totalCount());
  }

  @Transactional
  public User archiveUser(String userId) {
    User user = getUserById(userId);
    if (user.isArchived()) {
      throw new BadRequestException("user is already archived");
    }
    if (UNDER_MODIFICATION.equals(user.getStatus())) {
      throw new BadRequestException("user status is still under modification.");
    }
    repository.archiveUser(userId, now());
    eventProducer.accept(List.of(new UserArchivalRequested(userId, now())));
    return getUserById(userId);
  }

  private static UserStatusUpdateRequested.StatusAlteration getStatus(StatusEnum statusEnum) {
    return switch (statusEnum) {
      case ACTIVE -> ACTIVATE;
      case UNDER_MODIFICATION -> throw new IllegalArgumentException();
      case SUSPENDED -> SUSPEND;
    };
  }

  public User updateUserStatusAsync(String userId, StatusEnum statusEnum, String statusReason) {
    User user = getUserById(userId);
    if (user.isArchived()) {
      throw new BadRequestException("user is already archived");
    }
    if (statusEnum.equals(user.getStatus())) {
      return user;
    }
    if (UNDER_MODIFICATION.equals(user.getStatus())) {
      throw new BadRequestException("user status is still under modification.");
    }

    if (SUSPENDED.equals(statusEnum)) {
      userSuspensionValidator.accept(userId, statusReason);
    }
    // Set user status to UNDER_MODIFICATION — a transitional state, not a final status.
    // We keep the existing status_updated_at to avoid breaking duration tracking for the current
    // status.
    // For example, if the user is currently ACTIVE and remains ACTIVE after the transition,
    // we want the ACTIVE duration to remain accurate, as it will be computed from this timestamp.
    // Only when the final (non-transitional) status changes should status_updated_at be updated.
    updateUserStatus(userId, UNDER_MODIFICATION, user.getStatusReason(), user.getStatusUpdatedAt());
    eventProducer.accept(
        List.of(new UserStatusUpdateRequested(userId, getStatus(statusEnum), statusReason, now())));
    return user;
  }
}
