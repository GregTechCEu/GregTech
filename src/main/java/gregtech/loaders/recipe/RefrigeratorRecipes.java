package gregtech.loaders.recipe;

import gregtech.api.fluids.ICoolant;
import gregtech.api.fluids.ICryoGas;
import gregtech.api.GTValues;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.category.RecipeCategories;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.MarkerMaterials.Color;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;

public class RefrigeratorRecipes {
    public static void init() {
        //ticks = duration * timefactor

        ICoolant AmmoniaCoolant = new ICoolant(Ammonia);
        AmmoniaCoolant.setAmountToUse(8000);
        AmmoniaCoolant.setTimeFactor(8);

        ICoolant PropeneCoolant = new ICoolant(Propene);
        PropeneCoolant.setAmountToUse(8000);
        PropeneCoolant.setTimeFactor(7);

        ICoolant SodiumPotassiumCoolant = new ICoolant(SodiumPotassium);
        SodiumPotassiumCoolant.setAmountToUse(6000);
        SodiumPotassiumCoolant.setTimeFactor(1);

        ICoolant[] Coolants = {
                AmmoniaCoolant,
                PropeneCoolant,
                SodiumPotassiumCoolant
        };

        ICryoGas CryoAir = new ICryoGas( Air, LiquidAir);
        CryoAir.setEUt(128);
        CryoAir.setDuration(200);

        ICryoGas CryoNetherAir = new ICryoGas(NetherAir, LiquidNetherAir);
        CryoNetherAir.setEUt(2048);
        CryoNetherAir.setDuration(400);

        ICryoGas[] CryoGases = {
                CryoAir,
                CryoNetherAir
        };

        for (ICryoGas Cryogas : CryoGases) {
            for (ICoolant Coolant : Coolants) {
                RecipeMaps.REFRIGERATION_RECIPES.recipeBuilder()
                        .fluidInputs(Cryogas.getGas().getFluid(1000))
                        .notConsumable(Coolant.getCoolant().getFluid(Coolant.getAmount_to_use()))
                        .fluidOutputs(Cryogas.getLiquidGas().getFluid(1000))
                        .EUt(Cryogas.getEUt())
                        .duration(Cryogas.getDuration() * Coolant.getTimeFactor())
                        .buildAndRegister();
            }
        }
    }
}
