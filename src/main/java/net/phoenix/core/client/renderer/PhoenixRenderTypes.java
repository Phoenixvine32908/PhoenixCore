package net.phoenix.core.client.renderer;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

@OnlyIn(Dist.CLIENT)
public class PhoenixRenderTypes extends RenderType {

    private static final RenderType LIGHT_RING = RenderType.create(
            "phoenix_light_ring",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setLayeringState(RenderStateShard.NO_LAYERING)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    // ------------------------------------------------------------
    // NEW: Honey Fog RenderType (quad-compatible, soft translucent)
    // ------------------------------------------------------------
    private static final RenderType HONEY_FOG = RenderType.create(
            "phoenix_honey_fog",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    // soft translucent blending (not additive)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    // no z-fighting
                    .setLayeringState(RenderStateShard.NO_LAYERING)
                    // render both sides
                    .setCullState(RenderStateShard.NO_CULL)
                    // depth test ON so fog stays inside chamber
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));

    private PhoenixRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode,
                               int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                               Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType LIGHT_RING() {
        return LIGHT_RING;
    }

    public static RenderType HONEY_FOG() {
        return HONEY_FOG;
    }
}
