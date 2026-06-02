package gregtech.integration.exnihilo;

import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMapBuilder;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.Mods;
import gregtech.common.items.MetaItems;
import gregtech.integration.IntegrationModule;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.exnihilo.items.ExNihiloPebble;
import gregtech.integration.exnihilo.metatileentities.MetaTileEntities;
import gregtech.integration.exnihilo.recipes.CraftingRecipes;
import gregtech.integration.exnihilo.recipes.MachineRecipes;
import gregtech.integration.exnihilo.recipes.SieveDrops;
import gregtech.integration.exnihilo.recipes.SieveRecipes;
import gregtech.integration.exnihilo.recipes.ui.SieveUI;
import gregtech.modules.GregTechModules;

import net.minecraft.init.SoundEvents;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static gregtech.api.unification.ore.OrePrefix.Conditions.hasOreProperty;
import static gregtech.api.unification.ore.OrePrefix.Flags.ENABLE_UNIFICATION;

@GregTechModule(
                moduleID = GregTechModules.MODULE_EX_NIHILO,
                containerID = GTValues.MODID,
                modDependencies = Mods.Names.EX_NIHILO_CREATIO,
                name = "GregTech Ex Nihilo Creatio Integration",
                description = "Ex Nihilo Integration Module")
public class ExNihiloModule extends IntegrationSubmodule {

    // Items
    public static ExNihiloPebble pebbleItem;

    // Recipe maps
    public static final RecipeMap<SimpleRecipeBuilder> SIEVE_RECIPES = new RecipeMapBuilder<>("electric_sieve",
            new SimpleRecipeBuilder().duration(100).EUt(4))
                    .itemInputs(2)
                    .itemOutputs(36)
                    .ui(SieveUI::new)
                    .sound(SoundEvents.BLOCK_SAND_PLACE)
                    .build();

    // Ore prefixes
    public static OrePrefix oreChunk;
    public static OrePrefix oreEnderChunk;
    public static OrePrefix oreNetherChunk;

    // Icon Types
    public static MaterialIconType oreChunkIcon;
    public static MaterialIconType oreEnderChunkIcon;
    public static MaterialIconType oreNetherChunkIcon;

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(ExNihiloModule.class);
    }

    @Override
    public void preInit(@NotNull FMLPreInitializationEvent event) {
        getLogger().info("Registering Ex Nihilo Compat Items, Blocks, and Machines");
        pebbleItem = new ExNihiloPebble();
        MetaTileEntities.registerMetaTileEntities();
    }

    // Has to be done in init phase because of ExNi registering outside the Registry event
    @Override
    public void init(@NotNull FMLInitializationEvent event) {
        SieveDrops.registerSiftingRecipes();
        MachineRecipes.mirrorExNihiloRecipes();
    }

    @SubscribeEvent()
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        IntegrationModule.logger.info("Registering Ex Nihilo Compat Recipes");
        CraftingRecipes.registerRecipes();
        MachineRecipes.registerRecipes();
        SieveRecipes.registerRecipes();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerMaterials(MaterialEvent event) {
        oreChunkIcon = new MaterialIconType("oreChunk");
        oreEnderChunkIcon = new MaterialIconType("oreEnderChunk");
        oreNetherChunkIcon = new MaterialIconType("oreNetherChunk");

        oreChunk = new OrePrefix("oreChunk", -1, null, oreChunkIcon, ENABLE_UNIFICATION, hasOreProperty);
        oreEnderChunk = new OrePrefix("oreEnderChunk", -1, null, oreEnderChunkIcon, ENABLE_UNIFICATION, hasOreProperty);
        oreNetherChunk = new OrePrefix("oreNetherChunk", -1, null, oreNetherChunkIcon, ENABLE_UNIFICATION,
                hasOreProperty);

        oreChunk.setAlternativeOreName(OrePrefix.ore.name());
        oreEnderChunk.setAlternativeOreName(OrePrefix.oreEndstone.name());
        oreNetherChunk.setAlternativeOreName(OrePrefix.oreNetherrack.name());

        MetaItems.addOrePrefix(oreChunk, oreEnderChunk, oreNetherChunk);
    }
}
