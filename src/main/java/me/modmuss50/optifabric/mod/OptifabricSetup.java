package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;
import me.modmuss50.optifabric.patcher.ClassCache;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class OptifabricSetup implements Runnable {

    public static final String OPTIFABRIC_INCOMPATIBLE = "optifabric:incompatible";
    public static File optifineRuntimeJar = null;

    //This is called early on to allow us to get the transformers in before minecraft starts
    @Override
    public void run() {
        if (!validateMods()) return;

        Optifabric.config = new Optifabric().getConfig();

        try {
            OptifineSetup optifineSetup = new OptifineSetup();
            Pair<File, ClassCache> runtime = optifineSetup.getRuntime();

            //Add the optifine jar to the classpath, as
            ClassTinkerers.addURL(runtime.getLeft().toURI().toURL());

            OptifineInjector injector = new OptifineInjector(runtime.getRight());
            injector.setup();

            optifineRuntimeJar = runtime.getLeft();
        } catch (Throwable e) {
            if (!OptifabricError.hasError()) {
                OptifineVersion.jarType = OptifineVersion.JarType.INCOMPATIBLE;
                OptifabricError.setError("Failed to load optifine, check the log for more info \n\n " + e.getMessage());
            }
            throw new RuntimeException("Failed to setup optifine", e);
        }
    }

    private boolean validateMods() {
        List<ModMetadata> incompatibleMods = new ArrayList<>();
        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            ModMetadata metadata = container.getMetadata();
            if (metadata.containsCustomValue(OPTIFABRIC_INCOMPATIBLE)) {
                incompatibleMods.add(metadata);
            }
        }
        if (!incompatibleMods.isEmpty()) {
            OptifineVersion.jarType = OptifineVersion.JarType.INCOMPATIBLE;
            StringBuilder errorMessage = new StringBuilder("One or more mods have stated they are incompatible with Optifabric\nPlease remove Optifabric or the following mods:\n");
            for (ModMetadata metadata : incompatibleMods) {
                errorMessage.append(metadata.getName())
                        .append(" (")
                        .append(metadata.getId())
                        .append(")\n");
            }
            OptifabricError.setError(errorMessage.toString());
        }
        return incompatibleMods.isEmpty();
    }
}
