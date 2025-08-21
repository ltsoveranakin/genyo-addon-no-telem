package com.genyo.systems.modules.world;
import com.genyo.GenyoAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
public class GenyoAutoPenis extends Module{

    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private int genyoTicks = 0;

    private enum BuildState {
        genyoPlace, genyoBreak, genyoGood
    }

    @Override
    public void onActivate() {
        genyoState = BuildState.genyoPlace; // Reset state to start placing again
        genyoTicks = 0;                   // Reset tick counter
    }

    @Override
    public void onDeactivate() {
        genyoState = BuildState.genyoPlace; // Also reset on deactivation, in case needed
        genyoTicks = 0;
    }

    private BuildState genyoState = BuildState.genyoPlace;
    private boolean genyoPenisBuilt = false;

    //cba to port simpletimer
    private final Setting<Integer> delaySetting = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("penis placement delay in ticks")
        .defaultValue(1)
        .range(0, 10)
        .build()
    );

    public GenyoAutoPenis() {
        super(GenyoAddon.WORLD, "genyo-auto-penis", "show genyo's bbc");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        genyoTicks++;

        switch (genyoState) {
            case genyoPlace -> {
                int blockSlot = findBlockInHotbar();
                if (blockSlot == -1) return;

                mc.player.getInventory().setSelectedSlot(blockSlot);
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(blockSlot));

                if (genyoTicks >= delaySetting.get()) {
                    boolean allPlaced = true;

                    for (BlockPos pos : getPenis()) {
                        BlockState state = mc.world.getBlockState(pos);
                        if (state.isReplaceable() || !mc.world.getFluidState(pos).isEmpty()) {
                            FindItemResult itemResult = new FindItemResult(blockSlot, 1);
                            BlockUtils.place(pos, itemResult, false, 0);
                            genyoTicks = 0;
                            allPlaced = false;
                            break;
                        }
                    }

                    if (allPlaced) {
                        genyoState = BuildState.genyoBreak;
                        genyoTicks = 0;
                    }
                }
            }

            case genyoBreak -> {
                int pickSlot = findPickaxeInHotbar();
                if (pickSlot != -1) {
                    mc.player.getInventory().setSelectedSlot(pickSlot);
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(pickSlot));
                }
                List<BlockPos> centerPosList = List.of(new BlockPos(0, 0, -2));
                BlockPos center = rotateFromNorth(centerPosList).getFirst().add(mc.player.getBlockPos());
                BlockState state = mc.world.getBlockState(center);
                if (!state.isAir()) {
                    BlockUtils.breakBlock(center, false);
                } else {
                    genyoState = BuildState.genyoGood;
                }
            }

            case genyoGood -> {
                toggle();
                // Do nothing, structure is built and center block is broken
            }
        }
    }
    private int findPickaxeInHotbar() {
        for (int i = 36; i <= 44; i++) {
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (stack.getItem().getTranslationKey().contains("pickaxe")) {
                return toHotbar(i); // Convert inventory slot to hotbar index
            }
        }
        return -1; // No pickaxe found
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

    private List<BlockPos> getPenis() {
        List<BlockPos> northPenis = new ArrayList<>();
        BlockPos genyoPenisPos = new BlockPos(0, 0, 0).north().north();

        northPenis.add(genyoPenisPos);
        northPenis.add(genyoPenisPos.west());
        northPenis.add(genyoPenisPos.east());
        northPenis.add(genyoPenisPos.up());
        northPenis.add(genyoPenisPos.up().up());
        northPenis.add(genyoPenisPos.up().up().up());

        return getPositionsNextToPlayer(rotateFromNorth(northPenis));
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
