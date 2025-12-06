//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.core.components;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public interface IReactorComponent {
    Block block();

    Type getComponentType();

    public static enum Type implements StringRepresentable {
        HEAT("heat"),
        EFFICIENCY("efficiency"),
        THROTTLE("throttle");

        public final String name;

        private Type(String name) {
            this.name = name;
        }

        public @NotNull String m_7912_() {
            return this.name;
        }
    }
}
