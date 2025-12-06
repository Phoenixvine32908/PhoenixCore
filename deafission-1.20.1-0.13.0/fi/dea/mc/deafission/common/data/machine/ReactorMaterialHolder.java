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
import fi.dea.mc.deafission.rendering.ItemCountProperty;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ReactorMaterialHolder extends MultiblockPartMachine implements IMachineLife {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER;
    @Persisted
    private final ObjectHolderHandler heldItems = new ObjectHolderHandler(this);
    @Persisted
    private double _progress = (double)0.0F;

    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public void setProgress(double value) {
        this._progress = value;
    }

    public double getProgress() {
        return this._progress;
    }

    public ReactorMaterialHolder(IMachineBlockEntity holder) {
        super(holder);
        this.updateRenderItemCount(this.heldItems.getStackInSlot(0));
    }

    public ItemStack getHeldItem() {
        return this.heldItems.getStackInSlot(0);
    }

    public void setHeldItem(ItemStack heldItem) {
        this.heldItems.setStackInSlot(0, heldItem);
        this._progress = (double)0.0F;
    }

    public void onMachineRemoved() {
        this.clearInventory(this.heldItems.storage);
    }

    private void updateRenderItemCount(@Nullable ItemStack item) {
        int rods = item != null && !item.m_41619_() ? 1 : 0;
        this.setRenderState((MachineRenderState)this.getRenderState().m_61124_(ItemCountProperty.PROPERTY, rods));
    }

    static {
        MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ReactorMaterialHolder.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);
    }

    private class ObjectHolderHandler extends NotifiableItemStackHandler {
        public ObjectHolderHandler(MetaMachine metaTileEntity) {
            super(metaTileEntity, 1, IO.NONE, IO.NONE, (size) -> new CustomItemStackHandler(size) {
                    public int getSlotLimit(int slot) {
                        return 1;
                    }
                });
        }

        public int getSlotLimit(int slot) {
            return 1;
        }

        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }

        public void onContentsChanged() {
            super.onContentsChanged();
            ReactorMaterialHolder.this.updateRenderItemCount(this.getStackInSlot(0));
        }
    }
}
