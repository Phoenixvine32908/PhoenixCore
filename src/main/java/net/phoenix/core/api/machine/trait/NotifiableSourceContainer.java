package net.phoenix.core.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import net.phoenix.core.api.capability.SourceRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import org.jetbrains.annotations.NotNull;
import org.openjdk.nashorn.internal.objects.annotations.Getter;

import java.util.Collections;
import java.util.List;
import com.hollingsworth.arsnouveau.api.source.ISourceTile;


public class NotifiableSourceContainer extends NotifiableRecipeHandlerTrait<Integer> implements ISourceTile {

    private final IO handlerIO;
    private int storedSource;
    private final int maxCapacity;

    public NotifiableSourceContainer(MetaMachine machine, IO io, int maxCapacity) {
        super(machine);
        this.handlerIO = io;
        this.maxCapacity = maxCapacity;
        this.storedSource = 0;
    }

    @Override
    public IO getHandlerIO() {
        return handlerIO;
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, boolean simulate) {
        int amount = left.stream().reduce(0, Integer::sum);
        if (io == IO.IN) {
            // consume from this container
            int canExtract = Math.min(amount, storedSource);
            if (!simulate) {
                storedSource -= canExtract;
                notifyListeners();
            }
            amount -= canExtract;
        } else if (io == IO.OUT) {
            // insert into this container
            int space = maxCapacity - storedSource;
            int toInsert = Math.min(amount, space);
            if (!simulate) {
                storedSource += toInsert;
                notifyListeners();
            }
            amount -= toInsert;
        }
        return amount <= 0 ? null : Collections.singletonList(amount);
    }

    @NotNull
    @Override
    public List<Object> getContents() {
        return Collections.singletonList(storedSource);
    }

    @Override
    public double getTotalContentAmount() {
        return storedSource;
    }

    @Override
    public RecipeCapability<Integer> getCapability() {
        return SourceRecipeCapability.CAP;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Getter
    public int getStoredSource() {
        return storedSource;
    }

    public void setStoredSource(int amount) {
        this.storedSource = Math.min(amount, maxCapacity);
        notifyListeners();
    }

    @Getter
    public int getMaxCapacity() {
        return maxCapacity;
    }

    //
    // Ars Nouveau ISourceTile implementation for 1.20.1
    //

    @Override
    public int getTransferRate() {
        return 1000;
    }

    @Override
    public boolean canAcceptSource() {
        return this.handlerIO == IO.IN;
    }

    public boolean canProvideSource() {
        return this.handlerIO == IO.OUT;
    }

    @Override
    public int getSource() {
        return this.storedSource;
    }

    @Override
    public int getMaxSource() {
        return this.maxCapacity;
    }

    @Override
    public void setMaxSource(int max) {
        // This is a placeholder as maxCapacity is final in your class.
        // You can leave it empty or add logic to update if it were not final.
    }

    @Override
    public int setSource(int source) {
        this.setStoredSource(source);
        return this.storedSource;
    }

    @Override
    public int addSource(int source) {
        if (this.handlerIO != IO.IN || !canAcceptSource()) {
            return 0;
        }
        int inserted = Math.min(source, this.maxCapacity - this.storedSource);
        this.storedSource += inserted;
        this.notifyListeners();
        return inserted;
    }

    @Override
    public int removeSource(int source) {
        if (this.handlerIO != IO.OUT || !canProvideSource()) {
            return 0;
        }
        int extracted = Math.min(source, this.storedSource);
        this.storedSource -= extracted;
        this.notifyListeners();
        return extracted;
    }
}