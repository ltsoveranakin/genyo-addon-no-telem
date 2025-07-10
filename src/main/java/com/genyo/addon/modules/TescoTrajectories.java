package com.genyo.addon.modules;

import com.genyo.addon.GenyoAddon;
import com.genyo.addon.render.Render2DEngine;
import com.genyo.addon.render.Render3DEngine;
import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;

public class TescoTrajectories extends Module {

    public TescoTrajectories() {
        super(GenyoAddon.GENYO, "tesco-trajectories", "a thunderhackből mert az talán yobb yo yo yo yo yo-yo yoyo genyo");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("Items")
        .description("miknek írja a cuccli mucclikat wao wao wao")
        .defaultValue(getDefaultItems())
        .filter(this::itemFilter)
        .build()
    );

    private final Setting<SettingColor> renderColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Render Color")
        .description("egyszer volt hol nem volt egy veréb geci")
        .defaultValue(new Color(80, 180, 180, 255))
        .build()
    );

    private final Setting<Boolean> syncColors = sgGeneral.add(new BoolSetting.Builder()
        .name("Don't sync Landed with Render")
        .description("a cucc kettő részének a színe más legyen vagy nooooooooo la policiaaa")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> landedColor = sgGeneral.add(new ColorSetting.Builder()
        .name("Landed Color")
        .description("második színcápa színcápa mondj egy színt te gecis cápa")
        .defaultValue(renderColor.get())
        .visible(syncColors::get)
        .build()
    );

    private boolean itemFilter(Item item) {
        return item instanceof EnderPearlItem || item instanceof TridentItem || item instanceof ExperienceBottleItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem;
    }

    private List<Item> getDefaultItems() {
        List<Item> items = new ArrayList<>();

        for (Item item : Registries.ITEM) {
            if (itemFilter(item)) items.add(item);
        }

        return items;
    }

    // genyók

    private float getDistance(Item item) {
        return item instanceof BowItem ? 1.0f : 0.4f;
    }

    private float getThrowVelocity(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) return 0.5f;
        if (item instanceof ExperienceBottleItem) return 0.59f;
        if (item instanceof TridentItem) return 2f;
        return 1.5f;
    }

    private int getThrowPitch(Item item) {
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem || item instanceof ExperienceBottleItem)
            return 20;
        return 0;
    }

    @EventHandler
    public void onRender(Render3DEvent event) {
        if (mc.options.hudHidden) return; // mi afasz
        if (mc.player == null || mc.world == null || !mc.options.getPerspective().isFirstPerson()) return;

        // ez kurva jó cucc

        ItemStack itemStack = mc.player.getMainHandStack();
        if (!items.get().contains(itemStack.getItem())) {
            itemStack = mc.player.getOffHandStack();
            if (!items.get().contains(itemStack.getItem())) return;
        }

        boolean prev_bob = mc.options.getBobView().getValue();
        mc.options.getBobView().setValue(false);

        final float playerYaw = mc.player.getYaw();
        if (itemStack.getItem() instanceof CrossbowItem && Utils.hasEnchantment(itemStack, Enchantments.MULTISHOT)) {
            calcTrajectory(itemStack.getItem(), playerYaw - 10, event.renderer);
            calcTrajectory(itemStack.getItem(), playerYaw, event.renderer);
            calcTrajectory(itemStack.getItem(), playerYaw + 10, event.renderer);
        } else {
            calcTrajectory(itemStack.getItem(), playerYaw, event.renderer);
        }

        mc.options.getBobView().setValue(prev_bob);
    }

    private void calcTrajectory(Item item, float yaw, Renderer3D renderer) {
        double x = Render2DEngine.interpolate(mc.player.prevX, mc.player.getX(), Render3DEngine.getTickDelta());
        double y = Render2DEngine.interpolate(mc.player.prevY, mc.player.getY(), Render3DEngine.getTickDelta());
        double z = Render2DEngine.interpolate(mc.player.prevZ, mc.player.getZ(), Render3DEngine.getTickDelta());

        // Offset business
        final float pi_genyo = 3.1415927f;

        y = y + mc.player.getEyeHeight(mc.player.getPose()) - 0.1000000014901161; // mivan
        if (item == mc.player.getMainHandStack().getItem()) {
            x = x - MathHelper.cos(yaw / 180.0f * pi_genyo) * 0.16f;
            z = z - MathHelper.sin(yaw / 180.0f * pi_genyo) * 0.16f;
        } else {
            x = x + MathHelper.cos(yaw / 180.0f * pi_genyo) * 0.16f;
            z = z + MathHelper.sin(yaw / 180.0f * pi_genyo) * 0.16f;
        }

        float maxDist = getDistance(item);

        double motionX = -MathHelper.sin(yaw / 180.0f * pi_genyo) * MathHelper.cos(mc.player.getPitch() / 180.0f * pi_genyo) * maxDist;
        double motionY = -MathHelper.sin((mc.player.getPitch() - getThrowPitch(item)) / 180.0f * 3.141593f) * maxDist;
        double motionZ = MathHelper.cos(yaw / 180.0f * pi_genyo) * MathHelper.cos(mc.player.getPitch() / 180.0f * pi_genyo) * maxDist;

        float power = mc.player.getItemUseTime() / 20.0f;
        power = (power * power + power * 2.0f) / 3.0f;

        if (power > 1.0f || power == 0) {
            power = 1.0f;
        }

        final float distance = MathHelper.sqrt((float) (motionX * motionX + motionY * motionY + motionZ * motionZ));
        motionX /= distance;
        motionY /= distance;
        motionZ /= distance;

        final float pow = (item instanceof BowItem ? (power * 2.0f) : item instanceof CrossbowItem ? (2.2f) : 1.0f) * getThrowVelocity(item);

        motionX *= pow;
        motionY *= pow;
        motionZ *= pow;
        if (!mc.player.isOnGround())
            motionY += mc.player.getVelocity().getY();

        Vec3d lastPos;
        for (int i = 0; i < 300; i++) {
            lastPos = new Vec3d(x, y, z);
            x += motionX;
            y += motionY;
            z += motionZ;
            if (mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() == Blocks.WATER) {
                motionX *= 0.8;
                motionY *= 0.8;
                motionZ *= 0.8;
            } else {
                motionX *= 0.99;
                motionY *= 0.99;
                motionZ *= 0.99;
            }

            if (item instanceof BowItem) motionY -= 0.05000000074505806;
            else if (mc.player.getMainHandStack().getItem() instanceof CrossbowItem) motionY -= 0.05000000074505806;
            else motionY -= 0.03f;

            Vec3d pos = new Vec3d(x, y, z);

            for (Entity ent : mc.world.getEntities()) {
                if (ent instanceof ArrowEntity || ent.equals(mc.player)) continue;
                if (ent.getBoundingBox().intersects(new Box(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.3))) {
                    Render3DEngine.OUTLINE_QUEUE.add(new Render3DEngine.OutlineAction(
                        ent.getBoundingBox(),
                        getLandedColor(),
                        2f));
                    Render3DEngine.FILLED_QUEUE.add(new Render3DEngine.FillAction(
                        ent.getBoundingBox(), getLandedColor()
                    ));
                    break;
                }
            }

            BlockHitResult bhr = mc.world.raycast(new RaycastContext(lastPos, pos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
            if (bhr != null && bhr.getType() == HitResult.Type.BLOCK) {
                Render3DEngine.OUTLINE_SIDE_QUEUE.add(new Render3DEngine.OutlineSideAction(
                    new Box(bhr.getBlockPos()), getLandedColor(), 2f, bhr.getSide()
                ));
                Render3DEngine.FILLED_SIDE_QUEUE.add(new Render3DEngine.FillSideAction(
                    new Box(bhr.getBlockPos()), getLandedColor(), bhr.getSide()
                ));

                //renderer.boxLines(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, getLandedColor(), 0);
                //renderer.sideVertical(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, getLandedColor(), getLandedColor(), ShapeMode.Lines);
                //renderer.line(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, getLandedColor());

                break;
            }

            if (y <= -65) break;
            if (motionX == 0 && motionY == 0 && motionZ == 0) continue;

            //Render3DEngine.drawLine(lastPos, pos, renderColor.get());
            float lineW = RenderSystem.getShaderLineWidth();
            renderer.line(lastPos.x, lastPos.y, lastPos.z, pos.x, pos.y, pos.z, renderColor.get());
        }

    }

    private Color getLandedColor() {
        return !(syncColors.get()) ? renderColor.get() : landedColor.get();
    }

}
