package net.phoenix.core.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements VertexConsumer {

    @Unique
    public VertexConsumer phoenixCore$tangent(float x, float y, float z) {
        return this;
    }
}
