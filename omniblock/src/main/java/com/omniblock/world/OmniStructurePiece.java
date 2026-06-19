package com.omniblock.world;

import com.omniblock.register.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class OmniStructurePiece extends StructurePiece {

    private final int tier;

    public OmniStructurePiece(StructurePieceType type, int tier, BlockPos pos) {
        super(type, 0, makeBB(pos, tier));
        this.tier = tier;
    }

    public OmniStructurePiece(StructurePieceType type, StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(type, tag);
        this.tier = tag.getInt("Tier");
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("Tier", tier);
    }

    private static BoundingBox makeBB(BlockPos pos, int tier) {
        int r = switch (tier) { case 3 -> 14; case 2 -> 7; case 1 -> 6; default -> 3; };
        int h = switch (tier) { case 3 -> 14; case 2 -> 12; case 1 -> 6; default -> 4; };
        return new BoundingBox(pos.getX()-r, pos.getY()-60, pos.getZ()-r,
                               pos.getX()+r, pos.getY()+h, pos.getZ()+r);
    }

    // Prende il massimo Y tra i 4 angoli e il centro per ancorare la struttura al terreno più alto
    private int getMaxSurfaceY(WorldGenLevel level, BlockPos center, int radius) {
        int maxY = Integer.MIN_VALUE;
        int[] offsets = {-radius, radius};
        for (int dx : offsets)
            for (int dz : offsets) {
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, center.getX() + dx, center.getZ() + dz);
                if (y > maxY) maxY = y;
            }
        // Controlla anche il centro
        int centerY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, center.getX(), center.getZ());
        if (centerY > maxY) maxY = centerY;
        return maxY;
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator cg,
                            RandomSource random, BoundingBox bb, ChunkPos cp, BlockPos origin) {
        int cx = (boundingBox.minX() + boundingBox.maxX()) / 2;
        int cz = (boundingBox.minZ() + boundingBox.maxZ()) / 2;
        int r = switch (tier) { case 3 -> 14; case 2 -> 7; case 1 -> 6; default -> 3; };

        int surfaceY = getMaxSurfaceY(level, new BlockPos(cx, 0, cz), r);
        BlockPos base = new BlockPos(cx, surfaceY, cz);

        clearAbove(level, base, tier);

        switch (tier) {
            case 0 -> placeCommon(level, base);
            case 1 -> placeUncommon(level, base);
            case 2 -> placeRare(level, base);
            case 3 -> {}
        }
    }

    private void clearAbove(WorldGenLevel level, BlockPos base, int tier) {
        int r = switch (tier) { case 3 -> 14; case 2 -> 8; case 1 -> 7; default -> 4; };
        int h = switch (tier) { case 3 -> 14; case 2 -> 14; case 1 -> 10; default -> 6; };
        for (int x = -r; x <= r; x++)
            for (int z = -r; z <= r; z++)
                for (int y = 0; y <= h; y++)
                    level.setBlock(base.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
    }

    private void placeCommon(WorldGenLevel level, BlockPos base) {
        for (int x = -2; x <= 2; x++)
            for (int z = -2; z <= 2; z++)
                set(level, base.offset(x, 0, z), Blocks.COBBLESTONE.defaultBlockState());
        for (int i = -2; i <= 2; i++) {
            set(level, base.offset(-2, 0, i), Blocks.STONE_BRICKS.defaultBlockState());
            set(level, base.offset(2, 0, i), Blocks.STONE_BRICKS.defaultBlockState());
            set(level, base.offset(i, 0, -2), Blocks.STONE_BRICKS.defaultBlockState());
            set(level, base.offset(i, 0, 2), Blocks.STONE_BRICKS.defaultBlockState());
        }
        set(level, base.above(1), Blocks.STONE_BRICKS.defaultBlockState());
        set(level, base.above(2), Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        set(level, base.above(3), Blocks.STONE_BRICK_SLAB.defaultBlockState());
        set(level, base.above(4), ModBlocks.OMNI_SINGLE_COMMON.get().defaultBlockState());
        for (int[] c : new int[][]{{-1,-1},{-1,1},{1,-1},{1,1}})
            set(level, base.offset(c[0], 1, c[1]), Blocks.TORCH.defaultBlockState());
    }

    private void placeUncommon(WorldGenLevel level, BlockPos base) {
        for (int x = -5; x <= 5; x++)
            for (int z = -5; z <= 5; z++)
                set(level, base.offset(x, 0, z), Blocks.COARSE_DIRT.defaultBlockState());
        for (int x = -2; x <= 2; x++)
            for (int z = -2; z <= 2; z++)
                set(level, base.offset(x, 1, z), Blocks.DARK_OAK_PLANKS.defaultBlockState());
        for (int y = 1; y <= 5; y++) {
            for (int i = -2; i <= 2; i++) {
                boolean gap = (y <= 3) && (Math.abs(i) <= 0);
                if (!gap) {
                    set(level, base.offset(-2, y, i), Blocks.DARK_OAK_LOG.defaultBlockState());
                    set(level, base.offset(2, y, i), Blocks.DARK_OAK_LOG.defaultBlockState());
                    set(level, base.offset(i, y, -2), Blocks.DARK_OAK_LOG.defaultBlockState());
                    set(level, base.offset(i, y, 2), Blocks.DARK_OAK_LOG.defaultBlockState());
                }
            }
        }
        for (int x = -3; x <= 3; x++)
            for (int z = -3; z <= 3; z++)
                set(level, base.offset(x, 6, z), Blocks.DARK_OAK_PLANKS.defaultBlockState());
        set(level, base.above(7), Blocks.DARK_OAK_PLANKS.defaultBlockState());
        for (int[] c : new int[][]{{-4,-4},{-4,4},{4,-4},{4,4}}) {
            set(level, base.offset(c[0], 1, c[1]), Blocks.DARK_OAK_FENCE.defaultBlockState());
            set(level, base.offset(c[0], 2, c[1]), Blocks.DARK_OAK_FENCE.defaultBlockState());
            set(level, base.offset(c[0], 3, c[1]), Blocks.WHITE_WOOL.defaultBlockState());
        }
        set(level, base.offset(1, 1, 1), Blocks.CHEST.defaultBlockState());
        set(level, base.offset(-1, 1, -1), Blocks.TORCH.defaultBlockState());
        set(level, base.offset(1, 1, -1), Blocks.TORCH.defaultBlockState());
        setSpawner(level, base.offset(0, 0, 0), EntityType.VINDICATOR);
        set(level, base.offset(0, 1, 0), Blocks.DARK_OAK_TRAPDOOR.defaultBlockState());
        set(level, base.above(8), ModBlocks.OMNI_SINGLE_UNCOMMON.get().defaultBlockState());
        PendingSpawnerConfig.queueGuardians(base.above(1), PendingSpawnerConfig.GuardianTier.UNCOMMON, 3);
    }

    private void placeRare(WorldGenLevel level, BlockPos base) {
        for (int x = -6; x <= 6; x++)
            for (int z = -6; z <= 6; z++)
                set(level, base.offset(x, 0, z), Blocks.SMOOTH_STONE.defaultBlockState());
        for (int i = -6; i <= 6; i++) {
            set(level, base.offset(-6, 0, i), Blocks.POLISHED_ANDESITE.defaultBlockState());
            set(level, base.offset(6, 0, i), Blocks.POLISHED_ANDESITE.defaultBlockState());
            set(level, base.offset(i, 0, -6), Blocks.POLISHED_ANDESITE.defaultBlockState());
            set(level, base.offset(i, 0, 6), Blocks.POLISHED_ANDESITE.defaultBlockState());
        }
        int[] sizes = {4, 3, 2};
        int floorY = 1;
        for (int floor = 0; floor < 3; floor++) {
            int s = sizes[floor];
            for (int x = -s; x <= s; x++)
                for (int z = -s; z <= s; z++)
                    if (Math.abs(x) == s || Math.abs(z) == s)
                        for (int y = 0; y < 3; y++)
                            set(level, base.offset(x, floorY+y, z), Blocks.DARK_OAK_PLANKS.defaultBlockState());
            for (int x = -s+1; x <= s-1; x++)
                for (int z = -s+1; z <= s-1; z++)
                    set(level, base.offset(x, floorY, z), Blocks.DARK_OAK_PLANKS.defaultBlockState());
            for (int x = -s-1; x <= s+1; x++)
                for (int z = -s-1; z <= s+1; z++)
                    set(level, base.offset(x, floorY+3, z), Blocks.RED_CONCRETE.defaultBlockState());
            for (int[] c : new int[][]{{-s-1,-s-1},{-s-1,s+1},{s+1,-s-1},{s+1,s+1}})
                set(level, base.offset(c[0], floorY+4, c[1]),
                    Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            set(level, base.offset(0, floorY, s), Blocks.AIR.defaultBlockState());
            set(level, base.offset(0, floorY+1, s), Blocks.AIR.defaultBlockState());
            floorY += 4;
        }
        for (int y = 1; y < floorY; y++)
            set(level, base.offset(0, y, 0), Blocks.LADDER.defaultBlockState());
        setSpawner(level, base.offset(2, 1, 2), EntityType.VEX);
        setSpawner(level, base.offset(-2, 5, -2), EntityType.PILLAGER);
        set(level, base.offset(0, floorY, 0), ModBlocks.OMNI_SINGLE_RARE.get().defaultBlockState());
        PendingSpawnerConfig.queueGuardians(base.above(1), PendingSpawnerConfig.GuardianTier.RARE, 5);
    }

    private void setSpawner(WorldGenLevel level, BlockPos pos, net.minecraft.world.entity.EntityType<?> type) {
        set(level, pos, Blocks.SPAWNER.defaultBlockState());
        PendingSpawnerConfig.queueSpawner(pos, type);
    }

    private void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }
}
