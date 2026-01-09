package net.phoenix.core.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.phoenix.core.api.machine.trait.ITeslaBattery;
import net.phoenix.core.common.machine.multiblock.UniqueWorkableElectricMultiblockMachine;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;
import net.phoenix.core.saveddata.UniqueMultiblockData;
import net.phoenix.core.saveddata.UniqueMultiblockSavedData;
import net.phoenix.core.utils.TeamUtils;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeslaTowerMachine extends UniqueWorkableElectricMultiblockMachine
                               implements IEnergyInfoProvider, IFancyUIMachine, IDisplayUIMachine {

    public static final String MULTIBLOCK_TYPE = "tesla_tower";
    public static final String TTB_BATTERY_HEADER = "TTBattery_";

    private UUID ownerTeamUUID;
    private final ConditionalSubscriptionHandler energyTransferSubscription;

    public TeslaTowerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);

        this.energyTransferSubscription = new ConditionalSubscriptionHandler(this,
                this::tickEnergyTransfer, this::isSubscriptionActive);
    }



    private void tickEnergyTransfer() {
        if (isRemote() || isDuplicate || ownerTeamUUID == null) return;


        if (isActive() && getLevel() instanceof ServerLevel serverLevel) {
            setNetworkStatus(true);


            long internalStored = energyContainer.getEnergyStored();
            if (internalStored > 0) {
                TeslaTeamEnergyData teamData = TeslaTeamEnergyData.get(serverLevel);
                TeslaEnergyBank bank = teamData.getOrCreateEnergyBank(ownerTeamUUID);

                long space = bank.getCapacity() - bank.getStored();
                if (space > 0) {
                    long toMove = Math.min(internalStored, space);
                    toMove = Math.min(toMove, bank.getMaxInput());

                    if (toMove > 0) {
                        bank.deposit(toMove);
                        energyContainer.removeEnergy(toMove);
                        teamData.setDirty();
                    }
                }
            }
        } else {
            setNetworkStatus(false);
        }
    }

    private void setNetworkStatus(boolean online) {
        if (ownerTeamUUID != null && getLevel() instanceof ServerLevel serverLevel) {

            TeslaTeamEnergyData.get(serverLevel).setNetworkOnline(ownerTeamUUID, online);
        }
    }

    private Boolean isSubscriptionActive() {
        return isFormed();
    }

    /* ------------------------------------------------------------ */
    /* LIFECYCLE & STRUCTURE */
    /* ------------------------------------------------------------ */

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;

        ensureOwnerTeamUUID();
        if (ownerTeamUUID == null) {
            this.isDuplicate = true;
            return;
        }

        UniqueMultiblockSavedData uniqueSaved = UniqueMultiblockSavedData.getOrCreate(serverLevel);
        if (uniqueSaved.hasData(ownerTeamUUID, MULTIBLOCK_TYPE)) {
            UniqueMultiblockData.UniqueMultiblockEntry existing = uniqueSaved.getEntry(ownerTeamUUID, MULTIBLOCK_TYPE);
            if (existing == null || !existing.getDimension().equals(getLevel().dimension().location().toString()) ||
                    !existing.getPos().equals(getPos())) {
                this.isDuplicate = true;
                recipeLogic.setStatus(RecipeLogic.Status.SUSPEND);
                return;
            }
        }


        List<ITeslaBattery> batteries = collectBatteries();
        TeslaTeamEnergyData teamData = TeslaTeamEnergyData.get(serverLevel);
        TeslaEnergyBank bank = teamData.getOrCreateEnergyBank(ownerTeamUUID);

        long totalCap = 0, totalIn = 0, totalOut = 0;
        for (ITeslaBattery bat : batteries) {
            totalCap += bat.getCapacity();
            totalIn += bat.getMaxInput();
            totalOut += bat.getMaxOutput();
        }

        bank.setCapacity(totalCap);
        bank.setMaxInput(totalIn);
        bank.setMaxOutput(totalOut);
        teamData.setDirty();

        uniqueSaved.addMultiblock(ownerTeamUUID, MULTIBLOCK_TYPE, getLevel().dimension().location().toString(),
                getPos());
        this.isDuplicate = false;
        energyTransferSubscription.updateSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        setNetworkStatus(false);
        energyTransferSubscription.unsubscribe();

        if (!(getLevel() instanceof ServerLevel serverLevel) || ownerTeamUUID == null) return;

        TeslaTeamEnergyData teamData = TeslaTeamEnergyData.get(serverLevel);
        TeslaEnergyBank bank = teamData.getEnergyBank(ownerTeamUUID);
        if (bank != null) {
            bank.setCapacity(0);
            teamData.setDirty();
        }

        UniqueMultiblockSavedData.getOrCreate(serverLevel).removeMultiblock(ownerTeamUUID, MULTIBLOCK_TYPE,
                getLevel().dimension().location().toString(), getPos());
    }

    @Override
    public void onUnload() {
        super.onUnload();
        setNetworkStatus(false);
    }

    /* ------------------------------------------------------------ */
    /* ENERGY INFO & PERSISTENCE */
    /* ------------------------------------------------------------ */

    @Override
    public EnergyInfo getEnergyInfo() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return new EnergyInfo(BigInteger.ZERO, BigInteger.ZERO);
        ensureOwnerTeamUUID();
        if (ownerTeamUUID == null) return new EnergyInfo(BigInteger.ZERO, BigInteger.ZERO);

        TeslaEnergyBank bank = TeslaTeamEnergyData.get(serverLevel).getEnergyBank(ownerTeamUUID);
        return bank != null ?
                new EnergyInfo(BigInteger.valueOf(bank.getStored()), BigInteger.valueOf(bank.getCapacity())) :
                new EnergyInfo(BigInteger.ZERO, BigInteger.ZERO);
    }

    private void ensureOwnerTeamUUID() {
        if (ownerTeamUUID != null || !(getLevel() instanceof ServerLevel serverLevel)) return;
        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID == null) ownerUUID = new UUID(0L, 0L);

        ServerPlayer sp = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
        if (sp != null) ownerTeamUUID = TeamUtils.getTeamIdOrPlayerFallback(sp);
    }

    private List<ITeslaBattery> collectBatteries() {
        List<ITeslaBattery> list = new ArrayList<>();
        for (Map.Entry<?, ?> entry : getMultiblockState().getMatchContext().entrySet()) {
            if (entry.getKey() instanceof String key && key.startsWith(TTB_BATTERY_HEADER) &&
                    entry.getValue() instanceof BatteryMatchWrapper wrapper) {
                for (int i = 0; i < wrapper.amount; i++) list.add(wrapper.partType);
            }
        }
        return list;
    }

    @Override
    public long getInputPerSec() {
        return 0;
    }

    @Override
    public long getOutputPerSec() {
        return 0;
    }

    @Override
    public boolean supportsBigIntEnergyValues() {
        return true;
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (ownerTeamUUID != null) tag.putUUID("OwnerTeamUUID", ownerTeamUUID);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (tag.hasUUID("OwnerTeamUUID")) ownerTeamUUID = tag.getUUID("OwnerTeamUUID");
    }

    @Override
    protected void handleUniqueRegistration(UniqueMultiblockSavedData d, UUID o, String m, String dim, BlockPos p) {}

    @Override
    protected void handleUniqueRemoval(UniqueMultiblockSavedData d, UUID o, String m, String dim, BlockPos p) {}

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
