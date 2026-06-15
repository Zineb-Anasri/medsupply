package com.medsupply.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Shared Gson instance with java.time type adapters.
 *
 * <p>Plain {@code new Gson()} cannot (de)serialize {@link LocalDateTime}/{@link LocalDate}
 * on JDK 9+ — it reflects into their private fields and throws
 * "Failed making field java.time.LocalDateTime#date accessible". Every servlet/util that
 * serialises model objects (which carry createdAt/updatedAt/dueDate fields) must use
 * {@link #GSON} instead so dates are emitted as ISO-8601 strings.
 */
public final class JsonUtil {

    private JsonUtil() {}

    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
        .create();

    public static Gson gson() {
        return GSON;
    }

    /** Strips a trailing zone/offset/fractional-seconds so loose inputs still parse. */
    private static String normalize(String s) {
        return s.replace("Z", "")
                .replaceAll("\\+[0-9]{2}:[0-9]{2}$", "")
                .replaceAll("\\.[0-9]+$", "");
    }

    private static final class LocalDateTimeAdapter
            implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type type, JsonSerializationContext ctx) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
        }
        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) {
            if (json == null || json.isJsonNull()) return null;
            try {
                return LocalDateTime.parse(normalize(json.getAsString()));
            } catch (Exception e) {
                return null;
            }
        }
    }

    private static final class LocalDateAdapter
            implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate src, Type type, JsonSerializationContext ctx) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
        }
        @Override
        public LocalDate deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) {
            if (json == null || json.isJsonNull()) return null;
            try {
                return LocalDate.parse(json.getAsString().substring(0, 10));
            } catch (Exception e) {
                return null;
            }
        }
    }
}
