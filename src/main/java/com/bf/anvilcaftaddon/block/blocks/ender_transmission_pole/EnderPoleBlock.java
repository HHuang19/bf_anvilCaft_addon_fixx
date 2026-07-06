package com.bf.anvilcaftaddon.block.blocks.ender_transmission_pole;

import com.bf.anvilcaftaddon.DataComponents;
import com.bf.anvilcaftaddon.ModItems;
import com.bf.anvilcaftaddon.block.ModBlocks;
import dev.dubhe.anvilcraft.api.IHasMultiBlock;
import dev.dubhe.anvilcraft.api.power.IPowerComponent;
import dev.dubhe.anvilcraft.block.multipart.SimpleMultiPartBlock;
import dev.dubhe.anvilcraft.block.state.Vertical3PartHalf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class EnderPoleBlock extends SimpleMultiPartBlock<Vertical3PartHalf> implements IHasMultiBlock, EntityBlock {
    //实现super的构造函数
    public EnderPoleBlock() {
        super(Properties.of()
                .strength(2.0F)
                .lightLevel(state -> state.getValue(OVERLOAD) && state.getValue(SWITCH) == IPowerComponent.Switch.ON ? 15 : 0)
        );
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PARTHALF,Vertical3PartHalf.BOTTOM)
                .setValue(OVERLOAD,true)
                .setValue(SWITCH, IPowerComponent.Switch.OFF)
                .setValue(IsFather, true)
                .setValue(IsEffective, false)
        );
    }

    public static final BooleanProperty OVERLOAD = IPowerComponent.OVERLOAD;
    public static final BooleanProperty IsFather = BooleanProperty.create("is_father");//此电线杆是否是父级
    public static final BooleanProperty IsEffective = BooleanProperty.create("is_effective");//此电线杆是否有效
    public static final EnumProperty<IPowerComponent.Switch> SWITCH = IPowerComponent.SWITCH;

    public static final EnumProperty<Vertical3PartHalf> PARTHALF =
            EnumProperty.create("half", Vertical3PartHalf.class);//本方块的三部分
    public static final VoxelShape SHAPE_TOP =
            Shapes.or(Block.box(3, 5, 3, 13, 16, 13), Block.box(6, 0, 6, 10, 5, 10));
    public static final VoxelShape SHAPE_MID = Block.box(6, 0, 6, 10, 16, 10);
    public static final VoxelShape SHAPE_BOT =
            Shapes.or(Block.box(3, 4, 3, 13, 10, 13), Block.box(0, 0, 0, 16, 4, 16), Block.box(6, 10, 6, 10, 16, 10));

    @Override
    public void onRemove(Level level, BlockPos blockPos, BlockState blockState) {
        //被移除
    }

    @Override
    public void onPlace(Level level, BlockPos blockPos, BlockState blockState) {
        //被放置
    }

    @Override
    public Property<Vertical3PartHalf> getPart() {
        return PARTHALF;
    }//方块如何分

    @Override
    public Vertical3PartHalf[] getParts() {
        return Vertical3PartHalf.values();
    }//方块分为哪几段

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {//设置每段碰撞
        return switch (state.getValue(PARTHALF)) {
            case BOTTOM -> SHAPE_BOT;
            case MID -> SHAPE_MID;
            case TOP -> SHAPE_TOP;
        };
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;//模型
    }//游戏如何渲染

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {//对应的方块实体
        return new EnderPoleBlockEntity(blockPos,blockState);//使用方块实体
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {//创建一个构建器
        builder.add(PARTHALF).add(OVERLOAD).add(SWITCH).add(IsFather).add(IsEffective);//告诉构建器这个方块有什么属性
    }

    @Override
    public @Nullable BlockState getPlacementState(BlockPlaceContext context) {//其放置时的默认状态
        return defaultBlockState().setValue(PARTHALF, Vertical3PartHalf.BOTTOM).setValue(IsFather, false);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide)return null;//向游戏注册tick事件
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof EnderPoleBlockEntity) {
                ((EnderPoleBlockEntity) blockEntity).tick(level1, pos, state1);
            }
        };
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide) return;
        if (state.getValue(PARTHALF) != Vertical3PartHalf.BOTTOM) return;

        BlockPos topPos = pos.above(2);
        BlockState topState = level.getBlockState(topPos);
        if (!topState.is(ModBlocks.ENDERPOLE.get()) || topState.getValue(PARTHALF) != Vertical3PartHalf.TOP) return;

        boolean hasRedstoneSignal = level.hasNeighborSignal(pos);
        boolean isCurrentlyOff = (state.getValue(SWITCH) == IPowerComponent.Switch.OFF);

        if (isCurrentlyOff != hasRedstoneSignal) {
            IPowerComponent.Switch nextSwitch = hasRedstoneSignal ? IPowerComponent.Switch.OFF : IPowerComponent.Switch.ON;

            BlockState updatedBottom = state.setValue(SWITCH, nextSwitch);
            BlockState updatedTop = topState.setValue(SWITCH, nextSwitch);

            level.setBlockAndUpdate(pos, updatedBottom);
            level.setBlockAndUpdate(topPos, updatedTop);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.PASS;
        player.displayClientMessage(Component.literal("右键电线杆，通过客户端测试"), false);
        if (!state.getValue(IsEffective)) {
            player.displayClientMessage(Component.literal("对着无效电线杆使用"), false);
            player.displayClientMessage(Component.translatable("message.ender_pole.noeffective"), false);
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.literal("线杆有效...下一步..."), false);
        if (state.getValue(IsFather)) {
            player.displayClientMessage(Component.literal("右键电线杆中的父级..."), false);
            if (level.getBlockEntity(pos) instanceof EnderPoleBlockEntity _this) {
                player.displayClientMessage(Component.literal("确认为有效的电线杆..."), false);
                if (player.isShiftKeyDown()) {
                    _this.Set_Power(10, true);
                    player.displayClientMessage(Component.literal(
                        Component.translatable("message.ender_pole.set_power_add").append(
                            Component.literal()
                        )), false);
                } else {
                    _this.Set_Power(10, false);
                    player.displayClientMessage(Component.translatable("message.ender_pole.set_power_sub"), false);
                }
                level.setBlock(pos, state.setValue(IsFather, false), 3);
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
        Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(ModItems.ENDERPOLE_ITEM)) {
            if (stack.has(DataComponents.TARGET_POS)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (state.is(ModBlocks.ENDERPOLE.get())) {
                stack.set(DataComponents.TARGET_DIM.get(), level.dimension().location());
                stack.set(DataComponents.TARGET_POS.get(), pos);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

}
