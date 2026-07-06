package com.bf.anvilcaftaddon.block.blocks.PowerBlock;

import com.bf.anvilcaftaddon.block.ModBlockEntity;
import dev.dubhe.anvilcraft.api.power.IPowerProducer;
import dev.dubhe.anvilcraft.api.power.PowerComponentType;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class PowerBlockEntity extends BlockEntity implements IPowerProducer {
    @Nullable
    private PowerGrid grid = null;
    public PowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.APOWER_BE.get(), pos, state);
    }
    @Override public int getOutputPower() { return 1; }
    @Override public @NotNull PowerComponentType getComponentType() { return PowerComponentType.PRODUCER; }
    @Override public @NotNull BlockPos getPos() { return getBlockPos(); }
    @Override public @Nullable Level getCurrentLevel() { return level; }
    @Override public @Nullable PowerGrid getGrid() { return grid; }
    @Override public void setGrid(@Nullable PowerGrid g) { this.grid = g; }
    @Override public int getRange() { return 2; }
}
