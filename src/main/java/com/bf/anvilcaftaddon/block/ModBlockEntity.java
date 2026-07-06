package com.bf.anvilcaftaddon.block;

import com.bf.anvilcaftaddon.AnvilCaftAddon;
import com.bf.anvilcaftaddon.block.blocks.PowerBlock.PowerBlockEntity;
import com.bf.anvilcaftaddon.block.blocks.ender_transmission_pole.EnderPoleBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AnvilCaftAddon.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PowerBlockEntity>> APOWER_BE =
            BLOCK_ENTITIES.register("power_block_entity", () ->
                    BlockEntityType.Builder.of(PowerBlockEntity::new, ModBlocks.POWER_BLOCK.get()).build(null)
            );

    public static final Supplier<BlockEntityType<EnderPoleBlockEntity>> ENDER_POLE_ENTITY =
            BLOCK_ENTITIES.register("ender_pole_entity", () ->
                    BlockEntityType.Builder.of(
                            EnderPoleBlockEntity::new,
                            ModBlocks.ENDERPOLE.get() // 核心：这里直接传你的方块对象，不要写任何过滤
                    ).build(null)
            );


    public static void register(net.neoforged.bus.api.IEventBus modEventBus) {
        ModBlockEntity.BLOCK_ENTITIES.register(modEventBus);
    }
}
