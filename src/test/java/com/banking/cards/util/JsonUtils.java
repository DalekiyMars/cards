package com.banking.cards.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);

    private static final ObjectReader READER = MAPPER.reader();
    private static final ObjectWriter WRITER = MAPPER.writerWithDefaultPrettyPrinter();


    public static <T> String convertJsonFromFileToString(String filePath, Class<T> clazz) throws IOException {
        return WRITER.writeValueAsString(convertJsonFromFileToObject(filePath,clazz));
    }

    public static <T> T convertJsonFromFileToObject(String filePath, Class<T> clazz) throws IOException {
        return READER.readValue(new File(filePath), clazz);
    }

    public static <T> String convertJsonFromObjectToString(T object) throws IOException {
        return WRITER.writeValueAsString(object);
    }

    public static <T> T convertJsonStringToObject(String json, Class<T> clazz) throws JsonProcessingException {
        return MAPPER.readValue(json, clazz);
    }

    public static <T> List<T> convertJsonStringToObjectList(String json, Class<T> clazz){
        String[] rawObj = json.substring(1, json.length()-1).split("},");
        List<T> objs = new ArrayList<>();
        Arrays.stream(rawObj)
                .forEach(s -> {
                    try {
                        objs.add(convertJsonStringToObject(s + "}", clazz));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
        return objs;
    }

    public static <T> String convertListToJson(List<T> list) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(list);
    }
    public static String readFile(String filePath) throws IOException {
        return Files.readString(Path.of(filePath));
    }
}
