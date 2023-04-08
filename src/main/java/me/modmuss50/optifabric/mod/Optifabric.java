package me.modmuss50.optifabric.mod;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;

@Environment(EnvType.CLIENT)
public class Optifabric implements ModInitializer {

    public static void checkForErrors() {
        if (OptifabricError.hasError()) {
            System.out.println("An Optifabric error has occurred");
        }
    }

    @Override
    public void onInitialize() {

    }
}
