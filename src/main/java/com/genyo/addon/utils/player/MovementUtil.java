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

    public static double[] forwardWithoutStrafe(final double d) {
        float f3 = mc.player.getYaw();
        final double d4 = d * Math.cos(Math.toRadians(f3 + 90.0f));
        final double d5 = d * Math.sin(Math.toRadians(f3 + 90.0f));
        return new double[]{d4, d5};
    }

    public static double[] forward(final double d) {
        float f = mc.player.input.movementForward;
        float f2 = mc.player.input.movementSideways;
        float f3 = mc.player.getYaw();
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += ((f > 0.0f) ? -45 : 45);
            } else if (f2 < 0.0f) {
                f3 += ((f > 0.0f) ? 45 : -45);
            }
            f2 = 0.0f;
            if (f > 0.0f) {
                f = 1.0f;
            } else if (f < 0.0f) {
                f = -1.0f;
            }
        }
        final double d2 = Math.sin(Math.toRadians(f3 + 90.0f));
        final double d3 = Math.cos(Math.toRadians(f3 + 90.0f));
        final double d4 = f * d * d3 + f2 * d * d2;
        final double d5 = f * d * d2 - f2 * d * d3;
        return new double[]{d4, d5};
    }

    /**
     * @return
     */
    public static boolean isMovingInput()
    {
        return mc.player.input.movementForward != 0.0f
            || mc.player.input.movementSideways != 0.0f;
    }

}
