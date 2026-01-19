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
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine.State;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
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

public class TeslaWirelessChargerMachine extends TieredEnergyMachine
                                         implements IDataStickInteractable, IFancyUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            TeslaWirelessChargerMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private UUID boundTeam;

    @DescSynced
    private long lastTransferred = 0L;

    @DescSynced
    @RequireRerender
    private State state = State.IDLE;

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

    private void changeState(State newState) {
        if (this.state != newState) {
            this.state = newState;
            setRenderState(getRenderState().setValue(GTMachineModelProperties.CHARGER_STATE, newState));
        }
    }

    private void tickCharge() {
        if (!(getLevel() instanceof ServerLevel level) || boundTeam == null) {
            changeState(State.IDLE);
            return;
        }

        if (getOffsetTimer() % 10 != 0) return;

        TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);
        TeslaTeamEnergyData.TeamEnergy network = data.getOrCreate(boundTeam);

        if (network.stored.signum() <= 0) {
            this.lastTransferred = 0L;
            changeState(State.IDLE);
            return;
        }

        List<Player> playersToCharge = new ArrayList<>();
        for (Player player : level.getServer().getPlayerList().getPlayers()) {
            if (TeamUtils.isPlayerOnTeam(player, boundTeam)) {
                playersToCharge.add(player);
            }
        }

        handleRangeNotifications(playersToCharge);

        long movedThisCycle = 0L;
        long voltage = GTValues.V[getTier()];
        long ampsPerTick = 4;
        long ticksInBatch = 10;
        int totalPulses = (int) (ampsPerTick * ticksInBatch);

        for (Player player : playersToCharge) {
            List<IItemHandler> inventories = new ArrayList<>();
            inventories.add(new net.minecraftforge.items.wrapper.PlayerMainInvWrapper(player.getInventory()));
            CuriosApi.getCuriosInventory(player).ifPresent(h -> inventories.add(h.getEquippedCurios()));

            for (IItemHandler handler : inventories) {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (stack.isEmpty()) continue;

                    IElectricItem electric = GTCapabilityHelper.getElectricItem(stack);
                    if (electric != null && electric.chargeable() && electric.getTier() <= getTier()) {
                        long itemAcceptedTotal = 0;
                        for (int p = 0; p < totalPulses; p++) {
                            long networkCanProvide = network.stored.min(BigInteger.valueOf(voltage)).longValue();
                            long itemRemainingLimit = (voltage * totalPulses) - itemAcceptedTotal;
                            long pulseOffer = Math.min(networkCanProvide, Math.min(voltage, itemRemainingLimit));

                            if (pulseOffer <= 0) break;

                            long accepted = electric.charge(pulseOffer, getTier(), false, false);
                            if (accepted > 0) {
                                itemAcceptedTotal += accepted;
                                network.drain(BigInteger.valueOf(accepted));
                                movedThisCycle += accepted;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }

        this.lastTransferred = movedThisCycle / ticksInBatch;

        // Render Logic: RUNNING if energy moves, FINISHED if players present but full, IDLE otherwise
        if (movedThisCycle > 0) {
            data.setDirty();
            changeState(State.RUNNING);
        } else if (!playersToCharge.isEmpty()) {
            changeState(State.FINISHED);
        } else {
            changeState(State.IDLE);
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
                if (p != null)
                    p.displayClientMessage(Component.literal("Tesla Field Disconnected").withStyle(ChatFormatting.GRAY),
                            true);
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
        text.add(Component.literal(GTValues.VNF[getTier()] + " Wireless Charger").withStyle(ChatFormatting.GOLD,
                ChatFormatting.BOLD));
        text.add(Component.empty());

        if (boundTeam == null) {
            text.add(Component.literal("STATUS: ").append(Component.literal("UNBOUND").withStyle(ChatFormatting.RED)));
        } else {
            text.add(Component.literal("NETWORK: ")
                    .append(Component.literal(boundTeam.toString().substring(0, 8)).withStyle(ChatFormatting.AQUA)));
            text.add(Component.literal("RANGE: ")
                    .append(Component.literal("Omnipresent (Global)").withStyle(ChatFormatting.LIGHT_PURPLE)));

            String rate = com.gregtechceu.gtceu.utils.FormattingUtil.formatNumbers(lastTransferred);
            text.add(Component.literal("OUTPUT: ")
                    .append(Component.literal(rate + " EU/t").withStyle(ChatFormatting.GREEN)));
        }
    }

    @Override
    public IGuiTexture getTabIcon() {
        return GuiTextures.CHARGER_OVERLAY;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Tesla Field Generator");
    }
}
