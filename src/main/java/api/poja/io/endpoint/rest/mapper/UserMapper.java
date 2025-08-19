package api.poja.io.endpoint.rest.mapper;

import static api.poja.io.repository.model.enums.OrganizationInviteStatus.ACCEPTED;

import api.poja.io.endpoint.rest.model.GetUserResponse;
import api.poja.io.endpoint.rest.model.GetUserResponse.StatusEnum;
import api.poja.io.endpoint.rest.model.User;
import api.poja.io.endpoint.rest.model.UserRoleEnum;
import api.poja.io.model.UserWithLatestOrgInviteDTO;
import api.poja.io.repository.model.UserSubscription;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component("restMapper")
@AllArgsConstructor
public class UserMapper {
  private final UserRoleMapper userRoleMapper;

  public User toRest(api.poja.io.model.User domain) {
    List<UserRoleEnum> roles = Stream.of(domain.getRoles()).map(userRoleMapper::toRest).toList();

    return new User()
        .id(domain.getId())
        .username(domain.getUsername())
        .email(domain.getEmail())
        .githubId(domain.getGithubId())
        .firstName(domain.getFirstName())
        .lastName(domain.getLastName())
        .roles(roles)
        .avatar(domain.getAvatar())
        .stripeId(domain.getStripeId())
        .isBetaTester(domain.isBetaTester())
        .status(domain.getStatus())
        .statusReason(domain.getStatusReason())
        .mainOrgId(domain.getMainOrgId())
        .joinedAt(domain.getJoinedAt())
        .isArchived(domain.isArchived())
        .statusUpdatedAt(domain.getStatusUpdatedAt())
        .activeSubscriptionId(domain.getActiveSubscriptionId())
        .latestSubscriptionId(domain.getLatestSubscriptionId())
        .suspensionDurationInSeconds(domain.suspensionDurationInSeconds().getSeconds())
        .archived(domain.isArchived());
  }

  public GetUserResponse toGetUserResponse(api.poja.io.model.User domain) {
    return new GetUserResponse()
        .id(domain.getId())
        .username(domain.getUsername())
        .email(domain.getEmail())
        .firstName(domain.getFirstName())
        .lastName(domain.getLastName())
        .avatar(domain.getAvatar())
        .status(StatusEnum.fromValue(domain.getStatus().getValue()))
        .statusReason(domain.getStatusReason())
        .activeSubscriptionId(domain.getActiveSubscriptionId())
        .latestSubscriptionId(domain.getLatestSubscriptionId())
        .statusUpdatedAt(domain.getStatusUpdatedAt())
        .suspensionDurationInSeconds(domain.suspensionDurationInSeconds().getSeconds())
        .archived(domain.isArchived());
  }

  public GetUserResponse toGetUserResponse(UserWithLatestOrgInviteDTO domain) {
    var latestInvite = domain.latestInvite();
    var isOrgMember = latestInvite != null && latestInvite.getStatus().equals(ACCEPTED);
    var canBeInvitedToMoreOrg = true;

    UserSubscription activeSubscription = domain.activeSubscription();
    return new GetUserResponse()
        .id(domain.id())
        .username(domain.username())
        .email(domain.email())
        .firstName(domain.firstName())
        .lastName(domain.lastName())
        .avatar(domain.avatar())
        .isOrgMember(isOrgMember)
        .canBeInvitedToMoreOrg(canBeInvitedToMoreOrg)
        .status(StatusEnum.fromValue(domain.status().getValue()))
        .statusReason(domain.statusReason())
        .statusUpdatedAt(domain.statusUpdatedAt())
        .activeSubscriptionId(activeSubscription == null ? null : activeSubscription.getId())
        .latestSubscriptionId(domain.latestSubscriptionId())
        .suspensionDurationInSeconds(domain.suspensionDurationInSeconds().getSeconds())
        .archived(domain.archived());
  }
}
