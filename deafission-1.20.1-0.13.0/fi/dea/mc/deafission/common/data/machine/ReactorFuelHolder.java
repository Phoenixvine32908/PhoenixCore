//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import fi.dea.mc.deafission.common.data.items.FuelCellItem;
import fi.dea.mc.deafission.rendering.RodCountProperty;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ReactorFuelHolder extends MultiblockPartMachine implements IMachineLife {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER;
    @Persisted
    private final ObjectHolderHandler heldItems = new ObjectHolderHandler(this);

    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public ReactorFuelHolder(IMachineBlockEntity holder) {
        super(holder);
        ItemStack item = this.getHeldItem();
        this.updateRenderRodCount(item);
    }

    public @Nullable ItemStack getHeldItem() {
        ItemStack item = this.heldItems.getStackInSlot(0);
        return !item.m_41619_() && item.m_41720_() instanceof FuelCellItem ? item : null;
    }

    public boolean isEmpty() {
        return this.heldItems.getStackInSlot(0).m_41619_();
    }

    public void setHeldItem(ItemStack heldItem) {
        this.heldItems.setStackInSlot(0, heldItem);
    }

    public void onMachineRemoved() {
        this.clearInventory(this.heldItems.storage);
    }

    private void updateRenderRodCount(@Nullable ItemStack item) {
        int rods = item != null && item.m_41720_() instanceof FuelCellItem ? ((FuelCellItem)item.m_41720_()).getFuelStats(item).rods() : 0;
        this.setRenderState((MachineRenderState)this.getRenderState().m_61124_(RodCountProperty.PROPERTY, rods));
    }

    static {
        MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ReactorFuelHolder.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);
    }

    private class ObjectHolderHandler extends NotifiableItemStackHandler {
        public ObjectHolderHandler(MetaMachine metaTileEntity) {
            super(metaTileEntity, 1, IO.NONE, IO.BOTH, (size) -> new CustomItemStackHandler(size) {
                    public int getSlotLimit(int slot) {
                        return 1;
                    }
                });
        }

        public int getSlotLimit(int slot) {
            return 1;
        }

        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.m_41619_() ? true : stack.m_41720_() instanceof FuelCellItem;
        }

        public void onContentsChanged() {
            super.onContentsChanged();
            ReactorFuelHolder.this.updateRenderRodCount(this.getStackInSlot(0));
        }
    }
}
