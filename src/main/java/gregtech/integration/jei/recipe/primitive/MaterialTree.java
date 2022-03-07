package gregtech.integration.jei.recipe.primitive;

import com.google.common.collect.ImmutableList;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.ArrayList;

public class MaterialTree implements IRecipeWrapper {
    private final static ImmutableList<OrePrefix> PREFIXES = ImmutableList.of(
            OrePrefix.dustTiny,
            OrePrefix.dust,
            OrePrefix.dustSmall,
            OrePrefix.cableGtSingle,
            OrePrefix.ingotHot,
            OrePrefix.ingot,
            OrePrefix.gem,
            OrePrefix.block,
            OrePrefix.wireGtSingle,
            OrePrefix.stick,
            OrePrefix.nugget,
            OrePrefix.plate,
            OrePrefix.wireFine,
            OrePrefix.frameGt,
            OrePrefix.round,
            OrePrefix.pipeNormalFluid,
            OrePrefix.pipeNormalItem,
            OrePrefix.screw,
            OrePrefix.bolt,
            OrePrefix.gear,
            OrePrefix.plateDouble,
            OrePrefix.spring,
            OrePrefix.stickLong,
            OrePrefix.gearSmall,
            OrePrefix.plateDense,
            OrePrefix.springSmall,
            OrePrefix.ring,
            // fluid,
            OrePrefix.lens,
            OrePrefix.foil
            // TODO chunk?
    );

    private final List<List<ItemStack>> itemInputs = new ArrayList<>();
    private final List<List<FluidStack>> fluidInputs = new ArrayList<>();

    private final String name;
    private final String formula;
    private final int blastTemp;
    private final long avgM;
    private final long avgP;
    private final long avgN;

    public MaterialTree(Material material) {
        // adding an empty list to itemInputs/fluidInputs makes checking if a prefix exists later much easier
        List<ItemStack> inputDusts = new ArrayList<>();
        for (OrePrefix prefix : PREFIXES) {
            inputDusts.add(OreDictUnifier.get(prefix, material));
        }
        for (ItemStack stack : inputDusts) {
            List<ItemStack> matItemsStack = new ArrayList<>();
            matItemsStack.add(stack);
            this.itemInputs.add(matItemsStack);
        }

        List<FluidStack> matFluidsStack = new ArrayList<>();
        if (material.hasProperty(PropertyKey.FLUID)) {
            matFluidsStack.add(material.getFluid(1000));
        }
        this.fluidInputs.add(matFluidsStack);

        name = material.getLocalizedName();
        formula = material.getChemicalFormula();
        avgM = material.getMass();
        avgP = material.getProtons();
        avgN = material.getNeutrons();
        if (material.hasProperty(PropertyKey.BLAST)) {
            blastTemp = material.getBlastTemperature();
        } else {
            blastTemp = 0;
        }
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, this.itemInputs);
        ingredients.setInputLists(VanillaTypes.FLUID, this.fluidInputs);
        // these don't get displayed, but allow the material tree to show up on left *or* right click
        ingredients.setOutputLists(VanillaTypes.ITEM, this.itemInputs);
        ingredients.setOutputLists(VanillaTypes.FLUID, this.fluidInputs);
    }

    public String getMaterialName() {
        return name;
    }

    public String getMaterialFormula() {
        return formula;
    }

    public long getAvgM() {
        return avgM;
    }

    public long getAvgP() {
        return avgP;
    }

    public long getAvgN() {
        return avgN;
    }

    public int getBlastTemp() {
        return blastTemp;
    }
}
