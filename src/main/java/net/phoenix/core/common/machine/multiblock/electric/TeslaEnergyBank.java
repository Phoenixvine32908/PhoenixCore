package net.phoenix.core.common.machine.multiblock.electric;

import net.minecraft.nbt.CompoundTag;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TeslaEnergyBank {

    private long stored;
    private long capacity;
    private long maxInput;
    private long maxOutput;
    private int tier = 9;
    private int linkedHatchesCount = 0;

    public TeslaEnergyBank() {}

    public void deposit(long amount) {
        stored = Math.min(capacity, stored + amount);
    }

    public long extract(long amount) {
        long out = Math.min(stored, amount);
        stored -= out;
        return out;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Stored", stored);
        tag.putLong("Capacity", capacity);
        tag.putLong("MaxIn", maxInput);
        tag.putLong("MaxOut", maxOutput);
        tag.putInt("Tier", tier);
        tag.putInt("LinkedHatches", linkedHatchesCount);
        return tag;
    }

    public void load(CompoundTag tag) {
        stored = tag.getLong("Stored");
        capacity = tag.getLong("Capacity");
        maxInput = tag.getLong("MaxIn");
        maxOutput = tag.getLong("MaxOut");
        tier = tag.getInt("Tier");
        linkedHatchesCount = tag.getInt("LinkedHatches");
    }
}
