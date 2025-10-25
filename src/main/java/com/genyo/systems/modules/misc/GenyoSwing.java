package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.events.entity.SwingEvent;
import com.genyo.systems.modules.GenyoModule;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;

public class GenyoSwing extends GenyoModule {

    public GenyoSwing() {
        super(Genyo.MISC, "genyo-swing", "Change the swinginess (?) of the player's hand");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<SwingHand> swingHand = sgGeneral.add(new EnumSetting.Builder<SwingHand>()
        .name("Hand")
        .description("The player swinging hand")
        .defaultValue(SwingHand.MAINHAND)
        .build()
    );

    //
    private Hand prevPreferredHand;
    private Hand hand;

    @Override
    public void onActivate()
    {
        if (mc.player != null)
        {
            prevPreferredHand = mc.player.preferredHand;
            if (swingHand.get() != SwingHand.PACKET)
            {
                updateSwingHand(swingHand.get());
            }
        }
    }

    @Override
    public void onDeactivate()
    {
        if (mc.player != null)
        {
            mc.player.preferredHand = prevPreferredHand;
        }
    }

    @EventHandler
    public void onSwing(SwingEvent event)
    {
        updateSwingHand(swingHand.get());
    }

    private void updateSwingHand(SwingHand swingHand)
    {
        hand = switch (swingHand)
        {
            case MAINHAND -> Hand.MAIN_HAND;
            case OFFHAND -> Hand.OFF_HAND;
            case SWAP ->
            {
                if (!mc.player.handSwinging
                    || mc.player.handSwingTicks >= getHandSwingDuration() / 2
                    || mc.player.handSwingTicks < 0)
                {
                    yield hand != Hand.MAIN_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND;
                }
                yield hand;
            }
            case PACKET ->
            {
                mc.player.handSwingTicks = 0;
                mc.player.handSwinging = false;
                yield null;
            }
        };
        if (hand != null)
        {
            mc.player.preferredHand = hand;
        }
    }

    public int getHandSwingDuration()
    {
        if (StatusEffectUtil.hasHaste(mc.player)) {
            return 6 - (1 + StatusEffectUtil.getHasteAmplifier(mc.player));
        }
        return mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE) ?
            6 + (1 + mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) * 2 : 6;
    }

    public Hand getSwingHand()
    {
        return hand;
    }

    public Hand getPrevPreferredHand()
    {
        return prevPreferredHand;
    }

    private enum SwingHand
    {
        MAINHAND,
        OFFHAND,
        SWAP,
        PACKET
    }

}
