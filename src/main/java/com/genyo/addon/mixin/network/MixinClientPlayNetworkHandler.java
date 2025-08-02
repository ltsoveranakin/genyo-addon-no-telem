package com.genyo.addon.mixin.network;

import com.genyo.addon.mixin.accessor.AccessorClientConnection;
import com.genyo.addon.imixins.IClientPlayNetworkHandler;
import com.genyo.addon.modules.movement.GenyoVelocity;
import meteordevelopment.meteorclient.mixininterface.IExplosionS2CPacket;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler implements IClientPlayNetworkHandler {

    @Shadow
    public ClientConnection getConnection() {
        return null;
    }

    @Inject(method = "onExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void onExplosionVelocity(ExplosionS2CPacket packet, CallbackInfo ci) {
        GenyoVelocity velocity = Modules.get().get(GenyoVelocity.class);
        if (velocity.explosionConfig.get()) {
            IExplosionS2CPacket explosionPacket = (IExplosionS2CPacket) (Object) packet;
            explosionPacket.meteor$setVelocityX((float) (packet.playerKnockback().orElse(Vec3d.ZERO).x * velocity.getHorizontal(velocity.explosionsHorizontal)));
            explosionPacket.meteor$setVelocityY((float) (packet.playerKnockback().orElse(Vec3d.ZERO).y * velocity.getVertical(velocity.explosionsVertical)));
            explosionPacket.meteor$setVelocityZ((float) (packet.playerKnockback().orElse(Vec3d.ZERO).z * velocity.getHorizontal(velocity.explosionsHorizontal)));
        }
    }

    @Override
    public void sendQuietPacket(Packet<?> packet)
    {
        ((AccessorClientConnection) getConnection()).hookSendInternal(packet, null, true);
    }

}
