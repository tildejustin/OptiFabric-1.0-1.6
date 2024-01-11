package me.modmuss50.optifabric.mod;

import net.fabricmc.api.*;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.include.com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Environment(EnvType.CLIENT)
public class Optifabric {
    public static String error = null;
    private static final Map<String, List<String>> excludedClasses = new HashMap<>();
    private static final Path excludeConfig = FabricLoader.getInstance().getConfigDir().resolve("optifabric-excluded-classes.json");

    static {
        try {
            if (Files.exists(excludeConfig)) {
                JsonReader reader = new JsonReader(new FileReader(excludeConfig.toFile()));
                reader.beginObject();
                while (reader.hasNext()) {
                    String version = reader.nextName();
                    List<String> classes = new ArrayList<>();
                    reader.beginArray();
                    while (reader.hasNext()) {
                        classes.add(reader.nextString());
                    }
                    excludedClasses.put(version, classes);
                }
                reader.close();
            }
        } catch (IOException ignored) {
        }
    }

    public static boolean hasError() {
        return Optifabric.error != null;
    }

    public static List<String> getExcludedClasses() {
        return excludedClasses.getOrDefault(OptifineVersion.version, Collections.emptyList());
    }
}
