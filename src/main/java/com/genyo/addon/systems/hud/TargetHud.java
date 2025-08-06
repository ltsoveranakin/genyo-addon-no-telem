package com.genyo.addon.systems.hud;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.managers.Managers;
import com.genyo.addon.systems.modules.combat.GenyoAutoCrystal;
import com.genyo.addon.systems.modules.combat.KFCSpawnKill;
import com.genyo.addon.systems.modules.misc.GenyoAutoEZ;
import meteordevelopment.meteorclient.gui.screens.settings.ColorSettingScreen;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.elements.InventoryHud;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.systems.hud.screens.HudElementScreen;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TargetHud extends HudElement {

    public static final HudElementInfo<TargetHud> INFO = new HudElementInfo<>(GenyoAddon.HUD_GROUP, "target-hud", "Genyo genyo genyo", TargetHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBackground = settings.createGroup("Background");
    private final SettingGroup sgScale = settings.createGroup("Scale");

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .build()
    );

    // Scale

    private final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
        .name("custom-scale")
        .description("Applies a custom scale to this hud element.")
        .defaultValue(false)
        .visible(() -> false)
        .build()
    );

    private final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Custom scale.")
        .visible(customScale::get)
        .defaultValue(1)
        .min(0.5)
        .sliderRange(0.5, 3)
        .build()
    );

    // Background

    private final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("background")
        .description("Displays background.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color used for the background.")
        .visible(background::get)
        .defaultValue(new SettingColor(25, 25, 25, 120))
        .build()
    );

    public TargetHud() {
        super(INFO);
    }

    private Target currentTarget;
    private final List<HandItem> handItems = new ArrayList<>();
    private final List<ArmorRecord> armorItems = new ArrayList<>();

    private final Color WHITE = new Color(255, 255, 255);
    private final Color RED = new Color(255, 25, 25);
    private final Color AMBER = new Color(255, 105, 25);
    private final Color GREEN = new Color(25, 252, 25);
    private final Color GOLD = new Color(232, 185, 35);

    @Override
    public void tick(HudRenderer renderer) {
        if (mc.player == null || mc.world == null) return;

        getTarget();
        if (currentTarget == null) return;

        armorItems.clear();
        handItems.clear();

        for (int i = 0; i < 6; i++) {
            ItemStack itemStack = getItem(currentTarget.target, i);

            if (itemStack != null) {
                if (itemStack.getItem() instanceof ArmorItem) {
                    String durability = String.format("%.0f%%", ((itemStack.getMaxDamage() - itemStack.getDamage()) * 100f) / (float) itemStack.getMaxDamage());
                    armorItems.add(new ArmorRecord((ArmorItem) itemStack.getItem(), durability, (int) (((itemStack.getMaxDamage() - itemStack.getDamage()) * 100f) / (float) itemStack.getMaxDamage())));
                } else {
                    if (!itemStack.getItem().equals(Items.AIR)) handItems.add(new HandItem(itemStack.getItem(), itemStack.getCount()));
                }
            }
        }

        setSize(400 * getScale(), 200 * getScale());
    }

    @Override
    public void render(HudRenderer renderer) {
        if (mc.player == null || mc.world == null) return;
        if (currentTarget == null) return;

        double spaceWidth = renderer.textWidth(" ", shadow.get(), getScale());
        double rowHeight = (renderer.textHeight() + 2) * getScale();
        double x = this.x;
        double y = this.y;

        String targetHudText = "TargetHud";
        targetHudText += String.format(" (%s)", currentTarget.source);
        renderer.text(targetHudText, x + widthScale(0.5) - (renderer.textWidth(targetHudText, shadow.get(), getScale()) / 2), y + heightScale(1) - rowHeight, Color.WHITE, shadow.get(), getScale());
        x += widthScale(0.05);
        y += heightScale(0.1);

        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(currentTarget.target.getUuid());
        if (entry != null) {
            PlayerSkinDrawer.draw(renderer.drawContext, entry.getSkinTextures(), (int) x, (int) y, 64);
        }

        x += widthScale(0.05) + 64;

        renderer.text(currentTarget.target.getName().getString(), x, y, Color.YELLOW, shadow.get(), getScale());

        y += rowHeight;
        // Health
        String healthText = null;
        Color healthColor = null;

        float absorption = currentTarget.target.getAbsorptionAmount();
        int healthInt = Math.round(currentTarget.target.getHealth() + absorption);
        double healthPercentage = healthInt / (currentTarget.target.getMaxHealth() + absorption);

        if (healthPercentage <= 0.333) healthColor = RED;
        else if (healthPercentage <= 0.666) healthColor = AMBER;
        else healthColor = GREEN;

        healthText = String.format("%s", healthInt);

        renderer.text("HP:", x, y, Color.WHITE, shadow.get(), getScale());
        renderer.text(healthText, x + renderer.textWidth("HP:", shadow.get(), getScale()) + spaceWidth, y, healthColor, shadow.get(), getScale());

        y += rowHeight;

        // ping + distance
        String pingText = null;
        String distanceText = null;
        int ping = EntityUtils.getPing(currentTarget.target);
        pingText = String.format("[%sms]", ping);
        distanceText = String.format("%.1fm", mc.getCameraEntity().distanceTo(currentTarget.target));

        renderer.text(pingText, x, y, new Color(20, 170, 170), shadow.get(), getScale());
        renderer.text("|", x + spaceWidth + renderer.textWidth(pingText, shadow.get(), getScale()), y, Color.WHITE, shadow.get(), getScale());
        renderer.text(distanceText, x + spaceWidth + renderer.textWidth(pingText + "|", shadow.get(), getScale()) + spaceWidth, y, Color.LIGHT_GRAY, shadow.get(), getScale());
        if (background.get()) renderer.quad(this.x, this.y, getWidth(), getHeight(), backgroundColor.get());

        // Items
        if (!handItems.isEmpty()) {
            renderer.post(() -> {
                double internalX = this.x + widthScale(0.70);
                double internalY = this.y + heightScale(0.1);

                for (HandItem handItem : handItems) {
                    renderer.item(handItem.item.getDefaultStack(), (int) internalX, (int) internalY, (float) getScale() * 2, true, String.valueOf(handItem.count));

                    if (handItems.size() != 1) internalX += widthScale(0.1);
                }
            });
        }

        // Armor
        if (!armorItems.isEmpty()) {
            double finalX = x;
            double finalY = y;
            renderer.post(() -> {
                double internalX = finalX - 64 - spaceWidth - widthScale(0.05);
                double internalY = finalY + rowHeight + heightScale(0.1);

                for (ArmorRecord armorRecord : armorItems) {
                    renderer.item(armorRecord.item.getDefaultStack(), (int) internalX, (int) internalY, (float) getScale() * 2, false);

                    Color durabilityColor = null;

                    if (armorRecord.duraInt >= 66) durabilityColor = GREEN;
                    else if (armorRecord.duraInt >= 33) durabilityColor = AMBER;
                    else durabilityColor = RED;

                    renderer.text(armorRecord.durability, internalX + widthScale(0.09), internalY + heightScale(0.03), durabilityColor, shadow.get(), getScale());
                    if (armorItems.indexOf(armorRecord)-1 % 2 == 0) {
                        internalY += heightScale(0.2);
                        internalX -= widthScale(0.25);
                    } else {
                        internalX += widthScale(0.25);
                    }
                }
            });
        }

        // Pop count
        if (Modules.get().get(GenyoAutoEZ.class).taggedPlayers.containsKey(currentTarget.target)
            || Managers.COMBAT.popList.containsKey(currentTarget.target.getName().getString())) {

            int pops = 0;
            x += widthScale(0.45);
            renderer.text("Pops:", x, y, Color.WHITE, shadow.get(), getScale());

            if (Modules.get().get(GenyoAutoEZ.class).taggedPlayers.containsKey(currentTarget.target)) {
                pops = Modules.get().get(GenyoAutoEZ.class).taggedPlayers.get(currentTarget.target);
            } else {
                pops = Managers.COMBAT.popList.get(currentTarget.target.getName().getString());
            }

            renderer.text(String.valueOf(pops), x + spaceWidth + renderer.textWidth("Pops:", shadow.get(), getScale()), y, Color.YELLOW, shadow.get(), getScale());
        }

    }

    private int widthScale(double scale) {
        return (int) (getWidth() * scale);
    }

    private int heightScale(double scale) {
        return (int) (getHeight() * scale);
    }

    private void getTarget() {
        if (Modules.get().get(GenyoAutoCrystal.class).targetEntity != null) {
            currentTarget = new Target(Modules.get().get(GenyoAutoCrystal.class).targetEntity, Source.AutoCrystal);
        } else if (Modules.get().get(KFCSpawnKill.class).getEntityTarget() != null && Modules.get().get(KFCSpawnKill.class).getEntityTarget() instanceof PlayerEntity) {
            currentTarget = new Target((PlayerEntity) Modules.get().get(KFCSpawnKill.class).getEntityTarget(), Source.KillAura);
        } else if (mc.currentScreen instanceof ChatScreen
            || mc.currentScreen instanceof HudEditorScreen
            || mc.currentScreen instanceof HudElementScreen
            || mc.currentScreen instanceof ColorSettingScreen) {
            currentTarget = new Target(mc.player, Source.Brasil);
        } else {
            currentTarget = null;
        }
    }

    private ItemStack getItem(PlayerEntity entity, int index) {
        if (entity == null) return null;

        return switch (index) {
            case 0 -> entity.getMainHandStack();
            case 1 -> entity.getInventory().armor.get(3);
            case 2 -> entity.getInventory().armor.get(2);
            case 3 -> entity.getInventory().armor.get(1);
            case 4 -> entity.getInventory().armor.get(0);
            case 5 -> entity.getOffHandStack();
            default -> ItemStack.EMPTY;
        };
    }

    private record Target(PlayerEntity target, Source source) {
    }

    private record HandItem(Item item, int count) {
    }

    private record ArmorRecord(ArmorItem item, String durability, int duraInt) {
    }

    private enum Source {
        AutoCrystal,
        KillAura,
        Brasil
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }
}
