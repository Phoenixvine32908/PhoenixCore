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

public enum FissionReactorMachine$OperatingMode implements StringRepresentable {
    DEFAULT("default"),
    COOLDOWN("cooldown"),
    INCOMPLETE("incomplete");

    private final String name;
    private static final Map<String, FissionReactorMachine$OperatingMode> s_lookup = (Map)Stream.of(values()).collect(Collectors.toMap(FissionReactorMachine$OperatingMode::m_7912_, (e) -> e));

    private FissionReactorMachine$OperatingMode(String name) {
        this.name = name;
    }

    public @NotNull String m_7912_() {
        return this.name;
    }

    public static FissionReactorMachine$OperatingMode fromSerializedName(String name) {
        return (FissionReactorMachine$OperatingMode)s_lookup.getOrDefault(name, DEFAULT);
    }
}
