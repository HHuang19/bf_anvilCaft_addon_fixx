package com.bf.anvilcaftaddon;


import com.bf.anvilcaftaddon.block.ModBlocks;
import com.bf.anvilcaftaddon.block.blocks.ender_transmission_pole.EnderPoleBlock;
import com.bf.anvilcaftaddon.block.blocks.ender_transmission_pole.EnderPoleBlockEntity;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.block.state.Vertical3PartHalf;
import dev.dubhe.anvilcraft.data.AnvilCraftDatagen;
import it.unimi.dsi.fastutil.Stack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

//用于创建物品的类
public class ModItems {
    //延迟注册器，注册物品
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AnvilCaftAddon.MODID);

    public static DeferredItem<Item> Magnet =
            ITEMS.register("magnet",() -> new Item(new Item.Properties().stacksTo(1)
                    //food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()))
            ){
                @Override
                public InteractionResult useOn(UseOnContext context) {//实现收放铁砧
                    if (!context.getLevel().isClientSide && !context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.AIR)) {
                        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
                        ItemStack _This = context.getItemInHand();
                        BlockState AnvilState = context.getItemInHand().get(DataComponents.ANVIL_STATE);
                        if (AnvilState == null || AnvilState.equals(Blocks.AIR.defaultBlockState())) {
                            if (blockState.is(BlockTags.ANVIL)) {
    //                            context.getLevel().setBlock(context.getClickedPos(), Blocks.AIR.defaultBlockState(), 3);将目标方块先安全换成空气
    //                            context.getLevel().setBlock(new BlockPos(获取目标位置(比原铁砧高一格)
    //                                context.getClickedPos().getX(), context.getClickedPos().getY() + 1, context.getClickedPos().getZ())
    //                                    , blockState, 3);将铁砧放下
                                //以上为已弃用的功能-w-
                                if (AnvilState == null || AnvilState.equals(Blocks.AIR.defaultBlockState())) {
                                    _This.set(DataComponents.ANVIL_STATE, blockState);
                                    context.getLevel().setBlock(context.getClickedPos(), Blocks.AIR.defaultBlockState(), 3);
                                    return InteractionResult.SUCCESS;//结束事件
                                }
                            }
                        }
                        else {
                            BlockPos _TargetBlock = context.getClickedPos().relative(context.getClickedFace());
                            if (context.getLevel().getBlockState(_TargetBlock).is(Blocks.AIR)) {
                                context.getLevel().setBlock(_TargetBlock, Objects.requireNonNull(_This.get(DataComponents.ANVIL_STATE.get())), 3);
                                _This.set(DataComponents.ANVIL_STATE, Blocks.AIR.defaultBlockState());
                                return InteractionResult.SUCCESS;//结束事件
                            } else {
                                return InteractionResult.FAIL;
                            }
                        }
                        return InteractionResult.SUCCESS;
                    }

                    return super.useOn(context);
                }

                @Override
                public boolean isFoil(ItemStack stack) {//当物品携带铁砧时发光
                    return stack.get(DataComponents.ANVIL_STATE) != null && !stack.get(DataComponents.ANVIL_STATE).is(Blocks.AIR);
                }

                @Override
                public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                    if (stack.get(DataComponents.ANVIL_STATE) != null && !stack.get(DataComponents.ANVIL_STATE).is(Blocks.AIR)) {
                        tooltipComponents.add(Component.translatable("item.anvilcaftaddon.magnet.AVONtooltip").append(
                                Component.literal(stack.get(DataComponents.ANVIL_STATE).getBlock().asItem().toString())
                        ));
                    }else {
                        tooltipComponents.add(Component.translatable("item.anvilcaftaddon.magnet.AVOFFtooltip"));
                    }
                    super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                }
            });
    public static final DeferredItem<BlockItem> ENDERPOLE_ITEM =
        ITEMS.register("enderpole", () -> new BlockItem(ModBlocks.ENDERPOLE.get(), new Item.Properties()) {
            @Override
            public InteractionResult useOn(UseOnContext context) {
                if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof EnderPoleBlockEntity B_E) {
                    ItemStack _This = context.getItemInHand();
                    BlockEntity TState = switch (B_E.getBlockState().getValue(EnderPoleBlock.PARTHALF)){
                        case Vertical3PartHalf.TOP -> context.getLevel().getBlockEntity(context.getClickedPos());
                        case Vertical3PartHalf.MID -> context.getLevel().getBlockEntity(context.getClickedPos().above(1));
                        case Vertical3PartHalf.BOTTOM -> context.getLevel().getBlockEntity(context.getClickedPos().above(2));
                    };
                    _This.set(DataComponents.TARGET_POS, TState.getBlockPos());
                    _This.set(DataComponents.TARGET_DIM, TState.getLevel().dimension().location());
                    context.getPlayer().displayClientMessage(Component.translatable("item.anvilcaftaddon.enderpole.tooltip"), true);
                    return InteractionResult.SUCCESS;
                }
                return super.useOn(context);
            }

            @Override
            protected boolean updateCustomBlockEntityTag(
                BlockPos pos,
                Level level,
                @Nullable Player player,
                ItemStack stack,
                BlockState state
            ) {
                if (level.getBlockEntity(pos.above(2)) instanceof EnderPoleBlockEntity be) {
                    ResourceLocation dim = stack.get(DataComponents.TARGET_DIM);
                    BlockPos targetPos = stack.get(DataComponents.TARGET_POS);
                    if (dim != null && targetPos != null) {
                        be.Set_Outer(dim, targetPos);  // 标记为子杆
                        be.setChanged();
                    }
                }
                return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
            }
        });

    public static final DeferredItem<BlockItem> POWER_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("power_block", ModBlocks.POWER_BLOCK);




    public static void register(IEventBus eventBus) {
        // 传入eventBus，使这个类将它们都注册
        ITEMS.register(eventBus);
    }
}
