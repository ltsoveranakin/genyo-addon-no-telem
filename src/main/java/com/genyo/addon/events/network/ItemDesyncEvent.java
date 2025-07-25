package com.genyo.addon.events.network;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.item.ItemStack;

public class ItemDesyncEvent extends Cancellable {

    private static final ItemDesyncEvent INSTANCE = new ItemDesyncEvent();

    public ItemStack stack;

    public static ItemDesyncEvent get(ItemStack stack) {
        INSTANCE.stack = stack;

        return INSTANCE;
    }

    public void setStack(ItemStack stack)
    {
        this.stack = stack;
    }

}
