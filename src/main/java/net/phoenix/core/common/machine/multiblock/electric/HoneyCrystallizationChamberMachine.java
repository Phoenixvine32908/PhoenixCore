package net.phoenix.core.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IFluidRenderMulti;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HoneyCrystallizationChamberMachine extends WorkableElectricMultiblockMachine implements IFluidRenderMulti {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HoneyCrystallizationChamberMachine.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @Setter
    @DescSynced
    @RequireRerender
    private @NotNull Set<BlockPos> fluidBlockOffsets = new HashSet<>();

    // Constant honey fluidstack
    private static final FluidStack HONEY_STACK;
    static {
        Fluid honeyFluid = ForgeRegistries.FLUIDS
                .getValue(ResourceLocation.fromNamespaceAndPath("productivebees", "honey"));
        if (honeyFluid != null) {
            HONEY_STACK = new FluidStack(honeyFluid, 1000); // 1000 mB default
        } else {
            HONEY_STACK = FluidStack.EMPTY; // fallback in case fluid is missing
        }
    }

    public HoneyCrystallizationChamberMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        IFluidRenderMulti.super.onStructureFormed();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        IFluidRenderMulti.super.onStructureInvalid();
    }

    @NotNull
    @Override
    public Set<BlockPos> saveOffsets() {
        // Example: a 3-block tall honey column behind the controller
        Direction up = RelativeDirection.UP.getRelative(getFrontFacing(), getUpwardsFacing(), isFlipped());
        Direction back = getFrontFacing().getOpposite();

        BlockPos pos = getPos();
        BlockPos center = pos.relative(back);

        Set<BlockPos> offsets = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            offsets.add(center.relative(up, i).subtract(pos));
        }
        return offsets;
    }

    /**
     * PlasmaBoost config:
     * - name for display
     * - duration multiplier
     * - EUt multiplier
     * - consumeAmount = how much plasma to consume per interval (mB)
     * - ticksPerConsumption = how many ticks between each consumption
     */
    private record PlasmaBoost(String name, double durationMultiplier, double eutMultiplier, int consumeAmount,
                               int ticksPerConsumption) {}

    // --- Plasma registry with per-plasma configs ---
    private static final Map<Fluid, HoneyCrystallizationChamberMachine.PlasmaBoost> PLASMA_BOOSTS = new HashMap<>();
    static {
        // Here, the amount and ticks are set for a periodic consumption rate
        // Example: 100 mB every 2 seconds (40 ticks)
        PLASMA_BOOSTS.put(GTMaterials.Helium.getFluid(FluidStorageKeys.PLASMA),
                new HoneyCrystallizationChamberMachine.PlasmaBoost("Helium Plasma", 0.9, 0.8, 1, 40));
        // Example: 200 mB every 1 second (20 ticks)
        PLASMA_BOOSTS.put(GTMaterials.Iron.getFluid(FluidStorageKeys.PLASMA),
                new HoneyCrystallizationChamberMachine.PlasmaBoost("Iron Plasma", 0.7, 0.85, 200, 20));
        // Example: 50 mB every 0.5 seconds (10 ticks)
        PLASMA_BOOSTS.put(GTMaterials.Nickel.getFluid(FluidStorageKeys.PLASMA),
                new HoneyCrystallizationChamberMachine.PlasmaBoost("Nickel Plasma", 0.6, 0.9, 50, 10));
    }

    @DescSynced
    private boolean isPlasmaBoosted = false;

    @Nullable
    private HoneyCrystallizationChamberMachine.PlasmaBoost activeBoost = null;

    private int consumptionTimer = 0;

    public HoneyCrystallizationChamberMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    // ------------------------------------------//
    // Recipe Logic //
    // ------------------------------------------//

    private GTRecipe getPlasmaRecipe(HoneyCrystallizationChamberMachine.PlasmaBoost boost,
                                     net.minecraft.world.level.material.Fluid fluid) {
        return GTRecipeBuilder.ofRaw().inputFluids(new FluidStack(fluid, boost.consumeAmount())).buildRawRecipe();
    }

    @Override
    public boolean onWorking() {
        // Only perform the plasma check and consumption on the correct tick.
        if (this.consumptionTimer % (activeBoost == null ? 1 : activeBoost.ticksPerConsumption()) == 0) {

            // On the check tick, reset the boost state
            isPlasmaBoosted = false;
            activeBoost = null;

            for (var entry : PLASMA_BOOSTS.entrySet()) {
                var fluid = entry.getKey();
                var boost = entry.getValue();
                var plasmaRecipe = getPlasmaRecipe(boost, fluid);

                if (RecipeHelper.matchRecipe(this, plasmaRecipe).isSuccess() &&
                        RecipeHelper.handleRecipeIO(this, plasmaRecipe, IO.IN, this.recipeLogic.getChanceCaches())
                                .isSuccess()) {
                    isPlasmaBoosted = true;
                    activeBoost = boost;
                    break;
                }
            }
        }

        // The call to super.onWorking() now uses the current, persistent state
        boolean value = super.onWorking();

        // Increment the timer and reset it to prevent overflow
        this.consumptionTimer++;
        if (this.consumptionTimer > 72000) this.consumptionTimer = 0;

        return value;
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof HoneyCrystallizationChamberMachine furnace)) {
            return RecipeModifier.nullWrongType(HoneyCrystallizationChamberMachine.class, machine);
        }

        if (furnace.isPlasmaBoosted && furnace.activeBoost != null) {
            HoneyCrystallizationChamberMachine.PlasmaBoost boost = furnace.activeBoost;
            return ModifierFunction.builder()
                    .durationMultiplier(boost.durationMultiplier)
                    .eutMultiplier(boost.eutMultiplier)
                    .build();
        }

        return ModifierFunction.IDENTITY;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            if (isPlasmaBoosted && activeBoost != null) {
                textList.add(Component.literal("§b" + activeBoost.name + " Boost Active!§r"));
                textList.add(Component.literal(" - " + (int) (activeBoost.durationMultiplier * 100) + "% duration"));
                textList.add(Component.literal(" - " + (int) (activeBoost.eutMultiplier * 100) + "% EUt"));
                textList.add(Component.literal(
                        " - " + activeBoost.consumeAmount + " mB every " + activeBoost.ticksPerConsumption + " ticks"));
            } else {
                textList.add(Component.literal("§7No Plasma Catalyst§r"));
            }
        }
    }

    // Optional: Override to supply constant honey fluid for rendering
    public List<FluidStack> getRenderFluids() {
        return List.of(HONEY_STACK);
    }
}
