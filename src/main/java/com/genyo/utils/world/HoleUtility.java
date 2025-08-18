package com.genyo.utils.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HoleUtility {

    public static int calcOffset(double dec) {
        return dec >= 0.7 ? 1 : (dec <= 0.3 ? -1 : 0);
    }

    private static @NotNull BlockPos getPos(@NotNull Vec3d from) {
        return BlockPos.ofFloored(from.getX(), from.getY() - Math.floor(from.getY()) > 0.8 ? Math.floor(from.getY()) + 1.0 : Math.floor(from.getY()), from.getZ());
    }

    public static @NotNull List<BlockPos> getHolePoses(@NotNull Vec3d from) {
        List<BlockPos> positions = new ArrayList<>();

        double decimalX = from.getX() - Math.floor(from.getX());
        double decimalZ = from.getZ() - Math.floor(from.getZ());
        int offX = calcOffset(decimalX);
        int offZ = calcOffset(decimalZ);
        positions.add(getPos(from));
        for (int x = 0; x <= Math.abs(offX); ++x) {
            for (int z = 0; z <= Math.abs(offZ); ++z) {
                int properX = x * offX;
                int properZ = z * offZ;
                positions.add(Objects.requireNonNull(getPos(from)).add(properX, 0, properZ));
            }
        }

        return positions;
    }

    public static int calcLength(double decimal, boolean negative) {
        if (negative) return decimal <= 0.3 ? 1 : 0;
        return decimal >= 0.7 ? 1 : 0;
    }

    public static BlockPos addToPlayer(@NotNull BlockPos playerPos, double x, double y, double z) {
        if (playerPos.getX() < 0) x = -x;
        if (playerPos.getY() < 0) y = -y;
        if (playerPos.getZ() < 0) z = -z;
        return playerPos.add(BlockPos.ofFloored(x, y, z));
    }

    public static @NotNull List<BlockPos> getSurroundPoses(@NotNull Vec3d from) {
        final BlockPos fromPos = BlockPos.ofFloored(from);
        final ArrayList<BlockPos> tempOffsets = new ArrayList<>();

        final double decimalX = Math.abs(from.getX()) - Math.floor(Math.abs(from.getX()));
        final double decimalZ = Math.abs(from.getZ()) - Math.floor(Math.abs(from.getZ()));
        final int lengthXPos = calcLength(decimalX, false);
        final int lengthXNeg = calcLength(decimalX, true);
        final int lengthZPos = calcLength(decimalZ, false);
        final int lengthZNeg = calcLength(decimalZ, true);

        for (int x = 1; x < lengthXPos + 1; ++x) {
            tempOffsets.add(addToPlayer(fromPos, x, 0.0, 1 + lengthZPos));
            tempOffsets.add(addToPlayer(fromPos, x, 0.0, -(1 + lengthZNeg)));
        }
        for (int x = 0; x <= lengthXNeg; ++x) {
            tempOffsets.add(addToPlayer(fromPos, -x, 0.0, 1 + lengthZPos));
            tempOffsets.add(addToPlayer(fromPos, -x, 0.0, -(1 + lengthZNeg)));
        }
        for (int z = 1; z < lengthZPos + 1; ++z) {
            tempOffsets.add(addToPlayer(fromPos, 1 + lengthXPos, 0.0, z));
            tempOffsets.add(addToPlayer(fromPos, -(1 + lengthXNeg), 0.0, z));
        }
        for (int z = 0; z <= lengthZNeg; ++z) {
            tempOffsets.add(addToPlayer(fromPos, 1 + lengthXPos, 0.0, -z));
            tempOffsets.add(addToPlayer(fromPos, -(1 + lengthXNeg), 0.0, -z));
        }

        return tempOffsets;
    }

}
