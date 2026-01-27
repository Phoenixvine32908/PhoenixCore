package net.phoenix.core.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.phoenix.core.client.renderer.PhoenixShaders;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Unique
    private static final Logger phoenixCore$LOGGER = LogUtils.getLogger();

    @Inject(method = "reloadShaders", at = @At("TAIL"))
    private void phoenix$loadShaders(ResourceProvider provider, CallbackInfo ci) {
        try {
            ShaderInstance shader = new ShaderInstance(provider, "phoenixcore:black_hole",
                    DefaultVertexFormat.POSITION_TEX);
            PhoenixShaders.setBlackHoleShader(shader);
        } catch (Exception e) {
            PhoenixShaders.setBlackHoleShader(null);
            phoenixCore$LOGGER.error("[PhoenixCore] Failed to load black_hole shader.", e);
        }

        try {
            ShaderInstance shader = new ShaderInstance(provider, "phoenixcore:blender_material",
                    DefaultVertexFormat.BLOCK);
            PhoenixShaders.setBlenderShader(shader);
            phoenixCore$LOGGER.info("[PhoenixCore] blender_material shader loaded.");
        } catch (Exception e) {
            PhoenixShaders.setBlenderShader(null);
            phoenixCore$LOGGER.error("[PhoenixCore] Failed to load blender_material shader.", e);
        }
    }
}
