package com.bf.anvilcaftaddon;

import com.bf.anvilcaftaddon.Packet.DoubleJumpPacket;
import com.bf.anvilcaftaddon.Packet.DoubleWalk;
import com.bf.anvilcaftaddon.Packet.ResetDoubleJumpPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(value = Dist.CLIENT)
public class EventBus {
    // 记住上一 tick 的空格按键状态
    private static boolean prevJumpDown = false;
    // 记住上一 tick 的Ctrl按键状态
    private static boolean prevCtrlDown = false;
    // 记住上一 tick 是否在地面
    private static boolean wasOnGround = true;
    private static boolean wasOnFly = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {//客户端每帧
        Minecraft mc = Minecraft.getInstance();//mc实例
        LocalPlayer player = mc.player;//玩家
        if (player == null) return;//如果不存在玩家，直接取消
        ItemStack boots = player.getInventory().armor.get(0);//获取靴子
        ItemStack legs = player.getInventory().armor.get(1);//获取护腿
        Boolean canJump = boots.get(DataComponents.CAN_DOUBLE_JUMP);//获取物品组件状态

        boolean isJumpDown = mc.options.keyJump.isDown();//获取空格键的状态
        boolean isCtrlDown = mc.options.keySprint.isDown();//获取Ctrl键的状态
        boolean isOnGround = player.onGround();
        boolean isOnFly = player.getAbilities().flying;

        //player.displayClientMessage(Component.literal("Jump key is " + (isJumpDown ? "down" : "up")), false);

        TryJump(isJumpDown,isOnFly, boots, player, canJump);
        if (isOnGround && !wasOnGround) {
            ResetDoubleJump(isOnGround, boots, canJump);
        }
        TrySping(isCtrlDown, legs, player);



        // 更新记录状态（为下一 tick 准备）
        prevJumpDown = isJumpDown;
        prevCtrlDown = isCtrlDown;
        wasOnGround = isOnGround;
        wasOnFly = isOnFly;
    }

    private static void TryJump(boolean isJumpDown, boolean isOnFly, ItemStack boots, LocalPlayer player, Boolean canJump) { //尝试进行跳跃
        if (isJumpDown && !prevJumpDown) {//按下空格且上一秒空格键没有按下时
            //player.displayClientMessage(Component.literal("Double jump activated!"),false);

            if (!boots.isEmpty()//鞋子不为空
                    && !player.onGround()//玩家不在地面
                    && !isOnFly//玩家没在飞
                    && !player.isSwimming()//玩家没在潜水
                    && boots.getEnchantmentLevel(EnchantmentEffects.DOUBLE_JUMP) > 0//具有附魔
            ) {
                //player.displayClientMessage(Component.literal(DataComponents.CAN_DOUBLE_JUMP.toString()+":" + canJump), false);
                if (canJump != null && canJump && !wasOnGround && !wasOnFly) {
                    var motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.x, 0.42F, motion.z);
                    player.fallDistance = 0;

                    //通知服务端
                    PacketDistributor.sendToServer(new DoubleJumpPacket());
                }
            }
        }
    }

    private static void ResetDoubleJump(boolean isOnGround, ItemStack boots, Boolean canJump){
        if (isOnGround && (canJump == null || !canJump)) {
            if (!boots.isEmpty() && boots.getEnchantmentLevel(EnchantmentEffects.DOUBLE_JUMP) > 0) {
                // 通知服务端重置
                PacketDistributor.sendToServer(new ResetDoubleJumpPacket());
            }
        }
    }

    private static void TrySping(boolean isCtrlDown, ItemStack legs, LocalPlayer player){
        if (isCtrlDown && !prevCtrlDown) {//按下Ctrl且上一秒Ctrl键没有按下时

            //player.displayClientMessage(Component.literal(legs.toString()),false);

            if (!legs.isEmpty()//护腿不为空
                    && legs.getEnchantmentLevel(EnchantmentEffects.DOUBLE_WALK) > 0//具有附魔
            ) {

                if (player.hurtTime > 0) return;
                float yaw = player.getYRot();

                Vec3 viewVector = player.getDeltaMovement();
                //player.setDeltaMovement(dirX * 1.5 * level, 0, dirZ * 1.5 * level);
                if (viewVector.x == 0 && viewVector.z == 0){
                    double dirX = -Math.sin(yaw * Math.PI / 180.0);
                    double dirZ = Math.cos(yaw * Math.PI / 180.0);
                    player.setDeltaMovement(dirX*2, 0, dirZ*2);
                }
                else player.setDeltaMovement(viewVector.x*10, viewVector.y*10, viewVector.z*10);

                PacketDistributor.sendToServer(new DoubleWalk());
            }
        }
    }
}