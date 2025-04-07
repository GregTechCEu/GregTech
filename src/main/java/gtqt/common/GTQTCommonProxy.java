package gtqt.common;

import gregtech.api.GregTechAPI;
import gregtech.api.cover.CoverDefinition;
import gregtech.common.items.MetaItems;

import gtqt.common.metatileentities.GTQTMetaTileEntities;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;

public class GTQTCommonProxy {

    public static final CreativeTabs GTQTCore_TAB = new CreativeTabs("gtqt") {

        @Override
        public ItemStack createIcon() {
            return MetaItems.WETWARE_MAINFRAME_UHV.getStackForm();
        }
    };

    public static void registerRecipeHandlers(RegistryEvent.Register<IRecipe> event) {

    }

    public static void registerCoverBehavior(GregTechAPI.RegisterEvent<CoverDefinition> event) {

    }

    public static void init() {

    }

    public static void preInit() {
        GTQTMetaTileEntities.initialization();
    }
}
