package com.genyo.mixin;

import com.genyo.events.AttackBlockEvent;
import com.genyo.events.network.ItemDesyncEvent;
import com.genyo.events.network.PacketSneakingEvent;
import com.genyo.events.network.StrafeFixEvent;
import com.genyo.managers.Managers;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Shadow
    private GameMode gameMode;

    @Shadow
    protected abstract void syncSelectedSlot();

    @Shadow
    protected abstract void sendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator);

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

    /**
     * @param player
     * @param hand
     * @param cir
     */
    @Inject(method = "interactItem", at = @At(value = "HEAD"), cancellable = true)
    public void hookInteractItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        StrafeFixEvent strafeFixEvent = new StrafeFixEvent();
        MeteorClient.EVENT_BUS.post(strafeFixEvent);

        if (strafeFixEvent.isCancelled()) {
            cir.cancel();
            if (this.gameMode == GameMode.SPECTATOR)
            {
                cir.setReturnValue(ActionResult.PASS);
                return;
            }
            syncSelectedSlot();
            MutableObject<ActionResult> mutableObject = new MutableObject();
            this.sendSequencedPacket(mc.world, (sequence) ->
            {
                PlayerInteractItemC2SPacket playerInteractItemC2SPacket = new PlayerInteractItemC2SPacket(
                    hand, sequence, Managers.ROTATION.isRotating() ? Managers.ROTATION.getRotationYaw() : player.getYaw(),
                    Managers.ROTATION.isRotating() ? Managers.ROTATION.getRotationPitch() : player.getPitch());
                ItemStack itemStack = player.getStackInHand(hand);
                if (player.getItemCooldownManager().isCoolingDown(itemStack)) {
                    mutableObject.setValue(ActionResult.PASS);
                    return playerInteractItemC2SPacket;
                } else {
                    ActionResult actionResult = itemStack.use(mc.world, player, hand);
                    if (actionResult.isAccepted()) {
                        ItemStack itemStack2 = ActionResult.SUCCESS.getNewHandStack();
                        if (itemStack2 != itemStack) {
                            player.setStackInHand(hand, itemStack2);
                        }

                        mutableObject.setValue(actionResult);
                        return playerInteractItemC2SPacket;
                    } else {
                        return playerInteractItemC2SPacket;
                    }
                }
            });
            cir.setReturnValue(mutableObject.getValue());
        }
    }

}
