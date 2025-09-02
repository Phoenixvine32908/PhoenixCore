package net.phoenix.core.client.renderer.machine.multiblock;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;

import net.phoenix.core.client.renderer.machine.ArtificialStarRender;
import net.phoenix.core.client.renderer.machine.EyeOfHarmonyRender;
import net.phoenix.core.client.renderer.machine.PlasmaArcFurnaceRender;

public class CosmicDynamicRenderHelpers {

    public static DynamicRender<?, ?> getEyeOfHarmonyRender() {
        return EyeOfHarmonyRender.INSTANCE;
    }

    public static DynamicRender<?, ?> getArtificialStarRender() {
        return ArtificialStarRender.INSTANCE;
    }

    public static DynamicRender<?, ?> getPlasmaArcFurnaceRenderer() {
        return PlasmaArcFurnaceRender.INSTANCE;
    }
}
