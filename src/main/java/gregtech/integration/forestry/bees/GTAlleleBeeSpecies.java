package gregtech.integration.forestry.bees;

import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IAlleleBeeSpeciesBuilder;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAlleleFloat;
import forestry.api.genetics.IClassification;
import forestry.apiculture.genetics.alleles.AlleleBeeSpecies;
import forestry.core.genetics.alleles.AlleleFloat;
import gregtech.api.GTValues;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class GTAlleleBeeSpecies extends AlleleBeeSpecies {

    public static IAlleleFloat speedBlinding;

    public GTAlleleBeeSpecies(String modId, String uid, String unlocalizedName, String authority,
                              String unlocalizedDescription, boolean dominant, IClassification branch,
                              String binomial, int primaryColor, int secondaryColor) {
        super(modId, uid, unlocalizedName, authority, unlocalizedDescription, dominant, branch, binomial, primaryColor, secondaryColor);
        AlleleManager.alleleRegistry.registerAllele(this, EnumBeeChromosome.SPECIES);
    }

    @Nonnull
    @Override
    public IAlleleBeeSpeciesBuilder addProduct(@Nonnull ItemStack product, @Nonnull Float chance) {
        if (product == ItemStack.EMPTY) {
            product = new ItemStack(Items.BOAT);
        }
        if (chance <= 0.0f || chance > 1.0f) {
            chance = 0.1f;
        }
        return super.addProduct(product, chance);
    }

    @Nonnull
    @Override
    public IAlleleBeeSpeciesBuilder addSpecialty(@Nonnull ItemStack specialty, @Nonnull Float chance) {
        if (specialty == ItemStack.EMPTY) {
            specialty = new ItemStack(Items.BOAT);
        }
        if (chance <= 0.0f || chance > 1.0f) {
            chance = 0.1f;
        }
        return super.addSpecialty(specialty, chance);
    }

    public static void setupAlleles() {
        IAlleleFloat allele = (IAlleleFloat) AlleleManager.alleleRegistry.getAllele("magicbees.speedBlinding");
        if (allele == null) {
            allele = new AlleleFloat(GTValues.MODID, "gregtech.speedBlinding", "gregtech.speedBlinding", 2f, false);
            AlleleManager.alleleRegistry.registerAllele(allele, EnumBeeChromosome.SPEED);
        }
        speedBlinding = allele;
    }
}
