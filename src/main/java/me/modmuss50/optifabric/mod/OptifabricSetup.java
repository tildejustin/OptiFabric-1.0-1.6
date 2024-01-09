package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;
import me.modmuss50.optifabric.Pair;
import me.modmuss50.optifabric.patcher.ClassCache;
import net.fabricmc.loader.api.*;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.*;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class OptifabricSetup implements Runnable {
    public static final String OPTIFABRIC_INCOMPATIBLE = "optifabric:incompatible";
    public static Path optifineRuntimeJar = null;
    private static final String optifineMixinConfiguration = "optifabric.optifine.mixins.json";

    // this is called early on to allow us to get the transformers in before minecraft starts
    @Override
    public void run() {
        if (!this.validateMods()) {
            return;
        }
        try {
            Pair<Path, ClassCache> runtime = new OptifineSetup().getRuntime();

            // add the optifine jar without classes to be replaced to the classpath
            ClassTinkerers.addURL(runtime.left().toUri().toURL());

            OptifineInjector injector = new OptifineInjector(runtime.right());
            injector.setup();

            optifineRuntimeJar = runtime.left();
        } catch (Throwable e) {
            if (!Optifabric.hasError()) {
                OptifineVersion.jarType = OptifineVersion.JarType.INCOMPATIBLE;
                Optifabric.error = "Failed to load optifine, check the log for more info \n\n " + e.getMessage();
            }
            throw new RuntimeException("Failed to setup optifine", e);
        }
        if (hasOptifineMixins()) {
            Mixins.addConfiguration(optifineMixinConfiguration);
        }
    }

    private boolean validateMods() {
        List<String> incompatibleMods = FabricLoader.getInstance().getAllMods().stream()
                .map(ModContainer::getMetadata)
                .filter(metadata -> metadata.containsCustomValue(OPTIFABRIC_INCOMPATIBLE))
                .map(metadata -> String.format("%s (%s)\n", metadata.getName(), metadata.getId()))
                .collect(Collectors.toList());
        if (!incompatibleMods.isEmpty()) {
            OptifineVersion.jarType = OptifineVersion.JarType.INCOMPATIBLE;
            StringBuilder errorMessage = new StringBuilder()
                    .append("one or more mods have stated they are incompatible with Optifabric").append("\n")
                    .append("please remove Optifabric or the following mods:").append("\n");
            incompatibleMods.forEach(errorMessage::append);
            Optifabric.error = errorMessage.toString();
            return false;
        }
        return true;
    }

    private static boolean hasOptifineMixins() {
        IMixinService service = MixinService.getService();
        try (InputStream resource = service.getResourceAsStream(optifineMixinConfiguration)) {
            return resource != null;
        } catch (IOException e) {
            return false;
        }
    }
}
