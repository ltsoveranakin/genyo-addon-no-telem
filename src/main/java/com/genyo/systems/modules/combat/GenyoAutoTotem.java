package com.genyo.systems.modules.combat;

import com.genyo.GenyoAddon;
import com.genyo.events.world.LoadWorldEvent;
import com.genyo.managers.Managers;
import com.genyo.systems.modules.GenyoModule;
import com.genyo.systems.settings.FloatSetting;
import com.genyo.utils.math.timer.CacheTimer;
import com.genyo.utils.math.timer.Timer;
import com.genyo.utils.player.InventoryUtil;
import com.genyo.utils.player.PlayerUtil;
import com.genyo.utils.world.ExplosionUtil;
import com.genyo.utils.world.SneakBlocks;
import com.google.common.collect.Lists;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GenyoAutoTotem extends GenyoModule {

    public GenyoAutoTotem() {
        super(GenyoAddon.COMBAT, "genyo-auto-totem", "Automatically replenishes the totem in your offhand");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<OffhandItem> itemConfig = sgGeneral.add(new EnumSetting.Builder<OffhandItem>()
        .name("Item")
        .description("The item to wield in your offhand")
        .defaultValue(OffhandItem.TOTEM)
        .build()
    );

    private final Setting<Float> healthConfig = sgGeneral.add(new FloatSetting.Builder()
        .name("Health")
        .description("The health required to fall below before swapping to a totem")
        .min(0.0f)
        .defaultValue(14.0f)
        .max(20.0f)
        .build()
    );

    private final Setting<Boolean> gappleConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Offhand Gapple")
        .description("Equips a golden apple if holding down the item use button")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> crappleConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Crapple")
        .description("Uses a normal golden apple if Absorption is present")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> lethalConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Lethal")
        .description("Calculates lethal damage sources")
        .defaultValue(false)
        .visible(() -> itemConfig.get() != OffhandItem.TOTEM)
        .build()
    );

    private final Setting<Boolean> fastConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Fast Swap")
        .description("Swaps items to offhand")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> mainhandTotemConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Mainhand Totem")
        .description("Swaps to a totem in your mainhand")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> totemSlotConfig = sgGeneral.add(new IntSetting.Builder()
        .name("Totem Slot")
        .description("Slot to use for mainhand totem")
        .min(1)
        .defaultValue(1)
        .max(9)
        .visible(mainhandTotemConfig::get)
        .onChanged(this::resetMainhandSwap)
        .build()
    );

    private final Setting<Boolean> alternativeConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Alternative")
        .description("Replaces totem using the swap packet")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> debugConfig = sgGeneral.add(new BoolSetting.Builder()
        .name("Debug")
        .description("Debug on death")
        .defaultValue(false)
        .build()
    );

    private int lastHotbarSlot, lastTotemCount;
    private Item lastHotbarItem;
    private Item offhandItem;
    private boolean replacing;
    private long replaceTime;

    private final Timer mainhandSwapTimer = new CacheTimer();
    private boolean totemInMainhand;

    @Override
    public void onDeactivate()
    {
        lastHotbarSlot = -1;
        lastHotbarItem = null;
        offhandItem = null;
        totemInMainhand = false;
    }

    @EventHandler
    public void onLoadWorld(LoadWorldEvent event) {
        lastTotemCount = InventoryUtil.count(Items.TOTEM_OF_UNDYING);
    }

    @EventHandler(priority = Integer.MAX_VALUE - 1)
    public void onTick(TickEvent.Pre event)
    {
        if (mc.player == null) return;

        if (mainhandTotemConfig.get() && mainhandSwapTimer.passed(200))
        {
            int totemSlot1 = totemSlotConfig.get() - 1;
            ItemStack totemSlotStack = mc.player.getInventory().getStack(totemSlot1);
            totemSlot1 += 36;
            if (totemSlotStack.getItem() != Items.TOTEM_OF_UNDYING)
            {
                int n = 35;
                while (n >= 0)
                {
                    if (mc.player.getInventory().getStack(n).getItem() == Items.TOTEM_OF_UNDYING)
                    {
                        int slot = n < 9 ? n + 36 : n;
                        replacing = true;
                        if (alternativeConfig.get())
                        {
                            mc.interactionManager.clickSlot(0, slot, totemSlot1, SlotActionType.SWAP, mc.player);
                            replacing = false;
                        }
                        else
                        {
                            if (mc.player.currentScreenHandler.getCursorStack().getItem() != Items.TOTEM_OF_UNDYING)
                            {
                                mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                            }
                            if (mc.player.currentScreenHandler.getCursorStack().getItem() == Items.TOTEM_OF_UNDYING)
                            {
                                mc.interactionManager.clickSlot(0, totemSlot1, 0, SlotActionType.PICKUP, mc.player);
                                lastTotemCount = InventoryUtil.count(Items.TOTEM_OF_UNDYING) - 1;
                            }
                            replacing = false;
                            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty() && mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING)
                            {
                                mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                                return;
                            }
                        }
                    }
                    n--;
                }
            }

            totemInMainhand = checkMainhandTotem();
            if (totemInMainhand)
            {
                int totemSlot = -1;
                for (int i = 0; i < 9; i++)
                {
                    ItemStack stack = mc.player.getInventory().getStack(i);
                    if (stack.getItem() == Items.TOTEM_OF_UNDYING)
                    {
                        totemSlot = i;
                        break;
                    }
                }
                if (totemSlot != -1)
                {
                    Managers.INVENTORY.setClientSlot(totemSlot);
                }
            }
        }
        else
        {
            totemInMainhand = false;
        }

        offhandItem = itemConfig.get().getItem();
        if (checkLethal())
        {
            offhandItem = Items.TOTEM_OF_UNDYING;
        }
        else
        {
            // If offhand gap is enabled & the use key is pressed down, equip a golden apple.
            final Item mainHandItem = mc.player.getMainHandStack().getItem();
            if (gappleConfig.get() && mc.options.useKey.isPressed()
                && (mainHandItem instanceof SwordItem
                || mainHandItem instanceof TridentItem
                || mainHandItem instanceof AxeItem)
                && PlayerUtil.getLocalPlayerHealth() >= healthConfig.get())
            {
                if (mc.crosshairTarget instanceof BlockHitResult result)
                {
                    BlockState interactBlock = mc.world.getBlockState(result.getBlockPos());
                    if (!SneakBlocks.isSneakBlock(interactBlock))
                    {
                        offhandItem = getGoldenAppleType();
                    }
                }
                else
                {
                    offhandItem = getGoldenAppleType();
                }
            }
        }

        if (mc.player.getOffHandStack().getItem() == offhandItem)
        {
            return;
        }
        int n = 35;
        if (lastHotbarSlot != -1 && lastHotbarItem != null)
        {
            final ItemStack stack = mc.player.getInventory().getStack(lastHotbarSlot);
            if (stack.getItem().equals(offhandItem) && lastHotbarItem.equals(mc.player.getOffHandStack().getItem()))
            {
                final int tmp = lastHotbarSlot;
                lastHotbarSlot = -1;
                lastHotbarItem = null;
                n = tmp;
            }
        }
        while (n >= 0)
        {
            if (mc.player.getInventory().getStack(n).getItem() == offhandItem)
            {
                if (n < 9)
                {
                    lastHotbarItem = offhandItem;
                    lastHotbarSlot = n;
                }
                int slot = n < 9 ? n + 36 : n;
                replacing = true;
                if (alternativeConfig.get())
                {
                    mc.interactionManager.clickSlot(0, slot, 40, SlotActionType.SWAP, mc.player);
                    replacing = false;
                }
                else
                {
                    if (mc.player.currentScreenHandler.getCursorStack().getItem() != offhandItem)
                    {
                        mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                    }
                    if (mc.player.currentScreenHandler.getCursorStack().getItem() == offhandItem)
                    {
                        mc.interactionManager.clickSlot(0, 45, 0, SlotActionType.PICKUP, mc.player);
                        lastTotemCount = InventoryUtil.count(Items.TOTEM_OF_UNDYING) - 1;
                    }
                    replacing = false;
                    if (!mc.player.currentScreenHandler.getCursorStack().isEmpty() && mc.player.getOffHandStack().getItem() == offhandItem)
                    {
                        mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.PICKUP, mc.player);
                        return;
                    }
                }
            }
            n--;
        }
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) return;

        if (event.packet instanceof HealthUpdateS2CPacket packet
            && packet.getHealth() <= 0.0f && debugConfig.get())
        {
            if (lastTotemCount <= 0) return;

            final Set<String> failureReasonsSet = getFailureReasons();
            if (failureReasonsSet.isEmpty())
            {
                long serverLatency = System.currentTimeMillis() - replaceTime;
                sendError("Failed to replace totem in " + serverLatency + "ms!");
            }
            else
            {
                sendError("Failed to replace totem! Possible reasons: " + String.join(", ", failureReasonsSet));
            }
        }
        // Server should only send this when we pop a totem
        if (event.packet instanceof ScreenHandlerSlotUpdateS2CPacket packet
            && packet.getSlot() == 45 && offhandItem == Items.TOTEM_OF_UNDYING)
        {
            if (mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING || !packet.getStack().isEmpty())
            {
                return;
            }
            replaceTime = System.currentTimeMillis();
        }
    }

    private Set<String> getFailureReasons()
    {
        final Set<String> failureReasonsSet = new LinkedHashSet<>();
        if (mc.player.currentScreenHandler.syncId != 0)
        {
            failureReasonsSet.add("Current screen handler is not the player inventory");
        }
        if (!mc.player.currentScreenHandler.getCursorStack().isEmpty())
        {
            failureReasonsSet.add("Totem was not placed in offhand on time");
        }
        return failureReasonsSet;
    }

    private void resetMainhandSwap(Integer integer) {
        mainhandSwapTimer.reset();
    }

    private boolean checkLethal()
    {
        // If the player's health (+absorption) falls below the "safe" amount, equip a totem
        final float health = PlayerUtil.getLocalPlayerHealth();
        return health <= healthConfig.get() || lethalConfig.get() && checkLethalCrystal(health) ||
            PlayerUtil.computeFallDamage(mc.player.fallDistance, 1.0f) + 0.5f > mc.player.getHealth();
    }

    private boolean checkLethalCrystal(float health)
    {
        final List<Entity> entities = Lists.newArrayList(mc.world.getEntities());
        for (Entity e : entities)
        {
            if (e == null || !e.isAlive() || !(e instanceof EndCrystalEntity crystal))
            {
                continue;
            }
            if (mc.player.squaredDistanceTo(e) > 144.0)
            {
                continue;
            }
            double potential = ExplosionUtil.getDamageTo(mc.player, crystal.getPos(), false);
            if (health + 0.5 > potential)
            {
                continue;
            }
            return true;
        }

        return false;
    }

    private Item getGoldenAppleType()
    {
        if (crappleConfig.get() && InventoryUtil.hasItemInInventory(Items.GOLDEN_APPLE, true)
            && (mc.player.hasStatusEffect(StatusEffects.ABSORPTION)
            || !InventoryUtil.hasItemInInventory(Items.ENCHANTED_GOLDEN_APPLE, true)))
        {
            return Items.GOLDEN_APPLE;
        }
        return Items.ENCHANTED_GOLDEN_APPLE;
    }

    private boolean checkMainhandTotem()
    {
        if (mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING)
        {
            return false;
        }
        return checkLethalCrystal(PlayerUtil.getLocalPlayerHealth());
    }

    public boolean isTotemInMainhand()
    {
        return totemInMainhand;
    }

    public boolean isReplacing()
    {
        return replacing;
    }

    private enum OffhandItem
    {
        TOTEM(Items.TOTEM_OF_UNDYING),
        GAPPLE(Items.ENCHANTED_GOLDEN_APPLE),
        CRYSTAL(Items.END_CRYSTAL);

        private final Item item;

        OffhandItem(Item item)
        {
            this.item = item;
        }

        public Item getItem()
        {
            return item;
        }
    }
}
