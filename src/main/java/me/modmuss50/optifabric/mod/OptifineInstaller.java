package me.modmuss50.optifabric.mod;

import java.io.File;
import java.lang.reflect.*;
import java.net.*;

// a class used to extract the optifine jar from the installer
public class OptifineInstaller {
    public static void extract(File installer, File output, File minecraftJar) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, MalformedURLException {
        System.out.println("Running optifine patcher");
        @SuppressWarnings("resource") ClassLoader classLoader = new URLClassLoader(new URL[]{installer.toURI().toURL()}, OptifineInstaller.class.getClassLoader());
        Class<?> clazz = classLoader.loadClass("optifine.Patcher");
        Method method = clazz.getDeclaredMethod("process", File.class, File.class, File.class);
        method.invoke(null, minecraftJar, installer, output);
    }
}
