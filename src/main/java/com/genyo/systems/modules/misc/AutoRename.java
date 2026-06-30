package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.mixin.accessor.AccessorAnvilScreen;
import com.genyo.mixin.accessor.AccessorAnvilScreenHandler;
import com.genyo.systems.modules.GenyoModule;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.EXPThrower;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.sound.SoundEvents;

import java.util.List;

public class AutoRename extends GenyoModule {

    private static final int ANVIL_OFFSET = 3;
    private final Setting<List<Item>> itemList = settings.getDefaultGroup().add(new ItemListSetting.Builder()
        .name("items")
        .description("Items to automatically rename (or exclude from being renamed, if blacklist mode is enabled.)")
        .build()
    );

    private final Setting<String> itemName = settings.getDefaultGroup().add(new StringSetting.Builder()
        .name("custom-name")
        .description("The name you want to give to qualifying items.")
        .defaultValue("")
        .onChanged(name -> {
            if (name.length() > AnvilScreenHandler.MAX_NAME_LENGTH) {
                sendError("Custom name exceeds max accepted length!");
            }
        })
        .build()
    );

    private final Setting<Boolean> blacklistMode = settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("blacklist-mode")
        .description("Rename all items except the ones selected in the Items list.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> renameNamed = settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("rename-prenamed")
        .description("Rename items which already have a different custom name.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> muteAnvils = settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("mute-anvils")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pingOnDone = settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("sound-ping")
        .description("Play a sound cue when no more items can be renamed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> pingVolume = settings.getDefaultGroup().add(new DoubleSetting.Builder()
        .name("ping-volume")
        .sliderMin(0.0)
        .sliderMax(5.0)
        .defaultValue(1.0)
        .build()
    );

    private final Setting<Boolean> closeOnDone = settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("close-anvil")
        .description("Automatically close the anvil screen when no more items can be renamed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> disableOnDone = settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("disable-on-done")
        .description("Automatically disable the module when no more items can be renamed.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> enableExpThrower = settings.getDefaultGroup().add(new BoolSetting.Builder()
        .name("enable-exp-thrower")
        .description("Automatically enable the Exp Thrower module when no more items can be renamed.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> tickRate = settings.getDefaultGroup().add(new IntSetting.Builder()
        .name("tick-rate")
        .min(0).max(1000)
        .sliderRange(0, 100)
        .defaultValue(0)
        .build()
    );

    private int timer = 0;
    private boolean notified = false;
    public AutoRename() {
        super(Genyo.MISC, "auto-rename", "Auto renames items");
    }

    public boolean shouldMute() {
        return muteAnvils.get();
    }

    private boolean hasValidItems(AnvilScreenHandler handler) {
        if (mc.player == null) return false;
        for (int n = 0; n < 36 + ANVIL_OFFSET; n++) {
            if (n == 2) continue;
            ItemStack stack = handler.getSlot(n).getStack();
            if ((blacklistMode.get() && !itemList.get().contains(stack.getItem()))
                || (!blacklistMode.get() && itemList.get().contains(stack.getItem()))) {
                if (itemName.get().isBlank() && stack.contains(DataComponentTypes.CUSTOM_NAME)) return true;
                else if (!stack.getName().getString().equals(itemName.get())) return true;
            }
        }
        return false;
    }

    private void noXP() {
        if (mc.player == null) return;
        if (!notified) {
            sendError("Not enough experience!");
            if (pingOnDone.get())
                mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, pingVolume.get().floatValue(), 1.0f);
        }

        notified = true;
        if (closeOnDone.get()) mc.player.closeHandledScreen();
        if (disableOnDone.get()) this.toggle();
        if (enableExpThrower.get() && !Modules.get().isActive(EXPThrower.class))
            Modules.get().get(EXPThrower.class).toggle();
    }

    private void finished() {
        if (mc.player == null) return;
        if (!notified) {
            sendError("No more items to rename§a..!");
            if (pingOnDone.get())
                mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, pingVolume.get().floatValue(), 1.0f);
        }

        notified = true;
        if (closeOnDone.get()) mc.player.closeHandledScreen();
        if (disableOnDone.get()) this.toggle();
    }

    @Override
    public void onDeactivate() {
        timer = 0;
        notified = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        if (mc.currentScreen == null) {
            notified = false;
            return;
        }

        if (!(mc.currentScreen instanceof AnvilScreen anvilScreen)) return;
        if (!(mc.player.currentScreenHandler instanceof AnvilScreenHandler anvil)) return;

        if (timer < tickRate.get()) {
            timer++;
            return;
        } else {
            timer = 0;
        }

        ItemStack input1 = anvil.getSlot(AnvilScreenHandler.INPUT_1_ID).getStack();
        ItemStack input2 = anvil.getSlot(AnvilScreenHandler.INPUT_2_ID).getStack();
        ItemStack output = anvil.getSlot(AnvilScreenHandler.OUTPUT_ID).getStack();

        if (!hasValidItems(anvil)) finished();
        else if (input1.isEmpty() && input2.isEmpty()) {
            for (int n = ANVIL_OFFSET; n < 36 + ANVIL_OFFSET; n++) {
                ItemStack stack = anvil.getSlot(n).getStack();
                if (stack.contains(DataComponentTypes.CUSTOM_NAME) && !renameNamed.get()) continue;
                else if (stack.getName().getString().equals(itemName.get())) continue;
                else if (itemName.get().isBlank() && !stack.contains(DataComponentTypes.CUSTOM_NAME)) continue;
                if ((blacklistMode.get() && !itemList.get().contains(stack.getItem()))
                    || (!blacklistMode.get() && itemList.get().contains(stack.getItem()))) {
                    InvUtils.shiftClick().slotId(n);
                    ((AccessorAnvilScreen) anvilScreen).getNameField().setText(itemName.get());
                    ItemStack check = anvil.getSlot(AnvilScreenHandler.OUTPUT_ID).getStack();
                    if (itemList.get().contains(check.getItem())) {
                        if (check.getName().getString().equals(itemName.get()) || (itemName.get().isBlank() && stack.contains(DataComponentTypes.CUSTOM_NAME))) {
                            int cost = ((AccessorAnvilScreenHandler) anvil).getLevelCost().get();
                            if (mc.player.experienceLevel >= cost) {
                                InvUtils.shiftClick().slotId(AnvilScreenHandler.OUTPUT_ID);
                            } else noXP();
                            return;
                        }
                    }
                }
            }
            finished();
        } else if (!output.isEmpty() && itemList.get().contains(output.getItem())) {
            if (output.getName().getString().equals(itemName.get()) || (itemName.get().isBlank() && input1.contains(DataComponentTypes.CUSTOM_NAME))) {
                int cost = ((AccessorAnvilScreenHandler) anvil).getLevelCost().get();
                if (mc.player.experienceLevel >= cost) {
                    InvUtils.shiftClick().slotId(AnvilScreenHandler.OUTPUT_ID);
                } else noXP();
            }
        } else if (!input2.isEmpty()) {
            InvUtils.shiftClick().slotId(AnvilScreenHandler.INPUT_2_ID);
        } else if (output.isEmpty()) {
            InvUtils.shiftClick().slotId(AnvilScreenHandler.INPUT_1_ID);
        }
    }

}
