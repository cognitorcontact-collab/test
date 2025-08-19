package api.poja.io.service;

import api.poja.io.model.exception.NotFoundException;
import api.poja.io.repository.jpa.AppInstallationRepository;
import api.poja.io.repository.model.AppInstallation;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppInstallationService {
  private final AppInstallationRepository repository;

  public List<AppInstallation> saveAllForUser(List<AppInstallation> toSave) {
    var updatedToSave =
        toSave.stream()
            .map(
                installation ->
                    repository
                        .findByUserIdAndOwnerGithubLogin(
                            installation.getUserId(), installation.getOwnerGithubLogin())
                        .map(
                            i ->
                                i.toBuilder()
                                    .ghId(installation.getGhId())
                                    .avatarUrl(installation.getAvatarUrl())
                                    .build())
                        .orElse(installation))
            .toList();
    return repository.saveAll(updatedToSave);
  }

  public List<AppInstallation> saveAll(List<AppInstallation> toSave) {
    var updatedToSave =
        toSave.stream()
            .map(
                installation ->
                    repository
                        .findByOrgIdAndOwnerGithubLogin(
                            installation.getOrgId(), installation.getOwnerGithubLogin())
                        .map(
                            i ->
                                i.toBuilder()
                                    .ghId(installation.getGhId())
                                    .avatarUrl(installation.getAvatarUrl())
                                    .build())
                        .orElse(installation))
            .toList();
    return repository.saveAll(updatedToSave);
  }

  public List<AppInstallation> findAllByUserId(String userId) {
    return repository.findAllByUserId(userId);
  }

  public List<AppInstallation> findAllByOrgId(String orgId) {
    return repository.findAllByOrgId(orgId);
  }

  public Optional<AppInstallation> findById(String id) {
    return repository.findById(id);
  }

  public AppInstallation getById(String id) {
    return findById(id)
        .orElseThrow(() -> new NotFoundException("AppInstallation#Id = " + id + " not found."));
  }
}
