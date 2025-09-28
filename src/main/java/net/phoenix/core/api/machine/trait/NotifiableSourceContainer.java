package net.phoenix.core.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import net.phoenix.core.api.capability.SourceRecipeCapability;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class NotifiableSourceContainer extends NotifiableRecipeHandlerTrait<Integer> implements ISourceTile {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NotifiableSourceContainer.class,
            NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private int currentSource;

    @Persisted
    private int maxSource;

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
            // GT recipe wants to consume -> remove from container
            int extracted = Math.min(remaining, currentSource);
            if (!simulate) removeSource(extracted);
            remaining -= extracted;
        } else if (io == IO.OUT) {
            // GT recipe outputs -> insert into container
            int inserted = Math.min(remaining, maxSource - currentSource);
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

    // Ars Nouveau bridge
    @Override
    public int getTransferRate() {
        return maxConsumption;
    }

    @Override
    public boolean canAcceptSource() {
        // Corrected: The container can always accept source if it has space
        return currentSource < maxSource;
    }

    @Override
    public int getSource() {
        return currentSource;
    }

    @Override
    public int getMaxSource() {
        return maxSource;
    }

    @Override
    public void setMaxSource(int max) {
        this.maxSource = max;
    }

    @Override
    public int setSource(int source) {
        this.currentSource = Math.min(source, maxSource);
        notifyListeners();
        return this.currentSource;
    }

    @Override
    public int addSource(int amount) {
        // Corrected: Remove the check for handlerIO
        int inserted = Math.min(amount, maxSource - currentSource);
        currentSource += inserted;
        notifyListeners();
        return inserted;
    }

    @Override
    public int removeSource(int amount) {
        // Corrected: Remove the check for handlerIO
        int extracted = Math.min(amount, currentSource);
        currentSource -= extracted;
        notifyListeners();
        return extracted;
    }
}