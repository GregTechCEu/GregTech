package gregtech.common.metatileentities.multi.fission;

import gregtech.api.capability.IFissionRodPort;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.fluids.FluidConstants;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IFissionReactor;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.properties.impl.FissionProperty;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockFissionCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class MetaTileEntityFissionReactor extends RecipeMapMultiblockController implements IFissionReactor {

    public static final int STANDARD_TEMPERATURE_LIMIT = 15000;
    public static final float TEMPERATURE_LIMIT_MULTIPLIER_PER_INSTABILITY = 0.65f;
    public static final float RATE_MULTIPLIER_PER_FRAGILITY = 0.9f;

    // fuel rods are in the forward-back axis, provide one parallel each (at least one is required)
    /// fuel will determine base heat production, optimal temperature, and penalty coefficient
    /// rod will determine reduction to moderator/control rod bonus effect and instability
    /// empty spaces and control rods alleviate instability;
    /// excess instability reduces maximum reactor temperature before meltdown
    /// zirconium rod +0.9 bonus reduction, +1 instability per length
    /// bismuth rod +0.6 bonus reduction, +3 instability per length

    // coolant rods are in the up-down axis, allow for heat extraction via coolant based on temperature
    /// coolant will determine minimum reactor temperature and thermal capacity per liter of coolant
    /// rod will determine maximum coolant throughput rate and fragility
    /// empty spaces and moderator rods alleviate fragility;
    /// excess fragility reduces heat capture efficiency for coolants by reducing their throughput rate
    /// aluminium rod 5 parallel per 1000K reactor temp above coolant min, +1 fragility per length
    /// cerium rod 12 parallel per 1000K reactor temp above coolant min, +4 fragility per length

    // moderator rods are in the left-right axis, bonus reduces reaction speed penalty from temperature
    /// beryllium moderator reduces fragility by 3 per length and increases speed by 0.04, yields boron
    /// graphite moderator reduces fragility by 1 per length and increases speed by 0.07, yields nitrogen
    /// water moderator only increases speed by 0.05, yields heavy water
    /// if the multiplier exceeds 1, rest in peace.

    // control rods are in the left-right axis, bonus increases optimal temperature
    /// cadmium rods reduce instability by 3 per length and increase temperature by 900, yields indium
    /// hafnium rods reduce instability by 2 per length and increase temperature by 1700, yields tantalum
    /// molybdenum rods reduce instability by 1 per length and increase temperature by 2900, yields technetium

    // thermal voids are singleblocks anywhere, passively void heat based on temperature
    /// voids provide their cooling potential once for every side adjacent to any rod

    protected @Nullable String structureIssue;

    protected int heatCapacityFromVolume = 1;
    protected int heatLossFromSurfaceArea = 0;
    protected long currentHeat;

    protected int fuelRodPenalty10x = 10;
    protected int instability;
    protected int fragility;
    protected double moderatorRodBonus;
    protected int controlRodBonus;
    protected long thermalVoidCooling;
    protected int air;

    protected Map<RodPos, FissionRod> rods;

    public MetaTileEntityFissionReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.FISSION_RECIPES);
        this.recipeMapWorkable = new FissionRecipeLogic(this);
        recipeMapWorkable.setAllowOverclocking(false);
    }

    @Override
    public FissionRecipeLogic getRecipeMapWorkable() {
        return (FissionRecipeLogic) super.getRecipeMapWorkable();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFissionReactor(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        super.updateFormedValid();
        if (currentHeat > 0) {
            currentHeat -= heatLossFromSurfaceArea +
                    thermalVoidCooling * (getTemperature() - getBaseTemperature()) / 1000;
            if (currentHeat < 0) {
                currentHeat = 0;
            }
        }
        if (rods != null) {
            for (FissionRod r : rods.values()) {
                r.type().onReactorTick(this, r.port(), r.opposingPort(), r.length());
            }
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        currentHeat = 0;
        rods.clear();
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        // TODO make this fully expandable later (cleanroom logic time)
        TraceabilityPredicate casing = states(getCasingState());
        return FactoryBlockPattern.start()
                .aisle("CCCCCCC",
                        "CFFFFFC",
                        "CFFFFFC",
                        "CFFFFFC",
                        "CFFFFFC",
                        "CFFFFFC",
                        "CCCCCCC")
                .aisle("CVVVVVC",
                        "M#####M",
                        "M#####M",
                        "M#####M",
                        "M#####M",
                        "M#####M",
                        "CVVVVVC")
                .setRepeatable(5, 5)
                .aisle("CCCCCCC",
                        "CFFFFFC",
                        "CFFFFFC",
                        "CFFXFFC",
                        "CFFFFFC",
                        "CFFFFFC",
                        "CCCCCCC")
                .where('X', selfPredicate())
                .where('C', casing.or(autoAbilities()))
                .where('F', casing.or(tilePredicate((s, t) -> t instanceof MetaTileEntityFissionFuelRod, null)))
                .where('V', casing.or(tilePredicate((s, t) -> t instanceof MetaTileEntityFissionCoolantHatch, null)))
                .where('M',
                        casing.or(tilePredicate((s, t) -> t instanceof MetaTileEntityFissionTransmutationHatch, null)))
                .where('#',
                        air().or(states(MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.CasingType.INTERIOR_BEAM),
                                MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.CasingType.THERMAL_VOID))))
                .build();
    }

    protected IBlockState getCasingState() {
        // TODO placeholder
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        rods = context.getOrDefault("FissionRods", null);
        if (rods == null) return;
        assert structurePattern != null;
        int sum = 0;
        for (int i : structurePattern.formedRepetitionCount) {
            sum += i;
        }
        heatCapacityFromVolume = structurePattern.thumbLength * structurePattern.palmLength * sum;
        heatLossFromSurfaceArea = sum * structurePattern.thumbLength + sum * structurePattern.palmLength +
                structurePattern.thumbLength * structurePattern.palmLength;
        air = 0;
        thermalVoidCooling = 0;
        BlockPos.PooledMutableBlockPos mut = BlockPos.PooledMutableBlockPos.retain();
        for (var info : structurePattern.cache.entrySet()) {
            IBlockState state = info.getValue().getBlockState();
            BlockPos pos = BlockPos.fromLong(info.getKey());
            if (state.getBlock().isAir(state, getWorld(), pos)) {
                air++;
            } else if (state.getBlock() == MetaBlocks.FISSION_CASING &&
                    MetaBlocks.FISSION_CASING.getState(state) == BlockFissionCasing.CasingType.THERMAL_VOID) {
                        // check adjacency to fission rods
                        for (EnumFacing facing : EnumFacing.VALUES) {
                            mut.setPos(pos).move(facing);
                            if (RodPos.checkOverlap(facing.getAxis(), mut, rods::containsKey)) {
                                thermalVoidCooling += 8;
                            }
                        }
                    }
        }
        mut.release();
        recomputeRodStats();
    }

    @Override
    public void recomputeRodStats() {
        int fuelRodCount = 0;
        fuelRodPenalty10x = 10;
        instability = 0;
        fragility = 0;
        moderatorRodBonus = 0;
        controlRodBonus = 0;
        for (FissionRod rod : rods.values()) {
            IFissionRodPort.RodType type = rod.type();
            if (!type.isOperational(this, rod.port(), rod.opposingPort(), rod.length())) continue;
            fuelRodCount += type.getFuelRodCount();
            fuelRodPenalty10x += type.getFuelRodPenalty();
            instability += type.getInstabilityPerLength() * rod.length();
            fragility += type.getFragilityPerLength() * rod.length();
            moderatorRodBonus += type.getModeratorBonus();
            controlRodBonus += type.getControlRodBonus();
        }
        instability = Math.max(0, instability - air);
        fragility = Math.max(0, fragility - air);
        recipeMapWorkable.setParallelLimit(fuelRodCount);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected boolean finalStructureCheck(@NotNull PatternMatchContext context) {
        structureIssue = null;
        Set<IMultiblockPart> rawPartsSet = context.getOrCreate("MultiblockParts", HashSet::new);
        ArrayList<IMultiblockPart> parts = new ArrayList<>(rawPartsSet);
        AbilityInstances instances = new AbilityInstances(MultiblockAbility.FISSION_ROD_PORT);
        for (IMultiblockPart part : parts) {
            if (part instanceof IMultiblockAbilityPart abilityPart) {
                List<MultiblockAbility> abilityList = abilityPart.getAbilities();
                for (MultiblockAbility ability : abilityList) {
                    if (ability == MultiblockAbility.FISSION_ROD_PORT) {
                        abilityPart.registerAbilities(instances);
                        break;
                    }
                }
            }
        }
        Map<RodPos, FissionRod> rods = context.getOrCreate("FissionRods", Object2ObjectOpenHashMap::new);
        Long2ObjectMap<IFissionRodPort> unmatchedXYPorts = new Long2ObjectArrayMap<>();
        Long2ObjectMap<IFissionRodPort> unmatchedYZPorts = new Long2ObjectArrayMap<>();
        Long2ObjectMap<IFissionRodPort> unmatchedXZPorts = new Long2ObjectArrayMap<>();
        List<IFissionRodPort> ability = instances.cast();
        assert structurePattern != null;
        BlockPos.PooledMutableBlockPos mut = BlockPos.PooledMutableBlockPos.retain();
        outer:
        for (IFissionRodPort port : ability) {
            final BlockPos pos = port.getPos();
            inner:
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockInfo cache = structurePattern.cache.get(mut.setPos(pos).move(facing).toLong());
                if (cache == null) continue;
                if (cache.getBlockState().getBlock() != MetaBlocks.FISSION_CASING) continue;
                if (MetaBlocks.FISSION_CASING.getState(cache.getBlockState()) !=
                        BlockFissionCasing.CasingType.INTERIOR_BEAM)
                    continue;
                Long2ObjectMap<IFissionRodPort> map;
                long key = RodPos.key(facing.getAxis(), pos);
                ToIntFunction<BlockPos> selector;
                switch (facing.getAxis()) {
                    case X -> {
                        map = unmatchedYZPorts;
                        selector = Vec3i::getX;
                    }
                    case Y -> {
                        map = unmatchedXZPorts;
                        selector = Vec3i::getY;
                    }
                    default -> {
                        map = unmatchedXYPorts;
                        selector = Vec3i::getZ;
                    }
                }
                IFissionRodPort existing = map.get(key);
                if (existing != null) {
                    IFissionRodPort.RodType type = port.getRodType();
                    if (existing.getRodType() != type) return false;
                    int target = selector.applyAsInt(existing.getPos());
                    int dif = Math.abs(selector.applyAsInt(pos) - target);
                    if (dif < Math.abs(selector.applyAsInt(mut) - target)) {
                        // our EnumFacing is the wrong direction
                        facing = facing.getOpposite();
                        mut.move(facing, 2);
                    }
                    while (selector.applyAsInt(mut) != target) {
                        if (cache.getBlockState().getBlock() != MetaBlocks.FISSION_CASING ||
                                MetaBlocks.FISSION_CASING.getState(cache.getBlockState()) !=
                                        BlockFissionCasing.CasingType.INTERIOR_BEAM)
                            break inner;
                        if (RodPos.checkOverlap(facing.getAxis(), mut, rods::containsKey)) {
                            mut.release();
                            structureIssue = "gregtech.multiblock.fission.structure.intersecting";
                            return false;
                        }
                        mut.move(facing);
                    }
                    map.remove(key);
                    rods.put(new RodPos(facing.getAxis(), key), new FissionRod(port, existing, type, dif - 1));
                } else {
                    map.put(key, port);
                }
                continue outer;
            }
            mut.release();
            structureIssue = "gregtech.multiblock.fission.structure.missing_beam";
            return false;
        }
        mut.release();
        if (unmatchedXYPorts.isEmpty() && unmatchedXZPorts.isEmpty() && unmatchedYZPorts.isEmpty()) {
            return true;
        }
        structureIssue = "gregtech.multiblock.fission.structure.unmatched_port";
        return false;
    }

    @Override
    public int getBaseTemperature() {
        return FluidConstants.ROOM_TEMPERATURE;
    }

    @Override
    public int getTemperature() {
        return GTUtility.safeCastLongToInt(getBaseTemperature() + currentHeat / heatCapacityFromVolume);
    }

    @Override
    public int getInstability() {
        return instability;
    }

    @Override
    public int getTemperatureLimit() {
        return Math.max(getBaseTemperature() + 500, (int) (STANDARD_TEMPERATURE_LIMIT *
                Math.pow(TEMPERATURE_LIMIT_MULTIPLIER_PER_INSTABILITY, instability)));
    }

    @Override
    public int getFragility() {
        return fragility;
    }

    @Override
    public double getRateFactor() {
        return Math.pow(RATE_MULTIPLIER_PER_FRAGILITY, fragility);
    }

    @Override
    public void applyHeat(long heat) {
        this.currentHeat += heat;
        if (getTemperature() > getTemperatureLimit()) {
            // explodeMultiblock((float) Math.log(getTemperature()));
        }
    }

    @Override
    public double getModeratorRodBonus() {
        return moderatorRodBonus * 10 / fuelRodPenalty10x;
    }

    @Override
    public int getControlRodBonus() {
        return controlRodBonus * 10 / fuelRodPenalty10x;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(recipeMapWorkable.isWorkingEnabled(), recipeMapWorkable.isActive())
                .addEnergyUsageLine(getEnergyContainer())
                .addEnergyTierLine(GTUtility.getTierByVoltage(recipeMapWorkable.getMaxVoltage()))
                .addCustom(tl -> {
                    if (isStructureFormed()) {
                        FissionRecipeLogic logic = getRecipeMapWorkable();
                        ITextComponent tempStr = TextComponentUtil.stringWithColor(TextFormatting.RED,
                                getTemperature() + "K");
                        tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                                "gregtech.multiblock.fission.temperature", tempStr, getTemperatureLimit() + "K"));
                        if (logic.isActive()) {
                            FissionProperty.FissionValues values = logic.getValues();
                            double rate = logic.speedFactor(values) * 100;
                            ITextComponent rateStr = TextComponentUtil.stringWithColor(getColorForRate(rate),
                                    TextFormattingUtil.formatNumbers(rate) + "%");
                            tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                                    "gregtech.multiblock.fission.rate", rateStr));
                        }
                    }
                })
                .addParallelsLine(recipeMapWorkable.getParallelLimit())
                .addWorkingStatusLine()
                .addProgressLine(recipeMapWorkable.getProgressPercent());
    }

    private TextFormatting getColorForRate(double rate) {
        if (rate > 115) {
            return TextFormatting.WHITE;
        } else if (rate > 90) {
            return TextFormatting.GREEN;
        } else if (rate > 60) {
            return TextFormatting.DARK_GREEN;
        } else if (rate > 35) {
            return TextFormatting.YELLOW;
        } else if (rate > 15) {
            return TextFormatting.GOLD;
        } else {
            return TextFormatting.DARK_RED;
        }
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        super.addWarningText(textList);
        if (instability > 0) {
            textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                    "gregtech.multiblock.fission.unstable"));
        }
        if (fragility > 0) {
            textList.add(
                    TextComponentUtil.translationWithColor(TextFormatting.GRAY, "gregtech.multiblock.fission.fragile"));
        }
    }

    @Override
    protected void addErrorText(List<ITextComponent> textList) {
        if (isStructureFormed() && rods == null) {
            textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                    "gregtech.multiblock.fission.structure.init_failure"));
            return;
        }
        super.addErrorText(textList);
        if (isStructureFormed() && getRecipeMapWorkable().getParallelLimit() == 0) {
            textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                    "gregtech.multiblock.fission.structure.no_fuel"));
        }
        if (!isStructureFormed() && structureIssue != null) {
            textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, structureIssue));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.fission_reactor.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.fission_reactor.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.fission_reactor.tooltip.3"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setLong("Heat", currentHeat); // all other values are recomputed during structure load
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        currentHeat = data.getLong("Heat");
    }

    public static class FissionRecipeLogic extends MultiblockRecipeLogic {

        protected double actualRecipeProgress;
        protected @NotNull FissionProperty.FissionValues recipeValues = FissionProperty.FissionValues.EMPTY;

        public FissionRecipeLogic(MetaTileEntityFissionReactor tileEntity) {
            super(tileEntity);
        }

        @Override
        public @NotNull MetaTileEntityFissionReactor getMetaTileEntity() {
            return (MetaTileEntityFissionReactor) super.getMetaTileEntity();
        }

        public FissionProperty.FissionValues getValues() {
            return recipeValues;
        }

        public int optimalTemperature(@Nullable FissionProperty.FissionValues values) {
            if (values == null) values = getValues();
            return values.getOptimalTemperature() + getMetaTileEntity().getControlRodBonus();
        }

        public double speedFactor(@Nullable FissionProperty.FissionValues values) {
            if (values == null) values = getValues();
            int temp = getMetaTileEntity().getTemperature();
            int optimal = optimalTemperature(values);
            if (temp > optimal) {
                return getMetaTileEntity().getRateFactor() * (1 + getMetaTileEntity().getModeratorRodBonus()) *
                        Math.pow(values.getSpeedMultiplierPerKelvin(), temp - optimal);
            } else {
                return getMetaTileEntity().getRateFactor() * (1 + getMetaTileEntity().getModeratorRodBonus());
            }
        }

        protected void updateRecipeProgress() {
            if (getMetaTileEntity().rods == null) return;

            if (canRecipeProgress && drawEnergy(recipeEUt, true)) {
                drawEnergy(recipeEUt, false);
                FissionProperty.FissionValues values = getValues();
                double progress = speedFactor(values);
                actualRecipeProgress += progress;
                getMetaTileEntity()
                        .applyHeat((long) (values.getHeatEquivalentPerTick() * progress * parallelRecipesPerformed));
                if (actualRecipeProgress > maxProgressTime) {
                    completeRecipe();
                }
                if (this.hasNotEnoughEnergy && getEnergyInputPerSecond() > 19L * recipeEUt) {
                    this.hasNotEnoughEnergy = false;
                }
            } else if (recipeEUt > 0) {
                this.hasNotEnoughEnergy = true;
            }
        }

        @Override
        public int getProgress() {
            return (int) actualRecipeProgress;
        }

        @Override
        protected void setupRecipe(@NotNull Recipe recipe) {
            super.setupRecipe(recipe);
            actualRecipeProgress = 1;
            recipeValues = previousRecipe.getProperty(FissionProperty.getInstance(),
                    FissionProperty.FissionValues.EMPTY);
        }

        @Override
        protected void completeRecipe() {
            super.completeRecipe();
            actualRecipeProgress = 0;
        }

        @Override
        public void invalidate() {
            super.invalidate();
            actualRecipeProgress = 0;
        }

        @Override
        public @NotNull NBTTagCompound serializeNBT() {
            NBTTagCompound tag = super.serializeNBT();
            if (actualRecipeProgress > 0) {
                tag.setDouble("ActualProgress", actualRecipeProgress);
            }
            tag.setInteger("Parallels", parallelRecipesPerformed);
            tag.setLong("heatEquivalent", recipeValues.getHeatEquivalentPerTick());
            tag.setInteger("optimalTemp", recipeValues.getOptimalTemperature());
            tag.setDouble("penalty", recipeValues.getSpeedMultiplierPerKelvin());
            return tag;
        }

        @Override
        public void deserializeNBT(@NotNull NBTTagCompound compound) {
            super.deserializeNBT(compound);
            actualRecipeProgress = compound.getDouble("ActualProgress");
            parallelRecipesPerformed = compound.getInteger("Parallels");
            recipeValues = new FissionProperty.FissionValues(compound.getLong("heatEquivalent"),
                    compound.getInteger("optimalTemp"), compound.getDouble("penalty"));
        }
    }

    @Desugar
    protected record FissionRod(IFissionRodPort port, IFissionRodPort opposingPort, IFissionRodPort.RodType type,
                                int length) {}

    protected static final class RodPos {

        private final EnumFacing.@NotNull Axis axis;
        private final long pos;
        private int hash;

        public RodPos(EnumFacing.@NotNull Axis axis, long pos) {
            this.axis = axis;
            this.pos = pos;
        }

        public RodPos(EnumFacing.@NotNull Axis axis, @NotNull BlockPos pos) {
            this(axis, key(axis, pos));
        }

        public static long key(EnumFacing.@NotNull Axis axis, BlockPos pos) {
            return switch (axis) {
                case X -> pos.getY() + ((long) pos.getZ() << 32);
                case Y -> pos.getX() + ((long) pos.getZ() << 32);
                case Z -> pos.getX() + ((long) pos.getY() << 32);
            };
        }

        public static boolean checkOverlap(EnumFacing.@NotNull Axis axis, BlockPos pos,
                                           Predicate<RodPos> overlapCheck) {
            if (axis != EnumFacing.Axis.X && overlapCheck.test(new RodPos(EnumFacing.Axis.X, pos))) return true;
            if (axis != EnumFacing.Axis.Y && overlapCheck.test(new RodPos(EnumFacing.Axis.Y, pos))) return true;
            return axis != EnumFacing.Axis.Z && overlapCheck.test(new RodPos(EnumFacing.Axis.Z, pos));
        }

        public EnumFacing.@NotNull Axis axis() {
            return axis;
        }

        public long pos() {
            return pos;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (RodPos) obj;
            return Objects.equals(this.axis, that.axis) && this.pos == that.pos;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                hash = Objects.hash(axis, pos);
                if (hash == 0) hash = Integer.MAX_VALUE;
            }
            return hash;
        }

        @Override
        public String toString() {
            return "RodPos[" +
                    "axis=" + axis + ", " +
                    "pos=" + pos + ']';
        }
    }
}
