package net.phoenix.core.common.blockentity;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;

import net.phoenix.core.api.machine.trait.NotifiableSourceContainer;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import com.hollingsworth.arsnouveau.api.source.SourceManager;
import com.hollingsworth.arsnouveau.api.source.SourceProvider;

public class SourceHatchBlockEntity extends TieredIOPartMachine implements ISourceTile {

    private final NotifiableSourceContainer sourceContainer;

    public SourceHatchBlockEntity(IMachineBlockEntity holder, int tier, IO io) {
        super(holder, tier, io);
        this.sourceContainer = new NotifiableSourceContainer(this, io, getMaxCapacity(tier), getMaxConsumption(tier));
    }

    // --- Ars Nouveau ISourceTile forwarding ---
    @Override
    public int getTransferRate() {
        return sourceContainer.getTransferRate();
    }

    @Override
    public boolean canAcceptSource() {
        return sourceContainer.canAcceptSource();
    }

    @Override
    public int getSource() {
        return sourceContainer.getSource();
    }

    @Override
    public int getMaxSource() {
        return sourceContainer.getMaxSource();
    }

    @Override
    public void setMaxSource(int max) {
        sourceContainer.setMaxSource(max);
    }

    @Override
    public int setSource(int source) {
        return sourceContainer.setSource(source);
    }

    @Override
    public int addSource(int source) {
        return sourceContainer.addSource(source);
    }

    @Override
    public int removeSource(int source) {
        return sourceContainer.removeSource(source);
    }

    // --- Hook into Ars SourceManager ---
    @Override
    public void onLoad() {
        super.onLoad();
        if (!this.getLevel().isClientSide) {
            SourceManager.INSTANCE.addInterface(
                    this.getLevel(),
                    new SourceProvider(this, this.getPos()) // wrap this tile
            );
        }
    }

    // --- Helpers ---
    public static int getMaxCapacity(int tier) {
        return 1000 * tier;
    }

    public static int getMaxConsumption(int tier) {
        return 250 * tier;
    }
}
