package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.utils.math.timer.TickTimer;
import com.genyo.utils.math.timer.Timer;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Arrays;
import java.util.List;

public class AutoRename extends GenyoModule {

    public AutoRename() {
        super(Genyo.MISC, "auto-rename", "Can I talk my shit again?");
    }

    private final List<Item> defaultItems = Arrays.asList(Items.SHULKER_BOX, Items.WHITE_SHULKER_BOX,
        Items.LIGHT_GRAY_SHULKER_BOX, Items.GRAY_SHULKER_BOX, Items.BLACK_SHULKER_BOX, Items.BROWN_SHULKER_BOX, Items.RED_SHULKER_BOX, Items.ORANGE_SHULKER_BOX,
        Items.YELLOW_SHULKER_BOX, Items.LIME_SHULKER_BOX, Items.GREEN_SHULKER_BOX, Items.CYAN_SHULKER_BOX, Items.LIGHT_BLUE_SHULKER_BOX, Items.BLUE_SHULKER_BOX,
        Items.PURPLE_SHULKER_BOX, Items.PINK_SHULKER_BOX);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("Items")
        .description("Select items to rename")
        .defaultValue(defaultItems)
        .build()
    );

    private final Setting<String> text = sgGeneral.add(new StringSetting.Builder()
        .name("Text")
        .description("Asd")
        .defaultValue("Even if I don't hit again?")
        .build()
    );

    private final Setting<Integer> tickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("Tick Delay")
        .description("Dawg, are you fucking kidding?")
        .min(0)
        .defaultValue(10)
        .max(20)
        .build()
    );

    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("Chat Feedback")
        .description("Gives you a sweet message when it's done")
        .defaultValue(false)
        .build()
    );

    private final Timer delayTimer = new TickTimer();

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null ||
        !(mc.currentScreen instanceof AnvilScreen anvilScreen)) return;

        if (!delayTimer.passed(tickDelay.get())) return;

        if (mc.player.experienceLevel <= 0 && !mc.player.isCreative()) {
            if (debug.get()) sendError("Not enough XP level.");
            return;
        }

        final AnvilScreenHandler screenHandler = anvilScreen.getScreenHandler();
        if (!screenHandler.getSlot(1).getStack().isEmpty()) {
            moveToEmptySlot(screenHandler, 1);
            return;
        }
        if (!screenHandler.getSlot(0).getStack().isEmpty()) {
            moveToEmptySlot(screenHandler, 0);
            return;
        }
        if (!screenHandler.getSlot(2).getStack().isEmpty()) {
            moveToEmptySlot(screenHandler, 2);
            return;
        }

        AnvilScreenHandler handler = (AnvilScreenHandler) mc.player.currentScreenHandler;

        for (int i = 3; i < 36 + 3; i++)
        {
            final ItemStack itemStack = screenHandler.getSlot(i).getStack();

            if (itemStack.isEmpty() || equalsName(itemStack, text.get())) continue;

            if (!items.get().contains(itemStack.getItem())) continue;

            final String name = (!text.get().trim().isEmpty() ? text.get() : "");
            mc.interactionManager.clickSlot(screenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(screenHandler.syncId, screenHandler.getSlot(0).id, 0, SlotActionType.PICKUP, mc.player);

            ((AnvilScreen) mc.currentScreen).nameField.setText(name);
            handler.updateToClient();
            handler.updateResult();

            if (debug.get()) sendInfo("Successfully renamed item in slot: " + i + ".");

            mc.interactionManager.clickSlot(screenHandler.syncId, screenHandler.getSlot(2).id, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(screenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
            break;
        }
        delayTimer.reset();
    }

    private void moveToEmptySlot(final AnvilScreenHandler screenHandler, final int slot) {
        if (mc.interactionManager == null) return;

        for (int i = 3; i < 36 + 3; i++) {
            final ItemStack itemStack = screenHandler.getSlot(i).getStack();
            if (itemStack.isEmpty()) {
                mc.interactionManager.clickSlot(screenHandler.syncId, screenHandler.getSlot(slot).id, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(screenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                return;
            }
        }
        mc.interactionManager.clickSlot(screenHandler.syncId, screenHandler.getSlot(slot).id, 0, SlotActionType.THROW, mc.player);
    }

    private boolean equalsName(final ItemStack itemStack, final String itemName) {
        if (itemName.trim().isEmpty()) {
            return itemStack.get(DataComponentTypes.CUSTOM_NAME) == null;
        } else {
            return itemStack.getName().getString().equals(itemName);
        }
    }

}
