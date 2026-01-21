package net.phoenix.core.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.block.FluidBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.RenderTypeHelper;
import net.phoenix.core.client.renderer.PhoenixRenderTypes;
import net.phoenix.core.common.machine.multiblock.electric.HoneyCrystallizationChamberMachine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HoneyChamberDynamicRender extends
                                       DynamicRender<HoneyCrystallizationChamberMachine, HoneyChamberDynamicRender> {

    public static final HoneyChamberDynamicRender INSTANCE = new HoneyChamberDynamicRender();
    public static final Codec<HoneyChamberDynamicRender> CODEC = Codec.unit(HoneyChamberDynamicRender::new);
    public static final DynamicRenderType<HoneyCrystallizationChamberMachine, HoneyChamberDynamicRender> TYPE = new DynamicRenderType<>(
            CODEC);

    // --- Expanded Configuration ---
    private static final int STRAND_COUNT = 8; // Slightly more strands
    private static final float STRAND_RADIUS = 0.35f;
    private static final int STRAND_SEGMENTS = 24;

    // VASTLY increased counts for the 30-block field
    private static final int BEE_COUNT = 64;
    private static final int MOTE_COUNT = 150;
    private static final float FIELD_RADIUS = 30.0f;

    private final FluidBlockRenderer fluidRenderer = FluidBlockRenderer.Builder.create()
            .setFaceOffset(-0.125f)
            .setForcedLight(LightTexture.FULL_BRIGHT)
            .getRenderer();

    private final List<RelativeDirection> fluidFaces = List.of(RelativeDirection.DOWN, RelativeDirection.UP);
    private final List<BeeParticle> bees = new ArrayList<>();
    private final List<MoteParticle> motes = new ArrayList<>();
    private final Random random = new Random();
    private @Nullable Fluid cachedFluid = null;

    private HoneyChamberDynamicRender() {
        // Populate the expanded field
        for (int i = 0; i < BEE_COUNT; i++) bees.add(new BeeParticle(random));
        for (int i = 0; i < MOTE_COUNT; i++) motes.add(new MoteParticle(random));
    }

    @Override
    public @NotNull DynamicRenderType<HoneyCrystallizationChamberMachine, HoneyChamberDynamicRender> getType() {
        return TYPE;
    }

    @Override
    public void render(@NotNull HoneyCrystallizationChamberMachine machine, float partialTick,
                       @NotNull PoseStack stack, @NotNull MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!machine.isFormed()) return;

        float time = (machine.getOffsetTimer() + partialTick) * 0.02f;
        RecipeLogic logic = machine.getRecipeLogic();
        float progress = (logic != null && logic.isWorking()) ? (float) logic.getProgressPercent() : 0f;

        stack.pushPose();
        renderFluid(machine, stack, buffer, packedOverlay);

        // Center the render
        stack.translate(0.5, 0.5, 0.5);

        // 1. Fog Layer
        renderHoneyFog(stack, buffer.getBuffer(PhoenixRenderTypes.HONEY_FOG()), time, progress);

        // 2. Expanded Additive Effects
        VertexConsumer additiveVc = buffer.getBuffer(PhoenixRenderTypes.LIGHT_RING());
        renderHoneyStrands(stack, additiveVc, time, progress);

        // Motes and Bees now use the expanded field radius logic
        renderMotes(stack, additiveVc, time, progress);
        renderBees(stack, additiveVc, time, progress);

        stack.popPose();
    }

    private void renderFluid(HoneyCrystallizationChamberMachine machine, PoseStack stack, MultiBufferSource buffer,
                             int packedOverlay) {
        if (!ConfigHolder.INSTANCE.client.renderer.renderFluids) return;
        var offsets = machine.getFluidOffsets();
        if (offsets == null) return;

        var recipeLogic = machine.getRecipeLogic();
        if (recipeLogic == null || recipeLogic.getLastRecipe() == null) return;

        if (machine.isActive()) {
            if (cachedFluid == null || machine.getOffsetTimer() % 20 == 0) {
                cachedFluid = RenderUtil.getRecipeFluidToRender(recipeLogic.getLastRecipe());
            }
        } else {
            cachedFluid = null;
        }

        if (cachedFluid == null) return;

        var fluidLayer = ItemBlockRenderTypes.getRenderLayer(cachedFluid.defaultFluidState());
        var consumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(fluidLayer, false));

        for (RelativeDirection face : fluidFaces) {
            stack.pushPose();
            var dir = face.getRelative(machine.getFrontFacing(), machine.getUpwardsFacing(), machine.isFlipped());

            // Fix: Internal faces for horizontal directions must be flipped to face "in"
            if (dir.getAxis() != Direction.Axis.Y) dir = dir.getOpposite();

            fluidRenderer.drawPlane(dir, offsets, stack.last().pose(), consumer, cachedFluid,
                    RenderUtil.FluidTextureType.STILL, packedOverlay, machine.getPos());
            stack.popPose();
        }
    }

    private void renderHoneyStrands(PoseStack stack, VertexConsumer vc, float time, float progress) {
        Matrix4f pose = stack.last().pose();

        // Color shifts from liquid amber to crystalline gold based on progress
        float r1 = Mth.lerp(progress, 0.90f, 1.00f);
        float g1 = Mth.lerp(progress, 0.65f, 0.85f);
        float b1 = Mth.lerp(progress, 0.18f, 0.40f);

        for (int s = 0; s < STRAND_COUNT; s++) {
            float phase = (float) s / STRAND_COUNT * Mth.TWO_PI;
            Vec3 prev = getStrandPoint(phase, 0f, time);

            for (int i = 1; i <= STRAND_SEGMENTS; i++) {
                float t = (float) i / STRAND_SEGMENTS;
                Vec3 curr = getStrandPoint(phase, t, time);

                // Crystallized strands are slightly thicker and pulse more
                float width = (0.04f + (0.03f * progress)) * (0.8f + 0.2f * Mth.sin(time * 1.5f + s));

                float alpha = Mth.clamp((0.2f + 0.6f * progress) + 0.1f * Mth.sin(time * 2f + t * 5f), 0.1f, 0.9f);

                quadLine(vc, pose, prev, curr, width, r1, g1, b1, alpha);
                // Glow outer-wrap
                quadLine(vc, pose, prev, curr, width * 2.5f, r1, g1, b1, alpha * 0.2f);
                prev = curr;
            }
        }
    }

    private Vec3 getStrandPoint(float phase, float t, float time) {
        float angle = t * Mth.TWO_PI + time * 0.3f + phase;
        float radius = STRAND_RADIUS + 0.08f * Mth.sin(time * 0.7f + t * 4f + phase);
        // Vertical swaying
        float y = (t * 2.0f - 1.0f) + 0.05f * Mth.sin(time * 1.1f + phase + t * 3f);
        return new Vec3(radius * Mth.cos(angle), y, radius * Mth.sin(angle));
    }

    private void quadLine(VertexConsumer vc, Matrix4f pose, Vec3 start, Vec3 end, float width, float r, float g,
                          float b, float a) {
        Vec3 dir = end.subtract(start).normalize();
        Vec3 up = Math.abs(dir.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 side = dir.cross(up).normalize().scale(width);

        vertex(vc, pose, start.add(side), r, g, b, a);
        vertex(vc, pose, start.subtract(side), r, g, b, a);
        vertex(vc, pose, end.subtract(side), r, g, b, a);
        vertex(vc, pose, end.add(side), r, g, b, a);
    }

    private void vertex(VertexConsumer vc, Matrix4f pose, Vec3 p, float r, float g, float b, float a) {
        vc.vertex(pose, (float) p.x, (float) p.y, (float) p.z).color(r, g, b, a).endVertex();
    }

    private void renderBees(PoseStack stack, VertexConsumer vc, float time, float progress) {
        Matrix4f pose = stack.last().pose();
        for (BeeParticle bee : bees) {
            bee.update(time, progress); // Pass progress to make them swarm faster
            // Fade out at the edge of the 30-block field
            float distFactor = (float) Mth.clamp(1.0 - (bee.pos.length() / FIELD_RADIUS), 0.0, 1.0);
            float alpha = (0.3f + 0.7f * progress) * distFactor;

            quadLine(vc, pose, bee.prevPos, bee.pos, 0.05f, 1.0f, 0.8f, 0.1f, alpha);
        }
    }

    private void renderMotes(PoseStack stack, VertexConsumer vc, float time, float progress) {
        Matrix4f pose = stack.last().pose();
        for (MoteParticle mote : motes) {
            mote.update(time);
            float distFactor = (float) Mth.clamp(1.0 - (mote.pos.length() / FIELD_RADIUS), 0.0, 1.0);
            float alpha = (0.1f + 0.3f * progress) * distFactor *
                    (0.6f + 0.4f * Mth.sin(time * 2f + mote.flickerPhase));

            Vec3 p = mote.pos;
            quadLine(vc, pose, p.subtract(0, 0.03, 0), p.add(0, 0.03, 0), 0.015f, 1.0f, 0.9f, 0.4f, alpha);
            quadLine(vc, pose, p.subtract(0.03, 0, 0), p.add(0.03, 0, 0), 0.015f, 1.0f, 0.9f, 0.4f, alpha);
        }
    }

    private void renderHoneyFog(PoseStack stack, VertexConsumer vc, float time, float progress) {
        Matrix4f pose = stack.last().pose();
        float alpha = (0.08f + 0.2f * progress) * (0.9f + 0.1f * Mth.sin(time));
        float r = 0.9f, g = 0.7f, b = 0.2f, h = 1.2f, w = 0.03f;
        // Simple cage bounds
        Vec3[] p = { new Vec3(-h, -0.8, -h), new Vec3(-h, -0.8, h), new Vec3(-h, 0.8, -h), new Vec3(-h, 0.8, h),
                new Vec3(h, -0.8, -h), new Vec3(h, -0.8, h), new Vec3(h, 0.8, -h), new Vec3(h, 0.8, h) };

        for (int i = 0; i < 4; i++) {
            quadLine(vc, pose, p[i], p[i + 4], w, r, g, b, alpha); // Horizontal
            quadLine(vc, pose, p[i * 2 % 8], p[(i * 2 + 1) % 8], w, r, g, b, alpha); // Vertical
        }
    }

    @Override
    public boolean shouldRenderOffScreen(HoneyCrystallizationChamberMachine m) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    } // Increase view distance for the 30-block swarm

    @Override
    public @NotNull AABB getRenderBoundingBox(HoneyCrystallizationChamberMachine m) {
        // Expand bounding box to 30+ blocks to prevent clipping
        return new AABB(m.getPos()).inflate(FIELD_RADIUS + 5);
    }

    private static class BeeParticle {

        Vec3 pos, prevPos;
        float radius, speed, phase, height, nX, nZ;

        BeeParticle(Random r) {
            // Distribute bees up to the FIELD_RADIUS
            radius = r.nextFloat() * FIELD_RADIUS;
            speed = 0.2f + r.nextFloat() * 0.5f;
            phase = r.nextFloat() * 6.28f;
            height = -10.0f + r.nextFloat() * 20.0f; // Tall vertical spread
            nX = r.nextFloat() * 100f;
            nZ = r.nextFloat() * 100f;
            pos = prevPos = Vec3.ZERO;
        }

        void update(float t, float progress) {
            prevPos = pos;
            // Bees move faster and circle tighter as progress increases
            float curSpeed = speed * (1.0f + progress);
            float curRadius = radius * (1.0f - (progress * 0.5f));
            float a = t * curSpeed + phase;

            pos = new Vec3(
                    curRadius * Mth.cos(a) + Mth.sin(t * 0.5f + nX) * 2.0f,
                    height + Mth.sin(t * 0.3f) * 3.0f,
                    curRadius * Mth.sin(a) + Mth.cos(t * 0.5f + nZ) * 2.0f);
        }
    }

    private static class MoteParticle {

        Vec3 pos;
        float angle, radius, height, speed, flickerPhase;

        MoteParticle(Random r) {
            angle = r.nextFloat() * 6.28f;
            radius = r.nextFloat() * FIELD_RADIUS;
            height = -15.0f + r.nextFloat() * 30.0f; // Wide vertical volume
            speed = 0.05f + r.nextFloat() * 0.15f;
            flickerPhase = r.nextFloat() * 6.28f;
            pos = Vec3.ZERO;
        }

        void update(float t) {
            float a = angle + t * speed;
            pos = new Vec3(radius * Mth.cos(a), height + Mth.sin(t * 0.2f + flickerPhase), radius * Mth.sin(a));
        }
    }
}
