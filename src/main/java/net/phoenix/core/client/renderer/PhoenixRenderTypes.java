package net.phoenix.core.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class PhoenixRenderTypes extends RenderType {

    // ------------------------------------------------------------
    // Classic utility RenderTypes (restored)
    // ------------------------------------------------------------
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
                    .createCompositeState(false)
    );

    private static final RenderType HONEY_FOG = RenderType.create(
            "phoenix_honey_fog",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setLayeringState(RenderStateShard.NO_LAYERING)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .createCompositeState(false)
    );

    // ------------------------------------------------------------
    // Black hole lensing RenderType
    // ------------------------------------------------------------
    // Bind the main scene color texture to texture unit 0 for black hole lensing
    private static final RenderStateShard.EmptyTextureStateShard MAIN_SCENE_COLOR =
            new RenderStateShard.EmptyTextureStateShard(
                    () -> {
                        Minecraft mc = Minecraft.getInstance();
                        RenderSystem.setShaderTexture(0, mc.getMainRenderTarget().getColorTextureId());
                    },
                    () -> RenderSystem.setShaderTexture(0, 0)
            );

    private static final RenderType BLACK_HOLE = RenderType.create(
            "phoenix_black_hole",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> {
                        var s = PhoenixShaders.getBlackHoleShader();
                        return s != null ? s : net.minecraft.client.renderer.GameRenderer.getPositionTexShader();
                    }))
                    .setTextureState(MAIN_SCENE_COLOR)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );


    // ------------------------------------------------------------
    // Blender material RenderType
    // ------------------------------------------------------------
    public static final Function<ResourceLocation, RenderType> BLENDER_MATERIAL = Util.memoize(albedo -> {
        ResourceLocation normal   = withSuffix(albedo, "_n");
        ResourceLocation specular = withSuffix(albedo, "_s");

        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(PhoenixShaders::getBlenderShader))
                .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                        .add(albedo, false, false)   // Sampler0
                        .add(normal, false, false)   // Sampler1
                        .add(specular, false, false) // Sampler2
                        .build())
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .setCullState(RenderStateShard.NO_CULL)
                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                .createCompositeState(true);

        return RenderType.create(
                "phoenix_blender_material",
                DefaultVertexFormat.BLOCK,
                VertexFormat.Mode.QUADS,
                2097152,
                true,
                true,
                state
        );
    });

    private PhoenixRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode,
                               int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                               Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    // ------------------------------------------------------------
    // Accessors (restored + new)
    // ------------------------------------------------------------
    public static RenderType LIGHT_RING() {
        return LIGHT_RING;
    }

    public static RenderType HONEY_FOG() {
        return HONEY_FOG;
    }

    public static RenderType BLACK_HOLE() {
        return BLACK_HOLE;
    }

    public static RenderType BLENDER_MATERIAL(ResourceLocation albedo) {
        return BLENDER_MATERIAL.apply(albedo);
    }

    private static ResourceLocation withSuffix(ResourceLocation base, String suffix) {
        String p = base.getPath();
        int dot = p.lastIndexOf('.');
        if (dot >= 0) {
            return new ResourceLocation(base.getNamespace(), p.substring(0, dot) + suffix + p.substring(dot));
        }
        return new ResourceLocation(base.getNamespace(), p + suffix);
    }
}
