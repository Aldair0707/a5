package junit.a5;

import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

/** Entry point for the Simpson-rule t-distribution integrator. */
public class App {

    private static final double ERROR_TOLERANCE = 0.00001;
    private static final String DEFAULT_OUTPUT = "a5-results.txt";

    public static void main(String[] args) {
        try {
            double x;
            int dof;
            int numSegments;
            String outFile = DEFAULT_OUTPUT;

            if (args.length == 0) {
                Scanner console = new Scanner(System.in);
                x = readDouble(console, "x : ");
                dof = readInteger(console, "dof : ");
                numSegments = readInteger(console,
                        "num_seg inicial: ");
            } else if (args.length == 3 || args.length == 4) {
                x = Double.parseDouble(args[0]);
                dof = Integer.parseInt(args[1]);
                numSegments = Integer.parseInt(args[2]);
                if (args.length == 4) {
                    outFile = args[3];
                }
            } else {
                printUsage();
                return;
            }

            Logic logic = new Logic(numSegments, ERROR_TOLERANCE, dof, x);
            double result = logic.logic1a();
            String resultText = String.format(Locale.US, "%.10f%n", result);

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
        System.err.println("Enter the three inputs in this order: x, dof, num_seg.");
        System.err.println("Interactive: java junit.a5.App");
        System.err.println("Arguments:   java junit.a5.App <x> <dof> <num_seg> [outputFile]");
    }
}
