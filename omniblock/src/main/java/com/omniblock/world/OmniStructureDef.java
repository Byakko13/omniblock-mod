package com.omniblock.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import java.util.Optional;

public class OmniStructureDef extends Structure {

    public static StructureType<OmniStructureDef> TYPE;

    private final int tier;

    public static final MapCodec<OmniStructureDef> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Structure.settingsCodec(i),
        Codec.INT.fieldOf("tier").forGetter(s -> s.tier)
    ).apply(i, OmniStructureDef::new));

    public OmniStructureDef(Structure.StructureSettings settings, int tier) {
        super(settings);
        this.tier = tier;
    }

    @Override
    public StructureType<?> type() { return TYPE; }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext ctx) {
        ChunkPos cp = ctx.chunkPos();
        int x = cp.getMiddleBlockX();
        int z = cp.getMiddleBlockZ();
        int y = ctx.chunkGenerator().getFirstOccupiedHeight(
            x, z, Heightmap.Types.WORLD_SURFACE_WG,
            ctx.heightAccessor(), ctx.randomState());
        if (y < ctx.heightAccessor().getMinBuildHeight() + 5) return Optional.empty();
        BlockPos pos = new BlockPos(x, y, z);
        return Optional.of(new Structure.GenerationStub(pos, builder ->
            builder.addPiece(new OmniStructurePiece(
                OmniWorldGen.OMNI_PIECE_TYPE.get(), tier, pos
            ))
        ));
    }
}
