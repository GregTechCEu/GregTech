package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IRotorHolder;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.recipes.recipes.FuelRecipe;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.MetaFluids;
import gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityRotorHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

import static gregtech.api.metatileentity.multiblock.MultiblockAbility.ABILITY_ROTOR_HOLDER;

public class MetaTileEntityLargeTurbine2 extends FuelMultiblockController implements ITieredMetaTileEntity {

    public final int tier;

    public final IBlockState casingState;
    public final IBlockState gearboxState;
    public final ICubeRenderer casingRenderer;
    public final boolean hasMufflerHatch;
    public final ICubeRenderer frontOverlay;

    private static final int MIN_DURABILITY_TO_WARN = 10;

    public IFluidHandler exportFluidHandler;

    public MetaTileEntityLargeTurbine2(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier, IBlockState casingState, IBlockState gearboxState, ICubeRenderer casingRenderer, boolean hasMufflerHatch, ICubeRenderer frontOverlay) {
        super(metaTileEntityId, recipeMap, tier);
        this.casingState = casingState;
        this.gearboxState = gearboxState;
        this.casingRenderer = casingRenderer;
        this.hasMufflerHatch = hasMufflerHatch;
        this.frontOverlay = frontOverlay;
        this.tier = tier;
        this.recipeMapWorkable = new LargeTurbineWorkableHandler2(this, tier);
        this.recipeMapWorkable.enableOverclockVoltage();
        this.recipeMapWorkable.setOverclockTier(tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeTurbine2(metaTileEntityId, recipeMap, tier, casingState, gearboxState, casingRenderer, hasMufflerHatch, frontOverlay);
    }

    public IRotorHolder getRotorHolder() {
        List<IRotorHolder> abilities = getAbilities(MultiblockAbility.ABILITY_ROTOR_HOLDER_2);
        if (abilities.isEmpty())
            return null;
        return abilities.get(0);
    }

//    @Override
//    protected void updateFormedValid() {
//        if (isRotorFaceFree()) {
//            super.updateFormedValid();
//        }
//    }

    @Override
    public void invalidateStructure() {
        this.exportFluidHandler = null;
        super.invalidateStructure();
    }

//    /**
//     * @return true if turbine is formed and it's face is free and contains
//     * only air blocks in front of rotor holder
//     */
//    public boolean isRotorFaceFree() {
//        if (getAbilities(ABILITY_ROTOR_HOLDER).size() == 0)
//            return false;
//
//        return isStructureFormed() && getRotorHolder().isFrontFaceFree();
//    }

    /**
     * @return true if structure formed, workable is active and front face is free
     */
    public boolean isActive() {
//        return isRotorFaceFree() && recipeMapWorkable.isActive() && recipeMapWorkable.isWorkingEnabled();
        return recipeMapWorkable.isActive() && recipeMapWorkable.isWorkingEnabled();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.exportFluidHandler = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (isStructureFormed()) {
            IRotorHolder rotorHolder = getRotorHolder();
//            FluidStack fuelStack = ((LargeTurbineWorkableHandler2) recipeMapWorkable).getFuelStack();
//            int fuelAmount = fuelStack == null ? 0 : fuelStack.amount;

//            ITextComponent fuelName = new TextComponentTranslation(fuelAmount == 0 ? "gregtech.fluid.empty" : fuelStack.getUnlocalizedName());
//            textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.fuel_amount", fuelAmount, fuelName));

            if (rotorHolder.getRotorEfficiency() > 0) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.rotor_speed", rotorHolder.getRotorSpeed(), rotorHolder.getMaxRotorHolderSpeed()));
                textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.rotor_efficiency", rotorHolder.getTotalPower()));
                textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.rotor_efficiency", rotorHolder.getTotalEfficiency()));
                int rotorDurability = rotorHolder.getRotorDurabilityPercent();
                if (rotorDurability > MIN_DURABILITY_TO_WARN) {
                    textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.rotor_durability", rotorDurability));
                } else {
                    textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.low_rotor_durability",
                            MIN_DURABILITY_TO_WARN, rotorDurability).setStyle(new Style().setColor(TextFormatting.RED)));
                }
            }
//            if(!isRotorFaceFree()) {
//                textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.obstructed")
//                        .setStyle(new Style().setColor(TextFormatting.RED)));
//            }
        }
        super.addDisplayText(textList);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCC", "CHHC", "CCCC")
                .aisle("CHHC", "RGGD", "CTTC")
                .aisle("CCCC", "CSHC", "CCCC")
                .where('S', selfPredicate())
                .where('G', states(getGearBoxState()))
                .where('C', states(getCasingState()))
                .where('R', abilities(MultiblockAbility.ABILITY_ROTOR_HOLDER_2).addTooltips("gregtech.multiblock.pattern.clear_amount_3"))
                .where('D', abilities(MultiblockAbility.OUTPUT_ENERGY))
                .where('H', states(getCasingState()).or(autoAbilities(false, true, false, false, true, true, false)))
                .where('T', states(getCasingState()).or(autoAbilities(false, true)))
                .build();
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.large_turbine.description")};
    }

    @Override
    public boolean canShare() {
        return false;
    }

    public IBlockState getCasingState() {
        return casingState;
    }

    public IBlockState getGearBoxState() {
        return gearboxState;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return casingRenderer;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return frontOverlay;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return hasMufflerHatch;
    }

    @Override
    public boolean isStructureObstructed() {
//        return !isRotorFaceFree();
        return false;
    }

    @Override
    public int getTier() {
        return tier;
    }
}
