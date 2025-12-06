//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.machine;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ReactorRedstonePort$Type implements StringRepresentable {
    HEAT("heat"),
    FUELS("rods");

    private final String name;
    private static final Map<String, ReactorRedstonePort$Type> s_lookup = (Map)Stream.of(values()).collect(Collectors.toMap(ReactorRedstonePort$Type::m_7912_, (e) -> e));

    private ReactorRedstonePort$Type(String name) {
        this.name = name;
    }

    public @NotNull String m_7912_() {
        return this.name;
    }

    public static ReactorRedstonePort$Type fromSerializedName(String name) {
        return (ReactorRedstonePort$Type)s_lookup.getOrDefault(name, HEAT);
    }
}
