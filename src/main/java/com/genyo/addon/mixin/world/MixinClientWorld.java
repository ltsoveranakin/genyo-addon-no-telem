package com.genyo.addon.mixin.world;

import com.genyo.addon.events.world.AddEntityEvent;
import com.genyo.addon.events.world.RemoveEntityEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.entity.EntityLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld {

    @Shadow
    protected abstract EntityLookup<Entity> getEntityLookup();

    /**
     * @param entityId
     * @param removalReason
     * @param ci
     */
    @Inject(method = "removeEntity", at = @At(value = "HEAD"))
    private void hookRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci) {
        Entity entity = getEntityLookup().get(entityId);
        if (entity == null) return;

        RemoveEntityEvent removeEntityEvent = RemoveEntityEvent.get(entity, removalReason);
        MeteorClient.EVENT_BUS.post(removeEntityEvent);
    }

    /**
     * @param entity
     * @param ci
     */
    @Inject(method = "addEntity", at = @At(value = "HEAD"))
    private void hookAddEntity(Entity entity, CallbackInfo ci)
    {
        MeteorClient.EVENT_BUS.post(AddEntityEvent.get(entity));
    }

}
