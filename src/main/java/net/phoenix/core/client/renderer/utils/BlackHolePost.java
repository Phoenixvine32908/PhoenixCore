package net.phoenix.core.client.renderer.utils;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BlackHolePost {
    public static final BlackHolePost INSTANCE = new BlackHolePost();

    private boolean active = false;

    private Vec3 centerWorld = Vec3.ZERO;
    private float worldRadiusBlocks = 0.15f; // small by default
    private float fallbackRadiusUv = 0.02f;

    private float xUv = -1f, yUv = -1f;
    private float radiusUv = 0.02f;

    private float strength = 1.35f;

    private BlackHolePost() {}

    public boolean isActive() { return active; }
    public void clear() { active = false; }

    public void setWorld(Vec3 centerWorld, float worldRadiusBlocks, float strength, float fallbackRadiusUv) {
        this.centerWorld = centerWorld;
        this.worldRadiusBlocks = worldRadiusBlocks;
        this.strength = strength;
        this.fallbackRadiusUv = fallbackRadiusUv;
        this.active = true;
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
