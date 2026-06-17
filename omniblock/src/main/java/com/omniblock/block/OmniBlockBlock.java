package com.omniblock.block;

import com.omniblock.OmniBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.ArrayList;
import java.util.List;

public class OmniBlockBlock extends Block {

    // Rigenerazione in 3 secondi (60 tick)
    private static final int REGEN_TICKS = 60;

    // Fortune: ogni livello aggiunge 25% di probabilità di drop extra
    private static final double FORTUNE_BONUS_PER_LEVEL = 0.25;

    // Probabilità che un item droppato sia incantato (10%)
    private static final double ENCHANT_CHANCE = 0.10;

    // Lista degli enchantment "generici" applicabili a qualunque item
    private static final List<net.minecraft.resources.ResourceKey<
            net.minecraft.world.item.enchantment.Enchantment>> GENERIC_ENCHANTS = List.of(
            Enchantments.UNBREAKING,
            Enchantments.MENDING
    );

    public OmniBlockBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void playerDestroyed(Level level, Player player, BlockPos pos, BlockState state,
                                net.minecraft.world.level.block.entity.BlockEntity blockEntity,
                                ItemStack tool) {
        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;

            boolean hasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(
                    serverLevel.registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                            .getHolderOrThrow(Enchantments.SILK_TOUCH),
                    tool
            ) > 0;

            if (!hasSilkTouch) {
                int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(
                        serverLevel.registryAccess()
                                .registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                                .getHolderOrThrow(Enchantments.FORTUNE),
                        tool
                );

                // Drop base garantito
                spawnRandomItem(serverLevel, pos);

                // Drop extra in base alla Fortune
                for (int i = 0; i < fortuneLevel; i++) {
                    if (serverLevel.random.nextDouble() < FORTUNE_BONUS_PER_LEVEL) {
                        spawnRandomItem(serverLevel, pos);
                    }
                }

                // ── Effetti alla rottura ──────────────────────────────────────
                // Particelle colorate esplosive
                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        1, 0, 0, 0, 0);
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        40, 0.4, 0.4, 0.4, 0.15);
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        20, 0.5, 0.5, 0.5, 0.1);

                // Suono magico alla rottura
                serverLevel.playSound(null, pos,
                        SoundEvents.TOTEM_USE,
                        SoundSource.BLOCKS,
                        0.6f, 1.2f);
                serverLevel.playSound(null, pos,
                        SoundEvents.AMETHYST_BLOCK_BREAK,
                        SoundSource.BLOCKS,
                        1.0f, 0.8f);

                // Schedula regen
                serverLevel.scheduleTick(pos, this, REGEN_TICKS);
            }
        }
    }

    /**
     * Spawna un item casuale; con il 10% di probabilità lo incanta.
     */
    private void spawnRandomItem(ServerLevel level, BlockPos pos) {
        List<Item> items = OmniBlock.ALL_ITEMS_CACHE;
        if (items.isEmpty()) return;

        Item randomItem = items.get(level.random.nextInt(items.size()));
        ItemStack stack = new ItemStack(randomItem, 1);

        // 10% di probabilità di incantamento
        if (level.random.nextDouble() < ENCHANT_CHANCE) {
            tryEnchantStack(stack, level);
        }

        Block.popResource(level, pos, stack);
    }

    /**
     * Tenta di applicare un enchantment casuale allo stack.
     * Prima prova EnchantmentHelper.enchantItem (rispetta le regole vanilla),
     * poi come fallback applica uno degli enchantment generici.
     */
    private void tryEnchantStack(ItemStack stack, ServerLevel level) {
        // Prova incantamento casuale con livello 1-30
        int enchLevel = 5 + level.random.nextInt(26); // 5..30
        ItemStack result = net.minecraft.world.item.enchantment.EnchantmentHelper
                .enchantItem(level.random, stack, enchLevel, level.registryAccess(), java.util.Optional.empty());

        // Se non è stato incantato nulla, applica Unbreaking III come fallback
        if (!result.isEnchanted()) {
            var enchReg = level.registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
            var key = GENERIC_ENCHANTS.get(level.random.nextInt(GENERIC_ENCHANTS.size()));
            try {
                var holder = enchReg.getHolderOrThrow(key);
                stack.enchant(holder, 1 + level.random.nextInt(3));
            } catch (Exception ignored) {
                // Se l'enchantment non esiste nella versione corrente, skip
            }
        }
    }

    /**
     * Tick schedulato: rigenera il blocco con effetti.
     */
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, this.defaultBlockState(), 3);

            // ── Effetti alla rigenerazione ────────────────────────────────
            level.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    30, 0.3, 0.3, 0.3, 0.05);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    20, 0.4, 0.4, 0.4, 0.1);

            level.playSound(null, pos,
                    SoundEvents.BEACON_ACTIVATE,
                    SoundSource.BLOCKS,
                    0.5f, 1.5f);
            level.playSound(null, pos,
                    SoundEvents.AMETHYST_CLUSTER_PLACE,
                    SoundSource.BLOCKS,
                    1.0f, 1.2f);

            OmniBlock.LOGGER.debug("[OmniBlock] Rigenerato in {}", pos);
        } else {
            // Spazio occupato, riprova tra poco
            level.scheduleTick(pos, this, REGEN_TICKS);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && newState.isAir()) {
            level.scheduleTick(pos, this, REGEN_TICKS);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(); // Drop gestito manualmente
    }
}
