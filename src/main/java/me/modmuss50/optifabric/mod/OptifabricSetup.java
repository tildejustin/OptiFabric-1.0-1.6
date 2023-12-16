package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;
import me.modmuss50.optifabric.Pair;
import me.modmuss50.optifabric.metadata.OptifineContainer;
import me.modmuss50.optifabric.patcher.ClassCache;
import net.fabricmc.loader.api.*;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.impl.FabricLoaderImpl;

import java.io.File;
import java.util.*;

@SuppressWarnings("unused")
public class OptifabricSetup implements Runnable {
    public static final String OPTIFABRIC_INCOMPATIBLE = "optifabric:incompatible";
    public static File optifineRuntimeJar = null;

    // this is called early on to allow us to get the transformers in before minecraft starts
    @Override
    public void run() {
        if (!validateMods()) return;
        try {
            OptifineSetup optifineSetup = new OptifineSetup();
            Pair<File, ClassCache> runtime = optifineSetup.getRuntime();

            // add the optifine jar to the classpath, as
            ClassTinkerers.addURL(runtime.left().toURI().toURL());

            OptifineInjector injector = new OptifineInjector(runtime.right());
            injector.setup();

            optifineRuntimeJar = runtime.left();

            try {
                //noinspection unchecked
                ((List<ModContainer>) (List<? extends ModContainer>) FabricLoaderImpl.InitHelper.get().getModsInternal()).add(new OptifineContainer(runtime.left().toPath(), Version.parse(OptifineVersion.version)));
            } catch (VersionParsingException e) {
                throw new RuntimeException(e);
            }
        } catch (Throwable e) {
            if (!Optifabric.hasError()) {
                OptifineVersion.jarType = OptifineVersion.JarType.INCOMPATIBLE;
                Optifabric.error = "Failed to load optifine, check the log for more info \n\n " + e.getMessage();
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
            StringBuilder errorMessage = new StringBuilder("one or more mods have stated they are incompatible with optifabric\nplease remove optifabric or the following mods:\n");
            for (ModMetadata metadata : incompatibleMods) {
                errorMessage.append(metadata.getName()).append(" (").append(metadata.getId()).append(")\n");
            }
            Optifabric.error = errorMessage.toString();
        }
        return incompatibleMods.isEmpty();
    }
}
