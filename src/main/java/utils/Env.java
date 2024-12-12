package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Env {
    public static Properties readEnvVarFile(String envFileName) throws IOException {
        var props = new Properties();
        var envFile = Paths.get(envFileName);
        var inputStream = Files.newInputStream(envFile);
        props.load(inputStream);
        inputStream.close();
        return props;
    }
}
