package net.phoenix.core.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.phoenix.core.PhoenixCore;
import net.phoenix.core.client.renderer.PhoenixRenderTypes;
import net.phoenix.core.client.renderer.PhoenixShaders;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class BlackHoleRenderer extends DynamicRender<WorkableElectricMultiblockMachine, BlackHoleRenderer> {

    public static final BlackHoleRenderer INSTANCE = new BlackHoleRenderer();
    public static final Codec<BlackHoleRenderer> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, BlackHoleRenderer> TYPE = new DynamicRenderType<>(CODEC);

    public static final ResourceLocation DISK_MODEL_RL = PhoenixCore.id("machine/space");
    public static final ResourceLocation CORE_MODEL_RL = PhoenixCore.id("machine/star");

    private static final ResourceLocation DISK_TEX = PhoenixCore.id("textures/entity/black_hole_disk.png");

    private BlackHoleRenderer() {}

    @Override
    public @NotNull DynamicRenderType<WorkableElectricMultiblockMachine, BlackHoleRenderer> getType() {
        return TYPE;
    }

    @Override
    public void render(WorkableElectricMultiblockMachine machine, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {

        if (!machine.isFormed() || !machine.getRecipeLogic().isWorking()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        ShaderInstance bh = PhoenixShaders.getBlackHoleShader(); // may be null, that's fine

        Direction facing = machine.getFrontFacing();
        double offsetDistance = 20.0;

        Vec3 centerPos = Vec3.atCenterOf(machine.getPos()).add(
                facing.getStepX() * offsetDistance,
                facing.getStepY() * offsetDistance,
                facing.getStepZ() * offsetDistance
        );

        Vector2f screenUv = projectToScreenUv(centerPos);

        // Only update + render warp if shader exists and we projected on-screen
        if (bh != null && screenUv.x >= 0.0f) {
            updateLensUniforms(bh, screenUv, mc.level.getGameTime() + partialTick);
        }

        poseStack.pushPose();
        poseStack.translate(
                facing.getStepX() * offsetDistance,
                facing.getStepY() * offsetDistance,
                facing.getStepZ() * offsetDistance
        );

        renderPhysicalModels(poseStack, buffer, partialTick, packedLight, packedOverlay, facing);

        if (bh != null && screenUv.x >= 0.0f) {
            renderWarpQuad(poseStack, buffer);
        }

        poseStack.popPose();
    }


    private void renderPhysicalModels(PoseStack poseStack, MultiBufferSource buffer, float partialTick, int light,
                                      int overlay, Direction facing) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float time = mc.level.getGameTime() + partialTick;

        // Disk
        poseStack.pushPose();
        poseStack.mulPose(facing.getRotation());
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 1.5f));
        poseStack.mulPose(Axis.XP.rotationDegrees(20f));
        poseStack.scale(0.8f, 0.8f, 0.8f);
        renderModel(DISK_MODEL_RL, poseStack, buffer, light, overlay);
        poseStack.popPose();

        // Core
        poseStack.pushPose();
        float pulse = 1.0f + 0.08f * (float) Math.sin(time * 0.15f);
        poseStack.scale(pulse, pulse, pulse);
        renderModel(CORE_MODEL_RL, poseStack, buffer, 15728880, overlay);
        poseStack.popPose();
    }

    private void renderModel(ResourceLocation modelRL, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        // Hard safety: never use this RenderType if the shader didn't load
        if (PhoenixShaders.getBlenderShader() == null) return;

        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getModelManager().getModel(modelRL);

        VertexConsumer vc = buffer.getBuffer(PhoenixRenderTypes.BLENDER_MATERIAL(DISK_TEX));
        mc.getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(), vc, null, model,
                1.0f, 1.0f, 1.0f,
                light, overlay
        );
    }

    private void renderWarpQuad(PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();

        Quaternionf camRot = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        poseStack.mulPose(camRot);

        // tiny push forward
        poseStack.translate(0, 0, 0.01f);

        VertexConsumer vc = buffer.getBuffer(PhoenixRenderTypes.BLACK_HOLE());
        Matrix4f m = poseStack.last().pose();

        float size = 8.0f;
        vc.vertex(m, -size, -size, 0).uv(0, 0).endVertex();
        vc.vertex(m, -size,  size, 0).uv(0, 1).endVertex();
        vc.vertex(m,  size,  size, 0).uv(1, 1).endVertex();
        vc.vertex(m,  size, -size, 0).uv(1, 0).endVertex();

        poseStack.popPose();
    }

    private void updateLensUniforms(ShaderInstance shader, Vector2f screenUv, float gameTime) {
        float w = (float) Minecraft.getInstance().getWindow().getWidth();
        float h = (float) Minecraft.getInstance().getWindow().getHeight();

        var uScreen = shader.getUniform("ScreenSize");
        if (uScreen != null) uScreen.set(w, h);

        var uPos = shader.getUniform("BlackHolePos");
        if (uPos != null) uPos.set(screenUv.x, screenUv.y);

        var uTime = shader.getUniform("GameTime");
        if (uTime != null) uTime.set(gameTime);

        var uRad = shader.getUniform("BlackHoleRadius");
        if (uRad != null) uRad.set(0.06f);

        var uStr = shader.getUniform("DistortionStrength");
        if (uStr != null) uStr.set(1.25f);
    }


    private Vector2f projectToScreenUv(Vec3 worldPos) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        Vector4f p = new Vector4f(
                (float) (worldPos.x - camPos.x),
                (float) (worldPos.y - camPos.y),
                (float) (worldPos.z - camPos.z),
                1.0f
        );

        p.mul(RenderSystem.getModelViewMatrix());
        p.mul(RenderSystem.getProjectionMatrix());

        if (p.w() <= 0.0f) return new Vector2f(-1, -1);

        float ndcX = p.x() / p.w();
        float ndcY = p.y() / p.w();

        if (ndcX < -1.2f || ndcX > 1.2f || ndcY < -1.2f || ndcY > 1.2f) {
            return new Vector2f(-1, -1);
        }

        return new Vector2f((ndcX + 1.0f) * 0.5f, 1.0f - (ndcY + 1.0f) * 0.5f);
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull WorkableElectricMultiblockMachine m) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
