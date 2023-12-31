package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.IHeatingCoil;
import gregtech.api.capability.impl.HeatingCoilRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiMapMultiblockController;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipeproperties.TemperatureProperty;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityForgingFurnace extends MultiMapMultiblockController implements IHeatingCoil {

    private int blastFurnaceTemperature;

    public MetaTileEntityForgingFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, determineRecipeMaps());
        this.recipeMapWorkable = new HeatingCoilRecipeLogic(this);

        this.recipeMapWorkable.setParallelLimit(4);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityForgingFurnace(this.metaTileEntityId);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        Object type = context.get("CoilType");
        if (type instanceof IHeatingCoilBlockStats) {
            this.blastFurnaceTemperature = ((IHeatingCoilBlockStats) type).getCoilTemperature();
        } else {
            this.blastFurnaceTemperature = BlockWireCoil.CoilType.CUPRONICKEL.getCoilTemperature();
        }

        this.blastFurnaceTemperature += 100 *
                Math.max(0, GTUtility.getTierByVoltage(getEnergyContainer().getInputVoltage()) - GTValues.MV);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.blastFurnaceTemperature = 0;
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe, boolean consumeIfSuccess) {
        return this.blastFurnaceTemperature >= recipe.getProperty(TemperatureProperty.getInstance(), 0);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        TraceabilityPredicate casing = states(getCasingState()).setMinGlobalLimited(64);
        return FactoryBlockPattern.start(FRONT, UP, RIGHT)
                .aisle("TTTT", "TTTT", "TTTT", "TTTT")
                .aisle("TEET", "EPPE", "EPPE", "TEET").setRepeatable(1, 4)
                .aisle("TTTT", "TPPT", "TPPT", "TTTT")
                .aisle("    ", " PP ", " PP ", "    ")
                .aisle("CCCC", "CPPC", "CPPC", "CCCC")
                .aisle("HHHH", "H##H", "H##H", "HHHH")
                .aisle("CCCC", "C##C", "C##C", "CMMC")
                .aisle("HHHH", "H##H", "H##H", "HHHH")
                .aisle("CCCC", "CPPC", "CPPC", "CCCC")
                .aisle("    ", " PP ", " PP ", "    ")
                .aisle("IIII", "IPPI", "IPPI", "IIII")
                .aisle("IIII", "X##I", "I##I", "IIII")
                .aisle("IIII", "IIII", "IIII", "IIII")
                .where('T', states(getAltCasingState()).setMinGlobalLimited(20)
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(2))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1)))
                .where('E', states(getIntakeState()))
                .where('P', states(getPipeState()))
                .where('C', casing.or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                        .setMaxGlobalLimited(3, 1)))
                .where('M', casing.or(abilities(MultiblockAbility.MUFFLER_HATCH).setMaxGlobalLimited(1, 1)))
                .where('H', heatingCoils())
                .where('I', casing.or(autoAbilities(false, true, false, true, false, true, false)))
                .where('X', selfPredicate())
                .where('#', air())
                .where(' ', any())
                .build();
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.COBALT_IRIDIUM_FORGING);
    }

    protected static IBlockState getAltCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
    }

    protected static IBlockState getPipeState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE);
    }

    public IBlockState getIntakeState() {
        return MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.EXTREME_ENGINE_INTAKE_CASING);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        boolean input = iMultiblockPart instanceof MetaTileEntityItemBus bus &&
                bus.getAbility().equals(MultiblockAbility.IMPORT_ITEMS);
        input = input || iMultiblockPart instanceof MetaTileEntityFluidHatch hatch &&
                hatch.getAbility().equals(MultiblockAbility.IMPORT_FLUIDS);
        return input ? Textures.ROBUST_TUNGSTENSTEEL_CASING :
                Textures.COBALT_IRIDIUM_FORGING_CASING;
    }

    @Override
    protected @NotNull OrientedOverlayRenderer getFrontOverlay() {
        return Textures.LINEAR_FORGING_FURNACE_OVERLAY;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(recipeMapWorkable.isWorkingEnabled(), recipeMapWorkable.isActive())
                .addEnergyUsageLine(getEnergyContainer())
                .addEnergyTierLine(GTUtility.getTierByVoltage(recipeMapWorkable.getMaxVoltage()))
                .addCustom(tl -> {
                    // Coil heat capacity line
                    if (isStructureFormed()) {
                        ITextComponent heatString = TextComponentUtil.stringWithColor(
                                TextFormatting.RED,
                                TextFormattingUtil.formatNumbers(blastFurnaceTemperature) + "K");

                        tl.add(TextComponentUtil.translationWithColor(
                                TextFormatting.GRAY,
                                "gregtech.multiblock.blast_furnace.max_temperature",
                                heatString));
                    }
                })
                // Recipe map line
                .addCustom(tl -> tl.add(new TextComponentTranslation(getCurrentRecipeMap().getTranslationKey())
                        .setStyle(new Style().setColor(TextFormatting.WHITE))))
                .addParallelsLine(recipeMapWorkable.getParallelLimit())
                .addWorkingStatusLine()
                .addProgressLine(recipeMapWorkable.getProgressPercent());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.linear_forging_furnace.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.electric_blast_furnace.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.electric_blast_furnace.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.electric_blast_furnace.tooltip.3"));
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builderBase = MultiblockShapeInfo.builder()
                .where('T', getAltCasingState())
                .where('E', getIntakeState())
                .where('P', getPipeState())
                .where('C', getCasingState())
                .where('M', MetaTileEntities.MUFFLER_HATCH[GTValues.LV], EnumFacing.UP)
                .where('H', MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.CUPRONICKEL))
                .where('X', MetaTileEntities.LINEAR_FORGING_FURNACE, EnumFacing.SOUTH)
                .where('#', Blocks.AIR.getDefaultState())
                .where('Y', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LV], EnumFacing.NORTH)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.LV], EnumFacing.SOUTH)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LV], EnumFacing.EAST)
                .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.LV], EnumFacing.NORTH)
                .where('D', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.LV], EnumFacing.EAST)
                .where('A', () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH : getCasingState(), EnumFacing.SOUTH);
        for (int j = 1; j <= 4; j++) {
            MultiblockShapeInfo.Builder builder = builderBase.shallowCopy();
            String i = StringUtils.repeat('I', j + 2);
            String e = StringUtils.repeat('E', j);
            String t = StringUtils.repeat('T', j + 2);
            String p = StringUtils.repeat('P', j);
            String f = StringUtils.repeat('F', j + 2);
            builder.aisle(t + "#CHCHC#CCC",
                            "T" + e + "T#CHYHC#CCC",
                            "T" + e + "T#CHYHC#CCC",
                            f + "#CHCHC#CCC")
                    .aisle("T" + e + "T#CHCHC#CCC",
                            "T" + p + "PPP###PPP#C",
                            "T" + p + "PPP###PPP#D",
                            "T" + e + "T#CHMHC#CCC")
                    .aisle("T" + e + "T#CHCHC#CCC",
                            "T" + p + "PPP###PPP#O",
                            "T" + p + "PPP###PPP#O",
                            "T" + e + "T#CHCHC#CCC")
                    .aisle(t + "#CHCHC#CCC",
                            "T" + e + "T#CHCHC#CXC",
                            "T" + e + "T#CHCHC#CAC",
                            i + "#CHCHC#CCC");
            shapeInfo.add(builder.build());
        }
        return shapeInfo;
    }

    private static @NotNull RecipeMap<?> @NotNull [] determineRecipeMaps() {
        return new RecipeMap<?>[] { RecipeMaps.BLAST_RECIPES, RecipeMaps.FORGING_RECIPES };
    }

    @Override
    public void setRecipeMapIndex(int index) {
        super.setRecipeMapIndex(index);
        this.recipeMapWorkable.setParallelLimit(4 - 3 * index);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("Parallel", this.recipeMapWorkable.getParallelLimit());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.recipeMapWorkable.setParallelLimit(data.getInteger("Parallel"));
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = super.getDataInfo();
        list.add(new TextComponentTranslation("gregtech.multiblock.blast_furnace.max_temperature",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(blastFurnaceTemperature) + "K")
                        .setStyle(new Style().setColor(TextFormatting.RED))));
        list.add(new TextComponentTranslation(getCurrentRecipeMap().getTranslationKey())
                .setStyle(new Style().setColor(TextFormatting.WHITE)));
        return list;
    }

    @Override
    public int getCurrentTemperature() {
        return this.blastFurnaceTemperature;
    }
}
