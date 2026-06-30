package com.genyo.systems.modules.world;

import com.genyo.Genyo;
import com.genyo.managers.Managers;
import com.genyo.mixin.accessor.AccessorMinecraftClient;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.utils.math.timer.TickTimer;
import com.genyo.utils.world.SneakBlocks;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;

import java.util.List;

public class FastPlace extends GenyoModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Selection> selection = sgGeneral.add(new EnumSetting.Builder<Selection>()
        .name("selection")
        .description("The selection of items to apply fast plecements to")
        .defaultValue(Selection.WHITELIST)
        .build()
    );
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Fast place click delay")
        .min(0).defaultValue(1).max(4)
        .sliderRange(0, 4)
        .build()
    );
    private final Setting<Integer> startDelay = sgGeneral.add(new IntSetting.Builder()
        .name("start-tick-delay")
        .description("Delay on placing the blocks")
        .min(0).defaultValue(0).max(20)
        .sliderRange(0, 20)
        .build()
    );
    private final Setting<Boolean> ghostFix = sgGeneral.add(new BoolSetting.Builder()
        .name("ghost-fix")
        .description("Fixes item ghosting issue on some servers")
        .defaultValue(false)
        .build()
    );
    private final Setting<List<Item>> whitelist = sgGeneral.add(new ItemListSetting.Builder()
        .name("whitelist")
        .description("Item whitelist")
        .defaultValue(List.of(Items.EXPERIENCE_BOTTLE, Items.SNOWBALL, Items.EGG))
        .build()
    );
    private final Setting<List<Item>> blacklist = sgGeneral.add(new ItemListSetting.Builder()
        .name("blacklist")
        .description("Item blacklist")
        .defaultValue(List.of(Items.ENDER_PEARL, Items.ENDER_EYE))
        .build()
    );
    private final TickTimer startTimer = new TickTimer();

    public FastPlace() {
        super(Genyo.WORLD, "fast-place", "Place items and blocks faster");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (!mc.options.useKey.isPressed()) startTimer.reset();
        else if (startTimer.passed(startDelay.get())
            && ((AccessorMinecraftClient) mc).hookGetItemUseCooldown() > delay.get()
            && placeCheck(mc.player.getMainHandStack())) {
            if (ghostFix.get()) {
                Managers.NETWORK.sendSequencedPacket(id ->
                    new PlayerInteractItemC2SPacket(mc.player.getActiveHand(), id, mc.player.getYaw(), mc.player.getPitch()));
            }
            ((AccessorMinecraftClient) mc).hookSetItemUseCooldown(delay.get());
        }
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null) return;
        if (event.packet instanceof PlayerInteractBlockC2SPacket packet
            && ghostFix.get() && placeCheck(mc.player.getStackInHand(packet.getHand()))) {
            BlockState state = mc.world.getBlockState(packet.getBlockHitResult().getBlockPos());
            if (!SneakBlocks.isSneakBlock(state)) {
                event.cancel();
            }
        }
    }

    private boolean placeCheck(ItemStack held) {
        return switch (selection.get()) {
            case WHITELIST -> whitelist.get().contains(held.getItem());
            case BLACKLIST -> !blacklist.get().contains(held.getItem());
            case ALL -> true;
        };
    }

    public enum Selection {
        WHITELIST,
        BLACKLIST,
        ALL
    }

}
