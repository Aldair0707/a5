package junit.a5;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Coordinates Simpson integrations until successive estimates meet the tolerance. */
public class Logic {

    private static final int MAX_SEGMENTS = 1_048_576;

    private int intNumSeg;
    private final double dblE;
    private final int intDOF;
    private final double dblA;
    private final double dblX;
    private final List<String> iterationRows = new ArrayList<String>();
    private SimpsonIntegration finalIntegration;
    private double finalDifference;

    public Logic(int intNumSeg, double dblE, int intDOF, double dblX) {
        this(intNumSeg, dblE, intDOF, 0.0, dblX);
    }

    public Logic(int intNumSeg, double dblE, int intDOF, double dblA, double dblX) {
        if (intNumSeg < 2 || intNumSeg % 2 != 0) {
            throw new IllegalArgumentException("the initial segment count must be an even number of at least 2");
        }
        if (!Double.isFinite(dblE) || dblE <= 0.0) {
            throw new IllegalArgumentException("E must be a positive finite number");
        }
        if (intDOF <= 0) {
            throw new IllegalArgumentException("degrees of freedom must be a positive integer");
        }
        if (!Double.isFinite(dblA)) {
            throw new IllegalArgumentException("the lower integration bound must be finite");
        }
        if (!Double.isFinite(dblX)) {
            throw new IllegalArgumentException("the upper integration bound must be finite");
        }
        if (dblA > dblX) {
            throw new IllegalArgumentException("the lower bound must not exceed the upper bound");
        }

        this.intNumSeg = intNumSeg;
        this.dblE = dblE;
        this.intDOF = intDOF;
        this.dblA = dblA;
        this.dblX = dblX;
    }

    /**
     * Runs the integration, doubling the even segment count until the change
     * between consecutive estimates is no greater than E.
     */
    public double logic1a() {
        iterationRows.clear();
        double previousValue = Double.NaN;

        while (true) {
            SimpsonIntegration integration = new SimpsonIntegration(
                    intNumSeg, dblE, intDOF, dblA, dblX);
            double value = integration.integrate();
            finalDifference = Double.isNaN(previousValue)
                    ? Double.NaN : Math.abs(value - previousValue);
            iterationRows.add(String.format(Locale.US,
                    "%8d  %12.8f  %16.10f  %16s",
                    intNumSeg, integration.getW(), value,
                    Double.isNaN(finalDifference)
                            ? "-" : String.format(Locale.US, "%.10f", finalDifference)));
            finalIntegration = integration;

            if (!Double.isNaN(finalDifference) && finalDifference <= dblE) {
                return value;
            }
            if (intNumSeg >= MAX_SEGMENTS) {
                throw new IllegalStateException(
                        "the requested tolerance was not reached before the segment limit");
            }

            previousValue = value;
            intNumSeg *= 2;
        }
    }

    public String getDetailedReport() {
        if (finalIntegration == null) {
            throw new IllegalStateException("call logic1a() before requesting the report");
        }

        StringBuilder report = new StringBuilder();
        report.append(String.format(Locale.US,
                "Input: a = %.8f, x = %.8f, dof = %d, E = %.8f\n",
                dblA, dblX, intDOF, dblE));
        report.append("Convergence by segment count\n")
              .append(" segments       W             integral p       difference\n");
        for (String row : iterationRows) {
            report.append(row).append('\n');
        }
        report.append(String.format(Locale.US,
                "Final integral p = %.10f (segments = %d, last difference = %.10f)\n",
                finalIntegration.getFinalValue(), intNumSeg, finalDifference));
        report.append(finalIntegration.formatTable());
        return report.toString();
    }

    public int getNumSegments() {
        return intNumSeg;
    }

    public double getFinalDifference() {
        return finalDifference;
    }
}
