package net.phoenix.core.common.machine.multiblock.electric;

import net.minecraft.nbt.CompoundTag;

import lombok.Getter;
import lombok.Setter;

public class TeslaEnergyBank {

    @Getter
    @Setter
    private long stored;
    @Getter
    @Setter
    private long capacity;
    @Getter
    @Setter
    private long maxInput;
    @Getter
    @Setter
    private long maxOutput;

    public TeslaEnergyBank() {}

    public void deposit(long amount) {
        this.stored = Math.min(this.capacity, this.stored + amount);
    }

    public long extract(long amount) {
        long out = Math.min(this.stored, amount);
        this.stored -= out;
        return out;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Stored", stored);
        tag.putLong("Capacity", capacity);
        tag.putLong("MaxIn", maxInput);
        tag.putLong("MaxOut", maxOutput);
        return tag;
    }

    public void load(CompoundTag tag) {
        this.stored = tag.getLong("Stored");
        this.capacity = tag.getLong("Capacity");
        this.maxInput = tag.getLong("MaxIn");
        this.maxOutput = tag.getLong("MaxOut");
    }
}
