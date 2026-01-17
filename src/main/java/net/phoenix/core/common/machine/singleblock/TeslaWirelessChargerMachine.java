package net.phoenix.core.common.machine.singleblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.phoenix.core.common.data.item.PhoenixItems;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;
import net.phoenix.core.utils.TeamUtils;
import top.theillusivec4.curios.api.CuriosApi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeslaWirelessChargerMachine extends TieredEnergyMachine implements IDataStickInteractable, IFancyUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            TeslaWirelessChargerMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    @Persisted @DescSynced
    private UUID boundTeam;

    @DescSynced
    private long lastTransferred = 0L; // Fixed: Use long for syncing

    private final List<UUID> playersInRange = new ArrayList<>();
    private TickableSubscription tickSubs;

    public TeslaWirelessChargerMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubs = subscribeServerTick(tickSubs, this::tickCharge);
        }
    }

    private void tickCharge() {
        if (!(getLevel() instanceof ServerLevel level) || boundTeam == null) return;

        // Performance: Only run once per 10 ticks (0.5 seconds)
        if (getOffsetTimer() % 10 != 0) return;

        TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);
        TeslaTeamEnergyData.TeamEnergy network = data.getOrCreate(boundTeam);

        // Stop if the soul network is dry
        if (network.stored.signum() <= 0) {
            this.lastTransferred = 0L;
            return;
        }

        // 1. Gather all players on the linked team
        List<Player> playersToCharge = new ArrayList<>();
        for (Player player : level.getServer().getPlayerList().getPlayers()) {
            if (TeamUtils.isPlayerOnTeam(player, boundTeam)) {
                playersToCharge.add(player);
            }
        }

        handleRangeNotifications(playersToCharge);

        long movedThisCycle = 0L;
        long voltage = GTValues.V[getTier()];

        // 4A is the standard output for GT Battery Chargers
        long ampsPerTick = 4;
        long ticksInBatch = 10;

        // Total EU we can push to a player in this 10-tick "batch"
        long maxBatchTransfer = voltage * ampsPerTick * ticksInBatch;

        for (Player player : playersToCharge) {
            // Collect all target inventories (Main + Curios)
            List<IItemHandler> inventories = new ArrayList<>();
            inventories.add(new net.minecraftforge.items.wrapper.PlayerMainInvWrapper(player.getInventory()));
            CuriosApi.getCuriosInventory(player).ifPresent(h -> inventories.add(h.getEquippedCurios()));

            for (IItemHandler handler : inventories) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack.isEmpty()) continue;

                    IElectricItem electric = GTCapabilityHelper.getElectricItem(stack);

                    // Item must be chargeable and equal or lower tier than the charger
                    if (electric != null && electric.chargeable() && electric.getTier() <= getTier()) {

                        // Safety: Ensure we don't overflow long if network is massive
                        long availableInNetwork = network.stored.min(BigInteger.valueOf(maxBatchTransfer)).longValue();

                        // Loop 10 times to simulate 1 tick's worth of 4A charging per iteration.
                        // This bypasses many items' internal 1A/tick limiters.
                        long itemAcceptedTotal = 0;
                        for (int t = 0; t < ticksInBatch; t++) {
                            long tickOffer = Math.min(availableInNetwork - itemAcceptedTotal, voltage * ampsPerTick);
                            if (tickOffer <= 0) break;

                            long accepted = electric.charge(tickOffer, getTier(), false, false);
                            if (accepted > 0) {
                                itemAcceptedTotal += accepted;
                            } else {
                                break; // Item is full or won't accept more
                            }
                        }

                        if (itemAcceptedTotal > 0) {
                            network.drain(BigInteger.valueOf(itemAcceptedTotal));
                            movedThisCycle += itemAcceptedTotal;
                        }
                    }
                }
            }
        }

        // 2. Sync Stats
        // Divide by 10 so the UI displays the average EU/tick rate over the last second.
        this.lastTransferred = movedThisCycle / ticksInBatch;

        // 3. Mark for Save
        if (movedThisCycle > 0) {
            data.setDirty();
        }
    }

    private void handleRangeNotifications(List<Player> nearby) {
        List<UUID> nearbyUUIDs = nearby.stream().map(Player::getUUID).toList();
        for (Player p : nearby) {
            if (!playersInRange.contains(p.getUUID())) {
                p.displayClientMessage(Component.literal("Tesla Field Connected").withStyle(ChatFormatting.AQUA), true);
                playersInRange.add(p.getUUID());
            }
        }
        playersInRange.removeIf(uuid -> {
            if (!nearbyUUIDs.contains(uuid)) {
                Player p = getLevel().getPlayerByUUID(uuid);
                if (p != null) p.displayClientMessage(Component.literal("Tesla Field Disconnected").withStyle(ChatFormatting.GRAY), true);
                return true;
            }
            return false;
        });
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack stick) {
        if (!stick.is(PhoenixItems.TESLA_BINDER.get())) return InteractionResult.PASS;
        if (!isRemote()) {
            this.boundTeam = stick.getOrCreateTag().getUUID("TargetTeam");
            player.sendSystemMessage(Component.literal("Charger Synchronized").withStyle(ChatFormatting.LIGHT_PURPLE));
            this.markDirty();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup root = new WidgetGroup(0, 0, 170, 80);
        root.setBackground(GuiTextures.BACKGROUND_INVERSE);
        root.addWidget(new ComponentPanelWidget(10, 10, this::addDisplayText));
        return root;
    }

    private void addDisplayText(List<Component> text) {
        text.add(Component.literal(GTValues.VNF[getTier()] + " Wireless Charger").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        text.add(Component.empty());

        if (boundTeam == null) {
            text.add(Component.literal("STATUS: ").append(Component.literal("UNBOUND").withStyle(ChatFormatting.RED)));
        } else {
            text.add(Component.literal("NETWORK: ").append(Component.literal(boundTeam.toString().substring(0,8)).withStyle(ChatFormatting.AQUA)));

            // Updated Range Text
            text.add(Component.literal("RANGE: ").append(Component.literal("Omnipresent (Global)").withStyle(ChatFormatting.LIGHT_PURPLE)));

            String rate = com.gregtechceu.gtceu.utils.FormattingUtil.formatNumbers(lastTransferred);
            text.add(Component.literal("OUTPUT: ").append(Component.literal(rate + " EU/t").withStyle(ChatFormatting.GREEN)));
        }
    }

    @Override public IGuiTexture getTabIcon() { return GuiTextures.CHARGER_OVERLAY; }
    @Override public Component getTitle() { return Component.literal("Tesla Field Generator"); }
}