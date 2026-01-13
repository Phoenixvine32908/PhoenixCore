package net.phoenix.core.common.machine.multiblock.electric;

import com.google.common.annotations.VisibleForTesting;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.phoenix.core.api.machine.trait.ITeslaBattery;
import net.phoenix.core.common.data.item.PhoenixItems;
import net.phoenix.core.common.machine.multiblock.UniqueWorkableElectricMultiblockMachine;
import net.phoenix.core.utils.TeamUtils;

import org.jetbrains.annotations.NotNull;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

public class TeslaTowerMachine extends UniqueWorkableElectricMultiblockMachine
        implements IEnergyInfoProvider, IFancyUIMachine, IDataStickInteractable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            TeslaTowerMachine.class, UniqueWorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);
    // Structure Constants
    public static final int MAX_BATTERY_LAYERS = 18;
    public static final int MIN_CASINGS = 14;

    // Passive Drain Constants
    // 1% capacity per 24 hours
    public static final long PASSIVE_DRAIN_DIVISOR = 20 * 60 * 60 * 24 * 100;
    // no more than 100kEU/t per storage block
    public static final long PASSIVE_DRAIN_MAX_PER_STORAGE = 100_000L;

    // Match Context Headers
    public static final String TTB_BATTERY_HEADER= "TTBatteries_";

    private static final BigInteger BIG_INTEGER_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

    private IMaintenanceMachine maintenance;

    private TeslaTowerMachine.TeslaEnergyBank energyBank;
    private EnergyContainerList inputHatches;
    private EnergyContainerList outputHatches;
    private long passiveDrain;

    // Stats tracked for UI display
    private long netInLastSec;
    @Getter
    private long inputPerSec;
    private long netOutLastSec;
    @Getter
    private long outputPerSec;

    protected ConditionalSubscriptionHandler tickSubscription;

    public TeslaTowerMachine(IMachineBlockEntity holder) {
        super(holder);
        this.tickSubscription = new ConditionalSubscriptionHandler(this, this::transferEnergyTick, this::isFormed);
        this.energyBank = new TeslaTowerMachine.TeslaEnergyBank(this, List.of());
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        List<IEnergyContainer> inputs = new ArrayList<>();
        List<IEnergyContainer> outputs = new ArrayList<>();
        Long2ObjectMap<IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap",
                Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if (io == IO.NONE) continue;
            if (part instanceof IMaintenanceMachine maintenanceMachine) {
                this.maintenance = maintenanceMachine;
            }
            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                if (!handlerList.isValid(io)) continue;

                var containers = handlerList.getCapability(EURecipeCapability.CAP).stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .toList();

                if (handlerList.getHandlerIO().support(IO.IN)) {
                    inputs.addAll(containers);
                } else if (handlerList.getHandlerIO().support(IO.OUT)) {
                    outputs.addAll(containers);
                }

                traitSubscriptions
                        .add(handlerList.subscribe(tickSubscription::updateSubscription, EURecipeCapability.CAP));
            }
        }
        this.inputHatches = new EnergyContainerList(inputs);
        this.outputHatches = new EnergyContainerList(outputs);

        List<ITeslaBattery> batteries = new ArrayList<>();
        for (Map.Entry<String, Object> battery : getMultiblockState().getMatchContext().entrySet()) {
            if (battery.getKey().startsWith(TTB_BATTERY_HEADER) &&
                    battery.getValue() instanceof TeslaTowerMachine.BatteryMatchWrapper wrapper) {
                for (int i = 0; i < wrapper.amount; i++) {
                    batteries.add(wrapper.partType);
                }
            }
        }
        if (batteries.isEmpty()) {
            // only empty batteries found in the structure
            onStructureInvalid();
            return;
        }
        if (this.energyBank == null) {
            this.energyBank = new TeslaTowerMachine.TeslaEnergyBank(this, batteries);
        } else {
            this.energyBank = energyBank.rebuild(batteries);
        }
        this.passiveDrain = this.energyBank.getPassiveDrainPerTick();
    }

    @Override
    public void onStructureInvalid() {
        // don't null out energyBank since it holds the stored energy, which
        // we need to hold on to across rebuilds to not void all energy if a
        // multiblock part or block other than the controller is broken.
        inputHatches = null;
        outputHatches = null;
        passiveDrain = 0;
        netInLastSec = 0;
        inputPerSec = 0;
        netOutLastSec = 0;
        outputPerSec = 0;
        super.onStructureInvalid();
    }

    protected void transferEnergyTick() {
        if (!getLevel().isClientSide) {
            if (getOffsetTimer() % 20 == 0) {
                // active here is just used for rendering
                getRecipeLogic()
                        .setStatus(energyBank.hasEnergy() ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE);
                inputPerSec = netInLastSec;
                outputPerSec = netOutLastSec;
                netInLastSec = 0;
                netOutLastSec = 0;
            }

            if (isWorkingEnabled() && isFormed()) {
                // Bank from Energy Input Hatches
                long energyBanked = energyBank.fill(inputHatches.getEnergyStored());
                inputHatches.changeEnergy(-energyBanked);
                netInLastSec += energyBanked;

                // Passive drain
                long energyPassiveDrained = energyBank.drain(getPassiveDrain());
                netOutLastSec += energyPassiveDrained;

                // Debank to Dynamo Hatches
                long energyDebanked = energyBank
                        .drain(outputHatches.getEnergyCapacity() - outputHatches.getEnergyStored());
                outputHatches.changeEnergy(energyDebanked);
                netOutLastSec += energyDebanked;
            }
        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            if (!isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused"));

            } else if (isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running"));
                int currentProgress = (int) (recipeLogic.getProgressPercent() * 100);
                double maxInSec = (float) recipeLogic.getDuration() / 20.0f;
                double currentInSec = (float) recipeLogic.getProgress() / 20.0f;
                textList.add(
                        Component.translatable("gtceu.multiblock.progress", String.format("%.2f", (float) currentInSec),
                                String.format("%.2f", (float) maxInSec), currentProgress));
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
            }

            if (recipeLogic.isWaiting()) {
                textList.add(Component.translatable("gtceu.multiblock.waiting")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            }

            if (energyBank != null) {
                BigInteger energyStored = energyBank.getStored();
                BigInteger energyCapacity = energyBank.getCapacity();

                var STYLE_GOLD = Style.EMPTY.withColor(ChatFormatting.GOLD);
                var STYLE_DARK_RED = Style.EMPTY.withColor(ChatFormatting.DARK_RED);
                var STYLE_GREEN = Style.EMPTY.withColor(ChatFormatting.GREEN);
                var STYLE_RED = Style.EMPTY.withColor(ChatFormatting.RED);

                var storedComponent = Component.literal(FormattingUtil.formatNumbers(energyStored));
                textList.add(Component.translatable("gtceu.multiblock.power_substation.stored",
                        storedComponent.setStyle(STYLE_GOLD)));

                var capacityComponent = Component.literal(FormattingUtil.formatNumbers(energyCapacity));
                textList.add(Component.translatable("gtceu.multiblock.power_substation.capacity",
                        capacityComponent.setStyle(STYLE_GOLD)));

                var passiveDrainComponent = Component.literal(FormattingUtil.formatNumbers(getPassiveDrain()));
                textList.add(Component.translatable("gtceu.multiblock.power_substation.passive_drain",
                        passiveDrainComponent.setStyle(STYLE_DARK_RED)));

                var avgInComponent = Component.literal(FormattingUtil.formatNumbers(inputPerSec / 20));
                textList.add(Component
                        .translatable("gtceu.multiblock.power_substation.average_in",
                                avgInComponent.setStyle(STYLE_GREEN))
                        .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("gtceu.multiblock.power_substation.average_in_hover")))));

                var avgOutComponent = Component.literal(FormattingUtil.formatNumbers(Math.abs(outputPerSec / 20)));
                textList.add(Component
                        .translatable("gtceu.multiblock.power_substation.average_out",
                                avgOutComponent.setStyle(STYLE_RED))
                        .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("gtceu.multiblock.power_substation.average_out_hover")))));

                if (inputPerSec > outputPerSec) {
                    BigInteger timeToFillSeconds = energyCapacity.subtract(energyStored)
                            .divide(BigInteger.valueOf(inputPerSec - outputPerSec));
                    textList.add(Component.translatable("gtceu.multiblock.power_substation.time_to_fill",
                            getTimeToFillDrainText(timeToFillSeconds).setStyle(STYLE_GREEN)));
                } else if (inputPerSec < outputPerSec) {
                    BigInteger timeToDrainSeconds = energyStored
                            .divide(BigInteger.valueOf(outputPerSec - inputPerSec));
                    textList.add(Component.translatable("gtceu.multiblock.power_substation.time_to_drain",
                            getTimeToFillDrainText(timeToDrainSeconds).setStyle(STYLE_RED)));
                }
            }
        }
        getDefinition().getAdditionalDisplay().accept(this, textList);
    }

    private static MutableComponent getTimeToFillDrainText(BigInteger timeToFillSeconds) {
        if (timeToFillSeconds.compareTo(BIG_INTEGER_MAX_LONG) > 0) {
            // too large to represent in a java Duration
            timeToFillSeconds = BIG_INTEGER_MAX_LONG;
        }

        Duration duration = Duration.ofSeconds(timeToFillSeconds.longValue());
        String key;
        long fillTime;
        if (duration.getSeconds() <= 180) {
            fillTime = duration.getSeconds();
            key = "gtceu.multiblock.power_substation.time_seconds";
        } else if (duration.toMinutes() <= 180) {
            fillTime = duration.toMinutes();
            key = "gtceu.multiblock.power_substation.time_minutes";
        } else if (duration.toHours() <= 72) {
            fillTime = duration.toHours();
            key = "gtceu.multiblock.power_substation.time_hours";
        } else if (duration.toDays() <= 730) { // 2 years
            fillTime = duration.toDays();
            key = "gtceu.multiblock.power_substation.time_days";
        } else if (duration.toDays() / 365 < 1_000_000) {
            fillTime = duration.toDays() / 365;
            key = "gtceu.multiblock.power_substation.time_years";
        } else {
            return Component.translatable("gtceu.multiblock.power_substation.time_forever");
        }

        return Component.translatable(key, FormattingUtil.formatNumbers(fillTime));
    }

    public long getPassiveDrain() {
        if (ConfigHolder.INSTANCE.machines.enableMaintenance) {
            if (maintenance == null) {
                for (IMultiPart part : getParts()) {
                    if (part instanceof IMaintenanceMachine maintenanceMachine) {
                        this.maintenance = maintenanceMachine;
                        break;
                    }
                }
            }
            int multiplier = 1 + maintenance.getNumMaintenanceProblems();
            double modifier = maintenance.getDurationMultiplier();
            return (long) (passiveDrain * multiplier * modifier);
        }
        return passiveDrain;
    }

    public String getStored() {
        if (energyBank == null) {
            return "0";
        }
        return FormattingUtil.formatNumbers(energyBank.getStored());
    }

    public String getCapacity() {
        if (energyBank == null) {
            return "0";
        }
        return FormattingUtil.formatNumbers(energyBank.getCapacity());
    }

    @Override
    public EnergyInfo getEnergyInfo() {
        return new EnergyInfo(energyBank.getCapacity(), energyBank.getStored());
    }

    @Override
    public boolean supportsBigIntEnergyValues() {
        return true;
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 182 + 8, 117 + 8);
        group.addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(getScreenTexture())
                .addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()))
                .addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                        .setMaxWidthLimit(150)
                        .clickHandler(this::handleDisplayClick)));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer).widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public List<IFancyUIProvider> getSubTabs() {
        return getParts().stream().filter(IFancyUIProvider.class::isInstance).map(IFancyUIProvider.class::cast)
                .toList();
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        for (IMultiPart part : getParts()) {
            part.attachFancyTooltipsToController(this, tooltipsPanel);
        }
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        CompoundTag bankTag = energyBank.writeToNBT(new CompoundTag());
        tag.put("energyBank", bankTag);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        energyBank.readFromNBT(tag.getCompound("energyBank"));
    }


        /**
         * Rebuild the power storage with a new list of batteries.
         * Will use existing stored power and try to map it onto new batteries.
         * If there was more power before the rebuild operation, it will be lost.
         */
        public static class TeslaEnergyBank extends MachineTrait {

            protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
                    TeslaTowerMachine.TeslaEnergyBank.class);
            private static final String NBT_SIZE = "Size";
            private static final String NBT_STORED = "Stored";
            private static final String NBT_MAX = "Max";

            // Refactored to BigInteger
            private BigInteger[] storage;
            private BigInteger[] maximums;
            @Getter
            private BigInteger capacity;
            private int index;

            public TeslaEnergyBank(MetaMachine machine, List<ITeslaBattery> batteries) {
                super(machine);
                storage = new BigInteger[batteries.size()];
                maximums = new BigInteger[batteries.size()];
                capacity = BigInteger.ZERO;
                for (int i = 0; i < batteries.size(); i++) {
                    maximums[i] = batteries.get(i).getCapacity();
                    storage[i] = BigInteger.ZERO;
                    capacity = capacity.add(maximums[i]);
                }
            }

            public void readFromNBT(CompoundTag storageTag) {
                int size = storageTag.getInt(NBT_SIZE);
                storage = new BigInteger[size];
                maximums = new BigInteger[size];
                capacity = BigInteger.ZERO;
                for (int i = 0; i < size; i++) {
                    CompoundTag subtag = storageTag.getCompound(String.valueOf(i));
                    // Convert NBT Strings back to BigInteger for safety with huge numbers
                    storage[i] = new BigInteger(subtag.getString(NBT_STORED).isEmpty() ? "0" : subtag.getString(NBT_STORED));
                    maximums[i] = new BigInteger(subtag.getString(NBT_MAX).isEmpty() ? "0" : subtag.getString(NBT_MAX));
                    capacity = capacity.add(maximums[i]);
                }
            }

            public CompoundTag writeToNBT(CompoundTag compound) {
                compound.putInt(NBT_SIZE, storage.length);
                for (int i = 0; i < storage.length; i++) {
                    CompoundTag subtag = new CompoundTag();
                    subtag.putString(NBT_STORED, storage[i].toString());
                    subtag.putString(NBT_MAX, maximums[i].toString());
                    compound.put(String.valueOf(i), subtag);
                }
                return compound;
            }

            public TeslaTowerMachine.TeslaEnergyBank rebuild(@NotNull List<ITeslaBattery> batteries) {
                TeslaTowerMachine.TeslaEnergyBank newStorage = new TeslaTowerMachine.TeslaEnergyBank(this.machine, batteries);
                for (BigInteger stored : storage) {
                    newStorage.fill(stored);
                }
                return newStorage;
            }

            /** Overloaded fill for long (standard GTCEu hatches) **/
            public long fill(long amount) {
                BigInteger filled = fill(BigInteger.valueOf(amount));
                return filled.longValue();
            }

            public BigInteger fill(BigInteger amount) {
                if (amount.signum() < 0) return BigInteger.ZERO;

                if (index < storage.length && storage[index].equals(maximums[index])) {
                    if (index < storage.length - 1) index++;
                }

                BigInteger space = maximums[index].subtract(storage[index]);
                BigInteger toFill = amount.min(space);

                if (toFill.equals(BigInteger.ZERO) && index == storage.length - 1) {
                    return BigInteger.ZERO;
                }

                storage[index] = storage[index].add(toFill);
                BigInteger remaining = amount.subtract(toFill);

                if (remaining.signum() > 0 && index < storage.length - 1) {
                    return toFill.add(fill(remaining));
                }

                return toFill;
            }

            /** Overloaded drain for long **/
            public long drain(long amount) {
                BigInteger drained = drain(BigInteger.valueOf(amount));
                return drained.longValue();
            }

            public BigInteger drain(BigInteger amount) {
                if (amount.signum() < 0) return BigInteger.ZERO;

                if (index >= 0 && storage[index].equals(BigInteger.ZERO)) {
                    if (index > 0) index--;
                }

                BigInteger toDrain = storage[index].min(amount);

                if (toDrain.equals(BigInteger.ZERO) && index == 0) {
                    return BigInteger.ZERO;
                }

                storage[index] = storage[index].subtract(toDrain);
                BigInteger remaining = amount.subtract(toDrain);

                if (remaining.signum() > 0 && index > 0) {
                    return toDrain.add(drain(remaining));
                }

                return toDrain;
            }

            public BigInteger getStored() {
                BigInteger total = BigInteger.ZERO;
                for (BigInteger b : storage) total = total.add(b);
                return total;
            }

            public boolean hasEnergy() {
                return getStored().signum() > 0;
            }

            public long getPassiveDrainPerTick() {
                // Logic for passive drain using BigInteger
                BigInteger totalDrain = BigInteger.ZERO;
                BigInteger divisor = BigInteger.valueOf(PASSIVE_DRAIN_DIVISOR);
                BigInteger maxPerTick = BigInteger.valueOf(PASSIVE_DRAIN_MAX_PER_STORAGE);

                for (BigInteger max : maximums) {
                    BigInteger calculated = max.divide(divisor);
                    totalDrain = totalDrain.add(calculated.min(maxPerTick));
                }
                return totalDrain.longValue();
            }

            @Override
            public ManagedFieldHolder getFieldHolder() {
                return MANAGED_FIELD_HOLDER;
            }
        }

    @Getter
    public static class BatteryMatchWrapper {

        private final ITeslaBattery partType;
        private int amount;

        public BatteryMatchWrapper(ITeslaBattery partType) {
            this.partType = partType;
        }

        public TeslaTowerMachine.BatteryMatchWrapper increment() {
            amount++;
            return this;
        }
    }
}
