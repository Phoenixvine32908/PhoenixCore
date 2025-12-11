// GTCEu MetaMachine for source hatches
package net.phoenix.core.common.machine.multiblock.part.special;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.phoenix.core.api.machine.trait.NotifiableSourceContainer;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import com.hollingsworth.arsnouveau.api.source.ISpecialSourceProvider;
import com.hollingsworth.arsnouveau.api.source.SourceManager;
import lombok.Getter;

public class SourceHatchPartMachine extends MetaMachine implements ISpecialSourceProvider {

    @Getter
    private final ISourceTile sourceContainer;
    private final BlockPos pos;
    private final Level level;
    private final MetaMachineBlockEntity tileEntity;

    private static final int MAX_CAPACITY = 10000;
    private static final int MAX_CONSUMPTION = 100;
    private static final IO IO_DIRECTION = IO.OUT;

    public SourceHatchPartMachine(MetaMachineBlockEntity tileEntity, int tier, IO io) {
        super(tileEntity);
        this.sourceContainer = new NotifiableSourceContainer(this, io, getMaxCapacity(tier), getMaxConsumption(tier));
        this.pos = tileEntity.getBlockPos();
        this.level = tileEntity.getLevel();
        this.tileEntity = tileEntity;
    }

    public static int getMaxCapacity(int tier) {
        return (int) (GTValues.V[tier] * 100);
    }

    public static int getMaxConsumption(int tier) {
        return (int) (GTValues.V[tier] * 10);
    }

    @Override
    public ISourceTile getSource() {
        return this.sourceContainer;
    }

    @Override
    public boolean isValid() {
        return !tileEntity.isRemoved() && level.getBlockEntity(pos) == tileEntity;
    }

    @Override
    public BlockPos getCurrentPos() {
        return this.pos;
    }

    public void onBlockPlacedInWorld() {
        if (!this.level.isClientSide) {
            SourceManager.INSTANCE.addInterface(this.level, this);
        }
    }
}
