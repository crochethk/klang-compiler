package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Env {
    public static Properties readEnvVarFile(String envFileName) {
        var props = new Properties();
        var envFile = Paths.get(envFileName);
        try (var inputStream = Files.newInputStream(envFile)) {
            props.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            System.err.println("Error reading file '" + envFile.toAbsolutePath()
                    + "'. Does it exist?");
        }
        return props;
    }
}
