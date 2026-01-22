package net.phoenix.core.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderTarget.class)
public interface RenderTargetAccessor {
    // Mojang-mapped name in 1.20.1 is typically "frameBufferId"
    @Accessor("frameBufferId")
    int phoenixcore$fboId();
}
