package com.genyo.systems.hud;

import com.genyo.Genyo;
import com.genyo.managers.Managers;
import com.genyo.systems.modules.combat.GenyoAutoCrystal;
import com.genyo.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class GenyoTargetHud extends HudElement {

    private static final int PADDING = 6;    public static final HudElementInfo<GenyoTargetHud> INFO = new HudElementInfo<>(
        Genyo.HUD_GROUP, "target-hud", "Displays info about your crystal/killaura target.", GenyoTargetHud::new
    );
    private static final int BOTTOM_PADDING = 8;
    private static final int BAR_HEIGHT = 5;
    private static final int HAND_COLUMN_WIDTH = 28;
    private static final float HAND_ITEM_SCALE_BASE = 1.4f;
    private static final int HAND_ITEM_SIZE_BASE = (int) (16 * HAND_ITEM_SCALE_BASE);
    private static final int BORDER = 1;
    private static final int CONTENT_BASE_WIDTH = 220; // width of the info area (excluding face and hands)
    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");
    private final SettingGroup sgScale = settings.createGroup("Scale");
    // --- General ---
    private final Setting<Boolean> showFace = sgGeneral.add(new BoolSetting.Builder()
        .name("show-face")
        .description("Shows the target's player face on the left side of the HUD.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> flatFace = sgGeneral.add(new BoolSetting.Builder()
        .name("flat-face")
        .description("Renders a flat 2D face instead of the 3D player head.")
        .defaultValue(false)
        .visible(showFace::get)
        .build()
    );
    private final Setting<Boolean> showHealth = sgGeneral.add(new BoolSetting.Builder()
        .name("show-health")
        .description("Shows target health and absorption.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> showArmor = sgGeneral.add(new BoolSetting.Builder()
        .name("show-armor")
        .description("Shows target armor durability bars.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> showArmorItems = sgGeneral.add(new BoolSetting.Builder()
        .name("show-armor-items")
        .description("Renders armor item icons above bars.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> showGearPercent = sgGeneral.add(new BoolSetting.Builder()
        .name("show-gear-percent")
        .description("Shows armor durability percentage(s).")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> perPieceGear = sgGeneral.add(new BoolSetting.Builder()
        .name("per-piece-gear")
        .description("Show each piece's durability % above its icon instead of an average.")
        .defaultValue(false)
        .visible(showGearPercent::get)
        .build()
    );
    private final Setting<Boolean> showHands = sgGeneral.add(new BoolSetting.Builder()
        .name("show-hands")
        .description("Renders main hand and offhand items on the right.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> showDistance = sgGeneral.add(new BoolSetting.Builder()
        .name("show-distance")
        .description("Shows distance to target.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> showPing = sgGeneral.add(new BoolSetting.Builder()
        .name("show-ping")
        .description("Shows target ping.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> showSource = sgGeneral.add(new BoolSetting.Builder()
        .name("show-source")
        .description("Shows [AC] or [KA] source tag.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> showPops = sgGeneral.add(new BoolSetting.Builder()
        .name("show-pops")
        .description("Shows totem pop counter next to the target name.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .build()
    );
    // --- Colors ---
    private final Setting<SettingColor> bgColor = sgColors.add(new ColorSetting.Builder()
        .name("background-color")
        .defaultValue(new SettingColor(0, 0, 0, 120))
        .build()
    );
    private final Setting<SettingColor> borderColor = sgColors.add(new ColorSetting.Builder()
        .name("border-color")
        .defaultValue(new SettingColor(80, 80, 80, 180))
        .build()
    );
    private final Setting<SettingColor> nameColor = sgColors.add(new ColorSetting.Builder()
        .name("name-color")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<SettingColor> healthColorHigh = sgColors.add(new ColorSetting.Builder()
        .name("health-color-high")
        .defaultValue(new SettingColor(80, 220, 80, 255))
        .build()
    );
    private final Setting<SettingColor> healthColorLow = sgColors.add(new ColorSetting.Builder()
        .name("health-color-low")
        .defaultValue(new SettingColor(220, 60, 60, 255))
        .build()
    );
    private final Setting<SettingColor> armorColor = sgColors.add(new ColorSetting.Builder()
        .name("armor-color")
        .defaultValue(new SettingColor(100, 180, 255, 255))
        .build()
    );
    private final Setting<SettingColor> textColor = sgColors.add(new ColorSetting.Builder()
        .name("text-color")
        .defaultValue(new SettingColor(200, 200, 200, 255))
        .build()
    );
    private final Setting<SettingColor> popColor = sgColors.add(new ColorSetting.Builder()
        .name("pop-color")
        .description("Color of the pop counter text.")
        .defaultValue(new SettingColor(255, 80, 80, 255))
        .build()
    );
    private final Setting<SettingColor> gearColorHigh = sgColors.add(new ColorSetting.Builder()
        .name("gear-color-high")
        .defaultValue(new SettingColor(80, 220, 80, 255))
        .build()
    );
    private final Setting<SettingColor> gearColorLow = sgColors.add(new ColorSetting.Builder()
        .name("gear-color-low")
        .defaultValue(new SettingColor(220, 60, 60, 255))
        .build()
    );
    private final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
        .name("custom-scale")
        .defaultValue(false)
        .build()
    );
    private final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
        .name("scale")
        .defaultValue(1.0)
        .min(0.5)
        .sliderRange(0.5, 3.0)
        .visible(customScale::get)
        .build()
    );
    private final Setting<Double> iconScale = sgScale.add(new DoubleSetting.Builder()
        .name("icon-scale")
        .description("Scale multiplier for armor and hand item icons.")
        .defaultValue(1.0)
        .min(0.5)
        .sliderRange(0.5, 2.0)
        .build()
    );
    private int lastHeight = 60;

    public GenyoTargetHud() {
        super(INFO);
    }

    @Override
    public void tick(HudRenderer renderer) {
        LivingEntity target = getTarget();
        if (target == null) {
            if (isInEditor()) setSize(getPanelWidth(), getRequiredHeight(null, renderer));
            else setSize(0, 0);
            return;
        }
        lastHeight = getRequiredHeight(target, renderer);
        setSize(getPanelWidth(), lastHeight);
    }

    //  Render
    @Override
    public void render(HudRenderer renderer) {
        LivingEntity target = getTarget();

        if (target == null) {
            if (isInEditor()) renderer.text("Target HUD", x, y, textColor.get(), shadow.get(), getScale());
            return;
        }

        int pw = getPanelWidth();
        int ph = getRequiredHeight(target, renderer);
        double cx = x;
        double cy = y;

        // Background
        renderer.quad(cx, cy, pw, ph, bgColor.get());


        Color bc = borderColor.get();
        renderer.quad(cx, cy, pw, BORDER, bc); // top
        renderer.quad(cx, cy + ph - BORDER, pw, BORDER, bc); // bottom
        renderer.quad(cx, cy, BORDER, ph, bc); // left
        renderer.quad(cx + pw - BORDER, cy, BORDER, ph, bc); // right

        int faceSize = 0;
        if (showFace.get() && target instanceof PlayerEntity facePlayer) {
            faceSize = ph;
            PlayerListEntry entry = mc.getNetworkHandler() != null
                ? mc.getNetworkHandler().getPlayerListEntry(facePlayer.getUuid()) : null;
            if (entry != null) {
                if (flatFace.get()) {
                    net.minecraft.util.Identifier skinTex = entry.getSkinTextures().body().texturePath();

                    int drawX = (int) cx + BORDER;
                    int drawY = (int) cy + BORDER;
                    int size = faceSize - BORDER * 2;

                    var tex = mc.getTextureManager().getTexture(skinTex);

                    meteordevelopment.meteorclient.renderer.Renderer2D.TEXTURE.begin();
                    meteordevelopment.meteorclient.renderer.Renderer2D.TEXTURE.texQuad(
                        drawX, drawY, size, size,
                        0,                          // rotation
                        8f / 64f, 8f / 64f,             // texX1, texY1
                        16f / 64f, 16f / 64f,           // texX2, texY2
                        Color.WHITE
                    );
                    meteordevelopment.meteorclient.renderer.Renderer2D.TEXTURE.render(
                        tex.getGlTextureView(), tex.getSampler()
                    );

                    meteordevelopment.meteorclient.renderer.Renderer2D.TEXTURE.begin();
                    meteordevelopment.meteorclient.renderer.Renderer2D.TEXTURE.texQuad(
                        drawX, drawY, size, size,
                        0,
                        40f / 64f, 8f / 64f,
                        48f / 64f, 16f / 64f,
                        Color.WHITE
                    );
                    meteordevelopment.meteorclient.renderer.Renderer2D.TEXTURE.render(
                        tex.getGlTextureView(), tex.getSampler()
                    );
                } else {
                    ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
                    skull.set(DataComponentTypes.PROFILE,
                        ProfileComponent.ofStatic(facePlayer.getGameProfile()));

                    float itemScale = (faceSize - BORDER * 2) / 16f;
                    int drawX = (int) cx + BORDER;
                    int drawY = (int) cy + BORDER;
                    renderer.item(skull, drawX, drawY, itemScale, false);
                }
            }
            renderer.quad(cx + faceSize, cy, 1, ph, borderColor.get());
        }

        int accentX = (int) cx + faceSize;
        renderer.quad(accentX, cy, 2, ph, getHealthColor(target));

        int handColX = (int) (cx + pw - HAND_COLUMN_WIDTH - PADDING);
        if (showHands.get() && target instanceof PlayerEntity player) {
            renderer.quad(handColX - 1, cy + PADDING, 1, ph - PADDING * 2,
                new Color(80, 80, 80, 120));

            float handItemScale = HAND_ITEM_SCALE_BASE * (float) (double) iconScale.get();
            int handItemSize = (int) (16 * handItemScale);
            ItemStack mainHand = player.getMainHandStack();
            ItemStack offHand = player.getOffHandStack();
            int totalHandH = handItemSize * 2 + 4;
            int handStartY = (int) (cy + (ph - totalHandH) / 2.0);
            int handX = handColX + (HAND_COLUMN_WIDTH - handItemSize) / 2;

            if (!mainHand.isEmpty()) renderer.item(mainHand, handX, handStartY, handItemScale, true);
            if (!offHand.isEmpty()) renderer.item(offHand, handX, handStartY + handItemSize + 4, handItemScale, true);
        }

        int contentEndX = showHands.get() ? handColX - PADDING : (int) (cx + pw - PADDING);
        int textStartX = accentX + 2 + PADDING;
        int barW = contentEndX - textStartX;
        double curY = cy + PADDING;

        String name = target.getName().getString();
        String sourceTag = showSource.get() ? " " + getSource(target) : "";
        double curX = textStartX;
        renderer.text(name, curX, curY, nameColor.get(), shadow.get(), getScale());
        curX += renderer.textWidth(name, shadow.get(), getScale());
        if (!sourceTag.isEmpty()) {
            renderer.text(sourceTag, curX, curY, new Color(160, 160, 160, 200), shadow.get(), getScale());
            curX += renderer.textWidth(sourceTag, shadow.get(), getScale());
        }
        if (showPops.get() && target instanceof PlayerEntity popPlayer) {
            Integer pops = Managers.COMBAT.popList.get(popPlayer.getName().getString());
            if (pops != null && pops > 0) {
                String popText = " -" + pops;
                renderer.text(popText, curX, curY, popColor.get(), shadow.get(), getScale());
            }
        }
        curY += renderer.textHeight(shadow.get(), getScale()) + 2;

        if (showHealth.get()) {
            float health = target.getHealth();
            float absorption = target.getAbsorptionAmount();
            float maxHealth = target.getMaxHealth();
            float healthPct = MathHelper.clamp(health / maxHealth, 0f, 1f);

            renderer.quad(textStartX, curY, barW, BAR_HEIGHT, new Color(40, 40, 40, 180));
            renderer.quad(textStartX, curY, (int) (barW * healthPct), BAR_HEIGHT, getHealthColor(target));

            if (absorption > 0) {
                float absPct = MathHelper.clamp(absorption / maxHealth, 0f, 1f);
                int absW = (int) (barW * absPct);
                int fillW = (int) (barW * healthPct);
                int absX = Math.max(textStartX, textStartX + fillW - absW);
                renderer.quad(absX, curY, absW, BAR_HEIGHT, new Color(255, 220, 80, 120));
            }
            curY += BAR_HEIGHT + 2;

            String healthText = String.format("%.1f / %.1f", health, maxHealth);
            if (absorption > 0) healthText += String.format(" (+%.1f)", absorption);
            renderer.text(healthText, textStartX, curY, textColor.get(), shadow.get(), getScale());
            curY += renderer.textHeight(shadow.get(), getScale()) + 2;
        }

        if (target instanceof PlayerEntity player) {
            List<float[]> durs = getArmorDurabilities(player);
            int slotCount = 4;
            int gap = 2;
            int slotW = (barW - gap * (slotCount - 1)) / slotCount;

            float itemScale = Math.min((float) slotW / 16f, 1.5f) * (float) (double) iconScale.get();
            int itemRenderSize = (int) (16 * itemScale);

            if (showArmorItems.get()) {
                int itemOffsetY = (int) curY;
                for (int i = 0; i < slotCount; i++) {
                    ItemStack stack = player.getEquippedStack(ARMOR_SLOTS[i]);
                    if (!stack.isEmpty()) {
                        int slotX = textStartX + i * (slotW + gap);
                        int itemX = slotX + (slotW - itemRenderSize) / 2;
                        renderer.item(stack, itemX, itemOffsetY, itemScale, true);
                    }
                }
                curY += itemRenderSize + 3;
            }

            if (showArmor.get()) {
                for (int i = 0; i < slotCount; i++) {
                    float dur = durs.get(i)[0];
                    boolean has = durs.get(i)[1] > 0;
                    int slotX = textStartX + i * (slotW + gap);

                    renderer.quad(slotX, curY, slotW, BAR_HEIGHT, new Color(40, 40, 40, 180));
                    if (has) {
                        Color fill = dur < 0.2f ? new Color(220, 60, 60, 255) : armorColor.get();
                        renderer.quad(slotX, curY, (int) (slotW * dur), BAR_HEIGHT, fill);
                    }
                }
                curY += BAR_HEIGHT + 2;
            }

            if (showGearPercent.get()) {
                if (perPieceGear.get()) {
                    for (int i = 0; i < slotCount; i++) {
                        float dur = durs.get(i)[0];
                        boolean has = durs.get(i)[1] > 0;
                        if (!has) continue;
                        String label = String.format("%.0f%%", dur * 100f);
                        Color col = interpolateColor(gearColorLow.get(), gearColorHigh.get(), dur);
                        double lw = renderer.textWidth(label, shadow.get(), getScale());
                        double slotCentX = textStartX + i * (slotW + gap) + slotW / 2.0;
                        renderer.text(label, slotCentX - lw / 2.0, curY, col, shadow.get(), getScale());
                    }
                } else {
                    float gearPct = getGearPercent(player);
                    String gearText = String.format("Gear: %.0f%%", gearPct * 100f);
                    Color gearCol = interpolateColor(gearColorLow.get(), gearColorHigh.get(), gearPct);
                    renderer.text(gearText, textStartX, curY, gearCol, shadow.get(), getScale());
                }
                curY += renderer.textHeight(shadow.get(), getScale()) + 2;
            }
        }

        boolean hasDistance = showDistance.get();
        boolean hasPing = showPing.get() && target instanceof PlayerEntity;
        if (hasDistance || hasPing) {
            renderer.quad(textStartX, curY, barW, 1, new Color(80, 80, 80, 140));
            curY += 4;

            String distText = hasDistance ? String.format("%.1fm", mc.player.distanceTo(target)) : null;
            String pingText = null;
            if (hasPing) {
                int ping = getPing((PlayerEntity) target);
                if (ping >= 0) pingText = ping + "ms";
            }

            if (distText != null && pingText != null) {
                double distW = renderer.textWidth(distText, shadow.get(), getScale());
                double pingW = renderer.textWidth(pingText, shadow.get(), getScale());
                double textH = renderer.textHeight(shadow.get(), getScale());
                double totalW = distW + 9 + pingW;
                double startX = textStartX + (barW - totalW) / 2.0;

                renderer.text(distText, startX, curY, textColor.get(), shadow.get(), getScale());
                double divX = startX + distW + 4;
                renderer.quad(divX, curY - 1, 1, (int) textH + 2, new Color(100, 100, 100, 160));
                renderer.text(pingText, divX + 5, curY, textColor.get(), shadow.get(), getScale());
            } else {
                String single = distText != null ? distText : pingText;
                if (single != null)
                    renderer.text(single, textStartX, curY, textColor.get(), shadow.get(), getScale());
            }
        }
    }

    private LivingEntity getTarget() {
        GenyoAutoCrystal crystal = Modules.get().get(GenyoAutoCrystal.class);
        if (Modules.get().isActive(GenyoAutoCrystal.class) && crystal.targetEntity != null)
            return crystal.targetEntity;
        KillAura kfc = Modules.get().get(KillAura.class);
        if (Modules.get().isActive(KillAura.class)) {
            Entity e = kfc.getEntityTarget();
            if (e instanceof LivingEntity living) return living;
        }
        return null;
    }

    private String getSource(LivingEntity target) {
        GenyoAutoCrystal crystal = Modules.get().get(GenyoAutoCrystal.class);
        if (Modules.get().isActive(GenyoAutoCrystal.class) && crystal.targetEntity == target) return "[AC]";
        return "[KA]";
    }

    private Color getHealthColor(LivingEntity target) {
        float t = MathHelper.clamp(target.getHealth() / target.getMaxHealth(), 0f, 1f);
        return interpolateColor(healthColorLow.get(), healthColorHigh.get(), t);
    }

    private Color interpolateColor(SettingColor lo, SettingColor hi, float t) {
        return new Color(
            (int) (lo.r + (hi.r - lo.r) * t),
            (int) (lo.g + (hi.g - lo.g) * t),
            (int) (lo.b + (hi.b - lo.b) * t),
            255);
    }

    private List<float[]> getArmorDurabilities(PlayerEntity player) {
        List<float[]> result = new ArrayList<>();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.isEmpty() || stack.getMaxDamage() == 0) {
                result.add(new float[]{0f, 0f});
            } else {
                result.add(new float[]{
                    1f - (stack.getDamage() / (float) stack.getMaxDamage()),
                    stack.getMaxDamage()
                });
            }
        }
        return result;
    }

    private float getGearPercent(PlayerEntity player) {
        float total = 0f;
        int count = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getEquippedStack(slot);
            if (!stack.isEmpty() && stack.getMaxDamage() > 0) {
                total += 1f - (stack.getDamage() / (float) stack.getMaxDamage());
                count++;
            }
        }
        return count == 0 ? 1f : total / count;
    }

    private int getPing(PlayerEntity player) {
        PlayerListEntry entry = mc.getNetworkHandler() != null
            ? mc.getNetworkHandler().getPlayerListEntry(player.getUuid()) : null;
        return entry != null ? entry.getLatency() : -1;
    }

    private int getPanelWidth() {
        int handExtra = showHands.get() ? HAND_COLUMN_WIDTH + PADDING : 0;
        int faceExtra = showFace.get() ? lastHeight : 0;
        return CONTENT_BASE_WIDTH + handExtra + faceExtra;
    }

    private int getRequiredHeight(LivingEntity target, HudRenderer renderer) {
        boolean isPlayer = target == null || target instanceof PlayerEntity;

        double textH = renderer.textHeight(shadow.get(), getScale());

        double h = PADDING + textH + 2; // top padding + name row

        if (showHealth.get()) h += BAR_HEIGHT + 2 + textH + 2;

        if (isPlayer) {
            float itemScaleEst = Math.min(((CONTENT_BASE_WIDTH - PADDING * 2 - 2 - 3 * 2) / 4) / 16f, 1.5f)
                * (float) (double) iconScale.get();
            int itemSizeEst = (int) (16 * itemScaleEst);

            if (showArmorItems.get()) h += itemSizeEst + 3;
            if (showArmor.get()) h += BAR_HEIGHT + 2;
            if (showGearPercent.get()) h += textH + 2;
        }

        if (showDistance.get() || showPing.get())
            h += 1 + 4 + textH;

        h += BOTTOM_PADDING;

        if (showHands.get()) h = Math.max(h, HAND_ITEM_SIZE_BASE * 2 + 4 + PADDING * 2);

        return (int) Math.ceil(h);
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }


}
