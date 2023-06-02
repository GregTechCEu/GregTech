package gregtech.integration.exnihilo;

import exnihilocreatio.registries.manager.ExNihiloRegistryManager;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.SteamTexture;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.modules.GregTechModule;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.IntegrationModule;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.exnihilo.items.ExNihiloPebble;
import gregtech.integration.exnihilo.metatileentities.MetaTileEntitySieve;
import gregtech.integration.exnihilo.metatileentities.MetaTileEntitySteamSieve;
import gregtech.integration.exnihilo.recipes.ExNihiloRecipes;
import gregtech.integration.exnihilo.recipes.MeshRecipes;
import gregtech.integration.exnihilo.recipes.SieveDrops;
import gregtech.integration.exnihilo.recipes.recipemaps.SieveRecipeMap;
import gregtech.modules.GregTechModules;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static gregtech.api.unification.ore.OrePrefix.Conditions.hasOreProperty;
import static gregtech.api.unification.ore.OrePrefix.Flags.ENABLE_UNIFICATION;
import static gregtech.common.metatileentities.MetaTileEntities.getHighTier;
import static gregtech.common.metatileentities.MetaTileEntities.getMidTier;


@GregTechModule(
        moduleID = GregTechModules.MODULE_EN,
        containerID = GTValues.MODID,
        modDependencies = GTValues.MODID_EN,
        name = "GregTech ExNihiloCreatio integration",
        descriptionKey = "gregtech.modules.en_integration.description"
)
public class ExNihiloModule extends IntegrationSubmodule {


    // Items
    public static ExNihiloPebble GTPebbles;

    // Recipe maps
    public static final RecipeMap<SimpleRecipeBuilder> SIEVE_RECIPES = new SieveRecipeMap("electric_sieve", 2, false, 36, true, 0, false, 0, false, new SimpleRecipeBuilder().duration(100).EUt(4), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, ProgressWidget.MoveType.VERTICAL_INVERTED)
            .setSound(SoundEvents.BLOCK_SAND_PLACE);

    // Machines
    public static MetaTileEntitySteamSieve STEAM_SIEVE_BRONZE;
    public static MetaTileEntitySteamSieve STEAM_SIEVE_STEEL;
    public static MetaTileEntitySieve[] SIEVES = new MetaTileEntitySieve[GTValues.V.length - 1];

    // Textures
    public static final SteamTexture PROGRESS_BAR_SIFTER_STEAM = SteamTexture.fullImage("textures/gui/progress_bar/progress_bar_sift_%s.png");

    // Ore prefixes
    public static OrePrefix oreChunk;
    public static OrePrefix oreEnderChunk;
    public static OrePrefix oreNetherChunk;
    public static OrePrefix oreSandyChunk;

    // Icon Types

    public static MaterialIconType oreChunkIcon;
    public static MaterialIconType oreEnderChunkIcon;
    public static MaterialIconType oreNetherChunkIcon;
    public static MaterialIconType oreSandyChunkIcon;

    @Nonnull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(ExNihiloModule.class);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        getLogger().info("Registering Ex Nihilo Compat Items, Blocks, and Machines");
        GTPebbles = new ExNihiloPebble();
        ExNihiloRegistryManager.registerSieveDefaultRecipeHandler(new SieveDrops());
        registerMetaTileEntities();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        ExNihiloRecipes.registerExNihiloRecipes();
        MeshRecipes.init();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        IntegrationModule.logger.info("Registering Ex Nihilo Compat Recipes");
        ExNihiloRecipes.registerHandlers();
        ExNihiloRecipes.registerGTRecipes();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerMaterials(GregTechAPI.MaterialEvent event) {
        oreChunkIcon = new MaterialIconType("oreChunk");
        oreEnderChunkIcon = new MaterialIconType("oreEnderChunk");
        oreNetherChunkIcon = new MaterialIconType("oreNetherChunk");
        oreSandyChunkIcon = new MaterialIconType("oreSandyChunk");

        oreChunk = new OrePrefix("oreChunk", -1, null, oreChunkIcon, ENABLE_UNIFICATION, hasOreProperty);
        oreEnderChunk = new OrePrefix("oreEnderChunk", -1, null, oreEnderChunkIcon, ENABLE_UNIFICATION, hasOreProperty);
        oreNetherChunk = new OrePrefix("oreNetherChunk", -1, null, oreNetherChunkIcon, ENABLE_UNIFICATION, hasOreProperty);
        oreSandyChunk = new OrePrefix("oreSandyChunk", -1, null, oreSandyChunkIcon, ENABLE_UNIFICATION, hasOreProperty);

        oreChunk.setAlternativeOreName(OrePrefix.ore.name());
        oreEnderChunk.setAlternativeOreName(OrePrefix.oreEndstone.name());
        oreNetherChunk.setAlternativeOreName(OrePrefix.oreNetherrack.name());
        oreSandyChunk.setAlternativeOreName(OrePrefix.ore.name());

        MetaItems.addOrePrefix(oreChunk, oreEnderChunk, oreNetherChunk, oreSandyChunk);
    }

    private void registerMetaTileEntities() {
        STEAM_SIEVE_BRONZE = MetaTileEntities.registerMetaTileEntity(4000, new MetaTileEntitySteamSieve(new ResourceLocation(GTValues.MODID ,"sieve.steam"), false));
        STEAM_SIEVE_STEEL = MetaTileEntities.registerMetaTileEntity(4001, new MetaTileEntitySteamSieve(new ResourceLocation(GTValues.MODID, "steam_sieve_steel"), true));

        SIEVES[0] = MetaTileEntities.registerMetaTileEntity(4002, new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.lv"), 1));
        SIEVES[1] = MetaTileEntities.registerMetaTileEntity(4003, new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.mv"), 2));
        SIEVES[2] = MetaTileEntities.registerMetaTileEntity(4004, new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.hv"), 3));
        SIEVES[3] = MetaTileEntities.registerMetaTileEntity(4005, new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.ev"), 4));
        SIEVES[4] = MetaTileEntities.registerMetaTileEntity(4006, new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.iv"), 5));
        if (getMidTier("sieve")) {
            SIEVES[5] = MetaTileEntities.registerMetaTileEntity(4007, new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.luv"), 6));
            SIEVES[6] = MetaTileEntities.registerMetaTileEntity(4008, new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.zpm"), 7));
            SIEVES[7] = MetaTileEntities.registerMetaTileEntity(4009, new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.uv"), 8));
        }
        // TODO this config should ideally use the HIGH_TIER map instead of direct checking it, if the cfg is kept
        if (getHighTier("sieve") && ExNihiloConfig.highTierSieve) {
            SIEVES[8] = MetaTileEntities.registerMetaTileEntity(4010, new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.uhv"), 9));
            SIEVES[9] = MetaTileEntities.registerMetaTileEntity(4011, new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.uev"), 10));
            SIEVES[10] = MetaTileEntities.registerMetaTileEntity(4012, new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.uiv"), 11));
            SIEVES[11] = MetaTileEntities.registerMetaTileEntity(4013, new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.uxv"), 12));
            SIEVES[12] = MetaTileEntities.registerMetaTileEntity(4014, new MetaTileEntitySieve(new ResourceLocation(GTValues.MODID, "sieve.opv"), 13));
        }
    }

}
