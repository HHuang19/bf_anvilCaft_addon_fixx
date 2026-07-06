package com.bf.anvilcaftaddon.block.blocks.ender_transmission_pole;

import com.bf.anvilcaftaddon.DataComponents;
import com.bf.anvilcaftaddon.block.ModBlockEntity;
import com.bf.anvilcaftaddon.block.ModBlocks;
import dev.dubhe.anvilcraft.api.power.IPowerConsumer;
import dev.dubhe.anvilcraft.api.power.IPowerProducer;
import dev.dubhe.anvilcraft.api.power.PowerComponentType;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import dev.dubhe.anvilcraft.block.state.Vertical3PartHalf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnderPoleBlockEntity extends BlockEntity implements IPowerProducer, IPowerConsumer {


    public PowerGrid PoleOfGrid = null;
    public int Netpower = 50;
    @Nullable private ResourceLocation TDIM = null;
    @Nullable public BlockPos TPOS = null;
    ServerLevel otherLevel = null;



    public EnderPoleBlockEntity( BlockPos pos, BlockState blockState) {
        super(ModBlockEntity.ENDER_POLE_ENTITY.get(), pos, blockState);
    }
    private EnderPoleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }
    public static EnderPoleBlockEntity createBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState blockState
    ) {
        return new EnderPoleBlockEntity(type, pos, blockState);
    }

    @Override
    public @Nullable Level getCurrentLevel() {
        return this.getLevel();
    }

    @Override
    public @NotNull BlockPos getPos() {
        return this.getBlockPos();
    }

    @Override
    public void setGrid(@Nullable PowerGrid powerGrid) {
        this.PoleOfGrid = powerGrid;
    }

    @Override
    public @Nullable PowerGrid getGrid() {
        return this.PoleOfGrid;
    }


    public void tick(Level level, BlockPos pos, BlockState state) {
        otherLevel = ((ServerLevel) level).getServer().getLevel(
            ResourceKey.create(Registries.DIMENSION, this.TDIM));

        if (this.getComponentType() == PowerComponentType.INVALID) return;

        if (state.getValue(EnderPoleBlock.SWITCH) == Switch.OFF && this.getGrid() != null) {
            this.getGrid().remove(this);
        } else if (state.getValue(EnderPoleBlock.SWITCH) == Switch.ON && this.getGrid() == null) {
            PowerGrid.addComponent(this);
        }
        if (state.getValue(EnderPoleBlock.SWITCH) == Switch.ON)
            if (state.getValue(EnderPoleBlock.PARTHALF) == Vertical3PartHalf.TOP) {
                if (this.TDIM != null) {
                    if (otherLevel.getBlockEntity(this.TPOS) instanceof EnderPoleBlockEntity otherPole) {
                        if (otherPole.TPOS.equals(this.TPOS) && otherPole.TDIM.equals(this.TDIM)) {
                            if (!this.getBlockState().getValue(EnderPoleBlock.IsFather))
                                if (otherPole.getBlockState().getValue(EnderPoleBlock.SWITCH)==Switch.ON)
                                {//连接正常确认完毕
                                    this.getBlockState().setValue(EnderPoleBlock.IsEffective, true);
                                    otherPole.getBlockState().setValue(EnderPoleBlock.IsEffective, true);
                                    this.Netpower = otherPole.Netpower;
                                }
                            else if (otherPole.getBlockState().getValue(EnderPoleBlock.SWITCH)==Switch.OFF);
                            else this.getBlockState().setValue(EnderPoleBlock.IsEffective, false);
                        }else {
                            this.getBlockState().setValue(EnderPoleBlock.IsEffective, false);
                            this.TDIM = null;
                            this.TPOS = null;
                        }
                    }else {//如果那个杆子甚至不存在
                        this.getBlockState().setValue(EnderPoleBlock.IsEffective, false);
                        this.TDIM = null;
                        this.TPOS = null;
                    }
                }
            } else {
                BlockEntity _Top = state.getValue(EnderPoleBlock.PARTHALF) == Vertical3PartHalf.BOTTOM
                                    ? level.getBlockEntity(pos.above(2))
                                    : level.getBlockEntity(pos.below(1));
                this.getBlockState().setValue(EnderPoleBlock.IsEffective, _Top.getBlockState().getValue(EnderPoleBlock.IsEffective));
                this.getBlockState().setValue(EnderPoleBlock.SWITCH, _Top.getBlockState().getValue(EnderPoleBlock.SWITCH));
            }
        this.flushState(level, pos);
    }

    @Override
    public @NotNull PowerComponentType getComponentType() {
        if (Netpower == 0 ||
            this.getBlockState().getValue(EnderPoleBlock.PARTHALF) != Vertical3PartHalf.TOP)
                return PowerComponentType.INVALID;
        boolean IsZ = (Netpower > 0);
        if (this.getBlockState().getValue(EnderPoleBlock.IsFather))
            return IsZ? PowerComponentType.CONSUMER : PowerComponentType.PRODUCER;
        else return IsZ? PowerComponentType.PRODUCER : PowerComponentType.CONSUMER;
    }

    @Override
    public int getOutputPower() {
        if (Netpower == 0
            ||!this.getBlockState().getValue(EnderPoleBlock.IsEffective)||
            !this.getBlockState().getValue(EnderPoleBlock.SWITCH).equals(Switch.OFF))
                return 0;
        BlockState state = this.getBlockState();
        boolean IsZ = (Netpower > 0);
        if (state.getValue(EnderPoleBlock.IsFather))
            return IsZ? 0 : this.Netpower;
        else return IsZ? this.Netpower : 0;
    }

    @Override
    public int getInputPower() {
        if (Netpower == 0
            ||!this.getBlockState().getValue(EnderPoleBlock.IsEffective)||
            !this.getBlockState().getValue(EnderPoleBlock.SWITCH).equals(Switch.OFF))
            return 0;
        BlockState state = this.getBlockState();
        boolean IsZ = (Netpower > 0);
        if (state.getValue(EnderPoleBlock.IsFather))
            return IsZ? this.Netpower : 0;
        else return IsZ? 0 : this.Netpower;
    }
    public void Set_Power(int netpower) {this.Netpower=netpower;};
    public void Set_Power(int netpower, boolean add) {this.Netpower = add? this.Netpower+netpower : this.Netpower-netpower;}
    public void Set_Outer(ResourceLocation dim, BlockPos pos) {
        Vertical3PartHalf Part = this.getBlockState().getValue(EnderPoleBlock.PARTHALF);
        if (Part == Vertical3PartHalf.TOP) {this.TDIM = dim; this.TPOS = pos;}
        else {
            if (level != null) {
                BlockPos TopPos = (Part == Vertical3PartHalf.BOTTOM) ? this.getPos().above(2) : this.getPos().below(1);
                if (level.getBlockEntity(TopPos) instanceof EnderPoleBlockEntity ThisPole) {
                    otherLevel = ((ServerLevel) level).getServer().getLevel(
                        ResourceKey.create(Registries.DIMENSION, dim));
                    if (otherLevel != null){
                        BlockEntity otherpole = otherLevel.getBlockEntity(TopPos);
                        if (otherpole instanceof EnderPoleBlockEntity OtherPole &&
                            !otherpole.getBlockState().getValue(EnderPoleBlock.IsEffective) &&
                            !otherpole.getBlockState().getValue(EnderPoleBlock.IsFather)
                        )
                        {
                            ThisPole.TPOS = TopPos;
                            ThisPole.TDIM = dim;
                            ThisPole.Netpower = OtherPole.Netpower;
                            ((EnderPoleBlockEntity) otherpole).TDIM = level.dimension().location();
                            ((EnderPoleBlockEntity) otherpole).TPOS = TopPos;
                        }
                    }
                }
            }
        };
    }
}
