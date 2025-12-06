//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package fi.dea.mc.deafission.common.data.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMufflableMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.sound.AutoReleasedSound;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.annotation.UpdateListener;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import fi.dea.mc.deafission.Config;
import fi.dea.mc.deafission.FissionMod;
import fi.dea.mc.deafission.api.IReactorApi;
import fi.dea.mc.deafission.api.ReactorFuel;
import fi.dea.mc.deafission.api.ReactorMetrics;
import fi.dea.mc.deafission.client.FissionSounds;
import fi.dea.mc.deafission.common.data.FissionGtRecipes;
import fi.dea.mc.deafission.common.data.items.FuelCellItem;
import fi.dea.mc.deafission.common.data.machine.ReactorRedstonePort.Type;
import fi.dea.mc.deafission.common.data.recipe.ComponentRecipe.Cache;
import fi.dea.mc.deafission.core.ReactorCoolantRecipe;
import fi.dea.mc.deafission.core.ReactorCurve;
import fi.dea.mc.deafission.core.ReactorCurveState;
import fi.dea.mc.deafission.core.ReactorFuelRecipe;
import fi.dea.mc.deafission.core.ReactorProcessingRecipe;
import fi.dea.mc.deafission.core.components.ComponentTotals;
import fi.dea.mc.deafission.core.components.IReactorComponent;
import fi.dea.mc.deafission.util.FractionAccumulator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FissionReactorMachine extends MultiblockControllerMachine implements IFancyUIMachine, IMufflableMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER;
    private static final int s_renderHeatStateMultiplier = 20;
    @Persisted
    @DescSynced
    @RequireRerender
    private int _renderHeatState;
    @Persisted
    @DescSynced
    @RequireRerender
    private int _renderSize;
    private final State _state = new State();
    private final Metrics _metrics = new Metrics();
    private final Api _api = new Api();
    private final ReactorIo _io = new ReactorIo();
    private @Nullable ComponentTotals _components;
    private @Nullable TickableSubscription _tickSubscription;
    @DescSynced
    @UpdateListener(
        methodName = "onStatusSynced"
    )
    private byte _workingStatus;
    private @Nullable AutoReleasedSound _workingSound;
    @Persisted
    @DescSynced
    private boolean _isMuffled;

    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public int getRenderHeat() {
        return this._renderHeatState * 20;
    }

    public int getRenderSize() {
        return this._renderSize;
    }

    public FissionReactorMachine(IMachineBlockEntity holder) {
        super(holder);
        ReactorCurveState initialState = this._getStateSnapshotForNewReactor();
        ReactorCurve curve = (ReactorCurve)Config.reactorConfiguration.CurveFactory().apply(initialState);
        this._state.Heat = curve.getMinHeat();
    }

    public void onStructureFormed() {
        super.onStructureFormed();
        if (!this.isRemote()) {
            this._io.refresh();
            this._components = this._countPassiveComponents();
            this._renderSize = 1;
            this.updateTickSubscription();
        }
    }

    private ComponentTotals _countPassiveComponents() {
        Cache.ensureLoaded(this.holder.level());
        ComponentTotals total = ComponentTotals.Zero;

        for(BlockPos pos : this.getMultiblockState().getCache()) {
            Block block = this.holder.level().m_8055_(pos).m_60734_();

            for(IReactorComponent c : Cache.getComponents(block)) {
                total = total.add(c);
            }
        }

        return total;
    }

    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (!this.isRemote()) {
            this.updateTickSubscription();
            this._components = null;
            this._io.clear();
            this._api.clear();
            this.setRenderState((MachineRenderState)this.getRenderState().m_61124_(RecipeLogic.STATUS_PROPERTY, Status.IDLE));
            this._workingStatus = 0;
        }
    }

    public boolean isMachineInvalid() {
        return !this.isFormed() || this.isInValid();
    }

    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (!forDrop) {
            this._state.save(tag);
        }
    }

    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        this._state.load(tag);
    }

    private void updateTickSubscription() {
        if (!this.isRemote()) {
            if (this.isMachineInvalid()) {
                if (this._tickSubscription != null) {
                    this._tickSubscription.unsubscribe();
                    this._tickSubscription = null;
                }
            } else {
                this._tickSubscription = this.subscribeServerTick(this._tickSubscription, this::tickServer);
            }

        }
    }

    public void clientTick() {
        this.updateSound();
    }

    public void tickServer() {
        if (!this.isMachineInvalid()) {
            this._metrics.reset();
            ReactorCoolantRecipe oldRecipe = this._state.CoolantRecipe;
            ReactorCurveState state = this._getStateSnapshot();
            ReactorCurve curve = (ReactorCurve)Config.reactorConfiguration.CurveFactory().apply(state);
            if (state.recipe() != null) {
                if (this._state.CoolantRecipe != state.recipe()) {
                    this._state.resetAccu(state.recipe() != null ? state.recipe().output().length : 0);
                }

                this._state.CoolantRecipe = state.recipe();
            }

            if (this._state.CoolantRecipe != oldRecipe) {
                this._state.Mode = FissionReactorMachine.OperatingMode.COOLDOWN;
            }

            this._metrics.LastComponents = this._components;
            this._metrics.LastRecipe = this._state.CoolantRecipe != null ? this._state.CoolantRecipe.id() : null;
            this._metrics.LastMinHeat = curve.getMinHeat();
            this._metrics.LastMaxHeat = curve.getMaxHeat();
            this._metrics.LastMode = this._state.Mode;
            this._metrics.LastHeat = this._state.Heat;
            this._metrics.LastMass = curve.getThermalMass();
            if (curve.getMinHeat() == curve.getMaxHeat()) {
                this.setRenderState((MachineRenderState)this.getRenderState().m_61124_(RecipeLogic.STATUS_PROPERTY, Status.IDLE));
            } else if (this._state.Mode == FissionReactorMachine.OperatingMode.COOLDOWN) {
                this._tickCooldown(curve, state);
                this._workingStatus = 1;
            } else {
                this._tickNormal(curve, state);
            }

            this._renderHeatState = (int)Math.round(this._state.Heat / (double)20.0F);
            if (this._state.Heat - (double)1.0F < curve.getMinHeat()) {
                this.setRenderState((MachineRenderState)this.getRenderState().m_61124_(RecipeLogic.STATUS_PROPERTY, Status.IDLE));
            }

            this.updateRedstone(curve, state);
            this._api.update();
        }
    }

    private void updateRedstone(ReactorCurve curve, ReactorCurveState state) {
        double heatValue = (this._state.Heat - curve.getMinHeat()) / curve.getMaxHeat();
        float fuelValue = (float)state.RecipeFuels().stream().filter((f) -> f != null).count() / (float)state.RecipeFuels().size();
        int heatLevel = (int)Math.round(heatValue * (double)15.0F);
        int fuelsLevel = Math.round(fuelValue * 15.0F);

        for(ReactorRedstonePort rs : this._io._redstone) {
            rs.trySetSignal(Type.HEAT, heatLevel);
            rs.trySetSignal(Type.FUELS, fuelsLevel);
        }

    }

    private void _tickCooldown(ReactorCurve curve, ReactorCurveState state) {
        double coolantConversion = this._tickCoolant(curve, state);
        double cooling = Math.max(curve.getCooling(coolantConversion), curve.getMinimumCooling());
        double delta = -cooling / curve.getThermalMass();
        this._state.Heat = ReactorCurve.clampHeat(curve, this._state.Heat + delta);
        if (this._state.Heat == curve.getMinHeat()) {
            this._state.Mode = FissionReactorMachine.OperatingMode.DEFAULT;
        }

        this._metrics.LastCooling = cooling;
        this._metrics.LastCoolingPercent = coolantConversion;
        this.setRenderState((MachineRenderState)this.getRenderState().m_61124_(RecipeLogic.STATUS_PROPERTY, Status.WAITING));
    }

    private void _tickNormal(ReactorCurve curve, ReactorCurveState state) {
        double throttle = curve.getThrottle((double)1.0F);
        this._tickNormal_recipeItems(curve, state);
        this._tickNormal_fuelItems(curve, state);
        this._tickNormal_fuel(curve, state, throttle);
        double coolantConversion = this._tickCoolant(curve, state);
        this._tickNormal_heatAndProcessing(curve, state, coolantConversion, throttle);
        this._metrics.LastCoolingPercent = coolantConversion;
        this._metrics.LastThrottle = throttle;
        RecipeLogic.Status newWorkingState = state.RecipeFuels().stream().anyMatch((f) -> f != null) ? Status.WORKING : Status.WAITING;
        RecipeLogic.Status oldWorkingState = (RecipeLogic.Status)this.getRenderState().m_61143_(RecipeLogic.STATUS_PROPERTY);
        if (newWorkingState != oldWorkingState) {
            this.setRenderState((MachineRenderState)this.getRenderState().m_61124_(RecipeLogic.STATUS_PROPERTY, newWorkingState));
        }

        this._workingStatus = (byte)(newWorkingState == Status.WORKING ? 2 : 0);
    }

    private void _tickNormal_recipeItems(ReactorCurve curve, ReactorCurveState state) {
        Map<Item, ReactorProcessingRecipe> recipes = FissionGtRecipes.get_processingRecipes();

        for(ReactorMaterialHolder mh : this._io._materials) {
            if (mh.getHeldItem().m_41619_()) {
                ItemStack item = this._io.drainSingleInputItem((is) -> !(is.m_41720_() instanceof FuelCellItem) && recipes.get(is.m_41720_()) != null);
                if (item != null) {
                    mh.setHeldItem(item);
                }
            }
        }

    }

    private void _tickNormal_fuelItems(ReactorCurve curve, ReactorCurveState state) {
        if (state.recipe() != null) {
            if (!state.RecipeFuels().stream().allMatch((f) -> f != null)) {
                for(ReactorFuelHolder holder : this._io._fuels) {
                    if (holder.isEmpty()) {
                        ItemStack item = this._io.drainSingleInputItem((is) -> state.recipe().isValidFuel(is));
                        if (item != null) {
                            holder.setHeldItem(item);
                        }
                    }
                }

            }
        }
    }

    private void _tickNormal_fuel(ReactorCurve curve, ReactorCurveState state, double throttle) {
        if (state.recipe() != null && !state.RecipeFuels().stream().allMatch((f) -> f == null)) {
            double damage = curve.getFuelWear(throttle);
            if (damage != (double)0.0F) {
                int integerDamage = (int)damage;
                double fractionalDamage = damage - (double)integerDamage;
                double expectedTotal = (double)0.0F;
                double total = (double)0.0F;
                RandomSource random = ((Level)Objects.requireNonNull(this.getLevel())).m_213780_();

                for(int i = 0; i < state.RecipeFuels().size(); ++i) {
                    ReactorFuel fuelstat = (ReactorFuel)state.RecipeFuels().get(i);
                    if (fuelstat != null) {
                        ReactorFuelHolder holder = (ReactorFuelHolder)this._io._fuels.get(i);
                        ItemStack stack = (ItemStack)Objects.requireNonNull(holder.getHeldItem());
                        total += (double)integerDamage + fractionalDamage;
                        int fract = random.m_188500_() < fractionalDamage ? 1 : 0;
                        if (stack.m_220157_(integerDamage + fract, random, (ServerPlayer)null)) {
                            this._outputSpentFuel(stack);
                            holder.setHeldItem(ItemStack.f_41583_);
                        }
                    }
                }

                this._metrics.LastFuelDamage = total;
            }
        }
    }

    private void _outputSpentFuel(ItemStack stack) {
        Map<Item, ReactorFuelRecipe> map = FissionGtRecipes.get_fuelRecipes();
        ReactorFuelRecipe recipe = (ReactorFuelRecipe)map.get(stack.m_41720_());
        if (recipe != null) {
            for(ItemStack outputItem : recipe.outputs()) {
                this._io.pushOutputItem(outputItem);
            }

            for(FluidStack outputFluid : recipe.fluidOutputs()) {
                this._io.pushOutputFluid(outputFluid.getFluid(), outputFluid.getAmount());
            }

        }
    }

    private double _tickNormal_processing(ReactorCurve curve, ReactorCurveState state, double heating, double cooling) {
        Map<Item, ReactorProcessingRecipe> recipes = FissionGtRecipes.get_processingRecipes();
        double mass = curve.getThermalMass();
        double energyAtStart = state.Heat() * mass + heating;
        double energy = energyAtStart;

        for(ReactorMaterialHolder mt : this._io._materials) {
            ItemStack stack = mt.getHeldItem();
            if (!stack.m_41619_()) {
                ReactorProcessingRecipe recipe = (ReactorProcessingRecipe)recipes.get(stack.m_41720_());
                if (recipe != null) {
                    double progressTicks = mt.getProgress();
                    if (progressTicks >= (double)recipe.ticks()) {
                        this.tryFinishProcessingRecipe(mt, recipe);
                    } else if (!(state.Heat() < (double)recipe.minHeat())) {
                        double heatSpeed = recipe.minHeat() != 0 ? state.Heat() / (double)recipe.minHeat() : (double)1.0F;
                        double maxProgressIncrement;
                        if (recipe.heatPerTick() != (double)0.0F) {
                            double reactorEnergyAtMinHeat = (double)recipe.minHeat() * mass;
                            double energyAvailable = energy - reactorEnergyAtMinHeat;
                            double maxHeatPerTick = heatSpeed * recipe.heatPerTick();
                            maxProgressIncrement = Math.min(maxHeatPerTick, energyAvailable) / recipe.heatPerTick();
                        } else {
                            maxProgressIncrement = heatSpeed;
                        }

                        double progressMissing = (double)recipe.ticks() - progressTicks;
                        if (maxProgressIncrement >= progressMissing) {
                            mt.setProgress((double)recipe.ticks());
                            energy -= progressMissing * recipe.heatPerTick();
                            this.tryFinishProcessingRecipe(mt, recipe);
                        } else {
                            double progressNew = progressTicks + maxProgressIncrement;
                            mt.setProgress(progressNew);
                            energy -= maxProgressIncrement * recipe.heatPerTick();
                        }
                    }
                }
            }
        }

        double processingDelta = energy - energyAtStart;
        this._metrics.LastProcessing = processingDelta;
        return heating + processingDelta - cooling;
    }

    private void tryFinishProcessingRecipe(ReactorMaterialHolder mt, ReactorProcessingRecipe recipe) {
        if (recipe.output() != null) {
            this._io.pushOutputItem(recipe.output());
        }

        if (recipe.fluidOutput() != null) {
            this._io.pushOutputFluid(recipe.fluidOutput().getFluid(), recipe.fluidOutput().getAmount());
        }

        mt.setHeldItem(ItemStack.f_41583_);
    }

    private void _tickNormal_heatAndProcessing(ReactorCurve curve, ReactorCurveState state, double coolantConversion, double throttle) {
        double heating = curve.getHeating(throttle);
        double cooling = _tickNormal_heatAndProcessing_coolingOrMinimal(heating, curve.getCooling(coolantConversion), curve.getMinimumCooling());
        double processing = this._tickNormal_processing(curve, state, heating, cooling);
        double delta = processing / curve.getThermalMass();
        this._state.Heat = ReactorCurve.clampHeat(curve, this._state.Heat + delta);
        if (this._state.Heat == curve.getMaxHeat()) {
            this._state.Mode = FissionReactorMachine.OperatingMode.COOLDOWN;
        }

        this._metrics.LastCooling = cooling;
        this._metrics.LastHeating = heating;
    }

    private static double _tickNormal_heatAndProcessing_coolingOrMinimal(double heating, double cooling, double minCooling) {
        double actualCooling = heating > minCooling ? cooling : Math.max(cooling, minCooling);
        return actualCooling;
    }

    private double _tickCoolant(ReactorCurve curve, ReactorCurveState state) {
        if (state.recipe() == null) {
            return (double)0.0F;
        } else {
            double desired = curve.getCoolantConversion() / (double)state.recipe().divisor();
            if (desired == (double)0.0F) {
                return (double)1.0F;
            } else {
                double availableConversionFactor = Math.max((double)0.0F, Math.min((double)1.0F, (double)(state.ColdCoolantAmount() - 1) / desired));
                double conversion = availableConversionFactor * desired;
                double fractCold = conversion * (double)state.recipe().inputCoolant().getAmount();
                this._metrics.LastCoolantUse = fractCold;
                int cold = this._state.ColdCoolantConversionFraction.accumulate(fractCold);
                int res = this._io.drainInputFluid(state.recipe().inputCoolant().getFluid(), cold);
                double actualConversionFactor = cold == 0 ? (double)1.0F : (double)res / (double)cold;
                double actualConversion = actualConversionFactor * conversion;

                for(int i = 0; i < state.recipe().output().length; ++i) {
                    FluidStack output = state.recipe().output()[i];
                    int hot = this._state.HotCoolantConversionFractions[i].accumulate(actualConversion * (double)output.getAmount());
                    this._io.pushOutputFluid(output.getFluid(), hot);
                }

                double finalPercent = actualConversion / desired;

                assert finalPercent >= (double)0.0F && finalPercent <= (double)1.0F : "Coolant conversion math is bugged.";

                return finalPercent;
            }
        }
    }

    private ReactorCurveState _getStateSnapshot() {
        ReactorCoolantRecipe recipe = this._getRecipe();
        return new ReactorCurveState(this._state.Heat, this._getStateSnapshot_matchingFuel(recipe), recipe != null ? this._io.countInputFluid(recipe.inputCoolant().getFluid(), Integer.MAX_VALUE) : 0, 0, 0, 0, (ComponentTotals)Objects.requireNonNull(this._components), recipe);
    }

    private @Unmodifiable List<@Nullable ReactorFuel> _getStateSnapshot_matchingFuel(@Nullable ReactorCoolantRecipe recipe) {
        if (recipe == null) {
            return Collections.nCopies(this._io._fuels.size(), (Object)null);
        } else {
            List<ReactorFuel> list = this._io._fuels.stream().map((h) -> h.getHeldItem()).map((h) -> recipe.isValidFuel(h) ? ((FuelCellItem)h.m_41720_()).getFuelStats(h) : null).toList();
            return list;
        }
    }

    private ReactorCurveState _getStateSnapshotForNewReactor() {
        return new ReactorCurveState((double)0.0F, Collections.EMPTY_LIST, 0, 0, 0, 0, ComponentTotals.Zero, (ReactorCoolantRecipe)null);
    }

    private @Nullable ReactorCoolantRecipe _getRecipe() {
        if (this._io._fuels.size() == 0) {
            return null;
        } else {
            List<ItemStack> fuels = this._io._fuels.stream().map((h) -> h.getHeldItem()).filter((h) -> h != null).toList();
            if (this._state.CoolantRecipe != null && this._is_old_recipe_match(this._state.CoolantRecipe, fuels)) {
                return this._state.CoolantRecipe;
            } else {
                for(ReactorCoolantRecipe recipe : FissionGtRecipes.get_coolantRecipes()) {
                    if (this._is_new_recipe_match(recipe, fuels)) {
                        return recipe;
                    }
                }

                return null;
            }
        }
    }

    private boolean _is_old_recipe_match(ReactorCoolantRecipe recipe, List<ItemStack> fuels) {
        if (this._io.countInputFluid(recipe.inputCoolant().getFluid(), 1) > 0) {
            return true;
        } else {
            return this._is_fuel_match(recipe, fuels) || this._io.haveSingleInputInput((i) -> recipe.isValidFuel(i));
        }
    }

    private boolean _is_new_recipe_match(ReactorCoolantRecipe recipe, List<ItemStack> fuels) {
        if (!Config.reactorConfiguration.RunWithoutCoolant() && this._io.countInputFluid(recipe.inputCoolant().getFluid(), 1) == 0) {
            return false;
        } else {
            return this._is_fuel_match(recipe, fuels) || this._io.haveSingleInputInput((i) -> recipe.isValidFuel(i));
        }
    }

    private boolean _is_fuel_match(ReactorCoolantRecipe recipe, List<ItemStack> fuels) {
        return fuels.stream().anyMatch((f) -> recipe.isValidFuel(f));
    }

    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 190, 125);
        group.addWidget((new DraggableScrollableWidgetGroup(4, 4, 182, 117)).setBackground(GuiTextures.DISPLAY).addWidget(new LabelWidget(4, 5, this.self().getBlockState().m_60734_().m_7705_())).addWidget((new ComponentPanelWidget(4, 17, this::addDisplayText)).textSupplier(((Level)Objects.requireNonNull(this.getLevel())).f_46443_ ? null : this::addDisplayText).setMaxWidthLimit(200)));
        group.setBackground(new IGuiTexture[]{GuiTextures.BACKGROUND_INVERSE});
        return group;
    }

    void addDisplayText(List<Component> textList) {
        for(String m : this.getApi().getMetricsStrings()) {
            textList.add(Component.m_237113_(m));
        }

        textList.add(Component.m_237113_("Mass: " + this._metrics.LastMass));
    }

    protected void onStatusSynced(byte newValue, byte oldValue) {
        this.updateSound();
    }

    public boolean isMuffled() {
        return this._isMuffled;
    }

    public void setMuffled(boolean isMuffled) {
        this._isMuffled = isMuffled;
    }

    @OnlyIn(Dist.CLIENT)
    public void updateSound() {
        if (this._workingStatus != 0 && this.shouldSoundClipLaunch()) {
            SoundEntry sound = FissionSounds.FISSION_LOOP;
            if (this._workingSound != null) {
                if (this._workingSound.soundEntry == sound && !this._workingSound.m_7801_()) {
                    return;
                }

                this._workingSound.release();
                this._workingSound = null;
            }

            if (sound != null) {
                this._workingSound = sound.playAutoReleasedSound(this::shouldSoundClipPlay, this.getPos(), true, 0, 1.0F, 1.0F);
            }
        } else if (this._workingSound != null) {
            this._workingSound.release();
            this._workingSound = null;
        }

    }

    private boolean shouldSoundClipPlay() {
        return this.shouldSoundClipLaunch() && this._workingStatus != 0 && !this.isMachineInvalid() && this.holder.level().m_46749_(this.getPos()) && MetaMachine.getMachine(this.holder.level(), this.getPos()) == this;
    }

    @OnlyIn(Dist.CLIENT)
    private boolean shouldSoundClipLaunch() {
        return !this._isMuffled && ConfigHolder.INSTANCE.machines.machineSounds;
    }

    public IReactorApi getApi() {
        return this._api;
    }

    static {
        MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FissionReactorMachine.class, MultiblockControllerMachine.MANAGED_FIELD_HOLDER);
    }

    public class ReactorIo {
        private final List<IRecipeHandler<FluidIngredient>> _fluidIn = new ArrayList();
        private final List<IRecipeHandler<FluidIngredient>> _fluidOut = new ArrayList();
        private final List<IRecipeHandler<Ingredient>> _itemIn = new ArrayList();
        private final List<IRecipeHandler<Ingredient>> _itemOut = new ArrayList();
        private final List<ReactorFuelHolder> _fuels = new ArrayList();
        private final List<ReactorMaterialHolder> _materials = new ArrayList();
        private final List<ReactorRedstonePort> _redstone = new ArrayList();

        public void clear() {
            this._fluidIn.clear();
            this._fluidOut.clear();
            this._itemIn.clear();
            this._itemOut.clear();
            this._fuels.clear();
            this._materials.clear();
            this._redstone.clear();
        }

        public void refresh() {
            this.clear();

            for(IMultiPart part : FissionReactorMachine.this.getParts()) {
                for(RecipeHandlerList handler : part.getRecipeHandlers()) {
                    this.populate(this._fluidIn, GTRecipeCapabilities.FLUID, IO.IN, handler);
                    this.populate(this._fluidOut, GTRecipeCapabilities.FLUID, IO.OUT, handler);
                    this.populate(this._itemIn, GTRecipeCapabilities.ITEM, IO.IN, handler);
                    this.populate(this._itemOut, GTRecipeCapabilities.ITEM, IO.OUT, handler);
                }

                if (part instanceof ReactorFuelHolder fh) {
                    this._fuels.add(fh);
                }

                if (part instanceof ReactorMaterialHolder mh) {
                    this._materials.add(mh);
                }

                if (part instanceof ReactorRedstonePort rp) {
                    this._redstone.add(rp);
                }
            }

        }

        private <T> void populate(List<IRecipeHandler<T>> list, RecipeCapability<T> cap, IO io, RecipeHandlerList handler) {
            List<IRecipeHandler<?>> caps = handler.getCapability(cap);
            if (handler.getHandlerIO().support(io) && !caps.isEmpty()) {
                list.add((IRecipeHandler)caps.get(0));
            }

        }

        public int countInputFluid(Fluid fluid, int upTo) {
            List<FluidIngredient> drainRecipe = List.of(FluidIngredient.of(fluid, upTo));

            for(IRecipeHandler<FluidIngredient> handler : this._fluidIn) {
                drainRecipe = handler.handleRecipe(IO.IN, (GTRecipe)null, drainRecipe, true);
                if (drainRecipe == null) {
                    return upTo;
                }
            }

            return upTo - ((FluidIngredient)drainRecipe.get(0)).getAmount();
        }

        public int drainInputFluid(Fluid fluid, int amount) {
            if (amount == 0) {
                return 0;
            } else {
                List<FluidIngredient> drainRecipe = List.of(FluidIngredient.of(fluid, amount));

                for(IRecipeHandler<FluidIngredient> handler : this._fluidIn) {
                    drainRecipe = handler.handleRecipe(IO.IN, (GTRecipe)null, drainRecipe, false);
                    if (drainRecipe == null) {
                        return amount;
                    }
                }

                return amount - ((FluidIngredient)drainRecipe.get(0)).getAmount();
            }
        }

        public int pushOutputFluid(Fluid fluid, int amount) {
            if (amount == 0) {
                return 0;
            } else {
                List<FluidIngredient> fillRecipe = List.of(FluidIngredient.of(fluid, amount));

                for(IRecipeHandler<FluidIngredient> handler : this._fluidOut) {
                    fillRecipe = handler.handleRecipe(IO.OUT, (GTRecipe)null, fillRecipe, false);
                    if (fillRecipe == null) {
                        return amount;
                    }
                }

                return amount - ((FluidIngredient)fillRecipe.get(0)).getAmount();
            }
        }

        public int pushOutputItem(ItemStack item) {
            ItemStack work = new ItemStack(item.m_41720_(), item.m_41613_());

            for(IRecipeHandler<Ingredient> handler : this._itemOut) {
                if (handler instanceof NotifiableItemStackHandler nist) {
                    for(int slot = 0; slot < nist.getSlots(); ++slot) {
                        work = nist.insertItemInternal(slot, work, false);
                        if (work.m_41619_()) {
                            return item.m_41613_();
                        }
                    }
                } else {
                    FissionMod.LOG.warn("NotImplemented: output bus of type {}", handler.getClass().getName());
                }
            }

            return item.m_41613_() - work.m_41613_();
        }

        public @Nullable ItemStack drainSingleInputItem(Function<ItemStack, Boolean> predicate) {
            for(IRecipeHandler<Ingredient> handler : this._itemIn) {
                if (handler instanceof NotifiableItemStackHandler nist) {
                    for(int slot = 0; slot < nist.getSlots(); ++slot) {
                        ItemStack slotItem = nist.getStackInSlot(slot);
                        if (!slotItem.m_41619_() && (Boolean)predicate.apply(slotItem)) {
                            ItemStack extractedItem = nist.extractItemInternal(slot, 1, false);
                            if (extractedItem != null) {
                                return extractedItem;
                            }
                        }
                    }
                } else {
                    FissionMod.LOG.warn("NotImplemented: input bus of type {}", handler.getClass().getName());
                }
            }

            return null;
        }

        public boolean haveSingleInputInput(Function<ItemStack, Boolean> predicate) {
            for(IRecipeHandler<Ingredient> handler : this._itemIn) {
                if (handler instanceof NotifiableItemStackHandler nist) {
                    for(int slot = 0; slot < nist.getSlots(); ++slot) {
                        ItemStack slotItem = nist.getStackInSlot(slot);
                        if (!slotItem.m_41619_() && (Boolean)predicate.apply(slotItem)) {
                            ItemStack extractedItem = nist.extractItemInternal(slot, 1, true);
                            if (extractedItem != null) {
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }
    }

    public class Api implements IReactorApi {
        private @Nullable ReactorMetrics _currentMetrics;
        private @Nullable List<@Nullable ReactorFuel> _currentFuels;

        void update() {
            this._updateMetrics();
            this._updateFuels();
        }

        void clear() {
            this._currentMetrics = null;
            this._currentFuels = null;
        }

        public @Nullable ReactorMetrics getMetrics() {
            return this._currentMetrics;
        }

        public @Nullable List<@Nullable ReactorFuel> getFuels() {
            return this._currentFuels;
        }

        private void _updateMetrics() {
            this._currentMetrics = new ReactorMetrics(FissionReactorMachine.this._metrics.LastCoolantUse, FissionReactorMachine.this._metrics.LastCoolingPercent, FissionReactorMachine.this._metrics.LastFuelDamage, FissionReactorMachine.this._metrics.LastCooling, FissionReactorMachine.this._metrics.LastHeating, FissionReactorMachine.this._metrics.LastProcessing, FissionReactorMachine.this._metrics.LastMinHeat, FissionReactorMachine.this._metrics.LastMaxHeat, FissionReactorMachine.this._metrics.LastMass, FissionReactorMachine.this._metrics.LastThrottle, FissionReactorMachine.this._metrics.LastHeat, FissionReactorMachine.this._state.Heat, FissionReactorMachine.this._metrics.LastRecipe, FissionReactorMachine.this._metrics.LastComponents, FissionReactorMachine.this._metrics.LastMode);
        }

        private void _updateFuels() {
            this._currentFuels = FissionReactorMachine.this._io._fuels.stream().map((h) -> h.getHeldItem()).map((h) -> h != null ? ((FuelCellItem)h.m_41720_()).getFuelStats(h) : null).toList();
        }

        public byte[] getFuelDurabilityBytes() {
            byte[] arr = new byte[FissionReactorMachine.this._io._fuels.size()];

            for(int i = 0; i < arr.length; ++i) {
                ItemStack stack = ((ReactorFuelHolder)FissionReactorMachine.this._io._fuels.get(i)).getHeldItem();
                if (stack != null) {
                    int intValue = 1 + Math.round(254.0F - (float)stack.m_41773_() / (float)stack.m_41776_() * 254.0F);
                    arr[i] = (byte)(intValue & 255);
                }
            }

            return arr;
        }

        public List<String> getMetricsStrings() {
            return FissionReactorMachine.this._metrics.render();
        }
    }

    public static enum OperatingMode implements StringRepresentable {
        DEFAULT("default"),
        COOLDOWN("cooldown"),
        INCOMPLETE("incomplete");

        private final String name;
        private static final Map<String, OperatingMode> s_lookup = (Map)Stream.of(values()).collect(Collectors.toMap(OperatingMode::m_7912_, (e) -> e));

        private OperatingMode(String name) {
            this.name = name;
        }

        public @NotNull String m_7912_() {
            return this.name;
        }

        public static OperatingMode fromSerializedName(String name) {
            return (OperatingMode)s_lookup.getOrDefault(name, DEFAULT);
        }
    }

    private static class State {
        public double Heat = (double)0.0F;
        public FractionAccumulator ColdCoolantConversionFraction = new FractionAccumulator((double)0.0F);
        public FractionAccumulator[] HotCoolantConversionFractions = new FractionAccumulator[0];
        public OperatingMode Mode;
        public @Nullable ReactorCoolantRecipe CoolantRecipe;

        private State() {
            this.Mode = FissionReactorMachine.OperatingMode.DEFAULT;
        }

        public void save(CompoundTag tag) {
            tag.m_128347_("Heat", this.Heat);
            tag.m_128347_("ColdCoolantConversionFraction", this.ColdCoolantConversionFraction.getFraction());
            tag.m_128385_("HotCoolantConversionFractions", this._ser(this.HotCoolantConversionFractions));
            tag.m_128359_("Mode", this.Mode.m_7912_());
            if (this.CoolantRecipe != null) {
                tag.m_128359_("CoolantRecipe", this.CoolantRecipe.id().toString());
            }

        }

        public void load(CompoundTag tag) {
            this.Heat = tag.m_128459_("Heat");
            this.ColdCoolantConversionFraction = new FractionAccumulator(tag.m_128459_("ColdCoolantConversionFraction"));
            this.HotCoolantConversionFractions = this._deserFracts(tag.m_128465_("HotCoolantConversionFractions"));
            this.Mode = FissionReactorMachine.OperatingMode.fromSerializedName(tag.m_128461_("Mode"));
            ResourceLocation coolantRecipeId = ResourceLocation.parse(tag.m_128461_("CoolantRecipe"));
            this.CoolantRecipe = (ReactorCoolantRecipe)FissionGtRecipes.get_coolantRecipes().stream().filter((r) -> r.id().equals(coolantRecipeId)).findFirst().orElse((Object)null);
            this.resetAccu(this.CoolantRecipe != null ? this.CoolantRecipe.output().length : 0);
        }

        private int[] _ser(FractionAccumulator[] arr) {
            int[] ret = new int[arr.length];

            for(int i = 0; i < arr.length; ++i) {
                ret[i] = (int)(arr[i].getFraction() * (double)Integer.MAX_VALUE);
            }

            return ret;
        }

        private FractionAccumulator[] _deserFracts(int[] arr) {
            FractionAccumulator[] ret = new FractionAccumulator[arr.length];

            for(int i = 0; i < arr.length; ++i) {
                ret[i] = new FractionAccumulator((double)arr[i] / (double)Integer.MAX_VALUE);
            }

            return ret;
        }

        public void resetAccu(int outputFluids) {
            this.ColdCoolantConversionFraction = new FractionAccumulator((double)0.0F);
            this.HotCoolantConversionFractions = (FractionAccumulator[])IntStream.range(0, outputFluids).mapToObj((i) -> new FractionAccumulator((double)0.0F)).toArray((x$0) -> new FractionAccumulator[x$0]);
        }
    }

    private static class Metrics {
        public double LastCoolantUse;
        public double LastFuelDamage;
        public double LastCooling;
        public double LastCoolingPercent;
        public double LastHeating;
        public double LastProcessing;
        public double LastMinHeat;
        public double LastMaxHeat;
        public double LastMass;
        public double LastHeat;
        public double LastThrottle;
        public @Nullable ResourceLocation LastRecipe;
        public @Nullable ComponentTotals LastComponents;
        public OperatingMode LastMode;

        private Metrics() {
            this.LastMode = FissionReactorMachine.OperatingMode.DEFAULT;
        }

        public void reset() {
            this.LastCoolantUse = (double)0.0F;
            this.LastFuelDamage = (double)0.0F;
            this.LastCooling = (double)0.0F;
            this.LastCoolingPercent = (double)0.0F;
            this.LastHeating = (double)0.0F;
            this.LastProcessing = (double)0.0F;
            this.LastMinHeat = (double)0.0F;
            this.LastMaxHeat = (double)0.0F;
            this.LastMass = (double)0.0F;
            this.LastHeat = (double)0.0F;
            this.LastThrottle = (double)0.0F;
            this.LastRecipe = null;
            this.LastComponents = null;
            this.LastMode = FissionReactorMachine.OperatingMode.DEFAULT;
        }

        public List<String> render() {
            ArrayList<String> list = new ArrayList(7);
            list.add(String.format("Mode: %s", this.LastMode));
            if (this.LastComponents != null) {
                list.add(String.format("%s H %s Th %s Eff", this.LastComponents.heat(), this.LastComponents.throttle(), this.LastComponents.efficiency()));
            } else {
                list.add("INVALID");
            }

            String[] r = this.LastRecipe != null ? this.LastRecipe.toString().split("/") : null;
            list.add(String.format("Recipe: %s", r != null ? r[r.length - 1] : null));
            if (this.LastCoolingPercent != (double)1.0F) {
                long p = Math.round(this.LastCoolingPercent * (double)100.0F);
                list.add(String.format("Coolant: %.3f mB/t (had %s%%)", this.LastCoolantUse, p));
            } else {
                list.add(String.format("Coolant: %.3f mB/t", this.LastCoolantUse));
            }

            long p = Math.round(this.LastThrottle * (double)100.0F);
            list.add(String.format("Fuel: %.3f dmg @ %s%%", this.LastFuelDamage, p));
            list.add(String.format("Cooling: %.3f HU/t", this.LastCooling));
            list.add(String.format("Heating: %.3f HU/t", this.LastHeating));
            list.add(String.format("Processing: %.3f HU/t", this.LastProcessing));
            return list;
        }
    }
}
