package net.phoenix.core.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.ChatFormatting;
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
import net.phoenix.core.saveddata.TeslaTeamEnergyData;
import net.phoenix.core.saveddata.UniqueMultiblockSavedData;
import net.phoenix.core.utils.TeamUtils;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;

public class TeslaTowerMachine extends UniqueWorkableElectricMultiblockMachine
        implements IFancyUIMachine, IDisplayUIMachine, IDataStickInteractable, IEnergyInfoProvider {

    public static final String MULTIBLOCK_TYPE = "tesla_tower";
    public static final String TTB_BATTERY_HEADER = "TeslaTowerBattery_";

    private EnergyContainerList inputHatches;
    private EnergyContainerList outputHatches;

    private UUID ownerTeamUUID;
    private boolean isDuplicate;

    private final ConditionalSubscriptionHandler energyTransferSubscription;



    private BigInteger netInLastSec = BigInteger.ZERO;
    private BigInteger netOutLastSec = BigInteger.ZERO;
    private BigInteger totalInputPerTick = BigInteger.ZERO;
    private BigInteger totalOutputPerTick = BigInteger.ZERO;

    private BigInteger batteryCapacity = BigInteger.ZERO;
    private BigInteger batteryStored = BigInteger.ZERO;
    private int batteryTier = 9;
    private boolean isOnline;

    public TeslaTowerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.energyTransferSubscription = new ConditionalSubscriptionHandler(
                this, this::tickEnergyTransfer, this::isFormed);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && isFormed()) {
            energyTransferSubscription.updateSubscription();
        }
    }


    @Override
    public EnergyInfo getEnergyInfo() {
        if (!(getLevel() instanceof ServerLevel sl) || ownerTeamUUID == null) {
            return new EnergyInfo(BigInteger.ZERO, BigInteger.ZERO);
        }
        TeslaEnergyBank bank = TeslaTeamEnergyData.get(sl).getOrCreateEnergyBank(ownerTeamUUID);
        return new EnergyInfo(bank.getCapacity(), bank.getStored());
    }

    @Override public long getInputPerSec() { return 0; }
    @Override public long getOutputPerSec() { return 0; }
    @Override public boolean isOneProbeHidden() { return false; }
    @Override public boolean supportsBigIntEnergyValues() { return true; }


    private void refreshDisplayFields() {
        if (getLevel() instanceof ServerLevel sl && ownerTeamUUID != null) {
            TeslaTeamEnergyData data = TeslaTeamEnergyData.get(sl);
            TeslaEnergyBank bank = data.getOrCreateEnergyBank(ownerTeamUUID);

            batteryStored = bank.getStored();
            batteryCapacity = bank.getCapacity();
            batteryTier = bank.getTier();
            isOnline = data.isNetworkOnline(ownerTeamUUID);

            self().markDirty();
        }
    }

    @Override
    public @NotNull Widget createUIWidget() {
        WidgetGroup root = new WidgetGroup(0, 0, 190, 125);
        DraggableScrollableWidgetGroup panel =
                new DraggableScrollableWidgetGroup(4, 4, 182, 117)
                        .setBackground(getScreenTexture());

        panel.addWidget(new LabelWidget(4, 5,
                self().getBlockState().getBlock().getDescriptionId()));
        panel.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .setMaxWidthLimit(120));
        panel.addWidget(new ImageWidget(140, 50, 32, 32,
                () -> getTeslaTierTexture(batteryTier)));

        root.addWidget(panel);
        root.setBackground(com.gregtechceu.gtceu.api.gui.GuiTextures.BACKGROUND_INVERSE);
        return root;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> text) {
        super.addDisplayText(text);

        if (!isFormed() || ownerTeamUUID == null) {
            text.add(Component.literal("Tesla Network: Inactive")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        Style gold = Style.EMPTY.withColor(ChatFormatting.GOLD);
        Style aqua = Style.EMPTY.withColor(ChatFormatting.AQUA);
        Style gray = Style.EMPTY.withColor(ChatFormatting.GRAY);

        text.add(Component.literal("Tesla Network: ")
                .append(Component.literal(isOnline ? "ONLINE" : "OFFLINE")
                        .withStyle(isOnline ? ChatFormatting.GREEN : ChatFormatting.RED)));

        text.add(Component.literal("Team: ")
                .append(Component.literal(getTeamNameLabel()).withStyle(aqua)));

        text.add(Component.literal("Stored: ")
                .append(Component.literal(FormattingUtil.formatNumbers(batteryStored)).withStyle(gold))
                .append(Component.literal(" / ").withStyle(gray))
                .append(Component.literal(FormattingUtil.formatNumbers(batteryCapacity)).withStyle(gold)));

        text.add(Component.literal("IO: ")
                .append(Component.literal("+ " + FormattingUtil.formatNumbers(totalInputPerTick) + " EU/t")
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" | ").withStyle(gray))
                .append(Component.literal("- " + FormattingUtil.formatNumbers(totalOutputPerTick) + " EU/t")
                        .withStyle(ChatFormatting.RED)));

        text.add(Component.literal("Battery Tier: ")
                .append(Component.literal(GTValues.VN[batteryTier]).withStyle(aqua)));
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

    private String getTeamNameLabel() {
        return (getLevel() instanceof ServerLevel sl && ownerTeamUUID != null)
                ? TeamUtils.getTeamName(ownerTeamUUID)
                : "Unknown";
    }


    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack stack) {
        if (!stack.is(PhoenixItems.TESLA_BINDER.get())) return InteractionResult.PASS;

        if (!getLevel().isClientSide && ownerTeamUUID != null) {
            stack.getOrCreateTag().putUUID("TargetTeam", ownerTeamUUID);
            player.sendSystemMessage(Component.literal("Tesla Frequency Copied.")
                    .withStyle(ChatFormatting.GREEN));
        }
        return InteractionResult.SUCCESS;
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
            if (existing != null &&
                    (!existing.getDimension().equals(sl.dimension().location().toString())
                            || !existing.getPos().equals(getPos()))) {

                isDuplicate = true;
                recipeLogic.setStatus(RecipeLogic.Status.SUSPEND);
                return;
            }
        }

        collectHatches();

        TeslaEnergyBank bank =
                TeslaTeamEnergyData.get(sl).getOrCreateEnergyBank(ownerTeamUUID);
        bank.rebuild(collectBatteries());
        TeslaTeamEnergyData.get(sl).setDirty();

        unique.addMultiblock(ownerTeamUUID, MULTIBLOCK_TYPE,
                sl.dimension().location().toString(), getPos());

        isDuplicate = false;
        energyTransferSubscription.updateSubscription();
        refreshDisplayFields();
    }

    @Override
    public void onStructureInvalid() {
        energyTransferSubscription.updateSubscription();
        inputHatches = null;
        outputHatches = null;

        netInLastSec = BigInteger.ZERO;
        netOutLastSec = BigInteger.ZERO;
        totalInputPerTick = BigInteger.ZERO;
        totalOutputPerTick = BigInteger.ZERO;

        isDuplicate = false;
        isOnline = false;

        refreshDisplayFields();
        super.onStructureInvalid();
    }

    private void ensureOwnerTeamUUID() {
        if (!(getLevel() instanceof ServerLevel sl)) return;

        UUID owner = getOwnerUUID();
        if (owner != null) {
            ServerPlayer player = sl.getServer().getPlayerList().getPlayer(owner);
            if (player != null) {
                ownerTeamUUID = TeamUtils.getTeamIdOrPlayerFallback(player);
                self().markDirty();
            }
        }
    }

    private void collectHatches() {
        List<com.gregtechceu.gtceu.api.capability.IEnergyContainer> in = new ArrayList<>();
        List<com.gregtechceu.gtceu.api.capability.IEnergyContainer> out = new ArrayList<>();

        for (var part : getParts()) {
            if (part instanceof TeslaEnergyHatchPartMachine hatch) {
                if (hatch.energyContainer.getHandlerIO()
                        .support(com.gregtechceu.gtceu.api.capability.recipe.IO.IN))
                    in.add(hatch.energyContainer);
                else out.add(hatch.energyContainer);
            }
        }
        inputHatches = new EnergyContainerList(in);
        outputHatches = new EnergyContainerList(out);
    }

    private List<ITeslaBattery> collectBatteries() {
        List<ITeslaBattery> batteries = new ArrayList<>();

        for (Map.Entry<String, Object> entry :
                getMultiblockState().getMatchContext().entrySet()) {

            if (entry.getKey().startsWith(TTB_BATTERY_HEADER)
                    && entry.getValue() instanceof BatteryMatchWrapper wrapper) {

                for (int i = 0; i < wrapper.amount; i++) {
                    batteries.add(wrapper.partType);
                }
            }
        }
        return batteries;
    }

    protected void tickEnergyTransfer() {
        if (getLevel().isClientSide || isDuplicate || ownerTeamUUID == null) return;
        if (!(getLevel() instanceof ServerLevel sl)) return;

        TeslaEnergyBank bank =
                TeslaTeamEnergyData.get(sl).getOrCreateEnergyBank(ownerTeamUUID);

        if (getOffsetTimer() % 20 == 0) {
            totalInputPerTick = netInLastSec.divide(BigInteger.valueOf(20));
            totalOutputPerTick = netOutLastSec.divide(BigInteger.valueOf(20));
            netInLastSec = BigInteger.ZERO;
            netOutLastSec = BigInteger.ZERO;

            recipeLogic.setStatus(
                    bank.getStored().signum() > 0
                            ? RecipeLogic.Status.WORKING
                            : RecipeLogic.Status.IDLE);

            refreshDisplayFields();
        }

        if (!isWorkingEnabled()) return;

        if (inputHatches != null && inputHatches.getEnergyStored() > 0) {
            long move = Math.min(
                    inputHatches.getEnergyStored(),
                    bank.getMaxInput()
                            .min(bank.getCapacity().subtract(bank.getStored()))
                            .longValue());

            if (move > 0) {
                bank.fill(move);
                inputHatches.changeEnergy(-move);
                netInLastSec = netInLastSec.add(BigInteger.valueOf(move));
            }
        }

        if (outputHatches != null && bank.getStored().signum() > 0) {
            long move = Math.min(
                    bank.getStored().min(bank.getMaxOutput()).longValue(),
                    outputHatches.getEnergyCapacity() - outputHatches.getEnergyStored());

            if (move > 0) {
                bank.drain(move);
                outputHatches.changeEnergy(move);
                netOutLastSec = netOutLastSec.add(BigInteger.valueOf(move));
            }
        }

        TeslaTeamEnergyData.get(sl).setDirty();
    }



    public static class BatteryMatchWrapper {
        public final ITeslaBattery partType;
        public int amount;

        public BatteryMatchWrapper(ITeslaBattery type) {
            this.partType = type;
        }

        public BatteryMatchWrapper increment() {
            amount++;
            return this;
        }
    }
}
