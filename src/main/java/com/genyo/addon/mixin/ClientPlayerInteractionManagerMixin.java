package com.genyo.addon.mixin;

import com.genyo.addon.events.AttackBlockEvent;
import com.genyo.addon.events.network.ItemDesyncEvent;
import com.genyo.addon.events.network.PacketSneakingEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    /**
     * @param pos
     * @param direction
     * @param cir
     */
    @Inject(method = "attackBlock", at = @At(value = "HEAD"), cancellable = true)
    private void hookAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = mc.world.getBlockState(pos);
        if (MeteorClient.EVENT_BUS.post(AttackBlockEvent.get(pos, state, direction)).isCancelled()) cir.cancel();
    }

    @Redirect(
        method = "interactBlockInternal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack hookRedirectInteractBlockInternal$getStackInHand(ClientPlayerEntity entity, Hand hand)
    {
        if (hand.equals(Hand.OFF_HAND))
        {
            return entity.getStackInHand(hand);
        }
        ItemDesyncEvent itemDesyncEvent = new ItemDesyncEvent();
        MeteorClient.EVENT_BUS.post(itemDesyncEvent);

        return itemDesyncEvent.isCancelled() ? itemDesyncEvent.stack : entity.getStackInHand(Hand.MAIN_HAND);
    }

    @Redirect(
        method = "interactBlockInternal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",
            ordinal = 0))
    private boolean hookRedirectInteractBlockInternal$getMainHandStack(ItemStack instance)
    {
        ItemDesyncEvent itemDesyncEvent = new ItemDesyncEvent();
        MeteorClient.EVENT_BUS.post(itemDesyncEvent);

        return itemDesyncEvent.isCancelled() ? itemDesyncEvent.stack.isEmpty() : instance.isEmpty();
    }

    @Redirect(method = "interactBlockInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldCancelInteraction()Z"))
    private boolean hookRedirectInteractBlockInternal$shouldCancelInteraction(ClientPlayerEntity player)
    {
        PacketSneakingEvent packetSneakingEvent = new PacketSneakingEvent();
        MeteorClient.EVENT_BUS.post(packetSneakingEvent);
        return player.isSneaking() || packetSneakingEvent.isCancelled();
    }

}
