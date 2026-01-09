package net.phoenix.core.client.renderer.machine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public class BeeRender {

    public static final BeeRender INSTANCE = new BeeRender();

    private Bee dummyBee;

    /**
     * Renders a swarm of bees in a circular orbit.
     *
     * @param level       The current world level
     * @param timer       A consistent timer (e.g., machine.getOffsetTimer() + partialTick)
     * @param partialTick The partial tick for smooth animation
     * @param poseStack   The current PoseStack
     * @param buffer      The MultiBufferSource
     * @param packedLight The light level
     * @param beeCount    How many bees to render
     * @param orbitRadius How far from the center they orbit
     */
    public void renderSwarm(Level level, float timer, float partialTick, PoseStack poseStack,
                            MultiBufferSource buffer, int packedLight, int beeCount, float orbitRadius) {
        if (dummyBee == null && level != null) {
            dummyBee = EntityType.BEE.create(level);
            if (dummyBee != null) {
                dummyBee.setNoGravity(true);
                dummyBee.setInvulnerable(true);
            }
        }

        if (dummyBee == null) return;

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

        for (int i = 0; i < beeCount; i++) {
            poseStack.pushPose();

            float offset = i * ((float) Math.PI * 2 / beeCount);
            float angle = (timer * 0.03f) + offset;

            float x = (float) Math.cos(angle) * orbitRadius;
            float z = (float) Math.sin(angle) * orbitRadius;
            float y = (float) Math.sin(timer * 0.08f + i) * 0.5f;

            poseStack.translate(x, y, z);

            float yaw = (float) Math.toDegrees(-angle);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));

            float pitch = (float) Math.cos(timer * 0.08f + i) * 10f;
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));

            dummyBee.tickCount = (int) timer;

            dispatcher.render(dummyBee, 0, 0, 0, 0, partialTick, poseStack, buffer, packedLight);

            poseStack.popPose();
        }
    }

    /*
     * How to use: Put this in render class inside main render call.
     * BeeRender.INSTANCE.renderSwarm(
     * machine.getLevel(),
     * tick,
     * partialTick,
     * poseStack,
     * buffer,
     * packedLight,
     * 6, // beeCount
     * 4.5f // orbitRadius
     * );
     */
}
