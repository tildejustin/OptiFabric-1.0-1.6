package me.modmuss50.optifabric.mixin;

import java.io.IOException;
import java.io.InputStream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import me.modmuss50.optifabric.mod.OptifineResources;

@Mixin(value = DefaultResourcePack.class, priority = 400)
abstract class MixinDefaultResourcePack {

    private static String getPath(ResourceType type, Identifier id) {
        return "/assets/" + id.getNamespace() + "/" + id.getPath();
    }

    @Inject(method = "findInputStream", at = @At("HEAD"), cancellable = true)
    protected void onFindInputStream(ResourceType type, Identifier id, CallbackInfoReturnable<InputStream> callback) {
        String path = getPath(type, id);

        try {
            InputStream stream = OptifineResources.INSTANCE.getResource(path);
            if (stream != null) callback.setReturnValue(stream);
        } catch (IOException e) {
            //Optifine does this if it goes wrong so we will too
            e.printStackTrace();
        }
    }

    @Inject(method = "contains", at = @At("HEAD"), cancellable = true)
    public void doesContain(ResourceType type, Identifier id, CallbackInfoReturnable<Boolean> callback) {
        String path = getPath(type, id);

        if (OptifineResources.INSTANCE.hasResource(path)) callback.setReturnValue(true);
    }
}