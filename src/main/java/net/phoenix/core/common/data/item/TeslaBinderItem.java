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
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
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
                    boolean isNowLinked = data.toggleSoulLink(teamUUID, clickedPos);

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

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        ItemStack stack = holder.getHeld();
        CompoundTag tag = stack.getOrCreateTag();

        // GUARDS: Ensure lists exist to avoid null pointers
        ListTag hatchListData = tag.getList("HatchData", Tag.TAG_COMPOUND);
        ListTag machineListData = tag.getList("MachineData", Tag.TAG_COMPOUND);

        int totalEntries = hatchListData.size() + machineListData.size();
        int listHeight = Math.min(130, Math.max(40, totalEntries * 18 + 4));
        int windowHeight = 95 + listHeight;

        ModularUI ui = new ModularUI(230, windowHeight, holder, player).background(GuiTextures.BACKGROUND);
        WidgetGroup mainLayer = new WidgetGroup(0, 0, 230, windowHeight);
        WidgetGroup detailLayer = new WidgetGroup(0, 0, 230, windowHeight);
        detailLayer.setVisible(false);

        // --- DETAIL VIEW PANEL ---
        Consumer<CompoundTag> openDetail = (hTag) -> {
            detailLayer.clearAllWidgets();
            WidgetGroup detailHeader = new WidgetGroup(5, 5, 220, 20);
            detailLayer.addWidget(detailHeader);

            detailHeader.addWidget(new ButtonWidget(0, 0, 18, 18, GuiTextures.BUTTON, c -> {
                detailLayer.setVisible(false);
                mainLayer.setVisible(true);
            }).setButtonTexture(GuiTextures.BUTTON_LEFT));

            detailHeader.addWidget(new LabelWidget(25, 4, "Device Details").setTextColor(ChatFormatting.GOLD.getColor()));

            WidgetGroup detailDisplay = new WidgetGroup(5, 30, 220, 55);
            // Detail view keeps the dark GT display for contrast
            detailDisplay.setBackground(GuiTextures.DISPLAY);
            detailLayer.addWidget(detailDisplay);

            detailLayer.addWidget(new ComponentPanelWidget(12, 40, list -> {
                // Machine name is now the pre-translated string from NBT
                String name = hTag.contains("name") ? hTag.getString("name") : "Tesla Component";
                list.add(Component.literal(name).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));

                list.add(Component.empty());

                if (hTag.contains("buf")) {
                    list.add(Component.literal("Internal Buffer: ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(hTag.getString("buf") + " EU").withStyle(ChatFormatting.YELLOW)));
                }

                // UI FORMATTING: Append " EU/t" here once.
                // The NBT string now contains ONLY the number and sign (e.g., "-32")
                String flowValue = hTag.getString("transfer");
                list.add(Component.literal("Network Flow: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(flowValue + " EU/t").withStyle(ChatFormatting.AQUA)));

                if (hTag.contains("isOut")) {
                    list.add(hTag.getBoolean("isOut") ?
                            Component.literal("Link Mode: PULL").withStyle(ChatFormatting.RED) :
                            Component.literal("Link Mode: PUSH").withStyle(ChatFormatting.GREEN));
                }
            }));

            mainLayer.setVisible(false);
            detailLayer.setVisible(true);
        };

        // --- MAIN UI HEADER ---
        mainLayer.addWidget(new LabelWidget(8, 6, "Tesla Network Management").setTextColor(0xB000FF));

        WidgetGroup headerGroup = new WidgetGroup(5, 18, 220, 48);
        // CUSTOM TEXTURE: Using your new PhoenixCore tesla background
        headerGroup.setBackground(TESLA_BACKGROUND);
        mainLayer.addWidget(headerGroup);

        headerGroup.addWidget(new ComponentPanelWidget(5, 4, textList -> {
            textList.add(Component.literal("Network: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(tag.getString("TeamName")).withStyle(ChatFormatting.AQUA)));
            textList.add(Component.literal("Stored: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(tag.getString("StoredEU") + " EU").withStyle(ChatFormatting.GOLD)));
        }));

        // --- LIST GROUP ---
        // Draggable area for all your machines and hatches
        DraggableScrollableWidgetGroup listGroup = new DraggableScrollableWidgetGroup(7, 72, 216, listHeight - 4);
        // Keep standard GT display texture for the scrolling list for readability
        listGroup.setBackground(GuiTextures.DISPLAY);
        mainLayer.addWidget(listGroup);

        int currentY = 0;

        // 1. ADD HATCHES (Multiblocks)
        for (int i = 0; i < hatchListData.size(); i++) {
            CompoundTag hTag = hatchListData.getCompound(i);
            currentY = addListRow(listGroup, hTag, player, openDetail, currentY, true);
        }

        // 2. ADD SINGLE-BLOCK MACHINES (Using your Mixin data)
        for (int i = 0; i < machineListData.size(); i++) {
            CompoundTag mTag = machineListData.getCompound(i);
            currentY = addListRow(listGroup, mTag, player, openDetail, currentY, false);
        }

        ui.widget(mainLayer).widget(detailLayer);
        return ui;
    }

    private int addListRow(WidgetGroup group, CompoundTag data, Player player, Consumer<CompoundTag> clickAction, int y, boolean isHatch) {
        BlockPos pos = BlockPos.of(data.getLong("pos"));

        // Name formatting
        String displayName = isHatch ?
                (data.getBoolean("isOut") ? "§a[I]§r Hatch" : "§c[O]§r Hatch") :
                "§d[S]§r " + data.getString("name");

        double dist = Math.sqrt(player.blockPosition().distSqr(pos));

        WidgetGroup row = new WidgetGroup(0, y, 210, 18);

        // Main Detail Button
        row.addWidget(new ButtonWidget(0, 0, 190, 18, GuiTextures.BUTTON, c -> clickAction.accept(data)));
        row.addWidget(new LabelWidget(4, 4, displayName + " §7" + (int)dist + "m"));

        // Highlight Button (Fixed: Added Gear Image back)
        row.addWidget(new ButtonWidget(192, 0, 18, 18, GuiTextures.BUTTON, c -> {
            if (player.level().isClientSide) TeslaHighlightRenderer.highlight(pos, 200);
            player.playSound(SoundEvents.UI_BUTTON_CLICK.get(), 0.3f, 1.5f);
        }));
        // RESTORED: This puts the magnifying glass/gear icon on the button
        row.addWidget(new ImageWidget(193, 1, 16, 16, GuiTextures.IO_CONFIG_COVER_SETTINGS));

        group.addWidget(row);
        return y + 19;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !isSelected || level.getGameTime() % 20 != 0 || !(entity instanceof ServerPlayer serverPlayer)) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (tag.hasUUID("TargetTeam")) {
            UUID teamUUID = tag.getUUID("TargetTeam");
            TeslaTeamEnergyData data = TeslaTeamEnergyData.get((ServerLevel) level);
            TeslaTeamEnergyData.TeamEnergy team = data.getOrCreate(teamUUID);

            tag.putString("StoredEU", FormattingUtil.formatNumbers(team.stored));
            tag.putString("CapacityEU", FormattingUtil.formatNumbers(team.capacity));

            long totalNetFlow = 0;

            // 2. Process Hatches (Multiblock Parts)
            ListTag hatchList = new ListTag();
            for (TeslaTeamEnergyData.HatchInfo hatch : data.getHatches(teamUUID)) {
                if (hatch.isSoulLinked) continue;

                CompoundTag hTag = new CompoundTag();
                hTag.putLong("pos", hatch.pos.asLong());
                hTag.putBoolean("isOut", hatch.isPhysicalOutput);
                hTag.putString("buf", FormattingUtil.formatNumbers(hatch.buffered));

                BigInteger transfer = hatch.isPhysicalOutput ?
                        team.energyOutput.getOrDefault(hatch.pos, BigInteger.ZERO) :
                        team.energyInput.getOrDefault(hatch.pos, BigInteger.ZERO);

                long hatchFlow = transfer.longValue();
                totalNetFlow += (hatch.isPhysicalOutput ? -hatchFlow : hatchFlow);

                String flowSign = (hatchFlow == 0) ? "" : (hatch.isPhysicalOutput ? "-" : "+");
                hTag.putString("transfer", flowSign + FormattingUtil.formatNumbers(hatchFlow));
                hatchList.add(hTag);
            }
            tag.put("HatchData", hatchList);

            // 3. Process Single-Blocks (Machines using Mixin)
            ListTag machineList = new ListTag();
            List<BlockPos> toRemove = new ArrayList<>();

            for (BlockPos mPos : team.soulLinkedMachines) {
                if (level.isLoaded(mPos)) {
                    MetaMachine machine = MetaMachine.getMachine(level, mPos);

                    if (machine instanceof TieredEnergyMachine tiered && tiered.energyContainer != null) {
                        CompoundTag mTag = new CompoundTag();
                        mTag.putLong("pos", mPos.asLong());
                        mTag.putString("name", machine.getDefinition().getLangValue());
                        mTag.putString("buf", FormattingUtil.formatNumbers(tiered.energyContainer.getEnergyStored()));

                        if (machine instanceof TeslaBinderItem.ITeslaFlowTracker tracker) {
                            long machineDelta = tracker.phoenixCore$getTeslaFlow();
                            long networkPerspective = -machineDelta;
                            totalNetFlow += networkPerspective;

                            // UPDATE PERSISTENCE IN DATA CLASS
                            team.machineLastEnergy.put(mPos, tiered.energyContainer.getEnergyStored());
                            team.machineCurrentFlow.put(mPos, machineDelta);
                            data.setDirty();

                            String flowStr = (networkPerspective == 0) ? "0" :
                                    (networkPerspective > 0 ? "+" : "") + FormattingUtil.formatNumbers(networkPerspective);
                            mTag.putString("transfer", flowStr);
                        } else {
                            mTag.putString("transfer", "0");
                        }
                        machineList.add(mTag);
                    } else if (machine == null) {
                        toRemove.add(mPos);
                    }
                }
            }

            if (!toRemove.isEmpty()) {
                for (BlockPos pos : toRemove) {
                    team.soulLinkedMachines.remove(pos);
                    team.machineLastEnergy.remove(pos);
                    team.machineCurrentFlow.remove(pos);
                }
                data.setDirty();
            }

            tag.put("MachineData", machineList);
            tag.putLong("NetFlow", totalNetFlow);

            stack.setTag(tag);
            if (serverPlayer.containerMenu != null) {
                serverPlayer.containerMenu.broadcastChanges();
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }
    public interface ITeslaFlowTracker {
        /**
         * Gets the EU/t delta calculated by the Mixin.
         */
        default long phoenixCore$getTeslaFlow() {
            return 0L;
        }
        void phoenixCore$resetTeslaFlow();
    }
}
