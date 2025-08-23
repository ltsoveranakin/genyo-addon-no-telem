package com.genyo.systems.modules.world;

import com.genyo.Genyo;
import com.genyo.managers.Managers;
import com.genyo.systems.modules.GenyoModule;
import meteordevelopment.meteorclient.events.entity.player.BreakBlockEvent;
import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class GenyoGhostBlocks extends GenyoModule {

    public GenyoGhostBlocks() {
        super(Genyo.WORLD, "genyo-ghost-blocks", "maybe");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("Method to do the magic.")
        .defaultValue(Mode.Shoreline)
        .build()
    );

    //---------Shoreline----------//

    private final Setting<Boolean> place = sgGeneral.add(new BoolSetting.Builder()
        .name("Place")
        .description("Places blocks only after the server confirms")
        .defaultValue(false)
        .visible(() -> mode.get() == Mode.Shoreline)
        .build()
    );

    private final Setting<Boolean> destroy = sgGeneral.add(new BoolSetting.Builder()
        .name("Destroy")
        .description("Destroys blocks only after the server confirms")
        .defaultValue(false)
        .visible(() -> mode.get() == Mode.Shoreline)
        .build()
    );

    //---------AntiGhost----------//

    private final Setting<Integer> radius = sgGeneral.add(new IntSetting.Builder()
        .name("Radius")
        .description("Radius to check for ghost blocks.")
        .min(6)
        .defaultValue(6)
        .max(20)
        .visible(() -> mode.get() == Mode.AntiGhost)
        .build()
    );

    private final Setting<Keybind> toggleKey = sgGeneral.add(new KeybindSetting.Builder()
        .name("Toggle Keybind")
        .description("asd")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_Y))
        .action(this::antiGhost)
        .visible(() -> mode.get() == Mode.AntiGhost)
        .build()
    );

    private void antiGhost() {
        if (mode.get() != Mode.Shoreline) return;

        BlockPos pos = mc.player.getBlockPos();
        for (int dx = -radius.get(); dx <= radius.get(); ++dx) {
            for (int dy = -radius.get(); dy <= radius.get(); ++dy) {
                for (int dz = -radius.get(); dz <= radius.get(); ++dz) {
                    PlayerActionC2SPacket packet = new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                        new BlockPos(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz),
                        Direction.UP);

                    Managers.NETWORK.sendPacket(packet);
                }
            }
        }
    }

    @EventHandler
    private void onInteractBlock(InteractBlockEvent event) {
        if (mode.get() != Mode.Shoreline) return;

        if (!place.get()) return;
        if (!(mc.player.getStackInHand(event.hand).getItem() instanceof BlockItem)) return;
        if (mc.isInSingleplayer()) return;

        event.cancel();
        Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(event.hand, event.result, id));
        mc.player.swingHand(event.hand); // TODO: send as packet?
    }

    @EventHandler
    private void onBreakBlock(BreakBlockEvent event) {
        if (mode.get() != Mode.Shoreline) return;

        if (!destroy.get()) return;
        if (mc.isInSingleplayer()) return;

        event.cancel();
        BlockState state = mc.world.getBlockState(event.blockPos);
        state.getBlock().onBreak(mc.world, event.blockPos, state, mc.player);
    }

    private enum Mode {
        Shoreline,
        AntiGhost
    }

}
