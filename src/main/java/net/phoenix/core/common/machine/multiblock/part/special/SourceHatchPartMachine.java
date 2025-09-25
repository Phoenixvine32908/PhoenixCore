package net.phoenix.core.common.machine.multiblock.part.special;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.phoenix.core.api.machine.trait.NotifiableSourceContainer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SourceHatchPartMachine extends MetaMachine {

    public static int getSourceCapacity(int tier, int initialCapacity) {
        if (tier < GTValues.LV) {
            return initialCapacity;
        }
        return (int) (initialCapacity * Math.pow(4, tier - GTValues.LV));
    }

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SourceHatchPartMachine.class,
            MetaMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private final NotifiableSourceContainer sourceContainer;

    public SourceHatchPartMachine(IMachineBlockEntity holder, IO io, int capacity) {
        super(holder);
        this.sourceContainer = new NotifiableSourceContainer(this, io, capacity);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public NotifiableSourceContainer getSourceContainer() {
        return this.sourceContainer;
    }
}
