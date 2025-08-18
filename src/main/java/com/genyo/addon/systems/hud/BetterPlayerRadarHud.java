package com.genyo.addon.systems.hud;

import com.genyo.addon.GenyoAddon;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.*;
import meteordevelopment.meteorclient.systems.modules.render.Nametags;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BetterPlayerRadarHud extends HudElement {

    public static final HudElementInfo<BetterPlayerRadarHud> INFO = new HudElementInfo<>(GenyoAddon.HUD_GROUP, "better-player-radar", "Displays players in your visual range.", BetterPlayerRadarHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFeatures = settings.createGroup("Features");
    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgBackground = settings.createGroup("Background");

    // General

    private final Setting<Integer> limit = sgGeneral.add(new IntSetting.Builder()
        .name("limit")
        .description("The max number of players to show.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 20)
        .build()
    );

    private final Setting<Boolean> friends = sgGeneral.add(new BoolSetting.Builder()
        .name("display-friends")
        .description("Whether to show friends or not.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> primaryColor = sgGeneral.add(new ColorSetting.Builder()
        .name("primary-color")
        .description("Primary color.")
        .defaultValue(new SettingColor())
        .build()
    );

    private final Setting<SettingColor> secondaryColor = sgGeneral.add(new ColorSetting.Builder()
        .name("secondary-color")
        .description("Secondary color.")
        .defaultValue(new SettingColor(175, 175, 175))
        .build()
    );

    private final Setting<Alignment> alignment = sgGeneral.add(new EnumSetting.Builder<Alignment>()
        .name("alignment")
        .description("Horizontal alignment.")
        .defaultValue(Alignment.Auto)
        .build()
    );

    private final Setting<Integer> border = sgGeneral.add(new IntSetting.Builder()
        .name("border")
        .description("How much space to add around the element.")
        .defaultValue(0)
        .build()
    );

    // Features

    private final Setting<Boolean> heads = sgFeatures.add(new BoolSetting.Builder()
        .name("Heads")
        .description("Display head icons")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> distance = sgFeatures.add(new BoolSetting.Builder()
        .name("distance")
        .description("Shows the distance to the player next to their name.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> health = sgFeatures.add(new BoolSetting.Builder()
        .name("Health")
        .description("Display health")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ping = sgFeatures.add(new BoolSetting.Builder()
        .name("Ping")
        .description("Display ping")
        .defaultValue(true)
        .build()
    );

    /*private final Setting<Boolean> coloredPing = sgFeatures.add(new BoolSetting.Builder()
        .name("Colored Ping")
        .description("Dynamic color based on ping.")
        .defaultValue(true)
        .visible(ping::get)
        .build()
    );*/

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
        .defaultValue(new SettingColor(25, 25, 25, 50))
        .build()
    );

    private final List<AbstractClientPlayerEntity> players = new ArrayList<>();
    private final Color WHITE = new Color(255, 255, 255);
    private final Color RED = new Color(255, 25, 25);
    private final Color AMBER = new Color(255, 105, 25);
    private final Color GREEN = new Color(25, 252, 25);
    private final Color GOLD = new Color(232, 185, 35);

    public BetterPlayerRadarHud() {
        super(INFO);
    }

    @Override
    public void setSize(double width, double height) {
        super.setSize(width + border.get() * 2, height + border.get() * 2);
    }

    @Override
    protected double alignX(double width, Alignment alignment) {
        return box.alignX(getWidth() - border.get() * 2, width, alignment);
    }

    @Override
    public void tick(HudRenderer renderer) {
        double width = renderer.textWidth("Players:", shadow.get(), getScale());
        double height = renderer.textHeight(shadow.get(), getScale());

        if (mc.world == null) {
            setSize(width, height);
            return;
        }

        for (PlayerEntity player : getPlayers()) {
            if (player.equals(mc.player)) continue;
            if (!friends.get() && Friends.get().isFriend(player)) continue;

            String text = player.getName().getString();
            if (heads.get()) text += "as"; // 2 letter space
            if (distance.get()) text += String.format("| %sm", Math.round(PlayerUtils.distanceToCamera(player) * 10.0) / 10.0);
            if (ping.get()) text += String.format("| %sms", EntityUtils.getPing(player));

            // Health
            float absorption = player.getAbsorptionAmount();
            int healthInt = Math.round(player.getHealth() + absorption);

            String healthText = String.format("| %s", healthInt);

            if (health.get()) text += healthText;

            width = Math.max(width, renderer.textWidth(text, shadow.get(), getScale()));
            height += renderer.textHeight(shadow.get(), getScale()) + 2;
        }

        setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        double y = this.y + border.get();

        if (background.get()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), backgroundColor.get());
        }

        renderer.text("Players:", x + border.get() + alignX(renderer.textWidth("Players:", shadow.get(), getScale()), alignment.get()), y, secondaryColor.get(), shadow.get(), getScale());

        if (mc.world == null) return;
        double spaceWidth = renderer.textWidth(" ", shadow.get(), getScale());

        for (PlayerEntity entity : getPlayers()) {
            if (entity.equals(mc.player)) continue;
            if (!friends.get() && Friends.get().isFriend(entity)) continue;

            String text = entity.getName().getString();
            Color color = PlayerUtils.getPlayerColor(entity, primaryColor.get());

            // Strings
            String distanceText = null;
            String healthText = null;
            String fullHealthText = null;
            String pingText = null;

            // Health
            Color healthColor = null;


            double width = renderer.textWidth(text, shadow.get(), getScale());


            if (distance.get()) {
                width += spaceWidth;
                distanceText = String.format("(%.1fm)", mc.getCameraEntity().distanceTo(entity));
                width += renderer.textWidth(distanceText, shadow.get(), getScale());
            }

            double x = this.x + border.get()
                + alignX(width, alignment.get());
            y += renderer.textHeight(shadow.get(), getScale()) + 2;

            if (heads.get()) {
                x += 16 + spaceWidth;

                PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
                if (entry != null) {
                    PlayerSkinDrawer.draw(renderer.drawContext, entry.getSkinTextures(), (int) this.x + border.get(), (int) y, (int) 16);
                }
            }

            if (ping.get()) {
                int ping = EntityUtils.getPing(entity);
                width += spaceWidth;
                pingText = "| " + ping + "ms";
                width += renderer.textWidth(pingText, shadow.get(), getScale());
            }

            if (health.get()) {
                // Health
                float absorption = entity.getAbsorptionAmount();
                int healthInt = Math.round(entity.getHealth() + absorption);
                double healthPercentage = healthInt / (entity.getMaxHealth() + absorption);

                if (healthPercentage <= 0.333) healthColor = RED;
                else if (healthPercentage <= 0.666) healthColor = AMBER;
                else healthColor = GREEN;

                width += spaceWidth;
                healthText = String.format("%s", healthInt);
                fullHealthText = "| " + healthText;
                width += renderer.textWidth(fullHealthText, shadow.get(), getScale());
            }

            x = renderer.text(text, x, y, color, shadow.get());
            if (distance.get()) {
                renderer.text(distanceText, x + spaceWidth, y, secondaryColor.get(), shadow.get(), getScale());
                x += spaceWidth + renderer.textWidth(distanceText, shadow.get(), getScale());
            }
            if (ping.get()) {
                //TODO: ping color
                renderer.text(pingText, x + spaceWidth, y, secondaryColor.get(), shadow.get(), getScale());
                x += spaceWidth + renderer.textWidth(pingText, shadow.get(), getScale());
            }
            if (health.get()) {
                renderer.text("|", x + spaceWidth, y, secondaryColor.get(), shadow.get(), getScale());
                x += spaceWidth;
                renderer.text(healthText, x + spaceWidth + spaceWidth, y, healthColor, shadow.get(), getScale());
                x += spaceWidth;
            }
        }
    }

    private List<AbstractClientPlayerEntity> getPlayers() {
        players.clear();
        players.addAll(mc.world.getPlayers());
        if (players.size() > limit.get()) players.subList(limit.get() - 1, players.size() - 1).clear();
        players.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.getCameraEntity())));

        return players;
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }

}
