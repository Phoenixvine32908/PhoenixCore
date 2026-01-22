package net.phoenix.core.mixin;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.core.client.renderer.PhoenixShaders;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderType.class)
public abstract class RenderTypeMixin extends RenderStateShard {

    private RenderTypeMixin(String p_110161_, Runnable p_110162_, Runnable p_110163_) {
        super(p_110161_, p_110162_, p_110163_);
    }

    @Unique
    private static RenderType phoenix$getBlenderMaterial(ResourceLocation albedo, ResourceLocation normal,
                                                         ResourceLocation specular) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(PhoenixShaders::getBlenderShader))
                .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                        .add(albedo, false, false)   // Slot 0
                        .add(normal, false, false)   // Slot 1
                        .add(specular, false, false) // Slot 2
                        .build())
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(true);

        return RenderType.create("blender_material",
                DefaultVertexFormat.BLOCK,
                VertexFormat.Mode.QUADS,
                2097152,
                true,
                true,
                state);
    }
}
