package gregtech.integration.exnihilo.recipes;

import exnihilocreatio.blocks.BlockSieve;
import exnihilocreatio.registries.manager.ExNihiloRegistryManager;
import exnihilocreatio.registries.types.Siftable;
import exnihilocreatio.util.ItemInfo;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.MetaBlocks;
import gregtech.integration.exnihilo.ExNihiloConfig;
import gregtech.integration.exnihilo.ExNihiloModule;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SieveDrops {

    private static Map<SieveDropType, List<SieveDrop>> SIEVE_DROPS_MAP = new HashMap<>();
    public static void registerRecipes() {
        NonNullList<Siftable> siftable = NonNullList.create();
        if (ExNihiloConfig.overrideAllSiftDrops) {
            ExNihiloRegistryManager.SIEVE_REGISTRY.getRegistry().entrySet().stream().anyMatch(entry -> {
                if (entry.getKey().test(new ItemStack(Blocks.DIRT))) {
                    siftable.addAll(entry.getValue());
                    return true;
                }
                return false;
            });
            ExNihiloRegistryManager.SIEVE_REGISTRY.getRegistry().clear();
            ExNihiloRegistryManager.SIEVE_REGISTRY.getRegistry().put(Ingredient.fromStacks(new ItemStack(Blocks.DIRT)), siftable);
            ExNihiloRegistryManager.SIEVE_REGISTRY.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles, 3), 0.1f, BlockSieve.MeshType.STRING.getID());
            ExNihiloRegistryManager.SIEVE_REGISTRY.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles), 0.5f, BlockSieve.MeshType.STRING.getID());
            ExNihiloRegistryManager.SIEVE_REGISTRY.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles), 0.1f, BlockSieve.MeshType.STRING.getID());
            ExNihiloRegistryManager.SIEVE_REGISTRY.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles, 1), 0.5f, BlockSieve.MeshType.STRING.getID());
            ExNihiloRegistryManager.SIEVE_REGISTRY.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles, 1), 0.1f, BlockSieve.MeshType.STRING.getID());
            ExNihiloRegistryManager.SIEVE_REGISTRY.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles, 2), 0.5f, BlockSieve.MeshType.STRING.getID());
            ExNihiloRegistryManager.SIEVE_REGISTRY.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles, 2), 0.1f, BlockSieve.MeshType.STRING.getID());
            ExNihiloRegistryManager.SIEVE_REGISTRY.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles, 3), 0.5f, BlockSieve.MeshType.STRING.getID());
            ExNihiloRegistryManager.SIEVE_REGISTRY.register("dirt", new ItemInfo(MetaBlocks.RUBBER_SAPLING.getBlockState().getBlock()), 0.1f, BlockSieve.MeshType.STRING.getID());
        }
    }

    // TODO Move away from valueOf for GTCEu
    private enum SieveDropType implements IStringSerializable {
        SAND("sand", ExNihiloModule.oreSandyChunk),
        GRAVEL("gravel", ExNihiloModule.oreChunk),
        NETHERRACK("nether", ExNihiloModule.oreNetherChunk),
        END("end", ExNihiloModule.oreEnderChunk),
        GRANITE("crushedGranite", ExNihiloModule.oreChunk),
        DIORITE("crushedDiorite", ExNihiloModule.oreChunk),
        ANDESITE("crushedAndesite", ExNihiloModule.oreChunk);

        private final String name;
        private final OrePrefix prefix;

        SieveDropType(String name, OrePrefix prefix) {
            this.name = name;
            this.prefix = prefix;
        }

        @Override
        @Nonnull
        public String getName() {
            return name;
        }

        public OrePrefix getPrefix() {
            return prefix;
        }
    }

    private static class SieveDrop {
        public Material material;
        public float chance;
        public int level;

        public SieveDrop(Material material, float chance, int level) {
            this.material = material;
            this.chance = chance;
            this.level = level;
        }
    }
}
