package me.modmuss50.optifabric.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/* Optifine 1.4.3+ */
@Pseudo
@Mixin(targets = "ReflectorClass")
public abstract class ReflectorClassMixin {
    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "getTargetClass", at = @At(value = "INVOKE", target = "Ljava/lang/Class;forName(Ljava/lang/String;)Ljava/lang/Class;", remap = false), remap = false)
    private Class fixClassNameForFabricatedForge(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            if (FabricLoader.getInstance().isModLoaded("fabricated-forge")) {
                return Class.forName("net.minecraft." + className);
            } else {
                throw exception;
            }
        }

    }
}
