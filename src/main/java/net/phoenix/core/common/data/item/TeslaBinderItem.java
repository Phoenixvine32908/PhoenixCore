package net.phoenix.core.common.data.item;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.phoenix.core.utils.TeamUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static net.phoenix.core.api.gui.PhoenixGuiTextures.TESLA_BACKGROUND;

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
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack,
                                                           @NotNull Player player,
                                                           @NotNull LivingEntity interactionTarget,
                                                           @NotNull InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(@NotNull ItemStack itemStack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        // Use the level from context safely
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        MetaMachine machine = MetaMachine.getMachine(level, clickedPos);

        // 1. Tesla Multiblock Parts (Tower/Hatch)
        if (machine instanceof IDataStickInteractable interactable) {
            return player.isShiftKeyDown() ?
                    interactable.onDataStickShiftUse(player, itemStack) :
                    interactable.onDataStickUse(player, itemStack);
        }

        // 2. Single-Block Soul Linking (Server Side only)
        if (!level.isClientSide && machine != null) {
            if (level instanceof ServerLevel serverLevel &&
                    machine instanceof TieredEnergyMachine tiered && tiered.energyContainer != null) {

                CompoundTag tag = itemStack.getOrCreateTag();
                if (tag.hasUUID("TargetTeam")) {
                    UUID teamUUID = tag.getUUID("TargetTeam");
                    TeslaTeamEnergyData data = TeslaTeamEnergyData.get(serverLevel);

                    // Toggle the link in the Global Saved Data
                    // Pass 'level' as the second argument
                    boolean isNowLinked = data.toggleSoulLink(teamUUID, level, clickedPos);

                    if (isNowLinked) {
                        player.sendSystemMessage(
                                Component.literal("Core Synchronized: Machine linked to soul frequency.")
                                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
                        serverLevel.playSound(null, clickedPos, SoundEvents.BEACON_POWER_SELECT,
                                SoundSource.PLAYERS, 1f, 1.5f);
                    } else {
                        // --- INSTANT UNLINK FIX ---
                        // Manually remove the machine from the Item's NBT cache so the UI updates instantly
                        if (tag.contains("MachineData")) {
                            ListTag machineList = tag.getList("MachineData", Tag.TAG_COMPOUND);
                            long targetPosLong = clickedPos.asLong();

                            for (int i = 0; i < machineList.size(); i++) {
                                if (machineList.getCompound(i).getLong("pos") == targetPosLong) {
                                    machineList.remove(i);
                                    break;
                                }
                            }
                        }

                        player.sendSystemMessage(
                                Component.literal("Connection Severed: Machine removed from soul network.")
                                        .withStyle(ChatFormatting.GRAY));
                        serverLevel.playSound(null, clickedPos, SoundEvents.BEACON_DEACTIVATE,
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
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            if (stack.getTag().contains("TargetTeam")) {
                return Component.literal("Tesla Binder (Bound)").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC);
            }
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {
        if (stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("OwnerName")) {
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

    private String formatTeslaValue(String valueStr, boolean forceScientific) {
        if (valueStr == null || valueStr.isEmpty()) return "0";
        try {
            String cleanValue = valueStr.replaceAll("[§][0-9a-fk-or]", "")

                    .replace(",", "").replace("+", "").replace("-", "").trim();

            double value = Double.parseDouble(cleanValue);

            if (value == 0) return "0";
            if (forceScientific && value >= 1000) {

                return String.format("%.1e", value);
            }
            if (value >= 1_000_000_000_000L) {

                return String.format("%.3e", value);
            }
            return String.format("%,.0f", value);
        } catch (NumberFormatException ignored) {
            return valueStr.replaceAll("[§][0-9a-fk-or]", "");
        }
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        ItemStack stack = holder.getHeld();
        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains("FilterMode")) tag.putInt("FilterMode", 0);
        int filterMode = tag.getInt("FilterMode");

        int windowWidth = 260;
        int windowHeight = 280;

        ModularUI ui = new ModularUI(windowWidth, windowHeight, holder, player).background(GuiTextures.BACKGROUND);
        WidgetGroup mainLayer = new WidgetGroup(0, 0, windowWidth, windowHeight);
        WidgetGroup detailLayer = new WidgetGroup(0, 0, windowWidth, windowHeight);
        detailLayer.setVisible(false);

        // --- SHARED DETAIL LOGIC ---
        Consumer<CompoundTag> openDetail = (hTag) -> {
            detailLayer.clearAllWidgets();
            detailLayer.addWidget(new ButtonWidget(5, 5, 18, 18, GuiTextures.BUTTON_LEFT, c -> {
                detailLayer.setVisible(false);
                mainLayer.setVisible(true);
            }));

            boolean isHatch = hTag.contains("isOut");
            String hardwareTitle;
            ChatFormatting titleColor;

            if (isHatch) {
                boolean isUplink = hTag.getBoolean("isOut");
                hardwareTitle = isUplink ? "Tesla Uplink" : "Tesla Downlink";
                titleColor = isUplink ? ChatFormatting.GREEN : ChatFormatting.RED;
            } else {
                hardwareTitle = hTag.contains("name") ? hTag.getString("name") : "Soul Consumer";
                titleColor = ChatFormatting.LIGHT_PURPLE;
            }

            detailLayer
                    .addWidget(new LabelWidget(28, 9, hardwareTitle + " Details").setTextColor(titleColor.getColor()));

            WidgetGroup displayBox = new WidgetGroup(5, 30, windowWidth - 10, 90);
            displayBox.setBackground(GuiTextures.DISPLAY);
            displayBox.addWidget(new ComponentPanelWidget(8, 8, list -> {
                list.add(Component.literal(hardwareTitle).withStyle(titleColor, ChatFormatting.BOLD));
                BlockPos p = BlockPos.of(hTag.getLong("pos"));
                list.add(Component.literal("Location: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%d, %d, %d", p.getX(), p.getY(), p.getZ()))
                                .withStyle(ChatFormatting.WHITE)));

                if (hTag.contains("buf")) {
                    // Keep the detailed formatter here for the specific machine buffer
                    list.add(Component.literal("Internal Buffer: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(formatTeslaValue(hTag.getString("buf"), false))
                                    .withStyle(ChatFormatting.GOLD))
                            .append(Component.literal(" EU").withStyle(ChatFormatting.GOLD)));
                }

                long flowVal = Long.parseLong(hTag.getString("transfer").isEmpty() ? "0" : hTag.getString("transfer"));
                String sign = isHatch ? (hTag.getBoolean("isOut") ? "+" : "-") : "-";

                list.add(Component.literal("Current Flow: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(sign + formatTeslaValue(hTag.getString("transfer"), false))
                                .withStyle(flowVal > 0 ? titleColor : ChatFormatting.WHITE))
                        .append(" EU/t"));

                String statusText = (flowVal > 0) ? "OPERATIONAL" : "IDLE / NO LOAD";
                list.add(Component.literal("Status: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(statusText)
                                .withStyle(flowVal > 0 ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY)));
            }));
            detailLayer.addWidget(displayBox);

            int btnX = 5;
            int btnY = 125;
            int btnW = windowWidth - 10;
            detailLayer.addWidget(new ButtonWidget(btnX, btnY, btnW, 18, GuiTextures.BUTTON, c -> {
                if (player.level().isClientSide)
                    TeslaHighlightRenderer.highlight(BlockPos.of(hTag.getLong("pos")), 200);
            }));
            detailLayer.addWidget(new LabelWidget(btnX + (btnW / 2) - 45, btnY + 5, "§bHighlight Device"));

            mainLayer.setVisible(false);
            detailLayer.setVisible(true);
        };

        // --- MAIN HEADER (BigInt Compatible) ---
        mainLayer.addWidget(new LabelWidget(8, 6, "Tesla Network Management").setTextColor(0xB000FF));

        WidgetGroup header = new WidgetGroup(5, 18, windowWidth - 10, 80);
        header.setBackground(TESLA_BACKGROUND);
        header.addWidget(new ComponentPanelWidget(5, 4, text -> {
            text.add(Component.literal("Network: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(tag.getString("TeamName")).withStyle(ChatFormatting.AQUA)));

            text.add(Component.literal("Stored:   ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(compactTeslaValue(tag.getString("StoredEU")))
                            .withStyle(ChatFormatting.GOLD))
                    .append(" EU"));

            text.add(Component.literal("Capacity: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(compactTeslaValue(tag.getString("CapacityEU")))
                            .withStyle(ChatFormatting.YELLOW))
                    .append(" EU"));

            text.add(Component.literal("Net Input: ").withStyle(ChatFormatting.GRAY).append(Component
                    .literal("+" + compactTeslaValue(tag.getString("NetInput"))).withStyle(ChatFormatting.GREEN))
                    .append(" EU/t"));
            text.add(Component
                    .literal("Net Output: ").withStyle(ChatFormatting.GRAY).append(Component
                            .literal("-" + compactTeslaValue(tag.getString("NetOutput"))).withStyle(ChatFormatting.RED))
                    .append(" EU/t"));
        }));
        mainLayer.addWidget(header);

        // --- LIST & FILTER LOGIC ---
        String[] filters = { "ALL", "[I]", "[O]", "[S]" };
        for (int i = 0; i < filters.length; i++) {
            int targetMode = i;
            mainLayer.addWidget(new ButtonWidget(7 + (i * 62), 102, 60, 18, GuiTextures.BUTTON, c -> {
                stack.getOrCreateTag().putInt("FilterMode", targetMode);
                if (player instanceof ServerPlayer sp) HeldItemUIFactory.INSTANCE.openUI(holder, sp);
            }));
            LabelWidget fl = new LabelWidget(7 + (i * 62) + 18, 106, filters[i]);
            if (filterMode == targetMode) fl.setTextColor(ChatFormatting.YELLOW.getColor());
            mainLayer.addWidget(fl);
        }

        DraggableScrollableWidgetGroup listGroup = new DraggableScrollableWidgetGroup(7, 125, windowWidth - 14, 145);
        listGroup.setBackground(GuiTextures.DISPLAY);
        mainLayer.addWidget(listGroup);

        ListTag hatchData = tag.getList("HatchData", Tag.TAG_COMPOUND);
        ListTag machineData = tag.getList("MachineData", Tag.TAG_COMPOUND);
        int currentY = 0;

        for (int i = 0; i < hatchData.size(); i++) {
            CompoundTag hTag = hatchData.getCompound(i);
            if (filterMode == 0 || (filterMode == 1 && hTag.getBoolean("isOut")) ||
                    (filterMode == 2 && !hTag.getBoolean("isOut"))) {
                currentY = addListRow(listGroup, hTag, player, openDetail, currentY, true, windowWidth);
            }
        }
        if (filterMode == 0 || filterMode == 3) {
            for (int i = 0; i < machineData.size(); i++) {
                currentY = addListRow(listGroup, machineData.getCompound(i), player, openDetail, currentY, false,
                        windowWidth);
            }
        }

        ui.widget(mainLayer);
        ui.widget(detailLayer);
        return ui;
    }

    private int addListRow(WidgetGroup group, CompoundTag data, Player player, Consumer<CompoundTag> clickAction, int y,
                           boolean isHatch, int windowWidth) {
        BlockPos pos = BlockPos.of(data.getLong("pos")).immutable();
        var font = Minecraft.getInstance().font;

        // Fixed Hatch Logic: isOut = true is Input to network
        boolean isInput = data.getBoolean("isOut");
        String typeLabel = isHatch ? (isInput ? "[I]" : "[O]") : "[S]";
        String colorCode = isHatch ? (isInput ? "§a" : "§c") : "§d";
        String sign = isHatch ? (isInput ? "+" : "-") : "-";

        String statusIcon;
        boolean hasFlow = !data.getString("transfer").equals("0");
        if (!hasFlow) {
            statusIcon = "§7△";
        } else if (isHatch) {
            statusIcon = isInput ? "§2▲" : "§4▼"; // Input = Green Up, Output = Red Down
        } else {
            statusIcon = "§b⚡";
        }

        String rawName = data.contains("name") ? data.getString("name") : (isHatch ? "Tesla Hatch" : "Soul Machine");
        String flowStr = " §8(" + colorCode + sign + compactTeslaValue(data.getString("transfer")) + "§8)";

        int totalAvailableWidth = windowWidth - 110;
        int tailWidth = font.width(flowStr);
        int headWidth = font.width(statusIcon + " " + colorCode + typeLabel + "§r ");
        int availableForName = totalAvailableWidth - tailWidth - headWidth;

        String displayName = rawName;
        if (font.width(displayName) > availableForName) {
            while (font.width(displayName + "..") > availableForName && displayName.length() > 1) {
                displayName = displayName.substring(0, displayName.length() - 1);
            }
            displayName += "..";
        }

        String finalRowText = String.format("%s %s%s§r %s%s", statusIcon, colorCode, typeLabel, displayName, flowStr);

        int rowWidth = windowWidth - 20;
        WidgetGroup row = new WidgetGroup(0, y, rowWidth, 18);
        row.addWidget(new ButtonWidget(0, 0, rowWidth - 22, 18, GuiTextures.BUTTON, c -> clickAction.accept(data)));
        row.addWidget(new LabelWidget(4, 5, finalRowText));
        row.addWidget(new LabelWidget(rowWidth - 68, 5, getDistanceString(pos, player, data)));

        int gearX = rowWidth - 18;
        row.addWidget(new ButtonWidget(gearX, 0, 18, 18, GuiTextures.BUTTON, c -> {
            if (player.level().isClientSide) TeslaHighlightRenderer.highlight(pos, 200);
        }));
        row.addWidget(new ImageWidget(gearX + 1, 1, 16, 16, GuiTextures.IO_CONFIG_COVER_SETTINGS));

        group.addWidget(row);
        return y + 19;
    }

    // Helper to keep the main method clean
    private String getDistanceString(BlockPos pos, Player player, CompoundTag data) {
        if (data.contains("dim") && !player.level().dimension().location().toString().equals(data.getString("dim"))) {
            String dim = data.getString("dim");
            return "§8" + (dim.contains(":") ? dim.split(":")[1] : dim);
        }
        return "§7" + (int) Math.sqrt(player.blockPosition().distSqr(pos)) + "m";
    }

    private String compactTeslaValue(String value) {
        try {
            if (value == null || value.isEmpty()) return "0";
            java.math.BigInteger n = new java.math.BigInteger(value);

            if (n.equals(java.math.BigInteger.ZERO)) return "0";

            // Handle negatives
            boolean negative = n.signum() == -1;
            if (negative) n = n.abs();

            // 1,000 as a BigInteger
            java.math.BigInteger thousand = java.math.BigInteger.valueOf(1000);

            if (n.compareTo(thousand) < 0) return (negative ? "-" : "") + n.toString();

            String[] suffixes = new String[] { "", "k", "M", "G", "T", "P", "E", "Z", "Y" };
            int tier = 0;

            // Use BigDecimal for high-precision division
            java.math.BigDecimal dN = new java.math.BigDecimal(n);
            java.math.BigDecimal dThousand = new java.math.BigDecimal(1000);

            while (dN.compareTo(dThousand) >= 0 && tier < suffixes.length - 1) {
                dN = dN.divide(dThousand, 2, java.math.RoundingMode.HALF_UP);
                tier++;
            }

            // Final formatting: 1.5M, 100k, etc.
            String result;
            if (dN.compareTo(java.math.BigDecimal.valueOf(100)) >= 0) {
                result = String.format("%.0f%s", dN, suffixes[tier]);
            } else {
                result = String.format("%.1f%s", dN, suffixes[tier]);
            }

            return (negative ? "-" : "") + result;
        } catch (Exception e) {
            return "0";
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slotId,
                              boolean isSelected) {
        if (level.isClientSide || !(entity instanceof ServerPlayer serverPlayer)) return;

        // Pulse check (Every 5 ticks) to keep UI responsive but save CPU
        if (level.getGameTime() % 5 != 0) return;

        boolean isUIOpen = serverPlayer.containerMenu instanceof com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
        if (!isSelected && !isUIOpen) return;

        CompoundTag tag = stack.getOrCreateTag();
        if (tag.hasUUID("TargetTeam")) {
            UUID teamUUID = tag.getUUID("TargetTeam");
            ServerLevel overworld = serverPlayer.server.getLevel(Level.OVERWORLD);
            if (overworld == null) return;

            TeslaTeamEnergyData globalData = TeslaTeamEnergyData.get(overworld);
            TeslaTeamEnergyData.TeamEnergy team = globalData.getOrCreate(teamUUID);

            // --- GLOBAL NETWORK STATS ---
            tag.putString("StoredEU", team.stored.toString());
            tag.putString("CapacityEU", team.capacity.toString());
            tag.putString("TeamName", TeamUtils.getTeamName(teamUUID));
            tag.putString("NetInput", String.valueOf(team.lastNetInput));
            tag.putString("NetOutput", String.valueOf(team.lastNetOutput));

            // --- SECTION: PHYSICAL HATCHES (Uplink/Downlink) ---
            ListTag hatchList = new ListTag();
            for (TeslaTeamEnergyData.HatchInfo hatch : globalData.getHatches(teamUUID)) {
                if (hatch.isSoulLinked) continue;
                CompoundTag hTag = new CompoundTag();
                hTag.putLong("pos", hatch.pos.asLong());
                hTag.putBoolean("isOut", hatch.isPhysicalOutput);
                hTag.putString("dim", hatch.dimension.location().toString());

                // Sync Hatch Buffer
                hTag.putString("buf", hatch.buffered.toString());

                long flow = team.machineDisplayFlow.getOrDefault(hatch.pos, 0L);
                hTag.putString("transfer", String.valueOf(flow));
                hatchList.add(hTag);
            }
            tag.put("HatchData", hatchList);

            // --- SECTION: SOUL CONSUMERS (Singleblocks) ---
            ListTag machineList = new ListTag();
            List<BlockPos> toRemove = new ArrayList<>();
            for (BlockPos mPos : team.soulLinkedMachines) {
                ResourceKey<Level> mDim = team.getMachineDimension(mPos);
                ServerLevel mLevel = serverPlayer.server.getLevel(mDim != null ? mDim : Level.OVERWORLD);

                CompoundTag mTag = new CompoundTag();
                mTag.putLong("pos", mPos.asLong());
                mTag.putString("dim", mDim != null ? mDim.location().toString() : "minecraft:overworld");

                if (mLevel != null && mLevel.isLoaded(mPos)) {
                    MetaMachine machine = MetaMachine.getMachine(mLevel, mPos);
                    if (machine != null) {
                        mTag.putString("name", machine.getDefinition().getLangValue());
                        // Sync Machine Buffer
                        if (machine instanceof TieredEnergyMachine tiered && tiered.energyContainer != null) {
                            mTag.putString("buf", String.valueOf(tiered.energyContainer.getEnergyStored()));
                        }
                    } else {
                        toRemove.add(mPos);
                        continue;
                    }
                } else {
                    mTag.putString("name", "§8[Unloaded]§r");
                }
                mTag.putString("transfer", String.valueOf(team.machineDisplayFlow.getOrDefault(mPos, 0L)));
                machineList.add(mTag);
            }

            if (!toRemove.isEmpty()) {
                for (BlockPos pos : toRemove) globalData.removeMachineFromAllTeams(pos);
                globalData.setDirty();
            }
            tag.put("MachineData", machineList);
            serverPlayer.containerMenu.broadcastChanges();
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }
}
