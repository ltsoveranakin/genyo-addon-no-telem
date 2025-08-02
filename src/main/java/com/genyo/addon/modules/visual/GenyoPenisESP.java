package com.genyo.addon.modules.visual;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.modules.GenyoModule;
import com.genyo.addon.render.Render3DEngine;
import com.genyo.addon.settings.FloatSetting;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;

import java.util.ArrayList;
import java.util.List;

public class GenyoPenisESP extends GenyoModule {

    public GenyoPenisESP() {
        super(GenyoAddon.GENYO, "Genyo PenisESP", "faszfasz fasz fasz fasz fasz fsaz fasz");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> onlyOwn = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Own")
        .description("ya")
        .defaultValue(false)
        .build()
    );

    private final Setting<Float> ballSize = sgGeneral.add(new FloatSetting.Builder()
        .name("Ball Size")
        .description("ya")
        .min(0.1f)
        .defaultValue(1.5f)
        .max(0.5f)
        .build()
    );

    private final Setting<Float> penisSize = sgGeneral.add(new FloatSetting.Builder()
        .name("Penis Size")
        .description("ya")
        .min(0.1f)
        .defaultValue(1.5f)
        .max(3.0f)
        .build()
    );

    private final Setting<Float> friendSize = sgGeneral.add(new FloatSetting.Builder()
        .name("Friend Size")
        .description("fren")
        .min(0.1f)
        .defaultValue(1.5f)
        .max(3.0f)
        .build()
    );

    private final Setting<Float> enemySize = sgGeneral.add(new FloatSetting.Builder()
        .name("Enemy Size")
        .description("emeny >:(")
        .min(0.1f)
        .defaultValue(0.5f)
        .max(3.0f)
        .build()
    );

    private final Setting<Integer> gradation = sgGeneral.add(new IntSetting.Builder()
        .name("Gradation")
        .description("welcome to graduation, good morning.")
        .min(20)
        .defaultValue(30)
        .max(100)
        .build()
    );

    private final Setting<SettingColor> penisColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Penis Color")
        .description("wtf")
        .defaultValue(new Color(231, 180, 122, 255))
        .build()
    );

    private final Setting<SettingColor> headColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Head Color")
        .description("wtf 2.0")
        .defaultValue(new Color(240, 50, 180, 255))
        .build()
    );

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (onlyOwn.get() && player != mc.player) continue;
            double size = (Friends.get().isFriend(player) ? friendSize.get() : (player != mc.player ? enemySize.get() : penisSize.get()));

            Vec3d base = getBase(player, event.tickDelta);
            Vec3d forward = base.add(0, player.getHeight() / 2.4, 0).add(Vec3d.fromPolar(0, player.getYaw()).multiply(0.1));

            Vec3d left = forward.add(Vec3d.fromPolar(0, player.getYaw() - 90).multiply(ballSize.get()));
            Vec3d right = forward.add(Vec3d.fromPolar(0, player.getYaw() + 90).multiply(ballSize.get()));

            drawBall(player, ballSize.get(), gradation.get(), left, penisColor.get(), 0, event.tickDelta);
            drawBall(player, ballSize.get(), gradation.get(), right, penisColor.get(), 0, event.tickDelta);
            drawPenis(player, event.matrices, size, forward, event.tickDelta);
        }
    }

    public Vec3d getBase(Entity entity, float tickDelta) {
        double x = entity.prevX + ((entity.getX() - entity.prevX) * tickDelta);
        double y = entity.prevY + ((entity.getY() - entity.prevY) * tickDelta);
        double z = entity.prevZ + ((entity.getZ() - entity.prevZ) * tickDelta);

        return new Vec3d(x, y, z);
    }

    public void drawBall(PlayerEntity player, double radius, int gradation, Vec3d pos, Color color, int stage, float tickDelta) {
        float alpha, beta;

        for (alpha = 0.0f; alpha < Math.PI; alpha += Math.PI / gradation) {
            for (beta = 0.0f; beta < 2.0 * Math.PI; beta += Math.PI / gradation) {
                double x1 = (float) (pos.getX() + (radius * Math.cos(beta) * Math.sin(alpha)));
                double y1 = (float) (pos.getY() + (radius * Math.sin(beta) * Math.sin(alpha)));
                double z1 = (float) (pos.getZ() + (radius * Math.cos(alpha)));

                double sin = Math.sin(alpha + Math.PI / gradation);
                double x2 = (float) (pos.getX() + (radius * Math.cos(beta) * sin));
                double y2 = (float) (pos.getY() + (radius * Math.sin(beta) * sin));
                double z2 = (float) (pos.getZ() + (radius * Math.cos(alpha + Math.PI / gradation)));

                Vec3d base = getBase(player, tickDelta);
                Vec3d forward = base.add(0, player.getHeight() / 2.4, 0).add(Vec3d.fromPolar(0, player.getYaw()).multiply(0.1));
                Vec3d vec3d = new Vec3d(x1, y1, z1);

                switch (stage) {
                    case 1 -> {
                        if (!vec3d.isInRange(forward, 0.145)) continue;
                    }
                    case 2 -> {
                        double size = (Friends.get().isFriend(player) ? friendSize.get() : (player != mc.player ? enemySize.get() : penisSize.get()));
                        if (vec3d.isInRange(forward, size + 0.095)) continue;
                    }
                }

                //context.drawVerticalLine();

                Render3DEngine.drawLine(vec3d, new Vec3d(x2, y2, z2), color);
            }
        }
    }

    public void drawPenis(PlayerEntity player, MatrixStack event, double size, Vec3d start, float tickDelta) {
        Vec3d copy = start;
        start = start.add(Vec3d.fromPolar(0, player.getYaw()).multiply(0.1));
        Vec3d end = start.add(Vec3d.fromPolar(0, player.getYaw()).multiply(size));

        List<Vec3d> vecs = getVec3ds(start, 0.1);
        vecs.forEach(vec3d -> {
            if (!vec3d.isInRange(copy, 0.145)) return;
            if (vec3d.isInRange(copy, 0.135)) return;
            Vec3d pos = vec3d.add(Vec3d.fromPolar(0, player.getYaw()).multiply(size));

            Render3DEngine.drawLine(vec3d, pos, penisColor.get());
        });

        drawBall(player, 0.1, gradation.get(), start, penisColor.get(), 1, tickDelta);
        drawBall(player, 0.1, gradation.get(), end, headColor.get(), 2, tickDelta);
    }

    public List<Vec3d> getVec3ds(Vec3d vec3d, double radius) {
        List<Vec3d> vec3ds = new ArrayList<>();
        float alpha, beta;

        for (alpha = 0.0f; alpha < Math.PI; alpha += (float) (Math.PI / gradation.get())) {
            for (beta = 0.0f; beta < 2.01f * Math.PI; beta += (float) (Math.PI / gradation.get())) {
                double x1 = (float) (vec3d.getX() + (radius * Math.cos(beta) * Math.sin(alpha)));
                double y1 = (float) (vec3d.getY() + (radius * Math.sin(beta) * Math.sin(alpha)));
                double z1 = (float) (vec3d.getZ() + (radius * Math.cos(alpha)));

                Vec3d vec = new Vec3d(x1, y1, z1);
                vec3ds.add(vec);
            }
        }

        return vec3ds;
    }

}
