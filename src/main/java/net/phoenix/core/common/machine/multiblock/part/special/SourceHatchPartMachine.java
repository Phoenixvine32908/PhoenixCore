package net.phoenix.core.common.machine.multiblock.part.special;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;

import net.phoenix.core.api.machine.trait.NotifiableSourceContainer;

import lombok.Getter;

@Getter
public class SourceHatchPartMachine extends TieredIOPartMachine {

    private final NotifiableSourceContainer sourceContainer;

    public SourceHatchPartMachine(IMachineBlockEntity holder, int tier, IO io) {
        super(holder, tier, io);
        this.sourceContainer = new NotifiableSourceContainer(this, io, 5000);
    }
}
