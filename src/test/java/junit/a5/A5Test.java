package junit.a5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests acceptance values and the application's input, validation, and output paths. */
public class A5Test {

    private static final double ERROR_TOLERANCE = 0.00001;
    private static final int INITIAL_SEGMENTS = 10;

    @TempDir
    Path tempDir;

    @Test
    void integratesToX11WithNineDegreesOfFreedom() {
        assertEquals(0.35006, integrate(1.1, 9), 0.000005);
    }

    @Test
    void integratesToX11812WithTenDegreesOfFreedom() {
        assertEquals(0.36757, integrate(1.1812, 10), 0.000005);
    }

    @Test
    void integratesToX275WithThirtyDegreesOfFreedom() {
        assertEquals(0.49500, integrate(2.750, 30), 0.000005);
    }

    @Test
    void gammaFunctionSupportsIntegerAndHalfIntegerInputs() {
        GammaFunction gamma = new GammaFunction();

        assertEquals(1.0, gamma.computeIntGamma(1), 0.0);
        assertEquals(24.0, gamma.computeIntGamma(5), 0.0);
        assertEquals(1.0, gamma.computeDblGamma(1.0), 0.0);
        assertEquals(Math.sqrt(Math.PI), gamma.computeDblGamma(0.5), 1.0e-12);
        assertEquals(1.329340388179137, gamma.computeDblGamma(2.5), 1.0e-12);
        assertEquals(1.329340388179137, gamma.getGammaValue(), 1.0e-12);
    }

    @Test
    void gammaFunctionRejectsInvalidInputs() {
        GammaFunction gamma = new GammaFunction();

        assertThrows(IllegalArgumentException.class, () -> gamma.computeIntGamma(0));
        assertThrows(IllegalArgumentException.class, () -> gamma.computeDblGamma(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> gamma.computeDblGamma(0.0));
        assertThrows(IllegalArgumentException.class, () -> gamma.computeDblGamma(2147483648.0));
        assertThrows(IllegalArgumentException.class, () -> gamma.computeDblGamma(1.2));
    }

    @Test
    void logicRejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> new Logic(1, ERROR_TOLERANCE, 9, 1.1));
        assertThrows(IllegalArgumentException.class,
                () -> new Logic(3, ERROR_TOLERANCE, 9, 1.1));
        assertThrows(IllegalArgumentException.class,
                () -> new Logic(10, Double.NaN, 9, 1.1));
        assertThrows(IllegalArgumentException.class,
                () -> new Logic(10, 0.0, 9, 1.1));
        assertThrows(IllegalArgumentException.class,
                () -> new Logic(10, ERROR_TOLERANCE, 0, 1.1));
        assertThrows(IllegalArgumentException.class,
                () -> new Logic(10, ERROR_TOLERANCE, 9, Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new Logic(10, ERROR_TOLERANCE, 9, -1.0));
    }

    @Test
    void logicConvergesAndFormatsItsReport() {
        Logic logic = new Logic(2, 1.0e-12, 9, 1.1);
        assertThrows(IllegalStateException.class, logic::getDetailedReport);

        double result = logic.logic1a();

        assertEquals(0.35005864, result, 0.000005);
        assertTrue(logic.getNumSegments() > 2);
        assertTrue(logic.getFinalDifference() <= 1.0e-12);
        assertTrue(logic.getDetailedReport().contains("Simpson table for the final approximation"));
    }

    @Test
    void logicStopsAtTheSegmentLimit() {
        Logic logic = new Logic(1_048_576, Double.MIN_VALUE, 1, 0.0);

        assertThrows(IllegalStateException.class, logic::logic1a);
    }

    @Test
    void simpsonIntegrationValidatesAndFormatsTheCalculation() {
        assertThrows(IllegalArgumentException.class,
                () -> new SimpsonIntegration(0, ERROR_TOLERANCE, 9, 1.1));
        assertThrows(IllegalArgumentException.class,
                () -> new SimpsonIntegration(3, ERROR_TOLERANCE, 9, 1.1));

        SimpsonIntegration integration =
                new SimpsonIntegration(10, ERROR_TOLERANCE, 9, 1.1);
        assertThrows(IllegalStateException.class, integration::formatTable);

        SimpsonIntegration partialIntegration =
                new SimpsonIntegration(10, ERROR_TOLERANCE, 9, 1.1);
        partialIntegration.computeW(10, 1.1);
        partialIntegration.computeXi(10);
        assertThrows(IllegalStateException.class, partialIntegration::formatTable);

        assertEquals(0.3500589043, integration.integrate(), 1.0e-10);
        assertEquals(0.11, integration.getW(), 1.0e-12);
        assertEquals(0.3500589043, integration.getFinalValue(), 1.0e-10);
        assertTrue(integration.formatTable().contains("weighted f(xi)"));
    }

    @Test
    void outputWritesUtf8AndHandlesAPathWithoutAParent() throws IOException {
        Output output = new Output();
        Path result = tempDir.resolve("nested/result.txt");

        output.writeData(result.toString(), "0.3500586369\n");

        assertEquals("0.3500586369\n", Files.readString(result));
        assertThrows(IOException.class, () -> output.writeData("/", "0.0\n"));
    }

    @Test
    void appAcceptsInteractiveAndArgumentInputs() throws IOException {
        new App();

        CapturedOutput interactive = runApp(new String[0], "1.1\n9\n10\n");
        assertTrue(interactive.stdout.contains("0.3500586369"));
        assertEquals("", interactive.stderr);

        CapturedOutput threeArguments = runApp(
                new String[] {"1.1", "9", "10"}, "");
        assertTrue(threeArguments.stdout.contains("0.3500586369"));

        Path result = tempDir.resolve("custom-result.txt");
        CapturedOutput fourArguments = runApp(
                new String[] {"1.1", "9", "10", result.toString()}, "");
        assertTrue(fourArguments.stdout.contains("0.3500586369"));
        assertEquals("0.3500586369\n", Files.readString(result));
    }

    @Test
    void appReportsInvalidArgumentsAndInput() throws IOException {
        assertTrue(runApp(new String[] {"1"}, "").stderr.contains("Arguments:"));
        assertTrue(runApp(new String[] {"not-a-number", "9", "10"}, "")
                .stderr.contains("Input error:"));
        assertTrue(runApp(new String[] {"1.1", "9", "3"}, "")
                .stderr.contains("Input error:"));

        assertTrue(runApp(new String[0], "").stderr.contains("missing console input"));
        assertTrue(runApp(new String[0], "1.1\n").stderr.contains("missing console input"));
        assertTrue(runApp(new String[0], "1.1\n9\n").stderr.contains("missing console input"));
        assertTrue(runApp(new String[0], "bad-x\n9\n10\n").stderr.contains("Input error:"));
        assertTrue(runApp(new String[0], "1.1\nbad-dof\n10\n").stderr.contains("Input error:"));
    }

    @Test
    void appReportsOutputFileErrors() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("output-is-a-directory"));

        CapturedOutput output = runApp(
                new String[] {"1.1", "9", "10", directory.toString()}, "");

        assertTrue(output.stderr.contains("Could not write the output file:"));
    }

    private double integrate(double x, int dof) {
        return new Logic(INITIAL_SEGMENTS, ERROR_TOLERANCE, dof, x).logic1a();
    }

    private CapturedOutput runApp(String[] args, String consoleInput) throws IOException {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        Path defaultOutput = Paths.get("a5-results.txt");
        boolean outputExisted = Files.exists(defaultOutput);
        byte[] previousOutput = outputExisted ? Files.readAllBytes(defaultOutput) : null;
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream(consoleInput.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(stdout, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(stderr, true, StandardCharsets.UTF_8));
            App.main(args);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
            System.setErr(originalErr);
            if (outputExisted) {
                Files.write(defaultOutput, previousOutput);
            } else {
                Files.deleteIfExists(defaultOutput);
            }
        }

        return new CapturedOutput(
                stdout.toString(StandardCharsets.UTF_8),
                stderr.toString(StandardCharsets.UTF_8));
    }

    private static class CapturedOutput {
        private final String stdout;
        private final String stderr;

        private CapturedOutput(String stdout, String stderr) {
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
