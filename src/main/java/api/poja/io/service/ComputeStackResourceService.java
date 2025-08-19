package api.poja.io.service;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

import api.poja.io.aws.AwsConf;
import api.poja.io.endpoint.rest.model.EnvironmentType;
import api.poja.io.endpoint.rest.model.StackOutput;
import api.poja.io.repository.jpa.ComputeStackResourceRepository;
import api.poja.io.repository.model.ComputeStackResource;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ComputeStackResourceService {
  /**
   * The base URL template for accessing the Lambda function dashboard in AWS Console. This prefix
   * should be concatenated with the Lambda function name to form the complete URL.
   */
  private static final String WEBSITE_FUNCTION_DASHBOARD_URL_TEMPLATE =
      "https://%s.console.aws.amazon.com/lambda/home?region=%s#/functions/%s";

  private static final String WEBSITE_FUNCTION_DASHBOARD_URL_PREFIX_TEMPLATE =
      "https://%s.console.aws.amazon.com/lambda/home?region=%s#/functions/";
  public static final String API_URL_KEY = "ApiUrl";

  private final ComputeStackResourceRepository repository;
  private final AwsConf awsConf;
  private final StackService stackService;

  public List<ComputeStackResource> findAllByEnvironmentId(String environmentId) {
    return repository.findAllByEnvironmentIdOrderByCreationDatetimeDesc(environmentId);
  }

  public List<ComputeStackResource> findAllByCriteriaOrderByLatest(
      String orgId, String applicationId, String environmentId) {
    if (environmentId == null) {
      return repository.findAllByApplicationIdOrderByCreationDatetimeDesc(applicationId);
    }
    return repository.findAllByCriteriaOrderByCreationDatetimeDesc(applicationId, environmentId);
  }

  public Optional<ComputeStackResource> findLatestByEnvironmentId(String environmentId) {
    return repository.findTopByEnvironmentIdOrderByCreationDatetimeDesc(environmentId);
  }

  public List<ComputeStackResource> findLatestResourcesByEnvTypes(
      String applicationId, Set<EnvironmentType> type) {
    return repository.findLatestByEnvironmentTypes(applicationId, type);
  }

  public List<ComputeStackResource> findAllByStackIds(List<String> stackIds) {
    return repository.findAllByStackIdIn(stackIds);
  }

  public ComputeStackResource save(ComputeStackResource resource) {
    return repository.save(resource);
  }

  public Optional<ComputeStackResource> findOneByAppEnvDeplId(String appEnvDeplId) {
    return repository.findOneByAppEnvDeplId(appEnvDeplId);
  }

  public URI getFunctionDashboardUrl(String functionName) {
    String region = awsConf.getRegion().toString();
    return URI.create(
        WEBSITE_FUNCTION_DASHBOARD_URL_TEMPLATE.formatted(region, region, functionName));
  }

  public URI getFunctionDashboardUrlPrefix() {
    String region = awsConf.getRegion().toString();
    return URI.create(WEBSITE_FUNCTION_DASHBOARD_URL_PREFIX_TEMPLATE.formatted(region, region));
  }

  public Optional<URI> getComputeStackFrontalUrl(String stackId) {
    Optional<StackOutput> optionalApiUrl =
        stackService.getStackOutputs(stackId).stream()
            .filter(o -> API_URL_KEY.equals(o.getKey()))
            .findFirst();
    if (optionalApiUrl.isEmpty()) {
      log.error("could not find api url");
      return empty();
    }
    return Optional.of(URI.create(requireNonNull(optionalApiUrl.get().getValue())));
  }
}
