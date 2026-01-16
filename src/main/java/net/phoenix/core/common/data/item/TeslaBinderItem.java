package net.phoenix.core.common.data.item;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.phoenix.core.client.renderer.TeslaHighlightRenderer;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

public class TeslaBinderItem extends ComponentItem
                             implements IItemUIFactory, IInteractionItem, IAddInformation {

    public TeslaBinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntitySwing(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return false;
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(@NotNull ItemStack itemStack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        MetaMachine machine = MetaMachine.getMachine(context.getLevel(), context.getClickedPos());

        // 1. HIGH PRIORITY: Tesla Multiblock Parts (Tower/Hatch)
        if (machine instanceof IDataStickInteractable interactable) {
            if (player.isShiftKeyDown()) {
                return interactable.onDataStickShiftUse(player, itemStack);
            } else {
                return interactable.onDataStickUse(player, itemStack);
            }
        }

        // 2. MEDIUM PRIORITY: Single-Block Soul Linking
        if (!context.getLevel().isClientSide && machine != null) {
            // GOATED CHECK: Ensure it's a Tiered Machine AND has an internal energy buffer.
            // This excludes Multiblock Controllers (like EBF) because they don't have this trait.
            if (machine instanceof TieredEnergyMachine tiered && tiered.energyContainer != null) {

                CompoundTag tag = itemStack.getTag();
                if (tag != null && tag.hasUUID("TargetTeam")) {
                    UUID teamUUID = tag.getUUID("TargetTeam");
                    ServerLevel level = (ServerLevel) context.getLevel();

                    TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);

                    // Use toggleSoulLink to handle both Adding and Removing in one click
                    boolean isNowLinked = data.toggleSoulLink(teamUUID, context.getClickedPos());

                    if (isNowLinked) {
                        player.sendSystemMessage(
                                Component.literal("Core Synchronized: Machine linked to soul frequency.")
                                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
                        level.playSound(null, context.getClickedPos(), SoundEvents.BEACON_POWER_SELECT,
                                SoundSource.PLAYERS, 1f, 1.5f);
                    } else {
                        player.sendSystemMessage(
                                Component.literal("Connection Severed: Machine removed from soul network.")
                                        .withStyle(ChatFormatting.GRAY));
                        level.playSound(null, context.getClickedPos(), SoundEvents.BEACON_DEACTIVATE,
                                SoundSource.PLAYERS, 1f, 0.8f);
                    }
                    return InteractionResult.SUCCESS;

                } else {
                    player.sendSystemMessage(
                            Component.literal("Binder is not initialized. Shift-Right Click the air first.")
                                    .withStyle(ChatFormatting.RED));
                    return InteractionResult.FAIL;
                }
            } else {
                // Feedback if the player tries to link a Multiblock Controller or non-electric block
                player.sendSystemMessage(Component.literal("Invalid Target: Machine has no internal soul-buffer.")
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // 3. LOW PRIORITY: Shift + Right Click on standard blocks to bind the tool to the player
        if (player.isShiftKeyDown()) {
            if (!context.getLevel().isClientSide) {
                bindToPlayer(player, context.getItemInHand());
                context.getLevel().playSound(null, context.getClickedPos(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6f, 1.2f);
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player,
                                                           @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                bindToPlayer(player, stack);
                level.playSound(null, player.blockPosition(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6f, 1.2f);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.hasUUID("TargetTeam")) {
                level.playSound(null, player.blockPosition(),
                        SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 0.4f, 1.5f);
            }
        }

        return super.use(level, player, hand);
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
    public @NotNull Component getName(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("TargetTeam")) {
            return Component.literal("Tesla Binder (Bound)").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC);
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {
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
        ListTag hatchListData = holder.getHeld().getOrCreateTag().getList("HatchData", 10);

        // Dynamic Height Calculation
        int listHeight = Math.min(130, Math.max(40, hatchListData.size() * 18 + 4));
        // Added 10px buffer for the new header group
        int windowHeight = 95 + listHeight;

        ModularUI ui = new ModularUI(230, windowHeight, holder, player).background(GuiTextures.BACKGROUND);
        WidgetGroup mainLayer = new WidgetGroup(0, 0, 230, windowHeight);
        WidgetGroup detailLayer = new WidgetGroup(0, 0, 230, windowHeight);
        detailLayer.setVisible(false);

        // --- DETAIL VIEW ---
        Consumer<CompoundTag> openDetail = (hTag) -> {
            detailLayer.clearAllWidgets();
            // Header in Detail View
            WidgetGroup detailHeader = new WidgetGroup(5, 5, 220, 20);
            detailLayer.addWidget(detailHeader);

            detailHeader.addWidget(new ButtonWidget(0, 0, 18, 18, GuiTextures.BUTTON, c -> {
                detailLayer.setVisible(false);
                mainLayer.setVisible(true);
            }).setButtonTexture(GuiTextures.BUTTON_LEFT));

            detailHeader
                    .addWidget(new LabelWidget(25, 4, "Hatch Details").setTextColor(ChatFormatting.GOLD.getColor()));

            WidgetGroup detailDisplay = new WidgetGroup(5, 30, 220, 55);
            detailDisplay.setBackground(GuiTextures.DISPLAY);
            detailLayer.addWidget(detailDisplay);

            detailLayer.addWidget(new ComponentPanelWidget(12, 35, list -> {
                list.add(Component.literal("Buffer: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(hTag.getString("buf")).withStyle(ChatFormatting.YELLOW)));
                list.add(Component.literal("Flow: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(hTag.getString("transfer")).withStyle(ChatFormatting.AQUA)));
                list.add(hTag.getBoolean("isOut") ? Component.literal("Mode: IN").withStyle(ChatFormatting.RED) :
                        Component.literal("Mode: OUT").withStyle(ChatFormatting.GREEN));
            }));

            mainLayer.setVisible(false);
            detailLayer.setVisible(true);
        };

        // --- GROUPED HEADER ---
        // Title Label (Outside the box)
        // 0x8F00FF is the Hex code for Electric Violet
        mainLayer.addWidget(new LabelWidget(8, 6, "Tesla Network Management")
                .setTextColor(0xB000FF));

        // Header Background Group (Lines 1-3)
        WidgetGroup headerGroup = new WidgetGroup(5, 18, 220, 48);
        headerGroup.setBackground(GuiTextures.DISPLAY); // The dark inset background
        mainLayer.addWidget(headerGroup);

        headerGroup.addWidget(new ComponentPanelWidget(5, 4, textList -> {
            CompoundTag tag = holder.getHeld().getOrCreateTag();
            textList.add(Component.literal("Network: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(tag.getString("TeamName")).withStyle(ChatFormatting.AQUA)));
            textList.add(Component.literal("Stored: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(tag.getString("StoredEU") + " EU").withStyle(ChatFormatting.GOLD)));
            textList.add(Component.literal("Capacity: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(tag.getString("CapacityEU") + " EU").withStyle(ChatFormatting.YELLOW)));
        }));

        // --- THE LIST (Now starting at Y=70 to accommodate the box padding) ---
        WidgetGroup listDisplay = new WidgetGroup(5, 70, 220, listHeight);
        listDisplay.setBackground(GuiTextures.DISPLAY);
        mainLayer.addWidget(listDisplay);

        DraggableScrollableWidgetGroup listGroup = new DraggableScrollableWidgetGroup(7, 72, 216, listHeight - 4);
        mainLayer.addWidget(listGroup);

        int currentY = 0;
        for (int i = 0; i < hatchListData.size(); i++) {
            CompoundTag hTag = hatchListData.getCompound(i);
            BlockPos hPos = BlockPos.of(hTag.getLong("pos"));
            boolean isOut = hTag.getBoolean("isOut");
            double dist = Math.sqrt(player.blockPosition().distSqr(hPos));

            WidgetGroup row = new WidgetGroup(0, currentY, 210, 18);
            row.addWidget(new ButtonWidget(0, 0, 190, 18, GuiTextures.BUTTON, c -> openDetail.accept(hTag)));
            row.addWidget(new LabelWidget(4, 4, () -> (isOut ? "§a[I]§r " : "§c[O]§r ") + hPos.getX() + "," +
                    hPos.getZ() + " §7" + (int) dist + "m"));

            row.addWidget(new ButtonWidget(192, 0, 18, 18, GuiTextures.BUTTON, c -> {
                if (player.level().isClientSide) TeslaHighlightRenderer.highlight(hPos, 200);
                player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.3f, 1.5f);
            }));
            row.addWidget(new ImageWidget(193, 1, 16, 16, GuiTextures.IO_CONFIG_COVER_SETTINGS));

            listGroup.addWidget(row);
            currentY += 19;
        }

        ui.widget(mainLayer).widget(detailLayer);
        return ui;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slotId,
                              boolean isSelected) {
        // Only process on server and when the item is being held/selected
        if (!level.isClientSide && isSelected && entity instanceof Player player) {
            CompoundTag tag = stack.getOrCreateTag();

            // Ensure the binder has a frequency/team assigned
            if (tag.contains("TargetTeam")) {
                UUID teamUUID = tag.getUUID("TargetTeam");
                TeslaTeamEnergyData data = TeslaTeamEnergyData.get((ServerLevel) level);
                TeslaTeamEnergyData.TeamEnergy team = data.getOrCreate(teamUUID);

                // 1. UPDATE NETWORK GLOBALS
                // Stored and Capacity are BigInteger, so we format them for the UI scroller/labels
                tag.putString("StoredEU", FormattingUtil.formatNumbers(team.stored));
                tag.putString("CapacityEU", FormattingUtil.formatNumbers(team.capacity));

                // Helpful for UI header: "PlayerName's Network"
                tag.putString("TeamName", player.getName().getString() + "'s Network");

                // 2. UPDATE HATCH LIST DATA
                ListTag hatchList = new ListTag();

                // data.getHatches() now correctly populates HatchInfo.isPhysicalOutput
                // from the hatchIsOutput map in TeslaTeamEnergyData
                for (TeslaTeamEnergyData.HatchInfo hatch : data.getHatches(teamUUID)) {
                    CompoundTag hTag = new CompoundTag();

                    // Position for the "Highlight" button and distance calculation
                    hTag.putLong("pos", hatch.pos.asLong());

                    // Logic Fix: Use the physical state stored in HatchInfo
                    // This ensures [I] or [O] stays correct even when idle
                    boolean isOut = hatch.isPhysicalOutput;
                    hTag.putBoolean("isOut", isOut);

                    // Transfer rate: Pull from the live activity maps
                    // We use the boolean to know which map to check
                    BigInteger transfer = isOut ?
                            team.energyOutput.getOrDefault(hatch.pos, BigInteger.ZERO) :
                            team.energyInput.getOrDefault(hatch.pos, BigInteger.ZERO);

                    hTag.putString("transfer", FormattingUtil.formatNumbers(transfer.longValue()));

                    // Buffered energy inside the specific hatch
                    hTag.putString("buf", FormattingUtil.formatNumbers(hatch.buffered));

                    hatchList.add(hTag);
                }

                // Push the updated list to the item NBT
                tag.put("HatchData", hatchList);
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        // If the slot changed, do the animation.
        // If the item itself changed (e.g., from binder to pickaxe), do the animation.
        // If only NBT changed, return false (stop the bobbing).
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }
}
