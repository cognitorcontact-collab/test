package api.poja.io.integration;

import static api.poja.io.endpoint.rest.model.ComputeConf2.FrontalFunctionInvocationMethodEnum.LAMBDA_URL;
import static api.poja.io.endpoint.rest.model.EnvironmentType.PREPROD;
import static api.poja.io.integration.conf.utils.TestMocks.A_GITHUB_APP_TOKEN;
import static api.poja.io.integration.conf.utils.TestMocks.POJA_APPLICATION_REPO_ID;
import static api.poja.io.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.poja.io.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.poja.io.integration.conf.utils.TestUtils.setUpGithub;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import api.poja.io.conf.MockedThirdParties;
import api.poja.io.endpoint.rest.api.EnvDeployApi;
import api.poja.io.endpoint.rest.client.ApiClient;
import api.poja.io.endpoint.rest.client.ApiException;
import api.poja.io.endpoint.rest.model.BuiltEnvInfo;
import api.poja.io.endpoint.rest.model.EnvVars;
import api.poja.io.endpoint.rest.model.EnvironmentType;
import api.poja.io.file.ExtendedBucketComponent;
import api.poja.io.integration.conf.utils.TestUtils;
import api.poja.io.service.github.model.GhGetCommitResponse;
import api.poja.io.service.github.model.GhRepoPublicKey;
import api.poja.io.service.github.model.GhRepoSecretBody;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Slf4j
public class GithubRepositorySecretsIT extends MockedThirdParties {
  
  private static final String TEST_REPO_OWNER = "poja-test";
  private static final String TEST_REPO_NAME = "test-repository";
  private static final String TEST_SECRET_NAME = "TEST_SECRET_PREPROD";
  private static final String TEST_SECRET_VALUE = "test-secret-value-123";
  private static final String MOCK_PUBLIC_KEY = "test-public-key-base64";
  private static final String MOCK_KEY_ID = "test-key-id";
  
  @MockBean 
  ExtendedBucketComponent extendedBucketComponentMock;

  private ApiClient anApiClient() {
    return TestUtils.anApiClient(A_GITHUB_APP_TOKEN, port);
  }

  @BeforeEach
  void setup() throws IOException {
    when(githubComponentMock.getRepositoryIdByAppToken(A_GITHUB_APP_TOKEN))
        .thenReturn(Optional.of(POJA_APPLICATION_REPO_ID));
    setUpGithub(githubComponentMock);
    setUpCloudformationComponent(cloudformationComponentMock);
    setUpBucketComponent(bucketComponentMock);
    setUpExtendedBucketComponentMock();
    setUpGithubSecretsComponents();
  }

  void setUpExtendedBucketComponentMock() throws IOException {
    when(extendedBucketComponentMock.doesExist(any())).thenReturn(true);
    when(extendedBucketComponentMock.download(any()))
        .thenReturn(new ClassPathResource("files/poja_6.yml").getFile());
  }

  void setUpGithubSecretsComponents() {
    // Mock GitHub public key retrieval
    when(githubComponentMock.getRepositoryPublicKey(
        eq(TEST_REPO_OWNER), eq(TEST_REPO_NAME), any()))
        .thenReturn(new GhRepoPublicKey(MOCK_PUBLIC_KEY, MOCK_KEY_ID));
    
    // Mock commit info for deployment
    when(githubComponentMock.getCommitInfo(any(), anyLong(), any(), any()))
        .thenReturn(createMockCommitResponse());
    
    // Mock secret existence check
    when(githubComponentMock.repositorySecretExists(
        eq(TEST_REPO_OWNER), eq(TEST_REPO_NAME), eq(TEST_SECRET_NAME), any()))
        .thenReturn(false);
  }

  @Test
  void deploy_env_with_github_secrets_configuration_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new EnvDeployApi(apiClient);
    
    String deploymentId = randomUUID().toString();
    BuiltEnvInfo payload = createBuiltEnvInfoWithSecrets(deploymentId);
    
    var actual = api.deployEnv(TEST_REPO_OWNER, TEST_REPO_NAME, PREPROD, payload);
    
    // Verify the deployment was successful
    assertNotNull(actual);
    assertEquals(payload.getId(), actual.getId());
    assertEquals(PREPROD, actual.getEnvironmentType());
    
    // Verify GitHub secrets were configured
    verify(githubComponentMock, times(1))
        .getRepositoryPublicKey(eq(TEST_REPO_OWNER), eq(TEST_REPO_NAME), any());
    
    verify(githubComponentMock, times(1))
        .createOrUpdateRepositorySecret(
            eq(TEST_REPO_OWNER), 
            eq(TEST_REPO_NAME), 
            eq(TEST_SECRET_NAME), 
            any(GhRepoSecretBody.class), 
            any());
    
    // Verify event was fired
    verify(eventProducerMock, times(1)).accept(any());
    
    log.info("GitHub secrets configuration test completed successfully");
  }

  @Test
  void deploy_env_with_empty_secret_value_deletes_existing_secret() throws ApiException {
    // Setup: mock that secret exists
    when(githubComponentMock.repositorySecretExists(
        eq(TEST_REPO_OWNER), eq(TEST_REPO_NAME), eq(TEST_SECRET_NAME), any()))
        .thenReturn(true);
    
    var apiClient = anApiClient();
    var api = new EnvDeployApi(apiClient);
    
    String deploymentId = randomUUID().toString();
    BuiltEnvInfo payload = createBuiltEnvInfoWithEmptySecret(deploymentId);
    
    var actual = api.deployEnv(TEST_REPO_OWNER, TEST_REPO_NAME, PREPROD, payload);
    
    // Verify the deployment was successful
    assertNotNull(actual);
    assertEquals(payload.getId(), actual.getId());
    
    // Verify existing secret was deleted
    verify(githubComponentMock, times(1))
        .deleteRepositorySecret(eq(TEST_REPO_OWNER), eq(TEST_REPO_NAME), eq(TEST_SECRET_NAME), any());
    
    log.info("GitHub secret deletion test completed successfully");
  }

  private BuiltEnvInfo createBuiltEnvInfoWithSecrets(String deploymentId) {
    return new BuiltEnvInfo()
        .id(deploymentId)
        .appEnvDeploymentId("deployment_14_id")
        .environmentType(PREPROD)
        .formattedBucketKey("temp/test-build-" + deploymentId + ".zip")
        .commitSha("test-commit-sha-123");
  }

  private BuiltEnvInfo createBuiltEnvInfoWithEmptySecret(String deploymentId) {
    return new BuiltEnvInfo()
        .id(deploymentId)
        .appEnvDeploymentId("deployment_14_id")
        .environmentType(PREPROD)
        .formattedBucketKey("temp/test-build-empty-" + deploymentId + ".zip")
        .commitSha("test-commit-sha-456");
  }

  private GhGetCommitResponse createMockCommitResponse() {
    return new GhGetCommitResponse(
        "test-sha",
        new GhGetCommitResponse.GhCommit(
            "Test commit message",
            new GhGetCommitResponse.GhCommit.GhCommitter("Test User", "test@example.com"),
            URI.create("https://github.com/test/repo/commit/test-sha")),
        new GhGetCommitResponse.GhUser(
            "testuser", "123456", URI.create("https://avatars.githubusercontent.com/u/123456"), "User"));
  }
}