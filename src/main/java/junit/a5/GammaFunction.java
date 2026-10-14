package junit.a5;

/** Gamma function implementation for the integer and half-integer inputs used by the t density. */
public class GammaFunction {

    private double gammaValue;

    public double computeIntGamma(int intValue) {
        if (intValue < 1) {
            throw new IllegalArgumentException("Gamma is only defined here for positive integers");
        }

        gammaValue = 1.0;
        for (int factor = 2; factor < intValue; factor++) {
            gammaValue *= factor;
        }
        return gammaValue;
    }

    public double computeDblGamma(double doubleValue) {
        if (!Double.isFinite(doubleValue) || doubleValue <= 0.0) {
            throw new IllegalArgumentException("Gamma is only defined here for positive finite values");
        }

        double nearestInteger = Math.rint(doubleValue);
        if (Math.abs(doubleValue - nearestInteger) < 1.0e-10) {
            if (nearestInteger > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Gamma argument is too large");
            }
            return computeIntGamma((int) nearestInteger);
        }

        double twiceValue = doubleValue * 2.0;
        if (Math.abs(twiceValue - Math.rint(twiceValue)) >= 1.0e-10) {
            throw new IllegalArgumentException("this implementation expects an integer or half-integer Gamma argument");
        }

        gammaValue = Math.sqrt(Math.PI);
        double current = doubleValue;
        while (current > 0.5) {
            gammaValue *= current - 1.0;
            current -= 1.0;
        }
        return gammaValue;
    }

    public double getGammaValue() {
        return gammaValue;
    }
}
