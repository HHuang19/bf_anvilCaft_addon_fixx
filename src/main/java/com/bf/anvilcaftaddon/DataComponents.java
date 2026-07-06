package com.bf.anvilcaftaddon;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DataComponents {//用于注册数据类型

    //注册Anvil_state用于在物品里存放铁砧
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AnvilCaftAddon.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockState>> ANVIL_STATE =
            DATA_COMPONENTS.register("anvil_state", () -> DataComponentType.<BlockState>builder()
                    .persistent(BlockState.CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CAN_DOUBLE_JUMP =
            DATA_COMPONENTS.register("can_double_jump", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> TARGET_DIM  =
            DATA_COMPONENTS.register("target_dim", () -> DataComponentType.<ResourceLocation>builder()
                    .persistent(ResourceLocation.CODEC)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> TARGET_POS =
            DATA_COMPONENTS.register("target_pos", () -> DataComponentType.<BlockPos>builder()
                    .persistent(BlockPos.CODEC)
                    .build());




    public static void register(IEventBus modEventBus) {
        DataComponents.DATA_COMPONENTS.register(modEventBus);
    }
}
