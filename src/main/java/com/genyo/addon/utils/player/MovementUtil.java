package com.genyo.addon.utils.player;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.math.MathHelper;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MovementUtil {

    public static void applySneak()
    {
        final float modifier = MathHelper.clamp(0.3f + (EnchantmentUtil.getLevel(mc.player.getEquippedStack(EquipmentSlot.FEET), Enchantments.SWIFT_SNEAK) * 0.15F), 0.0f, 1.0f);
        mc.player.input.movementForward *= modifier;
        mc.player.input.movementSideways *= modifier;
    }

}
