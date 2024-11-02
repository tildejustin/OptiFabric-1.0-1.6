package me.modmuss50.optifabric.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

/* Optifine 1.3-1.4.2 */
@Pseudo
@Mixin(targets = "Reflector")
public class ReflectorMixin {
    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "getClassNameMap", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false), remap = false)
    private static Object fixClassNameForFabricatedForge(Map map, Object key, Object value) {
        if (FabricLoader.getInstance().isModLoaded("fabricated-forge")) {
            if (value instanceof String && !((String) value).contains(".")) {
                value = "net.minecraft." + value;
            }
        }

        return map.put(key, value);
    }
}
