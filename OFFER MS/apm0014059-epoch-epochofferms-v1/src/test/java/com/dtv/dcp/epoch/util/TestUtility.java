package com.dtv.dcp.epoch.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class TestUtility {

    public static String loadJson(String resourceName) {
        try {
            Stream stream = Files.lines(Paths.get(
                    TestUtility.class.getResource(resourceName).toURI()));
            StringBuilder stringBuilder = new StringBuilder();
            stream.forEach(value -> stringBuilder.append(value));
            return stringBuilder.toString();
        } catch (URISyntaxException | IOException exceptions) {
            return "";
        }
    }
}
