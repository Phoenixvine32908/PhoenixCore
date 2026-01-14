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
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
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
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.phoenix.core.api.gui.PhoenixGuiTextures;
import net.phoenix.core.api.machine.trait.ITeslaBattery;
import net.phoenix.core.common.data.item.PhoenixItems;
import net.phoenix.core.common.machine.multiblock.UniqueWorkableElectricMultiblockMachine;
import net.phoenix.core.common.machine.multiblock.part.special.TeslaEnergyHatchPartMachine;
import net.phoenix.core.configs.PhoenixConfigs;
import net.phoenix.core.utils.TeamUtils;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

public class TeslaTowerMachine extends UniqueWorkableElectricMultiblockMachine
        implements IEnergyInfoProvider, IFancyUIMachine, IDataStickInteractable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            TeslaTowerMachine.class, UniqueWorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);
public TeslaTowerMachine(IMachineBlockEntity holder) {
    super(holder);
    this.energyBank = new TeslaEnergyBank(this, List.of());
    this.tickSubscription = new ConditionalSubscriptionHandler(this, this::transferEnergyTick, this::isFormed);
}


    public static final int MAX_BATTERY_LAYERS = 18;
    public static final int MIN_CASINGS = 14;

    public static final long PASSIVE_DRAIN_DIVISOR = 20 * 60 * 60 * 24 * 100;
    public static final long PASSIVE_DRAIN_MAX_PER_STORAGE = 100_000L;

    public static final String TTB_BATTERY_HEADER= "TTBatteries_";

    private static final BigInteger BIG_INTEGER_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

    private IMaintenanceMachine maintenance;

    @Getter
    private TeslaTowerMachine.TeslaEnergyBank energyBank;
    private EnergyContainerList inputHatches;
    private EnergyContainerList outputHatches;
    private long passiveDrain;

    private long netInLastSec;
    @Getter
    private long inputPerSec;
    private long netOutLastSec;
    @Getter
    private long outputPerSec;

    protected ConditionalSubscriptionHandler tickSubscription;




    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        if (ownerTeamUUID != null) {
            registerTower(this);
        }
        TeslaWirelessRegistry.registerTower(this);

        this.maintenance = null;

        List<IEnergyContainer> inputs = new ArrayList<>();
        List<IEnergyContainer> outputs = new ArrayList<>();

        for (IMultiPart part : getParts()) {
            if (part instanceof IMaintenanceMachine maintenanceMachine) {
                this.maintenance = maintenanceMachine;
            }

            // Wired energy containers
            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                IO io = handlerList.getHandlerIO();
                if (io == IO.NONE) continue;

                var containers = handlerList.getCapability(EURecipeCapability.CAP).stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .toList();

                if (io.support(IO.IN)) inputs.addAll(containers);
                if (io.support(IO.OUT)) outputs.addAll(containers);

                traitSubscriptions.add(handlerList.subscribe(tickSubscription::updateSubscription, EURecipeCapability.CAP));
            }

            // Tesla wired hatches
            if (part instanceof TeslaEnergyHatchPartMachine hatch && !hatch.isWireless()) {
                if (hatch.getBoundTower() == null) hatch.bindToTower(this);
                if (hatch.getIO() == IO.IN) inputs.add(hatch.getEnergyContainer());
                else if (hatch.getIO() == IO.OUT) outputs.add(hatch.getEnergyContainer());
            }
        }

        this.inputHatches = new EnergyContainerList(inputs);
        this.outputHatches = new EnergyContainerList(outputs);

        // Collect batteries
        List<ITeslaBattery> batteries = new ArrayList<>();
        for (Map.Entry<String, Object> entry : getMultiblockState().getMatchContext().entrySet()) {
            if (entry.getKey().startsWith(TTB_BATTERY_HEADER) && entry.getValue() instanceof BatteryMatchWrapper wrapper) {
                for (int i = 0; i < wrapper.amount; i++) batteries.add(wrapper.partType);
            }
        }

        if (batteries.isEmpty()) {
            onStructureInvalid();
            return;
        }

        if (this.energyBank == null) this.energyBank = new TeslaEnergyBank(this, batteries);
        else this.energyBank = energyBank.rebuild(batteries);

        this.passiveDrain = this.energyBank.getPassiveDrainPerTick();
    }





    protected void transferEnergyTick() {
        if (getLevel().isClientSide) return;

        // Update stats every 20 ticks
        if (getOffsetTimer() % 20 == 0) {
            getRecipeLogic().setStatus(energyBank.hasEnergy() ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE);
            inputPerSec = netInLastSec;
            outputPerSec = netOutLastSec;
            netInLastSec = 0;
            netOutLastSec = 0;
        }

        if (!isWorkingEnabled() || !isFormed()) return;

        // ----- Wired GTCE hatches -----
        if (inputHatches != null && outputHatches != null) {
            // Input: pull energy from hatches → tower
            long energyFromInputs = energyBank.fill(inputHatches.getEnergyStored());
            inputHatches.changeEnergy(-energyFromInputs);
            netInLastSec += energyFromInputs;

            // Passive drain from tower
            long energyPassiveDrained = energyBank.drain(getPassiveDrain());
            netOutLastSec += energyPassiveDrained;

            // Output: push energy from tower → hatches
            long energyToOutputs = energyBank.drain(outputHatches.getEnergyCapacity() - outputHatches.getEnergyStored());
            outputHatches.changeEnergy(energyToOutputs);
            netOutLastSec += energyToOutputs;
        }

        // ----- Wireless Tesla hatches -----
        PhoenixConfigs.FeatureConfigs.TeslaConnectionMode mode = PhoenixConfigs.INSTANCE.features.teslaConnectionMode;
        for (IMultiPart part : getParts()) {
            if (!(part instanceof TeslaEnergyHatchPartMachine hatch)) continue;
            if (!hatch.isWireless()) continue;

            // TEAM_AUTO: auto-bind tower if unbound
            if (mode == PhoenixConfigs.FeatureConfigs.TeslaConnectionMode.TEAM_AUTO) {
                if (hatch.getOwnerTeamUUID() == null || !hatch.getOwnerTeamUUID().equals(ownerTeamUUID)) continue;
                if (hatch.getBoundTower() == null) hatch.bindToTower(this);
            }

            // DATA_STICK: only tick if bound
            if (mode == PhoenixConfigs.FeatureConfigs.TeslaConnectionMode.DATA_STICK && hatch.getBoundTower() != this)
                continue;

            tickWirelessHatch(hatch);
        }
    }

    /** Corrected energy transfer for a single wireless hatch */
    private void tickWirelessHatch(TeslaEnergyHatchPartMachine hatch) {
        IEnergyContainer container = hatch.getEnergyContainer();
        TeslaEnergyBank bank = this.energyBank;

        long stored = container.getEnergyStored();
        long capacity = container.getEnergyCapacity();
        long transferRate = container.getInputVoltage() * container.getInputAmperage();

        if (hatch.getIO() == IO.OUT) {
            long space = capacity - stored;
            long toPull = Math.min(transferRate, space);
            long pulled = bank.drain(toPull);
            container.changeEnergy(pulled);
        } else if (hatch.getIO() == IO.IN) {
            long toPush = Math.min(transferRate, stored);
            long accepted = bank.fill(toPush);
            container.changeEnergy(-accepted);
        }
    }





    private static final Map<UUID, TeslaTowerMachine> TEAM_TOWER_MAP = new HashMap<>();

    public static void registerTower(TeslaTowerMachine tower) {
        if (tower.ownerTeamUUID != null) {
            TEAM_TOWER_MAP.put(tower.ownerTeamUUID, tower);
        }
    }

    public static void unregisterTower(TeslaTowerMachine tower) {
        if (tower.ownerTeamUUID != null) {
            TEAM_TOWER_MAP.remove(tower.ownerTeamUUID);
        }
    }

    public static TeslaTowerMachine getTowerByTeam(UUID team) {
        return TEAM_TOWER_MAP.get(team);
    }





    @Override
    public void onStructureInvalid() {
        inputHatches = null;
        outputHatches = null;

        TeslaWirelessRegistry.unregisterTower(this);

        unregisterTower(this);

        passiveDrain = 0;
        netInLastSec = 0;
        inputPerSec = 0;
        netOutLastSec = 0;
        outputPerSec = 0;
        super.onStructureInvalid();
    }





    private static MutableComponent getTimeToFillDrainText(BigInteger timeToFillSeconds) {
        if (timeToFillSeconds.compareTo(BIG_INTEGER_MAX_LONG) > 0) {
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
        } else if (duration.toDays() <= 730) {
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

    private BigInteger[] storage;
    private BigInteger[] maximums;
    @Setter
    @Getter private BigInteger capacity;
    private int index;


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
    @Persisted
    private BlockPos boundTowerPos;



    @Nullable
    private TeslaTowerMachine getBoundTower() {
        if (boundTowerPos == null || !(getLevel() instanceof ServerLevel sl)) return null;
        BlockEntity be = getLevel().getBlockEntity(boundTowerPos);
        if (!(be instanceof IMachineBlockEntity mbe)) return null;
        if (!(mbe.getMetaMachine() instanceof TeslaTowerMachine tower)) return null;
        return tower;
    }


    private TickableSubscription energySyncSub;



    @Override
    public void onUnload() {
        super.onUnload();
        if (energySyncSub != null) {
            energySyncSub.unsubscribe();
            energySyncSub = null;
        }
    }




    public void bindToTower(TeslaTowerMachine tower) {
        if (tower == null) return;
        boundTowerPos = tower.self().getPos();
        self().markDirty();
    }





        public static class TeslaEnergyBank extends MachineTrait {

            protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
                    TeslaTowerMachine.TeslaEnergyBank.class);
            private static final String NBT_SIZE = "Size";
            private static final String NBT_STORED = "Stored";
            private static final String NBT_MAX = "Max";

            private BigInteger[] storage;
            private BigInteger[] maximums;
            @Getter
            private BigInteger capacity;
            private int index;
            private final List<ITeslaBattery> batteries;

            public TeslaEnergyBank(MetaMachine machine, List<ITeslaBattery> batteries) {
                super(machine);
                this.batteries = new ArrayList<>(batteries);
                storage = new BigInteger[batteries.size()];
                maximums = new BigInteger[batteries.size()];
                capacity = BigInteger.ZERO;
                for (int i = 0; i < batteries.size(); i++) {
                    maximums[i] = batteries.get(i).getCapacity();
                    storage[i] = BigInteger.ZERO;
                    capacity = capacity.add(maximums[i]);
                }
            }
            public int getHighestTier() {
                if (batteries.isEmpty()) return 0;
                return batteries.stream().mapToInt(ITeslaBattery::getTier).max().orElse(0);
            }

            public void readFromNBT(CompoundTag storageTag) {
                int size = storageTag.getInt(NBT_SIZE);
                storage = new BigInteger[size];
                maximums = new BigInteger[size];
                capacity = BigInteger.ZERO;
                for (int i = 0; i < size; i++) {
                    CompoundTag subtag = storageTag.getCompound(String.valueOf(i));
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
    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack binder) {
        if (!binder.is(PhoenixItems.TESLA_BINDER.get())) return InteractionResult.PASS;
        if (!getLevel().isClientSide) {
            ensureOwnerTeamUUID();
            if (ownerTeamUUID != null) {
                binder.getOrCreateTag().putUUID("TargetTeam", ownerTeamUUID);
                player.sendSystemMessage(Component.literal("Tesla Frequency Copied.").withStyle(ChatFormatting.GREEN));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(getLevel().isClientSide);
    }
    @Persisted
    private int batteryTier;
    @Persisted
    private UUID ownerTeamUUID;
    private void ensureOwnerTeamUUID() {
        if (!(getLevel() instanceof ServerLevel sl)) return;
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID != null) {
            ServerPlayer sp = sl.getServer().getPlayerList().getPlayer(ownerUUID);
            if (sp != null) {
                this.ownerTeamUUID = TeamUtils.getTeamIdOrPlayerFallback(sp);
                self().markDirty();
            }
        }
    }



    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);

        if (!isFormed()) {
            textList.add(Component.literal("Tesla Network: Inactive").withStyle(ChatFormatting.RED));
            return;
        }

        Style GOLD = Style.EMPTY.withColor(ChatFormatting.GOLD);
        Style AQUA = Style.EMPTY.withColor(ChatFormatting.AQUA);
        Style GRAY = Style.EMPTY.withColor(ChatFormatting.GRAY);
        Style GREEN = Style.EMPTY.withColor(ChatFormatting.GREEN);
        Style RED = Style.EMPTY.withColor(ChatFormatting.RED);

        textList.add(Component.literal("Tesla Network: ")
                .append(Component.literal(isWorkingEnabled() ? "ONLINE" : "OFFLINE")
                        .withStyle(isWorkingEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED)));

        textList.add(Component.literal("Team: ")
                .append(Component.literal(ownerTeamUUID == null ? "None" : TeamUtils.getTeamName(ownerTeamUUID))
                        .withStyle(AQUA)));

        if (energyBank != null) {
            textList.add(Component.literal("Stored: ")
                    .append(Component.literal(FormattingUtil.formatNumbers(energyBank.getStored()))
                            .withStyle(GOLD))
                    .append(Component.literal(" / ").withStyle(GRAY))
                    .append(Component.literal(FormattingUtil.formatNumbers(energyBank.getCapacity()))
                            .withStyle(GOLD)));

            textList.add(Component.literal("Battery Tier: ")
                    .append(Component.literal(GTValues.VN[energyBank.getHighestTier()]).withStyle(AQUA)));
        }
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 190, 125);
        var container = new DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(getScreenTexture());

        container.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));

        container.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText).setMaxWidthLimit(150));

        container.addWidget(new ImageWidget(140, 50, 32, 32, () -> getTeslaTierTexture(energyBank.getHighestTier())));

        group.addWidget(container);
        group.setBackground(com.gregtechceu.gtceu.api.gui.GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private com.lowdragmc.lowdraglib.gui.texture.IGuiTexture getTeslaTierTexture(int tier) {
        return switch (tier) {
            case 10 -> PhoenixGuiTextures.BATTERY_BAR_UEV;
            case 11 -> PhoenixGuiTextures.BATTERY_BAR_UIV;
            case 12 -> PhoenixGuiTextures.BATTERY_BAR_UXV;
            case 13 -> PhoenixGuiTextures.BATTERY_BAR_OPV;
            case 14 -> PhoenixGuiTextures.BATTERY_BAR_MAX;
            default -> PhoenixGuiTextures.BATTERY_BAR_UHV;
        };
    }
}
