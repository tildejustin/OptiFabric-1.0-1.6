package me.modmuss50.optifabric.mixin;

import me.modmuss50.optifabric.mod.Optifabric;
import me.modmuss50.optifabric.mod.OptifabricError;
import me.modmuss50.optifabric.mod.OptifineVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {

    protected MixinTitleScreen() {
        super();
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void init(CallbackInfo info) {
        Optifabric.checkForErrors();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render(int int_1, int int_2, float float_1, CallbackInfo info) {
        if (!OptifabricError.hasError()) {
            this.drawWithShadow(Minecraft.getMinecraft().textRenderer, OptifineVersion.version, 2, this.height - 20, 0xFFFFFFFF);
        }
    }
}
