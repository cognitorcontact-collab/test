package api.poja.io.symja;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import api.poja.io.service.symjaService.SymjaService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class SymjaTest {
  SymjaService subject = new SymjaService();

  @Test
  void computeMaxBasicUsersGivenPremium() {
    var case10 = subject.computeMaxBasicUsersGivenPremium(10);
    var case20 = subject.computeMaxBasicUsersGivenPremium(20);
    var case40 = subject.computeMaxBasicUsersGivenPremium(40);
    var case50 = subject.computeMaxBasicUsersGivenPremium(50);

    assertEquals(440, case10.intValue());
    assertEquals(380, case20.intValue());
    assertEquals(260, case40.intValue());
    assertEquals(200, case50.intValue());
    assertThrows(
        IllegalArgumentException.class, () -> subject.computeMaxBasicUsersGivenPremium(100));
  }

  @Test
  void computeNeededLogPolicies() {
    var case12And3 = subject.computeNeededLogPolicies(12, 3);
    var case1And1 = subject.computeNeededLogPolicies(1, 1);

    assertEquals(6, case12And3.intValue());
    assertEquals(1, case1And1.intValue());
  }
}
