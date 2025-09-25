package net.phoenix.core.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.phoenix.core.api.capability.SourceRecipeCapability;

import org.jetbrains.annotations.NotNull;
import org.openjdk.nashorn.internal.objects.annotations.Getter;

import java.util.Collections;
import java.util.List;

public class NotifiableSourceContainer extends NotifiableRecipeHandlerTrait<Integer> {

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
}
