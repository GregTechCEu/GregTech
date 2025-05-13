package gregtech.integration.exnihilo.recipes;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.MetaBlocks;
import gregtech.integration.IntegrationModule;
import gregtech.integration.exnihilo.ExNihiloConfig;
import gregtech.integration.exnihilo.ExNihiloModule;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import exnihilocreatio.ModBlocks;
import exnihilocreatio.blocks.BlockSieve;
import exnihilocreatio.registries.manager.ExNihiloRegistryManager;
import exnihilocreatio.registries.types.Siftable;
import exnihilocreatio.util.ItemInfo;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class SieveDrops {

    private static ArrayList<Pair<Ingredient, Siftable>> siftables = new ArrayList<>();

    private static boolean validateDrops(ItemStack output, int meshlevel, float chance) {
        if (output == null) {
            IntegrationModule.logger.error("Output stack is null!", new Throwable());
            return false;
        } else if (output.isEmpty()) {
            IntegrationModule.logger.error("Output stack is empty!", new Throwable());
            return false;
        }
        if (chance > 1F) {
            IntegrationModule.logger.error("Chance for {} can't be higher than 1!", output.getDisplayName(),
                    new Throwable());
            return false;
        }
        if (meshlevel > 4) {
            IntegrationModule.logger.error("Mesh Level for {} out of range!", output.getDisplayName(), new Throwable());
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    public static void removeDrop(ItemStack input, ItemStack output) {
        siftables.removeIf(
                pair -> pair.getKey().test(input) && output.isItemEqual(pair.getValue().getDrop().getItemStack()));
    }

    public static void addDrop(Block input, ItemStack output, int meshLevel, float chance) {
        addDrop(Ingredient.fromStacks(new ItemStack(input)), output, meshLevel, chance);
    }

    @SuppressWarnings("unused")
    public static void addDrop(ItemStack input, ItemStack output, int meshLevel, float chance) {
        if (input.isEmpty()) {
            IntegrationModule.logger.error("Input stack is empty!", new Throwable());
            return;
        }
        addDrop(Ingredient.fromStacks(input), output, meshLevel, chance);
    }

    public static void addDrop(String oredict, ItemStack output, int meshLevel, float chance) {
        List<ItemStack> stacks = OreDictUnifier.getAllWithOreDictionaryName(oredict);
        if (stacks.isEmpty()) {
            IntegrationModule.logger.error("Cannot find oredict {}!", oredict, new Throwable());
            return;
        }
        addDrop(Ingredient.fromStacks(stacks.toArray(new ItemStack[0])), output, meshLevel, chance);
    }

    public static void addDrop(Ingredient ingredient, ItemStack output, int meshLevel, float chance) {
        addDrop(ingredient, new Siftable(new ItemInfo(output), chance, meshLevel));
    }

    public static void addDrop(Ingredient ingredient, Siftable siftable) {
        if (!validateDrops(siftable.getDrop().getItemStack(), siftable.getMeshLevel(), siftable.getChance())) {
            return;
        }
        siftables.add(Pair.of(ingredient, siftable));
    }

    public static void addRecipes() {
        // Dirt
        addDrop("dirt", new ItemStack(ExNihiloModule.GTPebbles, 2), BlockSieve.MeshType.STRING.getID(),
                0.3f);
        addDrop("dirt", new ItemStack(ExNihiloModule.GTPebbles, 2, 1), BlockSieve.MeshType.STRING.getID(),
                0.3f);
        addDrop("dirt", new ItemStack(ExNihiloModule.GTPebbles, 2, 2), BlockSieve.MeshType.STRING.getID(),
                0.3f);
        addDrop("dirt", new ItemStack(ExNihiloModule.GTPebbles, 1, 3), BlockSieve.MeshType.STRING.getID(),
                0.3f);
        addDrop("dirt", new ItemStack(MetaBlocks.RUBBER_SAPLING.getBlockState().getBlock()),
                BlockSieve.MeshType.STRING.getID(),
                0.4f);

        // Crushed Andesite
        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Chalcopyrite), 1,
                0.07F);
        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Coal), 1, 0.08F);
        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Copper), 1, 0.07F);
        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Iron), 1, 0.09F);
        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Pyrite), 1, 0.09F);

        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Coal), 2, 0.23F);
        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Diamond), 2, 0.08F);
        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Graphite), 2, 0.005F);

        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.BandedIron), 3, 0.15F);
        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.BrownLimonite), 3,
                0.18F);
        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Malachite), 3, 0.075F);
        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.YellowLimonite), 3,
                0.18F);

        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Apatite), 4, 0.15F);
        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Pyrochlore), 4,
                0.005F);
        addDrop(ModBlocks.crushedAndesite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.TricalciumPhosphate),
                4, 0.1F);

        // Crushed Diorite
        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Cassiterite), 1, 0.07F);
        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Chalcopyrite), 1, 0.2F);
        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Coal), 1, 0.06F);
        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Zeolite), 1, 0.001F);

        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Calcite), 2, 0.1F);
        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Lapis), 2, 0.25F);
        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Lazurite), 2, 0.001F);
        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Sodalite), 2, 0.001F);

        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Cobaltite), 3, 0.02F);
        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Garnierite), 3, 0.12F);
        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Nickel), 3, 0.12F);
        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Pentlandite), 3, 0.09F);

        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Pentlandite), 4, 0.06F);
        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.GlauconiteSand), 4,
                0.04F);
        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Soapstone), 4, 0.16F);
        addDrop(ModBlocks.crushedDiorite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Talc), 4, 0.16F);

        // Crushed Granite
        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Coal), 1, 0.12F);
        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Gold), 1, 0.05F);
        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Magnetite), 1, 0.25F);
        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.VanadiumMagnetite), 1,
                0.005F);

        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Cinnabar), 2, 0.005F);
        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Redstone), 2, 0.3F);
        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Ruby), 2, 0.08F);

        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Bentonite), 3, 0.001F);
        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.GlauconiteSand), 3,
                0.04F);
        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Magnesite), 3, 0.005F);
        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Olivine), 3, 0.025F);

        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Grossular), 4, 0.001F);
        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Pyrolusite), 4, 0.1F);
        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Spessartine), 4,
                0.001F);
        addDrop(ModBlocks.crushedGranite, OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Tantalite), 4, 0.005F);

        // Gravel
        addDrop("gravel", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Cassiterite), 1, 0.05F);
        addDrop("gravel", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Coal), 1, 0.1F);
        addDrop("gravel", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Tin), 1, 0.2F);

        addDrop("gravel", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Galena), 2, 0.16F);
        addDrop("gravel", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Lead), 2, 0.08F);
        addDrop("gravel", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Silver), 2, 0.1F);

        addDrop("gravel", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Lepidolite), 3, 0.005F);
        addDrop("gravel", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.RockSalt), 3, 0.12F);
        addDrop("gravel", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Salt), 3, 0.2F);
        addDrop("gravel", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Spodumene), 3, 0.001F);

        addDrop("gravel", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Bauxite), 4, 0.3F);
        addDrop("gravel", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Kyanite), 4, 0.001F);
        addDrop("gravel", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Mica), 4, 0.005F);
        addDrop("gravel", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Pollucite), 4, 0.001F);

        // Sand
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.BasalticMineralSand), 2, 0.1F);
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.CassiteriteSand), 2, 0.12F);
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Diatomite), 2, 0.001F);
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Asbestos), 2, 0.001F);
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.GarnetSand), 2, 0.001F);
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.GraniticMineralSand), 2, 0.1F);
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Gypsum), 2, 0.1F);

        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Almandine), 3, 0.001F);
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Amethyst), 3, 0.001F);
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.GreenSapphire), 3, 0.15F);
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Opal), 3, 0.001F);
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Pyrope), 3, 0.001F);
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.GarnetRed), 3, 0.001F);
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Sapphire), 3, 0.35F);
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.GarnetYellow), 3, 0.001F);
        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Realgar), 3, 0.15F);

        addDrop("sand", OreDictUnifier.get(ExNihiloModule.oreChunk, Materials.Oilsands), 4, 0.3F);

        // Crushed Netherrack
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.BandedIron), 1,
                0.2F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.BrownLimonite),
                1, 0.2F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Copper), 1,
                0.2F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Gold), 1,
                0.1F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Stibnite), 1,
                0.005F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Tetrahedrite),
                1, 0.2F);
        addDrop(ModBlocks.netherrackCrushed,
                OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.YellowLimonite), 1, 0.2F);

        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Barite), 2,
                0.005F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.CertusQuartz),
                2, 0.25F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.NetherQuartz),
                2, 0.25F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Pyrite), 2,
                0.15F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Quartzite), 2,
                0.1F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Sphalerite), 2,
                0.05F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Sulfur), 2,
                0.15F);

        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Beryllium), 3,
                0.005F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.BlueTopaz), 3,
                0.075F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Bornite), 3,
                0.1F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Chalcocite), 3,
                0.1F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Emerald), 3,
                0.1F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Thorium), 3,
                0.001F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Topaz), 3,
                0.1F);

        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Bastnasite), 4,
                0.1F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Electrotine),
                4, 0.005F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Molybdenite),
                4, 0.06F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Monazite), 4,
                0.035F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Neodymium), 4,
                0.005F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Powellite), 4,
                0.03F);
        addDrop(ModBlocks.netherrackCrushed, OreDictUnifier.get(ExNihiloModule.oreNetherChunk, Materials.Wulfenite), 4,
                0.06F);

        // Crushed Endstone
        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Aluminium), 2,
                0.25F);
        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Bauxite), 2,
                0.25F);
        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Chromite), 2,
                0.3F);
        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Gold), 2, 0.2F);
        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Ilmenite), 2,
                0.25F);
        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Magnetite), 2,
                0.25F);
        addDrop(ModBlocks.endstoneCrushed,
                OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.VanadiumMagnetite), 2, 0.15F);

        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Bornite), 3,
                0.05F);
        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Cooperite), 3,
                0.05F);
        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Lithium), 3,
                0.05F);
        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Palladium), 3,
                0.005F);
        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Platinum), 3,
                0.005F);
        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Scheelite), 3,
                0.25F);
        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Tungstate), 3,
                0.25F);

        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Naquadah), 4,
                0.001F);
        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Pitchblende), 4,
                0.125F);
        addDrop(ModBlocks.endstoneCrushed, OreDictUnifier.get(ExNihiloModule.oreEnderChunk, Materials.Uraninite), 4,
                0.1F);
    }

    public static void registerRecipes() {
        // If all recipes are getting removed, backup and readd the dirt sifting table so a void world playthrough is
        // doable.
        if (ExNihiloConfig.overrideAllSiftDrops) {
            List<Siftable> siftablesDirt = ExNihiloRegistryManager.SIEVE_REGISTRY.getDrops(OreDictUnifier.get("dirt"));
            ExNihiloRegistryManager.SIEVE_REGISTRY.getRegistry().clear();
            ExNihiloRegistryManager.SIEVE_REGISTRY.register(
                    Ingredient.fromStacks(OreDictUnifier.getAllWithOreDictionaryName("dirt").toArray(new ItemStack[0])),
                    siftablesDirt);
        }
        for (Pair<Ingredient, Siftable> pair : siftables) {
            ExNihiloRegistryManager.SIEVE_REGISTRY.register(pair.getKey(), pair.getValue());
        }
        siftables = null; // let this get GC'd, no more eating my memory
    }
}
