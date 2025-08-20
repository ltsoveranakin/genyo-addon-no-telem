package com.genyo.mixin.network;

import com.genyo.GenyoAddon;
import com.genyo.mixin.accessor.AccessorClientConnection;
import com.genyo.imixins.IClientPlayNetworkHandler;
import com.genyo.systems.modules.misc.Einstein;
import com.genyo.systems.modules.movement.GenyoVelocity;
import meteordevelopment.meteorclient.mixininterface.IExplosionS2CPacket;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler implements IClientPlayNetworkHandler  {

    @Unique
    private boolean ignoreChatMessage;

    @Shadow
    public abstract void sendChatMessage(String content);

    @Shadow
    public ClientConnection getConnection() {
        return null;
    }

    protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
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

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (ignoreChatMessage) return;

        Einstein einstein = Modules.get().get(Einstein.class);
        if (Modules.get().isActive(Einstein.class)) {
            if (einstein.isInGame() && isChoice(message)) {
                ci.cancel();

                String correct = einstein.getCorrectChoice();

                einstein.endGame(message.equals(correct));
            }
        }
    }

    @Unique
    private boolean isChoice(String message) {
        final List<String> choices = Arrays.asList("A", "B", "C", "D");

        return choices.contains(message.toUpperCase());
    }

}
