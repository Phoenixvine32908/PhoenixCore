package net.phoenix.core.client.renderer.utils;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BlackHoleTargets {
    private static RenderTarget SCRATCH;

    private BlackHoleTargets() {}

    public static void ensureSize(int w, int h) {
        if (SCRATCH == null || SCRATCH.width != w || SCRATCH.height != h) {
            if (SCRATCH != null) SCRATCH.destroyBuffers();
            // depth isn't needed for the post-pass, but leaving it true is harmless
            SCRATCH = new TextureTarget(w, h, true, Minecraft.ON_OSX);
            SCRATCH.setClearColor(0f, 0f, 0f, 0f);
        }
    }

    public static RenderTarget scratch() {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        ensureSize(main.width, main.height);
        return SCRATCH;
    }
}
