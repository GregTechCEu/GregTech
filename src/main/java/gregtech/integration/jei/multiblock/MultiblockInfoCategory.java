package gregtech.integration.jei.multiblock;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.infos.*;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class MultiblockInfoCategory implements IRecipeCategory<MultiblockInfoRecipeWrapper> {

    private final IDrawable background;
    private final IDrawable icon;
    private final IGuiHelper guiHelper;

    public MultiblockInfoCategory(IJeiHelpers helpers) {
        this.guiHelper = helpers.getGuiHelper();
        this.background = this.guiHelper.createBlankDrawable(176, 166);
        this.icon = guiHelper.drawableBuilder(GuiTextures.MULTIBLOCK_CATEGORY.imageLocation, 0, 0, 18, 18).setTextureSize(18, 18).build();
    }

    public static final Map<String, MultiblockInfoRecipeWrapper> multiblockRecipes = new HashMap<String, MultiblockInfoRecipeWrapper>() {{
        put("primitive_blast_furnace", new MultiblockInfoRecipeWrapper(new PrimitiveBlastFurnaceInfo()));
        put("coke_oven", new MultiblockInfoRecipeWrapper(new CokeOvenInfo()));
        put("vacuum_freezer", new MultiblockInfoRecipeWrapper(new VacuumFreezerInfo()));
        put("implosion_compressor", new MultiblockInfoRecipeWrapper(new ImplosionCompressorInfo()));
        put("pyrolyze_oven", new MultiblockInfoRecipeWrapper(new PyrolyseOvenInfo()));
        put("cracker_unit", new MultiblockInfoRecipeWrapper(new CrackerUnitInfo()));
        put("diesel_engine", new MultiblockInfoRecipeWrapper(new LargeCombustionEngineInfo()));
        put("distillation_tower", new MultiblockInfoRecipeWrapper(new DistillationTowerInfo()));
        put("electric_blast_furnace", new MultiblockInfoRecipeWrapper(new ElectricBlastFurnaceInfo()));
        put("multi_smelter", new MultiblockInfoRecipeWrapper(new MultiSmelterInfo()));
        put("large_chemical_reactor", new MultiblockInfoRecipeWrapper(new LargeChemicalReactorInfo()));
        put("large_bronze_boiler", new MultiblockInfoRecipeWrapper(new LargeBoilerInfo(MetaTileEntities.LARGE_BRONZE_BOILER)));
        put("large_steel_boiler", new MultiblockInfoRecipeWrapper(new LargeBoilerInfo(MetaTileEntities.LARGE_STEEL_BOILER)));
        put("large_titanium_boiler", new MultiblockInfoRecipeWrapper(new LargeBoilerInfo(MetaTileEntities.LARGE_TITANIUM_BOILER)));
        put("large_tungstensteel_boiler", new MultiblockInfoRecipeWrapper(new LargeBoilerInfo(MetaTileEntities.LARGE_TUNGSTENSTEEL_BOILER)));
        put("large_steam_turbine", new MultiblockInfoRecipeWrapper(new LargeTurbineInfo(MetaTileEntities.LARGE_STEAM_TURBINE)));
        put("large_gas_turbine", new MultiblockInfoRecipeWrapper(new LargeTurbineInfo(MetaTileEntities.LARGE_GAS_TURBINE)));
        put("large_plasma_turbine", new MultiblockInfoRecipeWrapper(new LargeTurbineInfo(MetaTileEntities.LARGE_PLASMA_TURBINE)));
        put("assembly_line", new MultiblockInfoRecipeWrapper(new AssemblyLineInfo()));
        put("fusion_reactor_mk1", new MultiblockInfoRecipeWrapper(new FusionReactorInfo(GTValues.LuV)));
        put("fusion_reactor_mk2", new MultiblockInfoRecipeWrapper(new FusionReactorInfo(GTValues.ZPM)));
        put("fusion_reactor_mk3", new MultiblockInfoRecipeWrapper(new FusionReactorInfo(GTValues.UV)));
        put("primitive_water_pump", new MultiblockInfoRecipeWrapper(new PrimitivePumpInfo()));
        put("steam_grinder", new MultiblockInfoRecipeWrapper(new SteamGrinderInfo()));
        put("steam_oven", new MultiblockInfoRecipeWrapper(new SteamOvenInfo()));
    }};

    public static void registerRecipes(IModRegistry registry) {
        MinecraftForge.EVENT_BUS.post(new MultiInfoPageEvent(multiblockRecipes));
        registry.addRecipes(multiblockRecipes.values(), "gregtech:multiblock_info");
    }

    @Nonnull
    @Override
    public String getUid() {
        return "gregtech:multiblock_info";
    }

    @Nonnull
    @Override
    public String getTitle() {
        return I18n.format("gregtech.multiblock.title");
    }

    @Nonnull
    @Override
    public String getModName() {
        return GTValues.MODID;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MultiblockInfoRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        recipeWrapper.setRecipeLayout((RecipeLayout) recipeLayout, this.guiHelper);

        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        itemStackGroup.addTooltipCallback(recipeWrapper::addBlockTooltips);
    }
}
