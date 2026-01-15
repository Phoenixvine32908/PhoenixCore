package net.phoenix.core.common.machine.singleblock;

/*
 * public class TeslaWirelessChargerMachine extends TieredEnergyMachine
 * implements IEnergyInfoProvider, IDataStickInteractable, IFancyUIProvider, IFancyUIMachine, IControllable {
 * 
 * protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER =
 * new ManagedFieldHolder(TeslaWirelessChargerMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);
 * 
 * @Persisted @DescSynced
 * private UUID boundTeam;
 * 
 * @DescSynced
 * private BigInteger lastTransferred = BigInteger.ZERO;
 * 
 * private final List<UUID> playersInRange = new ArrayList<>();
 * 
 * // Config values based on Tier
 * private final double range;
 * private final BigInteger maxTransferPerTick;
 * 
 * public TeslaWirelessChargerMachine(IMachineBlockEntity holder, int tier) {
 * super(holder, tier);
 * this.range = 8.0 * (tier + 1); // e.g., LV (1) = 16 blocks, MV (2) = 24 blocks
 * this.maxTransferPerTick = BigInteger.valueOf(GTValues.V[tier] * 2);
 * }
 * 
 * @Override
 * public ManagedFieldHolder getFieldHolder() {
 * return MANAGED_FIELD_HOLDER;
 * }
 * 
 * // ======================
 * // Logic Loop
 * // ======================
 * 
 * @Override
 * public void serverTick() {
 * super.serverTick();
 * if (getOffsetTimer() % 10 == 0) { // Check twice per second
 * tickCharge();
 * }
 * }
 * 
 * private void tickCharge() {
 * if (!(getLevel() instanceof ServerLevel level)) return;
 * 
 * // 1. Resolve Team (Data Stick -> FTB Owner -> Player Owner)
 * UUID teamId = resolveTeamId();
 * if (teamId == null) return;
 * 
 * TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);
 * var network = data.getOrCreate(teamId);
 * if (network.stored.signum() <= 0) return;
 * 
 * // 2. Efficiently find players in 3D range
 * AABB area = new AABB(getPos()).inflate(range);
 * List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, area);
 * 
 * handleRangeNotifications(nearbyPlayers);
 * 
 * BigInteger movedThisTick = BigInteger.ZERO;
 * 
 * for (Player player : nearbyPlayers) {
 * // Security: Only charge players actually on the network's team
 * if (!TeamUtils.isPlayerOnTeam(player, teamId)) continue;
 * 
 * // 3. Process Inventories (Main + Curios)
 * List<IItemHandler> invs = new ArrayList<>();
 * invs.add(new net.minecraftforge.items.wrapper.PlayerMainInvWrapper(player.getInventory()));
 * 
 * if (GTCEu.Mods.isCuriosLoaded()) {
 * CuriosApi.getCuriosInventory(player).ifPresent(h -> invs.add(h.getEquippedCurios()));
 * }
 * 
 * for (IItemHandler handler : invs) {
 * for (int i = 0; i < handler.getSlots(); i++) {
 * ItemStack stack = handler.getStackInSlot(i);
 * if (stack.isEmpty()) continue;
 * 
 * // Charge GT Items
 * IElectricItem electric = GTCapabilityHelper.getElectricItem(stack);
 * if (electric != null && electric.chargeable()) {
 * long move = Math.min(network.stored.longValue(), maxTransferPerTick.longValue());
 * long actual = electric.charge(move, getTier(), false, false);
 * if (actual > 0) {
 * BigInteger actualBI = BigInteger.valueOf(actual);
 * data.addEnergy(teamId, actualBI.negate());
 * movedThisTick = movedThisTick.add(actualBI);
 * }
 * }
 * // Charge Forge Energy Items
 * else {
 * var fe = GTCapabilityHelper.getForgeEnergyItem(stack);
 * if (fe != null && fe.canReceive()) {
 * int actual = fe.receiveEnergy(maxTransferPerTick.intValue(), false);
 * if (actual > 0) {
 * BigInteger actualBI = BigInteger.valueOf(actual);
 * data.addEnergy(teamId, actualBI.negate());
 * movedThisTick = movedThisTick.add(actualBI);
 * }
 * }
 * }
 * }
 * }
 * }
 * this.lastTransferred = movedThisTick;
 * onChanged();
 * }
 * 
 * private UUID resolveTeamId() {
 * if (boundTeam != null) return boundTeam;
 * 
 * var owner = getOwner();
 * if (owner instanceof FTBOwner ftb) {
 * return ftb.getTeam() != null ? ftb.getTeam().getId() : null;
 * } else if (owner instanceof PlayerOwner p) {
 * return p.getUUID();
 * }
 * return null;
 * }
 * 
 * private void handleRangeNotifications(List<Player> nearby) {
 * List<UUID> nearbyUUIDs = nearby.stream().map(Player::getUUID).toList();
 * 
 * for (Player p : nearby) {
 * if (!playersInRange.contains(p.getUUID())) {
 * p.displayClientMessage(Component.literal("Tesla Field Connected").withStyle(ChatFormatting.AQUA), true);
 * playersInRange.add(p.getUUID());
 * }
 * }
 * playersInRange.removeIf(uuid -> {
 * if (!nearbyUUIDs.contains(uuid)) {
 * Player p = getLevel().getPlayerByUUID(uuid);
 * if (p != null) p.displayClientMessage(Component.literal("Tesla Field Disconnected").withStyle(ChatFormatting.GRAY),
 * true);
 * return true;
 * }
 * return false;
 * });
 * }
 * 
 * // ======================
 * // Data Stick & UI
 * // ======================
 * 
 * @Override
 * public InteractionResult onDataStickUse(Player player, ItemStack stick) {
 * if (!stick.is(PhoenixItems.TESLA_BINDER.get())) return InteractionResult.PASS;
 * if (!player.level().isClientSide) {
 * UUID team = stick.getOrCreateTag().getUUID("TargetTeam");
 * this.boundTeam = team;
 * onChanged();
 * player.sendSystemMessage(Component.literal("Manual Network Override: " +
 * TeamUtils.getTeamName(team)).withStyle(ChatFormatting.GREEN));
 * }
 * return InteractionResult.SUCCESS;
 * }
 * 
 * @Override
 * public Widget createUIWidget() {
 * WidgetGroup root = new WidgetGroup(0, 0, 176, 166);
 * root.setBackground(GuiTextures.BACKGROUND_INVERSE);
 * root.addWidget(new ComponentPanelWidget(10, 10, this::addDisplayText));
 * return root;
 * }
 * 
 * @Override
 * public void addDisplayText(List<Component> text) {
 * text.add(Component.literal("Tesla Wireless Charger").withStyle(ChatFormatting.GOLD));
 * 
 * UUID activeId = resolveTeamId();
 * if (activeId == null) {
 * text.add(Component.literal("Status: ").append(Component.literal("OFFLINE").withStyle(ChatFormatting.RED)));
 * } else {
 * text.add(Component.literal("Network: ").append(Component.literal(TeamUtils.getTeamName(activeId)).withStyle(
 * ChatFormatting.AQUA)));
 * text.add(Component.literal("Range: ").append(Component.literal((int)range + "m").withStyle(ChatFormatting.GRAY)));
 * 
 * String rate = com.gregtechceu.gtceu.utils.FormattingUtil.formatNumbers(lastTransferred);
 * text.add(Component.literal("Output: ").append(Component.literal(rate + " EU/t").withStyle(ChatFormatting.GREEN)));
 * }
 * }
 * 
 * @Override
 * public EnergyInfo getEnergyInfo() {
 * return null;
 * }
 * 
 * @Override
 * public long getInputPerSec() {
 * return 0;
 * }
 * 
 * @Override
 * public long getOutputPerSec() {
 * return 0;
 * }
 * 
 * @Override
 * public boolean supportsBigIntEnergyValues() {
 * return false;
 * }
 * 
 * @Override
 * public Widget createMainPage(FancyMachineUIWidget fancyMachineUIWidget) {
 * return null;
 * }
 * 
 * @Override
 * public IGuiTexture getTabIcon() {
 * return null;
 * }
 * 
 * @Override
 * public Component getTitle() {
 * return null;
 * }
 * }
 * 
 */
