package net.phoenix.core.client.renderer.utils;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BlackHoleTargets {
    private static RenderTarget scratch;

    private BlackHoleTargets() {}

    public static RenderTarget scratch() {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();

        int w = main.width;
        int h = main.height;

        if (scratch == null || scratch.width != w || scratch.height != h) {
            if (scratch != null) scratch.destroyBuffers();
            // true = has depth; fine either way, but safe
            scratch = new TextureTarget(w, h, true, Minecraft.ON_OSX);
        }
        return scratch;
    }
}
