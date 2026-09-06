package com.bazarfx.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic helper for reading/writing a List<T> to a JSON file under /data.
 * This is the whole "persistence layer" for the project - deliberately no database.
 * Reads and writes are synchronized because multiple background threads
 * (auto-save, order simulator, report generator) may touch storage concurrently.
 */
public class JsonStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Object LOCK = new Object();
    public static final String DATA_DIR = "data";

    static {
        try {
            Files.createDirectories(Path.of(DATA_DIR));
            Files.createDirectories(Path.of(DATA_DIR, "images"));
        } catch (IOException e) {
            throw new RuntimeException("Could not create data directory", e);
        }
    }

    public static <T> List<T> loadList(String fileName, Type listType) {
        synchronized (LOCK) {
            Path path = Path.of(DATA_DIR, fileName);
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }
            try (FileReader reader = new FileReader(path.toFile())) {
                List<T> result = GSON.fromJson(reader, listType);
                return result != null ? result : new ArrayList<>();
            } catch (IOException e) {
                System.err.println("Failed to load " + fileName + ": " + e.getMessage());
                return new ArrayList<>();
            }
        }
    }

    public static <T> void saveList(String fileName, List<T> list) {
        synchronized (LOCK) {
            Path path = Path.of(DATA_DIR, fileName);
            try (FileWriter writer = new FileWriter(path.toFile())) {
                GSON.toJson(list, writer);
            } catch (IOException e) {
                System.err.println("Failed to save " + fileName + ": " + e.getMessage());
            }
        }
    }
}
