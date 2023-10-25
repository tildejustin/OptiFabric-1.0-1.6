package me.modmuss50.optifabric.mod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Optifabric {
    public static void checkForErrors() {
        if (OptifabricError.hasError()) {
            System.out.println("An Optifabric error has occurred");
        }
    }
}
