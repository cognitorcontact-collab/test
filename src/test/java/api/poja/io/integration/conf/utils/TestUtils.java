package api.poja.io.integration.conf.utils;

import static api.poja.io.endpoint.rest.model.StackType.COMPUTE_PERMISSION;
import static api.poja.io.integration.conf.utils.TestMocks.*;
import static api.poja.io.model.UpdateStackResult.UpdateStatus.UPDATE_SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import api.poja.io.aws.cloudformation.CloudformationComponent;
import api.poja.io.endpoint.rest.client.ApiClient;
import api.poja.io.endpoint.rest.client.ApiException;
import api.poja.io.endpoint.rest.model.Stack;
import api.poja.io.endpoint.rest.security.github.GithubComponent;
import api.poja.io.file.ExtendedBucketComponent;
import api.poja.io.file.bucket.BucketComponent;
import api.poja.io.model.UpdateStackResult;
import api.poja.io.model.exception.BadRequestException;
import api.poja.io.model.exception.ServiceUnavailableException;
import api.poja.io.service.github.model.GhAppInstallation;
import api.poja.io.service.github.model.GhListAppInstallationReposResponse;
import api.poja.io.service.github.model.GhListAppInstallationReposResponse.Repository;
import api.poja.io.service.stripe.StripeService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.function.Executable;
import org.kohsuke.github.GHMyself;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class TestUtils {

  public static final long APP_INSTALLATION_1_ID = 12344;
  public static final GhAppInstallation GH_APP_JOE_DOE_INSTALLATION_1 =
      new GhAppInstallation(APP_INSTALLATION_1_ID, "joeDoe", "User", "http://testimage.com");
  public static final long APP_INSTALLATION_2_ID = 12346;
  public static final GhAppInstallation GH_APP_JOE_DOE_INSTALLATION_2 =
      new GhAppInstallation(APP_INSTALLATION_2_ID, "joeDoe", "User", "http://testimage.com");
  public static final String NO_MATCHING_DB_ACCOUNT_GITHUB_ID = "NO_MATCHING_DB_ACCOUNT_GITHUB_ID";

  public static ApiClient anApiClient(String token, int serverPort) {
    ApiClient client = new ApiClient();
    client.setScheme("http");
    client.setHost("localhost");
    client.setPort(serverPort);
    client.setRequestInterceptor(
        httpRequestBuilder -> httpRequestBuilder.header("Authorization", "Bearer " + token));
    return client;
  }

  public static ApiClient anUnauthenticatedApiClient(int serverPort) {
    ApiClient client = new ApiClient();
    client.setScheme("http");
    client.setHost("localhost");
    client.setPort(serverPort);
    return client;
  }

  public static void setUpGithub(GithubComponent githubComponent) {
    when(githubComponent.getGithubUserId(JOE_DOE_TOKEN)).thenReturn(Optional.of(JOE_DOE_GITHUB_ID));
    when(githubComponent.getGithubUserId(LOREM_IPSUM_TOKEN))
        .thenReturn(Optional.of(LOREM_IPSUM_GITHUB_ID));
    when(githubComponent.getGithubUserId(ADMIN_TOKEN)).thenReturn(Optional.of(ADMIN_GITHUB_ID));
    when(githubComponent.getGithubUserId(SUSPENDED_TOKEN))
        .thenReturn(Optional.of(SUSPENDED_GITHUB_ID));
    when(githubComponent.getGithubUserId(JANE_DOE_TOKEN))
        .thenReturn(Optional.of(JANE_DOE_GITHUB_ID));
    when(githubComponent.getGithubUserId(DENIS_RITCHIE_TOKEN))
        .thenReturn(Optional.of(DENIS_RITCHIE_GITHUB_ID));
    when(githubComponent.getGithubUserId(ARCHIVED_TOKEN))
        .thenReturn(Optional.of(ARCHIVED_GITHUB_ID));
    when(githubComponent.getGithubUserId(NO_MATCHING_DB_ACCOUNT_TOKEN))
        .thenReturn(Optional.of(NO_MATCHING_DB_ACCOUNT_GITHUB_ID));
    Set<GhAppInstallation> t = ghApps();
    when(githubComponent.listInstallations()).thenReturn(t);
    when(githubComponent.listInstallationRepositories(eq(APP_INSTALLATION_1_ID), any(), any()))
        .thenReturn(joeDoeRepos());
    when(githubComponent.getInstallationById(APP_INSTALLATION_1_ID))
        .thenReturn(GH_APP_JOE_DOE_INSTALLATION_1);
    when(githubComponent.getInstallationById(APP_INSTALLATION_2_ID))
        .thenReturn(GH_APP_JOE_DOE_INSTALLATION_2);
  }

  private static Set<GhAppInstallation> ghApps() {
    return Set.of(GH_APP_JOE_DOE_INSTALLATION_1, GH_APP_JOE_DOE_INSTALLATION_2);
  }

  private static GhListAppInstallationReposResponse joeDoeRepos() {
    return new GhListAppInstallationReposResponse(
        1,
        List.of(
            new Repository(
                10,
                "joe_doe_repo_mock",
                "description",
                true,
                "https://repoUrl.com/repo",
                "default",
                10)));
  }

  public static void setUpGithub(GithubComponent githubComponent, GHMyself githubUser) {
    when(githubComponent.getGithubUserId(JOE_DOE_TOKEN)).thenReturn(Optional.of(JOE_DOE_GITHUB_ID));
    when(githubComponent.getCurrentUserByToken(JOE_DOE_TOKEN)).thenReturn(Optional.of(githubUser));
    when(githubComponent.getGithubUserId(SUSPENDED_TOKEN))
        .thenReturn(Optional.of(SUSPENDED_GITHUB_ID));
    when(githubComponent.getCurrentUserByToken(SUSPENDED_TOKEN))
        .thenReturn(Optional.of(githubUser));
  }

  @SneakyThrows
  public static void setupJoeDoeGithubUser(GHMyself githubUser) {
    when(githubUser.getEmail()).thenReturn("test@example.com");
    when(githubUser.getLogin()).thenReturn(JOE_DOE_USERNAME);
    when(githubUser.getId()).thenReturn(Long.valueOf(JOE_DOE_GITHUB_ID));
    when(githubUser.getAvatarUrl()).thenReturn(JOE_DOE_AVATAR);
  }

  @SneakyThrows
  public static void setupSuspendedGithubUser(GHMyself githubUser) {
    when(githubUser.getEmail()).thenReturn("suspended@email.com");
    when(githubUser.getLogin()).thenReturn("Suspended");
    when(githubUser.getId()).thenReturn(Long.valueOf(SUSPENDED_GITHUB_ID));
    when(githubUser.getAvatarUrl()).thenReturn("https://example.com");
  }

  @SneakyThrows
  public static void setUpStripe(StripeService stripeService) {
    when(stripeService.createCustomer(any(), any())).thenReturn(stripeCustomer());
    when(stripeService.getPaymentMethods(any())).thenReturn(paymentMethods());
    when(stripeService.setDefaultPaymentMethod(any(), any())).thenReturn(paymentMethod());
    when(stripeService.detachPaymentMethod(any())).thenReturn(paymentMethod());
    when(stripeService.attachPaymentMethod(any(), any())).thenReturn(paymentMethod());
  }

  public static void setUpCloudformationComponent(CloudformationComponent cloudformationComponent) {
    when(cloudformationComponent.createStack(any(), any(), any(), any()))
        .thenReturn(POJA_CF_STACK_ID);
    when(cloudformationComponent.updateStack(any(), any(), any(), any()))
        .thenReturn(new UpdateStackResult(UPDATE_SUCCESS, POJA_CF_STACK_ID));
  }

  public static void setUpBucketComponent(BucketComponent bucketComponent) throws IOException {
    when(bucketComponent.presign(any(), any()))
        .thenReturn(new URL("https://example.com/templatel"));
  }

  public static void setUpExtendedBucketComponent(ExtendedBucketComponent extendedBucketComponent)
      throws IOException, URISyntaxException {
    ClassPathResource stackEventResource = new ClassPathResource("files/log.json");
    String stackEventFileBucketKey =
        String.format(
            "orgs/%s/apps/%s/envs/%s/stacks/%s/events/%s",
            JOE_DOE_MAIN_ORG_ID,
            OTHER_POJA_APPLICATION_ID,
            OTHER_POJA_APPLICATION_ENVIRONMENT_ID,
            COMPUTE_PERMISSION,
            "log.json");
    when(extendedBucketComponent.download(stackEventFileBucketKey))
        .thenReturn(stackEventResource.getFile());
    when(extendedBucketComponent.doesExist(stackEventFileBucketKey)).thenReturn(true);
  }

  public static List<Stack> ignoreStackIdsAndDatetime(List<Stack> stacks) {
    return stacks.stream().map(TestUtils::ignoreStackIdAndDatetime).toList();
  }

  public static List<Stack> ignoreCfStackIdsAndDatetime(List<Stack> stacks) {
    return stacks.stream().map(TestUtils::ignoreCfStackIdAndDatetime).toList();
  }

  public static Stack ignoreStackIdAndDatetime(Stack stack) {
    stack.id(POJA_CREATED_STACK_ID);
    return ignoreStackDatetime(stack);
  }

  public static Stack ignoreCfStackIdAndDatetime(Stack stack) {
    stack.cfStackId(POJA_CF_STACK_ID);
    return ignoreStackDatetime(stack);
  }

  public static Stack ignoreStackDatetime(Stack stack) {
    stack.creationDatetime(null);
    stack.updateDatetime(null);
    return stack;
  }

  @SneakyThrows
  public static Resource getResource(String resourceFilePath) {
    return new ClassPathResource(resourceFilePath);
  }

  public static void assertThrowsForbiddenException(Executable executable, String message) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    String responseBody = apiException.getResponseBody();
    assertEquals(
        "{" + "\"type\":\"403 FORBIDDEN\"," + "\"message\":\"" + message + "\"}", responseBody);
  }

  public static void assertThrowsBadRequestException(Executable executable, String message) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    String responseBody = apiException.getResponseBody();
    assertEquals(
        "{" + "\"type\":\"400 BAD_REQUEST\"," + "\"message\":\"" + message + "\"}", responseBody);
  }

  public static void assertThrowsPaymentRequiredException(Executable executable, String message) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    String responseBody = apiException.getResponseBody();
    assertEquals(
        "{" + "\"type\":\"402 PAYMENT_REQUIRED\"," + "\"message\":\"" + message + "\"}",
        responseBody);
  }

  public static void assertThrowsUnauthorizedException(Executable executable, String message) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    String responseBody = apiException.getResponseBody();
    assertEquals(
        "{" + "\"type\":\"401 UNAUTHORIZED\"," + "\"message\":\"" + message + "\"}", responseBody);
  }

  public static void assertThrowsDomainBadRequestException(
      String expectedBody, Executable executable) {
    BadRequestException badRequestException = assertThrows(BadRequestException.class, executable);
    assertEquals(expectedBody, badRequestException.getMessage());
  }

  public static void assertThrowsDomainServiceUnavailableException(
      String expectedBody, Executable executable) {
    ServiceUnavailableException serviceUnavailableException =
        assertThrows(ServiceUnavailableException.class, executable);
    assertEquals(expectedBody, serviceUnavailableException.getMessage());
  }

  public static void assertThrowsNotFoundException(Executable executable, String message) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    String responseBody = apiException.getResponseBody();
    assertEquals(
        "{" + "\"type\":\"404 NOT_FOUND\"," + "\"message\":\"" + message + "\"}", responseBody);
  }

  public static void assertThrowsApiException(Executable executable, String message) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    String responseBody = apiException.getResponseBody();
    assertEquals(message, responseBody);
  }
}
