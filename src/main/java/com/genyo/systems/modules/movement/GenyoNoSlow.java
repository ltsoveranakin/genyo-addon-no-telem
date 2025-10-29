package com.genyo.systems.modules.movement;

import com.genyo.Genyo;
import com.genyo.events.StageEvent;
import com.genyo.events.block.BlockSlipperinessEvent;
import com.genyo.events.block.SteppedOnSlimeBlockEvent;
import com.genyo.events.entity.SlowMovementEvent;
import com.genyo.events.entity.VelocityMultiplierEvent;
import com.genyo.events.network.MovementSlowdownEvent;
import com.genyo.events.network.PlayerUpdateEvent;
import com.genyo.events.network.SetCurrentHandEvent;
import com.genyo.events.network.StrafeFixEvent;
import com.genyo.managers.Managers;
import com.genyo.mixin.accessor.AccessorKeyBinding;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.systems.settings.FloatSetting;
import com.genyo.utils.math.GPositionUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.misc.Notifier;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class GenyoNoSlow extends GenyoModule {

    public GenyoNoSlow() {
        super(Genyo.MOVEMENT, "genyo-no-slow", "Prevents items from slowing the player.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> strict = sgGeneral.add(new BoolSetting.Builder()
        .name("Strict")
        .description("Strict NCP bypass for ground slowdowns")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> airStrict = sgGeneral.add(new BoolSetting.Builder()
        .name("Air Strict")
        .description("Strict NCP bypass for air slowdowns")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> grim = sgGeneral.add(new BoolSetting.Builder()
        .name("Grim")
        .description("Strict Grim bypass for slowdown")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> grimNew = sgGeneral.add(new BoolSetting.Builder()
        .name("Grim V3")
        .description("Strict GrimV3 bypass for slowdown")
        .defaultValue(false)
        .build()
    );

    /*private final Setting<Boolean> strafeFix = sgGeneral.add(new BoolSetting.Builder()
        .name("Strafe Fix")
        .description("Old NCP bypass for strafe")
        .defaultValue(false)
        .build()
    );*/

    private final Setting<Boolean> inventoryMove = sgGeneral.add(new BoolSetting.Builder()
        .name("Inventory Move")
        .description("Allows the player to move while in inventories or screens (gay)")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> arrowMove = sgGeneral.add(new BoolSetting.Builder()
        .name("Arrow Move")
        .description("Allows the player to look while in inventories or screens by using the arrow keys")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> items = sgGeneral.add(new BoolSetting.Builder()
        .name("Items")
        .description("Removes the slowdown effect caused by using items")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> sneak = sgGeneral.add(new BoolSetting.Builder()
        .name("Sneak")
        .description("Removes sneak slowdown")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> crawl = sgGeneral.add(new BoolSetting.Builder()
        .name("Crawl")
        .description("Removes crawl slowdown")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> shields = sgGeneral.add(new BoolSetting.Builder()
        .name("Shields")
        .description("Removes the slowdown effect caused by shields")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> webs = sgGeneral.add(new BoolSetting.Builder()
        .name("Webs")
        .description("Removes the slowdown caused when moving through webs")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> berryBush = sgGeneral.add(new BoolSetting.Builder()
        .name("Berry Bush")
        .description("Removes the slowdown caused when moving through webs")
        .defaultValue(false)
        .build()
    );

    private final Setting<Float> webSpeed = sgGeneral.add(new FloatSetting.Builder()
        .name("Web Multiplier")
        .description("Speed to fall through webs")
        .sliderRange(0f, 1f)
        .min(0f).defaultValue(1f).max(1f)
        .visible(() -> webs.get() || berryBush.get())
        .build()
    );

    private final Setting<Boolean> soulsand = sgGeneral.add(new BoolSetting.Builder()
        .name("Soulsand")
        .description("Removes the slowdown effect caused by walking over SoulSand blocks")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> honeyblock = sgGeneral.add(new BoolSetting.Builder()
        .name("Honey Block")
        .description("Removes the slowdown effect caused by walking over Honey blocks")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> slimeblock = sgGeneral.add(new BoolSetting.Builder()
        .name("Slime Block")
        .description("Removes the slowdown effect caused by walking over Slime blocks")
        .defaultValue(false)
        .build()
    );

    private boolean sneaking;

    @Override
    public void onDeactivate()
    {
        if (airStrict.get() && sneaking)
        {
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }
        sneaking = false;
        Managers.TICK.setClientTick(1.0f);
    }

    @EventHandler
    public void onSetCurrentHand(SetCurrentHandEvent event)
    {
        if (airStrict.get() && !sneaking && checkSlowed())
        {
            sneaking = true;
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.PRE && grim.get()
            && mc.player.isUsingItem() && !mc.player.isSneaking() && items.get())
        {

            // Grim focuses on other hand noslow checks
            if (mc.player.getActiveHand() == Hand.OFF_HAND && checkStack(mc.player.getMainHandStack()))
            {
                Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
            }
            else if (checkStack(mc.player.getOffHandStack()))
            {
                Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.OFF_HAND, id, mc.player.getYaw(), mc.player.getPitch()));
            }
        }
    }

    private boolean checkStack(ItemStack stack)
    {
        return !stack.getComponents().contains(DataComponentTypes.FOOD) && stack.getItem() != Items.BOW && stack.getItem() != Items.CROSSBOW && stack.getItem() != Items.SHIELD;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (airStrict.get() && !mc.player.isUsingItem())
        {
            sneaking = false;
            Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }
        if (inventoryMove.get() && checkScreen())
        {
            final long handle = mc.getWindow().getHandle();
            KeyBinding[] keys = new KeyBinding[]{mc.options.jumpKey, mc.options.forwardKey, mc.options.backKey, mc.options.rightKey, mc.options.leftKey};
            for (KeyBinding binding : keys)
            {
                binding.setPressed(InputUtil.isKeyPressed(handle, ((AccessorKeyBinding) binding).getBoundKey().getCode()));
            }
            if (arrowMove.get())
            {
                float yaw = mc.player.getYaw();
                float pitch = mc.player.getPitch();

                if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_UP)) pitch -= 3.0f;
                else if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_DOWN)) pitch += 3.0f;
                else if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT)) yaw -= 3.0f;
                else if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT)) yaw += 3.0f;

                mc.player.setYaw(yaw);
                mc.player.setPitch(MathHelper.clamp(pitch, -90.0f, 90.0f));
            }
        }

        if ((grim.get() || grimNew.get()) && webs.get())
        {
            Box bb = grim.get() ? mc.player.getBoundingBox().expand(1.0) : mc.player.getBoundingBox();
            for (BlockPos pos : getIntersectingWebs(bb))
            {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN));
            }
        }
    }

    /*@EventHandler
    public void onStrafeFix(StrafeFixEvent event)
    {
        if (strafeFix.get())
        {
            float yaw = Managers.ROTATION.getServerYaw();
            float pitch = Managers.ROTATION.getServerPitch();
            if (Managers.ROTATION.isRotating())
            {
                yaw = Managers.ROTATION.getRotationYaw();
                pitch = Managers.ROTATION.getRotationPitch();
            }
            event.cancel();
            event.yaw = yaw;
            event.pitch = pitch;
        }
    }*/

    @EventHandler
    public void onSlowMovement(SlowMovementEvent event)
    {
        Block block = event.state.getBlock();
        if (block instanceof CobwebBlock && webs.get()
            || block instanceof SweetBerryBushBlock && berryBush.get())
        {
            float multiplier = webSpeed.get();
            if (webSpeed.get() == 1.0f)
            {
                multiplier = 0.0f;
            }
            event.cancel();
            event.multiplier = multiplier;
        }
    }

    @EventHandler
    public void onMovementSlowdown(MovementSlowdownEvent event)
    {
        if (sneak.get() && mc.player.isSneaking() || crawl.get() && mc.player.isCrawling())
        {
            float f = 1.0f / (float) mc.player.getAttributeValue(EntityAttributes.SNEAKING_SPEED);
            event.input.movementForward *= f;
            event.input.movementSideways *= f;
        }

        if (checkSlowed())
        {
            event.input.movementForward *= 5.0f;
            event.input.movementSideways *= 5.0f;
        }
    }

    @EventHandler
    public void onSteppedOnSlimeBlock(SteppedOnSlimeBlockEvent event)
    {
        if (slimeblock.get())
        {
            event.cancel();
        }
    }

    @EventHandler
    public void onBlockSlipperiness(BlockSlipperinessEvent event)
    {
        if (event.block == Blocks.SLIME_BLOCK
            && slimeblock.get())
        {
            event.cancel();
            event.slipperiness = 0.6f;
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event)
    {
        if (mc.player == null || mc.world == null || mc.isInSingleplayer()) return;
        else if (event.packet instanceof PlayerMoveC2SPacket packet && packet.changesPosition()
            && strict.get() && checkSlowed())
        {
            Managers.INVENTORY.setSlotForced(mc.player.getInventory().selectedSlot);
        }
        else if (event.packet instanceof ClickSlotC2SPacket && strict.get())
        {
            if (mc.player.isUsingItem())
            {
                mc.player.stopUsingItem();
            }
            if (sneaking || Managers.POSITION.isSneaking())
            {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                    ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
            if (Managers.POSITION.isSprinting())
            {
                Managers.NETWORK.sendPacket(new ClientCommandC2SPacket(mc.player,
                    ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }
        }
    }

    private boolean checkGrimNew()
    {
        return !mc.player.isSneaking() && !mc.player.isCrawling() && !mc.player.isRiding() &&
            mc.player.getItemUseTimeLeft() < 5 || ((mc.player.getItemUseTime() > 1) && mc.player.getItemUseTime() % 2 != 0);
    }

    public boolean checkSlowed()
    {
        if (!grimNew.get() || checkGrimNew())
        {
            return !mc.player.isRiding() && !mc.player.isSneaking() && (mc.player.isUsingItem() && items.get()
                || mc.player.isBlocking() && shields.get() && !grimNew.get() && !grim.get());
        }
        return false;
    }

    public boolean checkScreen()
    {
        return mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen
            || mc.currentScreen instanceof SignEditScreen || mc.currentScreen instanceof DeathScreen);
    }

    public List<BlockPos> getIntersectingWebs(Box boundingBox)
    {
        final List<BlockPos> blocks = new ArrayList<>();
        for (BlockPos blockPos : GPositionUtils.getAllInBox(boundingBox))
        {
            BlockState state = mc.world.getBlockState(blockPos);
            if (state.getBlock() instanceof CobwebBlock)
            {
                blocks.add(blockPos);
            }
        }
        return blocks;
    }

    /*public boolean getStrafeFix()
    {
        return strafeFix.get();
    }*/

}
