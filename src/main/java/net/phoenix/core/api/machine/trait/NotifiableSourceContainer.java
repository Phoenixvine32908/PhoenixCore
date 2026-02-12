package net.phoenix.core.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.phoenix.core.api.capability.SourceRecipeCapability;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class NotifiableSourceContainer extends NotifiableRecipeHandlerTrait<Integer> implements ISourceTile {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NotifiableSourceContainer.class,
            NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private long currentSource;

    @Persisted
    private long maxSource;

    @Persisted
    private int maxConsumption;

    private final IO handlerIO;

    public NotifiableSourceContainer(MetaMachine machine, IO io, int maxCapacity, int maxConsumption) {
        super(machine);
        this.setMaxSource(maxCapacity);
        this.maxConsumption = maxConsumption;
        this.handlerIO = io;
    }

    @Override
    public IO getHandlerIO() {
        return handlerIO;
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, boolean simulate) {
        int remaining = left.stream().reduce(0, Integer::sum);

        if (io == IO.IN) {
            int extracted = Math.toIntExact(Math.min(remaining, currentSource));
            if (!simulate) removeSource(extracted);
            remaining -= extracted;
        } else if (io == IO.OUT) {
            int inserted = Math.toIntExact(Math.min(remaining, maxSource - currentSource));
            if (!simulate) addSource(inserted);
            remaining -= inserted;
        }

        return remaining <= 0 ? null : Collections.singletonList(remaining);
    }

    @Override
    public @NotNull List<Object> getContents() {
        return List.of(currentSource);
    }

    @Override
    public double getTotalContentAmount() {
        return currentSource;
    }

    @Override
    public RecipeCapability<Integer> getCapability() {
        return SourceRecipeCapability.CAP;
    }

    @Override
    public int getTransferRate() {
        return maxConsumption;
    }

    @Override
    public boolean canAcceptSource() {
        return currentSource < maxSource;
    }

    @Override
    public int getSource() {
        return Math.toIntExact(currentSource);
    }

    @Override
    public int getMaxSource() {
        return Math.toIntExact(maxSource);
    }

    @Override
    public void setMaxSource(int max) {
        this.maxSource = max;
    }

    @Override
    public int setSource(int source) {
        this.currentSource = Math.min(source, maxSource);
        notifyListeners();
        return Math.toIntExact(this.currentSource);
    }

    @Override
    public int addSource(int amount) {
        int inserted = Math.toIntExact(Math.min(amount, maxSource - currentSource));
        currentSource += inserted;
        notifyListeners();
        return inserted;
    }

    @Override
    public int removeSource(int amount) {
        int extracted = Math.toIntExact(Math.min(amount, currentSource));
        currentSource -= extracted;
        notifyListeners();
        return extracted;
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
