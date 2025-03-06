package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.IObjectHolder;
import gregtech.api.capability.IOpticalComputationHatch;
import gregtech.api.capability.IOpticalComputationProvider;
import gregtech.api.capability.IOpticalComputationReceiver;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStallType;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStandardStateMachineBuilder;
import gregtech.api.recipes.logic.statemachine.overclock.RecipeNoOverclockingOperator;
import gregtech.api.recipes.logic.statemachine.running.RecipeCleanupOperation;
import gregtech.api.recipes.logic.statemachine.running.RecipeComputationFinalizer;
import gregtech.api.recipes.logic.statemachine.running.RecipeProgressOperation;
import gregtech.api.recipes.lookup.property.MaxCWUtProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityResearchStation extends RecipeMapMultiblockController
                                           implements IOpticalComputationReceiver {

    private IOpticalComputationProvider computationProvider;
    private IObjectHolder objectHolder;

    protected boolean lackedComputation;

    public MetaTileEntityResearchStation(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.RESEARCH_STATION_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityResearchStation(metaTileEntityId);
    }

    @Override
    protected void modifyRecipeLogicStandardBuilder(RecipeStandardStateMachineBuilder builder) {
        super.modifyRecipeLogicStandardBuilder(builder);
        builder.setFinalCheck(run -> {
            IObjectHolder holder = getObjectHolder();
            if (!run.getItemsConsumed().isEmpty() && !run.getItemsConsumed().get(0)
                    .isItemEqual(holder.getHeldItem(false)))
                return false;
            holder.setLocked(true);
            return true;
        }).setOutputOperation(() -> workerNBT -> {
            NBTTagCompound recipe = workerNBT.getCompoundTag(RecipeCleanupOperation.STANDARD_RECIPE_KEY);
            NBTTagList list = recipe.getTagList("ItemsOut", Constants.NBT.TAG_COMPOUND);
            IObjectHolder holder = getObjectHolder();
            holder.setHeldItem(ItemStack.EMPTY);

            ItemStack outputItem = ItemStack.EMPTY;
            if (!list.isEmpty()) {
                outputItem = new ItemStack(list.getCompoundTagAt(0));
            }
            holder.setDataItem(outputItem);
            holder.setLocked(false);
        })
                .setOverclockFactory(RecipeNoOverclockingOperator::create)
                .setStallType(RecipeStallType.PAUSE)
                .setRecipeFinalizer(RecipeComputationFinalizer.STANDARD_INSTANCE)
                .setPerTickRecipeCheck((recipe) -> {
                    double progress = recipe.getInteger(RecipeProgressOperation.STANDARD_PROGRESS_KEY);
                    double maxProgress = recipe.getDouble("Duration");
                    double actualProgress = Math.min(1, maxProgress - progress);

                    int perTick = RecipeComputationFinalizer.getCWUt(recipe);

                    int draw = getComputationProvider().requestCWUt(perTick, false);
                    if (draw < perTick) {
                        lackedComputation = true;
                        return false;
                    } else {
                        lackedComputation = false;
                    }
                    getComputationProvider().requestCWUt(perTick, false);

                    long voltage = recipe.getLong("Voltage");
                    long amperage = recipe.getLong("Amperage");
                    long eut = (long) (actualProgress * voltage * amperage);
                    boolean generating = recipe.getBoolean("Generating");
                    if (!generating) {
                        return Math.abs(getEnergyContainer().removeEnergy(eut)) >= eut;
                    } else {
                        getEnergyContainer().addEnergy(eut);
                        return true;
                    }
                })
                .setProgressOperationOverride((b, s) -> {
                    b.andThenDefault(d -> {
                        NBTTagCompound recipe = d.getCompoundTag(RecipeCleanupOperation.STANDARD_RECIPE_KEY);
                        if (!d.getBoolean("RecipeCheckSuccess")) {
                            if (s == RecipeStallType.RESET) {
                                recipe.setInteger(RecipeProgressOperation.STANDARD_PROGRESS_KEY, 0);
                            } else if (s == RecipeStallType.DEGRESS) {
                                int prog = recipe.getInteger(RecipeProgressOperation.STANDARD_PROGRESS_KEY);
                                if (prog > 0) {
                                    recipe.setInteger(RecipeProgressOperation.STANDARD_PROGRESS_KEY,
                                            Math.max(0, prog - 2));
                                }
                            } else {
                                return;
                            }
                        }
                        int total = RecipeComputationFinalizer.getTotalCWU(recipe);
                        int progress;
                        if (total == 0) {
                            progress = 1;
                        } else {
                            progress = getComputationProvider().requestCWUt(total, false);

                        }
                        recipe.setInteger(RecipeProgressOperation.STANDARD_PROGRESS_KEY,
                                recipe.getInteger(RecipeProgressOperation.STANDARD_PROGRESS_KEY) + progress);
                    }, false);
                });
    }

    @Override
    protected @NotNull PropertySet computePropertySet() {
        PropertySet set = super.computePropertySet();
        set.add(new MaxCWUtProperty(getComputationProvider().requestCWUt(Integer.MAX_VALUE, true)));
        return super.computePropertySet();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        List<IOpticalComputationHatch> providers = getAbilities(MultiblockAbility.COMPUTATION_DATA_RECEPTION);
        if (providers != null && providers.size() >= 1) {
            computationProvider = providers.get(0);
        }
        List<IObjectHolder> holders = getAbilities(MultiblockAbility.OBJECT_HOLDER);
        if (holders != null && holders.size() >= 1) {
            objectHolder = holders.get(0);
            // cannot set in initializeAbilities since super() calls it before setting the objectHolder field here
            this.inputInventory = new ItemHandlerList(Collections.singletonList(objectHolder.getAsHandler()));
        }

        // should never happen, but would rather do this than have an obscure NPE
        if (computationProvider == null || objectHolder == null) {
            invalidateStructure();
        }
    }

    // force object holder to be facing the controller
    @Override
    public void checkStructurePattern() {
        super.checkStructurePattern();
        if (isStructureFormed() && objectHolder.getFrontFacing() != getFrontFacing().getOpposite()) {
            invalidateStructure();
        }
    }

    @Override
    public void invalidateStructure() {
        computationProvider = null;
        // recheck the ability to make sure it wasn't the one broken
        List<IObjectHolder> holders = getAbilities(MultiblockAbility.OBJECT_HOLDER);
        if (holders != null && holders.size() >= 1 && holders.get(0) == objectHolder) {
            objectHolder.setLocked(false);
        }
        objectHolder = null;
        super.invalidateStructure();
    }

    @Override
    public IOpticalComputationProvider getComputationProvider() {
        return computationProvider;
    }

    public IObjectHolder getObjectHolder() {
        return objectHolder;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "VVV", "PPP", "PPP", "PPP", "VVV", "XXX")
                .aisle("XXX", "VAV", "AAA", "AAA", "AAA", "VAV", "XXX")
                .aisle("XXX", "VAV", "XAX", "XSX", "XAX", "VAV", "XXX")
                .aisle("XXX", "XAX", "---", "---", "---", "XAX", "XXX")
                .aisle(" X ", "XAX", "---", "---", "---", "XAX", " X ")
                .aisle(" X ", "XAX", "-A-", "-H-", "-A-", "XAX", " X ")
                .aisle("   ", "XXX", "---", "---", "---", "XXX", "   ")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()))
                .where(' ', any())
                .where('-', air())
                .where('V', states(getVentState()))
                .where('A', states(getAdvancedState()))
                .where('P', states(getCasingState())
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1))
                        .or(maintenancePredicate())
                        .or(abilities(MultiblockAbility.COMPUTATION_DATA_RECEPTION).setExactLimit(1)))
                .where('H', abilities(MultiblockAbility.OBJECT_HOLDER))
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        return Collections.singletonList(MultiblockShapeInfo.builder(RIGHT, DOWN, FRONT)
                .aisle("XXX", "VVV", "POP", "PEP", "PMP", "VVV", "XXX")
                .aisle("XXX", "VAV", "AAA", "AAA", "AAA", "VAV", "XXX")
                .aisle("XXX", "VAV", "XAX", "XSX", "XAX", "VAV", "XXX")
                .aisle("XXX", "XAX", "---", "---", "---", "XAX", "XXX")
                .aisle("-X-", "XAX", "---", "---", "---", "XAX", "-X-")
                .aisle("-X-", "XAX", "-A-", "-H-", "-A-", "XAX", "-X-")
                .aisle("---", "XXX", "---", "---", "---", "XXX", "---")
                .where('S', MetaTileEntities.RESEARCH_STATION, EnumFacing.SOUTH)
                .where('X', getCasingState())
                .where('-', Blocks.AIR.getDefaultState())
                .where('V', getVentState())
                .where('A', getAdvancedState())
                .where('P', getCasingState())
                .where('O', MetaTileEntities.COMPUTATION_HATCH_RECEIVER, EnumFacing.NORTH)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LuV], EnumFacing.NORTH)
                .where('M',
                        () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                                getCasingState(),
                        EnumFacing.NORTH)
                .where('H', MetaTileEntities.OBJECT_HOLDER, EnumFacing.NORTH)
                .build());
    }

    @NotNull
    private static IBlockState getVentState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.COMPUTER_HEAT_VENT);
    }

    @NotNull
    private static IBlockState getAdvancedState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING);
    }

    @NotNull
    private static IBlockState getCasingState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.COMPUTER_CASING);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart == null || sourcePart instanceof IObjectHolder) {
            return Textures.ADVANCED_COMPUTER_CASING;
        }
        return Textures.COMPUTER_CASING;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.RESEARCH_STATION_OVERLAY;
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }

    // let it think it can "void" since we replace an input item with the finished
    // item on completion, instead of outputting into a dedicated output slot.
    @Override
    public boolean canVoidRecipeItemOutputs() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.research_station.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.research_station.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.research_station.tooltip.3"));
        tooltip.add(I18n.format("gregtech.machine.research_station.tooltip.4"));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(isWorkingEnabled(), isActive())
                .setWorkingStatusKeys(
                        "gregtech.multiblock.idling",
                        "gregtech.multiblock.work_paused",
                        "gregtech.machine.research_station.researching")
                .addEnergyUsageLine(getEnergyContainer())
                .addEnergyTierLine(GTUtility.getTierByVoltage(getEnergyContainer().getInputVoltage()))
                // .addComputationUsageExactLine(getRecipeMapWorkable().getCurrentDrawnCWUt())
                .addWorkingStatusLine();
        // .addProgressLine(recipeProgressPercent());
        // TODO multiple recipe display
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed(), false)
                .addLowPowerLine(insufficientEnergy())
                .addLowComputationLine(lackedComputation)
                .addMaintenanceProblemLines(getMaintenanceProblems());
    }
}
