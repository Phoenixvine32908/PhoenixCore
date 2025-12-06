//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import net.minecraft.world.item.ItemStack;

class ReactorMaterialHolder$ObjectHolderHandler extends NotifiableItemStackHandler {
    public ReactorMaterialHolder$ObjectHolderHandler(ReactorMaterialHolder var1, MetaMachine metaTileEntity) {
        super(metaTileEntity, 1, IO.NONE, IO.NONE, (size) -> new CustomItemStackHandler(size) {
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
        return true;
    }

    public void onContentsChanged() {
        super.onContentsChanged();
        this.this$0.updateRenderItemCount(this.getStackInSlot(0));
    }
}
