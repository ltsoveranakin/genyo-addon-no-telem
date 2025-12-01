package com.genyo.systems.modules.world;

import com.genyo.Genyo;
import com.genyo.managers.player.InteractionManager;
import com.genyo.systems.modules.PlacerModule;
import com.genyo.utils.math.timer.CacheTimer;
import com.genyo.utils.math.timer.Timer;
import com.genyo.utils.player.Rotation;
import com.genyo.utils.player.RotationUtil;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class AutoMineV2 extends PlacerModule {

    public AutoMineV2() {
        super(Genyo.WORLD, "auto-mine-v2", "Hopefully better automine");
    }

    /*private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("MS delay")
        .min(0).defaultValue(0).max(1000)
        .sliderRange(0, 1000)
        .onChanged((asd) -> {})
        .build()
    );

    private final Setting<Integer> minDmg = sgGeneral.add(new IntSetting.Builder()
        .name("minimum-damage")
        .description("-")
        .min(0).defaultValue(7).max(36)
        .sliderRange(0, 36)
        .build()
    );

    private final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("")
        .min(1d).defaultValue(5d).max(10d)
        .sliderRange(1d, 10d)
        .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("-")
        .min(3).defaultValue(6).max(6)
        .sliderRange(3, 6)
        .build()
    );

    private final Setting<Integer> blocksPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("blocks-per-tick")
        .description("-")
        .min(1).defaultValue(10).max(10)
        .sliderRange(1, 10)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("-")
        .defaultValue(false)
        .build()
    );

    // render

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("render")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> fill = sgRender.add(new ColorSetting.Builder()
        .name("fill-color")
        .description("Fill Color")
        .defaultValue(new SettingColor(255, 0, 0, 25))
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> line = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Line Color")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .visible(render::get)
        .build()
    );

    private final Setting<Integer> fadeTime = sgRender.add(new IntSetting.Builder()
        .name("fade-time")
        .description("Fade Time in milliseconds")
        .min(0).defaultValue(200).max(1000)
        .sliderRange(0, 1000)
        .build()
    );


    private Timer timer = new CacheTimer();
    public Map<BlockPos, Long> renderPositions = new HashMap<>();

    private Entity target;
    private List<BlockPos> toPlace = new ArrayList<>();

    public Map<BlockPos, Long> placed = new HashMap<>();

    double startY = 0;

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (getSlot() == -1) {
            return;
        }

        for (Map.Entry<BlockPos, Long> entry : placed.entrySet()) {
            BlockPos pos = entry.getKey();
            long time = entry.getValue();

            if (System.currentTimeMillis() - time > 200) {
                placed.remove(pos);
            }
        }

        toPlace.clear();
        int blocksInTick = 0;

        target = TargetUtils.getPlayerTarget(targetRange.get(), SortPriority.LowestDistance);

        if (target != null) {

            BlockPos targetPos = getBestCrystalPlacePos((PlayerEntity) target);

            if (mc.world.getBlockState(targetPos).isReplaceable()) {
                placeLabel:
                if (timer.passed(delay.get())) {

                    if (blocksInTick >= blocksPerTick.get()) break placeLabel;

                    if (toPlace.contains(targetPos)) break placeLabel;

                    if (placed.containsKey(targetPos)) {

                        if (CrystalManager.INSTANCE.isRecentlyBlocked(targetPos))
                            placed.remove(targetPos);
                        else if (System.currentTimeMillis() - placed.get(targetPos) < 60)
                            break placeLabel;
                    }

                    if (blocksInTick == 0 && rotate.get())
                        Rotation.get().setRotationSilentSync();

                    toPlace.add(targetPos);
                    blocksInTick++;
                    timer.reset();
                }
            }
        }

        doPlace();
    }

    public BlockPos getBestCrystalPlacePos(PlayerEntity player) {
        BlockPos bestPos = null;
        double bestDMG = 0.5D;

        final List<BlockPos> sphere = BlockUtils.sphere(range.getValue().doubleValue() + 1.0f, mc.player.getBlockPos(), true, false);
        Set<BlockPos> keySet = new HashSet<>(Set.copyOf(placed.keySet()));
        keySet.addAll(toPlace);

        for (BlockPos pos : sphere) {

            BlockPos basePos = pos.down();

            //check if valid place spot too cuz if theres an obby already there not tryna spam obby like a retard
            if (!CrystalUtil.canPlaceCrystal(basePos, CatAura.INSTANCE.onePointTwelve.getValue()) && !BlockUtils.isReplaceable(basePos))
                continue;

            if (basePos.getY() >= player.getBlockPos().getY())
                continue;

            if(!CrystalUtil.canPlaceCrystalAir(basePos)) continue;

            if(BlockUtils.isBlockedOff(basePos) || BlockUtils.isBlockedOff(pos)) continue;

            if(BlockUtils.isReplaceable(basePos)) {
                if (!BlockUtils.canPlaceBlock(basePos, strictDirection.getValue(), keySet)) continue;
            }

            double distance = mc.player.getEyePos().squaredDistanceTo(new Vec3d(basePos.getX() + 0.5, basePos.getY() + 0.5, basePos.getZ() + 0.5));
            if (distance > MathUtil.square(range.getValue().doubleValue()))
                continue;

            double dmg = CrystalUtil.calculateDamage(player, pos.toCenterPos(), CatAura.INSTANCE.terrain.getValue(), CatAura.INSTANCE.getMiningIgnore());
            if (dmg < minDmg.getValue().doubleValue()) {
                continue;
            }

            if (dmg > bestDMG) {
                bestPos = basePos;
                bestDMG = dmg;
            }


        }
        return bestPos;
    }

    @SubscribeEvent
    public void onPlayerUpdate(TickEvent.PlayerTickEvent.Post event) {

        if (NullUtils.nullCheck()) return;

        if (!AntiCheat.INSTANCE.protocol.getValue())
            doPlace();
    }


    @SubscribeEvent
    public void onCollision(CollisionBoxEvent event) {
        if (NullUtils.nullCheck()) return;


        if (placed.containsKey(event.getPos())) {

            event.setCancelled(true);
            event.setVoxelShape(VoxelShapes.cuboid(new Box(0, 0, 0, 1.0, 1.0, 1.0)));
        }
    }


    @Override
    public void onEnable() {
        super.onEnable();
        if (NullUtils.nullCheck()) return;

        startY = mc.player.getY();

        placed.clear();
    }


    @Override
    public void onDisable() {
        super.onDisable();
        if (NullUtils.nullCheck()) return;

        if (PriorityManager.INSTANCE.isUsageLocked() && PriorityManager.INSTANCE.usageLockCause.equals("AutoPlacer"))
            PriorityManager.INSTANCE.unlockUsageLock();

        placed.clear();

    }

    boolean rotateFlag = false;


    public void doPlace() {
        if (NullUtils.nullCheck()) return;

        int blockSlot = getSlot();

        int oldSlot = mc.player.getInventory().selectedSlot;
        boolean switched = false;
        for (BlockPos pos : toPlace) {

            if (blockSlot != mc.player.getInventory().selectedSlot) {
                InventoryUtils.switchToSlot(blockSlot);
                switched = true;
            }

            placed.put(pos, System.currentTimeMillis());


            if (rotate.getValue() && AntiCheat.INSTANCE.protocol.getValue())
                RotationUtils.doSilentRotate(pos, strictDirection.getValue());

            BlockUtils.placeBlock(pos, BlockUtils.getPlaceableSide(pos, strictDirection.getValue(), placed.keySet()), !mc.player.getMainHandStack().getItem().equals(Items.ENDER_CHEST));


            if (render.getValue())
                renderPositions.put(pos, System.currentTimeMillis());

        }
        if (switched) {
            InventoryUtils.switchToSlot(oldSlot);
        }
        if ((!toPlace.isEmpty() && AntiCheat.INSTANCE.protocol.getValue() && rotate.getValue()) || rotateFlag) {
            RotationUtils.silentSync();
        }
        toPlace.clear();

    }


    int getSlot() {
        return InventoryUtils.getHotbarItemSlot(Items.OBSIDIAN);
    }


    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event) {
        if (NullUtils.nullCheck()) return;

        if (event.getPacket() instanceof BlockUpdateS2CPacket packet) {
            final BlockPos targetPos = packet.getPos();
            if (placed.containsKey(targetPos)) {
                placed.remove(targetPos);
            }
        }
    }

    @Override
    public String getDescription() {
        return "AutoPlacer: places obby in places to place crystals";
    }*/

}
