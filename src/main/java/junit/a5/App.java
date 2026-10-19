package junit.a5;

import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

/** Entry point for the Simpson-rule t-distribution integrator. */
public class App {

    private static final double ERROR_TOLERANCE = 0.00001;
    private static final int NUM_SEG = 20;
    private static final String DEFAULT_OUTPUT = "a5-results.txt";

    public static void main(String[] args) {
        try {
            double lowerBound;
            double upperBound;
            int dof;
            String outFile = DEFAULT_OUTPUT;

            if (args.length == 0) {
                Scanner console = new Scanner(System.in);
                lowerBound = readDouble(console, "a: ");
                upperBound = readDouble(console, "x: ");
                dof = readInteger(console, "dof : ");
            } else if (args.length == 3 || args.length == 4) {
                lowerBound = Double.parseDouble(args[0]);
                upperBound = Double.parseDouble(args[1]);
                dof = Integer.parseInt(args[2]);
                if (args.length == 4) {
                    outFile = args[3];
                }
            } else {
                printUsage();
                return;
            }

            Logic logic = new Logic(
                    NUM_SEG, ERROR_TOLERANCE, dof, lowerBound, upperBound);
            double result = logic.logic1a();
            String resultText = String.format(Locale.US, "%.5f%n", result);

            System.out.print(resultText);
            new Output().writeData(outFile, resultText);
        } catch (IllegalArgumentException exception) {
            System.err.println("Input error: " + exception.getMessage());
            printUsage();
        } catch (IOException exception) {
            System.err.println("Could not write the output file: " + exception.getMessage());
        }
    }

    private static double readDouble(Scanner console, String prompt) {
        System.out.print(prompt);
        if (!console.hasNextLine()) {
            throw new IllegalArgumentException("missing console input");
        }
        return Double.parseDouble(console.nextLine().trim());
    }

    private static int readInteger(Scanner console, String prompt) {
        System.out.print(prompt);
        if (!console.hasNextLine()) {
            throw new IllegalArgumentException("missing console input");
        }
        return Integer.parseInt(console.nextLine().trim());
    }

    private static void printUsage() {
        System.err.println("Enter the inputs in this order: lowerBound, upperBound, dof.");
        System.err.println("The initial segment count num_seg is fixed at " + NUM_SEG + ".");
        System.err.println("Interactive: java junit.a5.App");
        System.err.println("Arguments:   java junit.a5.App <lowerBound> <upperBound> <dof> [outputFile]");
    }
}
