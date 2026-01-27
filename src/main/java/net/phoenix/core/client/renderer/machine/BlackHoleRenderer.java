package net.phoenix.core.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.phoenix.core.PhoenixCore;
import net.phoenix.core.client.renderer.PhoenixRenderTypes;
import net.phoenix.core.client.renderer.PhoenixShaders;
import net.phoenix.core.client.renderer.utils.BlackHolePost;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

/**
 * Renders the model and requests a world-anchored lensing post-pass.
 * (No world-space warp quad; lensing is done in ClientRenderHook.)
 */
public class BlackHoleRenderer extends DynamicRender<WorkableElectricMultiblockMachine, BlackHoleRenderer> {

    public static final BlackHoleRenderer INSTANCE = new BlackHoleRenderer();
    public static final Codec<BlackHoleRenderer> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, BlackHoleRenderer> TYPE = new DynamicRenderType<>(
            CODEC);

    public static final ResourceLocation CORE_MODEL_RL = PhoenixCore.id("obj/star");
    private static final ResourceLocation CORE_TEX = PhoenixCore.id("textures/entity/black_hole_disk.png");

    private static final float CORE_SCALE_BLOCKS = 0.01f;
    private static final float LENS_RADIUS_BLOCKS = 100.0f;
    private static final float LENS_STRENGTH = 12.25f;
    private static final float LENS_FALLBACK_RADIUS_UV = 0.5f;

    private static final double OFFSET_DISTANCE = 10.0;

    private BlackHoleRenderer() {}

    @Override
    public @NotNull DynamicRenderType<WorkableElectricMultiblockMachine, BlackHoleRenderer> getType() {
        return TYPE;
    }

    @Override
    public void render(WorkableElectricMultiblockMachine machine,
                       float partialTick,
                       @NotNull PoseStack poseStack,
                       @NotNull net.minecraft.client.renderer.MultiBufferSource buffer,
                       int packedLight,
                       int packedOverlay) {
        if (!machine.isFormed() || !machine.getRecipeLogic().isWorking()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Direction facing = machine.getFrontFacing();

        Vec3 centerPos = Vec3.atCenterOf(machine.getPos()).add(
                facing.getStepX() * OFFSET_DISTANCE,
                facing.getStepY() * OFFSET_DISTANCE,
                facing.getStepZ() * OFFSET_DISTANCE);

        long tick = mc.level.getGameTime();
        BlackHolePost.INSTANCE.setWorld(centerPos, LENS_RADIUS_BLOCKS, LENS_STRENGTH, LENS_FALLBACK_RADIUS_UV, tick);

        // Render model at same position
        poseStack.pushPose();
        poseStack.translate(
                facing.getStepX() * OFFSET_DISTANCE,
                facing.getStepY() * OFFSET_DISTANCE,
                facing.getStepZ() * OFFSET_DISTANCE);

        float time = mc.level.getGameTime() + partialTick;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 1.2f));
        poseStack.scale(CORE_SCALE_BLOCKS, CORE_SCALE_BLOCKS, CORE_SCALE_BLOCKS);
        renderModel(CORE_MODEL_RL, poseStack, buffer, 15728880, packedOverlay);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void renderModel(ResourceLocation modelRL, PoseStack poseStack,
                                    net.minecraft.client.renderer.MultiBufferSource buffer,
                                    int light, int overlay) {
        if (PhoenixShaders.getBlenderShader() == null) return;

        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getModelManager().getModel(modelRL);

        VertexConsumer vc = buffer.getBuffer(PhoenixRenderTypes.BLENDER_MATERIAL(CORE_TEX));
        mc.getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(), vc, null, model,
                1.0f, 1.0f, 1.0f,
                light, overlay);
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull WorkableElectricMultiblockMachine m) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
