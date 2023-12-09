package me.modmuss50.optifabric.mod;

import me.modmuss50.optifabric.*;
import me.modmuss50.optifabric.patcher.*;
import net.fabricmc.loader.api.*;
import net.fabricmc.loader.impl.launch.*;
import net.fabricmc.loader.impl.lib.tinyremapper.IMappingProvider;
import net.fabricmc.loader.impl.util.mappings.TinyRemapperMappingsHelper;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class OptifineSetup {
    private final File workingDir = FabricLoader.getInstance().getGameDir().resolve(".optifine").toFile();
    private final MappingConfiguration mappingConfiguration = new MappingConfiguration();

    private static Path getLaunchMinecraftJar() {
        try {
            return (Path) FabricLoader.getInstance().getObjectShare().get("fabric-loader:inputGameJar");
        } catch (NoClassDefFoundError | NoSuchMethodError old) {
            ModContainer mod = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow(() -> new IllegalStateException("No Minecraft?"));
            URI uri = mod.getRootPaths().get(0).toUri();
            assert "jar".equals(uri.getScheme());

            String path = uri.getSchemeSpecificPart();
            int split = path.lastIndexOf("!/");

            if (path.substring(0, split).indexOf(' ') > 0 && path.startsWith("file:///")) {//This is meant to be a URI...
                Path out = Paths.get(path.substring(8, split));
                if (Files.exists(out)) return out;
            }

            try {
                return Paths.get(new URI(path.substring(0, split)));
            } catch (URISyntaxException e) {
                throw new RuntimeException("Failed to find Minecraft jar from " + uri + " (calculated " + path.substring(0, split) + ')', e);
            }
        }
    }

    public Pair<File, ClassCache> getRuntime() throws Throwable {
        if (!workingDir.exists()) {
            workingDir.mkdirs();
        }
        File optifineModJar = OptifineVersion.findOptifineJar();

        byte[] modHash = IOUtils.fileHash(optifineModJar);

        File versionDir = new File(workingDir, OptifineVersion.version);
        if (!versionDir.exists()) {
            versionDir.mkdirs();
        }

        File remappedJar = new File(versionDir, "optifine-mapped.jar");
        File optifinePatches = new File(versionDir, "optifine.classes");

        ClassCache classCache = null;
        if (remappedJar.exists() && optifinePatches.exists()) {
            classCache = ClassCache.read(optifinePatches);
            //Validate that the classCache found is for the same input jar
            if (!Arrays.equals(classCache.getHash(), modHash)) {
                System.out.println("Class cache is from a different optifine jar, deleting and re-generating");
                classCache = null;
                optifinePatches.delete();
            }
        }

        if (remappedJar.exists() && classCache != null) {
            System.out.println("Found existing patched optifine jar, using that");
            return Pair.of(remappedJar, classCache);
        }

        System.out.println("Setting up optifine for the first time, this may take a few seconds.");

        //A jar without srgs
        File jarOfTheFree = new File(versionDir, "/optifine-jar-of-the-free.jar");
        List<String> srgs = new ArrayList<>();

        //noinspection SpellCheckingInspection
        System.out.println("De-Volderfiying jar");

        //Find all the SRG named classes and remove them
        ZipUtil.iterate(optifineModJar, (in, zipEntry) -> {
            String name = zipEntry.getName();
            if (name.startsWith("com/mojang/blaze3d/platform/")) {
                if (name.contains("$")) {
                    String[] split = name.replace(".class", "").split("\\$");
                    if (split.length >= 2) {
                        if (split[1].length() > 2) {
                            srgs.add(name);
                        }
                    }
                }
            }

            if (name.startsWith("srg/") || name.startsWith("net/minecraft/")) {
                srgs.add(name);
            }
        });

        if (jarOfTheFree.exists()) {
            jarOfTheFree.delete();
        }

        ZipUtil.removeEntries(optifineModJar, srgs.toArray(new String[0]), jarOfTheFree);

        System.out.println("Building lambda fix mappings");
        LambdaRebuilder rebuilder = new LambdaRebuilder(jarOfTheFree, this.getMinecraftJar().toFile());
        rebuilder.buildLambdaMap();

        System.out.println("Remapping optifine with fixed lambda names");
        File lambdaFixJar = new File(versionDir, "optifine-lambda-fix.jar");
        RemapUtils.mapJar(lambdaFixJar.toPath(), jarOfTheFree.toPath(), rebuilder, this.getLibs());

        this.remapOptifine(lambdaFixJar.toPath(), remappedJar.toPath());

        classCache = PatchSplitter.generateClassCache(remappedJar, optifinePatches, modHash);

        //We are done, lets get rid of the stuff we no longer need
        lambdaFixJar.delete();
        jarOfTheFree.delete();

        boolean extractClasses = Boolean.parseBoolean(System.getProperty("optifabric.extract", "false"));
        if (extractClasses) {
            System.out.println("Extracting optifine classes");
            File optifineClasses = new File(versionDir, "optifine-classes");
            if (optifineClasses.exists()) {
                IOUtils.deleteDirectory(optifineClasses);
            }
            ZipUtil.unpack(remappedJar, optifineClasses);
        }

        return Pair.of(remappedJar, classCache);
    }

    private void remapOptifine(Path input, Path remappedJar) throws Exception {
        String namespace = FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace();
        System.out.println("Remapping optifine to " + namespace);

        List<Path> mcLibs = getLibs();
        mcLibs.add(getMinecraftJar());

        RemapUtils.mapJar(remappedJar, input, createMappings("official", namespace), mcLibs);
    }

    IMappingProvider createMappings(@SuppressWarnings("SameParameterValue") String from, String to) {
        return TinyRemapperMappingsHelper.create(mappingConfiguration.getMappings(), from, to);
    }

    //Gets the minecraft libraries
    List<Path> getLibs() {
        return FabricLauncherBase.getLauncher().getClassPath().stream().filter(Files::exists).collect(Collectors.toList());
    }

    //Gets the official minecraft jar
    @SuppressWarnings("SpellCheckingInspection")
    Path getMinecraftJar() {
        String givenJar = System.getProperty("optifabric.mc-jar");
        if (givenJar != null) {
            File givenJarFile = new File(givenJar);

            if (givenJarFile.exists()) {
                return givenJarFile.toPath();
            } else {
                System.err.println("Supplied Minecraft jar at " + givenJar + " doesn't exist, falling back");
            }
        }

        Path minecraftJar = getLaunchMinecraftJar();

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Path officialNames = minecraftJar.resolveSibling(String.format("minecraft-%s-client.jar", OptifineVersion.minecraftVersion));

            if (Files.notExists(officialNames)) {
                Path parent = minecraftJar.getParent().resolveSibling(String.format("minecraft-%s-client.jar", OptifineVersion.minecraftVersion));

                if (Files.notExists(parent)) {
                    Path alternativeParent = parent.resolveSibling("minecraft-client.jar");

                    if (Files.notExists(alternativeParent)) {
                        throw new AssertionError("Unable to find Minecraft dev jar! Tried " + officialNames + ", " + parent + " and " + alternativeParent
                                + "\nPlease supply it explicitly with -Doptifabric.mc-jar");
                    }

                    parent = alternativeParent;
                }

                officialNames = parent;
            }

            minecraftJar = officialNames;
        }

        return minecraftJar;
    }
}
