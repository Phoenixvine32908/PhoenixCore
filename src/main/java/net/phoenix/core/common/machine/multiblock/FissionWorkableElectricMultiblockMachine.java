package net.phoenix.core.common.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import net.phoenix.core.PhoenixAPI;
import net.phoenix.core.api.block.IFissionCoolerType;
import net.phoenix.core.api.block.IFissionModeratorType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
public class FissionWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine
                                                      implements IExplosionMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            FissionWorkableElectricMultiblockMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private int meltdownTimerTicks = -1;
    @Persisted
    private int meltdownTimerMax = 0;

    // FIX 1: Serialization fix - Persist String IDs instead of the objects/interfaces
    @Persisted
    private List<String> persistedCoolerIDs = new ArrayList<>();
    @Persisted
    private List<String> persistedModeratorIDs = new ArrayList<>();

    // FIX 2: Runtime fields - Store the actual objects transiently
    private transient List<IFissionCoolerType> activeCoolers = new ArrayList<>();
    private transient List<IFissionModeratorType> activeModerators = new ArrayList<>();

    // FIX 3: Primary Component tracking (Transient)
    @Nullable
    private transient IFissionCoolerType primaryCoolerType = null;
    @Nullable
    private transient IFissionModeratorType primaryModeratorType = null;

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

    // NEW: Resolve persisted IDs back to objects on world load
    @Override
    public void onLoad() {
        super.onLoad();
        resolvePersistedComponents();
        // Recalculate primary components based on the loaded list
        this.primaryCoolerType = getPrimaryCooler(this.activeCoolers);
        this.primaryModeratorType = getPrimaryModerator(this.activeModerators);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        // FIX 4: Retrieve the component lists from the match context (plural keys)
        @SuppressWarnings("unchecked")
        List<IFissionCoolerType> foundCoolers = (List<IFissionCoolerType>) getMultiblockState().getMatchContext()
                .get("CoolerTypes");
        this.activeCoolers = foundCoolers == null ? new ArrayList<>() : foundCoolers;

        @SuppressWarnings("unchecked")
        List<IFissionModeratorType> foundModerators = (List<IFissionModeratorType>) getMultiblockState()
                .getMatchContext().get("ModeratorTypes");
        this.activeModerators = foundModerators == null ? new ArrayList<>() : foundModerators;

        // FIX 5: Persist the serialized names of the components
        this.persistedCoolerIDs = this.activeCoolers.stream()
                .map(IFissionCoolerType::getName).collect(Collectors.toList());
        this.persistedModeratorIDs = this.activeModerators.stream()
                .map(IFissionModeratorType::getName).collect(Collectors.toList());

        // FIX 6: Determine the Primary Components
        this.primaryCoolerType = getPrimaryCooler(this.activeCoolers);
        this.primaryModeratorType = getPrimaryModerator(this.activeModerators);

        meltdownTimerTicks = -1;
        meltdownTimerMax = 0;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();

        // Clear all lists/types
        activeCoolers.clear();
        activeModerators.clear();
        persistedCoolerIDs.clear();
        persistedModeratorIDs.clear();
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

        // REMOVED redundant component part retrieval logic (now handled by onStructureFormed/onLoad)

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

        // FIX 7: Cooling is ADDITIVE (Sum all)
        lastProvidedCooling = this.activeCoolers.stream()
                .mapToInt(IFissionCoolerType::getCoolerTemperature)
                .sum();

        // FIX 8: Coolant Needed is NON-ADDITIVE (Use Primary Cooler's rate)
        int coolantNeededPerTick = this.primaryCoolerType != null ? this.primaryCoolerType.getCoolantPerTick() : 0;

        // Use the primary type for coolant consumption check
        lastHasCoolant = tryConsumePrimaryCoolant(this.primaryCoolerType, coolantNeededPerTick);

        int effectiveProvidedCooling = lastHasCoolant ? lastProvidedCooling : 0;

        int deficit = Math.max(0, lastRequiredCooling - effectiveProvidedCooling);
        float deficitPct = lastRequiredCooling == 0 ? 0f : ((float) deficit / (float) lastRequiredCooling);

        handleDangerTiers(deficitPct);

        return true;
    }

    // NEW: Resolves stored string IDs back into object types
    private void resolvePersistedComponents() {
        this.activeCoolers.clear();
        this.activeModerators.clear();

        for (String id : this.persistedCoolerIDs) {
            IFissionCoolerType type = resolveCoolerType(id);
            if (type != null) this.activeCoolers.add(type);
        }

        for (String id : this.persistedModeratorIDs) {
            IFissionModeratorType type = resolveModeratorType(id);
            if (type != null) this.activeModerators.add(type);
        }
    }

    // NEW HELPER: Resolves IFissionCoolerType from its serialized name using the API map
    private @Nullable IFissionCoolerType resolveCoolerType(String serializedName) {
        return PhoenixAPI.FISSION_COOLERS.keySet().stream()
                .filter(type -> type.getName().equals(serializedName))
                .findFirst()
                .orElse(null);
    }

    // NEW HELPER: Resolves IFissionModeratorType from its serialized name using the API map
    private @Nullable IFissionModeratorType resolveModeratorType(String serializedName) {
        return PhoenixAPI.FISSION_MODERATORS.keySet().stream()
                .filter(type -> type.getName().equals(serializedName))
                .findFirst()
                .orElse(null);
    }

    // NEW HELPER: Finds the Primary Cooler Type
    private @Nullable IFissionCoolerType getPrimaryCooler(List<IFissionCoolerType> componentList) {
        if (componentList.isEmpty()) return null;
        Map<IFissionCoolerType, Long> counts = componentList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return counts.entrySet().stream()
                .max(Comparator.<Map.Entry<IFissionCoolerType, Long>>comparingLong(Map.Entry::getValue)
                        .thenComparingInt(e -> e.getKey().getTier()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // NEW HELPER: Finds the Primary Moderator Type
    private @Nullable IFissionModeratorType getPrimaryModerator(List<IFissionModeratorType> componentList) {
        if (componentList.isEmpty()) return null;
        Map<IFissionModeratorType, Long> counts = componentList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return counts.entrySet().stream()
                .max(Comparator.<Map.Entry<IFissionModeratorType, Long>>comparingLong(Map.Entry::getValue)
                        .thenComparingInt(e -> e.getKey().getTier()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // Updated and renamed logic to use primaryCoolerType
    private boolean tryConsumePrimaryCoolant(@Nullable IFissionCoolerType primaryCooler, int amountMb) {
        if (primaryCooler == null || amountMb <= 0) return true;

        Material requiredMat = primaryCooler.getRequiredCoolantMaterial();
        if (requiredMat == null || requiredMat == GTMaterials.NULL) return true;

        GTRecipe dummyRecipe = com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder.ofRaw()
                .inputFluids(requiredMat.getFluid(amountMb))
                .buildRawRecipe();

        if (!RecipeHelper.matchRecipe(this, dummyRecipe).isSuccess()) {
            return false;
        }

        if (!RecipeHelper.handleRecipeIO(
                this,
                dummyRecipe,
                IO.IN,
                getRecipeLogic().getChanceCaches()).isSuccess()) {
            return false;
        }
        return true;
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
        if (deficitPct <= 0f) {
            meltdownTimerTicks = -1;
            meltdownTimerMax = 0;
            return;
        }

        if (!lastHasCoolant) {
            int grace = 15;
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
        // FIX 9: Use Primary type's tier for explosion power
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
                    x,
                    y,
                    z,
                    power,
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

        // FIX 10: EU Boost/Discount is ADDITIVE (Sum all active moderators)
        double totalEUBoostPercent = m.activeModerators.stream()
                .mapToInt(IFissionModeratorType::getEUBoost)
                .sum();
        double totalFuelDiscountPercent = m.activeModerators.stream()
                .mapToInt(IFissionModeratorType::getFuelDiscount)
                .sum();

        double eutMultiplier = 1.0 + (totalEUBoostPercent / 100.0);
        double durationMultiplier = Math.max(0.01, 1.0 - (totalFuelDiscountPercent / 100.0));

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
                    .withStyle(s -> s.withColor(0xFF4444)));
            return;
        }
        // ... (Status messages)

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

        // FIX 11: Display Primary Name + Total Count + Summed Boosts/Discounts
        String moderatorName = getPrimaryModeratorName();
        String moderatorCount = this.activeModerators.size() > 1 ?
                " (" + this.activeModerators.size() + " total)" : "";

        Component moderatorDisplay;
        if (this.primaryModeratorType != null) {
            String namespace = getNamespaceForType(this.primaryModeratorType);
            moderatorDisplay = Component.translatable("block." + namespace + "." + moderatorName)
                    .append(moderatorCount);
        } else {
            moderatorDisplay = Component.literal("None");
        }

        textList.add(Component.translatable("phoenix.fission.moderator", moderatorDisplay));
        textList.add(Component.translatable("phoenix.fission.moderator_boost", getModeratorEUBoost()));
        textList.add(Component.translatable("phoenix.fission.moderator_fuel_discount", getModeratorFuelDiscount()));

        // FIX 12: Cooler Display: Primary Name + Total Count
        String coolerName = getPrimaryCoolerName();
        String coolerCount = this.activeCoolers.size() > 1 ?
                " (" + this.activeCoolers.size() + " total)" : "";

        Component coolerDisplay;
        if (this.primaryCoolerType != null) {
            String namespace = getNamespaceForType(this.primaryCoolerType);
            coolerDisplay = Component.translatable("block." + namespace + "." + coolerName)
                    .append(coolerCount);
        } else {
            coolerDisplay = Component.literal("None");
        }

        textList.add(Component.translatable("phoenix.fission.cooler", coolerDisplay));

        // Coolant Display: Uses Primary Coolant properties
        int coolantRate = getCoolantRatePerTick();

        if (coolantRate > 0 && this.primaryCoolerType != null) {
            Material coolantMat = this.primaryCoolerType.getRequiredCoolantMaterial();
            Component coolantComp;
            if (coolantMat == null || coolantMat == GTMaterials.NULL || coolantMat.getName().equals("none")) {
                coolantComp = Component.literal("None");
            } else {
                FluidStack fs = coolantMat.getFluid(1);
                coolantComp = fs.isEmpty() ?
                        Component.translatable(coolantMat.getDefaultTranslation()) : fs.getDisplayName();
            }

            textList.add(Component.translatable("phoenix.fission.coolant", coolantComp));

            textList.add(Component.translatable(lastHasCoolant ?
                    "phoenix.fission.coolant_status.ok" : "phoenix.fission.coolant_status.empty")
                    .withStyle(s -> s.withColor(lastHasCoolant ? 0x33FF33 : 0xFF3333)));

            textList.add(Component.translatable("phoenix.fission.coolant_rate", coolantRate));
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

    // NEW: Helper methods for namespace checking (moved to use primary types)
    private String getNamespaceForType(IFissionCoolerType type) {
        if (type.getClass().getName().contains("KjsCoolerType")) {
            return "kubejs";
        }
        return "phoenixcore";
    }

    private String getNamespaceForType(IFissionModeratorType type) {
        if (type.getClass().getName().contains("KjsModeratorType")) {
            return "kubejs";
        }
        return "phoenixcore";
    }
    // END NEW: Helper methods

    public float getExplosionProgress() {
        if (meltdownTimerTicks < 0) return 1f;
        if (meltdownTimerMax <= 0) return 0f;
        return (float) meltdownTimerTicks / (float) meltdownTimerMax;
    }

    public int getMeltdownSecondsRemaining() {
        if (meltdownTimerTicks < 0) return 0;
        return Math.max(0, meltdownTimerTicks / 20);
    }

    // FIX 13: Use SUM of all active moderators
    public int getModeratorEUBoost() {
        return this.activeModerators.stream()
                .mapToInt(IFissionModeratorType::getEUBoost)
                .sum();
    }

    // FIX 14: Use SUM of all active moderators
    public int getModeratorFuelDiscount() {
        return this.activeModerators.stream()
                .mapToInt(IFissionModeratorType::getFuelDiscount)
                .sum();
    }

    // FIX 15: Use PRIMARY moderator for name
    public String getPrimaryModeratorName() {
        return this.primaryModeratorType != null ? this.primaryModeratorType.getName() : "None";
    }

    // FIX 16: Use PRIMARY cooler for name
    public String getPrimaryCoolerName() {
        return this.primaryCoolerType != null ? this.primaryCoolerType.getName() : "None";
    }

    // FIX 17: Use PRIMARY cooler for coolant properties
    public String getCoolantName() {
        Material mat = this.primaryCoolerType != null ? this.primaryCoolerType.getRequiredCoolantMaterial() : null;
        if (mat == null || mat == GTMaterials.NULL) return "None";

        return mat.getName();
    }

    // FIX 18: Use PRIMARY cooler for coolant rate
    public int getCoolantRatePerTick() {
        return this.primaryCoolerType != null ? this.primaryCoolerType.getCoolantPerTick() : 0;
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
