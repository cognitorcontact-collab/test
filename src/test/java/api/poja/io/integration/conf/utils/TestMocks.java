package api.poja.io.integration.conf.utils;

import static api.poja.io.endpoint.rest.model.ComputeConf2.FrontalFunctionInvocationMethodEnum.HTTP_API;
import static api.poja.io.endpoint.rest.model.DurationUnit.MINUTES;
import static api.poja.io.endpoint.rest.model.Environment.StateEnum.HEALTHY;
import static api.poja.io.endpoint.rest.model.EnvironmentType.PREPROD;
import static api.poja.io.endpoint.rest.model.EnvironmentType.PROD;
import static api.poja.io.endpoint.rest.model.EventStackSource._1;
import static api.poja.io.endpoint.rest.model.MonthType.DECEMBER;
import static api.poja.io.endpoint.rest.model.MonthType.JANUARY;
import static api.poja.io.endpoint.rest.model.OrganizationInviteType.ACCEPTED;
import static api.poja.io.endpoint.rest.model.OrganizationInviteType.PENDING;
import static api.poja.io.endpoint.rest.model.StackResourceStatusType.CREATE_COMPLETE;
import static api.poja.io.endpoint.rest.model.StackResourceStatusType.CREATE_IN_PROGRESS;
import static api.poja.io.endpoint.rest.model.StackResourceStatusType.UPDATE_IN_PROGRESS;
import static api.poja.io.endpoint.rest.model.UserRoleEnum.USER;
import static api.poja.io.endpoint.rest.model.WithQueuesNbEnum.NUMBER_2;
import static api.poja.io.model.PojaVersion.*;
import static api.poja.io.service.pricing.PricingMethod.TEN_MICRO;
import static java.net.URI.create;

import api.poja.io.endpoint.rest.model.*;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestMocks {
  public static final String BAD_TOKEN = "BadToken";
  public static final String NO_MATCHING_DB_ACCOUNT_TOKEN = "NoMatchingAccountToken";
  public static final String JOE_DOE_ID = "joe-doe-id";
  public static final String NEW_USER_GITHUB_ID = "8987";
  public static final String NEW_USER_TOKEN = "new_user_github_token";
  public static final String A_GITHUB_APP_TOKEN = "github_app_token";
  public static final String JOE_DOE_EMAIL = "joe@email.com";
  public static final String JOE_DOE_GITHUB_ID = "1234";
  public static final String JOE_DOE_USERNAME = "JoeDoe";
  public static final String JOE_DOE_AVATAR =
      "https://github.com/images/" + JOE_DOE_USERNAME + ".png";
  public static final String JOE_DOE_TOKEN = "joe_doe_token";
  public static final String JOE_DOE_STRIPE_ID = "joe_stripe_id";
  public static final String JOE_DOE_MAIN_ORG_ID = "org-" + JOE_DOE_USERNAME + "-id";
  public static final String ADMIN_TOKEN = "admin_token";
  public static final String ADMIN_GITHUB_ID = "1007";
  public static final String SUSPENDED_ID = "suspended_id";
  public static final String SUSPENDED_TOKEN = "suspended_token";
  public static final String SUSPENDED_GITHUB_ID = "1008";
  public static final String JANE_DOE_ID = "jane_doe_id";
  public static final String JANE_DOE_TOKEN = "jane_doe_token";
  public static final String JANE_DOE_EMAIL = "jane@email.com";
  public static final String JANE_DOE_GITHUB_ID = "4321";
  public static final String JANE_DOE_USERNAME = "JaneDoe";
  public static final String JANE_DOE_AVATAR =
      "https://github.com/images/" + JANE_DOE_USERNAME + ".png";
  public static final String JANE_DOE_STRIPE_ID = "jane_stripe_id";
  public static final String JANE_DOE_MAIN_ORG_ID = "org-JaneDoe-id";
  public static final String DENIS_RITCHIE_TOKEN = "denis_ritchie_token";
  public static final String DENIS_RITCHIE_ID = "denis_ritchie_id";
  public static final String DENIS_RITCHIE_GITHUB_ID = "1010";
  public static final String LOREM_IPSUM_TOKEN = "lorem_ipsum_token";
  public static final String LOREM_IPSUM_ID = "lorem_ipsum_id";
  public static final String LOREM_IPSUM_GITHUB_ID = "4567";
  public static final String ARCHIVED_TOKEN = "archived_token";
  public static final String ARCHIVED_GITHUB_ID = "1009";
  public static final String POJA_CREATED_STACK_ID = "poja_created_stack_id";
  public static final String POJA_CF_STACK_ID = "poja_cf_stack_id";
  public static final String POJA_APPLICATION_ID = "poja_application_id";
  public static final String POJA_APPLICATION_NAME = "poja-app";
  public static final String POJA_APPLICATION_ENVIRONMENT_ID = "poja_application_environment_id";
  public static final String GH_APP_INSTALL_1_ID = "gh_app_install_1_id";
  public static final String GH_APP_INSTALL_2_ID = "gh_app_install_2_id";
  public static final String POJA_APPLICATION_REPO_ID = "gh_repository_1_id";
  public static final GithubRepository POJA_APPLICATION_GITHUB_REPOSITORY =
      new GithubRepository()
          .id(POJA_APPLICATION_REPO_ID)
          .name("poja_application")
          .isPrivate(false)
          .description("a regular poja app")
          .installationId(GH_APP_INSTALL_1_ID)
          .htmlUrl(create("http://github.com/user/repo"))
          .imported(false);
  public static final GithubRepository POJA_APPLICATION_GITHUB_REPOSITORY_WITHOUT_ID =
      new GithubRepository()
          .name("poja_application")
          .isPrivate(false)
          .description("a regular poja app")
          .installationId(GH_APP_INSTALL_1_ID)
          .htmlUrl(create("http://github.com/user/repo"))
          .imported(true);
  public static final Instant POJA_APPLICATION_CREATION_DATETIME =
      Instant.parse("2023-06-18T10:15:30.00Z");
  public static final String EVENT_STACK_ID = "event_stack_1_id";
  public static final String EVENT_STACK_NAME = "poja_app_event_stack";
  public static final String BUCKET_STACK_ID = "bucket_stack_1_id";
  public static final String BUCKET_STACK_NAME = "poja_app_bucket_stack";
  public static final String COMPUTE_PERM_STACK_ID = "compute_perm_stack_1_id";
  public static final String COMPUTE_PERM_STACK_NAME = "poja_app_compute_perm_stack";
  public static final String OTHER_POJA_APPLICATION_ID = "other_poja_application_id";
  public static final String OTHER_POJA_APPLICATION_ENVIRONMENT_ID =
      "other_poja_application_environment_id";
  public static final String OTHER_POJA_APPLICATION_ENVIRONMENT_2_ID =
      "other_poja_application_environment_2_id";
  public static final String PROD_COMPUTE_FRONTAL_FUNCTION = "prod-compute-frontal-function";
  public static final String PROD_COMPUTE_WORKER_1_FUNCTION = "prod-compute-worker-1-function";
  public static final String PROD_COMPUTE_WORKER_2_FUNCTION = "prod-compute-worker-2-function";
  public static final Instant BILLING_INFO_START_TIME_QUERY =
      Instant.parse("2024-09-01T00:00:00.00Z");
  public static final Instant BILLING_INFO_END_TIME_QUERY =
      Instant.parse("2024-09-30T23:59:59.00Z");
  public static final String POJA_APPLICATION_2_ID = "poja_application_2_id";
  public static final String POJA_APPLICATION_5_ID = "poja_application_5_id";
  public static final String ORG_1_ID = "org_1_id";
  public static final String ORG_2_ID = "org_2_id";
  public static final String ORG_3_ID = "org_3_id";
  public static final String ORG_5_ID = "org_5_id";
  public static final String ORG_1_NAME = "org_1_name";
  public static final String ORG_2_NAME = "org_2_name";
  public static final String INVITE_TO_CANCEL_ID = "invite_17_id";

  public static Customer stripeCustomer() {
    Customer customer = new Customer();
    customer.setId(JOE_DOE_STRIPE_ID);
    customer.setName("stripe customer");
    customer.setEmail("test@example.com");
    return customer;
  }

  public static PaymentMethod paymentMethod() {
    PaymentMethod.Card card = new PaymentMethod.Card();
    card.setBrand("visa");
    card.setLast4("4242");
    card.setExpMonth(12L);
    card.setExpYear(2025L);

    PaymentMethod paymentMethod = new PaymentMethod();
    paymentMethod.setId("payment_method_id");
    paymentMethod.setType("card");
    paymentMethod.setCard(card);
    paymentMethod.setCustomer(JOE_DOE_STRIPE_ID);
    return paymentMethod;
  }

  public static List<PaymentMethod> paymentMethods() {
    List<PaymentMethod> paymentMethods = new ArrayList<>();
    paymentMethods.add(paymentMethod());
    return paymentMethods;
  }

  public static User joeDoeUser() {
    return new User()
        .id(JOE_DOE_ID)
        .email(JOE_DOE_EMAIL)
        .username(JOE_DOE_USERNAME)
        .roles(List.of(USER))
        .firstName("Joe")
        .lastName("Doe")
        .githubId(JOE_DOE_GITHUB_ID)
        .avatar(JOE_DOE_AVATAR)
        .stripeId(JOE_DOE_STRIPE_ID)
        .isBetaTester(true)
        .status(User.StatusEnum.ACTIVE)
        .isArchived(false)
        .mainOrgId(JOE_DOE_MAIN_ORG_ID)
        .suspensionDurationInSeconds(0L)
        .statusUpdatedAt(Instant.parse("2024-03-25T12:00:00Z"))
        .joinedAt(Instant.parse("2024-03-25T12:00:00.00Z"));
  }

  public static GetUserResponse joeDoeUserResponse() {
    return new GetUserResponse()
        .id(JOE_DOE_ID)
        .email(JOE_DOE_EMAIL)
        .username(JOE_DOE_USERNAME)
        .firstName("Joe")
        .lastName("Doe")
        .avatar(JOE_DOE_AVATAR)
        .archived(false)
        .status(GetUserResponse.StatusEnum.ACTIVE)
        .suspensionDurationInSeconds(0L)
        .statusUpdatedAt(Instant.parse("2024-03-25T12:00:00Z"))
        .activeSubscriptionId(null)
        .latestSubscriptionId(null);
  }

  public static Organization joeDoeMainOrg() {
    return new Organization()
        .id(JOE_DOE_MAIN_ORG_ID)
        .name("org-" + JOE_DOE_USERNAME)
        .ownerId(JOE_DOE_ID);
  }

  public static User janeDoeUser() {
    return new User()
        .id(JANE_DOE_ID)
        .email(JANE_DOE_EMAIL)
        .username(JANE_DOE_USERNAME)
        .roles(List.of(USER))
        .firstName("Jane")
        .lastName("Doe")
        .githubId(JANE_DOE_GITHUB_ID)
        .avatar(JANE_DOE_AVATAR)
        .status(User.StatusEnum.ACTIVE)
        .isBetaTester(true)
        .isArchived(false)
        .stripeId(JANE_DOE_STRIPE_ID)
        .mainOrgId(JANE_DOE_MAIN_ORG_ID)
        .statusUpdatedAt(Instant.parse("2024-03-25T12:00:00Z"))
        .suspensionDurationInSeconds(0L)
        .joinedAt(Instant.parse("2024-03-25T12:00:00.00Z"));
  }

  public static GetUserResponse janeDoeUserResponse() {
    return new GetUserResponse()
        .id(JANE_DOE_ID)
        .email(JANE_DOE_EMAIL)
        .username(JANE_DOE_USERNAME)
        .firstName("Jane")
        .lastName("Doe")
        .avatar(JANE_DOE_AVATAR)
        .archived(false)
        .status(GetUserResponse.StatusEnum.ACTIVE)
        .activeSubscriptionId(null)
        .latestSubscriptionId(null)
        .statusUpdatedAt(Instant.parse("2024-03-25T12:00:00Z"))
        .suspensionDurationInSeconds(0L);
  }

  public static List<Environment> pojaAppEnvironments() {
    return List.of(pojaAppProdEnvironment());
  }

  public static Environment pojaAppProdEnvironment() {
    return new Environment()
        .id(POJA_APPLICATION_ENVIRONMENT_ID)
        .environmentType(PROD)
        .status(Environment.StatusEnum.ACTIVE)
        .state(HEALTHY);
  }

  public static ApplicationBase applicationToCreate() {
    return new ApplicationBase()
        .id(POJA_APPLICATION_ID + "_4")
        .name(POJA_APPLICATION_NAME + "-4")
        .userId(JOE_DOE_ID)
        .githubRepository(POJA_APPLICATION_GITHUB_REPOSITORY_WITHOUT_ID)
        .orgId(JOE_DOE_MAIN_ORG_ID)
        .archived(false);
  }

  public static Application joePojaApplication1() {
    return new Application()
        .id(POJA_APPLICATION_ID)
        .name(POJA_APPLICATION_NAME)
        .userId(JOE_DOE_ID)
        .creationDatetime(POJA_APPLICATION_CREATION_DATETIME)
        .githubRepository(POJA_APPLICATION_GITHUB_REPOSITORY)
        .archived(false)
        .status(Application.StatusEnum.ACTIVE)
        .orgId(JOE_DOE_MAIN_ORG_ID);
  }

  public static Application joePojaApplication2() {
    return new Application()
        .id(POJA_APPLICATION_2_ID)
        .name("poja-app-2")
        .userId(JOE_DOE_ID)
        .creationDatetime(Instant.parse("2023-06-18T10:16:30.00Z"))
        .githubRepository(
            new GithubRepository()
                .id("gh_repository_2_id")
                .name("poja_application_2")
                .isPrivate(false)
                .description("a regular poja app")
                .installationId(GH_APP_INSTALL_1_ID)
                .imported(false)
                .htmlUrl(create("http://github.com/user/repo")))
        .archived(false)
        .status(Application.StatusEnum.UNKNOWN)
        .orgId(JOE_DOE_MAIN_ORG_ID);
  }

  public static Application joeArchivedPojaApplication1() {
    return new Application()
        .id(POJA_APPLICATION_5_ID)
        .name(POJA_APPLICATION_NAME + "-5")
        .userId(JOE_DOE_ID)
        .creationDatetime(POJA_APPLICATION_CREATION_DATETIME)
        .githubRepository(POJA_APPLICATION_GITHUB_REPOSITORY)
        .archived(true)
        .status(Application.StatusEnum.UNKNOWN)
        .orgId(JOE_DOE_MAIN_ORG_ID);
  }

  public static Application janePojaApplication() {
    return new Application()
        .id("poja_application_3_id")
        .name("poja-app-3")
        .userId(JANE_DOE_ID)
        .creationDatetime(Instant.parse("2023-06-18T10:17:30.00Z"))
        .githubRepository(POJA_APPLICATION_GITHUB_REPOSITORY)
        .archived(false)
        .status(Application.StatusEnum.ACTIVE)
        .orgId(JANE_DOE_MAIN_ORG_ID);
  }

  public static PojaConf1 getValidPojaConf1() {
    String humanReadableValuePojaVersion = POJA_1.toHumanReadableValue();
    return new PojaConf1()
        .version(humanReadableValuePojaVersion)
        .general(
            new GeneralPojaConf()
                .appName("appname")
                .packageFullName("com.test.api")
                .withSnapstart(false)
                .withQueuesNb(NUMBER_2)
                .customJavaDeps(List.of())
                .customJavaEnvVars(Map.of())
                .customJavaRepositories(List.of()))
        .database(
            new DatabaseConf()
                .withDatabase(DatabaseConf.WithDatabaseEnum.NONE)
                .databaseNonRootPassword(null)
                .databaseNonRootUsername(null)
                .prodDbClusterTimeout(null)
                .auroraAutoPause(null)
                .auroraMaxCapacity(null)
                .auroraMinCapacity(null)
                .auroraSleep(null)
                .auroraScalePoint(null))
        .emailing(new MailingConf().sesSource("mail@mail.com"))
        .genApiClient(
            new GenApiClientConf()
                .awsAccountId(null)
                .tsClientDefaultOpenapiServerUrl(null)
                .tsClientApiUrlEnvVarName(null)
                .codeartifactRepositoryName(null)
                .codeartifactDomainName(null))
        .integration(
            new IntegrationConf()
                .withSentry(false)
                .withSwaggerUi(false)
                .withSonar(false)
                .withFileStorage(false)
                .withCodeql(false))
        .compute(
            new ComputeConf()
                .frontalMemory(BigDecimal.valueOf(1024))
                .frontalFunctionTimeout(BigDecimal.valueOf(600))
                .workerMemory(BigDecimal.valueOf(512))
                .workerBatch(BigDecimal.valueOf(5))
                .workerFunction1Timeout(BigDecimal.valueOf(600))
                .apiGatewayTimeout(BigDecimal.valueOf(30000))
                .workerFunction2Timeout(BigDecimal.valueOf(600)))
        .concurrency(
            new ConcurrencyConf()
                .frontalReservedConcurrentExecutionsNb(5)
                .workerReservedConcurrentExecutionsNb(5))
        .testing(
            new TestingConf().jacocoMinCoverage(BigDecimal.valueOf(0.2)).javaFacadeIt("FacadeIT"));
  }

  public static PojaConf2 getValidPojaConf2() {
    String humanReadableValuePojaVersion = POJA_2.toHumanReadableValue();
    return new PojaConf2()
        .version(humanReadableValuePojaVersion)
        .general(
            new GeneralPojaConf2()
                .appName("appname")
                .packageFullName("com.test.api")
                .withSnapstart(false)
                .customJavaDeps(List.of())
                .customJavaEnvVars(Map.of())
                .customJavaRepositories(List.of()))
        .database(new DatabaseConf2().withDatabase(DatabaseConf2.WithDatabaseEnum.NONE))
        .emailing(new MailingConf().sesSource("mail@mail.com"))
        .genApiClient(
            new GenApiClientConf()
                .awsAccountId(null)
                .tsClientDefaultOpenapiServerUrl(null)
                .tsClientApiUrlEnvVarName(null)
                .codeartifactRepositoryName(null)
                .codeartifactDomainName(null))
        .integration(
            new IntegrationConf()
                .withSentry(false)
                .withSwaggerUi(false)
                .withSonar(false)
                .withFileStorage(false)
                .withCodeql(false))
        .compute(
            new ComputeConf2()
                .frontalMemory(BigDecimal.valueOf(1024))
                .frontalFunctionTimeout(BigDecimal.valueOf(600))
                .frontalFunctionInvocationMethod(HTTP_API)
                .worker1Memory(BigDecimal.valueOf(512))
                .worker2Memory(BigDecimal.valueOf(513))
                .worker1Batch(BigDecimal.valueOf(5))
                .worker2Batch(BigDecimal.valueOf(6))
                .withQueuesNb(NUMBER_2)
                .workerFunction1Timeout(BigDecimal.valueOf(600))
                .apiGatewayTimeout(BigDecimal.valueOf(30000))
                .workerFunction2Timeout(BigDecimal.valueOf(700)))
        .concurrency(
            new ConcurrencyConf2()
                .frontalReservedConcurrentExecutionsNb(5)
                .worker1ReservedConcurrentExecutionsNb(5)
                .worker2ReservedConcurrentExecutionsNb(5))
        .testing(
            new TestingConf().jacocoMinCoverage(BigDecimal.valueOf(0.2)).javaFacadeIt("FacadeIT"))
        .scheduledTasks(
            List.of(
                new ScheduledTask()
                    .name("ScheduledTask1")
                    .description("some scheduled task")
                    .className("classname")
                    .eventStackSource(_1)
                    .scheduleExpression("cron(0 6 * * ? *)")));
  }

  public static PojaConf3 getValidPojaConf3(
      ComputeConf2.FrontalFunctionInvocationMethodEnum invocationMethodEnum) {
    String humanReadableValuePojaVersion = POJA_3.toHumanReadableValue();
    return new PojaConf3()
        .version(humanReadableValuePojaVersion)
        .general(
            new GeneralPojaConf2()
                .appName("appname")
                .packageFullName("com.test.api")
                .withSnapstart(false)
                .customJavaDeps(List.of())
                .customJavaEnvVars(Map.of())
                .customJavaRepositories(List.of()))
        .database(new DatabaseConf2().withDatabase(DatabaseConf2.WithDatabaseEnum.NONE))
        .emailing(new MailingConf().sesSource("mail@mail.com"))
        .genApiClient(
            new GenApiClientConf()
                .awsAccountId(null)
                .tsClientDefaultOpenapiServerUrl(null)
                .tsClientApiUrlEnvVarName(null)
                .codeartifactRepositoryName(null)
                .codeartifactDomainName(null))
        .integration(
            new IntegrationConf()
                .withSentry(false)
                .withSwaggerUi(false)
                .withSonar(false)
                .withFileStorage(false)
                .withCodeql(false))
        .compute(
            new ComputeConf2()
                .frontalMemory(BigDecimal.valueOf(1024))
                .frontalFunctionTimeout(BigDecimal.valueOf(600))
                .frontalFunctionInvocationMethod(invocationMethodEnum)
                .worker1Memory(BigDecimal.valueOf(512))
                .worker2Memory(BigDecimal.valueOf(513))
                .worker1Batch(BigDecimal.valueOf(5))
                .worker2Batch(BigDecimal.valueOf(6))
                .withQueuesNb(NUMBER_2)
                .workerFunction1Timeout(BigDecimal.valueOf(600))
                .apiGatewayTimeout(BigDecimal.valueOf(30000))
                .workerFunction2Timeout(BigDecimal.valueOf(700)))
        .concurrency(
            new ConcurrencyConf2()
                .frontalReservedConcurrentExecutionsNb(5)
                .worker1ReservedConcurrentExecutionsNb(5)
                .worker2ReservedConcurrentExecutionsNb(5))
        .testing(
            new TestingConf().jacocoMinCoverage(BigDecimal.valueOf(0.2)).javaFacadeIt("FacadeIT"))
        .scheduledTasks(
            List.of(
                new ScheduledTask()
                    .name("ScheduledTask1")
                    .description("some scheduled task")
                    .className("classname")
                    .eventStackSource(_1)
                    .scheduleExpression("cron(0 6 * * ? *)")));
  }

  public static PojaConf4 getValidPojaConf4(
      ComputeConf2.FrontalFunctionInvocationMethodEnum invocationMethodEnum) {
    String humanReadableValuePojaVersion = POJA_4.toHumanReadableValue();
    return new PojaConf4()
        .version(humanReadableValuePojaVersion)
        .general(
            new GeneralPojaConf2()
                .appName("appname")
                .packageFullName("com.test.api")
                .withSnapstart(false)
                .customJavaDeps(List.of())
                .customJavaEnvVars(Map.of())
                .customJavaRepositories(List.of()))
        .database(new DatabaseConf2().withDatabase(DatabaseConf2.WithDatabaseEnum.NONE))
        .emailing(new MailingConf().sesSource("mail@mail.com"))
        .genApiClient(
            new GenApiClientConf()
                .awsAccountId(null)
                .tsClientDefaultOpenapiServerUrl(null)
                .tsClientApiUrlEnvVarName(null)
                .codeartifactRepositoryName(null)
                .codeartifactDomainName(null))
        .integration(
            new IntegrationConf()
                .withSentry(false)
                .withSwaggerUi(false)
                .withSonar(false)
                .withFileStorage(false)
                .withCodeql(false))
        .compute(
            new ComputeConf2()
                .frontalMemory(BigDecimal.valueOf(1024))
                .frontalFunctionTimeout(BigDecimal.valueOf(600))
                .frontalFunctionInvocationMethod(invocationMethodEnum)
                .worker1Memory(BigDecimal.valueOf(512))
                .worker2Memory(BigDecimal.valueOf(513))
                .worker1Batch(BigDecimal.valueOf(5))
                .worker2Batch(BigDecimal.valueOf(6))
                .withQueuesNb(NUMBER_2)
                .workerFunction1Timeout(BigDecimal.valueOf(600))
                .apiGatewayTimeout(BigDecimal.valueOf(30000))
                .workerFunction2Timeout(BigDecimal.valueOf(700)))
        .concurrency(
            new ConcurrencyConf2()
                .frontalReservedConcurrentExecutionsNb(5)
                .worker1ReservedConcurrentExecutionsNb(5)
                .worker2ReservedConcurrentExecutionsNb(5))
        .testing(
            new TestingConf().jacocoMinCoverage(BigDecimal.valueOf(0.2)).javaFacadeIt("FacadeIT"))
        .scheduledTasks(
            List.of(
                new ScheduledTask()
                    .name("ScheduledTask1")
                    .description("some scheduled task")
                    .className("classname")
                    .eventStackSource(_1)
                    .scheduleExpression("cron(0 6 * * ? *)")));
  }

  public static PojaConf5 getValidPojaConf5(
      ComputeConf2.FrontalFunctionInvocationMethodEnum invocationMethodEnum) {
    String humanReadableValuePojaVersion = POJA_5.toHumanReadableValue();
    return new PojaConf5()
        .version(humanReadableValuePojaVersion)
        .general(
            new GeneralPojaConf2()
                .appName("appname")
                .packageFullName("com.test.api")
                .withSnapstart(false)
                .customJavaDeps(List.of())
                .customJavaEnvVars(Map.of())
                .customJavaRepositories(List.of()))
        .database(new DatabaseConf2().withDatabase(DatabaseConf2.WithDatabaseEnum.NONE))
        .emailing(new MailingConf().sesSource("mail@mail.com"))
        .genApiClient(
            new GenApiClientConf()
                .awsAccountId(null)
                .tsClientDefaultOpenapiServerUrl(null)
                .tsClientApiUrlEnvVarName(null)
                .codeartifactRepositoryName(null)
                .codeartifactDomainName(null))
        .integration(
            new IntegrationConf()
                .withSentry(false)
                .withSwaggerUi(false)
                .withSonar(false)
                .withFileStorage(false)
                .withCodeql(false))
        .compute(
            new ComputeConf2()
                .frontalMemory(BigDecimal.valueOf(1024))
                .frontalFunctionTimeout(BigDecimal.valueOf(600))
                .frontalFunctionInvocationMethod(invocationMethodEnum)
                .worker1Memory(BigDecimal.valueOf(512))
                .worker2Memory(BigDecimal.valueOf(513))
                .worker1Batch(BigDecimal.valueOf(5))
                .worker2Batch(BigDecimal.valueOf(6))
                .withQueuesNb(NUMBER_2)
                .workerFunction1Timeout(BigDecimal.valueOf(600))
                .apiGatewayTimeout(BigDecimal.valueOf(30000))
                .workerFunction2Timeout(BigDecimal.valueOf(700)))
        .concurrency(
            new ConcurrencyConf2()
                .frontalReservedConcurrentExecutionsNb(5)
                .worker1ReservedConcurrentExecutionsNb(5)
                .worker2ReservedConcurrentExecutionsNb(5))
        .testing(
            new TestingConf().jacocoMinCoverage(BigDecimal.valueOf(0.2)).javaFacadeIt("FacadeIT"))
        .scheduledTasks(
            List.of(
                new ScheduledTask()
                    .name("ScheduledTask1")
                    .description("some scheduled task")
                    .className("classname")
                    .eventStackSource(_1)
                    .scheduleExpression("cron(0 6 * * ? *)")));
  }

  public static PojaConf6 getValidPojaConf6(
      ComputeConf2.FrontalFunctionInvocationMethodEnum invocationMethodEnum) {
    String humanReadableValuePojaVersion = POJA_6.toHumanReadableValue();
    return new PojaConf6()
        .version(humanReadableValuePojaVersion)
        .general(
            new GeneralPojaConf6()
                .appName("appname")
                .packageFullName("com.test.api")
                .withSnapstart(false)
                .customJavaDeps(List.of())
                .customJavaEnvVars(
                    List.of(new EnvVars().name("key").value("sk").testValue("sk_test")))
                .customJavaRepositories(List.of())
                .environmentType(PREPROD))
        .database(new DatabaseConf2().withDatabase(DatabaseConf2.WithDatabaseEnum.NONE))
        .emailing(new MailingConf().sesSource("mail@mail.com"))
        .genApiClient(
            new GenApiClientConf()
                .awsAccountId(null)
                .tsClientDefaultOpenapiServerUrl(null)
                .tsClientApiUrlEnvVarName(null)
                .codeartifactRepositoryName(null)
                .codeartifactDomainName(null))
        .integration(
            new IntegrationConf()
                .withSentry(false)
                .withSwaggerUi(false)
                .withSonar(false)
                .withFileStorage(false)
                .withCodeql(false))
        .compute(
            new ComputeConf2()
                .frontalMemory(BigDecimal.valueOf(1024))
                .frontalFunctionTimeout(BigDecimal.valueOf(600))
                .frontalFunctionInvocationMethod(invocationMethodEnum)
                .worker1Memory(BigDecimal.valueOf(512))
                .worker2Memory(BigDecimal.valueOf(513))
                .worker1Batch(BigDecimal.valueOf(5))
                .worker2Batch(BigDecimal.valueOf(6))
                .withQueuesNb(NUMBER_2)
                .workerFunction1Timeout(BigDecimal.valueOf(600))
                .apiGatewayTimeout(BigDecimal.valueOf(30000))
                .workerFunction2Timeout(BigDecimal.valueOf(700)))
        .concurrency(
            new ConcurrencyConf2()
                .frontalReservedConcurrentExecutionsNb(5)
                .worker1ReservedConcurrentExecutionsNb(5)
                .worker2ReservedConcurrentExecutionsNb(5))
        .testing(
            new TestingConf().jacocoMinCoverage(BigDecimal.valueOf(0.2)).javaFacadeIt("FacadeIT"))
        .scheduledTasks(
            List.of(
                new ScheduledTask()
                    .name("ScheduledTask1")
                    .description("some scheduled task")
                    .className("classname")
                    .eventStackSource(_1)
                    .scheduleExpression("cron(0 6 * * ? *)")));
  }

  public static List<StackEvent> permStackEvents() {
    StackEvent createInProgress =
        new StackEvent()
            .eventId("ExecutionRole-CREATE_IN_PROGRESS-2024-07-26T05:08:30.029Z")
            .logicalResourceId("ExecutionRole")
            .resourceType("AWS::IAM::Role")
            .timestamp(Instant.parse("2024-07-26T05:08:30.029Z"))
            .resourceStatus(CREATE_IN_PROGRESS)
            .statusMessage(null);
    StackEvent createComplete =
        new StackEvent()
            .eventId("ExecutionRole-CREATE_COMPLETE-2024-07-26T05:08:48.624Z")
            .logicalResourceId("ExecutionRole")
            .resourceType("AWS::IAM::Role")
            .timestamp(Instant.parse("2024-07-26T05:08:48.624Z"))
            .resourceStatus(CREATE_COMPLETE)
            .statusMessage(null);
    StackEvent updateInProgress =
        new StackEvent()
            .eventId("9094a550-4b12-11ef-804a-0642aee31ca5")
            .logicalResourceId("prod-compute-permission-poja-second")
            .resourceType("AWS::CloudFormation::Stack")
            .timestamp(Instant.parse("2024-07-26T05:47:37.873Z"))
            .resourceStatus(UPDATE_IN_PROGRESS)
            .statusMessage("User Initiated");
    return List.of(createInProgress, createComplete, updateInProgress);
  }

  public static EnvBillingInfo joeDoeBillingInfo1() {
    var duration = new Duration().amount(590.0).unit(MINUTES);
    return new EnvBillingInfo()
        .startTime(BILLING_INFO_START_TIME_QUERY)
        .appId(OTHER_POJA_APPLICATION_ID)
        .envId(OTHER_POJA_APPLICATION_ENVIRONMENT_ID)
        .endTime(BILLING_INFO_END_TIME_QUERY)
        .computedPrice(new BigDecimal("1045.23239217329813"))
        .pricingMethod(TEN_MICRO.getName())
        .computeTime(Instant.parse("2024-09-11T23:00:00.00Z"))
        .resourceInvocationTotalDuration(duration);
  }

  public static EnvBillingInfo joeDoeBillingInfo2() {
    var duration = new Duration().amount(390.0).unit(MINUTES);
    return new EnvBillingInfo()
        .startTime(BILLING_INFO_START_TIME_QUERY)
        .endTime(BILLING_INFO_END_TIME_QUERY)
        .appId(OTHER_POJA_APPLICATION_ID)
        .envId(OTHER_POJA_APPLICATION_ENVIRONMENT_2_ID)
        .computedPrice(new BigDecimal("761.227392137823"))
        .pricingMethod(TEN_MICRO.getName())
        .computeTime(Instant.parse("2024-09-20T14:00:00.00Z"))
        .resourceInvocationTotalDuration(duration);
  }

  public static UserBillingInfo joeDoeTotalBillingInfo() {
    var duration = new Duration().amount(1680.0).unit(MINUTES);
    return new UserBillingInfo()
        .userId(JOE_DOE_ID)
        .startTime(BILLING_INFO_START_TIME_QUERY)
        .endTime(BILLING_INFO_END_TIME_QUERY)
        .computeTime(Instant.parse("2024-09-30T23:00:00Z"))
        .computedPrice(new BigDecimal("3042.58217648441926"))
        .pricingMethod(TEN_MICRO.getName())
        .resourceInvocationTotalDuration(duration);
  }

  public static OrgBillingInfo joeDoeMainOrgBillingInfo() {
    var duration = new Duration().amount(1680.0).unit(MINUTES);
    return new OrgBillingInfo()
        .userId(JOE_DOE_ID)
        .orgId(JOE_DOE_MAIN_ORG_ID)
        .startTime(BILLING_INFO_START_TIME_QUERY)
        .endTime(BILLING_INFO_END_TIME_QUERY)
        .computeTime(Instant.parse("2024-09-30T23:00:00Z"))
        .computedPrice(new BigDecimal("3042.58217648441926"))
        .pricingMethod(TEN_MICRO.getName())
        .resourceInvocationTotalDuration(duration);
  }

  public static Organization joeDoeOrg() {
    return new Organization().id(ORG_1_ID).name(ORG_1_NAME).ownerId(JOE_DOE_ID);
  }

  public static Organization janeDoeOrg() {
    return new Organization().id(ORG_2_ID).name(ORG_2_NAME).ownerId(JANE_DOE_ID);
  }

  public static Organization janeDoeNewOrg() {
    return new Organization().id("jane_doe_new_org").name("jd_new_org").ownerId(JANE_DOE_ID);
  }

  public static List<Organization> janeDoeOrgs() {
    return List.of(joeDoeOrg(), janeDoeOrg());
  }

  public static Organization joeDoeOrg2() {
    return new Organization().id("new_org_id").name("new_org_name").ownerId(JOE_DOE_ID);
  }

  public static List<OrganizationInvite> joeDoeAcceptedOrgInvites() {
    return List.of(
        new OrganizationInvite()
            .id("invite_1_id")
            .userId(JOE_DOE_ID)
            .type(ACCEPTED)
            .invitedAt(Instant.parse("2025-02-21T00:00:00.00Z"))
            .orgId(ORG_1_ID),
        new OrganizationInvite()
            .id("invite_4_id")
            .userId(JOE_DOE_ID)
            .type(ACCEPTED)
            .invitedAt(Instant.parse("2025-02-21T00:00:00.00Z"))
            .orgId(ORG_3_ID));
  }

  public static List<OrganizationInvite> joeDoePendingOrgInvites() {
    return List.of(
        new OrganizationInvite()
            .id("invite_8_id")
            .userId(JOE_DOE_ID)
            .type(PENDING)
            .invitedAt(Instant.parse("2025-02-21T00:00:00.00Z"))
            .orgId(ORG_2_ID));
  }

  public static List<OrganizationInvite> joeDoeOrgPendingInvites() {
    return List.of(
        new OrganizationInvite()
            .id("invite_14_id")
            .userId(LOREM_IPSUM_ID)
            .type(PENDING)
            .orgId(JOE_DOE_MAIN_ORG_ID),
        new OrganizationInvite()
            .id("invite_15_id")
            .userId(JANE_DOE_ID)
            .type(PENDING)
            .orgId(JOE_DOE_MAIN_ORG_ID),
        new OrganizationInvite()
            .id("invite_16_id")
            .userId(DENIS_RITCHIE_ID)
            .type(PENDING)
            .orgId(JOE_DOE_MAIN_ORG_ID));
  }

  public static OrganizationInvite inviteToCancel() {
    return new OrganizationInvite()
        .id(INVITE_TO_CANCEL_ID)
        .type(PENDING)
        .orgId(JANE_DOE_MAIN_ORG_ID)
        .userId(JOE_DOE_ID);
  }

  public static UserBillingDiscount joeDoeBillingDiscount1() {
    return new UserBillingDiscount()
        .id("ubd_1_id")
        .userId(JOE_DOE_ID)
        .amount(BigDecimal.valueOf(2))
        .year(2024)
        .month(JANUARY)
        .description("gift")
        .creationDatetime(Instant.parse("2024-01-02T00:00:00.00Z"));
  }

  public static UserBillingDiscount joeDoeBillingDiscount2() {
    return new UserBillingDiscount()
        .id("ubd_2_id")
        .userId(JOE_DOE_ID)
        .amount(BigDecimal.valueOf(1))
        .year(2025)
        .month(DECEMBER)
        .description("bonus")
        .creationDatetime(Instant.parse("2025-12-05T00:00:00.00Z"));
  }
}
