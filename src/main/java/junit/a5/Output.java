package junit.a5;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Writes the calculation report to a UTF-8 text file. */
public class Output {

    public void writeData(String outFile, String outText) throws IOException {
        Path path = Paths.get(outFile);
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(path, outText.getBytes(StandardCharsets.UTF_8));
    }
}
