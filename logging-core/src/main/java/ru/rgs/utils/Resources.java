/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Scanner;

public final class Resources {

    private Resources() {}

    public static String readFromClassPath(Class<?> clazz, String fileName, String defaultValue) {
        try (InputStream inputStream = clazz.getClassLoader().getResourceAsStream(fileName)) {
            return readInputStreamAsString(inputStream);
        } catch (IOException e) {
            return defaultValue;
        }
    }

    private static String readInputStreamAsString(InputStream inputStream) {
        return readInputStreamAsString(inputStream, Charset.defaultCharset());
    }

    private static String readInputStreamAsString(InputStream inputStream, Charset charset) {
        try (Scanner scanner = new Scanner(inputStream, charset.name())) {
            return scanner.useDelimiter("\\Z").next();
        }
    }
}