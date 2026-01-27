package net.phoenix.core.client.renderer;

import net.minecraft.client.renderer.ShaderInstance;

import org.jetbrains.annotations.Nullable;

public final class PhoenixShaders {

    private static @Nullable ShaderInstance BLACK_HOLE;
    private static @Nullable ShaderInstance BLENDER_MATERIAL;

    private PhoenixShaders() {}

    public static @Nullable ShaderInstance getBlackHoleShader() {
        return BLACK_HOLE;
    }

    public static @Nullable ShaderInstance getBlenderShader() {
        return BLENDER_MATERIAL;
    }

    public static void setBlackHoleShader(@Nullable ShaderInstance shader) {
        if (BLACK_HOLE != null) BLACK_HOLE.close();
        BLACK_HOLE = shader;
    }

    public static void setBlenderShader(@Nullable ShaderInstance shader) {
        if (BLENDER_MATERIAL != null) BLENDER_MATERIAL.close();
        BLENDER_MATERIAL = shader;
    }

    /** Call on client shutdown. */
    public static void closeAll() {
        if (BLACK_HOLE != null) BLACK_HOLE.close();
        if (BLENDER_MATERIAL != null) BLENDER_MATERIAL.close();
        BLACK_HOLE = null;
        BLENDER_MATERIAL = null;
    }
}
