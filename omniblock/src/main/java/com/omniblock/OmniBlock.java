package com.omniblock;

import com.omniblock.register.ModBlocks;
import com.omniblock.register.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(OmniBlock.MOD_ID)
public class OmniBlock {

    public static final String MOD_ID = "omniblock";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    // Cache of all registered items (populated at setup)
    public static final List<Item> ALL_ITEMS_CACHE = new ArrayList<>();

    public OmniBlock(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Collect all items from all mods into the cache
            ALL_ITEMS_CACHE.clear();
            for (ResourceLocation rl : BuiltInRegistries.ITEM.keySet()) {
                Item item = BuiltInRegistries.ITEM.get(rl);
                if (item != null) {
                    ALL_ITEMS_CACHE.add(item);
                }
            }
            LOGGER.info("[OmniBlock] Cached {} items from all loaded mods.", ALL_ITEMS_CACHE.size());
        });
    }
}
