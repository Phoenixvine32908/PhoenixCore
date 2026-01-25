package net.phoenix.core.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.core.PhoenixCore;
import net.phoenix.core.client.renderer.PhoenixShaders;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = PhoenixCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientShaders {
    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {

        // black hole post shader
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        new ResourceLocation(PhoenixCore.MOD_ID, "black_hole"),
                        DefaultVertexFormat.POSITION_TEX),
                PhoenixShaders::setBlackHoleShader
        );

        // blender material shader (your block model shader)
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                        new ResourceLocation(PhoenixCore.MOD_ID, "blender_material"),
                        DefaultVertexFormat.BLOCK),
                PhoenixShaders::setBlenderShader
        );

        PhoenixCore.LOGGER.info("[PhoenixCore] Shaders registered.");
    }
}
