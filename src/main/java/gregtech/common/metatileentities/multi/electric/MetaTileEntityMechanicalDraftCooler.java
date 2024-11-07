package gregtech.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.IProgressBarMultiblock;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityMechanicalDraftCooler extends RecipeMapMultiblockController
                                                 implements IProgressBarMultiblock {

    protected final boolean dry;

    public MetaTileEntityMechanicalDraftCooler(ResourceLocation metaTileEntityId, boolean dry) {
        super(metaTileEntityId, RecipeMaps.INDUSTRIAL_COOLING_RECIPES);
        this.dry = dry;
        this.recipeMapWorkable = new MultiblockRecipeLogic(this) {

            @Override
            protected double getOverclockingVoltageFactor() {
                return 3; // overclocks triple consumption instead of quadrupling
            }
        };
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
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.recipeMapWorkable.setSpeedBonus(getDurationModifier(getWorld().getBiome(getPos()), getPos(), dry));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.recipeMapWorkable.setSpeedBonus(1);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMechanicalDraftCooler(metaTileEntityId, dry);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        if (dry) {
            return wherify(
                    FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.BACK, RelativeDirection.UP)
                            .aisle("###########",
                                    "#C#######C#",
                                    "##C     C##",
                                    "##       ##",
                                    "##       ##",
                                    "##       ##",
                                    "##       ##",
                                    "##       ##",
                                    "##C     C##",
                                    "#C#######C#",
                                    "###########")
                            .aisle("###########",
                                    "#CCCCCCCCC#",
                                    "#CC     CC#",
                                    "#C       C#",
                                    "#C       C#",
                                    "#C       C#",
                                    "#C       C#",
                                    "#C       C#",
                                    "#CC     CC#",
                                    "#CCCCXCCCC#",
                                    "###########")
                            .aisle("##       ##",
                                    "#C       C#",
                                    "  C     C  ",
                                    "           ",
                                    "           ",
                                    "           ",
                                    "           ",
                                    "           ",
                                    "  C     C  ",
                                    "#C       C#",
                                    "##       ##")
                            .setRepeatable(2)
                            .aisle("###########",
                                    "#CCSSSSSCC#",
                                    "#CC B B CC#",
                                    "#SB B B BS#",
                                    "#SB B B BS#",
                                    "#SB B B BS#",
                                    "#SB B B BS#",
                                    "#SB B B BS#",
                                    "#CC B B CC#",
                                    "#CCSSSSSCC#",
                                    "###########")
                            .aisle("#SSS###SSS#",
                                    "SCSSSSSSSCS",
                                    "SSCBBBBBCSS",
                                    "SS       SS",
                                    "#SBBBBBBBS#",
                                    "#S       S#",
                                    "#SBBBBBBBS#",
                                    "SS       SS",
                                    "SSCBBBBBCSS",
                                    "SCSSSSSSSCS",
                                    "#SSS###SSS#")
                            .aisle("###########",
                                    "#CCSSSSSCC#",
                                    "#CCB B BCC#",
                                    "#S B B B S#",
                                    "#S B B B S#",
                                    "#S B B B S#",
                                    "#S B B B S#",
                                    "#S B B B S#",
                                    "#CCB B BCC#",
                                    "#CCSSSSSCC#",
                                    "###########")
                            .aisle("###########",
                                    "###SSSSS###",
                                    "##C     C##",
                                    "#SBBBBBBBS#",
                                    "#S       S#",
                                    "#SBBBBBBBS#",
                                    "#S       S#",
                                    "#SBBBBBBBS#",
                                    "##C     C##",
                                    "###SSSSS###",
                                    "###########")
                            .aisle("###########",
                                    "##SCCCCCS##",
                                    "#SC     CS#",
                                    "#C     C C#",
                                    "#C    C  C#",
                                    "#C   C   C#",
                                    "#C  C    C#",
                                    "#C C     C#",
                                    "#SC     CS#",
                                    "##SCCCCCS##",
                                    "###########")
                            .aisle("##SSSSSSS##",
                                    "##S     S##",
                                    "SS   F   SS",
                                    "S    F    S",
                                    "S    F    S",
                                    "S FFFRFFF S",
                                    "S    F    S",
                                    "S    F    S",
                                    "SS   F   SS",
                                    "##S     S##",
                                    "##SSSSSSS##")
                            .aisle("###########",
                                    "##SCCCCCS##",
                                    "#SC     CS#",
                                    "#C C     C#",
                                    "#C  C    C#",
                                    "#C   C   C#",
                                    "#C    C  C#",
                                    "#C     C C#",
                                    "#SC     CS#",
                                    "##SCCCCCS##",
                                    "###########")
                            .aisle("###########",
                                    "###########",
                                    "###     ###",
                                    "##       ##",
                                    "##       ##",
                                    "##       ##",
                                    "##       ##",
                                    "##       ##",
                                    "###     ###",
                                    "###########",
                                    "###########")
                            .setRepeatable(2)).build();
        } else {
            return wherify(
                    FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.BACK, RelativeDirection.UP)
                            .aisle("###########",
                                    "#C#######C#",
                                    "##CSSSSSC##",
                                    "##SSSSSSS##",
                                    "##SSSSSSS##",
                                    "##SSSSSSS##",
                                    "##SSSSSSS##",
                                    "##SSSSSSS##",
                                    "##CSSSSSC##",
                                    "#C#######C#",
                                    "###########")
                            .aisle("###########",
                                    "#CCCCCCCCC#",
                                    "#CCWWWWWCC#",
                                    "#CWWWWWWWC#",
                                    "#CWWWWWWWC#",
                                    "#CWWWWWWWC#",
                                    "#CWWWWWWWC#",
                                    "#CWWWWWWWC#",
                                    "#CCWWWWWCC#",
                                    "#CCCCXCCCC#",
                                    "###########")
                            .aisle("##       ##",
                                    "#C       C#",
                                    "  C     C  ",
                                    "           ",
                                    "           ",
                                    "           ",
                                    "           ",
                                    "           ",
                                    "  C     C  ",
                                    "#C       C#",
                                    "##       ##")
                            .setRepeatable(2)
                            .aisle("###########",
                                    "#CCSSSSSCC#",
                                    "#CC     CC#",
                                    "#S       S#",
                                    "#S       S#",
                                    "#S       S#",
                                    "#S       S#",
                                    "#S       S#",
                                    "#CC     CC#",
                                    "#CCSSSSSCC#",
                                    "###########")
                            .aisle("#SSS###SSS#",
                                    "SCSSSSSSSCS",
                                    "SSC     CSS",
                                    "SS       SS",
                                    "#S       S#",
                                    "#S       S#",
                                    "#S       S#",
                                    "SS       SS",
                                    "SSC     CSS",
                                    "SCSSSSSSSCS",
                                    "#SSS###SSS#")
                            .aisle("###########",
                                    "#CCSSSSSCC#",
                                    "#CCBBBBBCC#",
                                    "#SBBBBBBBS#",
                                    "#SBBBBBBBS#",
                                    "#SBBBBBBBS#",
                                    "#SBBBBBBBS#",
                                    "#SBBBBBBBS#",
                                    "#CCBBBBBCC#",
                                    "#CCSSSSSCC#",
                                    "###########")
                            .aisle("###########",
                                    "###SSSSS###",
                                    "##CBBBBBC##",
                                    "#SBBBBBBBS#",
                                    "#SBBBBBBBS#",
                                    "#SBBBBBBBS#",
                                    "#SBBBBBBBS#",
                                    "#SBBBBBBBS#",
                                    "##CBBBBBC##",
                                    "###SSSSS###",
                                    "###########")
                            .aisle("###########",
                                    "##SCCCCCS##",
                                    "#SC     CS#",
                                    "#C     C C#",
                                    "#C    C  C#",
                                    "#C   C   C#",
                                    "#C  C    C#",
                                    "#C C     C#",
                                    "#SC     CS#",
                                    "##SCCCCCS##",
                                    "###########")
                            .aisle("##SSSSSSS##",
                                    "##S     S##",
                                    "SS   F   SS",
                                    "S    F    S",
                                    "S    F    S",
                                    "S FFFRFFF S",
                                    "S    F    S",
                                    "S    F    S",
                                    "SS   F   SS",
                                    "##S     S##",
                                    "##SSSSSSS##")
                            .aisle("###########",
                                    "##SCCCCCS##",
                                    "#SC     CS#",
                                    "#C C     C#",
                                    "#C  C    C#",
                                    "#C   C   C#",
                                    "#C    C  C#",
                                    "#C     C C#",
                                    "#SC     CS#",
                                    "##SCCCCCS##",
                                    "###########")
                            .aisle("###########",
                                    "###########",
                                    "###     ###",
                                    "##       ##",
                                    "##       ##",
                                    "##       ##",
                                    "##       ##",
                                    "##       ##",
                                    "###     ###",
                                    "###########",
                                    "###########")
                            .setRepeatable(2)).build();
        }
    }

    @Contract("_ -> param1")
    @NotNull
    protected FactoryBlockPattern wherify(@NotNull FactoryBlockPattern pattern) {
        return pattern
                .where('X', selfPredicate())
                .where('C', states(getCasingState()).setMinGlobalLimited(150)
                        .or(autoAbilities(true, true, false, false, true, true, false)))
                .where('S', states(getSheetingStates()))
                .where('F', states(getFanCasingState()))
                .where('R', states(getFanRotorState()))
                .where('B', states(getBaffleState()))
                .where('W', blocks(Blocks.WATER))
                .where(' ', air())
                .where('#', any());
    }

    @NotNull
    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(
                dry ? BlockMetalCasing.MetalCasingType.BRASS_BRICKS : BlockMetalCasing.MetalCasingType.BRONZE_BRICKS);
    }

    @NotNull
    protected IBlockState @NotNull [] getSheetingStates() {
        List<IBlockState> states = new ObjectArrayList<>();
        for (EnumDyeColor color : EnumDyeColor.values()) {
            states.add(MetaBlocks.METAL_SHEET.getState(color));
            states.add(MetaBlocks.LARGE_METAL_SHEET.getState(color));
        }
        return states.toArray(new IBlockState[0]);
    }

    @NotNull
    protected IBlockState getBaffleState() {
        return dry ?
                MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE) :
                MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.PLASTIC_BAFFLES);
    }

    @NotNull
    protected IBlockState getFanCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @NotNull
    protected IBlockState getFanRotorState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX);
    }

    @Override
    public void update() {
        super.update();

        if (this.isActive() && getWorld().isRemote) {
            playParticleEffects();
        }
    }

    protected void playParticleEffects() {
        if (dry) return; // wind particles for dry?
        if (this.getWorld() == null || this.getPos() == null) return;
        EnumFacing back = RelativeDirection.BACK.getRelativeFacing(this.getFrontFacing(), this.getUpwardsFacing(),
                this.isFlipped());
        BlockPos center = this.getPos().offset(back, 4);
        double xC = center.getX() + 0.5;
        double zC = center.getZ() + 0.5;
        double yLow = center.getY() + 4.5;
        double yHigh = center.getY() + 7;

        for (int i = 0; i < 60; i++) {
            float x = GTValues.RNG.nextFloat() * 6.8f - 3.4f;
            float z = GTValues.RNG.nextFloat() * 6.8f - 3.4f;
            // kill the spawn attempt if it exceeds the baffle range
            if (Math.abs(x) > 2.4 && Math.abs(z) > 2.4) {
                continue;
            }
            // low particles - continuous water dripping
            this.getWorld().spawnParticle(EnumParticleTypes.DRIP_WATER, xC + x, yLow + 0.5,
                    zC + z, 0, 0, 0);

            // high particles - condensation in the updraft
            // TODO custom particle that fits the application better
            double dx = GTValues.RNG.nextGaussian() * 0.05;
            double dy = GTValues.RNG.nextGaussian() * 0.1 + 0.3;
            double dz = GTValues.RNG.nextGaussian() * 0.05;
            float yOffset = GTValues.RNG.nextFloat() * 2.5f;
            this.getWorld().spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, xC + x, yHigh + yOffset,
                    zC + z, dx, dy, dz);
        }
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return dry ? Textures.BRASS_PLATED_BRICKS : Textures.BRONZE_PLATED_BRICKS;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return dry;
    }

    protected static double getDurationModifier(@NotNull Biome biome, @Nullable BlockPos pos, boolean dry) {
        float temp = pos == null ? biome.getDefaultTemperature() : biome.getTemperature(pos);
        float humidity = biome.getRainfall();
        // temp is unbounded, humidity is bounded between 1f and 0f
        // hotter and dryer is better for wet mechanical draft, colder is better for dry mechanical draft
        return dry ? Math.exp(temp / 2) * 0.6 : Math.exp(-temp / 2) * MathHelper.clamp(humidity, 0.2, 0.8);
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return Textures.MECHANICAL_DRAFT_COOLER_OVERLAY;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        textList.add(new TextComponentTranslation("gregtech.machine.draft_cooler.actual_performance",
                TextFormattingUtil.formatNumbers(getDurationModifier(getWorld().getBiome(getPos()), getPos(), dry))));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        if (!dry) tooltip.add(I18n.format("gregtech.machine.mechanical_draft_cooler.tooltip.wet"));
        tooltip.add(I18n.format("gregtech.machine.mechanical_draft_cooler.tooltip.1"));
        if (!dry) {
            tooltip.add(I18n.format("gregtech.machine.mechanical_draft_cooler.tooltip.2"));
        } else {
            tooltip.add(I18n.format("gregtech.machine.mechanical_draft_cooler.tooltip.3"));
        }
        if (world != null && world.isRemote) {
            BlockPos pos = GTUtility.getClientPlayer().getPosition();
            Biome biome = world.getBiome(pos);
            tooltip.add(I18n.format("gregtech.machine.draft_cooler.predicted_performance",
                    TextFormattingUtil.formatNumbers(getDurationModifier(biome, pos, dry))));
        }
    }

    @Override
    public boolean showProgressBar() {
        return !dry;
    }

    @Override
    public double getFillPercentage(int index) {
        long capacity = 0;
        long filled = 0;
        for (IFluidTank tank : getAbilities(MultiblockAbility.IMPORT_FLUIDS)) {
            FluidStack drained = tank.drain(Integer.MAX_VALUE, false);
            if (drained != null && (drained.getFluid() == FluidRegistry.WATER ||
                    drained.getFluid() == Materials.DistilledWater.getFluid())) {
                capacity += tank.getCapacity();
                filled += drained.amount;
            }
        }
        return (double) filled / capacity;
    }

    @Override
    public TextureArea getProgressBarTexture(int index) {
        return GuiTextures.PROGRESS_BAR_FLUID_RIG_DEPLETION;
    }

    @Override
    public void addBarHoverText(List<ITextComponent> hoverList, int index) {
        if (dry) return;
        if (!isStructureFormed()) {
            hoverList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                    "gregtech.multiblock.invalid_structure"));
        } else {
            long capacity = 0;
            long filled = 0;
            for (IFluidTank tank : getAbilities(MultiblockAbility.IMPORT_FLUIDS)) {
                FluidStack drained = tank.drain(Integer.MAX_VALUE, false);
                if (drained != null && (drained.getFluid() == FluidRegistry.WATER ||
                        drained.getFluid() == Materials.DistilledWater.getFluid())) {
                    capacity += tank.getCapacity();
                    filled += drained.amount;
                }
            }
            if (filled == 0) {
                hoverList.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                        "gregtech.multiblock.large_boiler.no_water"));
            } else {
                ITextComponent waterInfo = TextComponentUtil.translationWithColor(
                        TextFormatting.BLUE,
                        "%s / %s L",
                        filled, capacity);
                hoverList.add(TextComponentUtil.translationWithColor(
                        TextFormatting.GRAY,
                        "gregtech.multiblock.large_boiler.water_bar_hover",
                        waterInfo));
            }
        }
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
            return 0;
        }

        @Override
        public boolean inputsEnergy(EnumFacing side) {
            return false;
        }

        @Override
        public long changeEnergy(long differenceAmount) {
            if (differenceAmount > 0) {
                return 0;
            } else if (differenceAmount < 0) {
                differenceAmount = super.changeEnergy(differenceAmount);
                // drain water first, then resort to distilled water
                FluidStack drain = waterTank.drain(Materials.Water.getFluid(-convertToWater(differenceAmount)), true);
                long amount = drain == null ? 0 : -drain.amount;
                if (differenceAmount < amount) {
                    drain = waterTank.drain(
                            Materials.DistilledWater.getFluid(-convertToWater(differenceAmount - amount)),
                            true);
                    amount -= drain == null ? 0 : drain.amount;
                }
                return amount;
            }
            return 0;
        }

        private static int convertToWater(long eu) {
            return GTUtility.safeCastLongToInt((long) Math.floor(eu / 256D));
        }

        @Override
        public long getEnergyStored() {
            FluidStack drain = waterTank.drain(Materials.Water.getFluid(Integer.MAX_VALUE), false);
            long amount = drain == null ? 0 : drain.amount;
            drain = waterTank.drain(Materials.DistilledWater.getFluid(Integer.MAX_VALUE), false);
            amount += drain == null ? 0 : drain.amount;
            return Math.min(amount * 256, super.getEnergyStored());
        }

        @Override
        public long getEnergyCapacity() {
            return Math.min(
                    getEnergyStored() + waterTank.fill(Materials.Water.getFluid(Integer.MAX_VALUE), false) * 256L,
                    super.getEnergyCapacity());
        }
    }
}
