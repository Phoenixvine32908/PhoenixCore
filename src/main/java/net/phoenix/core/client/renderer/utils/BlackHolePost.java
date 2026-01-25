package net.phoenix.core.client.renderer.utils;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * One-frame-ish message bus from in-world renderers -> post-processing pass.
 *
 * The renderer calls {@link #setWorld(Vec3, float, float, float, long)} each frame it wants
 * the lens. The post-pass projects it and draws the lensing shader.
 *
 * Includes a tiny "keep alive" to avoid flicker if your DynamicRender isn't invoked for 1-2 frames.
 */
@OnlyIn(Dist.CLIENT)
public final class BlackHolePost {
    public static final BlackHolePost INSTANCE = new BlackHolePost();

    private boolean active = false;

    private Vec3 centerWorld = Vec3.ZERO;

    /** Radius in world blocks used to estimate screen-space radius. (If you want 10-block diameter, use 5.) */
    private float worldRadiusBlocks = 1.0f;

    /** Used if edge projection fails. */
    private float fallbackRadiusUv = 3.03f;

    /** Screen UV projected by the post-pass */
    private float xUv = -1f, yUv = -1f, radiusUv = 0.03f;

    /** Shader strength */
    private float strength = 1.25f;

    /** Keep-alive (ticks) */
    private long lastTouchedTick = Long.MIN_VALUE;

    private BlackHolePost() {}

    public boolean isActive(long currentTick) {
        return active && (currentTick - lastTouchedTick) <= 3;
    }

    /** Clears only the activation flag (world data remains, so keep-alive can work). */
    public void clear() {
        active = false;
    }

    /** Clear projected screen UV (call after rendering the pass). */
    public void clearScreenUv() {
        xUv = -1f;
        yUv = -1f;
    }

    public void setWorld(Vec3 centerWorld, float worldRadiusBlocks, float strength, float fallbackRadiusUv, long currentTick) {
        this.centerWorld = centerWorld;
        this.worldRadiusBlocks = worldRadiusBlocks;
        this.strength = strength;
        this.fallbackRadiusUv = fallbackRadiusUv;
        this.active = true;
        this.lastTouchedTick = currentTick;
    }

    public void setScreenUv(float xUv, float yUv, float radiusUv) {
        this.xUv = xUv;
        this.yUv = yUv;
        this.radiusUv = radiusUv;
    }

    public Vec3 centerWorld() { return centerWorld; }
    public float worldRadiusBlocks() { return worldRadiusBlocks; }
    public float fallbackRadiusUv() { return fallbackRadiusUv; }

    public float xUv() { return xUv; }
    public float yUv() { return yUv; }
    public float radiusUv() { return radiusUv; }

    public float strength() { return strength; }
}
