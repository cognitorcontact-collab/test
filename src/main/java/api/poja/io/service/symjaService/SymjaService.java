package api.poja.io.service.symjaService;

import static org.matheclipse.core.expression.F.C1D3;
import static org.matheclipse.core.expression.F.Ceiling;
import static org.matheclipse.core.expression.F.CompoundExpression;
import static org.matheclipse.core.expression.F.Divide;
import static org.matheclipse.core.expression.F.Dummy;
import static org.matheclipse.core.expression.F.Function;
import static org.matheclipse.core.expression.F.Map;
import static org.matheclipse.core.expression.F.Max;
import static org.matheclipse.core.expression.F.Module;
import static org.matheclipse.core.expression.F.Select;
import static org.matheclipse.core.expression.F.Set;
import static org.matheclipse.core.expression.F.Slot1;
import static org.matheclipse.core.expression.F.Subtract;
import static org.matheclipse.core.expression.F.Times;
import static org.matheclipse.core.expression.F.Total;
import static org.matheclipse.core.expression.F.ZZ;
import static org.matheclipse.core.expression.S.AllTrue;
import static org.matheclipse.core.expression.S.IntegerPartitions;
import static org.matheclipse.core.expression.S.NonNegative;

import java.util.List;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IAST;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.core.interfaces.IInteger;
import org.matheclipse.core.interfaces.ISymbol;
import org.springframework.stereotype.Service;

@Service
public class SymjaService {
  private final ExprEvaluator evaluator = new ExprEvaluator(false, (short) 1);
  private final IAST neededLogPoliciesFn = neededLogPoliciesFn();
  private final IAST maxBasicUsersGivenPremiumFn = maxBasicUsersGivenPremiumFn();

  public Number computeMaxBasicUsersGivenPremium(int premiumUserNb) {
    IExpr res = evaluator.eval(F.ast(maxBasicUsersGivenPremiumFn, List.of(ZZ(premiumUserNb))));
    if (res.isNegativeResult()) {
      throw new IllegalArgumentException(
          "cannot compute max basic users given premium for " + premiumUserNb);
    }
    return res.toNumber();
  }

  public Number computeNeededLogPolicies(int nbApps, int nbOrgs) {
    IExpr res = evaluator.eval(F.ast(neededLogPoliciesFn, List.of(ZZ(nbApps), ZZ(nbOrgs))));
    if (res.isNegativeResult()) {
      throw new IllegalArgumentException(
          "could not compute neededLogPolicies for " + nbApps + " apps, " + nbOrgs + " orgs");
    }
    return res.toNumber();
  }

  private IAST maxBasicUsersGivenPremiumFn() {
    IInteger maxLogPolicies = ZZ(500);
    IInteger nbPremiumUserApps = ZZ(12);
    IInteger nbPremiumUserOrgs = ZZ(3);
    IInteger nbBasicUserApps = ZZ(2);
    IInteger nbBasicUserOrgs = ZZ(1);
    ISymbol dummyNbPremiumUsers = Dummy("nbPremiumUsers");
    ISymbol dummyPremiumLogPolicies = Dummy("premiumLogPolicies");
    ISymbol dummyBasicLogPolicies = Dummy("basicLogPolicies");
    return Function(
        F.List(dummyNbPremiumUsers),
        Module(
            F.List(dummyPremiumLogPolicies, dummyBasicLogPolicies),
            CompoundExpression(
                Set(
                    dummyPremiumLogPolicies,
                    F.ast(neededLogPoliciesFn, List.of(nbPremiumUserApps, nbPremiumUserOrgs))),
                Set(
                    dummyBasicLogPolicies,
                    F.ast(neededLogPoliciesFn, List.of(nbBasicUserApps, nbBasicUserOrgs))),
                Divide(
                    Subtract(maxLogPolicies, Times(dummyNbPremiumUsers, dummyPremiumLogPolicies)),
                    dummyBasicLogPolicies))));
  }

  private static IAST neededLogPoliciesFn() {
    ISymbol nbApps = Dummy("nbApps");
    ISymbol nbOrgs = Dummy("nbOrgs");
    ISymbol distributions = Dummy("distributions");
    ISymbol boxCounts = Dummy("boxCounts");

    return Function(
        F.List(nbApps, nbOrgs),
        Module(
            F.List(distributions, boxCounts),
            CompoundExpression(
                Set(
                    distributions,
                    Select(
                        IntegerPartitions.of(nbApps, F.List(nbOrgs)),
                        Function(AllTrue.of(Slot1, NonNegative)))),
                Set(
                    boxCounts,
                    Map(
                        Function(Total(Map(Function(Ceiling(Times(C1D3, Slot1))), Slot1))),
                        distributions)),
                Max(boxCounts))));
  }
}
