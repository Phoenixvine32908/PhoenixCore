package net.phoenix.core.common.machine.multiblock.part.special;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import lombok.Getter;
import net.phoenix.core.api.capability.ISourceProviderCapability;
import net.phoenix.core.api.machine.trait.NotifiableSourceContainer;

public class SourceHatchPartMachine extends TieredIOPartMachine
        implements ISourceProviderCapability {

    @Getter
    private final IO io;

    private final ISourceTile sourceContainer;

    public SourceHatchPartMachine(IMachineBlockEntity holder, int tier, IO io) {
        super(holder, tier, io);
        this.io = io;
        this.sourceContainer = new NotifiableSourceContainer(
                this, io, getMaxCapacity(tier), getMaxConsumption(tier)
        );
    }

    @Override
    public ISourceTile getSource() {
        return sourceContainer;
    }

    public static int getMaxCapacity(int tier) {
        return 1000 * tier;
    }

    public static int getMaxConsumption(int tier) {
        return 250 * tier;
    }
}
