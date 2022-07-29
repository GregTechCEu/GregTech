package gregtech.integration.jei.recipe.primitive;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe.ChanceEntry;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.MetaTileEntities;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gregtech.api.GTValues.M;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class OreByProduct implements IRecipeWrapper {

    private static final List<OrePrefix> ORES = new ArrayList<>();

    public static void addOreByProductPrefix(OrePrefix orePrefix) {
        if (!ORES.contains(orePrefix)) {
            ORES.add(orePrefix);
        }
    }

    private static final ImmutableList<OrePrefix> IN_PROCESSING_STEPS = ImmutableList.of(
            OrePrefix.crushed,
            OrePrefix.crushedPurified,
            OrePrefix.crushedRefined,
            OrePrefix.dustImpure
    );

    private final Int2ObjectMap<ChanceEntry> chances = new Int2ObjectOpenHashMap<>();
    private final List<List<ItemStack>> inputs = new ObjectArrayList<>();
    private final List<List<ItemStack>> outputs = new ObjectArrayList<>();
    private final List<List<FluidStack>> fluidInputs = new ObjectArrayList<>();
    private final List<List<FluidStack>> fluidOutputs = new ObjectArrayList<>();
    private final boolean hasDirectSmelt;
    private final boolean hasMercuryBath;
    private final boolean hasPersulfateBath;
    private final boolean hasVitriol;
    private final boolean hasSifter;
    private int currentSlot = 0;

    public OreByProduct(Material material) {
        // add the ores
        List<ItemStack> oreStacks = new ObjectArrayList<>();
        for (OrePrefix prefix : ORES) {
            // get all ores with the relevant oredicts instead of just the first unified ore
            oreStacks.addAll(OreDictionary.getOres(prefix.name() + material.toCamelCaseString()));
        }
        inputs.add(oreStacks);

        // add the machines

        // direct smelting (vanilla furnace)
        OreProperty property = material.getProperty(PropertyKey.ORE);
        Material smeltingResult = property.getDirectSmeltResult() != null ? property.getDirectSmeltResult() : material;
        if (!smeltingResult.hasProperty(PropertyKey.BLAST) && smeltingResult.hasProperty(PropertyKey.INGOT)) {
            addToInputs(new ItemStack(Blocks.FURNACE));
            hasDirectSmelt = true;
        } else {
            addToInputs(ItemStack.EMPTY);
            hasDirectSmelt = false;
        }

        // macerate ore -> crushed
        addToInputs(MetaTileEntities.MACERATOR[GTValues.LV].getStackForm());
        // water wash crushed -> crushed purified
        addToInputs(MetaTileEntities.CHEMICAL_BATH[GTValues.LV].getStackForm());
        // macerate crushed -> impure dust
        addToInputs(MetaTileEntities.MACERATOR[GTValues.LV].getStackForm());

        // mercury and persulfate washing
        boolean hasMercury = false;
        boolean hasPersulfate = false;
        for (Material byproduct : property.getOreByProducts()) {
            if (!hasMercury) hasMercury = byproduct.hasFlag(WASHING_MERCURY);
            if (!hasPersulfate) hasPersulfate = byproduct.hasFlag(WASHING_PERSULFATE);
        }

        if (hasMercury) {
            addToInputs(MetaTileEntities.CHEMICAL_BATH[GTValues.LV].getStackForm());
            this.hasMercuryBath = true;
        } else {
            addToInputs(ItemStack.EMPTY);
            this.hasMercuryBath = false;
        }

        if (hasPersulfate) {
            addToInputs(MetaTileEntities.CHEMICAL_BATH[GTValues.LV].getStackForm());
            this.hasPersulfateBath = true;
        } else {
            addToInputs(ItemStack.EMPTY);
            this.hasPersulfateBath = false;
        }

        // macerate crushed purified -> dust
        addToInputs(MetaTileEntities.MACERATOR[GTValues.LV].getStackForm());

        // thermal centrifuge purified crushed -> refined
        addToInputs(MetaTileEntities.THERMAL_CENTRIFUGE[GTValues.LV].getStackForm());

        // sift crushed purified -> gems
        if (material.hasProperty(PropertyKey.GEM)) {
            this.hasSifter = true;
            addToInputs(MetaTileEntities.SIFTER[GTValues.LV].getStackForm());
        } else {
            addToInputs(ItemStack.EMPTY);
            this.hasSifter = false;
        }

        // acid wash crushed purified -> refined + vitriol
        if (property.getVitriol() != null || material == Cassiterite || material == CassiteriteSand || material == Tin ||
                material == Copper || material == Gold || material == Cooperite || material == Platinum || material == Palladium) {
            addToInputs(MetaTileEntities.CHEMICAL_BATH[GTValues.LV].getStackForm());
            this.hasVitriol = true;
        } else {
            addToInputs(ItemStack.EMPTY);
            this.hasVitriol = false;
        }

        // macerate refined -> dust
        addToInputs(MetaTileEntities.MACERATOR[GTValues.LV].getStackForm());

        // centrifuge impure dust -> dust
        addToInputs(MetaTileEntities.CHEMICAL_BATH[GTValues.LV].getStackForm());

        // add prefixes that should count as inputs to input lists (they will not be displayed in actual page)
        for (OrePrefix prefix : IN_PROCESSING_STEPS) {
            inputs.add(Collections.singletonList(OreDictUnifier.get(prefix, material)));
        }

        // total number of inputs added
        currentSlot += inputs.size();

        // processing recipes
        oreRecipes(material, property);
        crushedRecipes(material, property);
        purifiedRecipes(material, property);
        refinedRecipes(material, property);
        dustRecipes(material, property);
    }

    private void oreRecipes(@Nonnull Material material, @Nonnull OreProperty property) {
        Material smeltingResult = property.getDirectSmeltResult() != null ? property.getDirectSmeltResult() : material;
        if (!smeltingResult.hasProperty(PropertyKey.BLAST) && smeltingResult.hasProperty(PropertyKey.INGOT)) {
            long amountOutput = OrePrefix.ore.getMaterialAmount(material);

            if (ConfigHolder.recipes.harderOreProcessing) {
                amountOutput *= (double) material.getNumComponentsOf(smeltingResult) / material.getNumComponents();
            }

            addToOutputs(OreDictUnifier.getIngot(smeltingResult, amountOutput * property.getOreMultiplier()));
        } else {
            addEmptyOutputs(1);
        }

        int baseOutputAmount = property.getOreMultiplier();
        int oreTypeMultiplier = (int) (ore.getMaterialAmount(material) / M);

        Material byproductMaterial = GTUtility.selectItemInList(0, material, property.getOreByProducts(), Material.class);
        ItemStack byproductStack = OreDictUnifier.get(gem, byproductMaterial);
        if (byproductStack.isEmpty()) byproductStack = OreDictUnifier.get(dust, byproductMaterial);

        // macerate ore -> crushed
        addToOutputs(material, OrePrefix.crushed, baseOutputAmount * oreTypeMultiplier * 2);
        addToOutputs(byproductStack);
        addChance(1400, 850);
    }

    private void crushedRecipes(@Nonnull Material material, @Nonnull OreProperty property) {
        Material primaryByproduct = GTUtility.selectItemInList(0, material, property.getOreByProducts(), Material.class);
        Material secondaryByproduct = GTUtility.selectItemInList(1, material, property.getOreByProducts(), Material.class);

        int crushedMultiplier = (int) (crushed.getMaterialAmount(material) / M);

        // macerate crushed -> impure
        addToOutputs(material, OrePrefix.dustImpure, crushedMultiplier);
        addToOutputs(secondaryByproduct, OrePrefix.dust, 1);
        addChance(1400, 850);

        // ore wash crushed -> crushed purified
        addToOutputs(material, OrePrefix.crushedPurified, 1);
        addToOutputs(primaryByproduct, OrePrefix.dustTiny, 1);
        fluidInputs.add(Collections.singletonList(Materials.Water.getFluid(1000)));

        Material mercuryByproduct = null;
        Material persulfateByproduct = null;
        for (Material byproduct : property.getOreByProducts()) {
            // find the first byproduct in the list with one of these flags (if any)
            if (byproduct.hasFlag(WASHING_MERCURY)) mercuryByproduct = byproduct;
            if (byproduct.hasFlag(WASHING_PERSULFATE)) persulfateByproduct = byproduct;
        }

        if (mercuryByproduct != null) {
            fluidInputs.add(Collections.singletonList(Materials.Mercury.getFluid(100)));
            addToOutputs(material, crushedPurified, 1);
            addToOutputs(mercuryByproduct, crushedPurified, 1);
        } else {
            fluidInputs.add(Collections.emptyList());
            addEmptyOutputs(2);
        }

        if (persulfateByproduct != null) {
            fluidInputs.add(Lists.newArrayList(SodiumPersulfate.getFluid(100), PotassiumPersulfate.getFluid(100)));
            addToOutputs(material, crushedPurified, 1);
            addToOutputs(persulfateByproduct, crushedPurified, 1);
        } else {
            fluidInputs.add(Collections.emptyList());
            addEmptyOutputs(2);
        }
    }

    private void purifiedRecipes(@Nonnull Material material, @Nonnull OreProperty property) {
        Material byproduct = GTUtility.selectItemInList(1, material, property.getOreByProducts(), Material.class);
        int crushedMultiplier = (int) (crushed.getMaterialAmount(material) / M);

        // macerate purified crushed -> dust
        addToOutputs(material, dust, crushedMultiplier);
        addToOutputs(material, dust, crushedMultiplier);
        addChance(2500, 0);
        addToOutputs(byproduct, dust, 1);
        addChance(1400, 850);

        // TC crushed purified -> refined
        addToOutputs(material, OrePrefix.crushedRefined, 1);
        addToOutputs(byproduct, OrePrefix.dustTiny, 3);

        // sifter purified ore -> gems
        if (material.hasProperty(PropertyKey.GEM)) {
            boolean highOutput = material.hasFlag(HIGH_SIFTER_OUTPUT);
            addToOutputs(material, gemExquisite, 1);
            if (highOutput) addChance(500, 150);
            else addChance(300, 100);
            addToOutputs(material, gemFlawless, 1);
            if (highOutput) addChance(1500, 200);
            else addChance(1000, 150);
            addToOutputs(material, gem, 1);
            if (highOutput) addChance(5000, 1000);
            else addChance(3500, 500);
            addToOutputs(material, dust, 1);
            if (highOutput) addChance(2500, 500);
            else addChance(5000, 750);

            ItemStack flawedStack = OreDictUnifier.get(OrePrefix.gemFlawed, material);
            if (!flawedStack.isEmpty()) {
                addToOutputs(flawedStack);
                if (highOutput) addChance(2000, 500);
                else addChance(2500, 300);
            } else {
                addEmptyOutputs(1);
            }
            ItemStack chippedStack = OreDictUnifier.get(OrePrefix.gemChipped, material);
            if (!chippedStack.isEmpty()) {
                addToOutputs(chippedStack);
                if (highOutput) addChance(3000, 350);
                else addChance(3500, 400);
            } else {
                addEmptyOutputs(1);
            }
        } else {
            addEmptyOutputs(6);
        }

        // vitriol bathing
        Material vitriol = property.getVitriol();
        if (vitriol != null) {
            fluidInputs.add(Collections.singletonList(SulfuricAcid.getFluid(vitriol == ClayVitriol ? 1500 : 500)));
            addToOutputs(material, crushedRefined, 1);
            addToOutputs(material, crushedRefined, 1);
            addChance(5000, 0);
            fluidOutputs.add(Collections.singletonList(vitriol.getFluid(500)));
        } else if (material == Cassiterite || material == CassiteriteSand || material == Tin) {
            fluidInputs.add(Collections.singletonList(SulfuricAcid.getFluid(500)));
            addToOutputs(material, crushedRefined, 1);
            addToOutputs(Zinc, crushedRefined, 1);
            addChance(5000, 0);
            fluidOutputs.add(Collections.singletonList(WhiteVitriol.getFluid(500)));
        } else if (material == Copper || material == Gold) {
            fluidInputs.add(Collections.singletonList(AquaRegia.getFluid(6000)));
            addToOutputs(material, crushedRefined, 1);
            addToOutputs(material == Copper ? Cobalt : Copper, crushedRefined, 1);
            addChance(5000, 0);
            fluidOutputs.add(Collections.singletonList(ChloroauricAcid.getFluid(500)));
        } else if (material == Cooperite || material == Platinum || material == Palladium) {
            fluidInputs.add(Collections.singletonList(AquaRegia.getFluid(9000)));
            addToOutputs(material, crushedRefined, 1);
            addToOutputs(PlatinumGroupSludge, dust, 2);
            addChance(5000, 0);
            fluidOutputs.add(Collections.singletonList(ChloroauricAcid.getFluid(500)));
        } else {
            fluidInputs.add(Collections.emptyList());
            addEmptyOutputs(2);
            fluidOutputs.add(Collections.emptyList());
        }
    }

    private void refinedRecipes(@Nonnull Material material, @Nonnull OreProperty property) {
        Material byproduct = GTUtility.selectItemInList(2, material, property.getOreByProducts(), Material.class);
        int crushedMultiplier = (int) (crushed.getMaterialAmount(material) / M);

        // macerate refined -> dust
        addToOutputs(material, dust, crushedMultiplier);
        addToOutputs(material, dust, crushedMultiplier);
        addChance(3333, 0);
        addToOutputs(byproduct, dust, 1);
        addChance(1400, 850);
    }

    private void dustRecipes(@Nonnull Material material, @Nonnull OreProperty property) {
        // bathe impure dust -> dust
        fluidInputs.add(Collections.singletonList(Water.getFluid(100)));
        addToOutputs(material, dust, 1);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, inputs);
        ingredients.setInputLists(VanillaTypes.FLUID, fluidInputs);
        ingredients.setOutputLists(VanillaTypes.ITEM, outputs);
        ingredients.setOutputLists(VanillaTypes.FLUID, fluidOutputs);
    }

    public void addTooltip(int slotIndex, boolean input, Object ingredient, List<String> tooltip) {
        if (chances.containsKey(slotIndex)) {
            ChanceEntry entry = chances.get(slotIndex);
            double chance = entry.getChance() / 100.0;
            double boost = entry.getBoostPerTier() / 100.0;
            tooltip.add(I18n.format("gregtech.recipe.chance", chance, boost));
        }
    }

    public ChanceEntry getChance(int slot) {
        return chances.get(slot);
    }

    public boolean hasSifter() {
        return hasSifter;
    }

    public boolean hasMercuryBath() {
        return hasMercuryBath;
    }

    public boolean hasPersulfateBath() {
        return hasPersulfateBath;
    }

    public boolean hasVitriol() {
        return hasVitriol;
    }

    public boolean hasDirectSmelt() {
        return hasDirectSmelt;
    }

    private void addToOutputs(Material material, OrePrefix prefix, int size) {
        addToOutputs(OreDictUnifier.get(prefix, material, size));
    }

    private void addToOutputs(ItemStack stack) {
        outputs.add(Collections.singletonList(stack));
        currentSlot++;
    }

    private void addEmptyOutputs(int amount) {
        for (int i = 0; i < amount; i++) {
            addToOutputs(ItemStack.EMPTY);
        }
    }

    private void addToInputs(ItemStack stack) {
        inputs.add(Collections.singletonList(stack));
    }

    private void addChance(int base, int tier) {
        // this is solely for the chance overlay and tooltip, neither of which care about the ItemStack
        chances.put(currentSlot - 1, new ChanceEntry(ItemStack.EMPTY, base, tier));
    }
}
