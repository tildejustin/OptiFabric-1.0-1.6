package me.modmuss50.optifabric.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// suppresses some warnings in the log
@Pseudo
@Mixin(targets = "ReflectorClass")
public class ReflectorClassMixin {
    @Shadow
    private String targetClassName;

    @Shadow
    private boolean checked;

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "getTargetClass", at = @At("HEAD"), remap = false)
    private void getTargetClass(CallbackInfoReturnable<Class<?>> infoReturnable) {
        if (!checked) { // only check the target if it hasn't been done yet
            String name = targetClassName.replaceAll("/", ".");
            if (name.startsWith("net.minecraft.launchwrapper") || name.startsWith("net.minecraftforge") || "optifine.OptiFineClassTransformer".equals(name)) {
                checked = true;
            }
        }
    }
}
