package api.poja.io.service;

import api.poja.io.model.BoundedPageSize;
import api.poja.io.model.PageFromOne;
import api.poja.io.model.page.Page;
import api.poja.io.repository.model.AppInstallation;
import api.poja.io.service.github.GithubService;
import api.poja.io.service.github.model.GhListAppInstallationReposResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppRepositoryService {
  private final AppInstallationService appInstallationService;
  private final GithubService githubService;

  public Page<GhListAppInstallationReposResponse.Repository> getOrgInstallationRepositories(
      String installationId, PageFromOne page, BoundedPageSize pageSize) {
    AppInstallation installation = appInstallationService.getById(installationId);
    return new Page<>(
        page,
        pageSize,
        githubService.listApplicationInstallationRepos(installation.getGhId(), page, pageSize));
  }
}
