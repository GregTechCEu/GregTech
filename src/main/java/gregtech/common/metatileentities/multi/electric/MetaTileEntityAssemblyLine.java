package gregtech.common.metatileentities.multi.electric;

import com.github.bsideup.jabel.Desugar;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.DistinctRecipeLogic;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTFluidIngredient;
import gregtech.api.recipes.ingredients.GTItemIngredient;
import gregtech.api.recipes.ingredients.match.AbstractMatchCalculation;
import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.recipes.ingredients.match.MatchRollController;
import gregtech.api.recipes.ingredients.old.GTRecipeInput;
import gregtech.api.recipes.properties.impl.ResearchProperty;
import gregtech.api.util.GTUtility;
import gregtech.api.util.RelativeDirection;
import gregtech.client.particle.GTLaserBeamParticle;
import gregtech.client.particle.GTParticleManager;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import gregtech.core.sound.GTSoundEvents;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityAssemblyLine extends RecipeMapMultiblockController {

    private static final ResourceLocation LASER_LOCATION = GTUtility.gregtechId("textures/fx/laser/laser.png");
    private static final ResourceLocation LASER_HEAD_LOCATION = GTUtility
            .gregtechId("textures/fx/laser/laser_start.png");

    @SideOnly(Side.CLIENT)
    private GTLaserBeamParticle[][] beamParticles;
    private int beamCount;
    private int beamTime;

    protected @Nullable List<IItemHandlerModifiable> orderedItemBusesCache;
    protected @Nullable List<IMultipleTankHandler> orderedFluidHatchesCache;

    public MetaTileEntityAssemblyLine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.ASSEMBLY_LINE_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAssemblyLine(metaTileEntityId);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern pattern = FactoryBlockPattern.start(FRONT, UP, RIGHT)
                .aisle("FIF", "RTR", "SAG", " Y ")
                .aisle("FIF", "RTR", "DAG", " Y ").setRepeatable(3, 15)
                .aisle("FOF", "RTR", "DAG", " Y ")
                .where('S', selfPredicate())
                .where('F', states(getCasingState())
                        .or(autoAbilities(false, true, false, false, false, false, false))
                        .or(fluidInputPredicate()))
                .where('O', abilities(MultiblockAbility.EXPORT_ITEMS)
                        .addTooltips("gregtech.multiblock.pattern.location_end"))
                .where('Y', states(getCasingState())
                        .or(abilities(MultiblockAbility.INPUT_ENERGY)
                                .setMinGlobalLimited(1)
                                .setMaxGlobalLimited(3)))
                .where('I', abilities(MultiblockAbility.IMPORT_ITEMS))
                .where('G', states(getGrateState()))
                .where('A',
                        states(MetaBlocks.MULTIBLOCK_CASING
                                .getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_CONTROL)))
                .where('R', states(MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.LAMINATED_GLASS)))
                .where('T',
                        states(MetaBlocks.MULTIBLOCK_CASING
                                .getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_LINE_CASING)))
                .where('D', dataHatchPredicate())
                .where(' ', any());
        return pattern.build();
    }

    @NotNull
    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @NotNull
    protected static IBlockState getGrateState() {
        return MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING);
    }

    @NotNull
    protected static TraceabilityPredicate fluidInputPredicate() {
        // block multi-fluid hatches if ordered fluids is enabled
        if (ConfigHolder.machines.orderedFluidAssembly) {
            return metaTileEntities(MultiblockAbility.REGISTRY.get(MultiblockAbility.IMPORT_FLUIDS).stream()
                    .filter(mte -> !(mte instanceof MetaTileEntityMultiFluidHatch))
                    .toArray(MetaTileEntity[]::new))
                            .setMaxGlobalLimited(4);
        }
        return abilities(MultiblockAbility.IMPORT_FLUIDS);
    }

    @NotNull
    protected static TraceabilityPredicate dataHatchPredicate() {
        // if research is enabled, require the data hatch, otherwise use a grate instead
        if (ConfigHolder.machines.enableResearch) {
            return abilities(MultiblockAbility.DATA_ACCESS_HATCH, MultiblockAbility.OPTICAL_DATA_RECEPTION)
                    .setExactLimit(1)
                    .or(states(getGrateState()));
        }
        return states(getGrateState());
    }

    @Override
    protected Function<BlockPos, Integer> multiblockPartSorter() {
        // player's right when looking at the controller, but the controller's left
        return RelativeDirection.LEFT.getSorter(getFrontFacing(), getUpwardsFacing(), isFlipped());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart != null) {
            // part rendering
            if (sourcePart instanceof IDataAccessHatch) {
                return Textures.GRATE_CASING_STEEL_FRONT;
            } else {
                return Textures.SOLID_STEEL_CASING;
            }
        } else {
            // controller rendering
            if (isStructureFormed()) {
                return Textures.GRATE_CASING_STEEL_FRONT;
            } else {
                return Textures.SOLID_STEEL_CASING;
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());
    }

    @Override
    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_MECHANICAL;
    }

    @Override
    public void update() {
        super.update();
        if (ConfigHolder.client.shader.assemblyLineParticles) {
            if (getRecipeMapWorkable().isWorking()) {
                int maxBeams = getAbilities(MultiblockAbility.IMPORT_ITEMS).size() + 1;
                int maxProgress = getRecipeMapWorkable().getMaxProgress();

                // Each beam should be visible for an equal amount of time, which is derived from the maximum number of
                // beams and the maximum progress in the recipe.
                int beamTime = Math.max(1, maxProgress / maxBeams);

                int beamCount = Math.min(maxBeams, getRecipeMapWorkable().getProgress() / beamTime + 1);

                if (beamCount != this.beamCount) {
                    if (beamCount < this.beamCount) {
                        // if beam count decreases, the last beam in the queue needs to be removed for the sake of fade
                        // time.
                        this.beamCount = Math.max(0, beamCount - 1);
                        writeCustomData(GregtechDataCodes.UPDATE_PARTICLE, this::writeParticles);
                    }
                    this.beamTime = beamTime;
                    this.beamCount = beamCount;
                    writeCustomData(GregtechDataCodes.UPDATE_PARTICLE, this::writeParticles);
                }
            } else if (beamCount != 0) {
                this.beamTime = 0;
                this.beamCount = 0;
                writeCustomData(GregtechDataCodes.UPDATE_PARTICLE, this::writeParticles);
            }
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        writeParticles(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        readParticles(buf);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.UPDATE_PARTICLE) {
            readParticles(buf);
        } else {
            super.receiveCustomData(dataId, buf);
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (getWorld().isRemote && beamParticles != null) {
            for (GTLaserBeamParticle[] particle : beamParticles) {
                if (particle[0] != null) {
                    particle[0].setExpired();
                    particle[1].setExpired();
                }
            }
            beamParticles = null;
        }
    }

    private void writeParticles(@NotNull PacketBuffer buf) {
        buf.writeVarInt(beamCount);
        buf.writeVarInt(beamTime);
    }

    @SideOnly(Side.CLIENT)
    private void readParticles(@NotNull PacketBuffer buf) {
        beamCount = buf.readVarInt();
        beamTime = buf.readVarInt();
        if (beamParticles == null) {
            beamParticles = new GTLaserBeamParticle[17][2];
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(getPos());

        EnumFacing relativeUp = RelativeDirection.UP.getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                isFlipped());
        EnumFacing relativeLeft = RelativeDirection.LEFT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                isFlipped());
        boolean negativeUp = relativeUp.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;

        for (int i = 0; i < beamParticles.length; i++) {
            GTLaserBeamParticle particle = beamParticles[i][0];
            if (i < beamCount && particle == null) {
                pos.setPos(getPos());
                if (negativeUp) {
                    // correct for the position of the block corresponding to its negative side
                    pos.move(relativeUp.getOpposite());
                }
                Vector3 startPos = new Vector3()
                        .add(pos.move(relativeLeft, i))
                        .add( // offset by 0.5 in both non-"upwards" directions
                                relativeUp.getAxis() == EnumFacing.Axis.X ? 0 : 0.5,
                                relativeUp.getAxis() == EnumFacing.Axis.Y ? 0 : 0.5,
                                relativeUp.getAxis() == EnumFacing.Axis.Z ? 0 : 0.5);
                Vector3 endPos = startPos.copy()
                        .subtract(relativeUp.getXOffset(), relativeUp.getYOffset(), relativeUp.getZOffset());

                beamParticles[i][0] = createALParticles(startPos, endPos);

                pos.setPos(getPos());
                if (negativeUp) {
                    pos.move(relativeUp.getOpposite());
                }
                startPos = new Vector3()
                        .add(pos.move(relativeLeft, i)
                                .move(getFrontFacing().getOpposite(), 2))
                        .add( // offset by 0.5 in both non-"upwards" directions
                                relativeUp.getAxis() == EnumFacing.Axis.X ? 0 : 0.5,
                                relativeUp.getAxis() == EnumFacing.Axis.Y ? 0 : 0.5,
                                relativeUp.getAxis() == EnumFacing.Axis.Z ? 0 : 0.5);
                endPos = startPos.copy()
                        .subtract(relativeUp.getXOffset(), relativeUp.getYOffset(), relativeUp.getZOffset());

                beamParticles[i][1] = createALParticles(startPos, endPos);

                // Don't forget to add particles
                GTParticleManager.INSTANCE.addEffect(beamParticles[i][0]);
                GTParticleManager.INSTANCE.addEffect(beamParticles[i][1]);

            } else if (i >= beamCount && particle != null) {
                particle.setExpired();
                beamParticles[i][0] = null;
                beamParticles[i][1].setExpired();
                beamParticles[i][1] = null;
            }
        }
    }

    @NotNull
    @SideOnly(Side.CLIENT)
    private GTLaserBeamParticle createALParticles(Vector3 startPos, Vector3 endPos) {
        return new GTLaserBeamParticle(this, startPos, endPos, beamTime)
                .setBody(LASER_LOCATION)
                .setBeamHeight(0.125f)
                // Try commenting or adjusting on the next four lines to see what happens
                .setDoubleVertical(true)
                .setHead(LASER_HEAD_LOCATION)
                .setHeadWidth(0.1f)
                .setEmit(0.2f);
    }

    @NotNull
    public List<IMultipleTankHandler> getOrderedFluidHatches() {
        if (orderedFluidHatchesCache != null) return orderedFluidHatchesCache;
        List<IMultipleTankHandler> orderedHandlerList = new ObjectArrayList<>();
        this.getMultiblockParts().stream()
                .filter(iMultiblockPart -> iMultiblockPart instanceof IMultiblockAbilityPart<?> abilityPart &&
                        abilityPart.getAbility() == MultiblockAbility.EXPORT_FLUIDS &&
                        abilityPart instanceof MetaTileEntityMultiblockPart)
                .map(iMultiblockPart -> (MetaTileEntityMultiblockPart) iMultiblockPart)
                .forEach(hatch -> {
                    List<IFluidTank> hatchTanks = new ObjectArrayList<>();
                    // noinspection unchecked
                    ((IMultiblockAbilityPart<IFluidTank>) hatch).registerAbilities(hatchTanks);
                    orderedHandlerList.add(new FluidTankList(false, hatchTanks));
                });
        return (orderedFluidHatchesCache = orderedHandlerList);
    }

    public List<IItemHandlerModifiable> getOrderedItemBuses() {
        if (orderedItemBusesCache != null) return orderedItemBusesCache;
        return (orderedItemBusesCache = this.getAbilities(MultiblockAbility.IMPORT_ITEMS));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        orderedItemBusesCache = null;
        orderedFluidHatchesCache = null;
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe, boolean consumeIfSuccess) {
        if (consumeIfSuccess) return true; // don't check twice
        if (getOrderedItemBuses().size() < recipe.getItemIngredients().size() ||
                getOrderedFluidHatches().size() < recipe.getFluidIngredients().size()) return false;
        if (!ConfigHolder.machines.enableResearch || !recipe.hasProperty(ResearchProperty.getInstance())) {
            return super.checkRecipe(recipe, false);
        } else {
            return isRecipeAvailable(getAbilities(MultiblockAbility.DATA_ACCESS_HATCH), recipe) ||
                    isRecipeAvailable(getAbilities(MultiblockAbility.OPTICAL_DATA_RECEPTION), recipe);
        }
    }

    private static boolean isRecipeAvailable(@NotNull Iterable<? extends IDataAccessHatch> hatches,
                                             @NotNull Recipe recipe) {
        for (IDataAccessHatch hatch : hatches) {
            // creative hatches do not need to check, they always have the recipe
            if (hatch.isCreative()) return true;

            // hatches need to have the recipe available
            if (hatch.isRecipeAvailable(recipe)) return true;
        }
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        if (ConfigHolder.machines.orderedAssembly && ConfigHolder.machines.orderedFluidAssembly) {
            tooltip.add(I18n.format("gregtech.machine.assembly_line.tooltip_ordered_both"));
        } else if (ConfigHolder.machines.orderedAssembly) {
            tooltip.add(I18n.format("gregtech.machine.assembly_line.tooltip_ordered_items"));
        } else if (ConfigHolder.machines.orderedFluidAssembly) {
            tooltip.add(I18n.format("gregtech.machine.assembly_line.tooltip_ordered_fluids"));
        }
    }

    protected static class AssemblyLineRecipeLogic extends MultiblockRecipeLogic {

        private final List<DistinctInputGroup> group = Collections.singletonList(new AssemblyLineInputGroup());

        public AssemblyLineRecipeLogic(MetaTileEntityAssemblyLine tileEntity) {
            super(tileEntity);
        }

        @Override
        public @NotNull MetaTileEntityAssemblyLine getMetaTileEntity() {
            return (MetaTileEntityAssemblyLine) metaTileEntity;
        }

        @Override
        public Collection<DistinctInputGroup> getInputGroups() {
            return group;
        }

        @Override
        protected @NotNull MatchCalculation<ItemStack> getItemMatch(@NotNull Recipe recipe,
                                                                    @NotNull List<ItemStack> items) {
            if (ConfigHolder.machines.orderedAssembly) return new OrderedItemMatch(recipe.getItemIngredients(),
                    getMetaTileEntity().getOrderedItemBuses(), items);
            else return super.getItemMatch(recipe, items);
        }

        @Override
        protected @NotNull MatchCalculation<FluidStack> getFluidMatch(@NotNull Recipe recipe,
                                                                      @NotNull List<FluidStack> fluids) {
            if (ConfigHolder.machines.orderedFluidAssembly) return new OrderedFluidMatch(recipe.getFluidIngredients(),
                        getMetaTileEntity().getOrderedFluidHatches(), fluids);
            else return super.getFluidMatch(recipe, fluids);
        }

        public class AssemblyLineInputGroup implements DistinctRecipeLogic.DistinctInputGroup {

            private final List<ItemStack> item = new AbstractList<>() {

                @Override
                public ItemStack set(int index, ItemStack element) {
                    for (IItemHandlerModifiable bus : getMetaTileEntity().getOrderedItemBuses()) {
                        int size = bus.getSlots();
                        if (index >= size) {
                            index -= size;
                            continue;
                        }
                        ItemStack oldStack = bus.getStackInSlot(index);
                        bus.setStackInSlot(index, element == null ? ItemStack.EMPTY : element);
                        return oldStack;
                    }
                    throw new IndexOutOfBoundsException();
                }

                @Override
                public ItemStack get(int index) {
                    for (IItemHandlerModifiable bus : getMetaTileEntity().getOrderedItemBuses()) {
                        int size = bus.getSlots();
                        if (index >= size) {
                            index -= size;
                            continue;
                        }
                        return bus.getStackInSlot(index);
                    }
                    throw new IndexOutOfBoundsException();
                }

                @Override
                public int size() {
                    int size = 0;
                    for (IItemHandlerModifiable bus : getMetaTileEntity().getOrderedItemBuses()) {
                        size += bus.getSlots();
                    }
                    return size;
                }
            };

            private final List<FluidStack> fluid = new AbstractList<>() {

                @Override
                public FluidStack set(int index, FluidStack element) {
                    for (IMultipleTankHandler hatch : getMetaTileEntity().getOrderedFluidHatches()) {
                        int size = hatch.getTanks();
                        if (index >= size) {
                            index -= size;
                            continue;
                        }
                        IFluidTank fluidTank = hatch.getTankAt(index).getDelegate();
                        FluidStack oldStack = fluidTank.getFluid();
                        if (fluidTank instanceof FluidTank) {
                            ((FluidTank) fluidTank).setFluid(element);
                        }
                        return oldStack;
                    }
                    throw new IndexOutOfBoundsException();
                }

                @Override
                public FluidStack get(int index) {
                    for (IMultipleTankHandler hatch : getMetaTileEntity().getOrderedFluidHatches()) {
                        int size = hatch.getTanks();
                        if (index >= size) {
                            index -= size;
                            continue;
                        }
                        return hatch.getTankAt(index).getFluid();
                    }
                    throw new IndexOutOfBoundsException();
                }

                @Override
                public int size() {
                    int size = 0;
                    for (IMultipleTankHandler hatch : getMetaTileEntity().getOrderedFluidHatches()) {
                        size += hatch.getTanks();
                    }
                    return size;
                }
            };

            @Override
            public @NotNull List<ItemStack> itemInventoryView() {
                return item;
            }

            @Override
            public boolean containsItemHandler(IItemHandlerModifiable handler) {
                return true;
            }

            @Override
            public @NotNull List<FluidStack> fluidInventoryView() {
                return fluid;
            }

            @Override
            public boolean containsFluidHandler(IFluidHandler handler) {
                return true;
            }

            @Override
            public void setInvalidItemInputs(boolean invalidInputs) {
                AssemblyLineRecipeLogic.super.setInvalidItemInputs(invalidInputs);
            }

            @Override
            public void setInvalidFluidInputs(boolean invalidInputs) {
                AssemblyLineRecipeLogic.super.setInvalidFluidInputs(invalidInputs);
            }

            @Override
            public boolean areItemInputsInvalid() {
                return hasInvalidItemInputs();
            }

            @Override
            public boolean areFluidInputsInvalid() {
                return hasInvalidFluidInputs();
            }

        }
    }

    protected static class OrderedItemMatch extends AbstractMatchCalculation<ItemStack> {

        protected final List<GTItemIngredient> ingredients;
        protected final List<IItemHandlerModifiable> orderedItemInputs;
        protected final List<ItemStack> itemView;

        public OrderedItemMatch(List<GTItemIngredient> ingredients, List<IItemHandlerModifiable> orderedItemInputs,
                                List<ItemStack> itemView) {
            this.ingredients = ingredients;
            this.orderedItemInputs = orderedItemInputs;
            this.itemView = itemView;
            if (ingredients.size() > orderedItemInputs.size()) reportNoValidScales();
        }

        @Override
        protected void rescale(int oldScale, int newScale) {}

        @Override
        protected long @Nullable [] attemptScaleInternal() {
            long[] consumptions = new long[itemView.size()];
            int offset = 0;
            for (int i = 0; i < ingredients.size(); i++) {
                GTItemIngredient ingredient = ingredients.get(i);
                int size = orderedItemInputs.get(i).getSlots();
                long desired = ingredient.getRequiredCount() * scaling;
                for (int j = offset; j < offset + size; j++) {
                    ItemStack stack = itemView.get(j);
                    if (ingredient.matches(stack)) {
                        int count = (int) Math.min(stack.getCount(), desired);
                        consumptions[j] = count;
                        desired -= count;
                        if (desired == 0) break;
                    }
                }
                // fail immediately if we can't match a given ingredient.
                if (desired > 0) return null;
                offset += size;
            }
            return consumptions;
        }

        @Override
        public @NotNull List<ItemStack> getConsumed(int scale) {
            // fail if we could not match at this scale
            long[] results = getMatchResultsForScale(scale);
            if (results == null) return Collections.emptyList();
            //noinspection rawtypes
            if (ingredients instanceof MatchRollController controller) {
                int offset = 0;
                long[] roll = controller.getConsumptionRollResults(scale);
                for (int i = 0; i < ingredients.size(); i++) {
                    GTItemIngredient ingredient = ingredients.get(i);
                    int size = orderedItemInputs.get(i).getSlots();
                    long desired = roll[i];
                    for (int j = offset; j < offset + size; j++) {
                        ItemStack stack = itemView.get(j);
                        if (ingredient.matches(stack)) {
                            int count = (int) Math.min(stack.getCount(), desired);
                            results[j] = count;
                            desired -= count;
                            if (desired == 0) break;
                        }
                    }
                    // should never happen unless some idiot matcher is requiring more after roll than before.
                    if (desired > 0) return Collections.emptyList();
                    offset += size;
                }
            }
            List<ItemStack> list = new ObjectArrayList<>(itemView.size());
            for (int i = 0; i < itemView.size(); i++) {
                ItemStack stack = itemView.get(i).copy();
                stack.setCount((int) results[i]);
                list.add(stack);
            }
            return list;
        }
    }

    protected static class OrderedFluidMatch extends AbstractMatchCalculation<FluidStack> {

        protected final List<GTFluidIngredient> ingredients;
        protected final List<IMultipleTankHandler> orderedFluidInputs;
        protected final List<FluidStack> fluidView;

        public OrderedFluidMatch(List<GTFluidIngredient> ingredients, List<IMultipleTankHandler> orderedFluidInputs,
                                 List<FluidStack> fluidView) {
            this.ingredients = ingredients;
            this.orderedFluidInputs = orderedFluidInputs;
            this.fluidView = fluidView;
        }

        @Override
        protected void rescale(int oldScale, int newScale) {}

        @Override
        protected long @Nullable [] attemptScaleInternal() {
            long[] consumptions = new long[fluidView.size()];
            int offset = 0;
            for (int i = 0; i < ingredients.size(); i++) {
                GTFluidIngredient ingredient = ingredients.get(i);
                int size = orderedFluidInputs.get(i).getTanks();
                long desired = ingredient.getRequiredCount() * scaling;
                for (int j = offset; j < offset + size; j++) {
                    FluidStack stack = fluidView.get(j);
                    if (ingredient.matches(stack)) {
                        long count = Math.min(stack.amount, desired);
                        consumptions[j] = count;
                        desired -= count;
                        if (desired == 0) break;
                    }
                }
                // fail immediately if we can't match a given ingredient.
                if (desired > 0) return null;
                offset += size;
            }
            return consumptions;
        }

        @Override
        public @NotNull List<FluidStack> getConsumed(int scale) {
            // fail if we could not match at this scale
            long[] results = getMatchResultsForScale(scale);
            if (results == null) return Collections.emptyList();

            //noinspection rawtypes
            if (ingredients instanceof MatchRollController controller) {
                int offset = 0;
                long[] roll = controller.getConsumptionRollResults(scale);
                for (int i = 0; i < ingredients.size(); i++) {
                    GTFluidIngredient ingredient = ingredients.get(i);
                    int size = orderedFluidInputs.get(i).getTanks();
                    long desired = roll[i];
                    for (int j = offset; j < offset + size; j++) {
                        FluidStack stack = fluidView.get(j);
                        if (ingredient.matches(stack)) {
                            long count = Math.min(stack.amount, desired);
                            results[j] = count;
                            desired -= count;
                            if (desired == 0) break;
                        }
                    }
                    // should never happen unless some idiot matcher is requiring more after roll than before.
                    if (desired > 0) return Collections.emptyList();
                    offset += size;
                }
            }
            List<FluidStack> list = new ObjectArrayList<>(fluidView.size());
            for (int i = 0; i < fluidView.size(); i++) {
                FluidStack stack = fluidView.get(i).copy();
                stack.amount = (int) results[i];
                list.add(stack);
            }
            return list;
        }
    }
}
