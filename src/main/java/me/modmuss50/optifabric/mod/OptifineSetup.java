package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;
import me.modmuss50.optifabric.*;
import me.modmuss50.optifabric.patcher.*;
import net.fabricmc.loader.api.*;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.IMappingProvider;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.*;

public class OptifineSetup {
    private final Path workingDir = FabricLoader.getInstance().getGameDir().resolve(".optifine");

    private static Path getLaunchMinecraftJar() {
        try {
            // TODO: https://github.com/FabricMC/fabric-loader/pull/876
            // return (Path) ((List<?>) FabricLoader.getInstance().getObjectShare().get("fabric-loader:inputGameJars")).get(0);
            return (Path) FabricLoader.getInstance().getObjectShare().get("fabric-loader:inputGameJar");
        } catch (NoClassDefFoundError | NoSuchMethodError old) {
            ModContainer mod = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow(() -> new IllegalStateException("no minecraft?"));
            URI uri = mod.getRootPaths().get(0).toUri();
            assert "jar".equals(uri.getScheme());

            String path = uri.getSchemeSpecificPart();
            int split = path.lastIndexOf("!/");
            if (path.substring(0, split).indexOf(' ') > 0 && path.startsWith("file:///")) {
                // this is meant to be a uri
                Path out = Paths.get(path.substring(8, split));
                if (Files.exists(out)) {
                    return out;
                }
            }

            try {
                return Paths.get(new URI(path.substring(0, split)));
            } catch (URISyntaxException e) {
                throw new RuntimeException("failed to find minecraft jar from " + uri + " (calculated " + path.substring(0, split) + ')', e);
            }
        }
    }


    /**
     * locates and prepares Optifine for remapping
     *
     * @return pair of remapped Optifine jar with classes intended to be replaced removed and a {@link me.modmuss50.optifabric.patcher.ClassCache} with the rest of the classes which need to be replaced by {@link ClassTinkerers#addReplacement}
     */
    public Pair<Path, ClassCache> getRuntime() throws IOException, NoSuchAlgorithmException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Files.createDirectories(this.workingDir);
        Path optifineModJar = OptifineVersion.findOptifineJar();
        byte[] modHash = IOUtils.fileHash(optifineModJar);
        Path versionDir = this.workingDir.resolve(OptifineVersion.version);
        Files.createDirectories(versionDir);
        Path remappedJar = versionDir.resolve("optifine-mapped.jar");
        Path optifinePatches = versionDir.resolve("optifine.classes");
        ClassCache classCache = null;
        if (Files.exists(remappedJar) && Files.exists(optifinePatches)) {
            classCache = ClassCache.read(optifinePatches.toFile());
            // validate that the class cache found is for the same input jar
            if (!Arrays.equals(classCache.getHash(), modHash)) {
                System.out.println("class cache is from a different optifine jar, deleting and re-generating");
                classCache = null;
                Files.delete(optifinePatches);
            }
        }

        if (Files.exists(remappedJar) && classCache != null) {
            System.out.println("found existing patched optifine jar, using that");
            return new Pair<>(remappedJar, classCache);
        }

        if (OptifineVersion.jarType == OptifineVersion.JarType.OPTIFINE_INSTALLER) {
            Path optifineMod = versionDir.resolve("optifine-mod.jar");
            if (!Files.exists(optifineMod)) {
                OptifineInstaller.extract(optifineModJar, optifineMod, this.getMinecraftJar());
            }
            optifineModJar = optifineMod;
        }

        System.out.println("setting up optifine for the first time, this may take a few seconds.");

        // a jar without srgs
        Path jarOfTheFree = versionDir.resolve("optifine-jar-of-the-free.jar");
        List<String> srgs = new ArrayList<>();

        System.out.println("removing srg named entries from jar");

        // find all the srg named classes and remove them
        try (ZipFile fs = new ZipFile(optifineModJar.toFile())) {
            fs.stream().map(ZipEntry::getName).filter(name -> {
                if (name.startsWith("srg/") || name.startsWith("net/minecraft/")) {
                    return true;
                }
                if (name.startsWith("com/mojang/blaze3d/platform/") && name.contains("$")) {
                    String[] split = name.replace(".class", "").split("\\$");
                    return split.length >= 2 && split[1].length() > 2;
                }
                return false;
            }).forEach(srgs::add);
        }

        Files.deleteIfExists(jarOfTheFree);
        Files.copy(optifineModJar, jarOfTheFree);
        try (FileSystem fs = FileSystems.newFileSystem(jarOfTheFree, null)) {
            for (String s : srgs) {
                Files.deleteIfExists(fs.getPath(s));
            }
        }

        System.out.println("building lambda fix mappings");
        LambdaRebuilder rebuilder = new LambdaRebuilder(jarOfTheFree, this.getMinecraftJar());
        rebuilder.buildLambdaMap();

        System.out.println("remapping optifine with fixed lambda names");
        Path lambdaFixJar = versionDir.resolve("optifine-lambda-fix.jar");
        RemapUtils.mapJar(lambdaFixJar, jarOfTheFree, rebuilder, this.getLibs());

        this.remapOptifine(lambdaFixJar, remappedJar);

        classCache = PatchSplitter.generateClassCache(remappedJar, optifinePatches, modHash);

        // we are done, lets get rid of the stuff we no longer need
        Files.deleteIfExists(lambdaFixJar);
        Files.deleteIfExists(jarOfTheFree);
        if (OptifineVersion.jarType == OptifineVersion.JarType.OPTIFINE_INSTALLER) {
            Files.deleteIfExists(optifineModJar);
        }

        if (Boolean.parseBoolean(System.getProperty("optifabric.extract", "false"))) {
            System.out.println("extracting optifine classes");
            Path optifineClasses = versionDir.resolve("optifine-classes");
            if (Files.exists(optifineClasses)) {
                IOUtils.deleteDirectory(optifineClasses);
            }
            try (ZipFile fs = new ZipFile(remappedJar.toFile())) {
                fs.stream().forEach(entry -> {
                    try {
                        Path p = optifineClasses.resolve(entry.getName());
                        if (entry.isDirectory()) {
                            Files.createDirectories(p);
                        } else {
                            Files.createDirectories(p.getParent());
                            Files.createFile(p);
                            Files.write(p, IOUtils.toByteArray(fs.getInputStream(entry)));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        return new Pair<>(remappedJar, classCache);
    }

    private void remapOptifine(Path input, Path remappedJar) throws IOException {
        String namespace = FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace();
        System.out.println("remapping optifine to " + namespace);
        List<Path> mcLibs = this.getLibs();
        System.out.println(mcLibs);
        mcLibs.add(this.getMinecraftJar());
        RemapUtils.mapJar(remappedJar, input, this.createMappings("official", namespace), mcLibs);
    }

    IMappingProvider createMappings(@SuppressWarnings("SameParameterValue") String from, String to) throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree();
        try (InputStream mappings = FabricLoader.class.getClassLoader().getResourceAsStream("mappings/mappings.tiny")) {
            // you've got bigger problems if you don't have a mappings set
            assert mappings != null;
            MappingReader.read(new InputStreamReader(mappings), tree);
        }
        return TinyRemapperMappingsHelper.create(tree, from, to);
    }

    List<Path> getLibs() {
        return FabricLauncherBase.getLauncher().getClassPath().stream().filter(Files::exists).collect(Collectors.toList());
    }

    // gets the official minecraft jar
    Path getMinecraftJar() {
        String givenJar = System.getProperty("optifabric.mc-jar");
        if (givenJar != null) {
            Path givenJarFile = Paths.get(givenJar);
            if (Files.exists(givenJarFile)) {
                return givenJarFile;
            } else {
                System.err.println("supplied minecraft jar at " + givenJar + " doesn't exist, falling back");
            }
        }

        Path minecraftJar = OptifineSetup.getLaunchMinecraftJar();

        // this doesn't work with the way gradle cache is set up, it should be looking in the universe loom-cache in .gradle, but instead it uses the project local one
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Path officialNames = minecraftJar.resolveSibling(String.format("minecraft-%s-client.jar", OptifineVersion.minecraftVersion));
            if (Files.notExists(officialNames)) {
                Path parent = minecraftJar.getParent().resolveSibling(String.format("minecraft-%s-client.jar", OptifineVersion.minecraftVersion));
                if (Files.notExists(parent)) {
                    Path alternativeParent = parent.resolveSibling("minecraft-client.jar");
                    if (Files.notExists(alternativeParent)) {
                        throw new AssertionError(String.format("unable to find minecraft dev jar! tried %s, %s and %s. please supply it explicitly with -Doptifabric.mc-jar", officialNames, parent, alternativeParent));
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
