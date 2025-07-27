package com.genyo.addon.mixin.meteor;

import com.genyo.addon.events.meteor.SettingChangedEvent;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.Setting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Setting.class)
public class MixinSetting {

    /*@Inject(method = "onChanged()V", at = @At("TAIL"))
    protected void injectOnChanged(CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(SettingChangedEvent.get((Setting<?>) (Object) this));
    }*/

}
