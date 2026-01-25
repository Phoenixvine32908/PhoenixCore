package net.phoenix.core.client.renderer.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix.core.PhoenixCore;
import net.phoenix.core.client.renderer.PhoenixShaders;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Objects;

/**
 * Applies the black hole lensing as a post-pass.
 *
 * Fixes:
 *  - UV origin consistency: projection now matches OpenGL (bottom-left origin).
 *  - Logs whether fallback radius was used.
 *  - Uses framebuffer sizes for ScreenSize.
 *  - Disables blending for fullscreen replace.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = PhoenixCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientRenderHook {

    private static final boolean DEBUG_BH = true;
    private static final int DEBUG_EVERY_TICKS = 10;

    // Optional visibility clamps (for testing)
    private static final float MIN_RADIUS_UV_FOR_VISIBILITY = 0.2f;
    private static final float MAX_RADIUS_UV = 4.30f;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var shader = PhoenixShaders.getBlackHoleShader();
        if (shader == null) return;

        long tick = mc.level.getGameTime();
        var post = BlackHolePost.INSTANCE;

        boolean dbg = DEBUG_BH && (tick % DEBUG_EVERY_TICKS == 0);

        if (!post.isActive(tick)) {
            if (dbg) PhoenixCore.LOGGER.info("BH skip: post inactive tick={}", tick);
            return;
        }

        // 1) Matrices + camera
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 centerWorld = post.centerWorld();

        Matrix4f view = event.getPoseStack().last().pose();
        Matrix4f proj = new Matrix4f(event.getProjectionMatrix());

        // 2) Project center
        float[] uv = projectWorldToUv(centerWorld, camPos, view, proj, dbg, "center");
        if (uv == null) {
            if (dbg) PhoenixCore.LOGGER.info("BH clear: center projection failed");
            post.clear();
            return;
        }

        // 3) Project edge to estimate radius
        org.joml.Vector3f leftF = mc.gameRenderer.getMainCamera().getLeftVector();
        Vec3 rightDir = new Vec3(-leftF.x(), -leftF.y(), -leftF.z());

        Vec3 edgeWorld = centerWorld.add(rightDir.scale(post.worldRadiusBlocks()));
        float[] uvEdge = projectWorldToUv(edgeWorld, camPos, view, proj, dbg, "edge");

        boolean usedFallback = (uvEdge == null);

        float radiusUvRaw = post.fallbackRadiusUv();
        if (!usedFallback) {
            radiusUvRaw = (float) Math.hypot(uvEdge[0] - uv[0], uvEdge[1] - uv[1]);
        }

        float radiusUv = radiusUvRaw;

        // visibility clamp (optional)
        radiusUv = Math.max(MIN_RADIUS_UV_FOR_VISIBILITY, radiusUv);

        // safety clamp
        radiusUv = Math.max(0.0025f, Math.min(MAX_RADIUS_UV, radiusUv));

        post.setScreenUv(uv[0], uv[1], radiusUv);

        if (dbg) {
            PhoenixCore.LOGGER.info(
                    "BH world center={}, cam={}, worldRadiusBlocks={}, strength={}, " +
                            "centerUv=({}, {}), edgeUv={}, usedFallback={}, radiusUvRaw={}, finalRadiusUv={}",
                    vec3Str(centerWorld), vec3Str(camPos),
                    fmt3(post.worldRadiusBlocks()), fmt3(post.strength()),
                    fmt4(uv[0]), fmt4(uv[1]),
                    (uvEdge == null ? "null" : "(" + fmt4(uvEdge[0]) + ", " + fmt4(uvEdge[1]) + ")"),
                    usedFallback,
                    fmt6(radiusUvRaw),
                    fmt6(radiusUv)
            );
        }

        // 4) Copy main -> scratch, then lens scratch -> main
        var main = mc.getMainRenderTarget();
        var scratch = BlackHoleTargets.scratch();

        if (dbg) {
            PhoenixCore.LOGGER.info("BH RT sizes: main={}x{}, scratch={}x{}", main.width, main.height, scratch.width, scratch.height);
        }

        // 4a) Copy pass
        scratch.bindWrite(false);
        RenderSystem.viewport(0, 0, scratch.width, scratch.height);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, main.getColorTextureId());
        drawFullscreenQuad();

        scratch.unbindWrite();

        // 4b) Lensing pass
        main.bindWrite(false);
        RenderSystem.viewport(0, 0, main.width, main.height);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        // fullscreen replace
        RenderSystem.disableBlend();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, scratch.getColorTextureId());

        // Upload uniforms (ScreenSize must match the UV space your shader expects)
        if (shader.getUniform("ScreenSize") != null)
            Objects.requireNonNull(shader.getUniform("ScreenSize")).set((float) main.width, (float) main.height);
        if (shader.getUniform("BlackHolePos") != null)
            Objects.requireNonNull(shader.getUniform("BlackHolePos")).set(post.xUv(), post.yUv());
        if (shader.getUniform("BlackHoleRadius") != null)
            Objects.requireNonNull(shader.getUniform("BlackHoleRadius")).set(post.radiusUv());
        if (shader.getUniform("DistortionStrength") != null)
            Objects.requireNonNull(shader.getUniform("DistortionStrength")).set(post.strength());
        if (shader.getUniform("GameTime") != null)
            Objects.requireNonNull(shader.getUniform("GameTime")).set((float) mc.level.getGameTime() + event.getPartialTick());

        if (dbg) {
            PhoenixCore.LOGGER.info(
                    "BH uniforms: ScreenSize=({}, {}), Pos=({}, {}), RadiusUv={}, Strength={}",
                    main.width, main.height,
                    fmt4(post.xUv()), fmt4(post.yUv()),
                    fmt6(post.radiusUv()),
                    fmt3(post.strength())
            );
        }

        drawFullscreenQuad();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        post.clearScreenUv();
    }

    /**
     * IMPORTANT: returns UV with OpenGL-style origin (bottom-left):
     *  u = (ndcX+1)/2
     *  v = (ndcY+1)/2   <-- NO inversion
     */
    private static float[] projectWorldToUv(Vec3 world, Vec3 camPos, Matrix4f view, Matrix4f proj, boolean dbg, String tag) {
        Vector4f p = new Vector4f(
                (float) (world.x - camPos.x),
                (float) (world.y - camPos.y),
                (float) (world.z - camPos.z),
                1.0f
        );
        p.mul(view).mul(proj);

        float w = p.w();
        if (w <= 0.0f) {
            if (dbg) PhoenixCore.LOGGER.info("BH project {}: behind camera (w={}) world={}", tag, fmt3(w), vec3Str(world));
            return null;
        }

        float ndcX = p.x() / w;
        float ndcY = p.y() / w;

        if (dbg) {
            PhoenixCore.LOGGER.info(
                    "BH project {}: world={} ndc=({}, {})",
                    tag, vec3Str(world), fmt3(ndcX), fmt3(ndcY)
            );
        }

        // reject far off-screen
        if (ndcX < -1.5f || ndcX > 1.5f || ndcY < -1.5f || ndcY > 1.5f) return null;

        return new float[] {
                (ndcX + 1.0f) * 0.5f,
                (ndcY + 1.0f) * 0.5f
        };
    }

    private static void drawFullscreenQuad() {
        var t = Tesselator.getInstance();
        BufferBuilder b = t.getBuilder();

        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        b.vertex(-1f, -1f, 0f).uv(0f, 0f).endVertex();
        b.vertex(-1f,  1f, 0f).uv(0f, 1f).endVertex();
        b.vertex( 1f,  1f, 0f).uv(1f, 1f).endVertex();
        b.vertex( 1f, -1f, 0f).uv(1f, 0f).endVertex();

        BufferUploader.drawWithShader(b.end());
    }

    private static String vec3Str(Vec3 v) {
        return String.format("(%.3f, %.3f, %.3f)", v.x, v.y, v.z);
    }

    private static String fmt3(double v) { return String.format("%.3f", v); }
    private static String fmt3(float v)  { return String.format("%.3f", v); }
    private static String fmt4(float v)  { return String.format("%.4f", v); }
    private static String fmt6(float v)  { return String.format("%.6f", v); }
}
