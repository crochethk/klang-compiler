package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Env {
    /**
     * Parses the specified file containing environment variables into a property set.
     * This method silently ignores IO errors (an empty set is returned in this case).
     */
    public static Properties readEnvVarFile(String envFileName) {
        var props = new Properties();
        var envFile = Paths.get(envFileName);
        try (var inputStream = Files.newInputStream(envFile)) {
            props.load(inputStream);
        } catch (IOException ioe) {
            //ignore
        }
        return props;
    }
}
