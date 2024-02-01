package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public abstract class MetaTileEntityAbstractTurbine extends MultiblockWithDisplayBase {

    public MetaTileEntityAbstractTurbine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {

    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCC", "CHHC", "CCCC")
                .aisle("CHHC", "RGGR", "CHHC")
                .aisle("CCCC", "CSHC", "CCCC")
                .where('S', selfPredicate())
                .where('G', states(getGearboxState()))
                .where('C', states(getCasingState()))
                .where('R', rotorHolderPredicate()
                        .or(abilities(MultiblockAbility.OUTPUT_ENERGY).setExactLimit(1)))
                .where('H', states(getCasingState())
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1))
                        .or(autoAbilities(true, true))) // maintenance, muffler
                .build();
    }

    protected abstract IBlockState getGearboxState();

    protected abstract IBlockState getCasingState();

    protected abstract int minHolderTier();

    protected abstract int maxHolderTier();

    private TraceabilityPredicate rotorHolderPredicate() {
        final int minTier = minHolderTier();
        final int maxTier = maxHolderTier();
        if (minTier > maxTier) {
            throw new IllegalStateException("Turbine with type " + this.getClass().getCanonicalName() +
                    " has minimum rotor holder tier larger than maximum!");
        }

        return metaTileEntities(MultiblockAbility.REGISTRY.get(MultiblockAbility.ROTOR_HOLDER).stream()
                .filter(mte -> mte instanceof ITieredMetaTileEntity tieredMte
                        && tieredMte.getTier() >= minTier && tieredMte.getTier() <= maxTier)
                .toArray(MetaTileEntity[]::new))
                .addTooltips("gregtech.multiblock.pattern.clear_amount_3")
                .addTooltip("gregtech.multiblock.pattern.error.limited.1", GTValues.VN[minTier])
                .addTooltip("gregtech.multiblock.pattern.error.limited.0", GTValues.VN[maxTier])
                .setExactLimit(1);
    }
}
