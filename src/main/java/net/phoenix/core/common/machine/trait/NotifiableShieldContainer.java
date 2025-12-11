package net.phoenix.core.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.phoenix.core.api.capability.PhoenixRecipeCapabilities;
import net.phoenix.core.common.machine.multiblock.Shield.ShieldTypes;
import net.phoenix.core.common.machine.multiblock.electric.HighPressurePlasmaArcFurnaceMachine;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NotifiableShieldContainer extends NotifiableRecipeHandlerTrait<ShieldTypes> {

    public NotifiableShieldContainer(MetaMachine machine) {
        super(machine);
    }

    public ShieldTypes getHeldShield() {
        if (!(getMachine() instanceof HighPressurePlasmaArcFurnaceMachine furnace)) {
            throw new IllegalStateException();
        }
        return furnace.getShieldType();
    }

    @Override
    public IO getHandlerIO() {
        return IO.IN;
    }

    @Override
    public List<ShieldTypes> handleRecipeInner(IO io, GTRecipe recipe, List<ShieldTypes> left, boolean simulate) {
        ShieldTypes recipeShieldType = left.get(0);

        if (getHeldShield() == recipeShieldType) {
            return null;
        }
        return left;
    }

    @Override
    public @NotNull List<Object> getContents() {
        return List.of(getHeldShield());
    }

    @Override
    public double getTotalContentAmount() {
        return 1;
    }

    @Override
    public RecipeCapability<ShieldTypes> getCapability() {
        return PhoenixRecipeCapabilities.SHIELDTYPES;
    }
}
