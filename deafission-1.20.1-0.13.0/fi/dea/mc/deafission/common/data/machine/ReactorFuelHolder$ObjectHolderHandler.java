//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import fi.dea.mc.deafission.common.data.items.FuelCellItem;
import net.minecraft.world.item.ItemStack;

class ReactorFuelHolder$ObjectHolderHandler extends NotifiableItemStackHandler {
    public ReactorFuelHolder$ObjectHolderHandler(ReactorFuelHolder var1, MetaMachine metaTileEntity) {
        super(metaTileEntity, 1, IO.NONE, IO.BOTH, (size) -> new CustomItemStackHandler(size) {
                public int getSlotLimit(int slot) {
                    return 1;
                }
            });
        this.this$0 = var1;
    }

    public int getSlotLimit(int slot) {
        return 1;
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return stack.m_41619_() ? true : stack.m_41720_() instanceof FuelCellItem;
    }

    public void onContentsChanged() {
        super.onContentsChanged();
        this.this$0.updateRenderRodCount(this.getStackInSlot(0));
    }
}
