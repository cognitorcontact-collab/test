package api.poja.io.endpoint.rest.mapper;

import static java.net.URI.create;

import api.poja.io.endpoint.rest.model.Application;
import api.poja.io.endpoint.rest.model.GithubRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component("RestApplicationMapper")
@AllArgsConstructor
public class ApplicationMapper {
  public Application toRest(api.poja.io.repository.model.Application domain) {
    var githubRepository =
        new GithubRepository()
            .name(domain.getGithubRepositoryName())
            .isPrivate(domain.isGithubRepositoryPrivate())
            .description(domain.getDescription())
            .installationId(domain.getInstallationId())
            .htmlUrl(
                domain.getGithubRepositoryUrl() != null
                    ? create(domain.getGithubRepositoryUrl())
                    : null)
            .id(domain.getGithubRepositoryId())
            .imported(domain.isImported());
    return new api.poja.io.endpoint.rest.model.Application()
        .id(domain.getId())
        .status(domain.getStatus())
        .name(domain.getName())
        .githubRepository(githubRepository)
        .archived(domain.isArchived())
        .userId(domain.getUserId())
        .creationDatetime(domain.getCreationDatetime())
        .orgId(domain.getOrgId());
  }
}
