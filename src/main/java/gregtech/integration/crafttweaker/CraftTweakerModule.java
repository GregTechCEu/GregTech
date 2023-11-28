package gregtech.integration.crafttweaker;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaOreDictItem;
import gregtech.api.modules.GregTechModule;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.integration.IntegrationModule;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.crafttweaker.recipe.MetaItemBracketHandler;
import gregtech.integration.crafttweaker.terminal.CTTerminalRegistry;
import gregtech.modules.GregTechModules;

import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import crafttweaker.CraftTweakerAPI;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@GregTechModule(
                moduleID = GregTechModules.MODULE_CT,
                containerID = GTValues.MODID,
                modDependencies = GTValues.MODID_CT,
                name = "GregTech CraftTweaker Integration",
                description = "CraftTweaker Integration Module")
public class CraftTweakerModule extends IntegrationSubmodule {

    public static MetaOreDictItem CT_OREDICT_ITEM;

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(CraftTweakerModule.class);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        CT_OREDICT_ITEM = new MetaOreDictItem((short) 0);
        CT_OREDICT_ITEM.setRegistryName("meta_oredict_item_ct");
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        CTTerminalRegistry.register();
    }

    @Override
    public void loadComplete(FMLLoadCompleteEvent event) {
        MetaItemBracketHandler.clearComponentRegistry();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMaterialEvent(MaterialEvent event) {
        IntegrationModule.logger.info("Running early CraftTweaker initialization scripts...");
        CraftTweakerAPI.tweaker.loadScript(false, "gregtech");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRecipeEvent(RegistryEvent.Register<IRecipe> event) {
        MetaItemBracketHandler.rebuildComponentRegistry();
    }
}
