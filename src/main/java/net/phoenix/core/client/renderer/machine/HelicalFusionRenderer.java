package net.phoenix.core.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;


import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;

import org.jetbrains.annotations.NotNull;

public class HelicalFusionRenderer extends DynamicRender<FusionReactorMachine, HelicalFusionRenderer> {

    public static final HelicalFusionRenderer INSTANCE = new HelicalFusionRenderer();
    public static final Codec<HelicalFusionRenderer> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<FusionReactorMachine, HelicalFusionRenderer> TYPE =
            new DynamicRenderType<>(CODEC);

    private static final float FADEOUT = 60f;
    private float delta = 0f;
    private int lastColor = -1;

    private HelicalFusionRenderer() {}

    @Override
    public @NotNull DynamicRenderType<FusionReactorMachine, HelicalFusionRenderer> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(FusionReactorMachine machine, Vec3 cameraPos) {
        return machine.isFormed() && (machine.getRecipeLogic().isWorking() || delta > 0);
    }

    @Override
    public void render(FusionReactorMachine machine, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        RecipeLogic logic = machine.getRecipeLogic();
        if (!machine.isFormed() || !machine.getRecipeLogic().isWorking()) return;

        int recipeColor = machine.getColor(); // <-- this gets the color from the active recipe

        lastColor = recipeColor;
        delta = FADEOUT;


        if (logic.isWorking()) {
            lastColor = recipeColor;
            delta = FADEOUT;
        } else {
            float alphaFactor = delta / FADEOUT;
            lastColor = FastColor.ARGB32.color(
                    Mth.floor(alphaFactor * 255),
                    FastColor.ARGB32.red(lastColor),
                    FastColor.ARGB32.green(lastColor),
                    FastColor.ARGB32.blue(lastColor)
            );
            delta -= Minecraft.getInstance().getDeltaFrameTime();
        }

        float r = FastColor.ARGB32.red(lastColor) / 255f;
        float g = FastColor.ARGB32.green(lastColor) / 255f;
        float b = FastColor.ARGB32.blue(lastColor) / 255f;
        float a = FastColor.ARGB32.alpha(lastColor) / 255f;

        VertexConsumer vc = buffer.getBuffer(GTRenderTypes.getLightRing());

        float time = (machine.getOffsetTimer() + partialTick) * 0.02f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        // Dual interleaved Lissajous curves (phase = 0 and π)
        renderCurve(vc, poseStack, time, 0f, r, g, b, a);
        renderCurve(vc, poseStack, time, Mth.PI, r, g, b, a);

        poseStack.popPose();
    }

    private void renderCurve(VertexConsumer vc, PoseStack stack, float time, float phase, float r, float g, float b, float a) {
        final int segments = 200;
        final float radius = 0.3f;

        for (int i = 0; i < segments; i++) {
            float t0 = (float)i / segments * Mth.TWO_PI + phase;
            float t1 = (float)(i + 1) / segments * Mth.TWO_PI + phase;

            Vec3 p0 = lissajousPoint(t0, time);
            Vec3 p1 = lissajousPoint(t1, time);

            renderSegment(stack, vc, p0, p1, radius, r, g, b, a);
        }
    }

    private Vec3 lissajousPoint(float t, float time) {
        float x = 2f * Mth.cos(4f * t);
        float y = 2f * Mth.sin(4f * t);
        float z = 17f * Mth.cos(t);

        float cos = Mth.cos(time);
        float sin = Mth.sin(time);

        float xr = x * cos - y * sin;
        float yr = x * sin + y * cos;

        return new Vec3(xr, yr, z * 0.15f);
    }

    private void renderSegment(PoseStack stack, VertexConsumer vc, Vec3 p0, Vec3 p1, float thickness, float r, float g, float b, float a) {
        Vec3 dir = p1.subtract(p0).normalize();
        Vec3 up = new Vec3(0,0,1);
        Vec3 side = dir.cross(up).normalize().scale(thickness);

        var pose = stack.last();
        vc.vertex(pose.pose(), (float)(p0.x - side.x), (float)(p0.y - side.y), (float)(p0.z - side.z))
                .color(r,g,b,a).endVertex();
        vc.vertex(pose.pose(), (float)(p0.x + side.x), (float)(p0.y + side.y), (float)(p0.z + side.z))
                .color(r,g,b,a).endVertex();
        vc.vertex(pose.pose(), (float)(p1.x + side.x), (float)(p1.y + side.y), (float)(p1.z + side.z))
                .color(r,g,b,a).endVertex();
        vc.vertex(pose.pose(), (float)(p1.x - side.x), (float)(p1.y - side.y), (float)(p1.z - side.z))
                .color(r,g,b,a).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(FusionReactorMachine machine) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public AABB getRenderBoundingBox(FusionReactorMachine machine) {
        return new AABB(machine.getPos()).inflate(16, 24, 16);
    }
}





/*
 * public class HelicalFusionRenderer
 * extends DynamicRender<FusionReactorMachine, HelicalFusionRenderer> {
 * 
 * public static final HelicalFusionRenderer INSTANCE = new HelicalFusionRenderer();
 * public static final Codec<HelicalFusionRenderer> CODEC = Codec.unit(INSTANCE);
 * public static final DynamicRenderType<FusionReactorMachine, HelicalFusionRenderer> TYPE =
 * new DynamicRenderType<>(CODEC);
 * 
 * @Override
 * public @NotNull DynamicRenderType<FusionReactorMachine, HelicalFusionRenderer> getType() {
 * return TYPE;
 * }
 * 
 * @Override
 * public boolean shouldRender(FusionReactorMachine machine, @NotNull Vec3 cameraPos) {
 * return machine.isFormed() && machine.getRecipeLogic() != null && machine.getRecipeLogic().isWorking();
 * }
 * 
 * @Override
 * public void render(
 * FusionReactorMachine machine,
 * float partialTick,
 * 
 * @NotNull PoseStack poseStack,
 * MultiBufferSource buffer,
 * int packedLight,
 * int packedOverlay
 * ) {
 * VertexConsumer vc = buffer.getBuffer(GTRenderTypes.getLightRing());
 * 
 * float time = machine.getOffsetTimer() + partialTick;
 * 
 * // Orange fusion glow
 * float pulse = Mth.sin(time * 0.1f) * 0.15f + 0.85f;
 * float r = 1.0f * pulse;
 * float g = 0.55f * pulse;
 * float b = 0.15f * pulse;
 * float a = 0.85f;
 * 
 * poseStack.pushPose();
 * PoseStack.Pose pose = poseStack.last();
 * 
 * int segments = 64;
 * float size = 4.0f;
 * float thickness = 0.35f;
 * 
 * for (int i = 0; i < segments; i++) {
 * float t0 = (float) i / segments;
 * float t1 = (float) (i + 1) / segments;
 * 
 * Vec3[] s0 = mobiusSection(t0, size, thickness);
 * Vec3[] s1 = mobiusSection(t1, size, thickness);
 * 
 * // OUTWARD FACE (correct winding CCW)
 * emitQuad(vc, pose, s0[0], s0[1], s1[1], s1[0], r, g, b, a);
 * 
 * // INWARD FACE
 * emitQuad(vc, pose, s1[2], s1[3], s0[3], s0[2], r, g, b, a);
 * }
 * 
 * poseStack.popPose();
 * }
 * 
 * 
 * private static Vec3[] mobiusSection(float t, float size, float halfThickness) {
 * float angle = t * Mth.TWO_PI;
 * float twist = angle * 0.5f;
 * 
 * float x = Mth.cos(angle) * size;
 * float z = Mth.sin(angle) * size;
 * 
 * float dx = -Mth.sin(angle);
 * float dz = Mth.cos(angle);
 * 
 * float nx = Mth.cos(twist);
 * float ny = Mth.sin(twist);
 * 
 * Vec3 offset = new Vec3(dx * nx, ny, dz * nx).normalize().scale(halfThickness);
 * 
 * return new Vec3[] {
 * new Vec3(x, 0, z).add(offset),
 * new Vec3(x, 0, z).subtract(offset),
 * new Vec3(x, 0, z).subtract(offset.scale(0.6)),
 * new Vec3(x, 0, z).add(offset.scale(0.6))
 * };
 * }
 * 
 * 
 * private static void emitQuad(
 * VertexConsumer vc,
 * PoseStack.Pose pose,
 * Vec3 a, Vec3 b, Vec3 c, Vec3 d,
 * float r, float g, float bCol, float aCol
 * ) {
 * Vec3 normal = b.subtract(a).cross(d.subtract(a)).normalize();
 * 
 * vertex(vc, pose, a, normal, r, g, bCol, aCol);
 * vertex(vc, pose, b, normal, r, g, bCol, aCol);
 * vertex(vc, pose, c, normal, r, g, bCol, aCol);
 * vertex(vc, pose, d, normal, r, g, bCol, aCol);
 * }
 * 
 * private static void vertex(
 * VertexConsumer vc,
 * PoseStack.Pose pose,
 * Vec3 v,
 * Vec3 n,
 * float r, float g, float b, float a
 * ) {
 * vc.vertex(pose.pose(), (float) v.x, (float) v.y, (float) v.z)
 * .color(r, g, b, a)
 * .normal(pose.normal(), (float) n.x, (float) n.y, (float) n.z)
 * .endVertex();
 * }
 * }
 */
