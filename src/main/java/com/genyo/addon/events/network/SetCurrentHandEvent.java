package com.genyo.addon.events.network;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class SetCurrentHandEvent extends Cancellable {

    private static final SetCurrentHandEvent INSTANCE = new SetCurrentHandEvent();

    public Hand hand;

    public static SetCurrentHandEvent get(Hand hand) {
        INSTANCE.hand = hand;

        return INSTANCE;
    }

    public ItemStack getStackInHand()
    {
        return mc.player.getStackInHand(hand);
    }

}
