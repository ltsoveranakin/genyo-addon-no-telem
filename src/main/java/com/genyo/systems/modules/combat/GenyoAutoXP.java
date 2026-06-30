package com.genyo.systems.modules.combat;

import com.genyo.Genyo;
import com.genyo.events.network.PlayerTickEvent;
import com.genyo.managers.Managers;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.utils.math.timer.TickTimer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class GenyoAutoXP extends GenyoModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> multitaskConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Multitask")
        .description("Allows you to throw xp while using items")
        .defaultValue(true)
        .build()
    );
    private final Setting<Integer> delayConfig = sgGeneral.add(new IntSetting.Builder()
        .name("Delay")
        .description("Delay to throw xp in ticks")
        .min(1)
        .defaultValue(1)
        .max(10)
        .build()
    );
    private final Setting<Integer> shiftTicksConfig = sgGeneral.add(new IntSetting.Builder()
        .name("Shift Ticks")
        .description("The number of XP bottles to throw in one tick")
        .min(1)
        .defaultValue(1)
        .max(64)
        .build()
    );
    private final Setting<Boolean> durabilityCheckConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Durability Check")
        .description("Check if your armor and held item durability is full then disables if it is")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> rotateConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Rotate")
        .description("Rotates the player while throwing xp")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> swingConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Swings hand while throwing xp")
        .defaultValue(false)
        .build()
    );
    private final TickTimer delayTimer = new TickTimer();

    public GenyoAutoXP() {
        super(Genyo.COMBAT, "Genyo AutoXP", "Auto mends armour");
    }

    @EventHandler
    public void onPlayerTick(PlayerTickEvent event) {
        if (mc.player == null || !delayTimer.passed(delayConfig.get())) {
            return;
        }

        if (mc.player.isUsingItem() && !multitaskConfig.get()) {
            return;
        }

        if (durabilityCheckConfig.get() && areItemsFullDura(mc.player)) {
            toggle();
            sendDisableMsg("Max durability reached.");
            return;
        }

        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof ExperienceBottleItem) {
                slot = i;
                break;
            }
        }
        if (slot == -1) {
            toggle();
            sendDisableMsg("No XP bottles in hotbar.");
            return;
        }

        Managers.INVENTORY.setSlot(slot);
        if (rotateConfig.get()) {
            setRotation(mc.player.getYaw(), 90.0f);
            if (isRotationBlocked()) {
                return;
            }
        }
        for (int i = 0; i < shiftTicksConfig.get(); i++) {
            Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
            if (swingConfig.get()) {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
        Managers.INVENTORY.syncToClient();
        delayTimer.reset();
    }

    private boolean areItemsFullDura(PlayerEntity player) {
        if (!isItemFullDura(player.getMainHandStack()) || !isItemFullDura(player.getOffHandStack())) {
            return false;
        }

        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getEquippedStack(slot);
            if (!isItemFullDura(stack)) {
                return false;
            }
        }

        return true;
    }

    private boolean isItemFullDura(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        int maxDura = stack.getMaxDamage();
        int currentDura = stack.getDamage();
        return currentDura == 0 || maxDura == 0;
    }

}
