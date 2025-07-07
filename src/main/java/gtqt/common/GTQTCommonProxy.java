package gtqt.common;

import gregtech.common.items.MetaItems;

import gtqt.api.util.wireless.WirelessWorldEventHandler;
import gtqt.common.items.GTQTMetaItems;
import gtqt.common.items.covers.GTQTCoverBehavior;
import gtqt.common.metatileentities.GTQTMetaTileEntities;

import gtqt.loaders.recipe.RecipeManager;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class GTQTCommonProxy {

    public static final CreativeTabs GTQTCore_PC = new CreativeTabs("gtqt_programmable") {

        @Override
        public ItemStack createIcon() {
            return MetaItems.INTEGRATED_CIRCUIT.getStackForm();
        }
    };
    public static void registerRecipeHandlers(RegistryEvent.Register<IRecipe> event) {

    }

    public static void registerCoverBehavior() {
        GTQTCoverBehavior.init();
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new WirelessWorldEventHandler());
    }

    public static void preInit() {
        GTQTMetaTileEntities.initialization();
        GTQTMetaItems.initialization();
    }

    public static void registerRecipes() {
        RecipeManager.register();
    }
}
