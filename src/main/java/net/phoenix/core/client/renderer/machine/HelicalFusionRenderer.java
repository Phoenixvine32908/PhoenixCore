package net.phoenix.core.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

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

        if (!machine.isFormed()) return;

        RecipeLogic logic = machine.getRecipeLogic();
        int recipeColor = machine.getColor();

        if (logic.isWorking()) {
            lastColor = recipeColor;
            delta = FADEOUT;
        } else if (delta > 0) {
            float alphaFactor = delta / FADEOUT;
            lastColor = FastColor.ARGB32.color(
                    Mth.floor(alphaFactor * 255),
                    FastColor.ARGB32.red(lastColor),
                    FastColor.ARGB32.green(lastColor),
                    FastColor.ARGB32.blue(lastColor)
            );
            delta -= Minecraft.getInstance().getDeltaFrameTime();
        }

        VertexConsumer vc = buffer.getBuffer(GTRenderTypes.getLightRing());
        float time = (machine.getOffsetTimer() + partialTick) * 0.02f;

        Vec3 offset = new Vec3(
                machine.getFrontFacing().step().x(),
                machine.getFrontFacing().step().y(),
                machine.getFrontFacing().step().z()
        ).normalize().scale(20.0);

        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);

        renderCurve(vc, poseStack, time, 0f, lastColor);
        renderCurve(vc, poseStack, time, Mth.PI, lastColor);

        poseStack.popPose();
    }

    private void renderCurve(VertexConsumer vc, PoseStack stack, float time, float phase, int baseColor) {
        final int segments = 400;
        final int crossSections = 12;
        final float baseRadius = 0.35f;

        Vec3[][] rings = new Vec3[segments][crossSections];
        Vec3[] centers = new Vec3[segments];

        Vec3 pInitial = lissajousPoint(phase, time);
        Vec3 pNextInit = lissajousPoint(0.001f + phase, time);
        Vec3 T = pNextInit.subtract(pInitial).normalize();
        Vec3 V = new Vec3(0, 1, 0);
        if (Math.abs(T.dot(V)) > 0.9f) V = new Vec3(0, 0, 1);
        Vec3 B = T.cross(V).normalize();
        Vec3 N = B.cross(T).normalize();

        for (int i = 0; i < segments; i++) {
            float t = (float) i / segments * Mth.TWO_PI;
            Vec3 p0 = lissajousPoint(t + phase, time);
            centers[i] = p0;

            Vec3 pNext = lissajousPoint(t + phase + 0.001f, time);
            Vec3 nextT = pNext.subtract(p0).normalize();

            Vec3 axis = T.cross(nextT);
            if (axis.length() > 0.0001f) {
                axis = axis.normalize();
                float angle = (float) Math.acos(Mth.clamp(T.dot(nextT), -1f, 1f));
                N = rotateAroundAxis(N, axis, angle).normalize();
                B = nextT.cross(N).normalize();
            }
            T = nextT;

            for (int j = 0; j < crossSections; j++) {
                float angle = j * Mth.TWO_PI / crossSections;
                float pulse = (0.8f + 0.2f * Mth.sin(i * 0.1f + time * 5f));
                float r = baseRadius * pulse;

                Vec3 offset = N.scale(Mth.cos(angle) * r).add(B.scale(Mth.sin(angle) * r));
                rings[i][j] = p0.add(rotateAroundAxis(offset, T, time * 0.5f));
            }
        }

        int r = FastColor.ARGB32.red(baseColor);
        int g = FastColor.ARGB32.green(baseColor);
        int b = FastColor.ARGB32.blue(baseColor);
        float a = FastColor.ARGB32.alpha(baseColor) / 255f;

        renderTube(stack, vc, rings, centers, segments, crossSections, r, g, b, a, 1.0f);
        renderTube(stack, vc, rings, centers, segments, crossSections, r, g, b, a * 0.3f, 1.4f);
    }

    private void renderTube(PoseStack stack, VertexConsumer vc, Vec3[][] rings, Vec3[] centers,
                            int segments, int crossSections, int rBase, int gBase, int bBase,
                            float alpha, float scale) {
        Matrix4f pose = stack.last().pose();

        for (int i = 0; i < segments; i++) {
            int nextI = (i + 1) % segments;
            float colorInterp = (Mth.sin((float)i / segments * Mth.TWO_PI) + 1f) / 2f;
            int r = Mth.floor(Mth.lerp(colorInterp, rBase, Math.min(255, rBase + 60)));
            int g = Mth.floor(Mth.lerp(colorInterp, gBase, Math.min(255, gBase + 60)));
            int b = Mth.floor(Mth.lerp(colorInterp, bBase, Math.min(255, bBase + 60)));

            for (int j = 0; j < crossSections; j++) {
                int nextJ = (j + 1) % crossSections;

                Vec3 v1 = scalePoint(rings[i][j], centers[i], scale);
                Vec3 v2 = scalePoint(rings[i][nextJ], centers[i], scale);
                Vec3 v3 = scalePoint(rings[nextI][nextJ], centers[nextI], scale);
                Vec3 v4 = scalePoint(rings[nextI][j], centers[nextI], scale);

                vertex(vc, pose, v1, r, g, b, alpha);
                vertex(vc, pose, v2, r, g, b, alpha);
                vertex(vc, pose, v3, r, g, b, alpha);
                vertex(vc, pose, v4, r, g, b, alpha);

                vertex(vc, pose, v4, r, g, b, alpha);
                vertex(vc, pose, v3, r, g, b, alpha);
                vertex(vc, pose, v2, r, g, b, alpha);
                vertex(vc, pose, v1, r, g, b, alpha);
            }
        }
    }

    private Vec3 scalePoint(Vec3 point, Vec3 center, float scale) {
        if (scale == 1.0f) return point;
        return center.add(point.subtract(center).scale(scale));
    }

    private void vertex(VertexConsumer vc, Matrix4f pose, Vec3 p, int r, int g, int b, float a) {
        vc.vertex(pose, (float)p.x, (float)p.y, (float)p.z)
                .color(r / 255f, g / 255f, b / 255f, a)
                .endVertex();
    }

    private Vec3 lissajousPoint(float t, float time) {
        float x = 2f * Mth.cos(4f * t);
        float y = 2f * Mth.sin(4f * t);
        float z = 17f * Mth.cos(t);
        float cos = Mth.cos(time);
        float sin = Mth.sin(time);
        return new Vec3(x * cos - y * sin, x * sin + y * cos, z * 0.4f);
    }

    private Vec3 rotateAroundAxis(Vec3 v, Vec3 axis, float angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        Vec3 a = axis.normalize();
        return new Vec3(
                v.x * cos + (a.y * v.z - a.z * v.y) * sin + a.x * (a.x * v.x + a.y * v.y + a.z * v.z) * (1 - cos),
                v.y * cos + (a.z * v.x - a.x * v.z) * sin + a.y * (a.x * v.x + a.y * v.y + a.z * v.z) * (1 - cos),
                v.z * cos + (a.x * v.y - a.y * v.x) * sin + a.z * (a.x * v.x + a.y * v.y + a.z * v.z) * (1 - cos)
        );
    }

    @Override public boolean shouldRenderOffScreen(FusionReactorMachine machine) { return true; }
    @Override public int getViewDistance() { return 128; }
    @Override public @NotNull AABB getRenderBoundingBox(FusionReactorMachine machine) {
        return new AABB(machine.getPos()).inflate(40, 40, 40);
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
 *
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
 * for (int i = 0; i < segments - 1; i++) {
 * float t0 = (float) i / segments;
 * float t1 = (float) (i + 1) / segments;
 * 
 * Vec3[] s0 = mobiusSection(t0, size, thickness);
 * Vec3[] s1 = mobiusSection(t1, size, thickness);
 * 
 *
 * emitQuad(vc, pose, s0[0], s0[1], s1[1], s1[0], r, g, b, a);
 * 
 *
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
