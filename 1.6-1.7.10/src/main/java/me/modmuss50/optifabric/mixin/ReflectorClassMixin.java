package me.modmuss50.optifabric.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

// suppresses some warnings in the logs
@Pseudo
@Mixin(targets = "ReflectorClass")
public class ReflectorClassMixin {
    @Shadow
    private boolean checked;

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "getTargetClass", at = @At("HEAD"), remap = false)
    private void getTargetClass(CallbackInfoReturnable<Class<?>> infoReturnable) {
        String targetClassName = this.getTargetClassName();
        if (!this.checked) { // only check the target if it hasn't been done yet
            String name = targetClassName.replaceAll("/", ".");
            if (name.startsWith("net.minecraft.launchwrapper") || name.startsWith("net.minecraftforge") || "optifine.OptiFineClassTransformer".equals(name)) {
                this.checked = true;
            }
        }
    }

    @Unique
    private String getTargetClassName() {
        try {
            Field targetClass = this.getClass().getDeclaredField("targetClassName");
            targetClass.setAccessible(true);
            return (String) targetClass.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            try {
                // 1.7.2 behavior
                Field targetClass = this.getClass().getDeclaredField("targetClassNames");
                targetClass.setAccessible(true);
                return ((String[]) targetClass.get(this))[0];
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
