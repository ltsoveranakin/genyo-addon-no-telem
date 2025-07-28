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

    /**
     * @return
     */
    public static boolean isInputtingMovement()
    {
        return mc.options.forwardKey.isPressed()
            || mc.options.backKey.isPressed()
            || mc.options.leftKey.isPressed()
            || mc.options.rightKey.isPressed();
    }

    /**
     * @return
     */
    public static boolean isMoving()
    {
        double d = mc.player.getX() - mc.player.lastRenderX;
        double e = mc.player.getY() - mc.player.lastRenderY;
        double f = mc.player.getZ() - mc.player.lastRenderZ;
        return MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0e-4);
    }

}
