package net.phoenix.core.common.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.phoenix.core.api.block.IFissionCoolerType;
import net.phoenix.core.api.block.IFissionModeratorType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
public class FissionSteamMultiblockMachine extends WorkableElectricMultiblockMachine
                                           implements IExplosionMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            FissionSteamMultiblockMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private int meltdownTimerTicks = -1;
    @Persisted
    private int meltdownTimerMax = 0;

    @Persisted
    private List<IFissionCoolerType> activeCoolers = new ArrayList<>();
    @Persisted
    private List<IFissionModeratorType> activeModerators = new ArrayList<>();

    @Nullable
    private transient IFissionCoolerType primaryCoolerType = null;
    @Nullable
    private transient IFissionModeratorType primaryModeratorType = null;

    public int lastRequiredCooling = 0;
    public int lastProvidedCooling = 0;
    public boolean lastHasCoolant = true;

    public FissionSteamMultiblockMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        // Retrieve the lists
        @SuppressWarnings("unchecked")
        List<IFissionCoolerType> coolers = (List<IFissionCoolerType>) getMultiblockState().getMatchContext()
                .get("CoolerTypes");
        this.activeCoolers = coolers == null ? new ArrayList<>() : coolers;

        @SuppressWarnings("unchecked")
        List<IFissionModeratorType> moderators = (List<IFissionModeratorType>) getMultiblockState().getMatchContext()
                .get("ModeratorTypes");
        this.activeModerators = moderators == null ? new ArrayList<>() : moderators;

        // Determine the Primary Components (FIXED: Calls specialized methods)
        this.primaryCoolerType = getPrimaryCooler(this.activeCoolers);
        this.primaryModeratorType = getPrimaryModerator(this.activeModerators);

        meltdownTimerTicks = -1;
        meltdownTimerMax = 0;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();

        activeCoolers.clear();
        activeModerators.clear();
        this.primaryCoolerType = null;
        this.primaryModeratorType = null;

        meltdownTimerTicks = -1;
        meltdownTimerMax = 0;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (recipe == null) return false;

        if (recipe.data.contains("required_cooling")) {
            lastRequiredCooling = recipe.data.getInt("required_cooling");
        } else {
            lastRequiredCooling = 0;
        }

        return super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        boolean ok = super.onWorking();
        if (!ok) return false;

        GTRecipe currentRecipe = recipeLogic.getLastRecipe();
        if (currentRecipe == null) return true;

        lastRequiredCooling = currentRecipe.data.contains("required_cooling") ?
                currentRecipe.data.getInt("required_cooling") : 0;

        // Cooling is ADDITIVE (Sum all)
        lastProvidedCooling = this.activeCoolers.stream()
                .mapToInt(IFissionCoolerType::getCoolerTemperature)
                .sum();

        // Coolant Needed is NON-ADDITIVE (Use Primary Cooler's rate)
        int coolantNeededPerTick = 0;
        if (this.primaryCoolerType != null) {
            coolantNeededPerTick = this.primaryCoolerType.getCoolantPerTick();
        }

        lastHasCoolant = tryConsumePrimaryCoolant(this.primaryCoolerType, coolantNeededPerTick);

        int effectiveProvidedCooling = lastHasCoolant ? lastProvidedCooling : 0;
        int deficit = Math.max(0, lastRequiredCooling - effectiveProvidedCooling);
        float deficitPct = lastRequiredCooling == 0 ? 0f : ((float) deficit / (float) lastRequiredCooling);

        handleDangerTiers(deficitPct);

        return true;
    }

    /**
     * Finds the "Primary" Cooler Type based on quantity, then tier.
     */
    private @Nullable IFissionCoolerType getPrimaryCooler(List<IFissionCoolerType> componentList) {
        if (componentList.isEmpty()) {
            return null;
        }

        Map<IFissionCoolerType, Long> counts = componentList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return counts.entrySet().stream()
                .max(Comparator.<Map.Entry<IFissionCoolerType, Long>>comparingLong(Map.Entry::getValue)
                        .thenComparingInt(e -> e.getKey().getTier()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Finds the "Primary" Moderator Type based on quantity, then tier.
     */
    private @Nullable IFissionModeratorType getPrimaryModerator(List<IFissionModeratorType> componentList) {
        if (componentList.isEmpty()) {
            return null;
        }

        Map<IFissionModeratorType, Long> counts = componentList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return counts.entrySet().stream()
                .max(Comparator.<Map.Entry<IFissionModeratorType, Long>>comparingLong(Map.Entry::getValue)
                        .thenComparingInt(e -> e.getKey().getTier()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // Consumes only the coolant required by the primary cooler at its designated rate.
    private boolean tryConsumePrimaryCoolant(@Nullable IFissionCoolerType primaryCooler, int amountMb) {
        if (primaryCooler == null || amountMb <= 0) return true;

        Material requiredMat = primaryCooler.getRequiredCoolantMaterial();
        if (requiredMat == null || requiredMat == GTMaterials.NULL) return true;

        FluidStack required = requiredMat.getFluid(amountMb);
        if (required == null || required.isEmpty()) return true;

        var tanks = getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
        boolean consumedThisType = false;

        for (var handler : tanks) {
            if (!(handler instanceof NotifiableFluidTank tank)) continue;

            for (int i = 0; i < tank.getTanks(); i++) {
                var fluid = tank.getFluidInTank(i);

                if (!fluid.isEmpty() && fluid.getFluid().isSame(required.getFluid())) {
                    int drained = tank.drainInternal(required, IFluidHandler.FluidAction.EXECUTE).getAmount();
                    if (drained >= amountMb) {
                        consumedThisType = true;
                        break;
                    }
                }
            }
            if (consumedThisType) break;
        }
        return consumedThisType;
    }

    private void handleDangerTiers(float deficitPct) {
        if (deficitPct <= 0f) {
            meltdownTimerTicks = -1;
            meltdownTimerMax = 0;
            return;
        }

        if (!lastHasCoolant) {
            int grace = 15; // seconds
            meltdownTimerMax = grace * 20;

            if (meltdownTimerTicks < 0)
                meltdownTimerTicks = meltdownTimerMax;

            meltdownTimerTicks -= 1;

            if (meltdownTimerTicks <= 0)
                doMeltdown();

            return;
        }

        float graceSeconds = 60f - (deficitPct * 50f);
        if (graceSeconds < 10f) graceSeconds = 10f;

        meltdownTimerMax = (int) (graceSeconds * 20);

        if (meltdownTimerTicks < 0)
            meltdownTimerTicks = meltdownTimerMax;

        meltdownTimerTicks -= 1;

        if (meltdownTimerTicks <= 0)
            doMeltdown();
    }

    private void doMeltdown() {
        // Explosion Tier uses PRIMARY/MAX TIER component's tier
        int coolerTier = this.primaryCoolerType != null ? this.primaryCoolerType.getTier() : 0;
        int moderatorTier = this.primaryModeratorType != null ? this.primaryModeratorType.getTier() : 0;

        float coolerPower = coolerTier * 1.5f;
        float moderatorPower = moderatorTier * 0.7f;

        float power = 6.0f + coolerPower + moderatorPower;

        if (getLevel() instanceof net.minecraft.server.level.ServerLevel world) {

            net.minecraft.world.entity.Entity explosionCauser = null;

            double x = getPos().getX() + 0.5;
            double y = getPos().getY() + 0.5;
            double z = getPos().getZ() + 0.5;

            world.explode(
                    explosionCauser,
                    x, y, z,
                    power,
                    net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);
        }

        meltdownTimerTicks = -1;
        meltdownTimerMax = 0;
    }

    public static com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction recipeModifier(
                                                                                            com.gregtechceu.gtceu.api.machine.MetaMachine machine,
                                                                                            com.gregtechceu.gtceu.api.recipe.GTRecipe recipe) {
        if (!(machine instanceof FissionSteamMultiblockMachine m))
            return RecipeModifier.nullWrongType(FissionSteamMultiblockMachine.class, machine);

        // Fuel Discount is ADDITIVE (Sum all)
        double totalFuelDiscountPercent = m.activeModerators.stream()
                .mapToInt(IFissionModeratorType::getFuelDiscount)
                .sum();

        double durationMultiplier = Math.max(0.01, 1.0 - (totalFuelDiscountPercent / 100.0));

        // EU boost is still ignored in the Steam Fission Reactor (eutMultiplier = 1.0)
        return ModifierFunction.builder()
                .eutMultiplier(1.0)
                .durationMultiplier(durationMultiplier)
                .build();
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);

        if (!isFormed()) {
            textList.add(Component.translatable("phoenix.fission.not_formed")
                    .withStyle(s -> s.withColor(0xFF4444)));
            return;
        }

        if (!isWorkingEnabled() && !isActive() && meltdownTimerTicks < 0) {
            textList.add(Component.translatable("phoenix.fission.status.safe_idle")
                    .withStyle(s -> s.withColor(0x33FF33)));
        }

        else if (lastRequiredCooling > 0 && lastProvidedCooling >= lastRequiredCooling && lastHasCoolant) {
            textList.add(Component.translatable("phoenix.fission.status.safe_working")
                    .withStyle(s -> s.withColor(0x00CCFF)));
        }

        else if (meltdownTimerTicks > 0) {
            int seconds = getMeltdownSecondsRemaining();
            textList.add(Component.translatable("phoenix.fission.status.danger_timer", seconds)
                    .withStyle(s -> s.withColor(0xFFAA00)));

            if (!lastHasCoolant) {
                textList.add(Component.translatable("phoenix.fission.status.no_coolant")
                        .withStyle(s -> s.withColor(0xFF3333)));
            } else {
                textList.add(Component.translatable("phoenix.fission.status.low_cooling")
                        .withStyle(s -> s.withColor(0xFF5555)));
            }
        }

        // Moderator Display: Primary Name + Total Count
        String moderatorName = getModeratorName();
        String moderatorCount = this.activeModerators.size() > 1 ?
                "(" + this.activeModerators.size() + " total)" : "";

        Component moderatorDisplay = Component.literal(moderatorName + " " + moderatorCount);

        textList.add(Component.translatable("phoenix.fission.moderator", moderatorDisplay));
        textList.add(Component.translatable("phoenix.fission.moderator_fuel_discount", getModeratorFuelDiscount()));

        // Cooler Display: Primary Name + Total Count
        String coolerName = getCoolerName();
        String coolerCount = this.activeCoolers.size() > 1 ?
                "(" + this.activeCoolers.size() + " total)" : "";

        Component coolerDisplay = Component.literal(coolerName + " " + coolerCount);
        textList.add(Component.translatable("phoenix.fission.cooler", coolerDisplay));

        // Coolant Display: Primary Material Name and Primary Rate
        int totalCoolantRate = getCoolantRatePerTick();

        if (totalCoolantRate > 0 && this.primaryCoolerType != null) {
            String coolantMatName = getCoolantName();
            Component coolantComp = Component.literal(coolantMatName);

            textList.add(Component.translatable("phoenix.fission.coolant", coolantComp));

            textList.add(Component.translatable(lastHasCoolant ?
                    "phoenix.fission.coolant_status.ok" : "phoenix.fission.coolant_status.empty")
                    .withStyle(s -> s.withColor(lastHasCoolant ? 0x33FF33 : 0xFF3333)));

            textList.add(Component.translatable("phoenix.fission.coolant_rate", totalCoolantRate));
        } else {
            Component coolantComp = Component.literal("None Required");
            textList.add(Component.translatable("phoenix.fission.coolant", coolantComp));
        }

        if (lastRequiredCooling > 0) {
            int color = lastProvidedCooling >= lastRequiredCooling ? 0x33FF33 : 0xFF3333;
            textList.add(Component.translatable("phoenix.fission.summary",
                    lastProvidedCooling, lastRequiredCooling)
                    .withStyle(s -> s.withColor(color)));
        }
    }

    @SuppressWarnings("unused")
    public float getExplosionProgress() {
        if (meltdownTimerTicks < 0) return 1f;
        if (meltdownTimerMax <= 0) return 0f;
        return (float) meltdownTimerTicks / (float) meltdownTimerMax;
    }

    public int getMeltdownSecondsRemaining() {
        if (meltdownTimerTicks < 0) return 0;
        return Math.max(0, meltdownTimerTicks / 20);
    }

    public int getModeratorEUBoost() {
        return this.activeModerators.stream()
                .mapToInt(IFissionModeratorType::getEUBoost)
                .sum();
    }

    public int getModeratorFuelDiscount() {
        return this.activeModerators.stream()
                .mapToInt(IFissionModeratorType::getFuelDiscount)
                .sum();
    }

    public String getModeratorName() {
        return this.primaryModeratorType != null ? this.primaryModeratorType.getName() : "None";
    }

    public String getCoolerName() {
        return this.primaryCoolerType != null ? this.primaryCoolerType.getName() : "None";
    }

    public String getCoolantName() {
        Material mat = this.primaryCoolerType != null ? this.primaryCoolerType.getRequiredCoolantMaterial() : null;
        if (mat == null || mat == GTMaterials.NULL) return "None";
        return mat.getName();
    }

    public int getCoolantRatePerTick() {
        return this.primaryCoolerType != null ? this.primaryCoolerType.getCoolantPerTick() : 0;
    }
}
