package com.genyo.addon.utils.player;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class InventoryUtil {

    /**
     * @param item
     * @return
     */
    public static int count(Item item)
    {
        if (mc.player == null)
        {
            return 0;
        }
        ItemStack offhandStack = mc.player.getOffHandStack();
        int itemCount = offhandStack.getItem() == item ? offhandStack.getCount() : 0;
        for (int i = 0; i < 36; i++)
        {
            ItemStack slot = mc.player.getInventory().getStack(i);
            if (slot.getItem() == item)
            {
                itemCount += slot.getCount();
            }
        }
        return itemCount;
    }

}
