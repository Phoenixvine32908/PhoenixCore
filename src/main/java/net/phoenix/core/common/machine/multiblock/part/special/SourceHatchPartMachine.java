package net.phoenix.core.common.machine.multiblock.part.special;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.entity.player.Player;
import net.phoenix.core.api.capability.ISourceProviderCapability;
import net.phoenix.core.api.machine.trait.NotifiableSourceContainer;
import net.phoenix.core.client.renderer.gui.SourceHatchFancyUIWidget;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class SourceHatchPartMachine extends TieredIOPartMachine implements ISourceProviderCapability {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SourceHatchPartMachine.class,
            TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    private final IO io;

    @Persisted
    @DescSynced
    private final NotifiableSourceContainer sourceContainer;

    public SourceHatchPartMachine(IMachineBlockEntity holder, int tier, IO io) {
        super(holder, tier, io);
        this.io = io;
        this.sourceContainer = new NotifiableSourceContainer(
                this, io, getMaxCapacity(tier), getMaxConsumption(tier));
    }

    @Override
    public ISourceTile getSource() {
        return sourceContainer;
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public ModularUI createUI(Player player) {
        int w = 176;
        int h = 166;
        return new ModularUI(w, h, this, player)
                .widget(new SourceHatchFancyUIWidget(this, w, h));
    }

    public static int getMaxCapacity(int tier) {
        return 1000 * tier;
    }

    public static int getMaxConsumption(int tier) {
        return 250 * tier;
    }
}
