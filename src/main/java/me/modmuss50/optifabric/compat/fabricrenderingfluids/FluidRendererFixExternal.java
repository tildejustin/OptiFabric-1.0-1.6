package me.modmuss50.optifabric.compat.fabricrenderingfluids;

import java.util.Objects;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;

/** Called from {@link FluidRendererFix}, here to avoid class loading Minecraft stuff too early */
public class FluidRendererFixExternal {
    public static boolean needsOptiFine(FluidState state) {
        return needsOptiFine(state.getFluid());
    }

    public static boolean needsOptiFine(Fluid fluid) {
        return (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) &&
                Objects.requireNonNull(FluidRenderHandlerRegistry.INSTANCE.get(fluid), "Can't find render handler for water?").getClass().getName().startsWith("net.fabricmc.fabric.");
    }
}