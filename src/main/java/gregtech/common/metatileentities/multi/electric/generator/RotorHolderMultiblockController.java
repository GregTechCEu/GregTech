package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityRotorHolder;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;

import static gregtech.api.metatileentity.multiblock.MultiblockAbility.ABILITY_ROTOR_HOLDER;

public abstract class RotorHolderMultiblockController extends FueledMultiblockController {

    public RotorHolderMultiblockController(ResourceLocation metaTileEntityId, FuelRecipeMap recipeMap, long maxVoltage) {
        super(metaTileEntityId, recipeMap, maxVoltage);
    }

    public MetaTileEntityRotorHolder getRotorHolder() {
        return getAbilities(ABILITY_ROTOR_HOLDER).get(0);
    }

    public List<MetaTileEntityRotorHolder> getRotorHolders() {
        return getAbilities(ABILITY_ROTOR_HOLDER);
    }

    @Override
    protected void updateFormedValid() {
        if (isRotorFaceFree()) {
            super.updateFormedValid();
        }
    }

    @Override
    public void invalidateStructure() {
        getRotorHolder().resetRotorSpeed();
        super.invalidateStructure();
    }

    /**
     * @return true if turbine is formed and it's face is free and contains
     * only air blocks in front of rotor holder
     */
    public boolean isRotorFaceFree() {
        if (getAbilities(ABILITY_ROTOR_HOLDER).size() == 0)
            return false;

        return isStructureFormed() && getRotorHolder().isFrontFaceFree();
    }

    /**
     * @return true if structure formed, workable is active and front face is free
     */
    public boolean isActive() {
        return isRotorFaceFree() && workableHandler.isActive() && workableHandler.isWorkingEnabled();
    }

    public abstract int getRotorSpeedIncrement();

    public abstract int getRotorSpeedDecrement();

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return super.getFrontOverlay();
    }
}
