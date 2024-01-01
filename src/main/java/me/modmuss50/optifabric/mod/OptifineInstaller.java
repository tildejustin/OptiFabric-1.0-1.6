package me.modmuss50.optifabric.mod;

import java.io.File;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.Path;

// a class used to extract the optifine jar from the installer
public class OptifineInstaller {
    public static void extract(Path installer, Path output, Path minecraftJar) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, MalformedURLException {
        System.out.println("Running optifine patcher");
        @SuppressWarnings("resource") ClassLoader classLoader = new URLClassLoader(new URL[]{installer.toUri().toURL()}, OptifineInstaller.class.getClassLoader());
        Class<?> clazz = classLoader.loadClass("optifine.Patcher");
        Method method = clazz.getDeclaredMethod("process", File.class, File.class, File.class);
        method.invoke(null, minecraftJar.toFile(), installer.toFile(), output.toFile());
    }
}
