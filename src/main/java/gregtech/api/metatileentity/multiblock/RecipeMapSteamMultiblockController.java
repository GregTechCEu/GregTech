package gregtech.api.metatileentity.multiblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.SteamMultiblockRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.common.ConfigHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public abstract class RecipeMapSteamMultiblockController extends MultiblockWithDisplayBase {

    protected static final double CONVERSION_RATE = ConfigHolder.machines.multiblockSteamToEU;

    public final RecipeMap<?> recipeMap;
    protected SteamMultiblockRecipeLogic recipeMapWorkable;

    protected IItemHandlerModifiable inputInventory;
    protected IItemHandlerModifiable outputInventory;
    protected IMultipleTankHandler steamFluidTank;

    public RecipeMapSteamMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, double conversionRate) {
        super(metaTileEntityId);
        this.recipeMap = recipeMap;
        this.recipeMapWorkable = new SteamMultiblockRecipeLogic(this, recipeMap, steamFluidTank, conversionRate);
        resetTileAbilities();
    }

    public IItemHandlerModifiable getInputInventory() {
        return inputInventory;
    }

    public IItemHandlerModifiable getOutputInventory() {
        return outputInventory;
    }

    public IMultipleTankHandler getSteamFluidTank() {
        return steamFluidTank;
    }

    /**
     * Performs extra checks for validity of given recipe before multiblock
     * will start it's processing.
     */
    public boolean checkRecipe(Recipe recipe, boolean consumeIfProcess) {
        return true;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
    }

    @Override
    protected void updateFormedValid() {
        recipeMapWorkable.update();
    }

    private void initializeAbilities() {
        this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.STEAM_IMPORT_ITEMS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.STEAM_EXPORT_ITEMS));
        this.steamFluidTank = new FluidTankList(true, getAbilities(MultiblockAbility.STEAM));
    }

    private void resetTileAbilities() {
        this.inputInventory = new ItemStackHandler(0);
        this.outputInventory = new ItemStackHandler(0);
        this.steamFluidTank = new FluidTankList(true);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            IFluidTank steamFluidTank = recipeMapWorkable.getSteamFluidTankCombined();
            if (steamFluidTank != null && steamFluidTank.getCapacity() > 0) {
                int steamStored = steamFluidTank.getFluidAmount();
                textList.add(new TextComponentTranslation("gregtech.multiblock.steam.steam_stored", steamStored, steamFluidTank.getCapacity()));
            }

            if (!recipeMapWorkable.isWorkingEnabled()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));

            } else if (recipeMapWorkable.isActive()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.running"));
                int currentProgress = (int) (recipeMapWorkable.getProgressPercent() * 100);
                if (this.recipeMapWorkable.getParallelLimit() != 1) {
                    textList.add(new TextComponentTranslation("gregtech.multiblock.parallel", this.recipeMapWorkable.getParallelLimit()));
                }
                textList.add(new TextComponentTranslation("gregtech.multiblock.progress", currentProgress));
            } else {
                textList.add(new TextComponentTranslation("gregtech.multiblock.idling"));
            }

            if (recipeMapWorkable.isHasNotEnoughEnergy()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.steam.low_steam").setStyle(new Style().setColor(TextFormatting.RED)));
            }
        }
    }

    @Override
    public TraceabilityPredicate autoAbilities() {
        return autoAbilities(true, true, true, true, true);
    }

    public TraceabilityPredicate autoAbilities(boolean checkSteam,
                                               boolean checkMaintainer,
                                               boolean checkItemIn,
                                               boolean checkItemOut,
                                               boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintainer, checkMuffler)
                .or(checkSteam ? abilities(MultiblockAbility.STEAM).setMinGlobalLimited(1).setPreviewCount(1) : new TraceabilityPredicate());
        if (checkItemIn) {
            if (recipeMap.getMinInputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.STEAM_IMPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1));
            }
            else if (recipeMap.getMaxInputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1));
            }
        }
        if (checkItemOut) {
            if (recipeMap.getMinOutputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.STEAM_EXPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1));
            }
            else if (recipeMap.getMaxOutputs() > 0) {
                predicate =  predicate.or(abilities(MultiblockAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1));
            }
        }
        return predicate;
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return !(trait instanceof SteamMultiblockRecipeLogic);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());
    }

    @Override
    public SoundEvent getSound() {
        return recipeMap.getSound();
    }

    @Override
    public boolean isActive() {
        return super.isActive() && recipeMapWorkable.isActive() && recipeMapWorkable.isWorkingEnabled();
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks), 176, 216);
        builder.shouldColor(false);
        builder.image(7, 4, 162, 121, GuiTextures.DISPLAY_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks));
        builder.label(11, 9, getMetaFullName(), 0xFFFFFF);
        builder.widget(new AdvancedTextWidget(11, 19, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(156)
                .setClickHandler(this::handleDisplayClick));
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks), 7, 134);
        return builder;
    }
}
