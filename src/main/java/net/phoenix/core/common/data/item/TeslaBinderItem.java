package net.phoenix.core.common.data.item;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class TeslaBinderItem extends ComponentItem
                             implements IItemUIFactory, IInteractionItem, IAddInformation {

    public TeslaBinderItem(Properties properties) {
        super(properties);
    }

    // --- CRASH FIX ---
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        // Prevents the StackOverflowError from the crash report
        return false;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        MetaMachine machine = MetaMachine.getMachine(context.getLevel(), context.getClickedPos());
        if (machine instanceof IDataStickInteractable interactable) {
            if (player.isShiftKeyDown()) {
                return interactable.onDataStickShiftUse(player, itemStack);
            } else {
                return interactable.onDataStickUse(player, itemStack);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // Shift + Right Click on block: Bind to player frequency
        if (player.isShiftKeyDown()) {
            if (!context.getLevel().isClientSide) bindToPlayer(player, context.getItemInHand());
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        // Normal Right Click on block:
        // If it's a Tesla Hatch, onItemUseFirst (IDataStickInteractable) handles it.
        // Otherwise, we want to allow the 'use' method to open the UI.
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 1. Shift + Right Click Air: Bind to player frequency
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) bindToPlayer(player, stack);
            return InteractionResultHolder.success(stack);
        }

        // 2. Server-side Visual Ping
        if (!level.isClientSide && level instanceof ServerLevel server) {
            if (stack.hasTag() && stack.getTag().contains("TargetTeam")) {
                UUID team = stack.getTag().getUUID("TargetTeam");
                var endpoints = TeslaTeamEnergyData.get(server).getEndpoints(team);

                for (BlockPos pos : endpoints) {
                    // Spawn particles at every hatch in the network so they glow through walls
                    server.sendParticles(ParticleTypes.GLOW,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            10, 0.5, 0.5, 0.5, 0.1);
                }

                if (!endpoints.isEmpty()) {
                    player.sendSystemMessage(Component.literal("Pinging " + endpoints.size() + " Tesla endpoints...")
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }
        }

        // 3. Open UI
        return IItemUIFactory.super.use(item, level, player, hand);
    }

    private void bindToPlayer(Player player, ItemStack stack) {
        UUID uuid = player.getUUID();
        var tag = stack.getOrCreateTag();
        tag.putUUID("TargetTeam", uuid);
        tag.putString("OwnerName", player.getName().getString());
        tag.putString("TeamName", player.getName().getString() + "'s Network");

        if (player.level() instanceof ServerLevel server) {
            TeslaTeamEnergyData.get(server).getOrCreate(uuid);
            server.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.1, player.getZ(), 20, 0.2, 0.2,
                    0.2, 0.02);
            player.playSound(SoundEvents.PLAYER_LEVELUP, 0.5f, 1.5f);
            player.sendSystemMessage(
                    Component.literal("Tesla frequency set to personal network.").withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("TargetTeam")) {
            return Component.literal("Tesla Binder (Bound)").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC);
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.hasTag() && stack.getTag().contains("OwnerName")) {
            int color = getAnimatedColor(0xA330FF, 0xFF66CC, 2000);

            // "Bound to Player: PlayerName" (Always animated)
            tooltip.add(Component.literal("Bound to Player: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(stack.getTag().getString("OwnerName"))
                            .withStyle(Style.EMPTY.withColor(color))));

            // "Bound to Team: Network Name" (Always animated)
            if (stack.getTag().contains("TeamName")) {
                tooltip.add(Component.literal("Bound to Team: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(stack.getTag().getString("TeamName"))
                                .withStyle(Style.EMPTY.withColor(color))));
            }
        } else {
            tooltip.add(Component.literal("Shift + Right-Click to bind to your frequency.")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private int getAnimatedColor(int color1, int color2, int duration) {
        float time = (System.currentTimeMillis() % duration) / (float) duration;
        float phase = (float) Math.sin(time * 2 * Math.PI) * 0.5f + 0.5f;
        int r = (int) (((color1 >> 16) & 0xFF) + (((color2 >> 16) & 0xFF) - ((color1 >> 16) & 0xFF)) * phase);
        int g = (int) (((color1 >> 8) & 0xFF) + (((color2 >> 8) & 0xFF) - ((color1 >> 8) & 0xFF)) * phase);
        int b = (int) ((color1 & 0xFF) + ((color2 & 0xFF) - (color1 & 0xFF)) * phase);
        return (r << 16) | (g << 8) | b;
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        return new ModularUI(240, 260, holder, player)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(10, 8,
                        Component.literal("Tesla Network Monitor")
                                .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE)))

                // ---- MAIN CONTENT ----
                .widget(new ComponentPanelWidget(10, 28, textList -> {
                    ItemStack stack = holder.getHeld();

                    if (!stack.hasTag() || !stack.getTag().contains("TargetTeam")) {
                        textList.add(Component.literal("Binder not bound to a network.")
                                .withStyle(ChatFormatting.RED));
                        return;
                    }

                    UUID teamUUID = stack.getTag().getUUID("TargetTeam");

                    if (!(player.level() instanceof ServerLevel server)) {
                        textList.add(Component.literal("Syncing with Tesla Cloud...")
                                .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
                        return;
                    }

                    TeslaTeamEnergyData data = TeslaTeamEnergyData.get(server);
                    TeslaTeamEnergyData.TeamEnergy team = data.getOrCreate(teamUUID);

                    // ---- HEADER ----
                    textList.add(Component.literal("Network: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(
                                    stack.getTag().getString("TeamName"))
                                    .withStyle(ChatFormatting.AQUA)));

                    textList.add(Component.literal("Status: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(
                                    data.isOnline(teamUUID) ? "ONLINE" : "OFFLINE")
                                    .withStyle(data.isOnline(teamUUID) ? ChatFormatting.GREEN : ChatFormatting.RED)));

                    textList.add(Component.empty());

                    // ---- TOTALS ----
                    textList.add(Component.literal("Stored EU: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(
                                    FormattingUtil.formatNumbers(team.stored))
                                    .withStyle(ChatFormatting.GOLD)));

                    textList.add(Component.literal("Capacity: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(
                                    FormattingUtil.formatNumbers(team.capacity))
                                    .withStyle(ChatFormatting.YELLOW)));

                    textList.add(Component.empty());

                    // ---- HATCH LIST ----
                    var hatches = data.getHatches(teamUUID);
                    textList.add(Component.literal("Active Hatches: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.valueOf(hatches.size()))
                                    .withStyle(ChatFormatting.BLUE)));

                    if (hatches.isEmpty()) {
                        textList.add(Component.literal("No active hatches.")
                                .withStyle(ChatFormatting.DARK_GRAY));
                        return;
                    }

                    textList.add(Component.empty());

                    for (var hatch : hatches) {
                        boolean isOutput = hatch.output.signum() > 0;

                        MutableComponent line = Component.literal(isOutput ? "OUT " : "IN  ")
                                .withStyle(isOutput ? ChatFormatting.RED : ChatFormatting.GREEN)
                                .append(Component.literal(
                                        String.format(
                                                " @ %d %d %d",
                                                hatch.pos.getX(),
                                                hatch.pos.getY(),
                                                hatch.pos.getZ()))
                                        .withStyle(ChatFormatting.GRAY))
                                .append(Component.literal(
                                        " (" + FormattingUtil.formatNumbers(hatch.buffered) + " EU)")
                                        .withStyle(ChatFormatting.GOLD));

                        textList.add(line);
                    }
                }).setMaxWidthLimit(210))

                .widget(new ButtonWidget(
                        10, 230, 220, 18,
                        GuiTextures.BUTTON,
                        click -> {
                            if (!(player.level() instanceof ServerLevel server)) return;
                            ItemStack stack = holder.getHeld();
                            if (!stack.hasTag() || !stack.getTag().contains("TargetTeam")) return;

                            UUID teamUUID = stack.getTag().getUUID("TargetTeam");
                            var hatches = TeslaTeamEnergyData.get(server).getHatches(teamUUID);

                            BlockPos nearest = hatches.stream()
                                    .map(h -> h.pos)
                                    .min(Comparator.comparingDouble(
                                            p -> p.distSqr(player.blockPosition())))
                                    .orElse(null);

                            if (nearest != null) {
                                server.sendParticles(
                                        ParticleTypes.GLOW,
                                        nearest.getX() + 0.5,
                                        nearest.getY() + 0.5,
                                        nearest.getZ() + 0.5,
                                        30, 0.3, 0.3, 0.3, 0.05);
                            }
                        }))
                .widget(new LabelWidget(
                        20, 235,
                        Component.literal("Ping Nearest Hatch")
                                .withStyle(ChatFormatting.AQUA)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && isSelected && entity instanceof Player player) {
            // If the player has the UI open, LDLib handles the sync automatically
            // when the server-side text components change.
            // The SavedData is checked every time the UI 'ticks' on the server.
        }
    }
}
