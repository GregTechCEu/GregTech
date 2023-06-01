/*
    Copyright 2019, TheLimePixel, dan
    GregBlock Utilities

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package gregtech.integration.exnihilo.recipes;

import exnihilocreatio.ModBlocks;
import exnihilocreatio.ModItems;
import exnihilocreatio.blocks.BlockSieve;
import exnihilocreatio.items.seeds.ItemSeedBase;
import exnihilocreatio.registries.manager.ISieveDefaultRegistryProvider;
import exnihilocreatio.registries.registries.SieveRegistry;
import exnihilocreatio.util.ItemInfo;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.integration.exnihilo.ExNihiloConfig;
import gregtech.integration.exnihilo.ExNihiloModule;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SieveDrops implements ISieveDefaultRegistryProvider {

    private static Map<SieveDropType, List<SieveDrop>> SIEVE_DROPS_MAP = new HashMap<>();

    // TODO Clean this up
    @Override
    public void registerRecipeDefaults(@Nonnull SieveRegistry registry) {
        if (ExNihiloConfig.overrideAllSiftDrops) {
            registry.clearRegistry();

            registry.register("dirt", new ItemInfo(ModItems.pebbles), 1f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ModItems.pebbles), 1f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ModItems.pebbles), 0.5f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ModItems.pebbles), 0.5f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ModItems.pebbles), 0.1f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ModItems.pebbles), 0.1f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ModItems.pebbles, 1), 0.5f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ModItems.pebbles, 1), 0.1f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ModItems.pebbles, 2), 0.5f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ModItems.pebbles, 2), 0.1f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ModItems.pebbles, 3), 0.5f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ModItems.pebbles, 3), 0.1f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles), 0.5f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles), 0.1f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles, 1), 0.5f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles, 1), 0.1f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles, 2), 0.5f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles, 2), 0.1f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles, 3), 0.5f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ExNihiloModule.GTPebbles, 3), 0.1f, BlockSieve.MeshType.STRING.getID());


            registry.register("dirt", new ItemInfo(Items.WHEAT_SEEDS), 0.7f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(Items.MELON_SEEDS), 0.35f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(Items.PUMPKIN_SEEDS), 0.35f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ModItems.resources, 3), 0.05f, BlockSieve.MeshType.STRING.getID());
            registry.register("dirt", new ItemInfo(ModItems.resources, 4), 0.05f, BlockSieve.MeshType.STRING.getID());
            for (ItemSeedBase seed : ModItems.itemSeeds) {
                registry.register("dirt", new ItemInfo(seed), 0.05f, BlockSieve.MeshType.STRING.getID());
            }
        }

        for (Map.Entry<SieveDropType, List<SieveDrop>> drops : SIEVE_DROPS_MAP.entrySet()) {
            OrePrefix prefix = drops.getKey().getPrefix();
            for (SieveDrop drop : drops.getValue()) {
                ItemStack stack = OreDictUnifier.get(prefix, drop.material);
                if (drops.getKey() != SieveDropType.NETHERRACK && drops.getKey() != SieveDropType.END) {
                    registry.register(drops.getKey().getName(), new ItemInfo(stack.getItem(), stack.getMetadata()), drop.chance, drop.level);
                } else {
                    registry.register(new ItemStack(drops.getKey() == SieveDropType.END ? ModBlocks.endstoneCrushed : ModBlocks.netherrackCrushed), new ItemInfo(stack.getItem(), stack.getMetadata()), drop.chance, drop.level);
                }
            }
        }
        SIEVE_DROPS_MAP = null; // can let this get GC'd now
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
