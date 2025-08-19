package api.poja.io.service.github;

import api.poja.io.endpoint.rest.model.EnvVars;
import api.poja.io.endpoint.rest.model.EnvironmentType;
import api.poja.io.endpoint.rest.model.RefreshToken;
import api.poja.io.endpoint.rest.model.Token;
import api.poja.io.endpoint.rest.security.github.GithubComponent;
import api.poja.io.endpoint.rest.security.github.GithubSecretsEncryptor;
import api.poja.io.model.BoundedPageSize;
import api.poja.io.model.PageFromOne;
import api.poja.io.service.github.model.*;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class GithubService {
  private final GithubComponent githubComponent;

  public Token exchangeCodeToToken(String code) {
    return githubComponent.exchangeCodeToToken(code);
  }

  public CreateRepoResponse createRepoFor(CreateRepoRequestBody requestBody, String token) {
    return githubComponent.createRepoFor(requestBody, token);
  }

  public UpdateRepoResponse updateRepoFor(
      UpdateRepoRequestBody application,
      String repositoryName,
      String token,
      String githubUsername) {
    return githubComponent.updateRepoFor(application, repositoryName, token, githubUsername);
  }

  public Set<GhAppInstallation> listApplicationInstallations() {
    return githubComponent.listInstallations();
  }

  public List<GhListAppInstallationReposResponse.Repository> listApplicationInstallationRepos(
      long installationId, PageFromOne page, BoundedPageSize pageSize) {
    return githubComponent
        .listInstallationRepositories(installationId, page, pageSize)
        .repositories();
  }

  public String getInstallationToken(long installationId, Duration duration) {
    return githubComponent.getAppInstallationToken(installationId, duration);
  }

  public GhAppInstallation getInstallationByGhId(long ghId) {
    return githubComponent.getInstallationById(ghId);
  }

  public Token refreshToken(RefreshToken refreshToken) {
    return githubComponent.refreshToken(refreshToken.getRefreshToken());
  }

  public void runWorkflowDispatch(
      String owner,
      String repoName,
      String token,
      String workflowId,
      GhWorkflowRunRequestBody workflowRunRequestBody) {
    githubComponent.runWorkflowDispatch(owner, repoName, token, workflowId, workflowRunRequestBody);
  }

  public void configureRepositorySecrets(
      String owner,
      String repoName,
      String token,
      List<EnvVars> envConfigs,
      EnvironmentType environmentType) {
    if (envConfigs == null || envConfigs.isEmpty()) {
      log.info("No environment variables to configure for repository {}/{}", owner, repoName);
      return;
    }

    GhRepoPublicKey publicKeyResponse;
    try {
      publicKeyResponse = githubComponent.getRepositoryPublicKey(owner, repoName, token);
    } catch (Exception e) {
      log.error(
          "Failed to retrieve public key for repository {}/{}: {}",
          owner,
          repoName,
          e.getMessage());
      throw new RuntimeException("Failed to retrieve repository public key.", e);
    }

    final String publicKey = publicKeyResponse.key();
    final String keyId = publicKeyResponse.key_id();
    log.info("Retrieved public key (id: {}) for repository {}/{}", keyId, owner, repoName);

    boolean hasErrors = false;

    for (EnvVars envConfig : envConfigs) {
      String suffix;
      if (environmentType.equals(EnvironmentType.PROD)) {
        suffix = "_PROD";
      } else if (environmentType.equals(EnvironmentType.PREPROD)) {
        suffix = "_PREPROD";
      } else {
        continue;
      }

      String secretName = envConfig.getName().toUpperCase() + suffix;
      String testValue = envConfig.getTestValue();

      if (testValue == null || testValue.isBlank()) {
        try {
          if (githubComponent.repositorySecretExists(owner, repoName, secretName, token)) {
            githubComponent.deleteRepositorySecret(owner, repoName, secretName, token);
            log.info("Deleted existing secret: {}", secretName);
          }
        } catch (Exception e) {
          log.error("Failed to delete secret {}: {}", secretName, e.getMessage());
          hasErrors = true;
        }
        continue;
      }

      try {
        String encryptedValue = GithubSecretsEncryptor.encrypt(testValue, publicKey);
        GhRepoSecretBody body = new GhRepoSecretBody(encryptedValue, keyId);
        githubComponent.createOrUpdateRepositorySecret(owner, repoName, secretName, body, token);
        log.info("Successfully created/updated secret: {}", secretName);
      } catch (Exception e) {
        log.error("Failed to configure secret {}: {}", secretName, e.getMessage());
        hasErrors = true;
      }
    }

    if (hasErrors) {
      log.warn(
          "Finished configuring secrets for repository {}/{} with some errors.", owner, repoName);
    } else {
      log.info(
          "Successfully finished configuring all environment secrets for repository {}/{}",
          owner,
          repoName);
    }
  }
}
