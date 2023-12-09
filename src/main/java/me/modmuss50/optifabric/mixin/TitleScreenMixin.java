package me.modmuss50.optifabric.mixin;

import me.modmuss50.optifabric.mod.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    @Inject(method = "init", at = @At("RETURN"))
    private void init(CallbackInfo info) {
        if (Optifabric.hasError()) System.out.println("An Optifabric error has occurred");
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render(CallbackInfo ci) {
        if (!Optifabric.hasError()) this.drawWithShadow(Minecraft.getMinecraft().textRenderer, OptifineVersion.version, 2, this.height - 20, 0xFFFFFFFF);
    }
}
