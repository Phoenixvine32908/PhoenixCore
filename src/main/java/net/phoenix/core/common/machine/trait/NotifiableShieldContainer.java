package net.phoenix.core.common.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import net.phoenix.core.api.capability.PhoenixRecipeCapabilities;
import net.phoenix.core.common.machine.multiblock.Shield.ShieldTypes; // <-- Import the enum
import net.phoenix.core.common.machine.multiblock.electric.HighPressurePlasmaArcFurnaceMachine;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// FIX HERE: Use the inner enum ShieldTypes as the generic type
public class NotifiableShieldContainer extends NotifiableRecipeHandlerTrait<ShieldTypes> {
    public NotifiableShieldContainer(MetaMachine machine) {
        super(machine);
    }

    // FIX HERE: Change return type
    public ShieldTypes getHeldShield() {
        if (!(getMachine() instanceof HighPressurePlasmaArcFurnaceMachine furnace)) {
            throw new IllegalStateException();
        }
        // Assuming HighPressurePlasmaArcFurnaceMachine's getter is now correct (returning ShieldTypes)
        return furnace.getShieldType();
    }

    @Override
    public IO getHandlerIO() {
        return IO.IN;
    }

    // FIX HERE: Change List generic type
    @Override
    public List<ShieldTypes> handleRecipeInner(IO io, GTRecipe recipe, List<ShieldTypes> left, boolean simulate) {
        ShieldTypes recipeShieldType = left.get(0); // FIX: Change variable type

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