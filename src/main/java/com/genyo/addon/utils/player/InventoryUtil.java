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

    public static boolean hasItemInInventory(final Item item, final boolean hotbar)
    {
        final int startSlot = hotbar ? 0 : 9;
        for (int i = startSlot; i < 36; ++i)
        {
            final ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (!itemStack.isEmpty() && itemStack.getItem() == item)
            {
                return true;
            }
        }
        return false;
    }

}
