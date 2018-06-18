/*
 * Copyright (c) 2018 RGS Group.
 * Any use of this code without the RGS express written consent is prohibited.
 */

package ru.rgs.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.XML;

import java.io.IOException;
import java.util.HashMap;

public final class Converter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Converter() {}

    public static Object xmlAsDataStructure(String value) {
        try {
            return OBJECT_MAPPER.readValue(XML.toJSONObject(value).toString(), HashMap.class);
        } catch (RuntimeException | IOException e) {
            return "Converting xml to map failed: " + e.getMessage();
        }
    }

    public static Object jsonAsDataStructure(String value) {
        try {
            return OBJECT_MAPPER.readValue(value, HashMap.class);
        } catch (RuntimeException | IOException e) {
            return "Converting json to map failed: " + e.getMessage();
        }
    }
}