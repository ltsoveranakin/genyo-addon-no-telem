package com.genyo.addon.modules.combat;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.events.entity.EntityDeathEvent;
import com.genyo.addon.events.network.DisconnectEvent;
import com.genyo.addon.events.network.PlayerTickEvent;
import com.genyo.addon.modules.GenyoModule;
import com.genyo.addon.utils.math.timer.CacheTimer;
import com.genyo.addon.utils.math.timer.Timer;
import com.genyo.addon.utils.player.InventoryUtil;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GenyoReplenish extends GenyoModule {

    public GenyoReplenish() {
        super(GenyoAddon.GENYO, "genyo-replenish", "fwejhfkljwefklwejfklkwlefjlwefl");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> percentConfig = sgGeneral.add(new IntSetting.Builder()
        .name("Percent")
        .description("The minimum percent of total stack before replenishing")
        .min(1)
        .defaultValue(25)
        .max(80)
        .build()
    );

    private final Setting<Boolean> resistantConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Allow Resistant")
        .description("Refills obsidian with other types of resistant blocks")
        .defaultValue(false)
        .build()
    );

    // Cached hotbar in case the hotbar slot becomes empty
    private final Map<Integer, ItemStack> hotbarCache = new ConcurrentHashMap<>();

    private final Timer lastDroppedTimer = new CacheTimer();

    @Override
    public void onDeactivate()
    {
        hotbarCache.clear();
    }

    @EventHandler
    public void onDisconnect(DisconnectEvent event)
    {
        hotbarCache.clear();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        if (event.entity instanceof ClientPlayerEntity)
        {
            hotbarCache.clear();
        }
    }

    @EventHandler
    public void onTick(PlayerTickEvent event)
    {
        if (mc.options.dropKey.isPressed())
        {
            lastDroppedTimer.reset();
        }

        boolean pauseReplenish = isInInventoryScreen() || !lastDroppedTimer.passed(100);

        if (!pauseReplenish)
        {
            for (int i = 0; i < 9; i++)
            {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.isEmpty())
                {
                    ItemStack cachedStack = hotbarCache.getOrDefault(i, null);
                    if (cachedStack != null && !cachedStack.isEmpty())
                    {
                        replenishStack(i, cachedStack);
                        break;
                    }
                    continue;
                }

                if (!stack.isStackable())
                {
                    continue;
                }

                double percentage = ((double) stack.getCount() / stack.getMaxCount()) * 100.0;
                if (percentage <= percentConfig.get())
                {
                    replenishStack(i, stack);
                    break;
                }
            }
        }

        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() && !pauseReplenish)
            {
                continue;
            }

            if (hotbarCache.containsKey(i))
            {
                hotbarCache.replace(i, stack.copy());
            }
            else
            {
                hotbarCache.put(i, stack.copy());
            }
        }
    }

    public boolean isInInventoryScreen()
    {
        return mc.currentScreen instanceof GenericContainerScreen || mc.currentScreen instanceof ShulkerBoxScreen || mc.currentScreen instanceof InventoryScreen;
    }

    private void replenishStack(int slot, ItemStack stack)
    {
        int slot1 = -1;
        boolean outOfObsidian = stack.getItem() == Items.OBSIDIAN && InventoryUtil.count(Items.OBSIDIAN) <= 1;
        for (int i = 9; i < 36; ++i)
        {
            ItemStack itemStack = mc.player.getInventory().getStack(i);

            if (itemStack.isEmpty())
            {
                continue;
            }

            if (!isSame(stack, itemStack, outOfObsidian) || !itemStack.isStackable())
            {
                continue;
            }

            slot1 = i;
        }

        if (slot1 != -1)
        {
            // sendModuleError("slot: " + slot + ", stack:" + stack.getName().getString());
            mc.interactionManager.clickSlot(0, slot1, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(0, slot + 36, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty())
            {
                mc.interactionManager.clickSlot(0, slot1, 0, SlotActionType.PICKUP, mc.player);
            }
        }
    }

    public boolean isSame(ItemStack stack1, ItemStack stack2, boolean outOfObsidian)
    {
        if (resistantConfig.get() && stack1.getItem() == Items.OBSIDIAN && outOfObsidian)
        {
            return stack2.getItem() == Items.ENDER_CHEST || stack2.getItem() == Items.CRYING_OBSIDIAN;
        }

        else if (stack1.getItem() instanceof BlockItem blockItem
            && (!(stack2.getItem() instanceof BlockItem blockItem1) || blockItem.getBlock() != blockItem1.getBlock()))
        {
            return false;
        }

        else if (!stack1.getName().getString().equals(stack2.getName().getString()))
        {
            return false;
        }

        return stack1.getItem().equals(stack2.getItem());
    }

}
