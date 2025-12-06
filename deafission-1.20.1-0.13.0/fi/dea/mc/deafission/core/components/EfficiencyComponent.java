//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.core.components;

import fi.dea.mc.deafission.core.components.IReactorComponent.Type;
import net.minecraft.world.level.block.Block;

public record EfficiencyComponent(Block block, double value) implements IReactorComponent {
    public IReactorComponent.Type getComponentType() {
        return Type.EFFICIENCY;
    }
}
