package com.genyo.utils.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.*;

public class EntityUtil {

    /**
     * @param entity
     * @return
     */
    public static float getHealth(Entity entity)
    {
        if (entity instanceof LivingEntity e)
        {
            return e.getHealth() + e.getAbsorptionAmount();
        }
        return 0.0f;
    }

    /**
     * @param e
     * @return
     */
    public static boolean isMonster(Entity e)
    {
        return e instanceof Monster && !isNeutralInternal(e);
    }

    private static boolean isNeutralInternal(Entity e)
    {
        return e instanceof EndermanEntity enderman && !enderman.isAttacking()
            || e instanceof ZombifiedPiglinEntity piglin && !piglin.isAttacking()
            || e instanceof WolfEntity wolf && !wolf.isAttacking()
            || e instanceof IronGolemEntity ironGolem && !ironGolem.isAttacking()
            || e instanceof BeeEntity bee && !bee.isAttacking();
    }

    /**
     * @param e
     * @return
     */
    public static boolean isNeutral(Entity e)
    {
        return e instanceof EndermanEntity || e instanceof ZombifiedPiglinEntity || e instanceof WolfEntity || e instanceof IronGolemEntity;
    }

    /**
     * @param e
     * @return
     */
    public static boolean isPassive(Entity e)
    {
        return e instanceof PassiveEntity || e instanceof AmbientEntity || e instanceof SquidEntity;
    }

}
