package net.phoenix.core.common.machine.multiblock.electric;

import net.minecraft.nbt.CompoundTag;
import net.phoenix.core.api.machine.trait.ITeslaBattery;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class TeslaEnergyBank {

    private long[] storage;
    private long[] maximums;

    @Setter
    @Getter
    private int tier;
    @Getter
    private BigInteger maxInput = BigInteger.ZERO;
    @Getter
    private BigInteger maxOutput = BigInteger.ZERO;

    public TeslaEnergyBank() {
        this.storage = new long[0];
        this.maximums = new long[0];
        this.tier = 9;
    }

    public TeslaEnergyBank(@NotNull List<ITeslaBattery> batteries) {
        List<Long> storedList = new ArrayList<>();
        List<Long> maxList = new ArrayList<>();
        int highestTier = 9;

        for (ITeslaBattery battery : batteries) {
            long cap = battery.getCapacity().min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
            maxList.add(cap);
            storedList.add(0L);

            if (battery.getTier() > highestTier) highestTier = battery.getTier();
        }

        this.storage = storedList.stream().mapToLong(Long::longValue).toArray();
        this.maximums = maxList.stream().mapToLong(Long::longValue).toArray();
        this.tier = highestTier;
    }

    public TeslaEnergyBank rebuild(@NotNull List<ITeslaBattery> batteries) {
        TeslaEnergyBank rebuilt = new TeslaEnergyBank(batteries);
        rebuilt.fill(this.getStored());
        rebuilt.tier = this.tier;
        rebuilt.maxInput = this.maxInput;
        rebuilt.maxOutput = this.maxOutput;
        return rebuilt;
    }

    public long fill(long amount) {
        if (amount <= 0) return 0;
        long remaining = amount;

        for (int i = 0; i < storage.length && remaining > 0; i++) {
            long space = maximums[i] - storage[i];
            if (space <= 0) continue;
            long moved = Math.min(space, remaining);
            storage[i] += moved;
            remaining -= moved;
        }

        return amount - remaining;
    }

    public long drain(long amount) {
        if (amount <= 0) return 0;
        long remaining = amount;

        for (int i = storage.length - 1; i >= 0 && remaining > 0; i--) {
            long available = storage[i];
            if (available <= 0) continue;
            long moved = Math.min(available, remaining);
            storage[i] -= moved;
            remaining -= moved;
        }

        return amount - remaining;
    }

    public void fill(@NotNull BigInteger amount) {
        if (amount.signum() <= 0) return;

        BigInteger remaining = amount;
        for (int i = 0; i < storage.length && remaining.signum() > 0; i++) {
            BigInteger space = BigInteger.valueOf(maximums[i] - storage[i]);
            if (space.signum() <= 0) continue;

            BigInteger moved = remaining.min(space);
            storage[i] += moved.longValue();
            remaining = remaining.subtract(moved);
        }
    }

    public @NotNull BigInteger drain(@NotNull BigInteger amount) {
        if (amount.signum() <= 0) return BigInteger.ZERO;

        BigInteger remaining = amount;
        BigInteger drained = BigInteger.ZERO;

        for (int i = storage.length - 1; i >= 0 && remaining.signum() > 0; i--) {
            BigInteger available = BigInteger.valueOf(storage[i]);
            if (available.signum() <= 0) continue;

            BigInteger moved = remaining.min(available);
            storage[i] -= moved.longValue();
            remaining = remaining.subtract(moved);
            drained = drained.add(moved);
        }

        return drained;
    }

    public @NotNull BigInteger getStored() {
        BigInteger total = BigInteger.ZERO;
        for (long s : storage) total = total.add(BigInteger.valueOf(s));
        return total;
    }

    public @NotNull BigInteger getCapacity() {
        BigInteger total = BigInteger.ZERO;
        for (long m : maximums) total = total.add(BigInteger.valueOf(m));
        return total;
    }

    public @NotNull CompoundTag writeToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Size", storage.length);

        for (int i = 0; i < storage.length; i++) {
            CompoundTag sub = new CompoundTag();
            sub.putLong("Stored", storage[i]);
            sub.putLong("Max", maximums[i]);
            tag.put(String.valueOf(i), sub);
        }

        tag.putInt("Tier", tier);
        tag.putString("MaxInput", maxInput.toString());
        tag.putString("MaxOutput", maxOutput.toString());
        return tag;
    }

    public void readFromNBT(@NotNull CompoundTag tag) {
        int size = tag.getInt("Size");
        storage = new long[size];
        maximums = new long[size];

        for (int i = 0; i < size; i++) {
            CompoundTag sub = tag.getCompound(String.valueOf(i));
            storage[i] = sub.getLong("Stored");
            maximums[i] = sub.getLong("Max");
        }

        tier = tag.getInt("Tier");
        maxInput = new BigInteger(tag.getString("MaxInput"));
        maxOutput = new BigInteger(tag.getString("MaxOutput"));
    }
}
