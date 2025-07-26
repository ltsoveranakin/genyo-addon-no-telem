package com.genyo.addon.modules.combat;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.events.network.PlayerTickEvent;
import com.genyo.addon.events.RunTickEvent;
import com.genyo.addon.managers.Managers;
import com.genyo.addon.modules.GenyoModule;
import com.genyo.addon.render.animation.Animation;
import com.genyo.addon.settings.FloatSetting;
import com.genyo.addon.utils.collection.EvictingQueue;
import com.genyo.addon.utils.entity.EntityUtil;
import com.genyo.addon.utils.math.PerSecondCounter;
import com.genyo.addon.utils.math.timer.CacheTimer;
import com.genyo.addon.utils.math.timer.Timer;
import com.genyo.addon.utils.player.InventoryUtil;
import com.genyo.addon.utils.player.PlayerUtil;
import com.genyo.addon.utils.world.BlastResistantBlocks;
import com.genyo.addon.utils.world.ExplosionUtil;
import com.google.common.collect.Lists;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.util.*;
import java.util.concurrent.*;

public class GenyoAutoCrystal extends GenyoModule {

    public GenyoAutoCrystal() {
        super(GenyoAddon.GENYO, "genyo-auto-crystal", "neger cock neger cock neger cock");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRotate = settings.createGroup("Rotate");
    private final SettingGroup sgTargets = settings.createGroup("Targets");
    private final SettingGroup sgBreak = settings.createGroup("Break");
    private final SettingGroup sgPlace = settings.createGroup("Place");
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> multitask = sgGeneral.add(new BoolSetting.Builder()
        .name("Allow Multitask")
        .description("Allows actions while using items")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> whileMining = sgGeneral.add(new BoolSetting.Builder()
        .name("While Mining")
        .description("Allows attacking while mining blocks")
        .defaultValue(false)
        .build()
    );

    private final Setting<Float> targetRange = sgGeneral.add(new FloatSetting.Builder()
        .name("Enemy Range")
        .description("Range to search for potential enemies")
        .min(1.0f)
        .defaultValue(10.f)
        .max(13.0f)
        .build()
    );

    private final Setting<Boolean> instant = sgGeneral.add(new BoolSetting.Builder()
        .name("Instant")
        .description("Instantly attacks crystals when they spawn")
        .defaultValue(false)
        .build()
    );

    private final Setting<Sequential> sequential = sgGeneral.add(new EnumSetting.Builder<Sequential>()
        .name("Sequential")
        .description("Places a crystal after spawn")
        .defaultValue(Sequential.NONE)
        .build()
    );

    private final Setting<Boolean> idPredict = sgGeneral.add(new BoolSetting.Builder()
        .name("Break Predict")
        .description("Attempts to predict crystal entity ids")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> instantCalc = sgGeneral.add(new BoolSetting.Builder()
        .name("Instant Calc")
        .description("Calculates a crystal when it spawns and attacks if it meets MINIMUM requirements, this will result in non-ideal crystal attacks")
        .defaultValue(false)
        //.visible(() -> false)
        .build()
    );

    private final Setting<Float> instantDamage = sgGeneral.add(new FloatSetting.Builder()
        .name("Instant Damage")
        .description("Minimum damage to attack crystals instantly")
        .min(1.0f)
        .defaultValue(6.0f)
        .max(10.0f)
        //.visible(() -> false)
        .build()
    );

    private final Setting<Boolean> instantMax = sgGeneral.add(new BoolSetting.Builder()
        .name("Instant Max")
        .description("Attacks crystals instantly if they exceed the previous max attack damage (Note: This is still not a perfect check because the next tick could have better damages)")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> raytraceC = sgGeneral.add(new BoolSetting.Builder()
        .name("Raytrace")
        .description("Raytrace to crystal position")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Swing hand when placing and attacking crystals")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotate = sgRotate.add(new BoolSetting.Builder()
        .name("Rotate")
        .description("Rotate before placing and breaking")
        .defaultValue(false)
        .build()
    );

    private final Setting<Rotate> strictRotate = sgRotate.add(new EnumSetting.Builder<Rotate>()
        .name("Yaw Step")
        .description("Rotates yaw over multiple ticks to prevent certain rotation flags in NCP")
        .defaultValue(Rotate.OFF)
        .build()
    );

    private final Setting<Integer> rotateLimit = sgRotate.add(new IntSetting.Builder()
        .name("Yaw Step Limit")
        .description("Maximum yaw rotation in degrees for one tick")
        .min(1)
        .defaultValue(180)
        .max(180)
        .visible(() -> rotate.get() && strictRotate.get() != Rotate.OFF)
        .build()
    );

    private final Setting<Boolean> players = sgTargets.add(new BoolSetting.Builder()
        .name("Players")
        .description("Target players")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> monsters = sgTargets.add(new BoolSetting.Builder()
        .name("Monsters")
        .description("Target monsters")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> neutrals = sgTargets.add(new BoolSetting.Builder()
        .name("Neutrals")
        .description("Target neutrals")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> animals = sgTargets.add(new BoolSetting.Builder()
        .name("Animals")
        .description("Target animals")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> shulkers = sgTargets.add(new BoolSetting.Builder()
        .name("Shulkers")
        .description("Target shulker boxes")
        .defaultValue(false)
        .build()
    );

    //Break
    private final Setting<Float> breakSpeed = sgBreak.add(new FloatSetting.Builder()
        .name("Break Speed")
        .description("Speed to break crystals")
        .min(0.1f)
        .defaultValue(18.0f)
        .max(20.0f)
        .build()
    );

    private final Setting<Float> attackDelay = sgBreak.add(new FloatSetting.Builder()
        .name("Attack Delay")
        .description("Added delays")
        .min(0.0f)
        .defaultValue(0.0f)
        .max(5.0f)
        .build()
    );

    private final Setting<Integer> attackFactorC = sgBreak.add(new IntSetting.Builder()
        .name("Attack Factor")
        .description("Factor of attack delay")
        .min(0)
        .defaultValue(0)
        .max(3)
        .visible(() -> attackDelay.get() > 0.0)
        .build()
    );

    private final Setting<Float> attackLimit = sgBreak.add(new FloatSetting.Builder()
        .name("Attack Limit")
        .description("The number of attacks before considering a crystal unbreakable")
        .min(0.5f)
        .defaultValue(1.5f)
        .max(20.0f)
        .build()
    );

    private final Setting<Boolean> breakDelayC = sgBreak.add(new BoolSetting.Builder()
        .name("Break Delay")
        .description("Uses attack latency to calculate break delays")
        .defaultValue(false)
        .build()
    );

    private final Setting<Float> breakTimeout = sgBreak.add(new FloatSetting.Builder()
        .name("Break Timeout")
        .description("Time after waiting for the average break time before considering a crystal attack failed")
        .min(0.0f)
        .defaultValue(3.0f)
        .max(10.0f)
        .visible(breakDelayC::get)
        .build()
    );

    private final Setting<Float> minTimeout = sgBreak.add(new FloatSetting.Builder()
        .name("Min Timeout")
        .description("Minimum time before considering a crystal break/place failed")
        .min(0.0f)
        .defaultValue(5.0f)
        .max(20.0f)
        .visible(breakDelayC::get)
        .build()
    );

    private final Setting<Integer> ticksExisted = sgBreak.add(new IntSetting.Builder()
        .name("Ticks Existed")
        .description("Minimum ticks alive to consider crystals for attack")
        .min(0)
        .defaultValue(0)
        .max(10)
        .build()
    );

    private final Setting<Float> breakRangeC = sgBreak.add(new FloatSetting.Builder()
        .name("Break Range")
        .description("Range to break crystals")
        .min(0.1f)
        .defaultValue(4.0f)
        .max(6.0f)
        .build()
    );

    private final Setting<Float> maxYOffset = sgBreak.add(new FloatSetting.Builder()
        .name("Max Y Offset")
        .description("Maximum crystal y-offset difference")
        .min(1.0f)
        .defaultValue(5.0f)
        .max(10.f)
        .build()
    );

    private final Setting<Float> breakWallRangeC = sgBreak.add(new FloatSetting.Builder()
        .name("Break Wall Range")
        .description("Range to break crystals through walls")
        .min(0.1f)
        .defaultValue(4.0f)
        .max(6.0f)
        .build()
    );

    private final Setting<Swap> antiWeakness = sgBreak.add(new EnumSetting.Builder<Swap>()
        .name("Anti Weakness")
        .description("Swap to tools before attacking crystals")
        .defaultValue(Swap.OFF)
        .build()
    );

    private final Setting<Float> swapDelay = sgBreak.add(new FloatSetting.Builder()
        .name("Swap Penalty")
        .description("Delay for attacking after swapping items which prevents NCP flags")
        .min(0.0f)
        .defaultValue(0.0f)
        .max(10.0f)
        .build()
    );

    private final Setting<Boolean> inhibit = sgPlace.add(new BoolSetting.Builder()
        .name("Inhibit")
        .description("Prevents excessive attacks")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> place = sgPlace.add(new BoolSetting.Builder()
        .name("Place")
        .description("Places crystals to damage enemies. Place settings will only function if this setting is enabled.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Float> placeSpeed = sgPlace.add(new FloatSetting.Builder()
        .name("Place Speed")
        .description("Speed to place crystals")
        .min(0.1f)
        .defaultValue(18.0f)
        .max(20.0f)
        .visible(place::get)
        .build()
    );

    private final Setting<Float> placeRangeC = sgPlace.add(new FloatSetting.Builder()
        .name("Place Range")
        .description("Range to place crystals")
        .min(0.1f)
        .defaultValue(4.0f)
        .max(6.0f)
        .visible(place::get)
        .build()
    );

    private final Setting<Float> placeWallRangeC = sgPlace.add(new FloatSetting.Builder()
        .name("Place Wall Range")
        .description("Range to place crystals through walls")
        .min(0.1f)
        .defaultValue(4.0f)
        .max(6.0f)
        .visible(place::get)
        .build()
    );

    private final Setting<Boolean> placeRangeEye = sgPlace.add(new BoolSetting.Builder()
        .name("Place Range Eye")
        .description("Calculates place ranges starting from the eye position of the player")
        .defaultValue(false)
        .visible(place::get)
        .build()
    );

    private final Setting<Boolean> placeRangeCenter = sgPlace.add(new BoolSetting.Builder()
        .name("Place Range Center")
        .description("Calculates place ranges to the center of the block")
        .defaultValue(true)
        .visible(place::get)
        .build()
    );

    private final Setting<Swap> autoSwap = sgPlace.add(new EnumSetting.Builder<Swap>()
        .name("Swap")
        .description("Swaps to an end crystal before placing if the player is not holding one")
        .defaultValue(Swap.OFF)
        .visible(place::get)
        .build()
    );

    private final Setting<Boolean> antiSurroundC = sgPlace.add(new BoolSetting.Builder()
        .name("Anti Surround")
        .description("Places on mining blocks that when broken, can be placed on to damage enemies. Instantly destroys items spawned from breaking block and allows faster placing")
        .defaultValue(false)
        .visible(place::get)
        .build()
    );

    private final Setting<ForcePlace> forcePlace = sgPlace.add(new EnumSetting.Builder<ForcePlace>()
        .name("Prevent Replace")
        .description("Attempts to replace crystals in surrounds")
        .defaultValue(ForcePlace.NONE)
        .build()
    );

    private final Setting<Boolean> breakValid = sgPlace.add(new BoolSetting.Builder()
        .name("Strict")
        .description("Only places crystals that can be attacked")
        .defaultValue(false)
        .visible(place::get)
        .build()
    );

    private final Setting<Boolean> strictDirection = sgPlace.add(new BoolSetting.Builder()
        .name("Strict Direction")
        .description("Interacts with only visible directions when placing crystals")
        .defaultValue(false)
        .visible(place::get)
        .build()
    );

    private final Setting<Placements> placements = sgPlace.add(new EnumSetting.Builder<Placements>()
        .name("Placements")
        .description("Version standard for placing end crystals")
        .defaultValue(Placements.NATIVE)
        .visible(place::get)
        .build()
    );

    private final Setting<Float> minDamage = sgDamage.add(new FloatSetting.Builder()
        .name("Min Damage")
        .description("Minimum damage required to consider attacking or placing an end crystal")
        .min(1.0f)
        .defaultValue(4.0f)
        .max(10.0f)
        .build()
    );

    private final Setting<Float> maxLocalDamage = sgDamage.add(new FloatSetting.Builder()
        .name("Max Local Damage")
        .description("The maximum player damage")
        .min(4.0f)
        .defaultValue(12.0f)
        .max(20.0f)
        .build()
    );

    private final Setting<Boolean> assumeArmor = sgDamage.add(new BoolSetting.Builder()
        .name("Assume Best Armor")
        .description("Assumes Prot 0 armor is max armor")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> armorBreaker = sgDamage.add(new BoolSetting.Builder()
        .name("Armor Breaker")
        .description("Attempts to break enemy armor with crystals")
        .defaultValue(true)
        .build()
    );

    private final Setting<Float> armorScale = sgDamage.add(new FloatSetting.Builder()
        .name("Armor Scale")
        .description("Armor damage scale before attempting to break enemy armor with crystals")
        .min(1.0f)
        .defaultValue(5.0f)
        .max(20.0f)
        .visible(armorBreaker::get)
        .build()
    );

    private final Setting<Float> lethalMultiplier = sgDamage.add(new FloatSetting.Builder()
        .name("Lethal Multiplier")
        .description("If we can kill an enemy with this many crystals, disregard damage values")
        .min(0.0f)
        .defaultValue(1.5f)
        .max(4.0f)
        .build()
    );

    private final Setting<Boolean> antiTotem = sgDamage.add(new BoolSetting.Builder()
        .name("Lethal Totem")
        .description("Predicts totems and places crystals to instantly double pop and kill the target")
        .defaultValue(false)
        .visible(place::get)
        .build()
    );

    private final Setting<Boolean> lethalDamage = sgDamage.add(new BoolSetting.Builder()
        .name("Lethal DamageTick")
        .description("Places lethal crystals only on ticks where they damage entities")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> safety = sgDamage.add(new BoolSetting.Builder()
        .name("Safety")
        .description("Accounts for total player safety when attacking and placing crystals")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> safetyOverride = sgDamage.add(new BoolSetting.Builder()
        .name("Safety Override")
        .description("Overrides the safety checks if the crystal will kill an enemy")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> blockDestruction = sgDamage.add(new BoolSetting.Builder()
        .name("Block Destruction")
        .description("Accounts for explosion block destruction when calculating damages")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> selfExtrapolate = sgDamage.add(new BoolSetting.Builder()
        .name("Self Extrapolate")
        .description("Accounts for motion when calculating self damage")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> extrapolateTicks = sgDamage.add(new IntSetting.Builder()
        .name("Extrapolation Ticks")
        .description("Accounts for motion when calculating enemy positions, not fully accurate.")
        .min(0)
        .defaultValue(0)
        .max(10)
        .build()
    );

    // Render

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("Renders the current placement")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> fadeTime = sgRender.add(new IntSetting.Builder()
        .name("Fade Time")
        .description("Timer for the fade")
        .min(0)
        .defaultValue(250)
        .max(1000)
        .build()
    );

    private final Setting<Boolean> disableDeath = sgRender.add(new BoolSetting.Builder()
        .name("Disable On Death")
        .description("Disables during disconnect/death")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> debugDamage = sgRender.add(new BoolSetting.Builder()
        .name("Debug Damage")
        .description("Renders damage")
        .defaultValue(false)
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Render Color")
        .description("asdsadsadsadsadsa")
        .defaultValue(new Color(236, 243, 122, 40))
        .build()
    );

    //
    private DamageData<EndCrystalEntity> attackCrystal;
    private DamageData<BlockPos> placeCrystal;
    //
    private BlockPos renderPos;
    private double renderDamage;
    private BlockPos renderSpawnPos;
    //
    private Vec3d crystalRotation;
    private boolean attackRotate;
    private boolean rotated;
    private float[] silentRotations;
    private float calculatePlaceCrystalTime = 0;
    //
    private static final Box FULL_CRYSTAL_BB = new Box(0.0, 0.0, 0.0, 1.0, 2.0, 1.0);
    private static final Box HALF_CRYSTAL_BB = new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    private final CacheTimer lastAttackTimer = new CacheTimer();
    private final Timer lastPlaceTimer = new CacheTimer();
    private final Timer lastSwapTimer = new CacheTimer();
    private final Timer autoSwapTimer = new CacheTimer();
    // default NCP config
    // fight.speed: limit: 13
    // shortterm: ticks: 8
    // limitforseconds: half: 8, one: 15, two: 30, four: 60, eight: 100
    private final Deque<Long> attackLatency = new EvictingQueue<>(20);
    private final Map<Integer, Long> attackPackets =
        Collections.synchronizedMap(new ConcurrentHashMap<>());
    private final Map<BlockPos, Long> placePackets =
        Collections.synchronizedMap(new ConcurrentHashMap<>());
    private final PerSecondCounter crystalCounter = new PerSecondCounter();
    private final Map<BlockPos, Animation> fadeList = new HashMap<>();
    private long predictId;
    // Antistuck
    private final Map<Integer, Integer> antiStuckCrystals = new HashMap<>();
    private final List<AntiStuckData> stuckCrystals = new CopyOnWriteArrayList<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    /*@Override
    public String getModuleData()
    {
        if (debug.get())
        {
            return String.format("%sms, %.0f, %dms, %d".formatted(
                new DecimalFormat("0.00")
                    .format(calculatePlaceCrystalTime / 1E6),
                placeCrystal == null ? 0 : lastAttackTimer.getLastResetTime() / 1E6,
                lastAttackTimer.passed(((20.0f - breakSpeed.get()) * 50.0f) + 2000.0f) ? 0 : getBreakMs(),
                crystalCounter.getPerSecond()));
        }
        else
        {
            return String.format("%dms, %d",
                lastAttackTimer.passed(((20.0f - breakSpeed.get()) * 50.0f) + 2000.0f) ? 0 : getBreakMs(),
                crystalCounter.getPerSecond());
        }
    }*/

    @Override
    public void onDeactivate()
    {
        renderPos = null;
        attackCrystal = null;
        placeCrystal = null;
        crystalRotation = null;
        silentRotations = null;
        calculatePlaceCrystalTime = 0;
        stuckCrystals.clear();
        attackPackets.clear();
        antiStuckCrystals.clear();
        placePackets.clear();
        attackLatency.clear();
        fadeList.clear();
        setStage("NONE");
    }

    @EventHandler
    public void onGameLeft(GameLeftEvent event) {
        if (disableDeath.get())
        {
            this.toggle();
        }
        else
        {
            this.onDeactivate();
        }
    }

    @EventHandler
    public void onPlayerUpdate(PlayerTickEvent event)
    {
        if (mc.player.isSpectator() || isSilentSwap(autoSwap.get()) && Modules.get().get(GenyoAutoMine.class).isSilentSwapping())
        {
            return;
        }


        for (AntiStuckData d : stuckCrystals)
        {
            double dist = mc.player.squaredDistanceTo(d.pos());
            double diff = d.stuckDist() - dist;
            if (diff > 0.5)
            {
                stuckCrystals.remove(d);
            }
        }

        if (mc.player.isUsingItem() && mc.player.getActiveHand() == Hand.MAIN_HAND
            || mc.options.attackKey.isPressed() || PlayerUtil.isHotbarKeysPressed())
        {
            autoSwapTimer.reset();
        }
        renderPos = null;
        ArrayList<Entity> entities = Lists.newArrayList(mc.world.getEntities());
        List<BlockPos> blocks = getSphere(placeRangeEye.get() ? mc.player.getEyePos() : mc.player.getPos());
        long timePre = System.nanoTime();
        if (place.get())
        {
            placeCrystal = calculatePlaceCrystal(blocks, entities);
        }
        attackCrystal = calculateAttackCrystal(entities);
        if (attackCrystal == null)
        {
            if (placeCrystal != null)
            {
                EndCrystalEntity crystalEntity = intersectingCrystalCheck(placeCrystal.getDamageData());
                if (crystalEntity != null)
                {
                    GenyoAddon.LOG.info("fewkjkfewf");

                    double self = ExplosionUtil.getDamageTo(mc.player, crystalEntity.getPos(),
                        blockDestruction.get(), selfExtrapolate.get() ? extrapolateTicks.get() : 0, false);
                    if (!safety.get() || !playerDamageCheck(self))
                    {
                        attackCrystal = new DamageData<>(crystalEntity, placeCrystal.getAttackTarget(),
                            placeCrystal.getDamage(), self, crystalEntity.getBlockPos().down(), false);
                    }
                }
            }
            calculatePlaceCrystalTime = System.nanoTime() - timePre;
        }

        if (inhibit.get() && attackCrystal != null
            && attackPackets.containsKey(attackCrystal.getDamageData().getId()))
        {
            float delay;
            if (attackDelay.get() > 0.0)
            {
                float attackFactor = 50.0f / Math.max(1.0f, attackFactorC.get());
                delay = attackDelay.get() * attackFactor;
            }
            else
            {
                delay = 1000.0f - breakSpeed.get() * 50.0f;
            }
            lastAttackTimer.setDelay(delay + 100.0f);
            attackPackets.remove(attackCrystal.getDamageData().getId());
        }

        float breakDelay = getBreakDelay();
        if (breakDelayC.get())
        {
            breakDelay = Math.max(minTimeout.get() * 50.0f, getBreakMs() + breakTimeout.get() * 50.0f);
        }
        attackRotate = attackCrystal != null && attackDelay.get() <= 0.0 && lastAttackTimer.passed(breakDelay);
        if (attackCrystal != null)
        {
            crystalRotation = attackCrystal.damageData.getPos();
        }
        else if (placeCrystal != null)
        {
            crystalRotation = placeCrystal.damageData.toCenterPos().add(0.0, 0.5, 0.0);
        }
        /*if (rotate.get() && crystalRotation != null && (placeCrystal == null || canHoldCrystal()))
        {
            float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), crystalRotation);
            if (strictRotate.get() == Rotate.FULL || strictRotate.get() == Rotate.SEMI && attackRotate)
            {
                float yaw;
                float serverYaw = Managers.ROTATION.getWrappedYaw();
                float diff = serverYaw - rotations[0];
                float diff1 = Math.abs(diff);
                if (diff1 > 180.0f)
                {
                    diff += diff > 0.0f ? -360.0f : 360.0f;
                }
                int dir = diff > 0.0f ? -1 : 1;
                float deltaYaw = dir * rotateLimit.get();
                if (diff1 > rotateLimit.get())
                {
                    yaw = serverYaw + deltaYaw;
                    rotated = false;
                }
                else
                {
                    yaw = rotations[0];
                    rotated = true;
                    crystalRotation = null;
                }
                rotations[0] = yaw;
            }
            else
            {
                rotated = true;
                crystalRotation = null;
            }
            setRotation(rotations[0], rotations[1]);
        }
        else
        {
            silentRotations = null;
        }
        if (isRotationBlocked() || !rotated && rotate.get())
        {
            return;
        }*/
//        if (rotateSilent.get() && silentRotations != null) {
//            setRotationSilent(silentRotations[0], silentRotations[1]);
//        }
        final Hand hand = getCrystalHand();
        if (attackCrystal != null)
        {
            // ChatUtil.clientSendMessage("yaw: " + rotations[0] + ", pitch: " + rotations[1]);
            if (attackRotate)
            {
                // ChatUtil.clientSendMessage("break range:" + Math.sqrt(mc.player.getEyePos().squaredDistanceTo(attackCrystal.getDamageData().getPos())));
                attackCrystal(attackCrystal.getDamageData(), hand);
                setStage("ATTACKING");
                lastAttackTimer.reset();
            }
        }
        boolean placeRotate = lastPlaceTimer.passed(1000.0f - placeSpeed.get() * 50.0f);
        if (placeCrystal != null)
        {
            renderPos = placeCrystal.getDamageData();
            renderDamage = placeCrystal.getDamage();
            if (placeRotate)
            {
                // ChatUtil.clientSendMessage("place range:" + Math.sqrt(mc.player.getEyePos().squaredDistanceTo(placeCrystal.getDamageData().toCenterPos())));
                placeCrystal(placeCrystal.getDamageData(), hand);
                setStage("PLACING");
                lastPlaceTimer.reset();
            }
        }
    }

    @EventHandler
    public void onRunTick(RunTickEvent event)
    {
        if (mc.player == null) return;

        final Hand hand = getCrystalHand();
        if (attackDelay.get() > 0.0)
        {
            float attackFactor = 50.0f / Math.max(1.0f, attackFactorC.get());
            if (attackCrystal != null && lastAttackTimer.passed(attackDelay.get() * attackFactor))
            {
                attackCrystal(attackCrystal.getDamageData(), hand);
                lastAttackTimer.reset();
            }
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event)
    {
        if (render.get())
        {
            BlockPos renderPos1 = null;
            double factor = 0.0f;
            for (Map.Entry<BlockPos, Animation> set : fadeList.entrySet())
            {
                if (set.getKey() == renderPos)
                {
                    continue;
                }

                if (set.getValue().getFactor() > factor)
                {
                    renderPos1 = set.getKey();
                    factor = set.getValue().getFactor();
                }

                set.getValue().setState(false);
                int boxAlpha = (int) (40 * set.getValue().getFactor());
                int lineAlpha = (int) (100 * set.getValue().getFactor());

                Color boxColor = color.get().a(boxAlpha);
                Color lineColor = color.get().a(lineAlpha);

                event.renderer.box(BlockPos.ofFloored(set.getKey().toCenterPos()), boxColor, lineColor, ShapeMode.Both, 0);
            }

            /*if (debugDamage.get() && renderPos1 != null)
            {
                RenderManager.renderSign(String.format("%.1f", renderDamage),
                    renderPos1.toCenterPos(), new Color(255, 255, 255, (int) (255.0f * factor)).getRGB());
            }*/

            fadeList.entrySet().removeIf(e ->
                e.getValue().getFactor() == 0.0);

            if (renderPos != null && isHoldingCrystal())
            {
                Animation animation = new Animation(true, fadeTime.get());
                fadeList.put(renderPos, animation);
            }
        }
    }

    @EventHandler(priority = Integer.MAX_VALUE)
    public void onPacketReceive(PacketEvent.Receive event)
    {
        if (mc.player == null || mc.world == null) return;

        if (event.packet instanceof BundleS2CPacket packet)
        {
            for (Packet<?> packet1 : packet.getPackets())
            {
                handleServerPackets(packet1);
            }
        }
        else
        {
            handleServerPackets(event.packet);
        }
    }

    private void handleServerPackets(Packet<?> serverPacket)
    {
        if (serverPacket instanceof ExplosionS2CPacket packet)
        {
            for (Entity entity : Lists.newArrayList(mc.world.getEntities()))
            {
                if (entity instanceof EndCrystalEntity && entity.squaredDistanceTo(packet.center().getX(), packet.center().getY(), packet.center().getZ()) < 144.0)
                {
                    mc.executeSync(() -> mc.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED));
                    antiStuckCrystals.remove(entity.getId());
                    Long attackTime = attackPackets.remove(entity.getId());
                    if (attackTime != null)
                    {
                        attackLatency.add(System.currentTimeMillis() - attackTime);
                    }
                }
            }
        }

        if (serverPacket instanceof PlaySoundS2CPacket packet)
        {
            if (packet.getSound().value() == SoundEvents.ENTITY_GENERIC_EXPLODE.value() && packet.getCategory() == SoundCategory.BLOCKS)
            {
                for (Entity entity : Lists.newArrayList(mc.world.getEntities()))
                {
                    if (entity instanceof EndCrystalEntity && entity.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ()) < 144.0)
                    {
                        mc.executeSync(() -> mc.world.removeEntity(entity.getId(), Entity.RemovalReason.DISCARDED));
                        antiStuckCrystals.remove(entity.getId());
                        Long attackTime = attackPackets.remove(entity.getId());
                        if (attackTime != null)
                        {
                            attackLatency.add(System.currentTimeMillis() - attackTime);
                        }
                    }
                }
            }
        }

        if (serverPacket instanceof EntitiesDestroyS2CPacket packet)
        {
            for (int id : packet.getEntityIds())
            {
                antiStuckCrystals.remove(id);
                Long attackTime = attackPackets.remove(id);
                if (attackTime != null)
                {
                    attackLatency.add(System.currentTimeMillis() - attackTime);
                }
            }
        }

        if (serverPacket instanceof ExperienceOrbSpawnS2CPacket packet && packet.getEntityId() > predictId)
        {
            predictId = packet.getEntityId();
        }

        if (serverPacket instanceof EntitySpawnS2CPacket packet && packet.getEntityId() > predictId)
        {
            predictId = packet.getEntityId();
        }
    }

    @EventHandler
    public void onEntityAdded(EntityAddedEvent event)
    {
        if (!(event.entity instanceof EndCrystalEntity crystalEntity)) return;


        Vec3d crystalPos = crystalEntity.getPos();
        BlockPos blockPos = BlockPos.ofFloored(crystalPos.add(0.0, -1.0, 0.0));
        renderSpawnPos = blockPos;
        Long time = placePackets.remove(blockPos);
        attackRotate = time != null;
        if (attackRotate)
        {
            crystalCounter.updateCounter();
        }
        if (!instant.get())
        {
            return;
        }
        if (attackRotate)
        {
            final Hand hand = getCrystalHand();
            attackInternal(crystalEntity, hand);
            setStage("ATTACKING");
            lastAttackTimer.reset();
            if (sequential.get() == Sequential.NORMAL)
            {
                placeSequentialCrystal(hand);
            }
        }
        else if (instantCalc.get())
        {
            if (attackRangeCheck(crystalPos))
            {
                return;
            }
            double selfDamage = ExplosionUtil.getDamageTo(mc.player, crystalPos,
                blockDestruction.get(), selfExtrapolate.get() ? extrapolateTicks.get() : 0, false);
            if (playerDamageCheck(selfDamage))
            {
                return;
            }
            for (Entity entity : mc.world.getEntities())
            {
                if (entity == null || !entity.isAlive() || entity == mc.player
                    || !isValidTarget(entity)
                    || (entity instanceof PlayerEntity && Friends.get().isFriend((PlayerEntity) entity)))
                {
                    continue;
                }
                double crystalDist = crystalPos.squaredDistanceTo(entity.getPos());
                if (crystalDist > 144.0f)
                {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist > targetRange.get() * targetRange.get())
                {
                    continue;
                }

                double damage = ExplosionUtil.getDamageTo(entity, crystalPos, blockDestruction.get(),
                    extrapolateTicks.get(), assumeArmor.get());
                // TODO: Test this
                DamageData<EndCrystalEntity> data = new DamageData<>(crystalEntity,
                    entity, damage, selfDamage, crystalEntity.getBlockPos().down(), false);
                attackRotate = damage > instantDamage.get() || attackCrystal != null
                    && damage >= attackCrystal.getDamage() && instantMax.get()
                    || entity instanceof LivingEntity entity1 && isCrystalLethalTo(data, entity1);
                if (attackRotate)
                {
                    final Hand hand = getCrystalHand();
                    attackInternal(crystalEntity, hand);
                    setStage("ATTACKING");
                    lastAttackTimer.reset();
                    if (sequential.get() == Sequential.NORMAL)
                    {
                        placeSequentialCrystal(hand);
                    }
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event)
    {
        if (mc.player == null) return;


        if (event.packet instanceof UpdateSelectedSlotC2SPacket)
        {
            lastSwapTimer.reset();
        }
    }

    public boolean isAttacking()
    {
        return attackCrystal != null;
    }

    public boolean isPlacing()
    {
        return placeCrystal != null && isHoldingCrystal();
    }

    public void attackCrystal(EndCrystalEntity entity, Hand hand)
    {
        if (attackCheckPre(hand))
        {
            return;
        }
        StatusEffectInstance weakness = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
        StatusEffectInstance strength = mc.player.getStatusEffect(StatusEffects.STRENGTH);
        if (weakness != null && (strength == null || weakness.getAmplifier() > strength.getAmplifier()))
        {
            int slot = -1;
            for (int i = 0; i < 9; ++i)
            {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (!stack.isEmpty() && (stack.getItem() instanceof SwordItem
                    || stack.getItem() instanceof AxeItem
                    || stack.getItem() instanceof PickaxeItem))
                {
                    slot = i;
                    break;
                }
            }
            if (slot != -1)
            {
                boolean canSwap = slot != Managers.INVENTORY.getServerSlot() && (antiWeakness.get() != Swap.NORMAL || autoSwapTimer.passed(500));
                if (antiWeakness.get() != Swap.OFF && canSwap)
                {
                    if (antiWeakness.get() == Swap.SILENT_ALT)
                    {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            slot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    }
                    else if (antiWeakness.get() == Swap.SILENT)
                    {
                        Managers.INVENTORY.setSlot(slot);
                    }
                    else
                    {
                        Managers.INVENTORY.setClientSlot(slot);
                    }
                }
                attackInternal(entity, Hand.MAIN_HAND);
                if (canSwap)
                {
                    if (antiWeakness.get() == Swap.SILENT_ALT)
                    {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            slot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    }
                    else if (antiWeakness.get() == Swap.SILENT)
                    {
                        Managers.INVENTORY.syncToClient();
                    }
                }

                if (sequential.get() == Sequential.STRICT)
                {
                    placeSequentialCrystal(hand);
                }
            }
        }
        else
        {
            attackInternal(entity, hand);
            if (sequential.get() == Sequential.STRICT)
            {
                placeSequentialCrystal(hand);
            }
        }
    }

    private void attackInternal(EndCrystalEntity crystalEntity, Hand hand)
    {
        attackInternal(crystalEntity.getId(), hand);
    }

    private void attackInternal(int crystalEntity, Hand hand)
    {
        hand = hand != null ? hand : Hand.MAIN_HAND;
        EndCrystalEntity entity2 = new EndCrystalEntity(mc.world, 0.0, 0.0, 0.0);
        entity2.setId(crystalEntity);
        PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(entity2, mc.player.isSneaking());
        mc.getNetworkHandler().sendPacket(packet);
        if (swing.get())
        {
            mc.player.swingHand(hand);
        }
        else
        {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
        }

        attackPackets.put(crystalEntity, System.currentTimeMillis());
        Integer antiStuckCount = antiStuckCrystals.get(crystalEntity);
        if (antiStuckCount != null)
        {
            antiStuckCrystals.replace(crystalEntity, antiStuckCount + 1);
        }
        else
        {
            antiStuckCrystals.put(crystalEntity, 1);
        }
    }

    private void placeSequentialCrystal(Hand hand)
    {
        if (placeCrystal == null)
        {
            return;
        }
        int latency = Managers.NETWORK.getClientLatency();
        if (!Managers.NETWORK.is2b2t() || latency >= 50)
        {
            placeCrystal(placeCrystal.getBlockPos(), hand);
        }
    }

    private void placeCrystal(BlockPos blockPos, Hand hand)
    {
        /*if (isRotationBlocked() || !rotated && rotate.get())
        {
            return;
        }*/

        placeCrystal(blockPos, hand, true);
    }

    public void placeCrystal(BlockPos blockPos, Hand hand, boolean checkPlacement)
    {
        if (checkPlacement && checkCanUseCrystal())
        {
            return;
        }
        Direction sidePlace = getPlaceDirection(blockPos);
        BlockHitResult result = new BlockHitResult(blockPos.toCenterPos(), sidePlace, blockPos, false);
        if (autoSwap.get() != Swap.OFF && hand != Hand.OFF_HAND && getCrystalHand() == null)
        {
            if (isSilentSwap(autoSwap.get()) && InventoryUtil.count(Items.END_CRYSTAL) == 0)
            {
                return;
            }
            int crystalSlot = getCrystalSlot();
            if (crystalSlot != -1)
            {
                boolean canSwap = crystalSlot != Managers.INVENTORY.getServerSlot() && (autoSwap.get() != Swap.NORMAL || autoSwapTimer.passed(500));
                if (canSwap)
                {
                    if (autoSwap.get() == Swap.SILENT_ALT)
                    {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            crystalSlot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    }
                    else if (autoSwap.get() == Swap.SILENT)
                    {
                        Managers.INVENTORY.setSlot(crystalSlot);
                    }
                    else
                    {
                        Managers.INVENTORY.setClientSlot(crystalSlot);
                    }
                }
                placeInternal(result, Hand.MAIN_HAND);
                placePackets.put(blockPos, System.currentTimeMillis());
                if (canSwap)
                {
                    if (autoSwap.get() == Swap.SILENT_ALT)
                    {
                        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId,
                            crystalSlot + 36, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
                    }
                    else if (autoSwap.get() == Swap.SILENT)
                    {
                        Managers.INVENTORY.syncToClient();
                    }
                }
            }
        }
        else if (isHoldingCrystal())
        {
            placeInternal(result, hand);
            placePackets.put(blockPos, System.currentTimeMillis());
        }
    }

    private void placeInternal(BlockHitResult result, Hand hand)
    {
        if (hand == null)
        {
            return;
        }
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(hand, result, id));
        if (swing.get())
        {
            mc.player.swingHand(hand);
        }
        else
        {
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(hand));
        }

        // Entity ID predict
        if (idPredict.get())
        {
            //boolean flag = AutoXPModule.getInstance().isEnabled() || mc.player.isUsingItem() && mc.player.getStackInHand(mc.player.getActiveHand()).getItem() instanceof ExperienceBottleItem;
            boolean flag = mc.player.isUsingItem() && mc.player.getStackInHand(mc.player.getActiveHand()).getItem() instanceof ExperienceBottleItem;
            int id = (int) (predictId + 1);
            if (flag || attackPackets.containsKey(id))
            {
                return;
            }
            Entity entity = mc.world.getEntityById(id);
            if (entity != null && !(entity instanceof EndCrystalEntity))
            {
                return;
            }
            EndCrystalEntity entity2 = new EndCrystalEntity(mc.world, 0.0, 0.0, 0.0);
            entity2.setId(id);
            PlayerInteractEntityC2SPacket packet = PlayerInteractEntityC2SPacket.attack(entity2, false);
            Managers.NETWORK.sendPacket(packet);
            Managers.NETWORK.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            attackPackets.put(id, System.currentTimeMillis());
        }
    }

    private boolean isSilentSwap(Swap swap)
    {
        return swap == Swap.SILENT || swap == Swap.SILENT_ALT;
    }

    private int getCrystalSlot()
    {
        int slot = -1;
        for (int i = 0; i < 9; i++)
        {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof EndCrystalItem)
            {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private Direction getPlaceDirection(BlockPos blockPos)
    {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        if (strictDirection.get())
        {
            if (mc.player.getY() >= blockPos.getY())
            {
                return Direction.UP;
            }
            BlockHitResult result = mc.world.raycast(new RaycastContext(
                mc.player.getEyePos(), new Vec3d(x + 0.5, y + 0.5, z + 0.5),
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE, mc.player));
            if (result != null && result.getType() == HitResult.Type.BLOCK)
            {
                return result.getSide();
            }
        }
        else
        {
            if (mc.world.isInBuildLimit(blockPos))
            {
                return Direction.DOWN;
            }
            BlockHitResult result = mc.world.raycast(new RaycastContext(
                mc.player.getEyePos(), new Vec3d(x + 0.5, y + 0.5, z + 0.5),
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE, mc.player));
            if (result != null && result.getType() == HitResult.Type.BLOCK)
            {
                return result.getSide();
            }
        }
        return Direction.UP;
    }

    private DamageData<EndCrystalEntity> calculateAttackCrystal(List<Entity> entities)
    {
        if (entities.isEmpty())
        {
            return null;
        }

        final List<DamageData<EndCrystalEntity>> validData = new ArrayList<>();

        DamageData<EndCrystalEntity> data = null;
        for (Entity crystal : entities)
        {
            if (!(crystal instanceof EndCrystalEntity crystal1) || !crystal.isAlive()
                || stuckCrystals.stream().anyMatch(d -> d.id() == crystal.getId()))
            {
                continue;
            }
            Long time = attackPackets.get(crystal.getId());
            boolean attacked = time != null && time < getBreakMs();
            if ((crystal.age < ticksExisted.get() || attacked) && inhibit.get())
            {
                continue;
            }
            if (attackRangeCheck(crystal1))
            {
                continue;
            }
            double selfDamage = ExplosionUtil.getDamageTo(mc.player, crystal.getPos(),
                blockDestruction.get(), selfExtrapolate.get() ? extrapolateTicks.get() : 0, false);
            boolean unsafeToPlayer = playerDamageCheck(selfDamage);
            if (unsafeToPlayer && !safetyOverride.get())
            {
                continue;
            }
            for (Entity entity : entities)
            {
                if (entity == null || !entity.isAlive() || entity == mc.player
                    || !isValidTarget(entity)
                    || (entity instanceof PlayerEntity && Friends.get().isFriend((PlayerEntity) entity)))
                {
                    continue;
                }
                double crystalDist = crystal.squaredDistanceTo(entity);
                if (crystalDist > 144.0f)
                {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist > targetRange.get() * targetRange.get())
                {
                    continue;
                }

                boolean antiSurround = false;
                if (antiSurroundC.get() && entity instanceof PlayerEntity player
                    && !BlastResistantBlocks.isUnbreakable(player.getBlockPos()))
                {
                    Set<BlockPos> miningPositions = new HashSet<>();
                    BlockPos miningBlock = Modules.get().get(GenyoAutoMine.class).getMiningBlock();
                    if (Modules.get().get(GenyoAutoMine.class).isActive() && miningBlock != null)
                    {
                        miningPositions.add(miningBlock);
                    }
                    if (Managers.BLOCK.getMines(0.75f).contains(player.getBlockPos().up()))
                    {
                        miningPositions.add(player.getBlockPos().up());
                    }
                    for (BlockPos miningBlockPos : miningPositions)
                    {
                        if (!Modules.get().get(GenyoSurroundV2.class).getSurroundNoDown(player).contains(miningBlockPos))
                        {
                            continue;
                        }
                        for (Direction direction : Direction.values())
                        {
                            BlockPos pos1 = miningBlockPos.offset(direction);
                            if (crystal.getBlockPos().equals(pos1.down()))
                            {
                                antiSurround = true;
                            }
                        }
                    }
                }

                double damage = ExplosionUtil.getDamageTo(entity, crystal.getPos(), blockDestruction.get(),
                    extrapolateTicks.get(), assumeArmor.get());
                if (checkOverrideSafety(unsafeToPlayer, damage, entity))
                {
                    continue;
                }

                DamageData<EndCrystalEntity> currentData = new DamageData<>(crystal1, entity,
                    damage, selfDamage, crystal1.getBlockPos().down(), antiSurround);
                validData.add(currentData);
                if (data == null || damage > data.getDamage())
                {
                    data = currentData;
                }
            }
        }
        if (data == null || targetDamageCheck(data))
        {
            if (antiSurroundC.get())
            {
                return validData.stream()
                    .filter(DamageData::isAntiSurround)
                    .min(Comparator.comparingDouble(d -> mc.player.squaredDistanceTo(d.getBlockPos().toCenterPos())))
                    .orElse(null);
            }
            return null;
        }
        return data;
    }

    private boolean attackRangeCheck(EndCrystalEntity entity)
    {
        return attackRangeCheck(entity.getPos());
    }

    /**
     * @param entityPos
     * @return
     */
    private boolean attackRangeCheck(Vec3d entityPos)
    {
        double breakRange = breakRangeC.get();
        double breakWallRange = breakWallRangeC.get();
        Vec3d playerPos = mc.player.getEyePos();
        double dist = playerPos.squaredDistanceTo(entityPos);
        if (dist > breakRange * breakRange)
        {
            return true;
        }
        double yOff = Math.abs(entityPos.getY() - mc.player.getY());
        if (yOff > maxYOffset.get())
        {
            return true;
        }
        BlockHitResult result = mc.world.raycast(new RaycastContext(
            playerPos, entityPos, RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE, mc.player));
        return result.getType() != HitResult.Type.MISS && dist > breakWallRange * breakWallRange;
    }

    private DamageData<BlockPos> calculatePlaceCrystal(List<BlockPos> placeBlocks, List<Entity> entities)
    {
        if (placeBlocks.isEmpty() || entities.isEmpty())
        {
            return null;
        }

        final List<DamageData<BlockPos>> validData = new ArrayList<>();

        DamageData<BlockPos> data = null;
        for (BlockPos pos : placeBlocks)
        {
            if (!canUseCrystalOnBlock(pos) || placeRangeCheck(pos) || intersectingAntiStuckCheck(pos))
            {
                continue;
            }
            double selfDamage = ExplosionUtil.getDamageTo(mc.player, crystalDamageVec(pos),
                blockDestruction.get(), selfExtrapolate.get() ? extrapolateTicks.get() : 0, false);
            boolean unsafeToPlayer = playerDamageCheck(selfDamage);
            if (unsafeToPlayer && !safetyOverride.get())
            {
                continue;
            }
            for (Entity entity : entities)
            {
                if (entity == null || !entity.isAlive() || entity == mc.player
                    || !isValidTarget(entity)
                    || (entity instanceof PlayerEntity && Friends.get().isFriend((PlayerEntity) entity)))
                {
                    continue;
                }
                double blockDist = pos.getSquaredDistance(entity.getPos());
                if (blockDist > 144.0f)
                {
                    continue;
                }
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist > targetRange.get() * targetRange.get())
                {
                    continue;
                }

                boolean antiSurround = false;
                if (antiSurroundC.get() && entity instanceof PlayerEntity player
                    && !BlastResistantBlocks.isUnbreakable(player.getBlockPos()))
                {
                    Set<BlockPos> miningPositions = new HashSet<>();
                    BlockPos miningBlock = Modules.get().get(GenyoAutoMine.class).getMiningBlock();
                    if (Modules.get().get(GenyoAutoMine.class).isActive() && miningBlock != null)
                    {
                        miningPositions.add(miningBlock);
                    }
                    if (Managers.BLOCK.getMines(0.75f).contains(player.getBlockPos().up()))
                    {
                        miningPositions.add(player.getBlockPos().up());
                    }
                    for (BlockPos miningBlockPos : miningPositions)
                    {
                        if (!Modules.get().get(GenyoSurroundV2.class).getSurroundNoDown(player).contains(miningBlockPos))
                        {
                            continue;
                        }
                        for (Direction direction : Direction.values())
                        {
                            BlockPos pos1 = miningBlockPos.offset(direction);
                            if (pos.equals(pos1.down()))
                            {
                                antiSurround = true;
                            }
                        }
                    }
                }

                double damage;
                damage = ExplosionUtil.getDamageTo(entity, crystalDamageVec(pos), blockDestruction.get(),
                    extrapolateTicks.get(), assumeArmor.get());
                if (checkOverrideSafety(unsafeToPlayer, damage, entity))
                {
                    continue;
                }

                DamageData<BlockPos> currentData = new DamageData<>(pos, entity,
                    damage, selfDamage, antiSurround);
                validData.add(currentData);
                if (data == null || damage > data.getDamage())
                {
                    data = currentData;
                }
            }
        }
        if (data == null || targetDamageCheck(data))
        {
            if (antiSurroundC.get())
            {
                return validData.stream()
                    .filter(DamageData::isAntiSurround)
                    .min(Comparator.comparingDouble(d -> mc.player.squaredDistanceTo(d.getBlockPos().toCenterPos())))
                    .orElse(null);
            }
            return null;
        }
        return data;
    }

    /**
     * @param pos
     * @return
     */
    private boolean placeRangeCheck(BlockPos pos)
    {
        double placeRange = placeRangeC.get();
        double placeWallRange = placeWallRangeC.get();
        Vec3d player = placeRangeEye.get() ? mc.player.getEyePos() : mc.player.getPos();
        double dist = placeRangeCenter.get() ?
            player.squaredDistanceTo(pos.toCenterPos()) : pos.getSquaredDistance(player.x, player.y, player.z);
        if (dist > placeRange * placeRange)
        {
            return true;
        }
        Vec3d raytrace = Vec3d.of(pos).add(0.5, 2.70000004768372, 0.5);
        BlockHitResult result = mc.world.raycast(new RaycastContext(
            mc.player.getEyePos(), raytrace,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE, mc.player));
        float maxDist = breakRangeC.get() * breakRangeC.get();
        if (result != null && result.getType() == HitResult.Type.BLOCK && !result.getBlockPos().equals(pos))
        {
            maxDist = breakWallRangeC.get() * breakWallRangeC.get();
            if (!raytraceC.get() || dist > placeWallRange * placeWallRange)
            {
                return true;
            }
        }
        return breakValid.get() && dist > maxDist;
    }

    public void placeCrystalForTarget(PlayerEntity target, BlockPos blockPos)
    {
        if (target == null || target.isDead() || placeRangeCheck(blockPos) || !canUseCrystalOnBlock(blockPos))
        {
            return;
        }
        double selfDamage = ExplosionUtil.getDamageTo(mc.player, crystalDamageVec(blockPos),
            blockDestruction.get(), Set.of(blockPos), selfExtrapolate.get() ? extrapolateTicks.get() : 0, false);
        if (playerDamageCheck(selfDamage))
        {
            return;
        }
        double damage = ExplosionUtil.getDamageTo(target, crystalDamageVec(blockPos), blockDestruction.get(),
            Set.of(blockPos), extrapolateTicks.get(), assumeArmor.get());
        if (damage < minDamage.get() && !isCrystalLethalTo(damage, target)
            || placeCrystal != null && placeCrystal.getDamage() >= damage)
        {
            return;
        }

        /*float[] rotations = RotationUtil.getRotationsTo(mc.player.getEyePos(), blockPos.toCenterPos());
        setRotation(rotations[0], rotations[1]);*/
        placeCrystal(blockPos, Hand.MAIN_HAND, false);
        fadeList.put(blockPos, new Animation(true, fadeTime.get()));
    }

    private boolean checkOverrideSafety(boolean unsafeToPlayer, double damage, Entity entity)
    {
        return safetyOverride.get() && unsafeToPlayer && damage < EntityUtil.getHealth(entity) + 0.5;
    }

    private boolean targetDamageCheck(DamageData<?> crystal)
    {
        double minDmg = minDamage.get();
        if (crystal.getAttackTarget() instanceof LivingEntity entity && isCrystalLethalTo(crystal, entity))
        {
            minDmg = 2.0f;
        }
        return crystal.getDamage() < minDmg;
    }

    private boolean playerDamageCheck(double playerDamage)
    {
        if (!mc.player.isCreative())
        {
            float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
            if (safety.get() && playerDamage >= health + 0.5f)
            {
                return true;
            }
            return playerDamage > maxLocalDamage.get();
        }
        return false;
    }

    private boolean isFeetSurrounded(LivingEntity entity)
    {
        BlockPos pos1 = entity.getBlockPos();
        if (!mc.world.getBlockState(pos1).isReplaceable())
        {
            return true;
        }
        for (Direction direction : Direction.values())
        {
            if (!direction.getAxis().isHorizontal())
            {
                continue;
            }
            BlockPos pos2 = pos1.offset(direction);
            if (mc.world.getBlockState(pos2).isReplaceable())
            {
                return false;
            }
        }
        return true;
    }

    private boolean checkAntiTotem(double damage, LivingEntity entity)
    {
        if (entity instanceof PlayerEntity p)
        {
            float phealth = EntityUtil.getHealth(p);
            if (phealth <= 2.0f && phealth - damage < 0.5f)
            {
                long time = Managers.TOTEM.getLastPopTime(p);
                if (time != -1)
                {
                    return System.currentTimeMillis() - time <= 500;
                }
            }
        }
        return false;
    }

    private boolean isCrystalLethalTo(DamageData<?> crystal, LivingEntity entity)
    {
        return isCrystalLethalTo(crystal.getDamage(), entity);
    }

    private boolean isCrystalLethalTo(double damage, LivingEntity entity)
    {
        if (lethalDamage.get() && lastAttackTimer.passed(500))
        {
            return true;
        }

        if (antiTotem.get() && checkAntiTotem(damage, entity))
        {
            return true;
        }
        float health = entity.getHealth() + entity.getAbsorptionAmount();
        if (damage * (1.0f + lethalMultiplier.get()) >= health + 0.5f)
        {
            return true;
        }
        if (armorBreaker.get())
        {
            for (ItemStack armorStack : entity.getArmorItems())
            {
                int n = armorStack.getDamage();
                int n1 = armorStack.getMaxDamage();
                float durability = ((n1 - n) / (float) n1) * 100.0f;
                if (durability < armorScale.get())
                {
                    return true;
                }
            }
        }

        // Antiregear
        if (shulkers.get() && entity instanceof PlayerEntity)
        {
            for (BlockPos pos : getSphere(3.0f, entity.getPos()))
            {
                BlockState state = mc.world.getBlockState(pos);
                if (state.getBlock() instanceof ShulkerBoxBlock)
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean attackCheckPre(Hand hand)
    {
        if (!lastSwapTimer.passed(swapDelay.get() * 25.0f))
        {
            return true;
        }
        if (hand == Hand.MAIN_HAND)
        {
            return checkCanUseCrystal();
        }
        return false;
    }

    private boolean checkCanUseCrystal()
    {
        return !multitask.get() && checkMultitask()
            || !whileMining.get() && mc.interactionManager.isBreakingBlock();
    }

    private boolean isHoldingCrystal()
    {
        if (!checkCanUseCrystal() && (autoSwap.get() == Swap.SILENT || autoSwap.get() == Swap.SILENT_ALT))
        {
            return true;
        }
        return getCrystalHand() != null;
    }

    private Vec3d crystalDamageVec(BlockPos pos)
    {
        return Vec3d.of(pos).add(0.5, 1.0, 0.5);
    }

    /**
     * Returns <tt>true</tt> if the {@link Entity} is a valid enemy to attack.
     *
     * @param e The potential enemy entity
     * @return <tt>true</tt> if the entity is an enemy
     */
    private boolean isValidTarget(Entity e)
    {
        return e instanceof PlayerEntity && players.get()
            || EntityUtil.isMonster(e) && monsters.get()
            || EntityUtil.isNeutral(e) && neutrals.get()
            || EntityUtil.isPassive(e) && animals.get();
    }

    /**
     * Returns <tt>true</tt> if an {@link EndCrystalItem} can be used on the
     * param {@link BlockPos}.
     *
     * @param pos The block pos
     * @return Returns <tt>true</tt> if the crystal item can be placed on the
     * block
     */
    public boolean canUseCrystalOnBlock(BlockPos pos)
    {
        BlockState state = mc.world.getBlockState(pos);
        if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK))
        {
            return false;
        }
        return isCrystalHitboxClear(pos);
    }

    public boolean isCrystalHitboxClear(BlockPos pos)
    {
        BlockPos p2 = pos.up();
        BlockState state2 = mc.world.getBlockState(p2);
        // ver 1.12.2 and below
        if (placements.get() == Placements.PROTOCOL && !mc.world.isAir(p2.up()))
        {
            return false;
        }
        if (!mc.world.isAir(p2) && !state2.isOf(Blocks.FIRE))
        {
            return false;
        }
        else
        {
            final Box bb = Managers.NETWORK.isCrystalPvpCC() ? HALF_CRYSTAL_BB : FULL_CRYSTAL_BB;
            double d = p2.getX();
            double e = p2.getY();
            double f = p2.getZ();
            List<Entity> list = getEntitiesBlockingCrystal(new Box(d, e, f,
                d + bb.maxX, e + bb.maxY, f + bb.maxZ));
            return list.isEmpty();
        }
    }

    private List<Entity> getEntitiesBlockingCrystal(Box box)
    {
        List<Entity> entities = new CopyOnWriteArrayList<>(
            mc.world.getOtherEntities(null, box));
        //
        for (Entity entity : entities)
        {
            if (entity == null || !entity.isAlive()
                || entity instanceof ExperienceOrbEntity
                || forcePlace.get() != ForcePlace.NONE
                && entity instanceof ItemEntity && entity.age <= 10)
            {
                entities.remove(entity);
            }
            else if (entity instanceof EndCrystalEntity entity1
                && entity1.getBoundingBox().intersects(box))
            {
                Integer antiStuckAttacks = antiStuckCrystals.get(entity1.getId());
                if (!attackRangeCheck(entity1) && (antiStuckAttacks == null || antiStuckAttacks <= attackLimit.get() * 10.0f))
                {
                    entities.remove(entity);
                }
                else
                {
                    double dist = mc.player.squaredDistanceTo(entity1);
                    stuckCrystals.add(new AntiStuckData(entity1.getId(), entity1.getBlockPos(), entity1.getPos(), dist));
                }
            }
        }
        return entities;
    }

    private boolean intersectingAntiStuckCheck(BlockPos blockPos)
    {
        if (stuckCrystals.isEmpty())
        {
            return false;
        }
        return stuckCrystals.stream().anyMatch(d -> d.blockPos().equals(blockPos.up()));
    }

    private EndCrystalEntity intersectingCrystalCheck(BlockPos pos)
    {
        return (EndCrystalEntity) mc.world.getOtherEntities(null, new Box(pos)).stream()
            .filter(e -> e instanceof EndCrystalEntity).min(Comparator.comparingDouble(e -> mc.player.distanceTo(e))).orElse(null);
    }

    private List<BlockPos> getSphere(Vec3d origin)
    {
        double rad = Math.ceil(placeRangeC.get());
        return getSphere(rad, origin);
    }

    private List<BlockPos> getSphere(double rad, Vec3d origin)
    {
        List<BlockPos> sphere = new ArrayList<>();
        for (double x = -rad; x <= rad; ++x)
        {
            for (double y = -rad; y <= rad; ++y)
            {
                for (double z = -rad; z <= rad; ++z)
                {
                    Vec3i pos = new Vec3i((int) (origin.getX() + x),
                        (int) (origin.getY() + y), (int) (origin.getZ() + z));
                    final BlockPos p = new BlockPos(pos);
                    sphere.add(p);
                }
            }
        }
        return sphere;
    }

    private boolean canHoldCrystal()
    {
        return isHoldingCrystal() || autoSwap.get() != Swap.OFF && getCrystalSlot() != -1;
    }

    private Hand getCrystalHand()
    {
        final ItemStack offhand = mc.player.getOffHandStack();
        final ItemStack mainhand = mc.player.getMainHandStack();
        if (offhand.getItem() instanceof EndCrystalItem)
        {
            return Hand.OFF_HAND;
        }
        else if (mainhand.getItem() instanceof EndCrystalItem)
        {
            return Hand.MAIN_HAND;
        }
        return null;
    }

    public float getBreakDelay()
    {
        return 1000.0f - breakSpeed.get() * 50.0f;
    }

    // Debug info
    public void setStage(String crystalStage)
    {
        // this.crystalStage = crystalStage;
    }

    public int getBreakMs()
    {
        if (attackLatency.isEmpty())
        {
            return 0;
        }
        float avg = 0.0f;
        // fix ConcurrentModificationException
        ArrayList<Long> latencyCopy = Lists.newArrayList(attackLatency);
        if (!latencyCopy.isEmpty())
        {
            for (float t : latencyCopy)
            {
                avg += t;
            }
            avg /= latencyCopy.size();
        }
        return (int) avg;
    }

    public boolean shouldPreForcePlace()
    {
        return forcePlace.get() == ForcePlace.PRE;
    }

    public float getPlaceRange()
    {
        return placeRangeC.get();
    }

    public enum Swap
    {
        NORMAL,
        SILENT,
        SILENT_ALT,
        OFF
    }

    public enum Sequential
    {
        NORMAL,
        STRICT,
        NONE
    }

    public enum ForcePlace
    {
        PRE,
        POST,
        NONE
    }

    public enum Placements
    {
        NATIVE,
        PROTOCOL
    }

    public enum Rotate
    {
        FULL,
        SEMI,
        OFF
    }

    private record AntiStuckData(int id, BlockPos blockPos, Vec3d pos, double stuckDist) {}

    private static class DamageData<T>
    {
        //
        private final List<String> tags = new ArrayList<>();
        private T damageData;
        private Entity attackTarget;
        private BlockPos blockPos;
        //
        private double damage, selfDamage;
        private boolean antiSurround;

        //
        public DamageData()
        {

        }

        public DamageData(BlockPos damageData, Entity attackTarget, double damage,
                          double selfDamage, boolean antiSurround)
        {
            this.damageData = (T) damageData;
            this.attackTarget = attackTarget;
            this.damage = damage;
            this.selfDamage = selfDamage;
            this.blockPos = damageData;
            this.antiSurround = antiSurround;
        }

        public DamageData(T damageData, Entity attackTarget, double damage,
                          double selfDamage, BlockPos blockPos, boolean antiSurround)
        {
            this.damageData = damageData;
            this.attackTarget = attackTarget;
            this.damage = damage;
            this.selfDamage = selfDamage;
            this.blockPos = blockPos;
            this.antiSurround = antiSurround;
        }

        public void setDamageData(T damageData, Entity attackTarget, double damage, double selfDamage)
        {
            this.damageData = damageData;
            this.attackTarget = attackTarget;
            this.damage = damage;
            this.selfDamage = selfDamage;
        }

        public T getDamageData()
        {
            return damageData;
        }

        public Entity getAttackTarget()
        {
            return attackTarget;
        }

        public double getDamage()
        {
            return damage;
        }

        public double getSelfDamage()
        {
            return selfDamage;
        }

        public BlockPos getBlockPos()
        {
            return blockPos;
        }

        public boolean isAntiSurround()
        {
            return antiSurround;
        }
    }

    private class AttackCrystalTask implements Callable<DamageData<EndCrystalEntity>>
    {
        private final List<Entity> threadSafeEntities;

        public AttackCrystalTask(List<Entity> threadSafeEntities)
        {
            this.threadSafeEntities = threadSafeEntities;
        }

        @Override
        public DamageData<EndCrystalEntity> call() throws Exception
        {
            return calculateAttackCrystal(threadSafeEntities);
        }
    }

    private class PlaceCrystalTask implements Callable<DamageData<BlockPos>>
    {
        private final List<BlockPos> threadSafeBlocks;
        private final List<Entity> threadSafeEntities;

        public PlaceCrystalTask(List<BlockPos> threadSafeBlocks,
                                List<Entity> threadSafeEntities)
        {
            this.threadSafeBlocks = threadSafeBlocks;
            this.threadSafeEntities = threadSafeEntities;
        }

        @Override
        public DamageData<BlockPos> call() throws Exception
        {
            return calculatePlaceCrystal(threadSafeBlocks, threadSafeEntities);
        }
    }

}
