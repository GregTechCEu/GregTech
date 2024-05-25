package gregtech.common.metatileentities.multi.electric;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEOutputHatch;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityDistillationTower extends RecipeMapMultiblockController {

    private final boolean useAdvHatchLogic;

    protected int layerCount;
    protected List<IFluidTank> orderedFluidOutputs;

    public MetaTileEntityDistillationTower(ResourceLocation metaTileEntityId) {
        this(metaTileEntityId, false);
    }

    public MetaTileEntityDistillationTower(ResourceLocation metaTileEntityId, boolean useAdvHatchLogic) {
        super(metaTileEntityId, RecipeMaps.DISTILLATION_RECIPES);
        this.useAdvHatchLogic = useAdvHatchLogic;
        if (useAdvHatchLogic) {
            this.recipeMapWorkable = new DistillationTowerRecipeLogic(this);
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDistillationTower(metaTileEntityId, this.useAdvHatchLogic);
    }

    @Override
    protected Function<BlockPos, Integer> multiblockPartSorter() {
        return RelativeDirection.UP.getSorter(getFrontFacing(), getUpwardsFacing(), isFlipped());
    }

    /**
     * Whether this multi can be rotated or face upwards. <br>
     * There will be <i>consequences</i> if this returns true. Go override {@link #determineOrderedFluidOutputs()}
     */
    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (isStructureFormed()) {
            FluidStack stackInTank = importFluids.drain(Integer.MAX_VALUE, false);
            if (stackInTank != null && stackInTank.amount > 0) {
                ITextComponent fluidName = TextComponentUtil.setColor(GTUtility.getFluidTranslation(stackInTank),
                        TextFormatting.AQUA);
                textList.add(TextComponentUtil.translationWithColor(
                        TextFormatting.GRAY,
                        "gregtech.multiblock.distillation_tower.distilling_fluid",
                        fluidName));
            }
        }
        super.addDisplayText(textList);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        if (!useAdvHatchLogic || this.structurePattern == null) return;
        this.layerCount = determineLayerCount(this.structurePattern);
        this.orderedFluidOutputs = determineOrderedFluidOutputs();
    }

    /**
     * Needs to be overriden for multiblocks that have different assemblies than the standard distillation tower.
     * 
     * @param structurePattern the structure pattern
     * @return the number of layers that <b>could</b> hold output hatches
     */
    protected int determineLayerCount(@NotNull BlockPattern structurePattern) {
        return structurePattern.formedRepetitionCount[1];
    }

    /**
     * Needs to be overriden for multiblocks that have different assemblies than the standard distillation tower.
     * 
     * @return the fluid hatches of the multiblock, in order, with null entries for layers that do not have hatches.
     */
    protected List<IFluidTank> determineOrderedFluidOutputs() {
        List<MetaTileEntityMultiblockPart> fluidExportParts = this.getMultiblockParts().stream()
                .filter(iMultiblockPart -> iMultiblockPart instanceof IMultiblockAbilityPart<?>abilityPart &&
                        abilityPart.getAbility() == MultiblockAbility.EXPORT_FLUIDS &&
                        abilityPart instanceof MetaTileEntityMultiblockPart)
                .map(iMultiblockPart -> (MetaTileEntityMultiblockPart) iMultiblockPart)
                .collect(Collectors.toList());
        // the fluidExportParts should come sorted in smallest Y first, largest Y last.
        List<IFluidTank> orderedTankList = new ObjectArrayList<>();
        int firstY = this.getPos().getY() + 1;
        int exportIndex = 0;
        for (int y = firstY; y < firstY + this.layerCount; y++) {
            if (fluidExportParts.size() <= exportIndex) break;
            MetaTileEntityMultiblockPart first = fluidExportParts.get(exportIndex);
            if (first.getPos().getY() == y) {
                // noinspection unchecked
                ((IMultiblockAbilityPart<IFluidTank>) first).registerAbilities(orderedTankList);
                exportIndex++;
            } else if (first.getPos().getY() > y) {
                orderedTankList.add(null);
            } else {
                GTLog.logger.error("The Distillation Tower at " + this.getPos() +
                        " had a fluid export hatch with an unexpected Y position.");
                this.invalidateStructure();
                return new ObjectArrayList<>();
            }
        }
        return orderedTankList;
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.layerCount = 0;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, UP)
                .aisle("YSY", "YYY", "YYY")
                .aisle("XXX", "X#X", "XXX").setRepeatable(1, 11)
                .aisle("XXX", "XXX", "XXX")
                .where('S', selfPredicate())
                .where('Y', states(getCasingState())
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setExactLimit(1)))
                .where('X', states(getCasingState())
                        .or(metaTileEntities(MultiblockAbility.REGISTRY.get(MultiblockAbility.EXPORT_FLUIDS).stream()
                                .filter(mte -> !(mte instanceof MetaTileEntityMultiFluidHatch) &&
                                        !(mte instanceof MetaTileEntityMEOutputHatch))
                                .toArray(MetaTileEntity[]::new)).setMaxLayerLimited(1, 1))
                        .or(autoAbilities(true, false)))
                .where('#', air())
                .build();
    }

    @Override
    protected boolean allowSameFluidFillForOutputs() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STAINLESS_CLEAN);
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_ELECTRICAL;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.DISTILLATION_TOWER_OVERLAY;
    }

    @Override
    public int getFluidOutputLimit() {
        return this.layerCount;
    }

    protected class DistillationTowerRecipeLogic extends MultiblockRecipeLogic {

        public DistillationTowerRecipeLogic(MetaTileEntityDistillationTower tileEntity) {
            super(tileEntity);
        }

        protected boolean applyFluidToOutputs(List<FluidStack> fluids, boolean doFill) {
            boolean valid = true;
            for (int i = 0; i < fluids.size(); i++) {
                IFluidTank tank = orderedFluidOutputs.get(i);
                // void if no hatch is found on that fluid's layer
                // this is considered trimming and thus ignores canVoid
                if (tank == null) continue;
                int accepted = tank.fill(fluids.get(i), doFill);
                if (accepted != fluids.get(i).amount) valid = false;
                if (!doFill && !valid) break;
            }
            return valid;
        }

        @Override
        protected void outputRecipeOutputs() {
            GTTransferUtils.addItemsToItemHandler(getOutputInventory(), false, itemOutputs);
            this.applyFluidToOutputs(fluidOutputs, true);
        }

        @Override
        protected boolean setupAndConsumeRecipeInputs(@NotNull Recipe recipe,
                                                      @NotNull IItemHandlerModifiable importInventory,
                                                      @NotNull IMultipleTankHandler importFluids) {
            this.overclockResults = calculateOverclock(recipe);

            modifyOverclockPost(overclockResults, recipe.getRecipePropertyStorage());

            if (!hasEnoughPower(overclockResults)) {
                return false;
            }

            IItemHandlerModifiable exportInventory = getOutputInventory();

            // We have already trimmed outputs and chanced outputs at this time
            // Attempt to merge all outputs + chanced outputs into the output bus, to prevent voiding chanced outputs
            if (!metaTileEntity.canVoidRecipeItemOutputs() &&
                    !GTTransferUtils.addItemsToItemHandler(exportInventory, true, recipe.getAllItemOutputs())) {
                this.isOutputsFull = true;
                return false;
            }

            // Perform layerwise fluid checks
            if (!metaTileEntity.canVoidRecipeFluidOutputs() &&
                    !this.applyFluidToOutputs(recipe.getAllFluidOutputs(), false)) {
                this.isOutputsFull = true;
                return false;
            }

            this.isOutputsFull = false;
            if (recipe.matches(true, importInventory, importFluids)) {
                this.metaTileEntity.addNotifiedInput(importInventory);
                return true;
            }
            return false;
        }
    }
}
