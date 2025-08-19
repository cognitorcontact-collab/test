package api.poja.io.service.logQuery;

import static api.poja.io.endpoint.rest.model.EnvironmentType.PREPROD;
import static api.poja.io.endpoint.rest.model.EnvironmentType.PROD;
import static api.poja.io.endpoint.rest.model.FunctionType.*;
import static api.poja.io.endpoint.rest.model.FunctionType.FRONTAL;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ofPattern;

import api.poja.io.aws.cloudwatch.CloudwatchComponent;
import api.poja.io.endpoint.rest.model.EnvironmentType;
import api.poja.io.endpoint.rest.model.FunctionType;
import api.poja.io.endpoint.rest.model.LogQueryResult;
import api.poja.io.endpoint.rest.model.PagedLogQueryResults;
import api.poja.io.model.BoundedPageSize;
import api.poja.io.model.PageFromOne;
import api.poja.io.model.exception.InternalServerErrorException;
import api.poja.io.model.page.Page;
import api.poja.io.model.page.Paginator;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetQueryResultsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResultField;

@Component("serviceLogQueryMapper")
@AllArgsConstructor
public class LogQueryMapper {
  private static final DateTimeFormatter formatter = ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
  private static final String AWS_LAMBDA_LOG_GROUP_NAME_PREFIX = "/aws/lambda";
  private static final String PREPROD_ENV_LOG_KEYWORD = "preprod-compute";
  private static final String PROD_ENV_LOG_KEYWORD = "prod-compute";
  private static final String WORKER1_KEYWORD = "WorkerFunction1";
  private static final String WORKER2_KEYWORD = "WorkerFunction2";
  private static final String FRONTAL_KEYWORD = "FrontalFunction";
  private final CloudwatchComponent cloudwatchComponent;
  private final Paginator paginator;

  public PagedLogQueryResults toRest(Page<LogQueryResult> results) {
    return new PagedLogQueryResults()
        .count(results.count())
        .hasPrevious(results.hasPrevious())
        .pageNumber(results.queryPage().getValue())
        .pageSize(results.queryPageSize().getValue())
        .data(results.data().stream().toList());
  }

  public Page<LogQueryResult> mapResults(
      PageFromOne page,
      BoundedPageSize pageSize,
      Set<EnvironmentType> environmentTypes,
      Set<FunctionType> functionTypes,
      GetQueryResultsResponse queryResultResponse) {
    if (!queryResultResponse.hasResults()) {
      return new Page<>(page, pageSize, List.of());
    }
    List<List<ResultField>> results = queryResultResponse.results();
    return paginator
        .apply(page, pageSize, results)
        .map(this::mapResult)
        .filter(rs -> environmentTypes.contains(rs.getSourceEnvType()))
        .filter(rs -> functionTypes.contains(rs.getSourceFunctionType()));
  }

  private LogQueryResult mapResult(List<ResultField> resultFields) {
    Instant timestamp = parseInstant(resultFields.getFirst());
    String message = resultFields.get(1).value();
    String logStream = resultFields.get(2).value();
    String logName = extractLogGroupName(resultFields.get(3).value());
    return new LogQueryResult()
        .sourceEnvType(envTypeFrom(logName))
        .timestamp(timestamp)
        .matchingLogContent(message)
        .sourceFunctionType(functionTypeFrom(logName))
        .logUri(cloudwatchComponent.computeLogUri(logName, logStream, timestamp));
  }

  private static String extractLogGroupName(String logFieldValue) {
    return logFieldValue.substring(logFieldValue.indexOf(AWS_LAMBDA_LOG_GROUP_NAME_PREFIX));
  }

  private static EnvironmentType envTypeFrom(String logName) {
    if (logName.contains(PREPROD_ENV_LOG_KEYWORD)) {
      return PREPROD;
    }
    if (logName.contains(PROD_ENV_LOG_KEYWORD)) {
      return PROD;
    }
    throw new InternalServerErrorException("unable to assess envtype from logname");
  }

  private static FunctionType functionTypeFrom(String logName) {
    if (logName.contains(WORKER1_KEYWORD)) {
      return WORKER1;
    }
    if (logName.contains(WORKER2_KEYWORD)) {
      return WORKER2;
    }
    if (logName.contains(FRONTAL_KEYWORD)) {
      return FRONTAL;
    }
    throw new InternalServerErrorException("unable to assess functiontype from logname");
  }

  private static Instant parseInstant(ResultField resultField) {
    String datetimeStr = resultField.value();
    LocalDateTime localDateTime = LocalDateTime.parse(datetimeStr, formatter);
    return localDateTime.toInstant(UTC);
  }
}
