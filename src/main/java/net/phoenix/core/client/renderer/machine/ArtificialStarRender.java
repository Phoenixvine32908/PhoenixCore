package net.phoenix.core.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.ModelUtils;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.ModelData;
import net.phoenix.core.PhoenixCore;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.joml.Quaternionf;

import java.util.List;

public class ArtificialStarRender extends DynamicRender<WorkableElectricMultiblockMachine, ArtificialStarRender> {

    public static final ArtificialStarRender INSTANCE = new ArtificialStarRender();
    public static final Codec<ArtificialStarRender> CODEC = Codec.unit(ArtificialStarRender.INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, ArtificialStarRender> TYPE = new DynamicRenderType<>(
            ArtificialStarRender.CODEC);

    public static final ResourceLocation ARTIFICIAL_STAR_MODEL_RL = PhoenixCore.id("obj/star");

    private static BakedModel artificialStarModel;
    private static final RandomSource random = RandomSource.create();

    // --- CONSTANTES ADICIONADAS PARA CONTROLE DA ROTAÇÃO ---
    private static final float ROTATION_SPEED = 0.025F; // Velocidade base do movimento
    private static final float MAX_ROTATION_ANGLE = 30.0F; // Amplitude máxima do movimento principal

    private ArtificialStarRender() {
        ModelUtils.registerBakeEventListener(true, event -> {
            artificialStarModel = event.getModels().get(ARTIFICIAL_STAR_MODEL_RL);
        });
    }

    @Override
    public DynamicRenderType<WorkableElectricMultiblockMachine, ArtificialStarRender> getType() {
        return TYPE;
    }

    @Override
    public void render(WorkableElectricMultiblockMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!machine.isFormed() || !machine.getRecipeLogic().isActive()) {
            return;
        }

        float tick = (machine.getOffsetTimer() + partialTick);

        // Lógica de posicionamento (sem alterações)
        // Center of the multiblock
        double x = 0.5;
        double y = 2.5; // a little above the controller block
        double z = 0.5;

        // Offset in front of the facing direction by 5 blocks
        switch (machine.getFrontFacing()) {
            case NORTH -> z -= 5.0;
            case SOUTH -> z += 5.0;
            case WEST -> x -= 5.0;
            case EAST -> x += 5.0;
        }

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        renderArtificialStar(tick, poseStack, buffer, packedOverlay);

        poseStack.popPose();
    }

    private void renderArtificialStar(float tick, PoseStack poseStack, MultiBufferSource buffer, int packedOverlay) {
        if (artificialStarModel == null) return;
        poseStack.pushPose();

        // Lógica de rotação em formato de símbolo do infinito
        float angleY = MAX_ROTATION_ANGLE * (float) Math.sin(tick * ROTATION_SPEED);
        float angleX = (MAX_ROTATION_ANGLE / 2.0f) * (float) Math.sin(tick * ROTATION_SPEED * 2.0f);

        poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(0.0F, 1.0F, 0.0F, angleY));
        poseStack.mulPose(new Quaternionf().fromAxisAngleDeg(1.0F, 0.0F, 0.0F, angleX));

        // Lógica de renderização dupla para efeito de bloom

        // 1. Renderiza a estrela principal (interna, SÓLIDA e MENOR)
        poseStack.pushPose();
        // ALTERADO: Escala diminuída para 0.40F
        poseStack.scale(0.40F, 0.40F, 0.40F);
        // ALTERADO: Usando solidBlockSheet para garantir que seja opaca
        renderModel(poseStack, buffer.getBuffer(Sheets.solidBlockSheet()), artificialStarModel, 1.0F, 1.0F, 1.0F, 1.0f,
                LightTexture.FULL_BRIGHT, packedOverlay);
        poseStack.popPose();

        // 2. Renderiza a aura de brilho (externa, MENOR e semi-transparente)
        poseStack.pushPose();
        // ALTERADO: Escala diminuída para 0.50F
        poseStack.scale(0.407F, 0.407F, 0.407F);
        renderModel(poseStack, buffer.getBuffer(RenderType.translucent()), artificialStarModel, 1.0F, 1.0F, 1.0F, 0.6F,
                LightTexture.FULL_BRIGHT, packedOverlay);
        poseStack.popPose();

        poseStack.popPose();
    }

    private void renderModel(PoseStack poseStack, VertexConsumer consumer, BakedModel model, float r, float g, float b,
                             float a, int light, int overlay) {
        if (model == null) return;
        PoseStack.Pose pose = poseStack.last();
        List<BakedQuad> quads = model.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, r, g, b, a, light, overlay, false);
        }
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(WorkableElectricMultiblockMachine machine) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(WorkableElectricMultiblockMachine machine) {
        return new AABB(machine.getPos()).inflate(getViewDistance());
    }
}
