package gregtech.api.metatileentity;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.*;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.recipes.FluidKey;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public abstract class WorkableTieredMetaTileEntity extends TieredMetaTileEntity implements IDataInfoProvider, ICleanroomReceiver {

    protected static Set<FluidKey> fluidKeyCache;

    protected final RecipeLogicEnergy workable;
    protected final RecipeMap<?> recipeMap;
    protected final ICubeRenderer renderer;

    private final Function<Integer, Integer> tankScalingFunction;

    public final boolean handlesRecipeOutputs;

    private ICleanroomProvider cleanroom;

    public WorkableTieredMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier,
                                        Function<Integer, Integer> tankScalingFunction) {
        this(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction, true);
    }

    public WorkableTieredMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier,
                                        Function<Integer, Integer> tankScalingFunction, boolean handlesRecipeOutputs) {
        super(metaTileEntityId, tier);
        this.renderer = renderer;
        this.handlesRecipeOutputs = handlesRecipeOutputs;
        this.workable = createWorkable(recipeMap);
        this.recipeMap = recipeMap;
        this.tankScalingFunction = tankScalingFunction;
        initializeInventory();
        reinitializeEnergyContainer();
    }

    protected RecipeLogicEnergy createWorkable(RecipeMap<?> recipeMap) {
        return new RecipeLogicEnergy(this, recipeMap, () -> energyContainer);
    }

    @Override
    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        if (isEnergyEmitter()) {
            this.energyContainer = EnergyContainerHandler.emitterContainer(this,
                    tierVoltage * 64L, tierVoltage, getMaxInputOutputAmperage());
        } else this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 64L, tierVoltage, 2, 0L, 0L) {
            @Override
            public long getInputAmperage() {
                if (getEnergyCapacity() / 2 > getEnergyStored() && workable.isActive()) {
                    return 2;
                }
                return 1;
            }
        };
    }

    @Override
    protected long getMaxInputOutputAmperage() {
        return 2L;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        renderer.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), workable.isActive(), workable.isWorkingEnabled());
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        if (workable == null) return new ItemStackHandler(0);
        return new NotifiableItemStackHandler(workable.getRecipeMap().getMaxInputs(), this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        if (workable == null) return new ItemStackHandler(0);
        return new NotifiableItemStackHandler(workable.getRecipeMap().getMaxOutputs(), this, true);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        if (workable == null) return new FluidTankList(false);
        FilteredFluidHandler[] fluidImports = new FilteredFluidHandler[workable.getRecipeMap().getMaxFluidInputs()];
        for (int i = 0; i < fluidImports.length; i++) {
            NotifiableFilteredFluidHandler filteredFluidHandler = new NotifiableFilteredFluidHandler(this.tankScalingFunction.apply(this.getTier()), this, false);
            filteredFluidHandler.setFillPredicate(this::canInputFluid);
            fluidImports[i] = filteredFluidHandler;
        }
        return new FluidTankList(false, fluidImports);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        if (workable == null) return new FluidTankList(false);
        FluidTank[] fluidExports = new FluidTank[workable.getRecipeMap().getMaxFluidOutputs()];
        for (int i = 0; i < fluidExports.length; i++) {
            fluidExports[i] = new NotifiableFluidTank(this.tankScalingFunction.apply(this.getTier()), this, true);
        }
        return new FluidTankList(false, fluidExports);
    }

    protected boolean canInputFluid(FluidStack inputFluid) {
        RecipeMap<?> recipeMap = workable.getRecipeMap();
        if (recipeMap.canInputFluidForce(inputFluid.getFluid())) {
            return true; // RecipeMap forces input
        }
        Collection<Recipe> inputCapableRecipes = recipeMap.getRecipesForFluid(inputFluid);
        if (inputCapableRecipes.isEmpty()) {
            // Fluid cannot be inputted anyway, short-circuit and inform that the fluid cannot be inputted
            return false;
        }
        boolean hasEmpty = false;
        Collection<FluidKey> fluids = null;
        for (IFluidTank fluidTank : importFluids) {
            FluidStack fluidInTank = fluidTank.getFluid();
            if (fluidInTank != null) {
                if (inputFluid.isFluidEqual(fluidInTank)) {
                    // Both fluids are equal, short-circuit and inform that the fluid can be inputted
                    return true;
                } else {
                    if (fluids == null) {
                        // Avoid object allocation + array expansion as this is a hotspot
                        if (fluidKeyCache == null) {
                            fluids = new ObjectArraySet<>(new FluidKey[] { new FluidKey(fluidInTank) });
                        } else {
                            fluidKeyCache.clear();
                            fluids = fluidKeyCache;
                        }
                    } else {
                        fluids.add(new FluidKey(fluidInTank));
                    }
                }
            } else {
                hasEmpty = true;
            }
        }
        if (!hasEmpty) {
            // No empty slots to fill input in, inform that the fluid cannot be inputted
            return false;
        }
        if (fluids == null) {
            // There are empty slots to fill, and there are no other fluids, inform that the fluid can be inputted
            return true;
        }
        for (FluidKey fluidKey : fluids) {
            Collection<Recipe> iter = recipeMap.getRecipesForFluid(fluidKey);
            Collection<Recipe> check;
            // Iterate with the smaller collection as base
            if (iter.size() > inputCapableRecipes.size()) {
                check = iter;
                iter = inputCapableRecipes;
            } else {
                check = inputCapableRecipes;
            }
            for (Recipe it : iter) {
                for (Recipe ch : check) {
                    // Check identity equality, fast-track instead of doing Recipe#equals' tedious checks
                    if (it == ch) {
                        // Recipe exists in both collections, inform that the fluid can be inputted
                        return true;
                    }
                }
            }
        }
        // Ultimatum
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        if (workable.getRecipeMap().getMaxFluidInputs() != 0)
            tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", this.tankScalingFunction.apply(getTier())));
    }

    public Function<Integer, Integer> getTankScalingFunction() {
        return tankScalingFunction;
    }

    public boolean isActive() {
        return workable.isActive() && workable.isWorkingEnabled();
    }

    @Override
    public SoundEvent getSound() {
        return workable.getRecipeMap().getSound();
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();

        if (workable != null) {
            list.add(new TextComponentTranslation("behavior.tricorder.workable_progress",
                    new TextComponentTranslation(GTUtility.formatNumbers(workable.getProgress() / 20)).setStyle(new Style().setColor(TextFormatting.GREEN)),
                    new TextComponentTranslation(GTUtility.formatNumbers(workable.getMaxProgress() / 20)).setStyle(new Style().setColor(TextFormatting.YELLOW))
            ));

            if (energyContainer != null) {
                list.add(new TextComponentTranslation("behavior.tricorder.workable_stored_energy",
                        new TextComponentTranslation(GTUtility.formatNumbers(energyContainer.getEnergyStored())).setStyle(new Style().setColor(TextFormatting.GREEN)),
                        new TextComponentTranslation(GTUtility.formatNumbers(energyContainer.getEnergyCapacity())).setStyle(new Style().setColor(TextFormatting.YELLOW))
                ));
            }
            // multi amp recipes: change 0 ? 0 : 1 to 0 ? 0 : amperage
            if (workable.getRecipeEUt() > 0) {
                list.add(new TextComponentTranslation("behavior.tricorder.workable_consumption",
                        new TextComponentTranslation(GTUtility.formatNumbers(workable.getRecipeEUt())).setStyle(new Style().setColor(TextFormatting.RED)),
                        new TextComponentTranslation(GTUtility.formatNumbers(workable.getRecipeEUt() == 0 ? 0 : 1)).setStyle(new Style().setColor(TextFormatting.RED))
                ));
            } else {
                list.add(new TextComponentTranslation("behavior.tricorder.workable_production",
                        new TextComponentTranslation(GTUtility.formatNumbers(workable.getRecipeEUt() * -1)).setStyle(new Style().setColor(TextFormatting.RED)),
                        new TextComponentTranslation(GTUtility.formatNumbers(workable.getRecipeEUt() == 0 ? 0 : 1)).setStyle(new Style().setColor(TextFormatting.RED))
                ));
            }
        }

        return list;
    }

    @Nullable
    @Override
    public ICleanroomProvider getCleanroom() {
        return this.cleanroom;
    }

    @Override
    public void setCleanroom(ICleanroomProvider provider) {
        this.cleanroom = provider;
    }
}
