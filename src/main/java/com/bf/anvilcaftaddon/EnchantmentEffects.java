package com.bf.anvilcaftaddon;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EnchantmentEffects {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(Registries.ENCHANTMENT, AnvilCaftAddon.MODID);
    public static final DeferredHolder<Enchantment, Enchantment> DOUBLE_JUMP =
            ENCHANTMENTS.register("double_jump", () -> {
                var boots = BuiltInRegistries.ITEM.getOrCreateTag(ItemTags.FOOT_ARMOR);
                return Enchantment.enchantment(
                        Enchantment.definition(
                                boots,
                                5,
                                1,
                                Enchantment.constantCost(15),
                                Enchantment.constantCost(35),
                                1,
                                EquipmentSlotGroup.FEET
                        )
                ).build(ResourceLocation.parse(AnvilCaftAddon.MODID + "/double_jump"));
            });//雷·跃
    public static final DeferredHolder<Enchantment, Enchantment> DOUBLE_WALK =
            ENCHANTMENTS.register("double_walk", () -> {
                var boots = BuiltInRegistries.ITEM.getOrCreateTag(ItemTags.FOOT_ARMOR);
                return Enchantment.enchantment(
                        Enchantment.definition(
                                boots,
                                5,
                                1,
                                Enchantment.constantCost(15),
                                Enchantment.constantCost(35),
                                1,
                                EquipmentSlotGroup.LEGS
                        )
                ).build(ResourceLocation.parse(AnvilCaftAddon.MODID + "/double_jump"));
            });//雷·行


    public static void register(IEventBus modEventBus) {
        EnchantmentEffects.ENCHANTMENTS.register(modEventBus);
    }
}
