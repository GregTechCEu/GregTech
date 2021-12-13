package gregtech.common.metatileentities.multi.electric.generator;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.sound.ISoundCreator;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.machines.FuelRecipeMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public abstract class FueledMultiblockController extends MultiblockWithDisplayBase implements ISoundCreator {

    protected final FuelRecipeMap recipeMap;
    protected final FuelRecipeLogic workableHandler;
    protected IEnergyContainer energyContainer;
    protected IMultipleTankHandler importFluidHandler;

    public FueledMultiblockController(ResourceLocation metaTileEntityId, FuelRecipeMap recipeMap, long maxVoltage) {
        super(metaTileEntityId);
        this.recipeMap = recipeMap;
        this.workableHandler = createWorkable(maxVoltage);
    }

    protected FuelRecipeLogic createWorkable(long maxVoltage) {
        return new FuelRecipeLogic(this, recipeMap,
                () -> energyContainer,
                () -> importFluidHandler, maxVoltage);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            if (!workableHandler.isWorkingEnabled()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));
            } else if (workableHandler.isActive()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.running"));
                textList.add(new TextComponentTranslation("gregtech.multiblock.generation_eu", workableHandler.getRecipeOutputVoltage()));
            } else {
                textList.add(new TextComponentTranslation("gregtech.multiblock.idling"));
            }
        }
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

    private void initializeAbilities() {
        this.importFluidHandler = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.OUTPUT_ENERGY));
    }

    private void resetTileAbilities() {
        this.importFluidHandler = null;
        this.energyContainer = null;
    }

    @Override
    protected void updateFormedValid() {
        if (!hasMufflerMechanics() || isMufflerFaceFree())
            this.workableHandler.update();
    }

    public boolean isActive() {
        return isStructureFormed() && workableHandler.isActive();
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return !(trait instanceof FuelRecipeLogic);
    }

    @Override
    public TraceabilityPredicate autoAbilities() {
        return autoAbilities(true, true, true, true, true);
    }

    public TraceabilityPredicate autoAbilities(boolean checkEnergyOut,
                                               boolean checkMaintainer,
                                               boolean checkFluidIn,
                                               boolean checkFluidOutput,
                                               boolean checkMuffler) {
        TraceabilityPredicate predicate = super.autoAbilities(checkMaintainer, checkMuffler)
                .or(checkEnergyOut ? abilities(MultiblockAbility.OUTPUT_ENERGY) : new TraceabilityPredicate());
        if (checkFluidIn) {
            predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1));
        }
        if (checkFluidOutput) {
            predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1));
        }
        return predicate;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                isStructureFormed() && workableHandler.isActive(), workableHandler.isWorkingEnabled());
    }

    @Override
    public void onAttached(Object... data) {
        super.onAttached(data);
        if (getWorld() != null && getWorld().isRemote) {
            this.setupSound(workableHandler.recipeMap.getSound(), this.getPos());
        }
    }

    public boolean canCreateSound() {
        return isActive();
    }

}
