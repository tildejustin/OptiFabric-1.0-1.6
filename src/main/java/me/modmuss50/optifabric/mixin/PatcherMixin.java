package me.modmuss50.optifabric.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Pseudo
@Mixin(targets = "optifine.Patcher")
public class PatcherMixin {
    @Redirect(method = "process", at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z"))
    private static boolean ignoreHashFailure(String instance, Object o) {
        return true;
    }
}
