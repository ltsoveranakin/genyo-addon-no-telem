package com.genyo.systems.modules.combat;

import com.genyo.Genyo;
import com.genyo.events.network.DisconnectEvent;
import com.genyo.events.network.PlayerTickEvent;
import com.genyo.events.world.RemoveEntityEvent;
import com.genyo.managers.Managers;
import com.genyo.managers.world.tick.TickSync;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.systems.modules.world.GenyoAutoMine;
import com.genyo.systems.settings.FloatSetting;
import com.genyo.utils.entity.EntityUtil;
import com.genyo.utils.math.timer.CacheTimer;
import com.genyo.utils.math.timer.Timer;
import com.genyo.utils.player.EnchantmentUtil;
import com.genyo.utils.player.PlayerUtil;
import com.genyo.utils.player.RotationUtil;
import com.genyo.utils.render.SInterpolation;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.apache.commons.lang3.mutable.MutableDouble;

import java.util.Comparator;
import java.util.stream.Stream;

public class KFCSpawnKill extends GenyoModule {

    public KFCSpawnKill() {
        super(Genyo.COMBAT, "KFC Spawn Kill", "ask about the name, i won't tell you.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRotate = settings.createGroup("Rotate");
    private final SettingGroup sgTarget = settings.createGroup("Target");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> multitaskConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Multitask")
        .description("tesco")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> swingConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Swings the hand after attacking")
        .defaultValue(true)
        .build()
    );

    private final Setting<TargetMode> modeConfig = sgGeneral.add(new EnumSetting.Builder<TargetMode>()
        .name("Mode")
        .description("The mode for targeting entities to attack")
        .defaultValue(TargetMode.SWITCH)
        .build()
    );

    private final Setting<Priority> priorityConfig = sgGeneral.add(new EnumSetting.Builder<Priority>()
        .name("Priority")
        .description("The value to prioritize when searching for targets")
        .defaultValue(Priority.HEALTH)
        .build()
    );

    private final Setting<Float> searchRangeConfig = sgGeneral.add(new FloatSetting.Builder()
        .name("Enemy Range")
        .description("Range to search for targets")
        .min(1.0f)
        .defaultValue(5.0f)
        .max(10.0f)
        .build()
    );

    private final Setting<Float> rangeConfig = sgGeneral.add(new FloatSetting.Builder()
        .name("Range")
        .description("Range to attack entities")
        .min(1.0f)
        .defaultValue(4.5f)
        .max(6.0f)
        .build()
    );

    private final Setting<Float> wallRangeConfig = sgGeneral.add(new FloatSetting.Builder()
        .name("Wall Range")
        .description("Range to attack entities through walls")
        .min(1.0f)
        .defaultValue(4.5f)
        .max(6.0f)
        .build()
    );

    private final Setting<Boolean> vanillaRangeConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Vanilla Range")
        .description("Only attack within vanilla range")
        .defaultValue(false)
        .build()
    );

    private final Setting<Float> fovConfig = sgGeneral.add(new FloatSetting.Builder()
        .name("FOV")
        .description("Field of view to attack entities")
        .min(1.0f)
        .defaultValue(180.0f)
        .max(180.0f)
        .build()
    );

    private final Setting<Boolean> attackDelayConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Attack Delay")
        .description("Delays attacks according to minecraft hit delays for maximum damage per attack")
        .defaultValue(true)
        .build()
    );

    private final Setting<Float> attackSpeedConfig = sgGeneral.add(new FloatSetting.Builder()
        .name("Attack Speed")
        .description("Delay for attacks (Only functions if AttackDelay is off)")
        .min(1.0f)
        .defaultValue(20.0f)
        .max(20.0f)
        .visible(() -> !attackDelayConfig.get())
        .build()
    );

    private final Setting<Float> randomSpeedConfig = sgGeneral.add(new FloatSetting.Builder()
        .name("Random Speed")
        .description("Randomized delay for attacks (Only functions if AttackDelay is off)")
        .min(0.0f)
        .defaultValue(0.0f)
        .max(10.0f)
        .visible(() -> !attackDelayConfig.get())
        .build()
    );

    private final Setting<Float> swapDelayConfig = sgGeneral.add(new FloatSetting.Builder()
        .name("Swap Penalty")
        .description("Delay for attacking after swapping items which prevents NCP flags")
        .min(0.0f)
        .defaultValue(0.0f)
        .max(10.0f)
        .build()
    );

    private final Setting<TickSync> tpsSyncConfig = sgGeneral.add(new EnumSetting.Builder<TickSync>()
        .name("TPS Sync")
        .description("Syncs the attacks with the server TPS")
        .defaultValue(TickSync.NONE)
        .build()
    );

    private final Setting<Swap> autoSwapConfig = sgGeneral.add(new EnumSetting.Builder<Swap>()
        .name("Auto Swap")
        .description("Automatically swaps to a weapon before attacking")
        .defaultValue(Swap.OFF)
        .build()
    );

    private final Setting<Boolean> swordCheckConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Sword-Check")
        .description("Checks if a weapon is in the hand before attacking")
        .defaultValue(true)
        .build()
    );

    // Rotate

    private final Setting<Vector> hitVectorConfig = sgRotate.add(new EnumSetting.Builder<Vector>()
        .name("Hit Vector")
        .description("The vector to aim for when attacking entities")
        .defaultValue(Vector.FEET)
        .build()
    );

    private final Setting<Boolean> rotateConfig = sgRotate.add(new BoolSetting.Builder()
        .name("Rotate")
        .description("Rotate before attacking")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> silentRotateConfig = sgRotate.add(new BoolSetting.Builder()
        .name("Rotate Silent")
        .description("Rotates silently to server")
        .defaultValue(false)
        .visible(rotateConfig::get)
        .build()
    );

    private final Setting<Boolean> strictRotateConfig = sgRotate.add(new BoolSetting.Builder()
        .name("Yaw Step")
        .description("Rotates yaw over multiple ticks to prevent certain rotation flags in NCP")
        .defaultValue(false)
        .visible(rotateConfig::get)
        .build()
    );

    private final Setting<Integer> rotateLimitConfig = sgRotate.add(new IntSetting.Builder()
        .name("YawStep-Limit")
        .description("Maximum yaw rotation in degrees for one tick")
        .min(1)
        .defaultValue(180)
        .max(180)
        .build()
    );

    private final Setting<Integer> ticksExistedConfig = sgRotate.add(new IntSetting.Builder()
        .name("Ticks Existed")
        .description("The minimum age of the entity to be considered for attack")
        .min(0)
        .defaultValue(0)
        .max(200)
        .build()
    );

    private final Setting<Boolean> armorCheckConfig = sgRotate.add(new BoolSetting.Builder()
        .name("Armor Check")
        .description("Checks if target has armor before attacking")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> stopSprintConfig = sgRotate.add(new BoolSetting.Builder()
        .name("Stop Sprint")
        .description("Stops sprinting before attacking to maintain vanilla behavior")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> stopShieldConfig = sgRotate.add(new BoolSetting.Builder()
        .name("Stop Shield")
        .description("Automatically handles shielding before attacking")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> maceBreachConfig = sgRotate.add(new BoolSetting.Builder()
        .name("Mace Breach")
        .description("Abuses vanilla exploit to apply breach enchantment to swords")
        .defaultValue(false)
        .visible(() -> autoSwapConfig.get() != Swap.SILENT)
        .build()
    );

    // Target

    private final Setting<Boolean> playersConfig = sgTarget.add(new BoolSetting.Builder()
        .name("Players")
        .description("Target players")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> monstersConfig = sgTarget.add(new BoolSetting.Builder()
        .name("Monsters")
        .description("Target monsters")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> neutralsConfig = sgTarget.add(new BoolSetting.Builder()
        .name("Neutrals")
        .description("Target neutrals")
        .defaultValue(false)
        .build()
    );


    private final Setting<Boolean> animalsConfig = sgTarget.add(new BoolSetting.Builder()
        .name("Animals")
        .description("Target animals")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> invisiblesConfig = sgTarget.add(new BoolSetting.Builder()
        .name("Invisibles")
        .description("Target invisible entities")
        .defaultValue(true)
        .build()
    );

    // Render

    private final Setting<Boolean> renderConfig = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("Renders an indicator over the target")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> disableDeathConfig = sgRender.add(new BoolSetting.Builder()
        .name("Disable on Death")
        .description("Disables during disconnect/death")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Render Color")
        .description("asdsadsadsadsadsa")
        .defaultValue(new Color(236, 243, 122, 255))
        .build()
    );

    private Entity entityTarget;
    private long randomDelay = -1;

    private boolean shielding;
    private boolean sneaking;
    private boolean sprinting;

    private long lastAttackTime;
    private final Timer critTimer = new CacheTimer();
    private final Timer autoSwapTimer = new CacheTimer();
    private final Timer switchTimer = new CacheTimer();
    private boolean rotated;

    private float[] silentRotations;

    @Override
    public void onDeactivate() {
        entityTarget = null;
        silentRotations = null;
    }

    @EventHandler
    public void onDisconnect(DisconnectEvent event)
    {
        if (disableDeathConfig.get()) {
            sendDisableMsg("Disabled because Auto Disable.");
            toggle();
        }
    }

    @EventHandler
    public void onRemoveEntity(RemoveEntityEvent event)
    {
        if (disableDeathConfig.get() && event.entity == mc.player) {
            sendDisableMsg("Disabled because Auto Disable.");
            toggle();
        }
    }

    @EventHandler
    public void onPlayerUpdate(PlayerTickEvent event)
    {
        if (Modules.get().get(GenyoAutoCrystal.class).isAttacking()
            || Modules.get().get(GenyoAutoCrystal.class).isPlacing()
            || autoSwapConfig.get() == Swap.SILENT && Modules.get().get(GenyoAutoMine.class).isSilentSwapping()
            || mc.player.isSpectator())
        {
            return;
        }

        if (!multitaskConfig.get() && checkMultitask(true)) return;

        final Vec3d eyepos = Managers.POSITION.getEyePos();
        entityTarget = switch (modeConfig.get())
        {
            case SWITCH -> getAttackTarget(eyepos);
            case SINGLE ->
            {
                if (entityTarget == null || !entityTarget.isAlive()
                    || !isInAttackRange(eyepos, entityTarget))
                {
                    yield getAttackTarget(eyepos);
                }
                yield entityTarget;
            }
        };
        if (entityTarget == null || !switchTimer.passed(swapDelayConfig.get() * 25.0f))
        {
            silentRotations = null;
            return;
        }
        if (mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND
            || mc.options.attackKey.isPressed() || PlayerUtil.isHotbarKeysPressed())
        {
            autoSwapTimer.reset();
        }

        int slot = getSwordSlot();
        // END PRE
        boolean silentSwapped = false;
        if (!(mc.player.getMainHandStack().isIn(ItemTags.SWORDS)) && slot != -1)
        {
            switch (autoSwapConfig.get())
            {
                case NORMAL ->
                {
                    if (autoSwapTimer.passed(500))
                    {
                        Managers.INVENTORY.setClientSlot(slot);
                    }
                }
                case SILENT ->
                {
                    Managers.INVENTORY.setSlot(slot);
                    silentSwapped = true;
                }
            }
        }
        if (!isHoldingSword() && autoSwapConfig.get() != Swap.SILENT)
        {
            return;
        }
        if (rotateConfig.get())
        {
            float[] rotation = RotationUtil.getRotationsTo(mc.player.getEyePos(),
                getAttackRotateVec(entityTarget));
            if (!silentRotateConfig.get() && strictRotateConfig.get())
            {
                float serverYaw = Managers.ROTATION.getWrappedYaw();
                float diff = serverYaw - rotation[0];
                float diff1 = Math.abs(diff);
                if (diff1 > 180.0f)
                {
                    diff += diff > 0.0f ? -360.0f : 360.0f;
                }
                int dir = diff > 0.0f ? -1 : 1;
                float deltaYaw = dir * rotateLimitConfig.get();
                float yaw;
                if (diff1 > rotateLimitConfig.get())
                {
                    yaw = serverYaw + deltaYaw;
                    rotated = false;
                }
                else
                {
                    yaw = rotation[0];
                    rotated = true;
                }
                rotation[0] = yaw;
            }
            else
            {
                rotated = true;
            }
            // what what you cannot hop in my car
            // bentley coupe ridin with stars
            if (silentRotateConfig.get())
            {
                silentRotations = rotation;
            }
            else
            {
                setRotation(rotation[0], rotation[1]);
            }
        }
        if (isRotationBlocked() || !rotated && rotateConfig.get() || !isInAttackRange(eyepos, entityTarget))
        {
            Managers.INVENTORY.syncToClient();
            return;
        }
        if (attackDelayConfig.get())
        {
            PlayerInventory inventory = mc.player.getInventory();
            ItemStack itemStack = inventory.getStack((slot == -1 || !swordCheckConfig.get()) ? mc.player.getInventory().selectedSlot : slot);

            MutableDouble attackSpeed = new MutableDouble(
                mc.player.getAttributeBaseValue(EntityAttributes.ATTACK_SPEED));

            AttributeModifiersComponent attributeModifiers =
                itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            if (attributeModifiers != null)
            {
                attributeModifiers.applyModifiers(EquipmentSlot.MAINHAND, (entry, modifier) ->
                {
                    if (entry == EntityAttributes.ATTACK_SPEED)
                    {
                        attackSpeed.add(modifier.value());
                    }
                });
            }

            double attackCooldownTicks = 1.0 / attackSpeed.getValue() * 20.0;

            int breachSlot = getBreachMaceSlot();
            if (autoSwapConfig.get() != Swap.SILENT && maceBreachConfig.get() && breachSlot != -1)
            {
                Managers.INVENTORY.setSlot(breachSlot);
            }

            float ticks = 20.0f - Managers.TICK.getTickSync(tpsSyncConfig.get());
            float currentTime = (System.currentTimeMillis() - lastAttackTime) + (ticks * 50.0f);
            if ((currentTime / 50.0f) >= attackCooldownTicks && attackTarget(entityTarget))
            {
                lastAttackTime = System.currentTimeMillis();
            }

            if (autoSwapConfig.get() != Swap.SILENT && maceBreachConfig.get() && breachSlot != -1)
            {
                Managers.INVENTORY.syncToClient();
            }
        }
        else
        {
            if (randomDelay < 0)
            {
                randomDelay = (long) RANDOM.nextFloat((randomSpeedConfig.get() * 10.0f) + 1.0f);
            }
            float delay = (attackSpeedConfig.get() * 50.0f) + randomDelay;

            int breachSlot = getBreachMaceSlot();
            if (autoSwapConfig.get() != Swap.SILENT && maceBreachConfig.get() && breachSlot != -1)
            {
                Managers.INVENTORY.setSlot(breachSlot);
            }

            long currentTime = System.currentTimeMillis() - lastAttackTime;
            if (currentTime >= 1000.0f - delay && attackTarget(entityTarget))
            {
                randomDelay = -1;
                lastAttackTime = System.currentTimeMillis();
            }

            if (autoSwapConfig.get() != Swap.SILENT && maceBreachConfig.get() && breachSlot != -1)
            {
                Managers.INVENTORY.syncToClient();
            }
        }

        if (autoSwapConfig.get() == Swap.SILENT && silentSwapped)
        {
            Managers.INVENTORY.syncToClient();
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null) return;

        if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            switchTimer.reset();
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (Modules.get().get(GenyoAutoCrystal.class).isAttacking()
            || Modules.get().get(GenyoAutoCrystal.class).isPlacing() || mc.player.isSpectator())
        {
            return;
        }

        if (entityTarget != null && renderConfig.get() && (isHoldingSword() || autoSwapConfig.get() == Swap.SILENT))
        {
            long currentTime = System.currentTimeMillis() - lastAttackTime;
            float animFactor = 1.0f - MathHelper.clamp(currentTime / 1000f, 0.0f, 1.0f);
            int attackDelay = (int) (70.0 * animFactor);

            event.renderer.box(SInterpolation.getInterpolatedEntityBox(entityTarget), color.get().a(30 + attackDelay),
                color.get().a(100), ShapeMode.Both, 0);
        }
    }

    private boolean attackTarget(Entity entity)
    {
/*
        Entity castEntity;
        // validate our server-sided rotations
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {
            return false;
        }
        // Get the entity raycasted & then check. If invalid, fail
        castEntity = ((EntityHitResult) mc.crosshairTarget).getEntity();
        if (castEntity == null || !castEntity.isAttackable()) {
            return false;
        }
        preAttackTarget();
        mc.doAttack();
        postAttackTarget(castEntity);
*/
        preAttackTarget();

        if (silentRotateConfig.get() && silentRotations != null)
        {
            setRotationSilent(silentRotations[0], silentRotations[1]);
        }

        PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking());
        Managers.NETWORK.sendPacket(packet);
        if (swingConfig.get())
        {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        else
        {
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        postAttackTarget(entity);

        if (silentRotateConfig.get())
        {
            Managers.ROTATION.setRotationSilentSync();
        }
        return true;
    }

    private int getSwordSlot()
    {
        float sharp = 0.0f;
        int slot = -1;
        // Maximize item attack damage
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof SwordItem swordItem)
            {
                float sharpness = EnchantmentUtil.getLevel(stack,
                    Enchantments.SHARPNESS) * 0.5f + 0.5f;
                float dmg = swordItem.getDefaultStack().getDamage() + sharpness;
                if (dmg > sharp)
                {
                    sharp = dmg;
                    slot = i;
                }
            }
            else if (stack.getItem() instanceof AxeItem axeItem)
            {
                float sharpness = EnchantmentUtil.getLevel(stack,
                    Enchantments.SHARPNESS) * 0.5f + 0.5f;
                float dmg = axeItem.getDefaultStack().getDamage() + sharpness;
                if (dmg > sharp)
                {
                    sharp = dmg;
                    slot = i;
                }
            }
            else if (stack.getItem() instanceof TridentItem)
            {
                float sharpness = EnchantmentUtil.getLevel(stack,
                    Enchantments.SHARPNESS) * 0.5f + 0.5f;
                float dmg = TridentItem.ATTACK_DAMAGE + sharpness;
                if (dmg > sharp)
                {
                    sharp = dmg;
                    slot = i;
                }
            }
            else if (stack.getItem() instanceof MaceItem)
            {
                float sharpness = EnchantmentUtil.getLevel(stack,
                    Enchantments.SHARPNESS) * 0.5f + 0.5f;
                float dmg = 5.0f + sharpness;
                if (dmg > sharp)
                {
                    sharp = dmg;
                    slot = i;
                }
            }
        }
        return slot;
    }

    private int getBreachMaceSlot()
    {
        int slot = -1;
        int maxBreach = 0;
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!(stack.getItem() instanceof MaceItem))
            {
                continue;
            }
            int breach = EnchantmentUtil.getLevel(stack, Enchantments.BREACH);
            if (breach > maxBreach)
            {
                slot = i;
                maxBreach = breach;
            }
        }
        return slot;
    }

    private void preAttackTarget()
    {
        final ItemStack offhand = mc.player.getOffHandStack();
        // Shield state
        shielding = false;
        if (stopShieldConfig.get())
        {
            shielding = offhand.getItem() == Items.SHIELD && mc.player.isBlocking();
            if (shielding)
            {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                    Managers.POSITION.getBlockPos(), Direction.getFacing(mc.player.getX(),
                    mc.player.getY(), mc.player.getZ())));
            }
        }
        sneaking = false;
        sprinting = false;
        if (stopSprintConfig.get())
        {
            sneaking = Managers.POSITION.isSneaking();
            if (sneaking)
            {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                    ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
            sprinting = Managers.POSITION.isSprinting();
            if (sprinting)
            {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                    ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
        }
    }

    // RELEASE
    private void postAttackTarget(Entity entity)
    {
        if (shielding)
        {
            Managers.NETWORK.sendSequencedPacket(s ->
                new PlayerInteractItemC2SPacket(Hand.OFF_HAND, s, mc.player.getYaw(), mc.player.getPitch()));
        }
        if (sneaking)
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
        if (sprinting)
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
//        if (CriticalsModule.getInstance().isEnabled() && critTimer.passed(500)) {
//            if (!mc.player.isOnGround()
//                    || mc.player.isRiding()
//                    || mc.player.isSubmergedInWater()
//                    || mc.player.isInLava()
//                    || mc.player.isHoldingOntoLadder()
//                    || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
//                    || mc.player.input.jumping) {
//                return;
//            }
//            CriticalsModule.getInstance().preAttackPacket(entity);
//            critTimer.reset();
//        }
    }

    private Entity getAttackTarget(Vec3d pos)
    {
        double min = Double.MAX_VALUE;
        Entity attackTarget = null;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == null || entity == mc.player
                || !entity.isAlive() || !isEnemy(entity)
                || (entity instanceof PlayerEntity && Friends.get().isFriend((PlayerEntity) entity))
                || entity instanceof EndCrystalEntity
                || entity instanceof ItemEntity
                || entity instanceof ArrowEntity
                || entity instanceof ExperienceBottleEntity)
            {
                continue;
            }
            if (armorCheckConfig.get()
                && entity instanceof LivingEntity livingEntity
                && !livingEntity.getArmorItems().iterator().hasNext())
            {
                continue;
            }
            double dist = pos.distanceTo(entity.getPos());
            if (dist <= searchRangeConfig.get())
            {
                if (entity.age < ticksExistedConfig.get())
                {
                    continue;
                }
                switch (priorityConfig.get())
                {
                    case DISTANCE ->
                    {
                        if (dist < min)
                        {
                            min = dist;
                            attackTarget = entity;
                        }
                    }
                    case HEALTH ->
                    {
                        if (entity instanceof LivingEntity e)
                        {
                            float health = e.getHealth() + e.getAbsorptionAmount();
                            if (health < min)
                            {
                                min = health;
                                attackTarget = entity;
                            }
                        }
                    }
                    case ARMOR ->
                    {
                        if (entity instanceof LivingEntity e)
                        {
                            float armor = getArmorDurability(e);
                            if (armor < min)
                            {
                                min = armor;
                                attackTarget = entity;
                            }
                        }
                    }
                }
            }
        }
        return attackTarget;
    }

    private float getArmorDurability(LivingEntity e)
    {
        float edmg = 0.0f;
        float emax = 0.0f;
        for (ItemStack armor : e.getArmorItems())
        {
            if (armor != null && !armor.isEmpty())
            {
                edmg += armor.getDamage();
                emax += armor.getMaxDamage();
            }
        }
        return 100.0f - edmg / emax;
    }

    public boolean isInAttackRange(Vec3d pos, Entity entity)
    {
        final Vec3d entityPos = getAttackRotateVec(entity);
        double dist = pos.distanceTo(entityPos);
        return isInAttackRange(dist, pos, entityPos);
    }

    /**
     * @param dist
     * @param pos
     * @return
     */
    public boolean isInAttackRange(double dist, Vec3d pos, Vec3d entityPos)
    {
        if (vanillaRangeConfig.get() && dist > 3.0f)
        {
            return false;
        }
        if (dist > rangeConfig.get())
        {
            return false;
        }
        BlockHitResult result = mc.world.raycast(new RaycastContext(
            pos, entityPos,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE, mc.player));
        if (result != null && !result.getBlockPos().equals(BlockPos.ofFloored(entityPos)) && dist > wallRangeConfig.get())
        {
            return false;
        }
        if (fovConfig.get() != 180.0f)
        {
            float[] rots = RotationUtil.getRotationsTo(pos, entityPos);
            float diff = MathHelper.wrapDegrees(mc.player.getYaw()) - rots[0];
            float magnitude = Math.abs(diff);
            return magnitude <= fovConfig.get();
        }
        return true;
    }

    public boolean isHoldingSword()
    {
        return !swordCheckConfig.get() || mc.player.getMainHandStack().getItem() instanceof SwordItem
            || mc.player.getMainHandStack().getItem() instanceof AxeItem
            || mc.player.getMainHandStack().getItem() instanceof TridentItem
            || mc.player.getMainHandStack().getItem() instanceof MaceItem;
    }

    private Vec3d getAttackRotateVec(Entity entity)
    {
        Vec3d feetPos = entity.getPos();
        return switch (hitVectorConfig.get())
        {
            case FEET -> feetPos;
            case TORSO -> feetPos.add(0.0, entity.getHeight() / 2.0f, 0.0);
            case EYES -> entity.getEyePos();
            case AUTO ->
            {
                Vec3d torsoPos = feetPos.add(0.0, entity.getHeight() / 2.0f, 0.0);
                Vec3d eyesPos = entity.getEyePos();
                yield Stream.of(feetPos, torsoPos, eyesPos).min(Comparator.comparing(b -> mc.player.getEyePos().squaredDistanceTo(b))).orElse(eyesPos);
            }
        };
    }

    /**
     * Returns <tt>true</tt> if the {@link Entity} is a valid enemy to attack.
     *
     * @param e The potential enemy entity
     * @return <tt>true</tt> if the entity is an enemy
     * @see EntityUtil
     */
    private boolean isEnemy(Entity e)
    {
        return (!e.isInvisible() || invisiblesConfig.get())
            && e instanceof PlayerEntity && playersConfig.get()
            || EntityUtil.isMonster(e) && monstersConfig.get()
            || EntityUtil.isNeutral(e) && neutralsConfig.get()
            || EntityUtil.isPassive(e) && animalsConfig.get();
    }

    public Entity getEntityTarget()
    {
        return entityTarget;
    }

    public enum TargetMode
    {
        SWITCH,
        SINGLE
    }

    public enum Swap
    {
        NORMAL,
        SILENT,
        OFF
    }

    public enum Vector
    {
        EYES,
        TORSO,
        FEET,
        AUTO
    }

    public enum Priority
    {
        HEALTH,
        DISTANCE,
        ARMOR
    }

}
