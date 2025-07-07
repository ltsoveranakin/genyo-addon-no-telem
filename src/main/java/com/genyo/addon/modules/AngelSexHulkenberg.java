package com.genyo.addon.modules;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.events.TotemPopEvent;
import com.genyo.addon.mixin.IEntity;
import com.genyo.addon.render.Render2DEngine;
import com.genyo.addon.render.Render3DEngine;
import com.genyo.addon.utils.MathUtil;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CopyOnWriteArrayList;

public final class AngelSexHulkenberg extends Module {

    public AngelSexHulkenberg() {
        super(GenyoAddon.GENYO, "angel-sex-hulkenberg", "jön a verstappen, nekiütközött a verstappen, kiesik a verstappen");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("Mode")
        .description("Ki a faszom az a Hulkenberg??????????")
        .defaultValue(Mode.Textured)
        .build()
    );

    private final Setting<Boolean> secondLayer = sgGeneral.add(new BoolSetting.Builder()
        .name("Second Layer")
        .description("kiyártam a kettedik osztájt")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("Color")
        .description("színcápa színcápa mondj egy színt")
        .defaultValue(new Color(53, 46, 46, 255))
        .build()
    );

    private final Setting<Integer> ySpeed = sgGeneral.add(new IntSetting.Builder()
        .name("Y Speed")
        .description("y show speed")
        .defaultValue(2)
        .min(0)
        .max(6)
        .build()
    );

    private final Setting<Integer> aSpeed = sgGeneral.add(new IntSetting.Builder()
        .name("Alpha Speed")
        .description("alpha-i show speed")
        .defaultValue(5)
        .min(1)
        .max(100)
        .build()
    );

    private final Setting<Double> rotSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Rotation Speed")
        .description("rotációs kapa")
        .defaultValue(1d)
        .min(0d)
        .max(6d)
        .build()
    );

    private final CopyOnWriteArrayList<Person> popList = new CopyOnWriteArrayList<>();

    private enum Mode {
        Simple, Textured
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        popList.forEach(person -> person.update(popList));
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        MatrixStack stack = event.matrices;

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();

        if (mode.get().equals(Mode.Simple)) RenderSystem.defaultBlendFunc();
        else RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);

        popList.forEach(person -> renderEntity(stack, person.player, person.getTexture(), person.getAlpha()));

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @EventHandler
    @SuppressWarnings("unused")
    private void onTotemPop(@NotNull TotemPopEvent e) {
        if (e.entity.equals(mc.player) || mc.world == null) return;
        //if (mc.world == null) return; -------- for testing
        if (mc.getServer() == null) return;

        AbstractClientPlayerEntity entity = new AbstractClientPlayerEntity(mc.world, new GameProfile(e.entity.getUuid(), e.entity.getName().getString())) {
            @Override public boolean isSpectator() {return false;}
            @Override public boolean isCreative() {return false;}
        };

        entity.copyPositionAndRotation(e.entity);
        entity.bodyYaw = e.entity.bodyYaw;
        entity.headYaw = e.entity.headYaw;
        entity.handSwingProgress = e.entity.handSwingProgress;
        entity.handSwingTicks = e.entity.handSwingTicks;
        entity.setSneaking(e.entity.isSneaking());
        entity.limbAnimator.setSpeed(e.entity.limbAnimator.getSpeed());
        entity.limbAnimator.pos = e.entity.limbAnimator.getPos();

        ServerWorld sWorld = mc.getServer().getWorld(entity.getWorld().getRegistryKey());
        popList.add(new Person(entity, ((AbstractClientPlayerEntity) e.entity).getSkinTextures().texture(), sWorld));
    }

    private void renderEntity(@NotNull MatrixStack matrices, @NotNull LivingEntity entity, Identifier texture, int alpha) {
        PlayerEntityRenderer entityRenderer = (PlayerEntityRenderer) mc.getEntityRenderDispatcher().getRenderer((AbstractClientPlayerEntity) entity);
        PlayerEntityRenderState renderState = entityRenderer.createRenderState();

        renderState.leftPantsLegVisible = secondLayer.get();
        renderState.rightPantsLegVisible = secondLayer.get();
        renderState.leftSleeveVisible = secondLayer.get();
        renderState.rightSleeveVisible = secondLayer.get();
        renderState.jacketVisible  = secondLayer.get();
        renderState.hatVisible = secondLayer.get();

        double x = entity.getX() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = entity.getY() - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = entity.getZ() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        ((IEntity) entity).setPos(entity.getPos().add(0, (double) ySpeed.get() / 50., 0));

        matrices.push();
        matrices.translate((float) x, (float) y, (float) z);

        float yRotYaw = ((alpha / 255f) * 360f * rotSpeed.get().floatValue());
        yRotYaw = yRotYaw == 0 ? 0 : Render2DEngine.interpolateFloat(yRotYaw, yRotYaw - (((aSpeed.get() / 255f) * 360f * rotSpeed.get().floatValue())), Render3DEngine.getTickDelta());

        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(MathUtil.rad(180 - entity.bodyYaw + yRotYaw)));
        prepareScale(matrices);

        float limbSpeed = Math.min(entity.limbAnimator.getSpeed(), 1f);

        entityRenderer.updateRenderState((AbstractClientPlayerEntity) entity, renderState, limbSpeed);

        BufferBuilder buffer;
        if (mode.get().equals(Mode.Textured)) {
            RenderSystem.setShaderTexture(0, texture);
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        } else {
            buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        }

        RenderSystem.setShaderColor(color.get().r, color.get().g, color.get().b, alpha / 255f);

        entityRenderer.render(renderState, matrices, mc.getBufferBuilders().getEntityVertexConsumers(), 1);
        //modelBase.render(matrices, buffer, 10, 0);
        endBuilding(buffer);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        matrices.pop();
    }

    public static void endBuilding(BufferBuilder bb) {
        BuiltBuffer builtBuffer = bb.endNullable();
        if (builtBuffer != null)
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
    }

    private static void prepareScale(@NotNull MatrixStack matrixStack) {
        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        matrixStack.scale(1.6f, 1.8f, 1.6f);
        matrixStack.translate(0.0F, -1.501F, 0.0F);
    }

    private class Person {
        private final AbstractClientPlayerEntity player;
        private final Identifier texture;
        private int alpha;
        private final ServerWorld world;

        public Person(AbstractClientPlayerEntity player, Identifier texture, ServerWorld world) {
            this.player = player;
            this.world = world;
            this.texture = texture;
            alpha = color.get().a;
        }

        public void update(CopyOnWriteArrayList<Person> arrayList) {
            if (alpha <= 0) {
                arrayList.remove(this);
                player.kill(world);
                player.remove(Entity.RemovalReason.KILLED);
                player.onRemoved();
                return;
            }
            alpha -= aSpeed.get();
        }

        public int getAlpha() {
            return MathUtil.clamp(alpha, 0, 255);
        }

        public Identifier getTexture() {
            return texture;
        }
    }
}
