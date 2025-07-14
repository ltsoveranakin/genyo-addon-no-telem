package com.genyo.addon.render;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Render3DEngine {

    public static ArrayList<FillAction> FILLED_QUEUE = new ArrayList<>();
    public static ArrayList<OutlineAction> OUTLINE_QUEUE = new ArrayList<>();
    public static ArrayList<FillSideAction> FILLED_SIDE_QUEUE = new ArrayList<>();
    public static ArrayList<OutlineSideAction> OUTLINE_SIDE_QUEUE = new ArrayList<>();
    public static ArrayList<LineAction> LINE_QUEUE = new ArrayList<>();

    @EventHandler
    public void onRender(Render3DEvent event) {
        MatrixStack stack = event.matrices;

        if (!FILLED_QUEUE.isEmpty() || !FILLED_SIDE_QUEUE.isEmpty()) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            RenderSystem.disableDepthTest();
            setupRender();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

            //FILLED_QUEUE.forEach(action -> setFilledBoxVertexes(bufferBuilder, stack.peek().getPositionMatrix(), action.box(), action.color()));

            FILLED_SIDE_QUEUE.forEach(action -> setFilledSidePoints(bufferBuilder, stack.peek().getPositionMatrix(), action.box, action.color(), action.side()));

            Render2DEngine.endBuilding(bufferBuilder);
            endRender();
            RenderSystem.enableDepthTest();

            FILLED_SIDE_QUEUE.clear();
            FILLED_QUEUE.clear();
        }

        if (!OUTLINE_QUEUE.isEmpty() || !OUTLINE_SIDE_QUEUE.isEmpty()) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            setupRender();
            RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);

            RenderSystem.lineWidth(2f);

            OUTLINE_QUEUE.forEach(action -> {
                //setOutlinePoints(action.box(), matrixFrom(action.box().minX, action.box().minY, action.box().minZ), buffer, action.color());
            });

            OUTLINE_SIDE_QUEUE.forEach(action -> {
                //setSideOutlinePoints(action.box, matrixFrom(action.box().minX, action.box().minY, action.box().minZ), bufferBuilder, action.color(), action.side());
                setSideOutlinePoints(action.box, stack, bufferBuilder, action.color(), action.side());
            });

            Render2DEngine.endBuilding(bufferBuilder);
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            endRender();

            OUTLINE_QUEUE.clear();
            OUTLINE_SIDE_QUEUE.clear();
        }

        if (!LINE_QUEUE.isEmpty()) {
            setupRender();
            Tessellator tessellator = Tessellator.getInstance();
            RenderSystem.disableCull();
            RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);

            RenderSystem.lineWidth(2f);
            RenderSystem.disableDepthTest();

            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
            LINE_QUEUE.forEach(action -> {
                MatrixStack matrices = matrixFrom(action.start.getX(), action.start.getY(), action.start.getZ());
                vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (action.end.getX() - action.start.getX()), (float) (action.end.getY() - action.start.getY()), (float) (action.end.getZ() - action.start.getZ()), action.color);
            });

            Render2DEngine.endBuilding(buffer);

            RenderSystem.enableCull();
            RenderSystem.lineWidth(1f);
            RenderSystem.enableDepthTest();
            endRender();

            LINE_QUEUE.clear();
        }
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void endRender() {
        RenderSystem.disableBlend();
    }

    /*public static void setFilledBoxVertexes(@NotNull BufferBuilder bufferBuilder, Matrix4f m, @NotNull Box box, @NotNull Color c) {
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());

        int meteor_rgb = new java.awt.Color(c.r, c.g, c.b, c.a).getRGB();

        bufferBuilder.vertex(m, minX, minY, minZ).color(meteor_rgb);
        bufferBuilder.vertex(m, maxX, minY, minZ).color(meteor_rgb);
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(meteor_rgb);
        bufferBuilder.vertex(m, minX, minY, maxZ).color(meteor_rgb);

        bufferBuilder.vertex(m, minX, minY, minZ).color(meteor_rgb);
        bufferBuilder.vertex(m, minX, maxY, minZ).color(meteor_rgb);
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(meteor_rgb);
        bufferBuilder.vertex(m, maxX, minY, minZ).color(meteor_rgb);

        bufferBuilder.vertex(m, maxX, minY, minZ).color(meteor_rgb);
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(meteor_rgb);
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(meteor_rgb);
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(meteor_rgb);

        bufferBuilder.vertex(m, minX, minY, maxZ).color(meteor_rgb);
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(meteor_rgb);
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(meteor_rgb);
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(meteor_rgb);

        bufferBuilder.vertex(m, minX, minY, minZ).color(meteor_rgb);
        bufferBuilder.vertex(m, minX, minY, maxZ).color(meteor_rgb);
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(meteor_rgb);
        bufferBuilder.vertex(m, minX, maxY, minZ).color(meteor_rgb);

        bufferBuilder.vertex(m, minX, maxY, minZ).color(meteor_rgb);
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(meteor_rgb);
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(meteor_rgb);
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(meteor_rgb);
    }*/

    /*public static void setOutlinePoints(Box box, MatrixStack matrices, BufferBuilder buffer, Color color) {
        box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        //vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
        //vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
        vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
        vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);
        vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
        //vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
        vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
        vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);
        //vertexLine(matrices, buffer, x1, y2, z1, x2, y2, z1, color);
        vertexLine(matrices, buffer, x2, y2, z1, x2, y2, z2, color);
        //vertexLine(matrices, buffer, x2, y2, z2, x1, y2, z2, color);
        vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);
    }*/

    public static void vertexLine(@NotNull MatrixStack matrices, @NotNull VertexConsumer buffer, float x1, float y1, float z1, float x2, float y2, float z2, @NotNull Color lineColor) {
        Matrix4f model = matrices.peek().getPositionMatrix();
        MatrixStack.Entry entry = matrices.peek();
        Vector3f normalVec = getNormal(x1, y1, z1, x2, y2, z2);

        buffer.vertex(model, x1, y1, z1).color(lineColor.r, lineColor.g, lineColor.b, lineColor.a).normal(entry, normalVec.x(), normalVec.y(), normalVec.z());
        buffer.vertex(model, x2, y2, z2).color(lineColor.r, lineColor.g, lineColor.b, lineColor.a).normal(entry, normalVec.x(), normalVec.y(), normalVec.z());
    }

    public static @NotNull Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }

    public static void setFilledSidePoints(BufferBuilder buffer, Matrix4f matrix, Box box, Color c, Direction dir) {
        Vec3d cameraPos = mc.player.getCameraPosVec(getTickDelta());

        float minX = (float) (box.minX - cameraPos.getX());
        float minY = (float) (box.minY - cameraPos.getY());
        float minZ = (float) (box.minZ - cameraPos.getZ());
        float maxX = (float) (box.maxX - cameraPos.getX());
        float maxY = (float) (box.maxY - cameraPos.getY());
        float maxZ = (float) (box.maxZ - cameraPos.getZ());

        int meteor_rgb = new java.awt.Color(c.r, c.g, c.b, 150).getRGB();

        switch (dir) {
            case Direction.DOWN -> {
                buffer.vertex(matrix, minX, minY, minZ).color(meteor_rgb);
                buffer.vertex(matrix, maxX, minY, minZ).color(meteor_rgb);
                buffer.vertex(matrix, maxX, minY, maxZ).color(meteor_rgb);
                buffer.vertex(matrix, minX, minY, maxZ).color(meteor_rgb);
            }
            case Direction.NORTH -> {
                buffer.vertex(matrix, minX, minY, minZ).color(meteor_rgb);
                buffer.vertex(matrix, minX, maxY, minZ).color(meteor_rgb);
                buffer.vertex(matrix, maxX, maxY, minZ).color(meteor_rgb);
                buffer.vertex(matrix, maxX, minY, minZ).color(meteor_rgb);
            }
            case Direction.EAST -> {
                buffer.vertex(matrix, maxX, minY, minZ).color(meteor_rgb);
                buffer.vertex(matrix, maxX, maxY, minZ).color(meteor_rgb);
                buffer.vertex(matrix, maxX, maxY, maxZ).color(meteor_rgb);
                buffer.vertex(matrix, maxX, minY, maxZ).color(meteor_rgb);
            }
            case Direction.SOUTH -> {
                buffer.vertex(matrix, minX, minY, maxZ).color(meteor_rgb);
                buffer.vertex(matrix, maxX, minY, maxZ).color(meteor_rgb);
                buffer.vertex(matrix, maxX, maxY, maxZ).color(meteor_rgb);
                buffer.vertex(matrix, minX, maxY, maxZ).color(meteor_rgb);
            }
            case Direction.WEST -> {
                buffer.vertex(matrix, minX, minY, minZ).color(meteor_rgb);
                buffer.vertex(matrix, minX, minY, maxZ).color(meteor_rgb);
                buffer.vertex(matrix, minX, maxY, maxZ).color(meteor_rgb);
                buffer.vertex(matrix, minX, maxY, minZ).color(meteor_rgb);
            }
            case Direction.UP -> {
                buffer.vertex(matrix, minX, maxY, minZ).color(meteor_rgb);
                buffer.vertex(matrix, minX, maxY, maxZ).color(meteor_rgb);
                buffer.vertex(matrix, maxX, maxY, maxZ).color(meteor_rgb);
                buffer.vertex(matrix, maxX, maxY, minZ).color(meteor_rgb);
            }
        }
    }

    public static void setSideOutlinePoints(Box box, MatrixStack matrices, BufferBuilder buffer, Color color, Direction dir) {
        Vec3d cameraPos = mc.player.getCameraPosVec(getTickDelta());

        float x1 = (float) (box.minX - cameraPos.getX());
        float y1 = (float) (box.minY - cameraPos.getY());
        float z1 = (float) (box.minZ - cameraPos.getZ());
        float x2 = (float) (box.maxX - cameraPos.getX());
        float y2 = (float) (box.maxY - cameraPos.getY());
        float z2 = (float) (box.maxZ - cameraPos.getZ());

        switch (dir) {
            case Direction.UP -> {
                vertexLine(matrices, buffer, x1, y2, z1, x2, y2, z1, color);
                vertexLine(matrices, buffer, x2, y2, z1, x2, y2, z2, color);
                vertexLine(matrices, buffer, x2, y2, z2, x1, y2, z2, color);
                vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);
            }
            case Direction.DOWN -> {
                vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
                vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
                vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
                vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);
            }
            case Direction.EAST -> {
                vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);
                vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
                vertexLine(matrices, buffer, x2, y2, z2, x2, y2, z1, color);
                vertexLine(matrices, buffer, x2, y1, z2, x2, y1, z1, color);
            }
            case Direction.WEST -> {
                vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
                vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
                vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);
                vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);
            }
            case Direction.NORTH -> {
                vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);
                vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
                vertexLine(matrices, buffer, x2, y1, z1, x1, y1, z1, color);
                vertexLine(matrices, buffer, x2, y2, z1, x1, y2, z1, color);
            }
            case Direction.SOUTH -> {
                vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
                vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
                vertexLine(matrices, buffer, x1, y1, z2, x2, y1, z2, color);
                vertexLine(matrices, buffer, x1, y2, z2, x2, y2, z2, color);
            }
        }
    }

    public static @NotNull MatrixStack matrixFrom(double x, double y, double z) {
        MatrixStack matrices = new MatrixStack();

        Camera camera = mc.getEntityRenderDispatcher().camera;
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        Vec3d cameraPos = mc.player.getCameraPosVec(getTickDelta());
        matrices.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);

        return matrices;
    }

    public static void drawLine(@NotNull Vec3d start, @NotNull Vec3d end, @NotNull Color color) {
        LINE_QUEUE.add(new LineAction(start, end, color));
    }

    public static float getTickDelta() {
        return mc.getRenderTickCounter().getTickDelta(true);
    }

    public record FillAction(Box box, Color color) { }

    public record OutlineAction(Box box, Color color, float lineWidth) { }

    public record FillSideAction(Box box, Color color, Direction side) { }

    public record OutlineSideAction(Box box, Color color, float lineWidth, Direction side) { }

    public record LineAction(Vec3d start, Vec3d end, Color color) { }
}
