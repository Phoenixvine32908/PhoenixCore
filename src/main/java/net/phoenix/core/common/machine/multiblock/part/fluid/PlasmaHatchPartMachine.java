package net.phoenix.core.common.machine.multiblock.part.fluid;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PlasmaHatchPartMachine extends FluidHatchPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            PlasmaHatchPartMachine.class,
            FluidHatchPartMachine.MANAGED_FIELD_HOLDER);

    public PlasmaHatchPartMachine(IMachineBlockEntity holder, int tier, IO io, int initialCapacity, int slots) {
        super(holder, tier, io, initialCapacity, slots);
    }

    @NotNull
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @NotNull
    @Override
    protected NotifiableFluidTank createTank(int initialCapacity, int slots, Object... args) {
        return super.createTank(initialCapacity, slots, args)
                .setFilter(fluidStack -> fluidStack.getFluid().is(CustomTags.PLASMA_FLUIDS));
    }
}
