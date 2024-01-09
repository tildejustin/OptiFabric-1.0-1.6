package me.modmuss50.optifabric.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// suppresses some warnings in the logs
@Pseudo
@Mixin(targets = "net.optifine.reflect.ReflectorClass")
public class ReflectorClassMixin {
    @Shadow
    private String targetClassName;

    @Shadow
    private boolean checked;

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "getTargetClass", at = @At("HEAD"), remap = false)
    private void getTargetClass(CallbackInfoReturnable<Class<?>> infoReturnable) {
        if (!this.checked) { // only check the target if it hasn't been done yet
            String name = this.targetClassName.replaceAll("/", ".");
            if (name.startsWith("net.minecraft.launchwrapper") || name.startsWith("net.minecraftforge") || "optifine.OptiFineClassTransformer".equals(name)) {
                this.checked = true;
            }
        }
    }
}
