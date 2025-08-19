package api.poja.io.integration;

import static api.poja.io.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.poja.io.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.poja.io.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.poja.io.integration.conf.utils.TestUtils.setUpGithub;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.poja.io.conf.MockedThirdParties;
import api.poja.io.endpoint.rest.api.PojaVersionsApi;
import api.poja.io.endpoint.rest.client.ApiClient;
import api.poja.io.endpoint.rest.client.ApiException;
import api.poja.io.endpoint.rest.model.PojaVersion;
import api.poja.io.endpoint.rest.model.PojaVersionsResponse;
import api.poja.io.integration.conf.utils.TestUtils;
import java.io.IOException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class PojaVersionIT extends MockedThirdParties {
  @BeforeEach
  void setup() throws IOException {
    setUpGithub(githubComponentMock);
    setUpCloudformationComponent(cloudformationComponentMock);
    setUpBucketComponent(bucketComponentMock);
  }

  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  private PojaVersion pojaConf1() {
    var from = api.poja.io.model.PojaVersion.POJA_1;
    return createFrom(from);
  }

  private static PojaVersion createFrom(api.poja.io.model.PojaVersion from) {
    return new PojaVersion()
        .major(from.getMajor())
        .minor(from.getMinor())
        .patch(from.getPatch())
        .humanReadableValue(from.toHumanReadableValue());
  }

  @Test
  void read_all_versions_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new PojaVersionsApi(apiClient);

    PojaVersionsResponse pojaVersions = api.getPojaVersions();
    var data = Objects.requireNonNull(pojaVersions.getData());

    assertTrue(data.contains(pojaConf1()));
  }
}
