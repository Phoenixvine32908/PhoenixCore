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
import com.lowdragmc.lowdraglib.gui.widget.*;


import dev.emi.emi.screen.widget.config.ListWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.phoenix.core.saveddata.TeslaTeamEnergyData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class TeslaBinderItem extends ComponentItem
                             implements IItemUIFactory, IInteractionItem, IAddInformation {

    public TeslaBinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
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

        // Shift + Right Click Block: Bind logic
        if (player.isShiftKeyDown()) {
            if (!context.getLevel().isClientSide) {
                bindToPlayer(player, context.getItemInHand());
                // Play sound here for binding specific block
                context.getLevel().playSound(null, context.getClickedPos(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6f, 1.2f);
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        // IMPORTANT: PASS here so that the use() method below triggers even when clicking a block
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            // Shift logic remains the same
            return InteractionResultHolder.success(stack);
        }

        if (!level.isClientSide && level instanceof ServerLevel server) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("TargetTeam")) {
                UUID team = tag.getUUID("TargetTeam");
                // This fetches all BlockPos registered to this team
                var endpoints = TeslaTeamEnergyData.get(server).getEndpoints(team);


                // Ping Sound
                level.playSound(null, player.blockPosition(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 0.4f, 1.5f);
            }
        }

        // This opens the UI via IItemUIFactory
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
        ModularUI ui = new ModularUI(240, 240, holder, player).background(GuiTextures.BACKGROUND);
        WidgetGroup mainLayer = new WidgetGroup(0, 0, 240, 240);
        WidgetGroup detailLayer = new WidgetGroup(0, 0, 240, 240);
        detailLayer.setVisible(false);

        // Header
        mainLayer.addWidget(new LabelWidget(10, 10, () -> Component.literal("Tesla Network Information")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE).getString()));

        mainLayer.addWidget(new ComponentPanelWidget(10, 30, textList -> {
            ItemStack s = holder.getHeld();
            if (s.hasTag()) {
                CompoundTag tag = s.getTag();
                textList.add(Component.literal("Network: ").withStyle(ChatFormatting.GRAY).append(Component.literal(tag.getString("TeamName")).withStyle(ChatFormatting.AQUA)));
                textList.add(Component.literal("Stored: ").withStyle(ChatFormatting.GRAY).append(Component.literal(tag.getString("StoredEU") + " EU").withStyle(ChatFormatting.GOLD)));
            }
        }));

        DraggableScrollableWidgetGroup componentSelection = new DraggableScrollableWidgetGroup(10, 80, 220, 150);
        mainLayer.addWidget(componentSelection);

        ItemStack stack = holder.getHeld();
        if (stack.hasTag() && stack.getTag().contains("HatchData")) {
            ListTag hatchListData = stack.getTag().getList("HatchData", 10);
            int currentY = 0;

            for (int i = 0; i < hatchListData.size(); i++) {
                CompoundTag hTag = hatchListData.getCompound(i);
                BlockPos hPos = BlockPos.of(hTag.getLong("pos"));
                boolean isOut = hTag.getBoolean("isOut");

                // Calculate distance for the label
                double dist = Math.sqrt(player.blockPosition().distSqr(hPos));

                WidgetGroup row = new WidgetGroup(0, currentY, 210, 20);

                // Main Row Button
                row.addWidget(new ButtonWidget(0, 0, 175, 20, GuiTextures.BUTTON, click -> {
                    // ... (Detail view logic remains the same)
                }));

                row.addWidget(new LabelWidget(5, 5, () -> {
                    String prefix = isOut ? "§c[OUT]§r " : "§a[IN]§r ";
                    return prefix + hPos.getX() + ", " + hPos.getZ() + " §7(" + (int)dist + "m)§r";
                }));

                // Locate Button
                row.addWidget(new ButtonWidget(177, 0, 20, 20, GuiTextures.BUTTON, click -> {
                    if (player.level() instanceof ServerLevel server) {
                        server.sendParticles(ParticleTypes.GLOW, hPos.getX() + 0.5, hPos.getY() + 0.5, hPos.getZ() + 0.5, 40, 0.2, 0.2, 0.2, 0.1);
                        server.sendParticles(ParticleTypes.ELECTRIC_SPARK, hPos.getX() + 0.5, hPos.getY() + 0.5, hPos.getZ() + 0.5, 10, 0.1, 0.1, 0.1, 0.02);
                        server.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.5f);
                    }
                }).setHoverTooltips(Component.literal("Locate this hatch")));

                row.addWidget(new ImageWidget(179, 2, 16, 16, GuiTextures.BUTTON_LIST));

                componentSelection.addWidget(row);
                currentY += 22;
            }
        }

        ui.widget(mainLayer);
        ui.widget(detailLayer);
        return ui;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && isSelected && entity instanceof Player player) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("TargetTeam")) {
                UUID teamUUID = tag.getUUID("TargetTeam");
                TeslaTeamEnergyData data = TeslaTeamEnergyData.get((ServerLevel) level);
                TeslaTeamEnergyData.TeamEnergy team = data.getOrCreate(teamUUID);

                // 1. Sync Header Data
                tag.putString("StoredEU", FormattingUtil.formatNumbers(team.stored));
                tag.putString("CapacityEU", FormattingUtil.formatNumbers(team.capacity));
                tag.putString("TeamName", player.getName().getString() + "'s Network");

                // 2. Sync Hatch List and Mode
                ListTag hatchList = new ListTag();
                for (TeslaTeamEnergyData.HatchInfo hatch : data.getHatches(teamUUID)) {
                    CompoundTag hTag = new CompoundTag();
                    hTag.putLong("pos", hatch.pos.asLong());

                    // FIX: Since tickWireless doesn't fill energyInput/Output maps,
                    // we check if the hatch IS the transmitter by looking at its stored logic.
                    // In your tickWireless: IO.OUT = Pushes to Cloud.
                    // We'll update the HatchInfo in the UI logic to reflect this.
                    hTag.putBoolean("isOut", hatch.output.signum() > 0 || hatch.input.signum() == 0);

                    hTag.putString("buf", hatch.buffered.toString());
                    hatchList.add(hTag);
                }
                tag.put("HatchData", hatchList);
            }
        }
    }
}
