package net.phoenix.core.common.machine.multiblock.part;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum ShieldRenderProperty implements StringRepresentable {

    NORMAL("normal"),
    INACTIVE("inactive"),
    DECAYED("decayed");

    private final String name;

    ShieldRenderProperty(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static final EnumProperty<ShieldRenderProperty> TYPE = EnumProperty.create("shield_state",
            ShieldRenderProperty.class);
}
