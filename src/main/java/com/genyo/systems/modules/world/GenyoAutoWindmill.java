package com.genyo.systems.modules.world;
import com.genyo.Genyo;
import com.genyo.systems.modules.GenyoModule;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GenyoAutoWindmill extends GenyoModule {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private int genyoTicks = 0;

    //cba to port simpletimer
    private final Setting<Integer> delaySetting = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("windmill placement delay in ticks")
        .defaultValue(1)
        .range(0, 10)
        .build()
    );

    public GenyoAutoWindmill() {
        super(Genyo.WORLD, "genyo-auto-windmill", "Slam Genyo's windmill");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        genyoTicks++;

        int blockSlot = findBlockInHotbar();
        if (blockSlot == -1) return;

        mc.player.getInventory().setSelectedSlot(blockSlot);
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(blockSlot));

        if (genyoTicks >= delaySetting.get()) {
            List<BlockPos> windmill = getwindmill();
            for (int i = 0; i < windmill.size(); i++) {
                BlockPos pos = windmill.get(i);
                BlockState state = mc.world.getBlockState(pos);
                if (state.isReplaceable() || !mc.world.getFluidState(pos).isEmpty()) {
                    FindItemResult itemResult = new FindItemResult(blockSlot, 1);
                    BlockUtils.place(pos, itemResult, false, 0);
                    genyoTicks = 0;
                    if (i == windmill.size() - 1) toggle(); // toggle after last genyoblok
                    return;
                }
            }
        }
    }


    private int findBlockInHotbar() {
        for (int i = 36; i <= 44; i++) {
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (stack.getItem() instanceof BlockItem)
                return this.toHotbar(i);
        }
        return -1;
    }

    private int toHotbar(int slot) {
        return slot - 36;
    }

    private List<BlockPos> getwindmill() {
        List<BlockPos> northWindmill = new ArrayList<>();
        BlockPos genyoWindmillPos = new BlockPos(0, 0, 0).north().north();

        northWindmill.add(genyoWindmillPos);
        northWindmill.add(genyoWindmillPos.west());
        northWindmill.add(genyoWindmillPos.west().west());
        northWindmill.add(genyoWindmillPos.up());
        northWindmill.add(genyoWindmillPos.up().up());
        northWindmill.add(genyoWindmillPos.up().up().west());
        northWindmill.add(genyoWindmillPos.up().up().west().west());
        northWindmill.add(genyoWindmillPos.up().up().west().west().up());
        northWindmill.add(genyoWindmillPos.up().up().west().west().up().up());
        northWindmill.add(genyoWindmillPos.up().up().east());
        northWindmill.add(genyoWindmillPos.up().up().east().east());
        northWindmill.add(genyoWindmillPos.up().up().east().east().down());
        northWindmill.add(genyoWindmillPos.up().up().east().east().down().down());
        northWindmill.add(genyoWindmillPos.up().up().up());
        northWindmill.add(genyoWindmillPos.up().up().up().up());
        northWindmill.add(genyoWindmillPos.up().up().up().up().east());
        northWindmill.add(genyoWindmillPos.up().up().up().up().east().east());
        return getPositionsNextToPlayer(rotateFromNorth(northWindmill));
    }

    private List<BlockPos> getPositionsNextToPlayer(List<BlockPos> shapePositions) {
        return shapePositions.stream()
            .map(pos -> pos.add(mc.player.getBlockPos().getX(), mc.player.getBlockPos().getY(), mc.player.getBlockPos().getZ()))
            .collect(Collectors.toList());
    }

    private List<BlockPos> rotateFromNorth(List<BlockPos> northPos) {
        return switch (mc.player.getHorizontalFacing()) {
            default -> northPos;
            case EAST -> northPos.stream().map(pos -> pos.rotate(BlockRotation.CLOCKWISE_90)).collect(Collectors.toList());
            case SOUTH -> northPos.stream().map(pos -> pos.rotate(BlockRotation.CLOCKWISE_180)).collect(Collectors.toList());
            case WEST -> northPos.stream().map(pos -> pos.rotate(BlockRotation.COUNTERCLOCKWISE_90)).collect(Collectors.toList());
        };
    }
}
