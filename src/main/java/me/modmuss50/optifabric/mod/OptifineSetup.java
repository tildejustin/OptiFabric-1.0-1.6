package me.modmuss50.optifabric.mod;

import me.modmuss50.optifabric.patcher.ClassCache;
import me.modmuss50.optifabric.patcher.LambadaRebuiler;
import me.modmuss50.optifabric.patcher.PatchSplitter;
import me.modmuss50.optifabric.util.RemapUtils;
import me.modmuss50.optifabric.util.ZipUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.lib.mappingio.tree.MappingTree;
import net.fabricmc.loader.impl.lib.tinyremapper.IMappingProvider;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptifineSetup {
	private File workingDir = new File(String.valueOf(FabricLoader.getInstance().getGameDir()), ".optifine");
	private File versionDir;

	public Pair<File, ClassCache> getRuntime() throws Throwable {
		if (!workingDir.exists()) {
			workingDir.mkdirs();
		}
		File optifineModJar = OptifineVersion.findOptifineJar();

		byte[] modHash = fileHash(optifineModJar);

		versionDir = new File(workingDir, OptifineVersion.version);
		if (!versionDir.exists()) {
			versionDir.mkdirs();
		}

		File remappedJar = new File(versionDir, "Optifine-mapped.jar");
		File optifinePatches = new File(versionDir, "Optifine.classes");

		ClassCache classCache = null;
		if(remappedJar.exists() && optifinePatches.exists()){
			classCache = ClassCache.read(optifinePatches);
			//Validate that the classCache found is for the same input jar
			if(!Arrays.equals(classCache.getHash(), modHash)){
				System.out.println("Class cache is from a different optifine jar, deleting and re-generating");
				classCache = null;
				optifinePatches.delete();
			}
		}

		if (remappedJar.exists() && classCache != null) {
			System.out.println("Found existing patched optifine jar, using that");
			return Pair.of(remappedJar, classCache);
		}

		if (OptifineVersion.jarType == OptifineVersion.JarType.OPTFINE_INSTALLER) {
			File optifineMod = new File(versionDir, "/Optifine-mod.jar");
			if (!optifineMod.exists()) {
				OptifineInstaller.extract(optifineModJar, optifineMod, getMinecraftJar().toFile());
			}
			optifineModJar = optifineMod;
		}

		System.out.println("Setting up optifine for the first time, this may take a few seconds.");

		//A jar without srgs
		File jarOfTheFree = new File(versionDir, "/Optifine-jarofthefree.jar");

		System.out.println("De-Volderfiying jar");

		//Find all the SRG named classes and remove them
		ZipUtils.transform(optifineModJar, (zip, zipEntry) -> {
			String name = zipEntry.getName();
			if(name.startsWith("com/mojang/blaze3d/platform/")){
				if(name.contains("$")){
					String[] split = name.replace(".class", "").split("\\$");
					if(split.length >= 2){
						if(split[1].length() > 2){
							return false;
						}
					}
				}
			}
			return !(name.startsWith("srg/") || name.startsWith("net/minecraft/"));
		}, jarOfTheFree);

		System.out.println("Building lambada fix mappings");
		LambadaRebuiler rebuiler = new LambadaRebuiler(jarOfTheFree, getMinecraftJar().toFile());
		rebuiler.buildLambadaMap();

		System.out.println("Remapping optifine with fixed lambada names");
		File lambadaFixJar = new File(versionDir, "/Optifine-lambadafix.jar");
		RemapUtils.mapJar(lambadaFixJar.toPath(), jarOfTheFree.toPath(), rebuiler, getLibs());

		remapOptifine(lambadaFixJar.toPath(), remappedJar);

		classCache = PatchSplitter.generateClassCache(remappedJar, optifinePatches, modHash);

		if(true){
			//We are done, lets get rid of the stuff we no longer need
			lambadaFixJar.delete();
			jarOfTheFree.delete();

			if(OptifineVersion.jarType == OptifineVersion.JarType.OPTFINE_INSTALLER){
				optifineModJar.delete();
			}

			File extractedMappings = new File(versionDir, "mappings.tiny");
			File fieldMappings = new File(versionDir, "mappings.full.tiny");
			extractedMappings.delete();
			fieldMappings.delete();
		}

		boolean extractClasses = Boolean.parseBoolean(System.getProperty("optifabric.extract", "false"));
		if(extractClasses){
			System.out.println("Extracting optifine classes");
			File optifineClasses = new File(versionDir, "optifine-classes");
			if(optifineClasses.exists()){
				FileUtils.deleteDirectory(optifineClasses);
			}
			ZipUtils.extract(remappedJar, optifineClasses);
		}

		return Pair.of(remappedJar, classCache);
	}

	private void remapOptifine(Path input, File remappedJar) throws Exception {
		String namespace = FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace();
		System.out.println("Remapping optifine to :" + namespace);

		List<Path> mcLibs = getLibs();
		mcLibs.add(getMinecraftJar());

		RemapUtils.mapJar(remappedJar.toPath(), input, createMappings("official", namespace), mcLibs);
	}

	//Optifine currently has two fields that match the same name as Yarn mappings, we'll rename Optifine's to something else
	IMappingProvider createMappings(String from, String to) {
		MappingTree normalMappings = FabricLauncherBase.getLauncher().getMappingConfiguration().getMappings();

		//In dev
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			try {
				File fullMappings = extractMappings();
				return (out) -> {
					RemapUtils.getTinyRemapper(fullMappings, from, to).load(out);
					//TODO use the mappings API here to stop neededing to change this each version
					out.acceptField(new IMappingProvider.Member("dbq", "CLOUDS", "Ldbe;"),
							"CLOUDS_OF");
					out.acceptField(new IMappingProvider.Member("dqr", "renderDistance", "I"),
							"renderDistance_OF");
				};
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		//In prod
		int fromId = normalMappings.getNamespaceId(from);
		return (out) -> {
			for (MappingTree.ClassMapping classDef : normalMappings.getClasses()) {
				String className = classDef.getName(from);
				out.acceptClass(className, classDef.getName(to));

				for (MappingTree.FieldMapping field : classDef.getFields()) {
					out.acceptField(new IMappingProvider.Member(className, field.getName(from), field.getDesc(fromId)), field.getName(to));
				}

				for (MappingTree.MethodMapping method : classDef.getMethods()) {
					out.acceptMethod(new IMappingProvider.Member(className, method.getName(from), method.getDesc(fromId)), method.getName(to));
				}
			}
		};
	}

	//Gets the minecraft librarys
	private static List<Path> getLibs() {
		Path[] libs = FabricLauncherBase.getLauncher().getClassPath().stream().filter(Files::exists).toArray(Path[]::new);

		out: if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			Path launchJar = getLaunchMinecraftJar();

			for (int i = 0, end = libs.length; i < end; i++) {
				Path lib = libs[i];

				if (launchJar.equals(lib)) {
					libs[i] = getMinecraftJar();
					break out;
				}
			}

			//Can't find the launch jar apparently, remapping will go wrong if it is left in
			throw new IllegalStateException("Unable to find Minecraft jar (at " + launchJar + ") in classpath: " + Arrays.toString(libs));
		}

		return new ArrayList<>(Arrays.asList(libs));
	}

	//Gets the offical minecraft jar
	private static Path getMinecraftJar() {
		String givenJar = System.getProperty("optifabric.mc-jar");
		if (givenJar != null) {
			File givenJarFile = new File(givenJar);

			if (givenJarFile.exists()) {
				return givenJarFile.toPath();
			} else {
				System.err.println("Supplied Minecraft jar at " + givenJar + " doesn't exist, falling back");
			}
		}

        return getLaunchMinecraftJar();
	}

	private static Path getLaunchMinecraftJar() {
		try {
			return (Path) FabricLoader.getInstance().getObjectShare().get("fabric-loader:inputGameJar");
		} catch (NoClassDefFoundError | NoSuchMethodError old) {
			ModContainer mod = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow(() -> new IllegalStateException("No Minecraft?"));
			URI uri = mod.getRootPaths().getFirst().toUri();
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

	//Extracts the devtime mappings out of yarn into a file
	File extractMappings() throws IOException {
		File extractedMappings = new File(versionDir, "mappings.tiny");
		if (extractedMappings.exists()) {
			extractedMappings.delete();
		}
		InputStream mappingStream = FabricLauncherBase.class.getClassLoader().getResourceAsStream("mappings/mappings.tiny");
		FileUtils.copyInputStreamToFile(mappingStream, extractedMappings);
		if (!extractedMappings.exists()) {
			throw new RuntimeException("failed to extract mappings!");
		}
		return extractedMappings;
	}

	byte[] fileHash(File input) throws IOException {
		try (InputStream is = new FileInputStream(input)) {
			return DigestUtils.md5(is);
		}
	}
}