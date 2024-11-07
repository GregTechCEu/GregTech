package gregtech.common.metatileentities.multi.electric;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityMechanicalDraftCooler extends RecipeMapMultiblockController {

    protected final boolean dry;

    public MetaTileEntityMechanicalDraftCooler(ResourceLocation metaTileEntityId, boolean dry) {
        super(metaTileEntityId, RecipeMaps.INDUSTRIAL_COOLING_RECIPES);
        this.dry = dry;
    }

    @Override
    protected void initializeAbilities() {
        if (dry) {
            super.initializeAbilities();
        } else {
            this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
            this.inputFluidInventory = new FluidTankList(allowSameFluidFillForOutputs(),
                    getAbilities(MultiblockAbility.IMPORT_FLUIDS));
            this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
            this.outputFluidInventory = new FluidTankList(allowSameFluidFillForOutputs(),
                    getAbilities(MultiblockAbility.EXPORT_FLUIDS));

            List<IEnergyContainer> inputEnergy = new ArrayList<>(getAbilities(MultiblockAbility.INPUT_ENERGY));
            inputEnergy.addAll(getAbilities(MultiblockAbility.SUBSTATION_INPUT_ENERGY));
            inputEnergy.addAll(getAbilities(MultiblockAbility.INPUT_LASER));
            this.energyContainer = new WaterEnergyContainer(inputEnergy, inputFluidInventory);
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMechanicalDraftCooler(metaTileEntityId, dry);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return null;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return dry ? Textures.BRASS_PLATED_BRICKS : Textures.BRONZE_PLATED_BRICKS;
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return Textures.MECHANICAL_DRAFT_COOLER_OVERLAY;
    }

    private static class WaterEnergyContainer extends EnergyContainerList {

        protected final @NotNull IFluidHandler waterTank;

        public WaterEnergyContainer(@NotNull List<IEnergyContainer> energyContainerList,
                                    @NotNull IFluidHandler waterTank) {
            super(energyContainerList);
            this.waterTank = waterTank;
        }

        @Override
        public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
            long allowed = super.acceptEnergyFromNetwork(side, voltage, amperage);
            waterTank.fill(Materials.Water.getFluid(GTUtility.safeCastLongToInt(allowed)), true);
            return allowed;
        }

        @Override
        public boolean inputsEnergy(EnumFacing side) {
            return false;
        }

        @Override
        public long changeEnergy(long differenceAmount) {
            differenceAmount = super.changeEnergy(differenceAmount);
            if (differenceAmount > 0) {
                return waterTank.fill(Materials.Water.getFluid(GTUtility.safeCastLongToInt(differenceAmount)), true);
            } else if (differenceAmount < 0) {
                // drain water first, then resort to distilled water
                FluidStack drain = waterTank
                        .drain(Materials.Water.getFluid(GTUtility.safeCastLongToInt(differenceAmount)), true);
                long amount = drain == null ? 0 : drain.amount;
                if (differenceAmount > amount) {
                    drain = waterTank.drain(
                            Materials.DistilledWater.getFluid(GTUtility.safeCastLongToInt(differenceAmount - amount)),
                            true);
                    amount += drain == null ? 0 : drain.amount;
                }
                return amount;
            }
            return 0;
        }

        @Override
        public long getEnergyStored() {
            FluidStack drain = waterTank.drain(Materials.Water.getFluid(Integer.MAX_VALUE), false);
            long amount = drain == null ? 0 : drain.amount;
            drain = waterTank.drain(Materials.DistilledWater.getFluid(Integer.MAX_VALUE), false);
            amount += drain == null ? 0 : drain.amount;
            return Math.min(amount, super.getEnergyStored());
        }

        @Override
        public long getEnergyCapacity() {
            return Math.min(getEnergyStored() + waterTank.fill(Materials.Water.getFluid(Integer.MAX_VALUE), false),
                    super.getEnergyCapacity());
        }
    }
}
