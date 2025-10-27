package com.genyo.systems.modules.misc;

import com.genyo.Genyo;
import com.genyo.managers.Managers;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.utils.player.InventoryUtil;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.raid.Raid;

public class AutoOminous extends GenyoModule {

    public AutoOminous() {
        super(Genyo.MISC, "auto-ominous", "");
    }

    private boolean drinking = false;
    private boolean lookForEffect = false;

    private int selectedSlot, itemSlot = -1;
    private boolean wasHeld = false;

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null || mc.getServer() == null) return;

        ServerWorld world = mc.getServer().getWorld(mc.player.getWorld().getRegistryKey());
        if (world == null) return;

        Raid currentRaid = world.getRaidAt(mc.player.getBlockPos());
        if (currentRaid == null) return;

        if (currentRaid.hasWon()) {
            if (mc.player.getActiveStatusEffects().containsKey(StatusEffects.HERO_OF_THE_VILLAGE)) {
                if (!mc.player.getActiveStatusEffects().containsKey(StatusEffects.BAD_OMEN)) {
                    if (!drinking) {
                        drinkPotion();
                        lookForEffect = true;
                    }
                } else if (lookForEffect) {
                    stopDrinking();
                }
            }
        }
    }

    private void drinkPotion() {
        if (!InventoryUtil.hasItemInInventory(Items.OMINOUS_BOTTLE, true)) {
            sendError("No Ominous Bottle found in hotbar!");
            return;
        }

        FindItemResult result = InvUtils.find(Items.OMINOUS_BOTTLE);
        if (!result.found()) {
            sendError("Couldn't find Ominous Bottle.");
            return;
        }

        selectedSlot = mc.player.getInventory().selectedSlot;
        itemSlot = result.slot();
        wasHeld = result.isMainHand();

        if (!wasHeld) {
            InvUtils.swap(itemSlot, false);
            Managers.INVENTORY.syncToClient();
        }

        startDrinking();
    }

    private void startDrinking() {
        mc.options.useKey.setPressed(true);
        if (!mc.player.isUsingItem()) Utils.rightClick();

        drinking = true;
    }

    private void stopDrinking() {
        mc.options.useKey.setPressed(false);
        drinking = false;
        lookForEffect = false;

        InvUtils.swap(selectedSlot, false);
        Managers.INVENTORY.syncToClient();
    }

}
