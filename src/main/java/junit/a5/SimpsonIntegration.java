package junit.a5;

import java.util.Locale;

/** Computes one composite Simpson-rule integral of the Student's t density. */
public class SimpsonIntegration {

    private final int intNumSeg;
    private final double dblE;
    private final int intDOF;
    private final double dblX;

    private double dblW;
    private double[] dblTotXi;
    private double[] dblFirstBaseTerms;
    private double dblExponent;
    private double dblCoeff;
    private double[] dblFxi;
    private double[] dblFinalTerms;
    private double dblFinalValue;

    public SimpsonIntegration(int intNumSeg, double dblE, int intDOF, double dblX) {
        if (intNumSeg < 2 || intNumSeg % 2 != 0) {
            throw new IllegalArgumentException("Simpson's rule requires an even segment count of at least 2");
        }
        this.intNumSeg = intNumSeg;
        this.dblE = dblE;
        this.intDOF = intDOF;
        this.dblX = dblX;
    }

    public double integrate() {
        computeW(intNumSeg, dblX);
        computeXi(intNumSeg);
        computeFirstBaseTerms(intNumSeg, dblTotXi, intDOF);
        computeExponent(intDOF);
        computeCoefficient(intDOF);
        computeFxi(intNumSeg);
        computeFinalTerms(intNumSeg);
        return computeFinalValue(intNumSeg);
    }

    public void computeW(int intNumSeg, double dblX) {
        dblW = dblX / intNumSeg;
    }

    public void computeXi(int intNumSeg) {
        dblTotXi = new double[intNumSeg + 1];
        for (int index = 0; index <= intNumSeg; index++) {
            dblTotXi[index] = index * dblW;
        }
        // Avoid accumulated rounding at the endpoint.
        dblTotXi[intNumSeg] = dblX;
    }

    public void computeFirstBaseTerms(int intNumSeg, double[] dblTotXi, int intDOF) {
        dblFirstBaseTerms = new double[intNumSeg + 1];
        for (int index = 0; index <= intNumSeg; index++) {
            double xi = dblTotXi[index];
            dblFirstBaseTerms[index] = 1.0 + (xi * xi) / intDOF;
        }
    }

    public void computeExponent(int intDOF) {
        dblExponent = -(intDOF + 1.0) / 2.0;
    }

    public void computeCoefficient(int intDOF) {
        GammaFunction gamma = new GammaFunction();
        double numerator = gamma.computeDblGamma((intDOF + 1.0) / 2.0);
        double denominatorGamma = gamma.computeDblGamma(intDOF / 2.0);
        dblCoeff = numerator
                / (Math.sqrt(intDOF * Math.PI) * denominatorGamma);
    }

    public void computeFxi(int intNumSeg) {
        dblFxi = new double[intNumSeg + 1];
        for (int index = 0; index <= intNumSeg; index++) {
            dblFxi[index] = dblCoeff * Math.pow(dblFirstBaseTerms[index], dblExponent);
        }
    }

    public void computeFinalTerms(int intNumSeg) {
        dblFinalTerms = new double[intNumSeg + 1];
        for (int index = 0; index <= intNumSeg; index++) {
            int multiplier;
            if (index == 0 || index == intNumSeg) {
                multiplier = 1;
            } else if (index % 2 == 1) {
                multiplier = 4;
            } else {
                multiplier = 2;
            }
            dblFinalTerms[index] = multiplier * dblFxi[index];
        }
    }

    public double computeFinalValue(int intNumSeg) {
        double sum = 0.0;
        for (int index = 0; index <= intNumSeg; index++) {
            sum += dblFinalTerms[index];
        }
        dblFinalValue = (dblW / 3.0) * sum;
        return dblFinalValue;
    }

    public double getW() {
        return dblW;
    }

    public double getFinalValue() {
        return dblFinalValue;
    }

    public String formatTable() {
        if (dblTotXi == null || dblFinalTerms == null) {
            throw new IllegalStateException("run integrate() before formatting the calculation table");
        }

        StringBuilder table = new StringBuilder();
        table.append("Simpson table for the final approximation\n")
             .append("    i          xi          f(xi)    multiplier   weighted f(xi)\n");
        for (int index = 0; index <= intNumSeg; index++) {
            int multiplier = (index == 0 || index == intNumSeg)
                    ? 1 : (index % 2 == 1 ? 4 : 2);
            table.append(String.format(Locale.US,
                    "%5d  %11.7f  %12.9f  %10d  %15.9f%n",
                    index, dblTotXi[index], dblFxi[index], multiplier,
                    dblFinalTerms[index]));
        }
        return table.toString();
    }
}
