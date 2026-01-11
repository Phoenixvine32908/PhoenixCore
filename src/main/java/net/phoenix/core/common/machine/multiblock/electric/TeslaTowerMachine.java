package net.phoenix.core.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.phoenix.core.api.gui.PhoenixGuiTextures;
import net.phoenix.core.api.machine.trait.ITeslaBattery;
import net.phoenix.core.common.data.item.PhoenixItems;
import net.phoenix.core.common.machine.multiblock.UniqueWorkableElectricMultiblockMachine;
import net.phoenix.core.common.machine.multiblock.part.special.TeslaEnergyHatchPartMachine;
import net.phoenix.core.configs.PhoenixConfigs;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;
import net.phoenix.core.saveddata.UniqueMultiblockSavedData;
import net.phoenix.core.utils.TeamUtils;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;

public class TeslaTowerMachine extends UniqueWorkableElectricMultiblockMachine
        implements IEnergyInfoProvider, IFancyUIMachine, IDisplayUIMachine, IDataStickInteractable {

    public static final String MULTIBLOCK_TYPE = "tesla_tower";
    public static final String TTB_BATTERY_HEADER = "TeslaTowerBattery_";

    private EnergyContainerList inputHatches;
    private EnergyContainerList outputHatches;

    @Persisted private long netInLastSec;
    @Persisted private long netOutLastSec;
    @Persisted private long totalInputPerTick;
    @Persisted private long totalOutputPerTick;
    @Persisted private long batteryCapacity;
    @Persisted private long batteryStored;
    @Persisted private int batteryTier;
    @Persisted private boolean isOnline;

    private UUID ownerTeamUUID;
    private boolean isDuplicate = false;
    private final ConditionalSubscriptionHandler energyTransferSubscription;

    public TeslaTowerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.energyTransferSubscription = new ConditionalSubscriptionHandler(this, this::tickEnergyTransfer, this::isFormed);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && isFormed()) {
            energyTransferSubscription.updateSubscription();
        }
    }

    /**
     * Pulls data from the Global Server Bank and pushes it into @Persisted fields.
     */
    private void refreshDisplayFields() {
        if (getLevel() instanceof ServerLevel sl && ownerTeamUUID != null) {
            var bank = TeslaTeamEnergyData.get(sl).getOrCreateEnergyBank(ownerTeamUUID);
            if (bank != null) {
                this.batteryStored = bank.getStored();
                this.batteryCapacity = bank.getCapacity();
                this.batteryTier = bank.getTier();
                this.isOnline = TeslaTeamEnergyData.get(sl).isNetworkOnline(ownerTeamUUID);

                self().markDirty();
            }
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!(getLevel() instanceof ServerLevel sl)) return;

        ensureOwnerTeamUUID();
        if (ownerTeamUUID == null) return;

        UniqueMultiblockSavedData unique = UniqueMultiblockSavedData.getOrCreate(sl);
        if (unique.hasData(ownerTeamUUID, MULTIBLOCK_TYPE)) {
            var existing = unique.getEntry(ownerTeamUUID, MULTIBLOCK_TYPE);
            if (existing != null && (!existing.getDimension().equals(getLevel().dimension().location().toString()) || !existing.getPos().equals(getPos()))) {
                this.isDuplicate = true;
                recipeLogic.setStatus(RecipeLogic.Status.SUSPEND);
                return;
            }
        }

        List<IEnergyContainer> inputs = new ArrayList<>();
        List<IEnergyContainer> outputs = new ArrayList<>();
        traitSubscriptions.clear();

        Long2ObjectMap<IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);

        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if (io == IO.NONE) continue;

            for (var handlerList : part.getRecipeHandlers()) {
                if (!handlerList.isValid(io)) continue;

                var containers = handlerList.getCapability(EURecipeCapability.CAP).stream()
                        .filter(IEnergyContainer.class::isInstance).map(IEnergyContainer.class::cast).toList();

                if (handlerList.getHandlerIO().support(IO.IN)) inputs.addAll(containers);
                else if (handlerList.getHandlerIO().support(IO.OUT)) outputs.addAll(containers);

                traitSubscriptions.add(handlerList.subscribe(energyTransferSubscription::updateSubscription, EURecipeCapability.CAP));
            }
        }

        this.inputHatches = new EnergyContainerList(inputs);
        this.outputHatches = new EnergyContainerList(outputs);

        var bank = TeslaTeamEnergyData.get(sl).getOrCreateEnergyBank(ownerTeamUUID);
        List<ITeslaBattery> batteries = collectBatteries();
        long totalCap = 0;
        int highestTier = 9;
        for (ITeslaBattery bat : batteries) {
            totalCap += bat.getCapacity();
            if (bat.getTier() > highestTier) highestTier = bat.getTier();
        }

        long totalWirelessIn = 0, totalWirelessOut = 0;
        for (IMultiPart part : getParts()) {
            if (part instanceof TeslaEnergyHatchPartMachine hatch) {
                long v = GTValues.V[hatch.getTier()];
                long perTick = v * hatch.getAmperage();
                if (hatch.energyContainer.getHandlerIO() == IO.IN) totalWirelessIn += perTick;
                else if (hatch.energyContainer.getHandlerIO() == IO.OUT) totalWirelessOut += perTick;
            }
        }

        bank.setCapacity(totalCap);
        bank.setTier(highestTier);
        bank.setMaxInput(totalWirelessIn);
        bank.setMaxOutput(totalWirelessOut);

        setNetworkStatus(true);
        TeslaTeamEnergyData.get(sl).setDirty();
        unique.addMultiblock(ownerTeamUUID, MULTIBLOCK_TYPE, getLevel().dimension().location().toString(), getPos());
        this.isDuplicate = false;

        energyTransferSubscription.updateSubscription();
        refreshDisplayFields();
    }

    protected void tickEnergyTransfer() {
        if (getLevel().isClientSide || isDuplicate || ownerTeamUUID == null) return;
        if (!(getLevel() instanceof ServerLevel sl)) return;

        var bank = TeslaTeamEnergyData.get(sl).getOrCreateEnergyBank(ownerTeamUUID);

        if (getOffsetTimer() % 20 == 0) {
            totalInputPerTick = netInLastSec / 20;
            totalOutputPerTick = netOutLastSec / 20;
            netInLastSec = 0;
            netOutLastSec = 0;

            getRecipeLogic().setStatus(bank.getStored() > 0 ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE);
            refreshDisplayFields();
        }

        if (isWorkingEnabled() && isFormed()) {
            if (inputHatches != null && inputHatches.getEnergyStored() > 0) {
                long bankSpace = bank.getCapacity() - bank.getStored();
                long toTransfer = Math.min(inputHatches.getEnergyStored(), Math.min(bankSpace, bank.getMaxInput()));
                if (toTransfer > 0) {
                    bank.deposit(toTransfer);
                    inputHatches.changeEnergy(-toTransfer);
                    netInLastSec += toTransfer;
                }
            }

            if (outputHatches != null && bank.getStored() > 0) {
                long hatchSpace = outputHatches.getEnergyCapacity() - outputHatches.getEnergyStored();
                long toTransfer = Math.min(bank.getStored(), Math.min(hatchSpace, bank.getMaxOutput()));
                if (toTransfer > 0) {
                    long pulled = bank.extract(toTransfer);
                    outputHatches.changeEnergy(pulled);
                    netOutLastSec += pulled;
                }
            }
            TeslaTeamEnergyData.get(sl).setDirty();
        }
    }

    @Override
    public @NotNull Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 190, 125);
        var container = new DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(getScreenTexture());
        container.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        container.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText).setMaxWidthLimit(120));

        container.addWidget(new ImageWidget(140, 50, 32, 32, () -> getTeslaTierTexture(this.batteryTier)));

        group.addWidget(container);
        group.setBackground(com.gregtechceu.gtceu.api.gui.GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed() || ownerTeamUUID == null) {
            textList.add(Component.literal("Tesla Network: Inactive").withStyle(ChatFormatting.RED));
            return;
        }

        var GOLD = Style.EMPTY.withColor(ChatFormatting.GOLD);
        var AQUA = Style.EMPTY.withColor(ChatFormatting.AQUA);
        var GRAY = Style.EMPTY.withColor(ChatFormatting.GRAY);

        textList.add(Component.literal("Tesla Network: ")
                .append(Component.literal(this.isOnline ? "ONLINE" : "OFFLINE")
                        .withStyle(this.isOnline ? ChatFormatting.GREEN : ChatFormatting.RED)));

        textList.add(Component.literal(" Team: ").append(Component.literal(getTeamNameLabel()).withStyle(AQUA)));

        textList.add(Component.literal(" Stored: ")
                .append(Component.literal(FormattingUtil.formatNumbers(batteryStored)).withStyle(GOLD))
                .append(Component.literal(" / ").withStyle(GRAY))
                .append(Component.literal(FormattingUtil.formatNumbers(batteryCapacity)).withStyle(GOLD)));

        textList.add(Component.literal(" IO: ")
                .append(Component.literal("+ " + FormattingUtil.formatNumbers(totalInputPerTick) + " EU/t ").withStyle(ChatFormatting.GREEN))
                .append(Component.literal("| ").withStyle(GRAY))
                .append(Component.literal("- " + FormattingUtil.formatNumbers(totalOutputPerTick) + " EU/t").withStyle(ChatFormatting.RED)));

        textList.add(Component.literal(" Battery Tier: ").append(Component.literal(GTValues.VN[batteryTier]).withStyle(AQUA)));
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

    private void setNetworkStatus(boolean online) {
        if (ownerTeamUUID != null && getLevel() instanceof ServerLevel sl) {
            TeslaTeamEnergyData.get(sl).setNetworkOnline(ownerTeamUUID, online);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        setNetworkStatus(false);
        energyTransferSubscription.unsubscribe();
    }

    private List<ITeslaBattery> collectBatteries() {
        List<ITeslaBattery> list = new ArrayList<>();
        for (Map.Entry<?, ?> entry : getMultiblockState().getMatchContext().entrySet()) {
            if (entry.getKey() instanceof String key && key.startsWith(TTB_BATTERY_HEADER) && entry.getValue() instanceof BatteryMatchWrapper wrapper) {
                for (int i = 0; i < wrapper.amount; i++) list.add(wrapper.partType);
            }
        }
        return list;
    }

    @Override public EnergyInfo getEnergyInfo() { return new EnergyInfo(BigInteger.valueOf(batteryCapacity), BigInteger.valueOf(batteryStored)); }
    @Override public long getInputPerSec() { return totalInputPerTick * 20; }
    @Override public long getOutputPerSec() { return totalOutputPerTick * 20; }
    @Override public boolean supportsBigIntEnergyValues() { return true; }
    private String getTeamNameLabel() { return ownerTeamUUID == null ? "Unlinked" : TeamUtils.getTeamName(ownerTeamUUID); }
    @Override public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) { super.saveCustomPersistedData(tag, forDrop); if (ownerTeamUUID != null) tag.putUUID("OwnerTeamUUID", ownerTeamUUID); }
    @Override public void loadCustomPersistedData(@NotNull CompoundTag tag) { super.loadCustomPersistedData(tag); if (tag.hasUUID("OwnerTeamUUID")) ownerTeamUUID = tag.getUUID("OwnerTeamUUID"); }

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

    @Override protected void handleUniqueRegistration(UniqueMultiblockSavedData d, UUID o, String m, String dim, BlockPos p) {}
    @Override protected void handleUniqueRemoval(UniqueMultiblockSavedData d, UUID o, String m, String dim, BlockPos p) {}

    public static class BatteryMatchWrapper {
        public final ITeslaBattery partType;
        public int amount;
        public BatteryMatchWrapper(ITeslaBattery type) { this.partType = type; }
        public BatteryMatchWrapper increment() { amount++; return this; }
    }
}