package gregtech.common.metatileentities.multi.electric.generator;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityRotorHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.List;

import static gregtech.api.metatileentity.multiblock.MultiblockAbility.ABILITY_ROTOR_HOLDER;

public class MetaTileEntityLargeTurbine extends RotorHolderMultiblockController {

    private static final int MIN_DURABILITY_TO_WARN = 10;

    public enum TurbineType {

        STEAM(RecipeMaps.STEAM_TURBINE_FUELS, MetaBlocks.TURBINE_CASING.getState(TurbineCasingType.STEEL_TURBINE_CASING), MetaBlocks.TURBINE_CASING.getState(TurbineCasingType.STEEL_GEARBOX), Textures.SOLID_STEEL_CASING, true, false, Textures.LARGE_STEAM_TURBINE_OVERLAY),
        GAS(RecipeMaps.GAS_TURBINE_FUELS, MetaBlocks.TURBINE_CASING.getState(TurbineCasingType.STAINLESS_TURBINE_CASING), MetaBlocks.TURBINE_CASING.getState(TurbineCasingType.STEEL_GEARBOX), Textures.CLEAN_STAINLESS_STEEL_CASING, false, true, Textures.LARGE_GAS_TURBINE_OVERLAY),
        PLASMA(RecipeMaps.PLASMA_GENERATOR_FUELS, MetaBlocks.TURBINE_CASING.getState(TurbineCasingType.TUNGSTENSTEEL_TURBINE_CASING), MetaBlocks.TURBINE_CASING.getState(TurbineCasingType.STEEL_GEARBOX), Textures.ROBUST_TUNGSTENSTEEL_CASING, true, false, Textures.LARGE_PLASMA_TURBINE_OVERLAY);

        public final FuelRecipeMap recipeMap;
        public final IBlockState casingState;
        public final IBlockState gearboxState;
        public final ICubeRenderer casingRenderer;
        public final boolean hasOutputHatch;
        public final boolean hasMufflerHatch;
        public final ICubeRenderer frontOverlay;

        TurbineType(FuelRecipeMap recipeMap, IBlockState casingState, IBlockState gearboxState, ICubeRenderer casingRenderer, boolean hasOutputHatch, boolean hasMufflerHatch, ICubeRenderer frontOverlay) {
            this.recipeMap = recipeMap;
            this.casingState = casingState;
            this.gearboxState = gearboxState;
            this.casingRenderer = casingRenderer;
            this.hasOutputHatch = hasOutputHatch;
            this.hasMufflerHatch = hasMufflerHatch;
            this.frontOverlay = frontOverlay;
        }
    }

    public final TurbineType turbineType;
    public IFluidHandler exportFluidHandler;

    public MetaTileEntityLargeTurbine(ResourceLocation metaTileEntityId, TurbineType turbineType) {
        super(metaTileEntityId, turbineType.recipeMap, GTValues.V[4]);
        this.turbineType = turbineType;
    }

    @Override
    protected FuelRecipeLogic createWorkable(long maxVoltage) {
        return new LargeTurbineWorkableHandler(this, recipeMap, () -> energyContainer, () -> importFluidHandler);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeTurbine(metaTileEntityId, turbineType);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.exportFluidHandler = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.exportFluidHandler = null;
    }

    @Override
    public int getRotorSpeedIncrement() {
        return 1;
    }

    @Override
    public int getRotorSpeedDecrement() {
        return -3;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (isStructureFormed()) {
            MetaTileEntityRotorHolder rotorHolder = getRotorHolder();
            FluidStack fuelStack = ((LargeTurbineWorkableHandler) workableHandler).getFuelStack();
            int fuelAmount = fuelStack == null ? 0 : fuelStack.amount;

            ITextComponent fuelName = new TextComponentTranslation(fuelAmount == 0 ? "gregtech.fluid.empty" : fuelStack.getUnlocalizedName());
            textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.fuel_amount", fuelAmount, fuelName));

            if (rotorHolder.getRotorEfficiency() > 0.0) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.rotor_speed", rotorHolder.getCurrentRotorSpeed(), rotorHolder.getMaxRotorSpeed()));
                textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.rotor_efficiency", (int) (rotorHolder.getRotorEfficiency() * 100)));
                int rotorDurability = (int) (rotorHolder.getRotorDurability() * 100);
                if (rotorDurability > MIN_DURABILITY_TO_WARN) {
                    textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.rotor_durability", rotorDurability));
                } else {
                    textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.low_rotor_durability",
                            MIN_DURABILITY_TO_WARN, rotorDurability).setStyle(new Style().setColor(TextFormatting.RED)));
                }
            }
            if(!isRotorFaceFree()) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.turbine.obstructed")
                        .setStyle(new Style().setColor(TextFormatting.RED)));
            }
        }
        super.addDisplayText(textList);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        if (turbineType == null)
            return null;

        TraceabilityPredicate predicate = states(getCasingState()).or(autoAbilities(true, true, true, true, false));
        return FactoryBlockPattern.start()
                .aisle("CCCC", "CHHC", "CCCC")
                .aisle("CHHC", "RGGD", "CTTC")
                .aisle("CCCC", "CSHC", "CCCC")
                .where('S', selfPredicate())
                .where('G', states(getGearBoxState()))
                .where('C', states(getCasingState()))
                .where('R', abilities(ABILITY_ROTOR_HOLDER).addTooltips("gregtech.multiblock.pattern.clear_amount_3"))
                .where('D', abilities(MultiblockAbility.OUTPUT_ENERGY))
                .where('H', predicate)
                .where('T', predicate.or(autoAbilities(false, false, false, false, true)))
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
        return turbineType.casingState;
    }

    public IBlockState getGearBoxState() {
        return turbineType.gearboxState;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return turbineType.casingRenderer;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return turbineType.frontOverlay;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return turbineType.hasMufflerHatch;
    }

    @Override
    public boolean isStructureObstructed() {
        return !isRotorFaceFree();
    }
}
