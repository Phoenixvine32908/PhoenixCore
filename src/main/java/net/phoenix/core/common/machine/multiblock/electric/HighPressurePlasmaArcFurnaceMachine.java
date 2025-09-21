package net.phoenix.core.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * High-Pressure Plasma Arc Furnace:
 * - Runs normally without plasma.
 * - If supported plasma is present, consumes it as a catalyst
 *   and boosts recipe duration/EUt accordingly.
 */
public class HighPressurePlasmaArcFurnaceMachine extends WorkableElectricMultiblockMachine {

    /**
     * @param name for GUI display
     */ // --- Boost config class ---
        private record PlasmaBoost(String name, double durationMultiplier, double eutMultiplier, int consumeAmount) {
    }

    // --- Plasma registry (add more as you like) ---
    private static final Map<net.minecraft.world.level.material.Fluid, PlasmaBoost> PLASMA_BOOSTS = new HashMap<>();
    static {
        PLASMA_BOOSTS.put(GTMaterials.Argon.getFluid(FluidStorageKeys.PLASMA), new PlasmaBoost("Helium Plasma", 0.9, 0.8, 100));
        PLASMA_BOOSTS.put(GTMaterials.Neon.getFluid(FluidStorageKeys.PLASMA),  new PlasmaBoost("Nickel Plasma", 0.7, 0.85, 150));
        PLASMA_BOOSTS.put(GTMaterials.Xenon.getFluid(FluidStorageKeys.PLASMA), new PlasmaBoost("Iron Plasma", 0.6, 0.9, 200));
    }

    // --- Tracks last plasma used for GUI display ---
    @Nullable
    private PlasmaBoost lastPlasmaBoost = null;

    public HighPressurePlasmaArcFurnaceMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    /**
     * Check machine tanks for plasma, consume it if available.
     *
     * @return PlasmaBoost used, or null if none consumed
     */
    @Nullable
    private PlasmaBoost tryConsumePlasma() {
        var handlers = this.getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);

        for (var handler : handlers) {
            if (handler instanceof NotifiableFluidTank tank) {
                for (int i = 0; i < tank.getTanks(); i++) {
                    FluidStack fs = tank.getFluidInTank(i);
                    if (!fs.isEmpty() && PLASMA_BOOSTS.containsKey(fs.getFluid())) {
                        PlasmaBoost boost = PLASMA_BOOSTS.get(fs.getFluid());
                        var drainedSim = tank.drainInternal(boost.consumeAmount, IFluidHandler.FluidAction.SIMULATE);
                        if (drainedSim.getAmount() >= boost.consumeAmount) {
                            tank.drainInternal(boost.consumeAmount, IFluidHandler.FluidAction.EXECUTE);
                            lastPlasmaBoost = boost;
                            return boost;
                        }
                    }
                }
            }
        }
        lastPlasmaBoost = null;
        return null;
    }

    /**
     * Recipe modifier — applies boost if plasma is consumed.
     */
    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof HighPressurePlasmaArcFurnaceMachine furnace)) {
            return RecipeModifier.nullWrongType(HighPressurePlasmaArcFurnaceMachine.class, machine);
        }

        PlasmaBoost boost = furnace.tryConsumePlasma();
        if (boost != null) {
            return ModifierFunction.builder()
                    .durationMultiplier(boost.durationMultiplier)
                    .eutMultiplier(boost.eutMultiplier)
                    .build();
        }

        return ModifierFunction.NULL;
    }

    @Override
    public boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        // Prevent auto-voiding of plasma outputs
        return capability != FluidRecipeCapability.CAP;
    }

    // --- GUI hook ---
    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            if (lastPlasmaBoost != null) {
                textList.add(Component.literal("§b" + lastPlasmaBoost.name + " Boost Active!§r"));
                textList.add(Component.literal(" - " + (int)(lastPlasmaBoost.durationMultiplier * 100) + "% duration"));
                textList.add(Component.literal(" - " + (int)(lastPlasmaBoost.eutMultiplier * 100) + "% EUt"));
                textList.add(Component.literal(" - " + lastPlasmaBoost.consumeAmount + " mB consumed per tick"));
            } else {
                textList.add(Component.literal("§7No Plasma Catalyst§r"));
            }
        }
    }
}


/*

public class HighPressurePlasmaArcFurnace extends WorkableElectricMultiblockMachine {

    private static final Map<String, PlasmaBoost> BOOSTS = new HashMap<>();
    private PlasmaBoost activeBoost = null;

    static {
        // name, durationMult, eutMult, inputMult, outputMult, consumeAmount
        BOOSTS.put("Argon", new PlasmaBoost("Argon Plasma", 0.8, 0.8, 1.0, 1.2, 100));
        BOOSTS.put("Neon",  new PlasmaBoost("Neon Plasma", 0.7, 1.0, 0.9, 1.5, 50));
        BOOSTS.put("Xenon", new PlasmaBoost("Xenon Plasma", 0.6, 0.9, 0.8, 2.0, 25));
    }

    public HighPressurePlasmaArcFurnace(IMachineBlockEntity holder) {
        super(holder);
    }


private PlasmaBoost tryConsumeCatalyst() {
    var fluidHandlers = getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
    for (var handler : fluidHandlers) {
        if (!(handler instanceof NotifiableFluidTank tank)) continue;

        for (int i = 0; i < tank.getTanks(); i++) {
            var fs = tank.getFluidInTank(i);
            if (fs.isEmpty()) continue;

            if (fs.getFluid().equals(GTMaterials.Argon.getFluid(FluidStorageKeys.PLASMA))) {
                if (fs.getAmount() >= BOOSTS.get("Argon").consumeAmount) {
                    tank.drainInternal(BOOSTS.get("Argon").consumeAmount, true);
                    return BOOSTS.get("Argon");
                }
            }
            if (fs.getFluid().equals(GTMaterials.Neon.getFluid(FluidStorageKeys.PLASMA))) {
                if (fs.getAmount() >= BOOSTS.get("Neon").consumeAmount) {
                    tank.drainInternal(BOOSTS.get("Neon").consumeAmount, true);
                    return BOOSTS.get("Neon");
                }
            }
            if (fs.getFluid().equals(GTMaterials.Xenon.getFluid(FluidStorageKeys.PLASMA))) {
                if (fs.getAmount() >= BOOSTS.get("Xenon").consumeAmount) {
                    tank.drainInternal(BOOSTS.get("Xenon").consumeAmount, true);
                    return BOOSTS.get("Xenon");
                }
            }
        }
    }
    return null;
}


  Recipe modifier — boosts recipes based on catalyst present.

public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
    if (!(machine instanceof HighPressurePlasmaArcFurnace furnace)) {
        return RecipeModifier.nullWrongType(HighPressurePlasmaArcFurnace.class, machine);
    }

    PlasmaBoost boost = furnace.tryConsumeCatalyst();
    if (boost == null) {
        furnace.activeBoost = null;
        return ModifierFunction.NULL;
    }

    furnace.activeBoost = boost;

    return ModifierFunction.builder()
            .durationMultiplier(boost.durationMultiplier)
            .eutMultiplier(boost.eutMultiplier)
            .inputModifier(ContentModifier.multiplier(boost.inputMultiplier))
            .outputModifier(ContentModifier.multiplier(boost.outputMultiplier))
            .build();
}

@Override
public void addDisplayText(List<Component> textList) {
    super.addDisplayText(textList);
    if (isFormed()) {
        if (activeBoost != null) {
            textList.add(Component.literal("Catalyst: ")
                    .append(Component.literal(activeBoost.name).withStyle(ChatFormatting.AQUA)));
            textList.add(Component.literal(" - Duration x" + FormattingUtil.formatNumbers(activeBoost.durationMultiplier)));
            textList.add(Component.literal(" - EUt x" + FormattingUtil.formatNumbers(activeBoost.eutMultiplier)));
            textList.add(Component.literal(" - Inputs x" + FormattingUtil.formatNumbers(activeBoost.inputMultiplier)));
            textList.add(Component.literal(" - Outputs x" + FormattingUtil.formatNumbers(activeBoost.outputMultiplier)));
        } else {
            textList.add(Component.literal("No catalyst active").withStyle(ChatFormatting.GRAY));
        }
    }
}

private static final class PlasmaBoost {
    final String name;
    final double durationMultiplier;
    final double eutMultiplier;
    final double inputMultiplier;
    final double outputMultiplier;
    final int consumeAmount;

    PlasmaBoost(String name, double durationMultiplier, double eutMultiplier,
                double inputMultiplier, double outputMultiplier, int consumeAmount) {
        this.name = name;
        this.durationMultiplier = durationMultiplier;
        this.eutMultiplier = eutMultiplier;
        this.inputMultiplier = inputMultiplier;
        this.outputMultiplier = outputMultiplier;
        this.consumeAmount = consumeAmount;
    }
}
}
 */
