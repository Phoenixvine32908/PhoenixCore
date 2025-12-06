//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.core.components;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum IReactorComponent$Type implements StringRepresentable {
    HEAT("heat"),
    EFFICIENCY("efficiency"),
    THROTTLE("throttle");

    public final String name;

    private IReactorComponent$Type(String name) {
        this.name = name;
    }

    public @NotNull String m_7912_() {
        return this.name;
    }
}
