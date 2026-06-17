package com.omniblock.register;

import com.omniblock.OmniBlock;
import com.omniblock.block.OmniBlockBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(OmniBlock.MOD_ID);

    public static final DeferredBlock<OmniBlockBlock> OMNI_BLOCK =
            BLOCKS.register("omni_block", () -> new OmniBlockBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .requiresCorrectToolForDrops()
                            .strength(3.0f, 6.0f)          // hardness, blast resistance
                            .sound(SoundType.METAL)
                            .lightLevel(state -> 7)         // glows slightly
            ));
}
