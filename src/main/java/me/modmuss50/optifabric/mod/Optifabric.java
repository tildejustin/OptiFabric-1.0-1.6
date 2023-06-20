package me.modmuss50.optifabric.mod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.include.com.google.gson.Gson;
import org.spongepowered.include.com.google.gson.GsonBuilder;

import java.io.*;

@Environment(EnvType.CLIENT)
public class Optifabric {

    public static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("optifabric.json").toFile();
    public static Optifabric.Config config;

    public static void checkForErrors() {
        if (OptifabricError.hasError()) {
            System.out.println("An Optifabric error has occurred");
        }
    }

    public static ClassExcluder getVersionExcluder() {
        for (ClassExcluder classExcluder : config.excluded) {
            if (classExcluder.version.equals(OptifineVersion.version)) {
                return classExcluder;
            }
        }
        return null;
    }

    public Config getConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (CONFIG_FILE.createNewFile()) {
                try (Writer writer = new FileWriter(CONFIG_FILE)) {
                    gson.toJson(new Config(false, new ClassExcluder[]{new ClassExcluder("OptiFine_1.3.2_L_B2", new String[]{"ik.class"})}), writer);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(CONFIG_FILE));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gson.fromJson(bufferedReader, Config.class);
    }

    @SuppressWarnings("unused")
    public class Config {
        public boolean alwayRecache;
        public ClassExcluder[] excluded;

        public Config(Boolean alwaysRecache, ClassExcluder[] excluded) {
            this.alwayRecache = alwaysRecache;
            this.excluded = excluded;
        }

//        public Config(Boolean alwaysRecache) {
//            this.alwayRecache = alwaysRecache;
//            this.excludedClasses = null;
//        }
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class ClassExcluder {
        public String version;
        public String[] classes;

        ClassExcluder(String version, String[] classes) {
            this.version = version;
            this.classes = classes;
        }
    }
}
