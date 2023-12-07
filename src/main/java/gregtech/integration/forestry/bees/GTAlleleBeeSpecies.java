package gregtech.integration.forestry.bees;

import gregtech.api.GTValues;
import gregtech.integration.IntegrationModule;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IAlleleBeeSpeciesBuilder;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAlleleFloat;
import forestry.api.genetics.IClassification;
import forestry.apiculture.genetics.alleles.AlleleBeeSpecies;
import forestry.core.genetics.alleles.AlleleFloat;
import org.jetbrains.annotations.NotNull;

public class GTAlleleBeeSpecies extends AlleleBeeSpecies {

    public static IAlleleFloat speedBlinding;

    public GTAlleleBeeSpecies(String modId, String uid, String unlocalizedName, String authority,
                              String unlocalizedDescription, boolean dominant, IClassification branch,
                              String binomial, int primaryColor, int secondaryColor) {
        super(modId, uid, unlocalizedName, authority, unlocalizedDescription, dominant, branch, binomial, primaryColor,
                secondaryColor);
        AlleleManager.alleleRegistry.registerAllele(this, EnumBeeChromosome.SPECIES);
    }

    @NotNull
    @Override
    public IAlleleBeeSpeciesBuilder addProduct(@NotNull ItemStack product, @NotNull Float chance) {
        if (product == ItemStack.EMPTY) {
            IntegrationModule.logger.warn(
                    "GTAlleleBeeSpecies#addProduct() passed an empty ItemStack for allele {}! Setting default",
                    getUID());
            product = new ItemStack(Items.BOAT);
        }
        if (chance <= 0.0f || chance > 1.0f) {
            IntegrationModule.logger.warn(
                    "GTAlleleBeeSpecies#addProduct() passed a chance value out of bounds for allele {}! Setting to 0.1",
                    getUID());
            chance = 0.1f;
        }
        return super.addProduct(product, chance);
    }

    @NotNull
    @Override
    public IAlleleBeeSpeciesBuilder addSpecialty(@NotNull ItemStack specialty, @NotNull Float chance) {
        if (specialty == ItemStack.EMPTY) {
            IntegrationModule.logger.warn(
                    "GTAlleleBeeSpecies#addProduct() passed an empty ItemStack for allele {}! Setting default",
                    getUID());
            specialty = new ItemStack(Items.BOAT);
        }
        if (chance <= 0.0f || chance > 1.0f) {
            IntegrationModule.logger.warn(
                    "GTAlleleBeeSpecies#addSpecialty() passed a chance value out of bounds for allele {}! Setting to 0.1",
                    getUID());
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
