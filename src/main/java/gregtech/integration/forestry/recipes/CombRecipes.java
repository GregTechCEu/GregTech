package gregtech.integration.forestry.recipes;

import com.google.common.collect.ImmutableMap;
import forestry.api.recipes.ICentrifugeRecipe;
import forestry.api.recipes.RecipeManagers;
import forestry.core.ModuleCore;
import forestry.factory.MachineUIDs;
import forestry.factory.ModuleFactory;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.common.items.MetaItems;
import gregtech.integration.IntegrationUtil;
import gregtech.integration.forestry.ForestryConfig;
import gregtech.integration.forestry.ForestryUtil;
import gregtech.integration.forestry.bees.GTCombType;
import gregtech.integration.forestry.bees.GTDropType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;

import java.util.Arrays;
import java.util.Map;

public class CombRecipes {

    public static void initForestryCombs() {
        if (!ModuleFactory.machineEnabled(MachineUIDs.CENTRIFUGE)) return;
        for (ICentrifugeRecipe recipe : RecipeManagers.centrifugeManager.recipes()) {
            ItemStack combStack = recipe.getInput();
            if (combStack.getItem().getRegistryName().getNamespace().equals(GTValues.MODID)) continue;
            RecipeBuilder<?> builder = RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                    .inputs(combStack.copy())
                    .duration(Voltage.ULV.getCentrifugeTime()).EUt(Voltage.ULV.getCentrifugeEnergy());

            boolean hadSomeOutput = false;
            for (Map.Entry<ItemStack, Float> entry : recipe.getAllProducts().entrySet()) {
                hadSomeOutput = true;
                if (entry.getValue() >= 1.0f) {
                    builder.outputs(entry.getKey());
                } else {
                    builder.chancedOutput(entry.getKey(), Math.max(1, Math.round(entry.getValue() * 10000)), 0);
                }
            }
            if (hadSomeOutput) {
                // For some reason Forestry occasionally has recipes that have no outputs at all, which will
                // cause us to error. Discard these if we come across them.
                builder.buildAndRegister();
            }
        }
    }

    // forgive me for the code i am about to write
    public static void initGTCombs() {
        // Organic
        addProcessGT(GTCombType.COAL, new Material[]{Materials.Coal}, Voltage.LV);
        addProcessGT(GTCombType.COKE, new Material[]{Materials.Coke}, Voltage.LV);
        addCentrifugeToItemStack(GTCombType.STICKY, new ItemStack[]{MetaItems.STICKY_RESIN.getStackForm(), MetaItems.PLANT_BALL.getStackForm(), ModuleCore.getItems().beeswax.getItemStack()}, new int[]{50 * 100, 15 * 100, 50 * 100}, Voltage.ULV);
        addProcessGT(GTCombType.OIL, new Material[]{Materials.Oilsands}, Voltage.LV);
        addProcessGT(GTCombType.APATITE, new Material[]{Materials.Apatite, Materials.Phosphate}, Voltage.LV);
        addCentrifugeToMaterial(GTCombType.ASH, new Material[]{Materials.DarkAsh, Materials.Ash}, new int[]{50 * 100, 50 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 50 * 100);
        addCentrifugeToItemStack(GTCombType.BIOMASS, new ItemStack[]{ForestryUtil.getDropStack(GTDropType.BIOMASS), ForestryUtil.getDropStack(GTDropType.ETHANOL), ModuleCore.getItems().beeswax.getItemStack()}, new int[]{70 * 100, 30 * 100, 50 * 100}, Voltage.ULV);
        if (ForestryConfig.harderGTCombRecipes) {
            addCentrifugeToItemStack(GTCombType.COAL, new ItemStack[]{OreDictUnifier.get(OrePrefix.gem, Materials.Coal), ModuleCore.getItems().beeswax.getItemStack()}, new int[]{5 * 100, 50 * 100}, Voltage.ULV);
            addCentrifugeToItemStack(GTCombType.COKE, new ItemStack[]{OreDictUnifier.get(OrePrefix.gem, Materials.Coke), ModuleCore.getItems().beeswax.getItemStack()}, new int[]{5 * 100, 50 * 100}, Voltage.ULV);
            // TODO the recipe below gave 1 oilberry instead of oilsands dust. change to that if oilberry balance stays the same in the new crop system
            addCentrifugeToItemStack(GTCombType.OIL, new ItemStack[]{OreDictUnifier.get(OrePrefix.dustTiny, Materials.Oilsands), ForestryUtil.getDropStack(GTDropType.OIL), ModuleCore.getItems().beeswax.getItemStack()}, new int[]{70 * 100, 100 * 100, 50 * 100}, Voltage.ULV);
        } else {
            addCentrifugeToItemStack(GTCombType.COAL, new ItemStack[]{OreDictUnifier.get(OrePrefix.gem, Materials.Coal), OreDictUnifier.get(OrePrefix.dustTiny, Materials.Coal), ModuleCore.getItems().beeswax.getItemStack()}, new int[]{5 * 100, 100 * 100, 50 * 100}, Voltage.ULV);
            addCentrifugeToItemStack(GTCombType.COKE, new ItemStack[]{OreDictUnifier.get(OrePrefix.gem, Materials.Coke), OreDictUnifier.get(OrePrefix.dustTiny, Materials.Coke), ModuleCore.getItems().beeswax.getItemStack()}, new int[]{5 * 100, 100 * 100, 50 * 100}, Voltage.ULV);
            addCentrifugeToItemStack(GTCombType.OIL, new ItemStack[]{OreDictUnifier.get(OrePrefix.dustSmall, Materials.Oilsands), ForestryUtil.getDropStack(GTDropType.OIL), ModuleCore.getItems().beeswax.getItemStack()}, new int[]{70 * 100, 100 * 100, 50 * 100}, Voltage.ULV);
            addCentrifugeToMaterial(GTCombType.APATITE, new Material[]{Materials.Apatite, Materials.Phosphate}, new int[]{100 * 100, 80 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
        }

        // Industrial
        if (Loader.isModLoaded(GTValues.MODID_EB)) {
            addCentrifugeToItemStack(GTCombType.ENERGY, new ItemStack[]{MetaItems.ENERGIUM_DUST.getStackForm(), ModuleCore.getItems().refractoryWax.getItemStack()}, new int[]{20 * 100, 50 * 100}, Voltage.HV, 196);
            ItemStack wax = ModuleCore.getItems().beeswax.getItemStack();
            if (Loader.isModLoaded(GTValues.MODID_MB)) {
                wax = IntegrationUtil.getModItem(GTValues.MODID_MB, "wax", 2);
            }
            addCentrifugeToItemStack(GTCombType.LAPOTRON, new ItemStack[]{OreDictUnifier.get(OrePrefix.dust, Materials.Lapotron), wax}, new int[]{15 * 100, 40 * 100}, Voltage.HV, 196);
        }

        // Alloy
        addProcessGT(GTCombType.REDALLOY, new Material[]{Materials.RedAlloy, Materials.Redstone, Materials.Copper}, Voltage.LV);
        addProcessGT(GTCombType.STAINLESSSTEEL, new Material[]{Materials.StainlessSteel, Materials.Iron, Materials.Chrome, Materials.Manganese, Materials.Nickel}, Voltage.HV);
        // redstone alloy, conductive iron, vibrant alloy, energetic alloy, electrical steel, dark steel, pulsating iron, enderium
        if (ForestryConfig.harderGTCombRecipes) {
            addCentrifugeToMaterial(GTCombType.REDALLOY, new Material[]{Materials.RedAlloy}, new int[]{100 * 100}, new int[]{}, Voltage.ULV, ModuleCore.getItems().refractoryWax.getItemStack(), 50 * 100);
            addCentrifugeToMaterial(GTCombType.STAINLESSSTEEL, new Material[]{Materials.StainlessSteel}, new int[]{50 * 100}, new int[]{}, Voltage.HV, ModuleCore.getItems().refractoryWax.getItemStack(), 50 * 100);
        } else {
            addCentrifugeToMaterial(GTCombType.REDALLOY, new Material[]{Materials.RedAlloy, Materials.Redstone, Materials.Copper}, new int[]{100 * 100, 75 * 100, 90 * 100}, new int[]{}, Voltage.ULV, ModuleCore.getItems().refractoryWax.getItemStack(), 50 * 100);
            addCentrifugeToMaterial(GTCombType.STAINLESSSTEEL, new Material[]{Materials.StainlessSteel, Materials.Iron, Materials.Chrome, Materials.Manganese, Materials.Nickel}, new int[]{50 * 100, 75 * 100, 55 * 100, 75 * 100, 75 * 100}, new int[]{}, Voltage.HV, ModuleCore.getItems().refractoryWax.getItemStack(), 50 * 100);
        }

        // Gem
        addProcessGT(GTCombType.STONE, new Material[]{Materials.Soapstone, Materials.Talc, Materials.Apatite, Materials.Phosphate, Materials.TricalciumPhosphate}, Voltage.LV);
        addProcessGT(GTCombType.CERTUS, new Material[]{Materials.CertusQuartz, Materials.Quartzite, Materials.Barite}, Voltage.LV);
        addProcessGT(GTCombType.REDSTONE, new Material[]{Materials.Redstone, Materials.Cinnabar}, Voltage.LV);
        addCentrifugeToMaterial(GTCombType.RAREEARTH, new Material[]{Materials.RareEarth}, new int[]{100 * 100}, new int[]{1}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
        addProcessGT(GTCombType.LAPIS, new Material[]{Materials.Lapis, Materials.Sodalite, Materials.Lazurite, Materials.Calcite}, Voltage.LV);
        addProcessGT(GTCombType.RUBY, new Material[]{Materials.Ruby, Materials.Redstone}, Voltage.LV);
        addProcessGT(GTCombType.REDGARNET, new Material[]{Materials.GarnetRed, Materials.GarnetYellow}, Voltage.LV);
        addProcessGT(GTCombType.YELLOWGARNET, new Material[]{Materials.GarnetYellow, Materials.GarnetRed}, Voltage.LV);
        addProcessGT(GTCombType.SAPPHIRE, new Material[]{Materials.Sapphire, Materials.GreenSapphire, Materials.Almandine, Materials.Pyrope}, Voltage.LV);
        addProcessGT(GTCombType.DIAMOND, new Material[]{Materials.Diamond, Materials.Graphite}, Voltage.LV);
        addProcessGT(GTCombType.OLIVINE, new Material[]{Materials.Olivine, Materials.Bentonite, Materials.Magnesite, Materials.GlauconiteSand}, Voltage.LV);
        addProcessGT(GTCombType.EMERALD, new Material[]{Materials.Emerald, Materials.Beryllium, Materials.Thorium}, Voltage.LV);
        addProcessGT(GTCombType.PYROPE, new Material[]{Materials.Pyrope, Materials.Aluminium, Materials.Magnesium, Materials.Silicon}, Voltage.LV);
        addProcessGT(GTCombType.GROSSULAR, new Material[]{Materials.Grossular, Materials.Aluminium, Materials.Silicon}, Voltage.LV);
        if (ForestryConfig.harderGTCombRecipes) {
            addCentrifugeToMaterial(GTCombType.STONE, new Material[]{Materials.Stone, Materials.GraniteBlack, Materials.GraniteRed, Materials.Basalt, Materials.Marble, Materials.Redrock}, new int[]{70 * 100, 50 * 100, 50 * 100, 50 * 100, 50 * 100, 50 * 100}, new int[]{9, 9, 9, 9, 9, 9}, Voltage.ULV, ItemStack.EMPTY, 50 * 100);
        } else {
            addCentrifugeToMaterial(GTCombType.STONE, new Material[]{Materials.Soapstone, Materials.Talc, Materials.Apatite, Materials.Phosphate, Materials.TricalciumPhosphate}, new int[]{95 * 100, 90 * 100, 80 * 100, 75 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 50 * 100);
            addCentrifugeToMaterial(GTCombType.CERTUS, new Material[]{Materials.CertusQuartz, Materials.Quartzite, Materials.Barite}, new int[]{100 * 100, 80 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 50 * 100);
            addCentrifugeToMaterial(GTCombType.REDSTONE, new Material[]{Materials.Redstone, Materials.Cinnabar}, new int[]{100 * 100, 80 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.LAPIS, new Material[]{Materials.Lapis, Materials.Sodalite, Materials.Lazurite, Materials.Calcite}, new int[]{100 * 100, 90 * 100, 90 * 100, 85 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.RUBY, new Material[]{Materials.Ruby, Materials.Redstone}, new int[]{100 * 100, 90 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.REDGARNET, new Material[]{Materials.GarnetRed, Materials.GarnetYellow}, new int[]{100 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.YELLOWGARNET, new Material[]{Materials.GarnetYellow, Materials.GarnetRed}, new int[]{100 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.SAPPHIRE, new Material[]{Materials.Sapphire, Materials.GreenSapphire, Materials.Almandine, Materials.Pyrope}, new int[]{100 * 100, 90 * 100, 90 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.DIAMOND, new Material[]{Materials.Diamond, Materials.Graphite}, new int[]{100 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.OLIVINE, new Material[]{Materials.Olivine, Materials.Bentonite, Materials.Magnesite, Materials.GlauconiteSand}, new int[]{100 * 100, 90 * 100, 80 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.EMERALD, new Material[]{Materials.Emerald, Materials.Beryllium, Materials.Thorium}, new int[]{100 * 100, 85 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.PYROPE, new Material[]{Materials.Pyrope, Materials.Aluminium, Materials.Magnesium, Materials.Silicon}, new int[]{100 * 100, 75 * 100, 80 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.GROSSULAR, new Material[]{Materials.Grossular, Materials.Aluminium, Materials.Silicon}, new int[]{100 * 100, 75 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
        }

        // Metals
        addProcessGT(GTCombType.SLAG, new Material[]{Materials.Salt, Materials.RockSalt, Materials.Lepidolite, Materials.Spodumene, Materials.Monazite}, Voltage.LV);
        addProcessGT(GTCombType.COPPER, new Material[]{Materials.Copper, Materials.Tetrahedrite, Materials.Chalcopyrite, Materials.Malachite, Materials.Pyrite, Materials.Stibnite}, Voltage.LV);
        addProcessGT(GTCombType.TIN, new Material[]{Materials.Tin, Materials.Cassiterite, Materials.CassiteriteSand}, Voltage.LV);
        addProcessGT(GTCombType.LEAD, new Material[]{Materials.Lead, Materials.Galena}, Voltage.LV);
        addProcessGT(GTCombType.NICKEL, new Material[]{Materials.Nickel, Materials.Garnierite, Materials.Pentlandite, Materials.Cobaltite, Materials.Wulfenite, Materials.Powellite}, Voltage.LV);
        addProcessGT(GTCombType.ZINC, new Material[]{Materials.Zinc, Materials.Sulfur}, Voltage.LV);
        addProcessGT(GTCombType.SILVER, new Material[]{Materials.Silver, Materials.Galena}, Voltage.LV);
        addProcessGT(GTCombType.GOLD, new Material[]{Materials.Gold, Materials.Magnetite}, Voltage.LV);
        addChemicalProcess(GTCombType.GOLD, Materials.Magnetite, Materials.Gold, Voltage.LV);
        addProcessGT(GTCombType.SULFUR, new Material[]{Materials.Sulfur, Materials.Pyrite, Materials.Sphalerite}, Voltage.LV);
        addProcessGT(GTCombType.GALLIUM, new Material[]{Materials.Gallium, Materials.Niobium}, Voltage.LV);
        addProcessGT(GTCombType.ARSENIC, new Material[]{Materials.Arsenic, Materials.Bismuth, Materials.Antimony}, Voltage.LV);
        addProcessGT(GTCombType.IRON, new Material[]{Materials.Iron, Materials.Magnetite, Materials.BrownLimonite, Materials.YellowLimonite, Materials.VanadiumMagnetite, Materials.BandedIron, Materials.Pyrite}, Voltage.LV);
        addProcessGT(GTCombType.STEEL, new Material[]{Materials.Iron, Materials.Magnetite, Materials.YellowLimonite, Materials.BrownLimonite, Materials.VanadiumMagnetite, Materials.BandedIron, Materials.Pyrite, Materials.Molybdenite, Materials.Molybdenum}, Voltage.LV);

        addChemicalProcess(GTCombType.STEEL, Materials.BrownLimonite, Materials.YellowLimonite, Voltage.LV);
        addChemicalProcess(GTCombType.STEEL, Materials.YellowLimonite, Materials.BrownLimonite, Voltage.LV);
        if (ForestryConfig.harderGTCombRecipes) {
            addCentrifugeToMaterial(GTCombType.SLAG, new Material[]{Materials.Stone, Materials.GraniteBlack, Materials.GraniteRed}, new int[]{50 * 100, 20 * 100, 20 * 100}, new int[]{9, 9, 9}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.COPPER, new Material[]{Materials.Copper}, new int[]{70 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.TIN, new Material[]{Materials.Tin}, new int[]{60 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.LEAD, new Material[]{Materials.Lead}, new int[]{45 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.IRON, new Material[]{Materials.Iron}, new int[]{30 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.STEEL, new Material[]{Materials.Steel}, new int[]{40 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.SILVER, new Material[]{Materials.Silver}, new int[]{80 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
        } else {
            addCentrifugeToMaterial(GTCombType.SLAG, new Material[]{Materials.Salt, Materials.RockSalt, Materials.Lepidolite, Materials.Spodumene, Materials.Monazite}, new int[]{100 * 100, 100 * 100, 100 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.COPPER, new Material[]{Materials.Copper, Materials.Tetrahedrite, Materials.Chalcopyrite, Materials.Malachite, Materials.Pyrite, Materials.Stibnite}, new int[]{100 * 100, 85 * 100, 95 * 100, 80 * 100, 75 * 100, 65 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.TIN, new Material[]{Materials.Tin, Materials.Cassiterite, Materials.CassiteriteSand}, new int[]{100 * 100, 85 * 100, 65 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.LEAD, new Material[]{Materials.Lead, Materials.Galena}, new int[]{100 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.IRON, new Material[]{Materials.Iron, Materials.Magnetite, Materials.BrownLimonite, Materials.YellowLimonite, Materials.VanadiumMagnetite, Materials.BandedIron}, new int[]{100 * 100, 90 * 100, 85 * 100, 85 * 100, 80 * 100, 85 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.STEEL, new Material[]{Materials.Steel, Materials.Magnetite, Materials.VanadiumMagnetite, Materials.BandedIron, Materials.Molybdenite, Materials.Molybdenum}, new int[]{100 * 100, 90 * 100, 80 * 100, 85 * 100, 65 * 100, 65 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);

            addCentrifugeToMaterial(GTCombType.NICKEL, new Material[]{Materials.Nickel, Materials.Garnierite, Materials.Pentlandite, Materials.Cobaltite, Materials.Wulfenite, Materials.Powellite}, new int[]{100 * 100, 85 * 100, 85 * 100, 80 * 100, 75 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.ZINC, new Material[]{Materials.Zinc, Materials.Sphalerite, Materials.Sulfur}, new int[]{100 * 100, 80 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.SILVER, new Material[]{Materials.Silver, Materials.Galena}, new int[]{100 * 100, 80 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.GOLD, new Material[]{Materials.Gold}, new int[]{100 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.SULFUR, new Material[]{Materials.Sulfur, Materials.Pyrite, Materials.Sphalerite}, new int[]{100 * 100, 90 * 100, 80 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.GALLIUM, new Material[]{Materials.Gallium, Materials.Niobium}, new int[]{80 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.ARSENIC, new Material[]{Materials.Arsenic, Materials.Bismuth, Materials.Antimony}, new int[]{80 * 100, 70 * 100, 70 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
        }

        // Rare Metals
        addProcessGT(GTCombType.BAUXITE, new Material[]{Materials.Bauxite, Materials.Aluminium}, Voltage.LV);
        addProcessGT(GTCombType.ALUMINIUM, new Material[]{Materials.Aluminium, Materials.Bauxite}, Voltage.LV);
        addProcessGT(GTCombType.MANGANESE, new Material[]{Materials.Manganese, Materials.Grossular, Materials.Spessartine, Materials.Pyrolusite, Materials.Tantalite}, Voltage.LV);
        addProcessGT(GTCombType.TITANIUM, new Material[]{Materials.Titanium, Materials.Ilmenite, Materials.Bauxite, Materials.Rutile}, Voltage.EV);
        addProcessGT(GTCombType.MAGNESIUM, new Material[]{Materials.Magnesium, Materials.Magnesite}, Voltage.LV);
        addProcessGT(GTCombType.CHROME, new Material[]{Materials.Chrome, Materials.Ruby, Materials.Chromite, Materials.Redstone, Materials.Neodymium, Materials.Bastnasite}, Voltage.HV);
        addProcessGT(GTCombType.TUNGSTEN, new Material[]{Materials.Tungsten, Materials.Tungstate, Materials.Scheelite, Materials.Lithium}, Voltage.IV);
        addProcessGT(GTCombType.PLATINUM, new Material[]{Materials.Platinum, Materials.Cooperite, Materials.Palladium}, Voltage.HV);
        addProcessGT(GTCombType.MOLYBDENUM, new Material[]{Materials.Molybdenum, Materials.Molybdenite, Materials.Powellite, Materials.Wulfenite}, Voltage.LV);
        addChemicalProcess(GTCombType.MOLYBDENUM, Materials.Osmium, Materials.Osmium, Voltage.IV);
        addAutoclaveProcess(GTCombType.MOLYBDENUM, Materials.Osmium, Voltage.IV, 5);
        addProcessGT(GTCombType.IRIDIUM, new Material[]{Materials.Iridium, Materials.Osmium}, Voltage.IV);
        addProcessGT(GTCombType.OSMIUM, new Material[]{Materials.Osmium, Materials.Iridium}, Voltage.IV);
        addProcessGT(GTCombType.LITHIUM, new Material[]{Materials.Lithium, Materials.Aluminium}, Voltage.MV);
        addProcessGT(GTCombType.SALT, new Material[]{Materials.Salt, Materials.RockSalt, Materials.Saltpeter}, Voltage.MV);
        addProcessGT(GTCombType.ELECTROTINE, new Material[]{Materials.Electrotine, Materials.Electrum, Materials.Redstone}, Voltage.MV);

        if (ForestryConfig.harderGTCombRecipes) {
            addCentrifugeToMaterial(GTCombType.SALT, new Material[]{Materials.Salt}, new int[]{100 * 100}, new int[]{9}, Voltage.MV, 160, ItemStack.EMPTY, 50 * 100);
        } else {
            addCentrifugeToMaterial(GTCombType.BAUXITE, new Material[]{Materials.Bauxite, Materials.Aluminium}, new int[]{75 * 100, 55 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.ALUMINIUM, new Material[]{Materials.Aluminium, Materials.Bauxite}, new int[]{60 * 100, 80 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.MANGANESE, new Material[]{Materials.Manganese, Materials.Grossular, Materials.Spessartine, Materials.Pyrolusite, Materials.Tantalite}, new int[]{30 * 100, 100 * 100, 100 * 100, 100 * 100, 100 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.TITANIUM, new Material[]{Materials.Titanium, Materials.Ilmenite, Materials.Bauxite, Materials.Rutile}, new int[]{90 * 100, 80 * 100, 75 * 100, 75 * 100}, new int[]{}, Voltage.EV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.MAGNESIUM, new Material[]{Materials.Magnesium, Materials.Magnesite}, new int[]{100 * 100, 80 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.CHROME, new Material[]{Materials.Chrome, Materials.Ruby, Materials.Chromite, Materials.Redstone, Materials.Neodymium, Materials.Bastnasite}, new int[]{50 * 100, 100 * 100, 50 * 100, 100 * 100, 80 * 100, 80 * 100}, new int[]{}, Voltage.HV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.TUNGSTEN, new Material[]{Materials.Tungsten, Materials.Tungstate, Materials.Scheelite, Materials.Lithium}, new int[]{50 * 100, 80 * 100, 75 * 100, 75 * 100}, new int[]{}, Voltage.IV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.PLATINUM, new Material[]{Materials.Platinum, Materials.Cooperite, Materials.Palladium}, new int[]{40 * 100, 40 * 100, 40 * 100}, new int[]{}, Voltage.HV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.MOLYBDENUM, new Material[]{Materials.Molybdenum, Materials.Molybdenite, Materials.Powellite, Materials.Wulfenite}, new int[]{100 * 100, 80 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.IRIDIUM, new Material[]{Materials.Iridium, Materials.Osmium}, new int[]{20 * 100, 15 * 100}, new int[]{}, Voltage.IV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.OSMIUM, new Material[]{Materials.Osmium, Materials.Iridium}, new int[]{25 * 100, 15 * 100}, new int[]{}, Voltage.IV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.LITHIUM, new Material[]{Materials.Lithium, Materials.Aluminium}, new int[]{85 * 100, 75 * 100}, new int[]{}, Voltage.MV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.SALT, new Material[]{Materials.Salt, Materials.RockSalt, Materials.Saltpeter}, new int[]{100 * 100, 75 * 100, 65 * 100}, new int[]{9, 4, 4}, Voltage.MV, 160, ItemStack.EMPTY, 50 * 100);
            addCentrifugeToMaterial(GTCombType.ELECTROTINE, new Material[]{Materials.Electrotine, Materials.Electrum, Materials.Redstone}, new int[]{80, 75, 65}, new int[]{}, Voltage.MV, ItemStack.EMPTY, 30 * 100);
        }

        // Radioactive
        addProcessGT(GTCombType.ALMANDINE, new Material[]{Materials.Almandine, Materials.Pyrope, Materials.Sapphire, Materials.GreenSapphire}, Voltage.LV);
        addProcessGT(GTCombType.URANIUM, new Material[]{Materials.Uranium238, Materials.Pitchblende, Materials.Uraninite, Materials.Uranium235}, Voltage.EV);
        addProcessGT(GTCombType.PLUTONIUM, new Material[]{Materials.Plutonium239, Materials.Uranium235}, Voltage.EV);
        addChemicalProcess(GTCombType.PLUTONIUM, Materials.Uranium235, Materials.Plutonium239, Voltage.EV);
        addProcessGT(GTCombType.NAQUADAH, new Material[]{Materials.Naquadah, Materials.NaquadahEnriched, Materials.Naquadria}, Voltage.IV);
        addProcessGT(GTCombType.NAQUADRIA, new Material[]{Materials.Naquadria, Materials.NaquadahEnriched, Materials.Naquadah}, Voltage.LUV);
        addProcessGT(GTCombType.THORIUM, new Material[]{Materials.Thorium, Materials.Uranium238, Materials.Coal}, Voltage.EV);
        addProcessGT(GTCombType.LUTETIUM, new Material[]{Materials.Lutetium, Materials.Thorium}, Voltage.IV);
        addProcessGT(GTCombType.AMERICIUM, new Material[]{Materials.Americium, Materials.Lutetium}, Voltage.LUV);
        addProcessGT(GTCombType.NEUTRONIUM, new Material[]{Materials.Neutronium, Materials.Americium}, Voltage.UV);
        if (!ForestryConfig.harderGTCombRecipes) {
            addCentrifugeToMaterial(GTCombType.ALMANDINE, new Material[]{Materials.Almandine, Materials.Pyrope, Materials.Sapphire, Materials.GreenSapphire}, new int[]{90 * 100, 80 * 100, 75 * 100, 75 * 100}, new int[]{}, Voltage.ULV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.URANIUM, new Material[]{Materials.Uranium238, Materials.Pitchblende, Materials.Uraninite, Materials.Uranium235}, new int[]{50 * 100, 65 * 100, 75 * 100, 50 * 100}, new int[]{}, Voltage.EV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.PLUTONIUM, new Material[]{Materials.Plutonium239, Materials.Uranium235}, new int[]{10, 5}, new int[]{}, Voltage.EV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.NAQUADAH, new Material[]{Materials.Naquadah, Materials.NaquadahEnriched, Materials.Naquadria}, new int[]{10 * 100, 5 * 100, 5 * 100}, new int[]{}, Voltage.IV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.NAQUADRIA, new Material[]{Materials.Naquadria, Materials.NaquadahEnriched, Materials.Naquadah}, new int[]{10 * 100, 10 * 100, 15 * 100}, new int[]{}, Voltage.LUV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.THORIUM, new Material[]{Materials.Thorium, Materials.Uranium238, Materials.Coal}, new int[]{75 * 100, 75 * 100, 95 * 100}, new int[]{}, Voltage.EV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.LUTETIUM, new Material[]{Materials.Lutetium, Materials.Thorium}, new int[]{35 * 100, 55 * 100}, new int[]{}, Voltage.IV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.AMERICIUM, new Material[]{Materials.Americium, Materials.Lutetium}, new int[]{25 * 100, 45 * 100}, new int[]{}, Voltage.LUV, ItemStack.EMPTY, 30 * 100);
            addCentrifugeToMaterial(GTCombType.NEUTRONIUM, new Material[]{Materials.Neutronium, Materials.Americium}, new int[]{15 * 100, 35 * 100}, new int[]{}, Voltage.UV, ItemStack.EMPTY, 30 * 100);
        }

        if (Loader.isModLoaded(GTValues.MODID_MB)) {
            addProcessGT(GTCombType.SPARKLING, new Material[]{Materials.NetherStar}, Voltage.EV);
            addCentrifugeToItemStack(GTCombType.SPARKLING, new ItemStack[]{IntegrationUtil.getModItem(GTValues.MODID_MB, "wax", 0), IntegrationUtil.getModItem(GTValues.MODID_MB, "resource", 5), OreDictUnifier.get(OrePrefix.dustTiny, Materials.NetherStar)}, new int[]{50 * 100, 10 * 100, (ForestryConfig.harderGTCombRecipes ? 10 : 50) * 100}, Voltage.EV);
        }
    }

    /**
     * Currently used separately for STEEL, GOLD, MOLYBDENUM, PLUTONIUM
     **/
    private static void addChemicalProcess(GTCombType comb, Material inMaterial, Material outMaterial, Voltage volt) {
        if (OreDictUnifier.get(OrePrefix.crushedPurified, outMaterial, 4).isEmpty() || OreDictUnifier.get(OrePrefix.crushed, inMaterial).isEmpty() || inMaterial.hasFlag(MaterialFlags.DISABLE_ORE_BLOCK))
            return;

        RecipeBuilder<?> builder = RecipeMaps.CHEMICAL_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(9, ForestryUtil.getCombStack(comb)))
                .input(OrePrefix.crushed, inMaterial)
                .fluidInputs(volt.getFluid())
                .output(OrePrefix.crushedPurified, outMaterial, 4)
                .duration(volt.getChemicalTime())
                .EUt(volt.getChemicalEnergy());

        OreProperty property = inMaterial.getProperty(PropertyKey.ORE);
        if (property != null && !property.getOreByProducts().isEmpty()) {
            Material byproduct = property.getOreByProducts().get(0);
            if (byproduct != null && byproduct.hasProperty(PropertyKey.FLUID)) {
                builder.fluidOutputs(byproduct.getFluid(GTValues.L));
            }
        }

        if (volt.compareTo(Voltage.IV) > 0) {
            builder.cleanroom(CleanroomType.CLEANROOM);
        }
        builder.buildAndRegister();
    }

    /**
     * Currently only used separately for GTCombType.MOLYBDENUM
     *
     * @param circuitNumber should not conflict with addProcessGT
     **/
    private static void addAutoclaveProcess(GTCombType comb, Material material, Voltage volt, int circuitNumber) {
        if (OreDictUnifier.get(OrePrefix.crushedPurified, material, 4).isEmpty()) return;

        RecipeBuilder<?> builder = RecipeMaps.AUTOCLAVE_RECIPES.recipeBuilder()
                .inputs(GTUtility.copy(9, ForestryUtil.getCombStack(comb)))
                .circuitMeta(circuitNumber)
                .fluidInputs(Materials.Mutagen.getFluid((int) Math.max(1, material.getMass() + volt.getMutagenAmount())))
                .output(OrePrefix.crushedPurified, material, 4)
                .duration((int) (material.getMass() * 128))
                .EUt(volt.getAutoclaveEnergy());

        if (volt.compareTo(Voltage.HV) > 0) {
            builder.cleanroom(CleanroomType.CLEANROOM);
        }
        builder.buildAndRegister();
    }

    /**
     * this only adds Chemical and AutoClave process.
     * If you need Centrifuge recipe. use  addCentrifugeToMaterial or addCentrifugeToItemStack
     *
     * @param volt     This determine the required Tier of process for this recipes. This decide the required aEU/t, progress time, required additional UU-Matter, requirement of cleanRoom, needed fluid stack for Chemical.
     * @param material result of Material that should be generated by this process.
     **/
    private static void addProcessGT(GTCombType comb, Material[] material, Voltage volt) {
        for (int i = 0; i < material.length; i++) {
            addChemicalProcess(comb, material[i], material[i], volt);
            addAutoclaveProcess(comb, material[i], volt, i + 1);
        }
    }

    /**
     * this method only adds Centrifuge based on Material. If volt is lower than MV than it will also adds forestry centrifuge recipe.
     *
     * @param comb      BeeComb
     * @param material  resulting Material of processing. must be less than or equal to 9.
     * @param chance    chance to get result, 10000 == 100%
     * @param volt      required Voltage Tier for this recipe, this also affect the duration, amount of UU-Matter, and needed liquid type and amount for chemical reactor
     * @param stackSize This parameter can be null, in that case stack size will be just 1. This handle the stackSize of the resulting Item, and Also the Type of Item. if this value is multiple of 9, than related Material output will be dust, if this value is multiple of 4 than output will be Small dust, else the output will be Tiny dust
     * @param beeWax    if this is null, than the comb will product default Bee wax. But if aMaterial is more than 5, beeWax will be ignored in Gregtech Centrifuge.
     * @param waxChance have same format like "chance"
     **/
    private static void addCentrifugeToMaterial(GTCombType comb, Material[] material, int[] chance, int[] stackSize, Voltage volt, ItemStack beeWax, int waxChance) {
        addCentrifugeToMaterial(comb, material, chance, stackSize, volt, volt.getCentrifugeTime(), beeWax, waxChance);
    }

    private static void addCentrifugeToMaterial(GTCombType comb, Material[] material, int[] chance, int[] stackSize, Voltage volt, int duration, ItemStack beeWax, int waxChance) {
        ItemStack[] output = new ItemStack[material.length + 1];
        stackSize = Arrays.copyOf(stackSize, material.length);
        chance = Arrays.copyOf(chance, output.length);
        chance[chance.length - 1] = waxChance;
        for (int i = 0; i < material.length; i++) {
            if (chance[i] == 0) continue;

            if (Math.max(1, stackSize[i]) % 9 == 0) {
                output[i] = OreDictUnifier.get(OrePrefix.dust, material[i], Math.max(1, stackSize[i]) / 9);
            } else if (Math.max(1, stackSize[i]) % 4 == 0) {
                output[i] = OreDictUnifier.get(OrePrefix.dustSmall, material[i], Math.max(1, stackSize[i]) / 4);
            } else {
                output[i] = OreDictUnifier.get(OrePrefix.dustTiny, material[i], Math.max(1, stackSize[i]));
            }
        }
        if (beeWax != ItemStack.EMPTY) {
            output[output.length - 1] = beeWax;
        } else {
            output[output.length - 1] = ModuleCore.getItems().beeswax.getItemStack();
        }

        addCentrifugeToItemStack(comb, output, chance, volt, duration);
    }

    /**
     * @param volt required Tier of system. If it's lower than MV, it will also add forestry centrifuge.
     * @param item must be less than or equal to 9.
     **/
    private static void addCentrifugeToItemStack(GTCombType comb, ItemStack[] item, int[] chance, Voltage volt) {
        addCentrifugeToItemStack(comb, item, chance, volt, volt.getCentrifugeTime());
    }

    private static void addCentrifugeToItemStack(GTCombType comb, ItemStack[] item, int[] chance, Voltage volt, int duration) {
        ItemStack combStack = ForestryUtil.getCombStack(comb);

        // Start of the Forestry Map
        ImmutableMap.Builder<ItemStack, Float> product = new ImmutableMap.Builder<>();
        // Start of the GregTech Map
        RecipeBuilder<?> builder = RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                .inputs(combStack)
                .duration(duration)
                .EUt(volt.getCentrifugeEnergy());

        int numGTOutputs = 0;
        for (int i = 0; i < item.length; i++) {
            if (item[i] == null || item[i] == ItemStack.EMPTY) continue;
            // Add to Forestry Map
            product.put(item[i], chance[i] / 10000.0f);
            // Add to GregTech Map
            if (numGTOutputs < RecipeMaps.CENTRIFUGE_RECIPES.getMaxOutputs()) {
                if (chance[i] >= 10000) {
                    builder.outputs(item[i]);
                } else {
                    builder.chancedOutput(item[i], chance[i], 0);
                }
                numGTOutputs++;
            }
        }

        // Finalize Forestry Map
        if (volt.compareTo(Voltage.MV) < 0 || !ForestryConfig.harderGTCombRecipes) {
            if (ModuleFactory.machineEnabled(MachineUIDs.CENTRIFUGE)) {
                RecipeManagers.centrifugeManager.addRecipe(40, combStack, product.build());
            }
        }
        // Finalize GregTech Map
        builder.buildAndRegister();
    }

    private enum Voltage {
        ULV, LV, MV, HV, EV, IV, LUV, ZPM, UV, UHV, UEV, UIV, UXV, OPV, MAX;

        public int getVoltage() {
            return (int) GTValues.V[ordinal()];
        }

        public int getChemicalEnergy() {
            return getVoltage() * 3 / 4;
        }

        public int getAutoclaveEnergy() {
            return (int) ((getVoltage() * 3 / 4) * Math.max(1, Math.pow(2, 5 - ordinal())));
        }

        public int getCentrifugeEnergy() {
            return this == Voltage.ULV ? 5 : (getVoltage() / 16) * 15;
        }

        public int getChemicalTime() {
            return 64 + ordinal() * 32;
        }

        public int getCentrifugeTime() {
            if (ForestryConfig.harderGTCombRecipes) {
                return 128 * (Math.max(1, ordinal()));
            } else {
                return 96 + ordinal() * 32;
            }
        }

        public FluidStack getFluid() {
            if (this.compareTo(Voltage.MV) < 0) {
                return Materials.Water.getFluid((this.compareTo(Voltage.ULV) > 0) ? 1000 : 500);
            } else if (this.compareTo(Voltage.HV) < 0) {
                return Materials.DistilledWater.getFluid(1000);
            } else if (this.compareTo(Voltage.EV) < 0) {
                return Materials.SulfuricAcid.getFluid(GTValues.L);
            } else if (this.compareTo(Voltage.IV) < 0) {
                return Materials.HydrochloricAcid.getFluid(GTValues.L);
            } else if (this.compareTo(Voltage.LUV) < 0) {
                return Materials.HydrofluoricAcid.getFluid((int) (Math.pow(2, this.compareTo(Voltage.HV)) * GTValues.L));
            } else {
                return Materials.FluoroantimonicAcid.getFluid((int) (Math.pow(2, this.compareTo(Voltage.LUV)) * GTValues.L));
            }
        }

        public int getMutagenAmount() {
            return 9 * ((this.compareTo(Voltage.MV) < 0) ? 10 : 10 * this.compareTo(Voltage.MV));
        }
    }
}
