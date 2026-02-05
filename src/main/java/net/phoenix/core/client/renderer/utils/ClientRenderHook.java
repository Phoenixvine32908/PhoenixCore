package net.phoenix.core.client.renderer.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.core.PhoenixCore;
import net.phoenix.core.client.renderer.PhoenixShaders;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = PhoenixCore.MOD_ID)
public final class ClientRenderHook {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // Reverting to AFTER_PARTICLES as requested for a stable baseline.
        // This will work for solid blocks but not for transparent ones like water.
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        var shader = PhoenixShaders.getBlackHoleShader();
        var post = BlackHolePost.INSTANCE;

        if (mc.level == null || shader == null || !post.isActive(mc.level.getGameTime())) {
            return;
        }

        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 centerWorld = post.centerWorld();
        Matrix4f proj = event.getProjectionMatrix();

        float[] uv = projectWorldToUv(centerWorld, camPos, event.getPoseStack().last().pose(), proj);
        if (uv == null) {
            post.clearScreenUv();
            return;
        }

        double distance = camPos.distanceTo(centerWorld);
        float tanFovXOver2 = 1.0f / proj.m00();
        float radiusUv = (float) ((post.worldRadiusBlocks() / distance) / (2.0f * tanFovXOver2));
        post.setScreenUv(uv[0], uv[1], radiusUv);

        var main = mc.getMainRenderTarget();
        BlackHoleTargets.ensureSize(main.width, main.height);
        var scratch = BlackHoleTargets.scratch();

        Matrix4f oldProj = RenderSystem.getProjectionMatrix();
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f().identity(), VertexSorting.DISTANCE_TO_ORIGIN);

        // Copy Pass
        scratch.bindWrite(false);
        RenderSystem.disableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, main.getColorTextureId());
        drawFullscreenQuad();
        scratch.unbindWrite();

        // Lensing Pass
        main.bindWrite(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, scratch.getColorTextureId());

        var window = mc.getWindow();
        if (shader.getUniform("ScreenSize") != null)
            shader.getUniform("ScreenSize").set((float)window.getWidth(), (float)window.getHeight());
        if (shader.getUniform("BlackHolePos") != null)
            shader.getUniform("BlackHolePos").set(post.xUv(), post.yUv());
        if (shader.getUniform("BlackHoleRadius") != null)
            shader.getUniform("BlackHoleRadius").set(post.radiusUv());
        if (shader.getUniform("DistortionStrength") != null)
            shader.getUniform("DistortionStrength").set(post.strength());
        if (shader.getUniform("GameTime") != null)
            shader.getUniform("GameTime").set(mc.level.getGameTime() + event.getPartialTick());

        drawFullscreenQuad();

        // Cleanup
        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(oldProj, VertexSorting.DISTANCE_TO_ORIGIN);
        RenderSystem.disableBlend();
    }

    private static float[] projectWorldToUv(Vec3 world, Vec3 camPos, Matrix4f view, Matrix4f proj) {
        Vector4f p = new Vector4f((float) (world.x - camPos.x), (float) (world.y - camPos.y), (float) (world.z - camPos.z), 1.0f);
        p.mul(view).mul(proj);
        if (p.w() <= 0.0f) return null;
        float ndcX = p.x() / p.w();
        float ndcY = p.y() / p.w();
        if (ndcX < -1.5f || ndcX > 1.5f || ndcY < -1.5f || ndcY > 1.5f) return null;
        return new float[]{(ndcX + 1.0f) * 0.5f, (ndcY + 1.0f) * 0.5f};
    }

    private static void drawFullscreenQuad() {
        var t = Tesselator.getInstance();
        BufferBuilder b = t.getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        b.vertex(-1f, -1f, 0f).uv(0f, 0f).endVertex();
        b.vertex(-1f, 1f, 0f).uv(0f, 1f).endVertex();
        b.vertex(1f, 1f, 0f).uv(1f, 1f).endVertex();
        b.vertex(1f, -1f, 0f).uv(1f, 0f).endVertex();
        BufferUploader.drawWithShader(b.end());
    }
}
