package gregtech.common.metatileentities.multi;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.IProgressBarMultiblock;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.logic.OCParams;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityNaturalDraftCooler extends RecipeMapMultiblockController implements IProgressBarMultiblock {

    protected final int tier;

    public MetaTileEntityNaturalDraftCooler(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, RecipeMaps.INDUSTRIAL_COOLING_RECIPES);
        this.tier = tier;
        this.recipeMapWorkable = new MultiblockRecipeLogic(this) {

            @Override
            protected void modifyOverclockPre(@NotNull OCParams ocParams, @NotNull RecipePropertyStorage storage) {
                ocParams.setDuration((int) Math
                        .max(ocParams.duration() * getDurationModifier(getWorld().getBiome(getPos()), getPos()), 0));
            }

            @Override
            public boolean isAllowOverclocking() {
                return false;
            }
        };
    }

    @Override
    protected void initializeAbilities() {
        this.inputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.inputFluidInventory = new FluidTankList(allowSameFluidFillForOutputs(),
                getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.outputFluidInventory = new FluidTankList(allowSameFluidFillForOutputs(),
                getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        this.energyContainer = new WaterEnergyContainer(inputFluidInventory);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int pow = 0;
        for (IMultiblockPart part : getMultiblockParts()) {
            if (part instanceof ITieredMetaTileEntity tiered && part instanceof IMultiblockAbilityPart<?>ability &&
                    ability.getAbility() == MultiblockAbility.IMPORT_FLUIDS) {
                pow = Math.max(pow, tiered.getTier() - (GTValues.HV + tier));
            }
            if (pow >= 4) {
                pow = 4;
                break;
            }
        }
        this.recipeMapWorkable.setParallelLimit((int) Math.pow((1 + tier), pow));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.recipeMapWorkable.setParallelLimit(1);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityNaturalDraftCooler(metaTileEntityId, tier);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.BACK, RelativeDirection.UP)
                .aisle("#####ZZZZZ#####",
                        "###ZZZZZZZZZ###",
                        "##ZZZZZZZZZZZ##",
                        "#ZZZZZZZZZZZZZ#",
                        "#ZZZZZZZZZZZZZ#",
                        "ZZZZZZZZZZZZZZZ",
                        "ZZZZZZZZZZZZZZZ",
                        "ZZZZZZZZZZZZZZZ",
                        "ZZZZZZZZZZZZZZZ",
                        "ZZZZZZZZZZZZZZZ",
                        "#ZZZZZZZZZZZZZ#",
                        "#ZZZZZZZZZZZZZ#",
                        "##ZZZZZZZZZZZ##",
                        "###ZZZZZZZZZ###",
                        "#####ZZZZZ#####")
                .aisle("#####ZZZZZ#####",
                        "###ZZZZZZZZZ###",
                        "##ZZZZWWWZZZZ##",
                        "#ZZZWWWWWWWZZZ#",
                        "#ZZWWWWWWWWWZZ#",
                        "ZZZWWWWWWWWWZZZ",
                        "ZZWWWWWWWWWWWZZ",
                        "ZZWWWWWWWWWWWZZ",
                        "ZZWWWWWWWWWWWZZ",
                        "ZZZWWWWWWWWWZZZ",
                        "#ZZWWWWWWWWWZZ#",
                        "#ZZZWWWWWWWZZZ#",
                        "##ZZZZWWWZZZZ##",
                        "###ZZZZZZZZZ###",
                        "#####ZZXZZ#####")
                .aisle("###         ###",
                        "##   F F F   ##",
                        "#  F       F  #",
                        "  F         F  ",
                        "               ",
                        " F           F ",
                        "               ",
                        " F           F ",
                        "               ",
                        " F           F ",
                        "               ",
                        "  F         F  ",
                        "#  F       F  #",
                        "##   F F F   ##",
                        "###         ###")
                .aisle("###         ###",
                        "##           ##",
                        "#   F F F F   #",
                        "               ",
                        "  F         F  ",
                        "               ",
                        "  F         F  ",
                        "               ",
                        "  F         F  ",
                        "               ",
                        "  F         F  ",
                        "               ",
                        "#   F F F F   #",
                        "##           ##",
                        "###         ###")
                .aisle("###############",
                        "####CCCCCCC####",
                        "###CFFFFFFFC###",
                        "##CF       FC##",
                        "#CF         FC#",
                        "#CF         FC#",
                        "#CF         FC#",
                        "#CF         FC#",
                        "#CF         FC#",
                        "#CF         FC#",
                        "#CF         FC#",
                        "##CF       FC##",
                        "###CFFFFFFFC###",
                        "####CCCCCCC####",
                        "###############")
                .aisle("###############",
                        "#####CCCCC#####",
                        "###CC F F CC###",
                        "##C#F     F#C##",
                        "##CF       FC##",
                        "#C           C#",
                        "#CF         FC#",
                        "#C           C#",
                        "#CF         FC#",
                        "#C           C#",
                        "##CF       FC##",
                        "##C#F     F#C##",
                        "###CC F F CC###",
                        "#####CCCCC#####",
                        "###############")
                .aisle("###############",
                        "#####CCCCC#####",
                        "####C F F C####",
                        "###CFBBBBBFC###",
                        "##CFBBBBBBBFC##",
                        "#C BBBBBBBBB C#",
                        "#CFBBBBBBBBBFC#",
                        "#C BBBBBBBBB C#",
                        "#CFBBBBBBBBBFC#",
                        "#C BBBBBBBBB C#",
                        "##CFBBBBBBBFC##",
                        "###CFBBBBBFC###",
                        "####C F F C####",
                        "#####CCCCC#####",
                        "###############")
                .aisle("###############",
                        "######CCC######",
                        "####CCF FCC####",
                        "###CFBBBBBFC###",
                        "##CFBBBBBBBFC##",
                        "##CBBBBBBBBBC##",
                        "#CFBBBBBBBBBFC#",
                        "#C BBBBBBBBB C#",
                        "#CFBBBBBBBBBFC#",
                        "##CBBBBBBBBBC##",
                        "##CFBBBBBBBFC##",
                        "###CFBBBBBFC###",
                        "####CCF FCC####",
                        "######CCC######",
                        "###############")
                .aisle("###############",
                        "######CCC######",
                        "#####CF FC#####",
                        "###CC     CC###",
                        "###C       C###",
                        "##C         C##",
                        "#CF         FC#",
                        "#C           C#",
                        "#CF         FC#",
                        "##C         C##",
                        "###C       C###",
                        "###CC     CC###",
                        "#####CF FC#####",
                        "######CCC######",
                        "###############")
                .aisle("###############",
                        "###############",
                        "#####CCCCC#####",
                        "####C     C####",
                        "###C       C###",
                        "##C         C##",
                        "##C         C##",
                        "##C         C##",
                        "##C         C##",
                        "##C         C##",
                        "###C       C###",
                        "####C     C####",
                        "#####CCCCC#####",
                        "###############",
                        "###############")
                .setRepeatable(4)
                .aisle("###############",
                        "######CCC######",
                        "#####C   C#####",
                        "###CC     CC###",
                        "###C       C###",
                        "##C         C##",
                        "#C           C#",
                        "#C           C#",
                        "#C           C#",
                        "##C         C##",
                        "###C       C###",
                        "###CC     CC###",
                        "#####C   C#####",
                        "######CCC######",
                        "###############")
                .aisle("###############",
                        "######CCC######",
                        "####CC   CC####",
                        "###C       C###",
                        "##C         C##",
                        "##C         C##",
                        "#C           C#",
                        "#C           C#",
                        "#C           C#",
                        "##C         C##",
                        "##C         C##",
                        "###C       C###",
                        "####CC   CC####",
                        "######CCC######",
                        "###############")
                .aisle("###############",
                        "######CCC######",
                        "####CC   CC####",
                        "###C       C###",
                        "##C         C##",
                        "##C         C##",
                        "#C           C#",
                        "#C           C#",
                        "#C           C#",
                        "##C         C##",
                        "##C         C##",
                        "###C       C###",
                        "####CC   CC####",
                        "######CCC######",
                        "###############")
                .aisle("###############",
                        "###############",
                        "######   ######",
                        "####       ####",
                        "###         ###",
                        "###         ###",
                        "##           ##",
                        "##           ##",
                        "##           ##",
                        "###         ###",
                        "###         ###",
                        "####       ####",
                        "######   ######",
                        "###############",
                        "###############")
                .aisle("###############",
                        "###############",
                        "######   ######",
                        "####       ####",
                        "###         ###",
                        "###         ###",
                        "##           ##",
                        "##           ##",
                        "##           ##",
                        "###         ###",
                        "###         ###",
                        "####       ####",
                        "######   ######",
                        "###############",
                        "###############")
                .where('X', selfPredicate())
                .where('C', states(getConcreteStates()))
                .where('Z', states(getConcreteStates()).setMinGlobalLimited(200)
                        .or(autoAbilities(false, false, false, false, true, true, false)))
                .where('F', frames(getFrameMaterials()))
                .where('B', states(getBaffleState()))
                .where('W', blocks(Blocks.WATER))
                .where(' ', air())
                .where('#', any())
                .build();
        // TODO medium and large natural draft cooler
    }

    @NotNull
    protected static IBlockState @NotNull [] getConcreteStates() {
        List<IBlockState> states = new ObjectArrayList<>();
        for (StoneVariantBlock block : MetaBlocks.STONE_BLOCKS.values()) {
            states.add(block.getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT));
            states.add(block.getState(StoneVariantBlock.StoneType.CONCRETE_DARK));
        }
        return states.toArray(new IBlockState[0]);
    }

    @NotNull
    protected static IBlockState getBaffleState() {
        return MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.PLASTIC_BAFFLES);
    }

    @NotNull
    protected static Material[] getFrameMaterials() {
        return new Material[] { Materials.Steel };
    }

    @Override
    public void update() {
        super.update();

        if (this.isActive() && getWorld().isRemote) {
            VanillaParticleEffects.NATURAL_DRAFT_EFFECTS.runEffect(this);
        }
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return Textures.NATURAL_DRAFT_COOLER_OVERLAY;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(recipeMapWorkable.isWorkingEnabled(), recipeMapWorkable.isActive())
                .addParallelsLine(recipeMapWorkable.getParallelLimit())
                .addWorkingStatusLine()
                .addProgressLine(recipeMapWorkable.getProgressPercent());
        textList.add(new TextComponentTranslation("gregtech.machine.draft_cooler.actual_performance",
                TextFormattingUtil.formatNumbers(getDurationModifier(getWorld().getBiome(getPos()), getPos()))));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.natural_draft_cooler.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.natural_draft_cooler.tooltip.2", 1 + tier,
                GTValues.VOCNF[GTValues.HV + tier]));
        tooltip.add(I18n.format("gregtech.machine.natural_draft_cooler.tooltip.3"));
        if (world != null && world.isRemote) {
            BlockPos pos = GTUtility.getClientPlayer().getPosition();
            Biome biome = world.getBiome(pos);
            tooltip.add(I18n.format("gregtech.machine.draft_cooler.predicted_performance",
                    TextFormattingUtil.formatNumbers(getDurationModifier(biome, pos))));
        }
    }

    protected static double getDurationModifier(@NotNull Biome biome, @Nullable BlockPos pos) {
        float temp = pos == null ? biome.getDefaultTemperature() : biome.getTemperature(pos);
        float humidity = biome.getRainfall();
        // temp is unbounded, humidity is bounded between 1f and 0f
        // hotter and wetter is better for natural draft, so higher values = smaller modifier
        return Math.exp(-temp / 2) * (1 - MathHelper.clamp(humidity, 0.2, 0.8));
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

    private static class WaterEnergyContainer implements IEnergyContainer {

        protected final @NotNull IFluidHandler waterTank;

        public WaterEnergyContainer(@NotNull IFluidHandler waterTank) {
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
            return amount * 256;
        }

        @Override
        public long getEnergyCapacity() {
            return getEnergyStored() + waterTank.fill(Materials.Water.getFluid(Integer.MAX_VALUE), false) * 256L;
        }

        @Override
        public long getInputAmperage() {
            return Integer.MAX_VALUE;
        }

        @Override
        public long getInputVoltage() {
            return Integer.MAX_VALUE;
        }
    }
}
