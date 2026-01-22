package net.phoenix.core.client.renderer.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.core.PhoenixCore;
import net.phoenix.core.client.renderer.PhoenixShaders;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL13;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = PhoenixCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientRenderHook {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var shader = PhoenixShaders.getBlackHoleShader();
        var post = BlackHolePost.INSTANCE;
        if (shader == null || !post.isActive()) return;

        // 1. Projection
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        Matrix4f view = event.getPoseStack().last().pose();
        Matrix4f proj = new Matrix4f(event.getProjectionMatrix());

        float[] uv = projectWorldToUv(post.centerWorld(), camPos, view, proj);
        if (uv == null) { post.clear(); return; }

        // 2. Radius calculation via Camera-Left vector
        org.joml.Vector3f leftF = mc.gameRenderer.getMainCamera().getLeftVector();
        Vec3 rightDir = new Vec3(-leftF.x(), -leftF.y(), -leftF.z());
        Vec3 edgeWorld = post.centerWorld().add(rightDir.scale(post.worldRadiusBlocks()));
        float[] uvEdge = projectWorldToUv(edgeWorld, camPos, view, proj);

        float radiusUv = post.fallbackRadiusUv();
        if (uvEdge != null) {
            radiusUv = (float) Math.hypot(uvEdge[0] - uv[0], uvEdge[1] - uv[1]);
        }
        radiusUv = Math.max(0.0025f, Math.min(0.35f, radiusUv));

        post.setScreenUv(uv[0], uv[1], radiusUv);

        // 3. Render
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> shader);

        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        RenderSystem.bindTexture(mc.getMainRenderTarget().getColorTextureId());

        // Upload Uniforms
        if (shader.getUniform("ScreenSize") != null)
            shader.getUniform("ScreenSize").set((float)mc.getWindow().getWidth(), (float)mc.getWindow().getHeight());
        if (shader.getUniform("BlackHolePos") != null)
            shader.getUniform("BlackHolePos").set(post.xUv(), post.yUv());
        if (shader.getUniform("BlackHoleRadius") != null)
            shader.getUniform("BlackHoleRadius").set(post.radiusUv());
        if (shader.getUniform("DistortionStrength") != null)
            shader.getUniform("DistortionStrength").set(post.strength());
        if (shader.getUniform("GameTime") != null)
            shader.getUniform("GameTime").set((float)mc.level.getGameTime() + event.getPartialTick());

        drawFullscreenQuad();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        post.clear();
    }

    private static float[] projectWorldToUv(Vec3 world, Vec3 camPos, Matrix4f view, Matrix4f proj) {
        Vector4f p = new Vector4f((float)(world.x - camPos.x), (float)(world.y - camPos.y), (float)(world.z - camPos.z), 1.0f);
        p.mul(view).mul(proj);
        if (p.w() <= 0.0f) return null; // Behind camera
        return new float[]{(p.x() / p.w() + 1.0f) * 0.5f, 1.0f - (p.y() / p.w() + 1.0f) * 0.5f};
    }

    private static void drawFullscreenQuad() {
        var t = Tesselator.getInstance();
        var b = t.getBuilder();
        b.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        b.vertex(-1, -1, 0).uv(0, 0).endVertex();
        b.vertex(-1,  1, 0).uv(0, 1).endVertex();
        b.vertex( 1,  1, 0).uv(1, 1).endVertex();
        b.vertex( 1, -1, 0).uv(1, 0).endVertex();
        t.end();
    }
}