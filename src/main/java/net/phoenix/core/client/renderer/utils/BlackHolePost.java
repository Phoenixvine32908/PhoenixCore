package net.phoenix.core.client.renderer.utils;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BlackHolePost {

    public static final BlackHolePost INSTANCE = new BlackHolePost();

    private boolean active = false;
    private Vec3 centerWorld = Vec3.ZERO;
    private float worldRadiusBlocks = 1.0f;
    private float fallbackRadiusUv = 0.03f;
    private float xUv = -1f, yUv = -1f, radiusUv = 0.03f;
    private float strength = 1.25f;
    private long lastTouchedTick = Long.MIN_VALUE;

    private BlackHolePost() {}

    public boolean isActive(long currentTick) {
        return active && (currentTick - lastTouchedTick) <= 3;
    }

    public void clear() {
        active = false;
    }

    public void clearScreenUv() {
        xUv = -1f;
        yUv = -1f;
    }

    public void setWorld(Vec3 centerWorld, float worldRadiusBlocks, float strength, float fallbackRadiusUv,
                         long currentTick) {
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

    public Vec3 centerWorld() {
        return centerWorld;
    }

    public float worldRadiusBlocks() {
        return worldRadiusBlocks;
    }

    public float fallbackRadiusUv() {
        return fallbackRadiusUv;
    }

    public float xUv() {
        return xUv;
    }

    public float yUv() {
        return yUv;
    }

    public float radiusUv() {
        return radiusUv;
    }

    public float strength() {
        return strength;
    }
}
