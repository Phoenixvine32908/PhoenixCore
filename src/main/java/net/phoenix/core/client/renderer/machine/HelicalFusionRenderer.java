package net.phoenix.core.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

public class HelicalFusionRenderer
                                   extends DynamicRender<WorkableElectricMultiblockMachine, HelicalFusionRenderer> {

    public static final HelicalFusionRenderer INSTANCE = new HelicalFusionRenderer();
    public static final Codec<HelicalFusionRenderer> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, HelicalFusionRenderer> TYPE = new DynamicRenderType<>(
            CODEC);

    @Override
    public @NotNull DynamicRenderType<WorkableElectricMultiblockMachine, HelicalFusionRenderer> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(WorkableElectricMultiblockMachine machine, @NotNull Vec3 cameraPos) {
        return machine.isFormed() && machine.getRecipeLogic() != null && machine.getRecipeLogic().isWorking();
    }

    @Override
    public void render(
                       WorkableElectricMultiblockMachine machine,
                       float partialTick,
                       @NotNull PoseStack poseStack,
                       MultiBufferSource buffer,
                       int packedLight,
                       int packedOverlay) {
        VertexConsumer vc = buffer.getBuffer(GTRenderTypes.getLightRing());

        float time = machine.getOffsetTimer() + partialTick;

        poseStack.pushPose();
        PoseStack.Pose pose = poseStack.last();

        int segments = 128;      // smooth
        float radius = 4.0f;    // loop size
        float thickness = 0.7f; // TRUE thickness

        // Render TWO counter-rotating bands
        renderBand(vc, pose, segments, radius, thickness, time, +1);
        renderBand(vc, pose, segments, radius, thickness, time, -1);

        poseStack.popPose();
    }

    /* ========================================================= */

    private static void renderBand(
                                   VertexConsumer vc,
                                   PoseStack.Pose pose,
                                   int segments,
                                   float radius,
                                   float thickness,
                                   float time,
                                   int direction) {
        for (int i = 0; i < segments; i++) {
            float t0 = (float) i / segments;
            float t1 = (float) (i + 1) / segments;

            BandSection a = bandSection(t0, radius, thickness, time, direction);
            BandSection b = bandSection(t1, radius, thickness, time, direction);

            float hue = (time * 0.02f + t0 * 2f) % 1f;
            float[] rgb = hsvToRgb(hue, 0.8f, 1.0f);

            float shimmer = Mth.sin(time * 0.3f + t0 * 20f) * 0.15f + 0.85f;
            float r = rgb[0] * shimmer;
            float g = rgb[1] * shimmer;
            float bCol = rgb[2] * shimmer;

            // OUTER
            emitQuad(vc, pose, a.outerTop, a.outerBottom, b.outerBottom, b.outerTop, r, g, bCol, 0.9f);
            // INNER
            emitQuad(vc, pose, b.innerTop, b.innerBottom, a.innerBottom, a.innerTop, r, g, bCol, 0.9f);
            // TOP
            emitQuad(vc, pose, a.outerTop, b.outerTop, b.innerTop, a.innerTop, r, g, bCol, 0.9f);
            // BOTTOM
            emitQuad(vc, pose, a.innerBottom, b.innerBottom, b.outerBottom, a.outerBottom, r, g, bCol, 0.9f);
        }
    }

    /* ========================================================= */

    private static class BandSection {

        Vec3 outerTop, outerBottom;
        Vec3 innerTop, innerBottom;
    }

    private static BandSection bandSection(
                                           float t,
                                           float radius,
                                           float thickness,
                                           float time,
                                           int direction) {
        float n = 4.0f; // corner sharpness
        float angle = t * Mth.TWO_PI;

        float cx = (float) (Math.signum(Math.cos(angle)) *
                Math.pow(Math.abs(Math.cos(angle)), 2.0 / n) * radius);
        float cz = (float) (Math.signum(Math.sin(angle)) *
                Math.pow(Math.abs(Math.sin(angle)), 2.0 / n) * radius);

        Vec3 center = new Vec3(cx, 0, cz);

        Vec3 tangent = new Vec3(
                -Mth.sin(angle),
                0,
                Mth.cos(angle)).normalize();

        Vec3 up = new Vec3(0, 1, 0);
        float twist = angle * direction + time * 0.25f;
        Vec3 normal = tangent.cross(up)
                .scale(Mth.cos(twist))
                .add(up.scale(Mth.sin(twist)))
                .normalize();

        float inner = thickness * 0.9f;

        BandSection s = new BandSection();
        s.outerTop = center.add(normal.scale(thickness));
        s.outerBottom = center.subtract(normal.scale(thickness));
        s.innerTop = center.add(normal.scale(inner));
        s.innerBottom = center.subtract(normal.scale(inner));

        return s;
    }

    /* ========================================================= */

    private static void emitQuad(
                                 VertexConsumer vc,
                                 PoseStack.Pose pose,
                                 Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                                 float r, float g, float bCol, float aCol) {
        Vec3 normal = a.add(b).add(c).add(d).scale(0.25).normalize();

        vertex(vc, pose, a, normal, r, g, bCol, aCol);
        vertex(vc, pose, b, normal, r, g, bCol, aCol);
        vertex(vc, pose, c, normal, r, g, bCol, aCol);
        vertex(vc, pose, d, normal, r, g, bCol, aCol);
    }

    private static void vertex(
                               VertexConsumer vc,
                               PoseStack.Pose pose,
                               Vec3 v,
                               Vec3 n,
                               float r, float g, float b, float a) {
        vc.vertex(pose.pose(), (float) v.x, (float) v.y, (float) v.z)
                .color(r, g, b, a)
                .normal(pose.normal(), (float) n.x, (float) n.y, (float) n.z)
                .endVertex();
    }

    /* ========================================================= */

    private static float[] hsvToRgb(float h, float s, float v) {
        float r = 0, g = 0, b = 0;
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        switch (i % 6) {
            case 0 -> {
                r = v;
                g = t;
                b = p;
            }
            case 1 -> {
                r = q;
                g = v;
                b = p;
            }
            case 2 -> {
                r = p;
                g = v;
                b = t;
            }
            case 3 -> {
                r = p;
                g = q;
                b = v;
            }
            case 4 -> {
                r = t;
                g = p;
                b = v;
            }
            case 5 -> {
                r = v;
                g = p;
                b = q;
            }
        }
        return new float[] { r, g, b };
    }
}

/*
 * public class HelicalFusionRenderer
 * extends DynamicRender<WorkableElectricMultiblockMachine, HelicalFusionRenderer> {
 * 
 * public static final HelicalFusionRenderer INSTANCE = new HelicalFusionRenderer();
 * public static final Codec<HelicalFusionRenderer> CODEC = Codec.unit(INSTANCE);
 * public static final DynamicRenderType<WorkableElectricMultiblockMachine, HelicalFusionRenderer> TYPE =
 * new DynamicRenderType<>(CODEC);
 * 
 * @Override
 * public @NotNull DynamicRenderType<WorkableElectricMultiblockMachine, HelicalFusionRenderer> getType() {
 * return TYPE;
 * }
 * 
 * @Override
 * public boolean shouldRender(WorkableElectricMultiblockMachine machine, @NotNull Vec3 cameraPos) {
 * return machine.isFormed() && machine.getRecipeLogic() != null && machine.getRecipeLogic().isWorking();
 * }
 * 
 * @Override
 * public void render(
 * WorkableElectricMultiblockMachine machine,
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
