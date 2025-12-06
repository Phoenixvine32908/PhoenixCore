package net.phoenix.core.common.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.phoenix.core.api.block.IFissionCoolerType;
import net.phoenix.core.api.block.IFissionModeratorType;
import net.phoenix.core.common.block.FissionCoolerBlock;
import net.phoenix.core.common.block.FissionModeratorBlock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@MethodsReturnNonnullByDefault
public class FissionWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine
                                                      implements IExplosionMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            FissionWorkableElectricMultiblockMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private int meltdownTimerTicks = -1;
    @Persisted
    private int meltdownTimerMax = 0;

    private IFissionCoolerType activeCooler = FissionCoolerBlock.fissionCoolerType.COOLER_TRUE_HEAT_STABLE;

    private IFissionModeratorType activeModerator = FissionModeratorBlock.fissionModeratorType.MODERATOR_GRAPHITE;

    public int lastRequiredCooling = 0;
    public int lastProvidedCooling = 0;

    public boolean lastHasCoolant = true;

    public FissionWorkableElectricMultiblockMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        Object cooler = getMultiblockState().getMatchContext().get("CoolerType");
        if (cooler instanceof IFissionCoolerType ct) activeCooler = ct;

        Object moderator = getMultiblockState().getMatchContext().get("ModeratorType");
        if (moderator instanceof IFissionModeratorType mt) activeModerator = mt;

        meltdownTimerTicks = -1;
        meltdownTimerMax = 0;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();

        activeCooler = FissionCoolerBlock.fissionCoolerType.COOLER_TRUE_HEAT_STABLE;

        activeModerator = FissionModeratorBlock.fissionModeratorType.MODERATOR_GRAPHITE;
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

        List<IFissionModeratorType> mods = getParts().stream()
                .filter(p -> p instanceof IFissionModeratorType)
                .map(p -> (IFissionModeratorType) p)
                .toList();
        if (!mods.isEmpty()) activeModerator = mods.stream()
                .max(Comparator.comparingInt(IFissionModeratorType::getTier)).orElse(activeModerator);

        List<IFissionCoolerType> cools = getParts().stream()
                .filter(p -> p instanceof IFissionCoolerType)
                .map(p -> (IFissionCoolerType) p)
                .toList();
        Optional<IFissionCoolerType> opt = cools.stream()
                // Use getCoolerTemperature() for matching
                .filter(c -> c.getCoolerTemperature() >= lastRequiredCooling)
                // Use getCoolerTemperature() for comparison
                .min(Comparator.comparingInt(IFissionCoolerType::getCoolerTemperature));
        opt.ifPresent(c -> activeCooler = c);

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

        // Use getCoolerTemperature() for provided cooling power
        lastProvidedCooling = activeCooler == null ? 0 : activeCooler.getCoolerTemperature();

        // Use the interface's calculated coolant rate
        int coolantNeededPerTick = activeCooler.getCoolantPerTick();

        lastHasCoolant = tryConsumeCoolantFromParts(activeCooler, coolantNeededPerTick);

        // Effective cooling is 0 if no coolant is present, triggering the grace period
        int effectiveProvidedCooling = lastHasCoolant ? lastProvidedCooling : 0;

        int deficit = Math.max(0, lastRequiredCooling - effectiveProvidedCooling);
        float deficitPct = lastRequiredCooling == 0 ? 0f : ((float) deficit / (float) lastRequiredCooling);

        handleDangerTiers(deficitPct);

        return true;
    }

    // Removed the now-redundant calculateCoolantPerTick() method

    private boolean tryConsumeCoolantFromParts(@Nullable IFissionCoolerType cooler, int mb) {
        if (cooler == null || mb <= 0) return false;

        Material mat = cooler.getRequiredCoolantMaterial();
        if (mat == null) return false;

        FluidStack required = mat.getFluid(mb);
        if (required == null || required.isEmpty()) return false;

        var tanks = getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);

        for (var handler : tanks) {
            if (!(handler instanceof NotifiableFluidTank tank)) continue;

            for (int i = 0; i < tank.getTanks(); i++) {
                var fluid = tank.getFluidInTank(i);

                if (!fluid.isEmpty() && fluid.getFluid().isSame(required.getFluid())) {
                    // ✅ Use drainInternal instead of drain
                    int drained = tank.drainInternal(required, IFluidHandler.FluidAction.EXECUTE).getAmount();
                    if (drained >= mb) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private FluidStack materialToFluidStack(Material mat, int mb) {
        if (mat == null || mb <= 0) return FluidStack.EMPTY;

        try {
            FluidStack fs = mat.getFluid(mb);
            return fs == null ? FluidStack.EMPTY : fs;
        } catch (Throwable ignored) {
            return FluidStack.EMPTY;
        }
    }

    private void handleDangerTiers(float deficitPct) {
        // Case 1: No deficit → Safe
        if (deficitPct <= 0f) {
            meltdownTimerTicks = -1;
            meltdownTimerMax = 0;
            return;
        }

        // Case 2: No coolant → fixed 15s countdown
        if (!lastHasCoolant) {
            int grace = 15; // seconds
            meltdownTimerMax = grace * 20;

            if (meltdownTimerTicks < 0)
                meltdownTimerTicks = meltdownTimerMax;

            meltdownTimerTicks -= 1; // 1 tick per tick

            if (meltdownTimerTicks <= 0)
                doMeltdown();

            return;
        }

        // Case 3: Cooler temp is BELOW requirement
        // deficitPct = (required - provided) / required
        // Map deficitPct from 0→60 seconds, 1→10 seconds
        float graceSeconds = 60f - (deficitPct * 50f);
        if (graceSeconds < 10f) graceSeconds = 10f;

        meltdownTimerMax = (int) (graceSeconds * 20);

        // Initialize timer if first time
        if (meltdownTimerTicks < 0)
            meltdownTimerTicks = meltdownTimerMax;

        // Tick down 1 per tick
        meltdownTimerTicks -= 1;

        if (meltdownTimerTicks <= 0)
            doMeltdown();
    }

    private void doMeltdown() {
        float power = 6.0f + (activeCooler == null ? 0f : activeCooler.getTier() * 1.5f) +
                (activeModerator == null ? 0f : activeModerator.getTier() * 0.7f);

        if (getLevel() instanceof net.minecraft.server.level.ServerLevel world) {

            // FIX: The BlockEntity (getHolder()) cannot be cast to Entity.
            // Use 'null' as the explosion source to satisfy the API signature.
            net.minecraft.world.entity.Entity explosionCauser = null; // Use null instead of casting

            double x = getPos().getX() + 0.5;
            double y = getPos().getY() + 0.5;
            double z = getPos().getZ() + 0.5;

            // explode(Entity, double, double, double, float, Level.ExplosionInteraction)
            world.explode(
                    // ARG 1: Entity Source (now null, no crash)
                    explosionCauser,
                    // ARG 2, 3, 4: Coordinates
                    x,
                    y,
                    z,
                    // ARG 5: Power
                    power,
                    // ARG 6: ExplosionInteraction
                    net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);
        }

        meltdownTimerTicks = -1;
        meltdownTimerMax = 0;
    }

    public static com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction recipeModifier(
                                                                                            com.gregtechceu.gtceu.api.machine.MetaMachine machine,
                                                                                            com.gregtechceu.gtceu.api.recipe.GTRecipe recipe) {
        if (!(machine instanceof FissionWorkableElectricMultiblockMachine m))
            return com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier
                    .nullWrongType(FissionWorkableElectricMultiblockMachine.class, machine);

        IFissionModeratorType mod = m.activeModerator;
        double eutMultiplier = 1.0;
        double durationMultiplier = 1.0;

        if (mod != null) {
            eutMultiplier *= 1.0 + (mod.getEUBoost() / 100.0);
            durationMultiplier *= Math.max(0.01, 1.0 - (mod.getFuelDiscount() / 100.0));
        }

        return com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction.builder()
                .eutMultiplier(eutMultiplier)
                .durationMultiplier(durationMultiplier)
                .build();
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);

        if (!isFormed()) {
            textList.add(Component.translatable("phoenix.fission.not_formed")
                    .withStyle(s -> s.withColor(0xFF4444))); // red
            return;
        }

        // SAFE IDLE
        if (!isWorkingEnabled() && !isActive() && meltdownTimerTicks < 0) {
            textList.add(Component.translatable("phoenix.fission.status.safe_idle")
                    .withStyle(s -> s.withColor(0x33FF33))); // green
        }

        // SAFE WORKING
        else if (lastRequiredCooling > 0 && lastProvidedCooling >= lastRequiredCooling && lastHasCoolant) {
            textList.add(Component.translatable("phoenix.fission.status.safe_working")
                    .withStyle(s -> s.withColor(0x00CCFF))); // cyan
        }

        // DANGER – MELTDOWN TIMER
        else if (meltdownTimerTicks > 0) {
            int seconds = getMeltdownSecondsRemaining();
            textList.add(Component.translatable("phoenix.fission.status.danger_timer", seconds)
                    .withStyle(s -> s.withColor(0xFFAA00))); // orange-yellow

            if (!lastHasCoolant) {
                textList.add(Component.translatable("phoenix.fission.status.no_coolant")
                        .withStyle(s -> s.withColor(0xFF3333))); // bright red
            } else {
                textList.add(Component.translatable("phoenix.fission.status.low_cooling")
                        .withStyle(s -> s.withColor(0xFF5555))); // red tint
            }
        }

        // --- DETAILS ---
        String modKey = getModeratorName();
        Component moderatorName = modKey.equals("None") ?
                Component.literal("None") : Component.translatable("block.phoenixcore." + modKey);

        String coolerKey = getCoolerName();
        Component coolerName = coolerKey.equals("None") ?
                Component.literal("None") : Component.translatable("block.phoenixcore." + coolerKey);

        textList.add(Component.translatable("phoenix.fission.moderator", moderatorName));
        textList.add(Component.translatable("phoenix.fission.moderator_boost", getModeratorEUBoost()));
        textList.add(Component.translatable("phoenix.fission.moderator_fuel_discount", getModeratorFuelDiscount()));

        textList.add(Component.translatable("phoenix.fission.cooler", coolerName));

        Material coolantMat = activeCooler == null ? null : activeCooler.getRequiredCoolantMaterial();
        Component coolantComp;
        if (coolantMat == null || coolantMat == GTMaterials.NULL || coolantMat.getName().equals("none")) {
            coolantComp = Component.literal("None");
        } else {
            FluidStack fs = coolantMat.getFluid(1);
            coolantComp = fs.isEmpty() ?
                    Component.translatable(coolantMat.getDefaultTranslation()) : fs.getDisplayName();
        }

        textList.add(Component.translatable("phoenix.fission.coolant", coolantComp));

        // color-coded coolant status
        textList.add(Component.translatable(lastHasCoolant ?
                "phoenix.fission.coolant_status.ok" : "phoenix.fission.coolant_status.empty")
                .withStyle(s -> s.withColor(lastHasCoolant ? 0x33FF33 : 0xFF3333)));

        textList.add(Component.translatable("phoenix.fission.coolant_rate", activeCooler.getCoolantPerTick()));

        if (lastRequiredCooling > 0) {
            // Summary color logic
            int color = lastProvidedCooling >= lastRequiredCooling ? 0x33FF33 : 0xFF3333;
            textList.add(Component.translatable("phoenix.fission.summary",
                    lastProvidedCooling, lastRequiredCooling)
                    .withStyle(s -> s.withColor(color)));
        }
    }

    @SuppressWarnings("unused") // Method is for external GUI/rendering
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
        return activeModerator == null ? 0 : activeModerator.getEUBoost();
    }

    public int getModeratorFuelDiscount() {
        return activeModerator == null ? 0 : activeModerator.getFuelDiscount();
    }

    public String getModeratorName() {
        return activeModerator == null ? "None" : activeModerator.getName();
    }

    public String getCoolerName() {
        return activeCooler == null ? "None" : activeCooler.getName();
    }

    public String getCoolantName() {
        Material mat = activeCooler == null ? null : activeCooler.getRequiredCoolantMaterial();
        if (mat == null || mat == GTMaterials.NULL) return "None";

        return mat.getName();
    }

    // Removed the internal calculateCoolantPerTick, relying on activeCooler.getCoolantPerTick()
    public int getCoolantRatePerTick() {
        return activeCooler == null ? 0 : activeCooler.getCoolantPerTick();
    }

    public record CoolantProperties(int exampleCoolingValue, double exampleMultiplier) {

    }

    public static final Map<Material, CoolantProperties> COOLANT_PROPERTIES = new HashMap<>();
    static {
        try {
            COOLANT_PROPERTIES.put(GTMaterials.SodiumPotassium, new CoolantProperties(3, 2.0));
        } catch (Throwable ignored) {}
        try {
            COOLANT_PROPERTIES.put(GTMaterials.DistilledWater, new CoolantProperties(1, 1.0));
        } catch (Throwable ignored) {}
        try {
            COOLANT_PROPERTIES.put(GTMaterials.Lead, new CoolantProperties(5, 0.8));
        } catch (Throwable ignored) {}
    }
}
