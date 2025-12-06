//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.compat;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import fi.dea.mc.deafission.common.data.machine.FissionReactorMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

class FissionCcReactorPeripheral$Provider implements IPeripheralProvider {
    private FissionCcReactorPeripheral$Provider() {
    }

    public LazyOptional<IPeripheral> getPeripheral(Level world, BlockPos pos, Direction side) {
        BlockEntity block = world.m_7702_(pos);
        if (block instanceof MetaMachineBlockEntity meta) {
            MetaMachine var7 = meta.getMetaMachine();
            if (var7 instanceof FissionReactorMachine reactor) {
                return LazyOptional.of(() -> new FissionCcReactorPeripheral(reactor));
            } else {
                return LazyOptional.empty();
            }
        } else {
            return LazyOptional.empty();
        }
    }
}
