package api.poja.io.integration;

import static api.poja.io.endpoint.rest.model.UpdateUserStatusRequestBody.ActionEnum.ACTIVATE;
import static api.poja.io.endpoint.rest.model.UpdateUserStatusRequestBody.ActionEnum.SUSPEND;
import static api.poja.io.integration.conf.utils.TestMocks.*;
import static api.poja.io.integration.conf.utils.TestUtils.assertThrowsBadRequestException;
import static api.poja.io.integration.conf.utils.TestUtils.assertThrowsUnauthorizedException;
import static api.poja.io.integration.conf.utils.TestUtils.setUpGithub;
import static api.poja.io.integration.conf.utils.TestUtils.setUpStripe;
import static api.poja.io.integration.conf.utils.TestUtils.setupJoeDoeGithubUser;
import static api.poja.io.integration.conf.utils.TestUtils.setupSuspendedGithubUser;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import api.poja.io.conf.MockedThirdParties;
import api.poja.io.endpoint.rest.api.SecurityApi;
import api.poja.io.endpoint.rest.api.UserApi;
import api.poja.io.endpoint.rest.client.ApiClient;
import api.poja.io.endpoint.rest.client.ApiException;
import api.poja.io.endpoint.rest.model.CreateUser;
import api.poja.io.endpoint.rest.model.CreateUsersRequestBody;
import api.poja.io.endpoint.rest.model.UpdateUserStatusRequestBody;
import api.poja.io.endpoint.rest.model.User;
import api.poja.io.endpoint.rest.model.Whoami;
import api.poja.io.integration.conf.utils.TestUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHMyself;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
class UserIT extends MockedThirdParties {
  private static Whoami joeDoeWhoami() {
    return new Whoami().user(joeDoeUser());
  }

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, port);
  }

  @BeforeEach
  void setup() {
    setUpStripe(stripeServiceMock);
  }

  @Test
  void whoami_ok() throws ApiException {
    var githubUser = mock(GHMyself.class);
    setupJoeDoeGithubUser(githubUser);
    setUpGithub(githubComponentMock, githubUser);
    ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
    SecurityApi api = new SecurityApi(joeDoeClient);

    Whoami actual = api.whoami();

    assertEquals(joeDoeWhoami(), actual);
  }

  @Test
  void whoami_bad_token_ko() {
    setUpGithub(githubComponentMock);
    ApiClient badTokenClient = anApiClient(BAD_TOKEN);
    SecurityApi api = new SecurityApi(badTokenClient);

    assertThrowsUnauthorizedException(api::whoami, "Bad credentials");
  }

  @Test
  void whoami_not_existing_account_for_token_ko() {
    setUpGithub(githubComponentMock);
    ApiClient noMatchingAccountInDbClient = anApiClient(NO_MATCHING_DB_ACCOUNT_TOKEN);
    SecurityApi api = new SecurityApi(noMatchingAccountInDbClient);

    assertThrowsUnauthorizedException(api::whoami, "username not found");
  }

  @Test
  void get_stats_ok() throws ApiException {
    setUpGithub(githubComponentMock);
    ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
    UserApi api = new UserApi(joeDoeClient);

    var userStats = api.getUserStatistics();
    assertEquals(3, userStats.getSuspendedUsersNb());
    assertEquals(1, userStats.getArchivedUsersNb());
    assertEquals(14, userStats.getUsersCount());
  }

  @Test
  void suspended_whoami_ok() {
    var githubUser = mock(GHMyself.class);
    setupSuspendedGithubUser(githubUser);
    setUpGithub(githubComponentMock, githubUser);
    ApiClient suspendedClient = anApiClient(SUSPENDED_TOKEN);
    SecurityApi api = new SecurityApi(suspendedClient);

    assertDoesNotThrow(api::whoami);
  }

  @Test
  void signup_ok() throws ApiException {
    var githubUser = mock(GHMyself.class);
    when(githubComponentMock.getGithubUserId(NEW_USER_TOKEN))
        .thenReturn(Optional.of(NEW_USER_GITHUB_ID));
    when(githubComponentMock.getCurrentUserByToken(NEW_USER_TOKEN))
        .thenReturn(Optional.of(githubUser));
    when(githubUser.getLogin()).thenReturn("new_user_github_username");
    when(githubUser.getId()).thenReturn(Long.valueOf(NEW_USER_GITHUB_ID));
    when(githubUser.getAvatarUrl()).thenReturn(JOE_DOE_AVATAR);
    ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
    UserApi api = new UserApi(joeDoeClient);

    CreateUser toCreate =
        new CreateUser()
            .firstName("firstName")
            .lastName("lastName")
            .email("test@example.com")
            .token(NEW_USER_TOKEN);

    User actual =
        Objects.requireNonNull(
                api.createUser(new CreateUsersRequestBody().data(List.of(toCreate))).getData())
            .getFirst();

    assertEquals("test@example.com", actual.getEmail());
    assertEquals(JOE_DOE_AVATAR, actual.getAvatar());
    assertEquals(NEW_USER_GITHUB_ID, actual.getGithubId());
    assertEquals("new_user_github_username", actual.getUsername());
    assertEquals(JOE_DOE_STRIPE_ID, actual.getStripeId());
  }

  @Test
  void get_paginated_users_by_username_ok() throws ApiException {
    setUpGithub(githubComponentMock);
    ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
    UserApi api = new UserApi(joeDoeClient);

    var expected = List.of(joeDoeUserResponse(), janeDoeUserResponse());

    var actualFilteredByName = api.getUsers("j", 1, 15).getData();

    var actualWithEmptyUsername = api.getUsers("", 1, 15).getData();
    var joeDoe = api.getUsers("joe", 1, 10).getData();

    assertEquals(joeDoeUserResponse(), joeDoe.getFirst());
    assertTrue(Objects.requireNonNull(actualFilteredByName).containsAll(expected));
    assertTrue(Objects.requireNonNull(actualWithEmptyUsername).containsAll(expected));
  }

  @Test
  void get_user_by_id_ok() throws ApiException {
    setUpGithub(githubComponentMock);
    ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
    UserApi api = new UserApi(joeDoeClient);

    var actual = api.getUserById(JOE_DOE_ID);

    assertEquals(joeDoeUserResponse(), actual);
  }

  @Test
  void delete_user_by_id_ok() throws ApiException {
    setUpGithub(githubComponentMock);
    ApiClient adminClient = anApiClient(ADMIN_TOKEN);
    UserApi api = new UserApi(adminClient);

    api.deleteUser("to_archive_id");
  }

  @Test
  void suspend_user_by_id_ok() throws ApiException {
    setUpGithub(githubComponentMock);
    ApiClient adminClient = anApiClient(ADMIN_TOKEN);
    UserApi api = new UserApi(adminClient);

    String reason =
        "Your account has been temporarily suspended due to repeated deployment of applications"
            + " that violate our Acceptable Use Policy.";

    api.updateUserStatus(
        "to_suspend_id", new UpdateUserStatusRequestBody().action(SUSPEND).reason(reason));
  }

  @Test
  void activate_user_by_id_ok() throws ApiException {
    setUpGithub(githubComponentMock);
    ApiClient adminClient = anApiClient(ADMIN_TOKEN);
    UserApi api = new UserApi(adminClient);

    api.updateUserStatus("to_activate_id", new UpdateUserStatusRequestBody().action(ACTIVATE));
  }

  @Test
  void archived_whoami_ko() {
    setUpGithub(githubComponentMock);
    ApiClient apiClient = anApiClient(ARCHIVED_TOKEN);
    SecurityApi api = new SecurityApi(apiClient);

    assertThrowsUnauthorizedException(api::whoami, "User account has been deactivated");
  }

  @Test
  void cannot_suspend_user_with_the_same_reason_within_grace_period() {
    setUpGithub(githubComponentMock);
    ApiClient adminClient = anApiClient(ADMIN_TOKEN);
    UserApi api = new UserApi(adminClient);

    assertThrowsBadRequestException(
        () ->
            api.updateUserStatus(
                "recsus_id",
                new UpdateUserStatusRequestBody()
                    .action(SUSPEND)
                    .reason("admin: first suspension")),
        "User.id=recsus_id cannot be suspended for the same reason again until 3 days.");
  }
}
