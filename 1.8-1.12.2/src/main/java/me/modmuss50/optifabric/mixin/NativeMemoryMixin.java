package me.modmuss50.optifabric.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Pseudo
@Mixin(targets = "net.optifine.util.NativeMemory")
public class NativeMemoryMixin {
    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/optifine/util/NativeMemory;makeLongSupplier([[Ljava/lang/String;)Ljava/util/function/LongSupplier;", ordinal = 0))
    private static String[][] addNewSharedSecretsLocation(String[][] paths) {
        return new String[][]{paths[0], paths[1], {"jdk.internal.access.SharedSecrets", "getJavaNioAccess", "getDirectBufferPool", "getMemoryUsed"}};
    }
}
