package com.omniblock.register;

import com.omniblock.OmniBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(OmniBlock.MOD_ID);

    public static final DeferredItem<BlockItem> OMNI_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("omni_block", ModBlocks.OMNI_BLOCK,
                    new Item.Properties());
}
