package net.phoenix.core.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.phoenix.core.PhoenixCore;
import net.phoenix.core.client.renderer.PhoenixRenderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlackHoleRenderer extends DynamicRender<WorkableElectricMultiblockMachine, BlackHoleRenderer> {

    public static final BlackHoleRenderer INSTANCE = new BlackHoleRenderer();
    public static final Codec<BlackHoleRenderer> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, BlackHoleRenderer> TYPE = new DynamicRenderType<>(
            CODEC);

    public static final ResourceLocation SPHERE_MODEL_RL = PhoenixCore.id("obj/star");
    static final RandomSource random = RandomSource.create(0L);

    private static final double OFFSET_DISTANCE = 10.0;
    private static final float OUTER_SPHERE_SCALE = 1.0f;
    private static final float INNER_SPHERE_SCALE = 0.4f;

    private BlackHoleRenderer() {}

    @Override
    public @NotNull DynamicRenderType<WorkableElectricMultiblockMachine, BlackHoleRenderer> getType() {
        return TYPE;
    }

    @Override
    public void render(WorkableElectricMultiblockMachine machine,
                       float partialTick,
                       @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer,
                       int packedLight,
                       int packedOverlay) {
        if (!machine.isFormed() || !machine.getRecipeLogic().isWorking()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Direction facing = machine.getFrontFacing();

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.translate(
                facing.getStepX() * OFFSET_DISTANCE,
                facing.getStepY() * OFFSET_DISTANCE,
                facing.getStepZ() * OFFSET_DISTANCE);

        float time = mc.level.getGameTime() + partialTick;

        // Render the outer sphere (for distortion)
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 1.2f));
        poseStack.scale(OUTER_SPHERE_SCALE, OUTER_SPHERE_SCALE, OUTER_SPHERE_SCALE);
        renderModel(SPHERE_MODEL_RL, poseStack, buffer, packedLight, packedOverlay, true);
        poseStack.popPose();

        // Render the inner sphere (the black hole itself)
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 1.2f));
        poseStack.scale(INNER_SPHERE_SCALE, INNER_SPHERE_SCALE, INNER_SPHERE_SCALE);
        renderModel(SPHERE_MODEL_RL, poseStack, buffer, packedLight, packedOverlay, false);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void renderModel(ResourceLocation modelRL, PoseStack poseStack,
                                    MultiBufferSource buffer,
                                    int light, int overlay, boolean isOuterSphere) {
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getModelManager().getModel(modelRL);
        PoseStack.Pose pose = poseStack.last();

        if (isOuterSphere) {
            VertexConsumer vc = buffer.getBuffer(PhoenixRenderTypes.BLACK_HOLE_TEST_TRANSLUCENT());
            List<BakedQuad> quads = model.getQuads(null, null, random, ModelData.EMPTY, null);
            for (BakedQuad quad : quads) {
                vc.putBulkData(pose, quad, 1.0f, 0.0f, 0.0f, 0.5f, light, overlay, false);
            }
        } else {
            VertexConsumer vc = buffer.getBuffer(PhoenixRenderTypes.BLACK_HOLE_TEST_SOLID());
            List<BakedQuad> quads = model.getQuads(null, null, random, ModelData.EMPTY, null);
            for (BakedQuad quad : quads) {
                vc.putBulkData(pose, quad, 0.0f, 0.0f, 0.0f, 1.0f, light, overlay, false);
            }
        }
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
