package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.IndicatorImageWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStandardStateMachineBuilder;
import gregtech.api.recipes.logic.statemachine.running.RecipeProgressOperation;
import gregtech.api.recipes.logic.workable.RecipeSteamWorkable;
import gregtech.api.util.FacingPos;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public abstract class RecipeMapSteamMultiblockController extends MultiblockWithDisplayBase implements IControllable,
                                                         RecipeSteamWorkable.ISupportsRecipeSteamWorkable {

    protected static final double CONVERSION_RATE = ConfigHolder.machines.multiblockSteamToEU;

    public final RecipeMap<?> recipeMap;
    protected RecipeSteamWorkable workable;

    protected IItemHandlerModifiable inputInventory;
    protected IItemHandlerModifiable outputInventory;
    protected IMultipleTankHandler steamFluidTank;

    protected Deque<ItemStack> bufferedItemOutputs;
    protected boolean awaitingItemOutputSpace;

    protected final double conversionRate;

    public RecipeMapSteamMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                              double conversionRate) {
        super(metaTileEntityId);
        this.recipeMap = recipeMap;
        this.conversionRate = conversionRate;
        this.bufferedItemOutputs = new ArrayDeque<>();
        RecipeStandardStateMachineBuilder std = new RecipeStandardStateMachineBuilder(recipeMap::getLookup);
        modifyRecipeLogicStandardBuilder(std);
        this.workable = new RecipeSteamWorkable(this, std);
        resetTileAbilities();
    }

    /**
     * Called during initialization. Should not require any fields to be populated for the state machine to be
     * constructed.
     * Override this if you merely need to set up the standard state machine builder.
     */
    protected void modifyRecipeLogicStandardBuilder(RecipeStandardStateMachineBuilder builder) {
        builder.setOffthreadSearchAndSetup(true)
                .setDownTransformForParallels(true)
                .setParallelLimit(this::getBaseParallelLimit)
                .setDurationDiscount(() -> 1.5)
                .setMaintenance(this::getMaintenanceValues)
                .setItemInput(this::getInputInventory)
                .setProperties(this::computePropertySet)
                .setItemOutput(bufferedItemOutputs::addAll)
                .setItemTrim(this::getItemOutputLimit)
                .setNotifiedItemInputs(this::getNotifiedItemInputList)
                .setItemOutAmountLimit(() -> getOutputInventory().getSlots() * 64)
                .setItemOutStackLimit(() -> getOutputInventory().getSlots())
                .setPerTickRecipeCheck((recipe) -> {
                    double progress = recipe.getInteger(RecipeProgressOperation.STANDARD_PROGRESS_KEY);
                    double maxProgress = recipe.getDouble("Duration");
                    long voltage = recipe.getLong("Voltage");
                    long amperage = recipe.getLong("Amperage");
                    long eut = (long) (Math.min(1, maxProgress - progress) * voltage * amperage);
                    boolean generating = recipe.getBoolean("Generating");
                    if (!generating) {
                        FluidStack drain = getSteamFluidTank().drain((int) (eut * conversionRate), true);
                        return (drain == null ? 0 : drain.amount) >= eut;
                    } else {
                        return true;
                    }
                });
    }

    public IMultipleTankHandler getSteamFluidTank() {
        return steamFluidTank;
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
        updateBufferedOutputs();
    }

    @Override
    public boolean shouldRecipeWorkableUpdate() {
        return isStructureFormed() && !isStructureObstructed();
    }

    @Override
    public boolean areOutputsClogged() {
        updateBufferedOutputs();
        return awaitingItemOutputSpace;
    }

    protected void updateBufferedOutputs() {
        if (awaitingItemOutputSpace && !getNotifiedItemOutputList().isEmpty()) {
            awaitingItemOutputSpace = false;
        }
        getNotifiedItemOutputList().clear();
        getNotifiedFluidInputList().clear();
        if (!awaitingItemOutputSpace) {
            while (!bufferedItemOutputs.isEmpty()) {
                ItemStack first = bufferedItemOutputs.removeFirst();
                ItemStack remainder = GTTransferUtils.insertItem(getOutputInventory(), first, false);
                if (!remainder.isEmpty() && !canVoidRecipeItemOutputs()) {
                    bufferedItemOutputs.addFirst(remainder);
                    awaitingItemOutputSpace = true;
                    break;
                }
            }
        }
    }

    protected void initializeAbilities() {
        this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.STEAM_IMPORT_ITEMS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.STEAM_EXPORT_ITEMS));
        this.steamFluidTank = new FluidTankList(true, getAbilities(MultiblockAbility.STEAM));
    }

    protected void resetTileAbilities() {
        this.inputInventory = new GTItemStackHandler(this, 0);
        this.outputInventory = new GTItemStackHandler(this, 0);
        this.steamFluidTank = new FluidTankList(true);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(isWorkingEnabled(), isActive())
                .addCustom(tl -> {
                    // custom steam tank line
                    int steam = 0;
                    int sCapacity = 0;
                    for (int i = 0; i < getSteamFluidTank().getTanks(); i++) {
                        IMultipleTankHandler.MultiFluidTankEntry tank = getSteamFluidTank().getTankAt(i);
                        steam += tank.getFluidAmount();
                        sCapacity += tank.getCapacity();
                    }
                    if (sCapacity > 0) {
                        String stored = TextFormattingUtil.formatNumbers(steam);
                        String capacity = TextFormattingUtil.formatNumbers(sCapacity);

                        ITextComponent steamInfo = TextComponentUtil.stringWithColor(
                                TextFormatting.BLUE,
                                stored + " / " + capacity + " L");

                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "gregtech.multiblock.steam.steam_stored",
                                steamInfo));
                    }
                })
                .addParallelsLine(getBaseParallelLimit())
                .addWorkingStatusLine();
        // .addProgressLine(recipeMapWorkable.getProgressPercent());
        // TODO multiple recipe display
    }

    protected int getBaseParallelLimit() {
        return 1;
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed(), false)
                .addCustom(tl -> {
                    if (isStructureFormed() && insufficientSteam()) {
                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.YELLOW,
                                "gregtech.multiblock.steam.low_steam"));
                    }
                })
                .addMaintenanceProblemLines(getMaintenanceProblems());
    }

    @Override
    public boolean isWorkingEnabled() {
        return workable.getProgressAndComplete().isLogicEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (isWorkingAllowed != workable.getProgressAndComplete().isLogicEnabled()) {
            workable.getProgressAndComplete().setLogicEnabled(isWorkingAllowed);
            workable.getLookupAndSetup().setLogicEnabled(isWorkingAllowed);
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, b -> b.writeBoolean(isWorkingAllowed));
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            setWorkingEnabled(buf.readBoolean());
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
                .or(checkSteam ? abilities(MultiblockAbility.STEAM).setMinGlobalLimited(1).setPreviewCount(1) :
                        new TraceabilityPredicate());
        if (checkItemIn) {
            if (recipeMap.getMaxInputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.STEAM_IMPORT_ITEMS).setPreviewCount(1));
            }
        }
        if (checkItemOut) {
            if (recipeMap.getMaxOutputs() > 0) {
                predicate = predicate.or(abilities(MultiblockAbility.STEAM_EXPORT_ITEMS).setPreviewCount(1));
            }
        }
        return predicate;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                isActive(), isWorkingEnabled());
    }

    @Override
    public SoundEvent getSound() {
        return recipeMap.getSound();
    }

    @Override
    public boolean isActive() {
        return workable.isRunning();
    }

    protected boolean insufficientSteam() {
        if (!isActive()) return false;
        // if the steam container has less than a tenth of its capacity, we're probably low on steam.
        int steam = 0;
        int capacity = 0;
        for (int i = 0; i < getSteamFluidTank().getTanks(); i++) {
            IMultipleTankHandler.MultiFluidTankEntry tank = getSteamFluidTank().getTankAt(i);
            steam += tank.getFluidAmount();
            capacity += tank.getCapacity();
        }
        return steam <= capacity * 0.1;
    }

    public IItemHandlerModifiable getInputInventory() {
        return inputInventory;
    }

    public IItemHandlerModifiable getOutputInventory() {
        return outputInventory;
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI
                .builder(GuiTextures.BACKGROUND_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks), 176, 208);
        builder.shouldColor(false);
        builder.image(4, 4, 168, 117, GuiTextures.DISPLAY_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks));
        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
        builder.widget(new AdvancedTextWidget(9, 20, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(162)
                .setClickHandler(this::handleDisplayClick));
        builder.widget(new IndicatorImageWidget(152, 101, 17, 17, getLogo())
                .setWarningStatus(getWarningLogo(), this::addWarningText)
                .setErrorStatus(getErrorLogo(), this::addErrorText));
        builder.bindPlayerInventory(entityPlayer.inventory,
                GuiTextures.SLOT_STEAM.get(ConfigHolder.machines.steelSteamMultiblocks), 7, 125);
        return builder;
    }

    @Override
    public @NotNull Collection<FacingPos> getVentingBlockFacings() {
        return Collections.singleton(new FacingPos(getPos(), getFrontFacing()));
    }

    @Override
    public float getVentingDamage() {
        return 0;
    }
}
