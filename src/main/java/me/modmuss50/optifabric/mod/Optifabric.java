package me.modmuss50.optifabric.mod;

import net.fabricmc.api.*;

@Environment(EnvType.CLIENT)
public class Optifabric {
    public static String error = null;

    public static boolean hasError() {
        return Optifabric.error != null;
    }
}
