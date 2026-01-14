package net.phoenix.core.common.machine.multiblock.electric;

/*
 * 
 * public class TeslaEnergyBank extends MachineTrait {
 * 
 * public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(TeslaEnergyBank.class);
 * 
 * private long[] storage;
 * private long[] maximums;
 * 
 * @Setter @Getter
 * private int tier;
 * 
 * @Getter
 * private BigInteger maxInput = BigInteger.ZERO;
 * 
 * @Getter
 * private BigInteger maxOutput = BigInteger.ZERO;
 * 
 * 
 * public TeslaEnergyBank(MetaMachine machine) {
 * super(machine);
 * this.storage = new long[0];
 * this.maximums = new long[0];
 * this.tier = 0;
 * }
 * 
 * 
 * public void initialize(@NotNull List<ITeslaBattery> batteries) {
 * int size = batteries.size();
 * this.storage = new long[size];
 * this.maximums = new long[size];
 * 
 * int highestTier = 0;
 * for (int i = 0; i < size; i++) {
 * ITeslaBattery battery = batteries.get(i);
 * this.maximums[i] = battery.getCapacity().min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
 * if (battery.getTier() > highestTier) highestTier = battery.getTier();
 * }
 * 
 * this.tier = highestTier;
 * calculateIORates();
 * }
 * 
 * private void calculateIORates() {
 * long voltage = GTValues.V[this.tier];
 * this.maxInput = BigInteger.valueOf(voltage * 2);
 * this.maxOutput = BigInteger.valueOf(voltage * 2);
 * }
 * 
 * public void rebuild(@NotNull List<ITeslaBattery> batteries) {
 * BigInteger currentlyStored = getStored();
 * initialize(batteries);
 * fill(currentlyStored);
 * }
 * 
 * 
 * 
 * public long fill(long amount) {
 * if (amount <= 0) return 0;
 * BigInteger before = getStored();
 * fill(BigInteger.valueOf(amount));
 * return getStored().subtract(before).longValue();
 * }
 * 
 * public long drain(long amount) {
 * if (amount <= 0) return 0;
 * return drain(BigInteger.valueOf(amount)).longValue();
 * }
 * 
 * public void fill(@NotNull BigInteger amount) {
 * if (amount.signum() <= 0) return;
 * BigInteger remaining = amount;
 * 
 * for (int i = 0; i < storage.length && remaining.signum() > 0; i++) {
 * long space = maximums[i] - storage[i];
 * if (space <= 0) continue;
 * 
 * long moved = remaining.min(BigInteger.valueOf(space)).longValue();
 * storage[i] += moved;
 * remaining = remaining.subtract(BigInteger.valueOf(moved));
 * }
 * }
 * 
 * public @NotNull BigInteger drain(@NotNull BigInteger amount) {
 * if (amount.signum() <= 0) return BigInteger.ZERO;
 * BigInteger remaining = amount;
 * BigInteger totalDrained = BigInteger.ZERO;
 * 
 * for (int i = storage.length - 1; i >= 0 && remaining.signum() > 0; i--) {
 * if (storage[i] <= 0) continue;
 * 
 * long moved = remaining.min(BigInteger.valueOf(storage[i])).longValue();
 * storage[i] -= moved;
 * remaining = remaining.subtract(BigInteger.valueOf(moved));
 * totalDrained = totalDrained.add(BigInteger.valueOf(moved));
 * }
 * return totalDrained;
 * }
 * 
 * public @NotNull BigInteger getStored() {
 * BigInteger total = BigInteger.ZERO;
 * for (long s : storage) total = total.add(BigInteger.valueOf(s));
 * return total;
 * }
 * 
 * public @NotNull BigInteger getCapacity() {
 * BigInteger total = BigInteger.ZERO;
 * for (long m : maximums) total = total.add(BigInteger.valueOf(m));
 * return total;
 * }
 * 
 * public boolean hasEnergy() {
 * for (long l : storage) if (l > 0) return true;
 * return false;
 * }
 * 
 * // --- Persistence ---
 * 
 * @Override
 * public void loadCustomPersistedData(@NotNull CompoundTag tag) {
 * int size = tag.getInt("Size");
 * this.storage = new long[size];
 * this.maximums = new long[size];
 * for (int i = 0; i < size; i++) {
 * CompoundTag sub = tag.getCompound(String.valueOf(i));
 * this.storage[i] = sub.getLong("S");
 * this.maximums[i] = sub.getLong("M");
 * }
 * this.tier = tag.getInt("Tier");
 * this.maxInput = new BigInteger(tag.getString("In"));
 * this.maxOutput = new BigInteger(tag.getString("Out"));
 * }
 * 
 * @Override
 * public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
 * tag.putInt("Size", storage.length);
 * for (int i = 0; i < storage.length; i++) {
 * CompoundTag sub = new CompoundTag();
 * sub.putLong("S", storage[i]);
 * sub.putLong("M", maximums[i]);
 * tag.put(String.valueOf(i), sub);
 * }
 * tag.putInt("Tier", tier);
 * tag.putString("In", maxInput.toString());
 * tag.putString("Out", maxOutput.toString());
 * }
 * 
 * @Override
 * public ManagedFieldHolder getFieldHolder() {
 * return MANAGED_FIELD_HOLDER;
 * }
 * }
 */
