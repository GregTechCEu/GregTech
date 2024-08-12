package gregtech.api.metatileentity.multiblock;

import gregtech.api.GregTechAPI;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.capability.IMultipleRecipeMaps;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.pattern.pattern.IBlockPattern;
import gregtech.api.pattern.pattern.PatternState;
import gregtech.api.pattern.pattern.PreviewBlockPattern;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.world.DummyWorld;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.handler.MultiblockPreviewRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOrientedCubeRenderer;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static gregtech.api.capability.GregtechDataCodes.*;

public abstract class MultiblockControllerBase extends MetaTileEntity implements IMultiblockController {

    /**
     * Null until the first time {@link MultiblockControllerBase#getMatchingShapes()} is called, if it is not overriden
     */
    protected PreviewBlockPattern defaultPattern;

    private final Map<MultiblockAbility<Object>, List<Object>> multiblockAbilities = new HashMap<>();

    // treeset here to get logn time for contains, and for automatically sorting itself
    // prioritize the manually specified sorter first, defaulting to the hashcode for tiebreakers
    private final NavigableSet<IMultiblockPart> multiblockParts = new TreeSet<>(Comparator.comparingLong(part -> {
        MetaTileEntity mte = (MetaTileEntity) part;
        return ((long) multiblockPartSorter().apply(mte.getPos()) << 32) | mte.getPos().hashCode();
    }));

    protected EnumFacing upwardsFacing = EnumFacing.NORTH;
    protected final Object2ObjectMap<String, IBlockPattern> structures = new Object2ObjectOpenHashMap<>();

    public MultiblockControllerBase(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public void onPlacement(EntityLivingBase placer) {
        super.onPlacement(placer);
        reinitializeStructurePattern();
    }

    public void reinitializeStructurePattern() {
        createStructurePatterns();
        validateStructurePatterns();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (getOffsetTimer() % 20 == 0 || isFirstTick()) {
                checkStructurePatterns();
            }
            // DummyWorld is the world for the JEI preview. We do not want to update the Multi in this world,
            // besides initially forming it in checkStructurePattern
            if (isStructureFormed() && !(getWorld() instanceof DummyWorld)) {
                updateFormedValid();
            }
        }
    }

    /**
     * Called when the multiblock is formed and validation predicate is matched
     */
    protected abstract void updateFormedValid();

    /**
     * @return structure pattern of this multiblock
     */
    // todo fix central monitor, charcoal pile igniter, and cleanroom(and vacuum freezer)
    @NotNull
    protected abstract IBlockPattern createStructurePattern();

    protected void createStructurePatterns() {
        structures.put("MAIN", createStructurePattern());
    }

    private void validateStructurePatterns() {
        List<String> failures = new ArrayList<>();

        for (Object2ObjectMap.Entry<String, IBlockPattern> pattern : structures.object2ObjectEntrySet()) {
            if ("MAIN".equals(pattern.getKey())) continue;

            if (pattern.getValue().legacyBuilderError()) {
                failures.add(pattern.getKey());
            }
        }

        if (!failures.isEmpty()) {
            throw new IllegalStateException("Structure patterns " + Arrays.toString(failures.toArray()) +
                    " needs some legacy updating");
        }
    }

    public EnumFacing getUpwardsFacing() {
        return upwardsFacing;
    }

    public void setUpwardsFacing(EnumFacing upwardsFacing) {
        if (!allowsExtendedFacing()) return;
        if (upwardsFacing == null || upwardsFacing == EnumFacing.UP || upwardsFacing == EnumFacing.DOWN) {
            GTLog.logger.error("Tried to set upwards facing to invalid facing {}! Skipping", upwardsFacing);
            return;
        }
        if (this.upwardsFacing != upwardsFacing) {
            this.upwardsFacing = upwardsFacing;
            if (getWorld() != null && !getWorld().isRemote) {
                notifyBlockUpdate();
                markDirty();
                writeCustomData(UPDATE_UPWARDS_FACING, buf -> buf.writeByte(upwardsFacing.getIndex()));
                for (IBlockPattern pattern : structures.values()) {
                    pattern.clearCache();
                }
                checkStructurePatterns();
            }
        }
    }

    public boolean isFlipped() {
        return getSubstructure("MAIN").getPatternState().isFlipped();
    }

    /** <strong>Should not be called outside of structure formation logic!</strong> */
    @ApiStatus.Internal
    protected void setFlipped(boolean flipped, String name) {
        PatternState structure = getSubstructure(name).getPatternState();

        boolean flip = structure.isActualFlipped();
        if (flip != flipped) {
            structure.setActualFlipped(flipped);
            notifyBlockUpdate();
            markDirty();
            writeCustomData(UPDATE_FLIP, buf -> buf.writeString(name).writeBoolean(flipped));
        }
    }

    @SideOnly(Side.CLIENT)
    public abstract ICubeRenderer getBaseTexture(IMultiblockPart sourcePart);

    /**
     * Gets the inactive texture for this part, used for when the multiblock is unformed and you want the part to keep
     * its overlay. Return null to ignore and make hatches go back to their default textures on unform.
     */
    public @Nullable ICubeRenderer getInactiveTexture(IMultiblockPart part) {
        return null;
    }

    public boolean shouldRenderOverlay(IMultiblockPart sourcePart) {
        return true;
    }

    /**
     * Override this method to change the Controller overlay
     *
     * @return The overlay to render on the Multiblock Controller
     */
    @SideOnly(Side.CLIENT)
    @NotNull
    protected ICubeRenderer getFrontOverlay() {
        return Textures.MULTIBLOCK_WORKABLE_OVERLAY;
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getFrontDefaultTexture() {
        return getFrontOverlay().getParticleSprite();
    }

    public static TraceabilityPredicate tilePredicate(@NotNull BiFunction<BlockWorldState, MetaTileEntity, Boolean> predicate,
                                                      @Nullable Supplier<BlockInfo[]> candidates) {
        return new TraceabilityPredicate((worldState, patternState) -> {
            TileEntity tileEntity = worldState.getTileEntity();
            if (!(tileEntity instanceof IGregTechTileEntity))
                return false;
            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
            return predicate.apply(worldState, metaTileEntity);
        }, candidates);
    }

    public static TraceabilityPredicate metaTileEntities(MetaTileEntity... metaTileEntities) {
        ResourceLocation[] ids = Arrays.stream(metaTileEntities).filter(Objects::nonNull)
                .map(tile -> tile.metaTileEntityId).toArray(ResourceLocation[]::new);
        return tilePredicate((state, tile) -> ArrayUtils.contains(ids, tile.metaTileEntityId),
                getCandidates(metaTileEntities));
    }

    private static Supplier<BlockInfo[]> getCandidates(MetaTileEntity... metaTileEntities) {
        return () -> Arrays.stream(metaTileEntities).filter(Objects::nonNull).map(tile -> {
            MetaTileEntityHolder holder = new MetaTileEntityHolder();
            holder.setMetaTileEntity(tile);
            holder.getMetaTileEntity().onPlacement();
            holder.getMetaTileEntity().setFrontFacing(EnumFacing.SOUTH);
            return new BlockInfo(tile.getBlock().getDefaultState(), holder);
        }).toArray(BlockInfo[]::new);
    }

    private static Supplier<BlockInfo[]> getCandidates(IBlockState... allowedStates) {
        return () -> Arrays.stream(allowedStates).map(state -> new BlockInfo(state, null)).toArray(BlockInfo[]::new);
    }

    public static TraceabilityPredicate abilities(MultiblockAbility<?>... allowedAbilities) {
        return tilePredicate((state, tile) -> tile instanceof IMultiblockAbilityPart<?> &&
                ArrayUtils.contains(allowedAbilities, ((IMultiblockAbilityPart<?>) tile).getAbility()),
                getCandidates(Arrays.stream(allowedAbilities)
                        .flatMap(ability -> MultiblockAbility.REGISTRY.get(ability).stream())
                        .toArray(MetaTileEntity[]::new)));
    }

    public static TraceabilityPredicate states(IBlockState... allowedStates) {
        return new TraceabilityPredicate(
                (worldState, patternState) -> ArrayUtils.contains(allowedStates, worldState.getBlockState()),
                getCandidates(allowedStates));
    }

    /**
     * Use this predicate for Frames in your Multiblock. Allows for Framed Pipes as well as normal Frame blocks.
     */
    public static TraceabilityPredicate frames(Material... frameMaterials) {
        return states(Arrays.stream(frameMaterials).map(m -> MetaBlocks.FRAMES.get(m).getBlock(m))
                .toArray(IBlockState[]::new))
                        .or(new TraceabilityPredicate(blockWorldState -> {
                            TileEntity tileEntity = blockWorldState.getTileEntity();
                            if (!(tileEntity instanceof IPipeTile<?, ?>pipeTile)) {
                                return false;
                            }
                            return ArrayUtils.contains(frameMaterials, pipeTile.getFrameMaterial());
                        }));
    }

    public static TraceabilityPredicate blocks(Block... block) {
        return new TraceabilityPredicate(
                (worldState, patternState) -> ArrayUtils.contains(block, worldState.getBlockState().getBlock()),
                getCandidates(Arrays.stream(block).map(Block::getDefaultState).toArray(IBlockState[]::new)));
    }

    public static TraceabilityPredicate air() {
        return TraceabilityPredicate.AIR;
    }

    public static TraceabilityPredicate any() {
        return TraceabilityPredicate.ANY;
    }

    public static TraceabilityPredicate heatingCoils() {
        return TraceabilityPredicate.HEATING_COILS.get();
    }

    /**
     * Ensures that all the blockstates that are in the map are the same type. Returns the type if all match, or null if
     * they don't(or none match).
     * Example: {@code allSameType(GregTechAPI.HEATING_COILS, getSubstructure("MAIN").getCache())}
     * 
     * @param info  The info, such as GregTechAPI.HEATING_COILS
     * @param cache The cache for the pattern.
     */
    public static <V> V allSameType(Object2ObjectMap<IBlockState, V> info, Long2ObjectMap<BlockInfo> cache) {
        V type = null;
        for (BlockInfo blockInfo : cache.values()) {
            V state = info.get(blockInfo.getBlockState());
            if (state != null) {
                if (type != state) {
                    if (type == null) type = state;
                    else return null;
                }
            }
        }

        return type;
    }

    public TraceabilityPredicate selfPredicate() {
        return metaTileEntities(this).setCenter();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        ICubeRenderer baseTexture = getBaseTexture(null);
        pipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        if (baseTexture instanceof SimpleOrientedCubeRenderer) {
            baseTexture.renderOriented(renderState, translation, pipeline, getFrontFacing());
        } else {
            baseTexture.render(renderState, translation, pipeline);
        }

        if (allowsExtendedFacing()) {
            double degree = Math.PI / 2 * (upwardsFacing == EnumFacing.EAST ? -1 :
                    upwardsFacing == EnumFacing.SOUTH ? 2 : upwardsFacing == EnumFacing.WEST ? 1 : 0);
            Rotation rotation = new Rotation(degree, frontFacing.getXOffset(), frontFacing.getYOffset(),
                    frontFacing.getZOffset());
            translation.translate(0.5, 0.5, 0.5);
            if (frontFacing == EnumFacing.DOWN && upwardsFacing.getAxis() == EnumFacing.Axis.Z) {
                translation.apply(new Rotation(Math.PI, 0, 1, 0));
            }
            translation.apply(rotation);
            translation.scale(1.0000f);
            translation.translate(-0.5, -0.5, -0.5);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseTexture(null).getParticleSprite(), getPaintingColorForRendering());
    }

    /**
     * Override to disable Multiblock pattern from being added to Jei
     */
    public boolean shouldShowInJei() {
        return true;
    }

    /**
     * Used if MultiblockPart Abilities need to be sorted a certain way, like
     * Distillation Tower and Assembly Line.
     */
    protected Function<BlockPos, Integer> multiblockPartSorter() {
        return BlockPos::hashCode;
    }

    public void checkStructurePatterns() {
        for (String name : structures.keySet()) {
            checkStructurePattern(name);
        }
    }

    public void checkStructurePattern() {
        checkStructurePattern("MAIN");
    }

    public void checkStructurePattern(String name) {
        IBlockPattern pattern = getSubstructure(name);
        if (!pattern.getPatternState().shouldUpdate()) return;

        long time = System.nanoTime();
        PatternState result = pattern.checkPatternFastAt(getWorld(), getPos(),
                getFrontFacing(), getUpwardsFacing(), allowsFlip());
        GTLog.logger.info(
                "structure check for " + getClass().getSimpleName() + " took " + (System.nanoTime() - time) + " nanos");

        if (result.getState().isValid()) { // structure check succeeds
            // if structure isn't formed or cache fails
            if (result.isFormed()) {
                // fast rebuild parts
                if (result.getState() == PatternState.EnumCheckState.VALID_UNCACHED) {
                    // add any new parts, because removal of parts is impossible
                    // it is possible for old parts to persist, so check that
                    forEachMultiblockPart(name, part -> {
                        // this part is already added, so igore it
                        if (multiblockParts.contains(part)) return true;

                        // todo maybe move below into separate check?
                        if (part.isAttachedToMultiBlock() && !part.canPartShare(this, name)) {
                            invalidateStructure(name);
                            return false;
                        }
                        part.addToMultiBlock(this, name);
                        if (part instanceof IMultiblockAbilityPart<?>abilityPart) {
                            // noinspection unchecked
                            registerMultiblockAbility((IMultiblockAbilityPart<Object>) abilityPart);
                        }
                        return true;
                    });
                    formStructure(name);
                }
                return;
            }

            AtomicBoolean valid = new AtomicBoolean(true);

            forEachMultiblockPart(name, part -> {
                if (part.isAttachedToMultiBlock() && !part.canPartShare(this, name)) {
                    valid.set(false);
                    return false;
                }
                return true;
            });

            // since the structure isn't formed, don't invalidate, instead just don't form it
            if (!valid.get()) return;

            // normal rebuild parts
            forEachMultiblockPart(name, part -> {
                // parts *should* not have this controller added
                multiblockParts.add(part);
                part.addToMultiBlock(this, name);
                if (part instanceof IMultiblockAbilityPart<?>abilityPart) {
                    // noinspection unchecked
                    registerMultiblockAbility((IMultiblockAbilityPart<Object>) abilityPart);
                }
                return true;
            });
            formStructure(name);
        } else { // structure check fails
            if (result.isFormed()) { // invalidate if not already
                invalidateStructure(name);
            }
        }
    }

    /**
     * Perform an action for each multiblock part in the substructure. This uses the pattern's cache, which is always
     * accurate if the structure is valid(and has undefined behavior(probably empty) if not). Using the cache means
     * you can clear the multi's multiblock parts during this without causing a CME(which would happen if this iterates
     * over multiblockParts instead)
     * 
     * @param name   The name of the substructure.
     * @param action The action to perform. Return true if the iteration should keep going, or false if it should stop.
     *               This is for stuff like non-wallshareable hatches which instantly invalidate a multiblock.
     */
    protected void forEachMultiblockPart(String name, Predicate<IMultiblockPart> action) {
        Long2ObjectMap<BlockInfo> cache = getSubstructure(name).getCache();
        for (BlockInfo info : cache.values()) {
            TileEntity te = info.getTileEntity();
            if (!(te instanceof IGregTechTileEntity gtte)) continue;
            MetaTileEntity mte = gtte.getMetaTileEntity();
            if (mte instanceof IMultiblockPart part) {
                if (!action.test(part)) return;
            }
        }
    }

    protected List<BlockPos> getVABlocks(Long2ObjectMap<BlockInfo> cache) {
        List<BlockPos> pos = new ArrayList<>();
        for (Long2ObjectMap.Entry<BlockInfo> entry : cache.long2ObjectEntrySet()) {
            if (entry.getValue().getBlockState().getBlock() instanceof VariantActiveBlock<?>) {
                pos.add(BlockPos.fromLong(entry.getLongKey()));
            }
        }
        return pos;
    }

    protected void registerMultiblockAbility(IMultiblockAbilityPart<Object> part) {
        List<Object> abilityList = multiblockAbilities.computeIfAbsent(part.getAbility(), k -> new ArrayList<>());
        part.registerAbilities(abilityList);
    }

    // todo do
    protected void forEachFormed(String name, Consumer<BlockInfo> action) {
        Long2ObjectMap<BlockInfo> cache = getSubstructure(name).getCache();
        for (BlockInfo info : cache.values()) {
            action.accept(info);
        }
    }

    protected void formStructure(String name) {
        getSubstructure(name).getPatternState().setFormed(true);
        setFlipped(getSubstructure(name).getPatternState().isFlipped(), name);
        writeCustomData(STRUCTURE_FORMED, buf -> buf.writeString(name).writeBoolean(true));
    }

    /**
     * Use {@link MultiblockControllerBase#formStructure(String)} instead!
     */
    @Deprecated
    protected void formStructure() {
        formStructure("MAIN");
    }

    /**
     * Use {@link MultiblockControllerBase#invalidateStructure(String)} instead!
     */
    @Deprecated
    public void invalidateStructure() {
        invalidateStructure("MAIN");
    }

    public void invalidateStructure(String name) {
        if (!getSubstructure(name).getPatternState().isFormed()) return;
        // invalidate the main structure
        if ("MAIN".equals(name)) {
            this.multiblockParts.forEach(part -> part.removeFromMultiBlock(this));
            this.multiblockAbilities.clear();
            this.multiblockParts.clear();
            structures.forEach((s, p) -> {
                p.getPatternState().setFormed(false);
                p.getPatternState().setFlipped(false);
            });
            writeCustomData(STRUCTURE_FORMED, buf -> buf.writeString("null"));
        } else {
            getSubstructure(name).getPatternState().setFormed(false);
            writeCustomData(STRUCTURE_FORMED, buf -> buf.writeString(name).writeBoolean(false));

            multiblockParts.removeIf(part -> {
                if (name.equals(part.getSubstructureName())) {
                    part.removeFromMultiBlock(this);
                    return true;
                }
                return false;
            });
        }
    }

    protected void invalidStructureCaches() {
        for (IBlockPattern pattern : structures.values()) {
            pattern.clearCache();
        }
    }

    protected IBlockPattern getSubstructure(String name) {
        return structures.get(name);
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!getWorld().isRemote) {
            invalidateStructure("MAIN");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAbilities(MultiblockAbility<T> ability) {
        List<T> rawList = (List<T>) multiblockAbilities.getOrDefault(ability, Collections.emptyList());
        return Collections.unmodifiableList(rawList);
    }

    // todo fix/update usages of this
    public NavigableSet<IMultiblockPart> getMultiblockParts() {
        return Collections.unmodifiableNavigableSet(multiblockParts);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("UpwardsFacing")) {
            this.upwardsFacing = EnumFacing.VALUES[data.getByte("UpwardsFacing")];
        }
        this.reinitializeStructurePattern();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setByte("UpwardsFacing", (byte) upwardsFacing.getIndex());
        return data;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(upwardsFacing.getIndex());
        // todo see if necessary to sync this
        // buf.writeLong(GTUtility.boolArrToLong(s));
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.upwardsFacing = EnumFacing.VALUES[buf.readByte()];
        // GTUtility.longToBoolArr(buf.readLong(), structuresFormed);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_UPWARDS_FACING) {
            this.upwardsFacing = EnumFacing.VALUES[buf.readByte()];
            scheduleRenderUpdate();
        } else if (dataId == STRUCTURE_FORMED) {
            // it forces me so uh yay
            String name = buf.readString(65536);
            if ("null".equals(name)) {
                for (IBlockPattern pattern : structures.values()) {
                    pattern.getPatternState().setFormed(false);
                }
            } else {
                getSubstructure(name).getPatternState().setFormed(buf.readBoolean());
            }

            if (!isStructureFormed()) {
                GregTechAPI.soundManager.stopTileSound(getPos());
            }
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        T result = super.getCapability(capability, side);
        if (result != null)
            return result;
        if (capability == GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER) {
            return GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER.cast(this);
        }
        return null;
    }

    public boolean isStructureFormed() {
        return isStructureFormed("MAIN");
    }

    public boolean isStructureFormed(String name) {
        if (getWorld() == null) return false;

        return getSubstructure(name).getPatternState().isFormed();
    }

    @Override
    public void setFrontFacing(EnumFacing frontFacing) {
        EnumFacing oldFrontFacing = getFrontFacing();
        super.setFrontFacing(frontFacing);

        // Set the upwards facing in a way that makes it "look like" the upwards facing wasn't changed
        if (allowsExtendedFacing()) {
            EnumFacing newUpwardsFacing = RelativeDirection.simulateAxisRotation(frontFacing, oldFrontFacing,
                    getUpwardsFacing());
            setUpwardsFacing(newUpwardsFacing);
        }

        if (getWorld() != null && !getWorld().isRemote) {
            // clear cache since the cache has no concept of pre-existing facing
            // for the controller block (or any block) in the structure
            invalidStructureCaches();
            // recheck structure pattern immediately to avoid a slight "lag"
            // on deforming when rotating a multiblock controller
            checkStructurePatterns();
        }
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        if (this instanceof IMultipleRecipeMaps) {
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.toggle_mode_covers"));
        } else {
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        }
        if (allowsExtendedFacing()) {
            tooltip.add(I18n.format("gregtech.tool_action.wrench.extended_facing"));
        } else {
            tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        }
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (super.onRightClick(playerIn, hand, facing, hitResult))
            return true;

        if (this.getWorld().isRemote && !this.isStructureFormed() && playerIn.isSneaking() &&
                playerIn.getHeldItem(hand).isEmpty()) {
            MultiblockPreviewRenderer.renderMultiBlockPreview(this, 60000);
            return true;
        }
        return false;
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide,
                                 CuboidRayTraceResult hitResult) {
        if (wrenchSide == getFrontFacing() && allowsExtendedFacing()) {
            if (!getWorld().isRemote) {
                setUpwardsFacing(playerIn.isSneaking() ? upwardsFacing.rotateYCCW() : upwardsFacing.rotateY());
            }
            return true;
        }
        return super.onWrenchClick(playerIn, hand, wrenchSide, hitResult);
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return allowsExtendedFacing() || super.isValidFrontFacing(facing);
    }

    // todo tooltip on multis saying if this is enabled or disabled?
    /** Whether this multi can be rotated or face upwards. */
    public boolean allowsExtendedFacing() {
        return true;
    }

    /** Set this to false only if your multiblock is set up such that it could have a wall-shared controller. */
    public boolean allowsFlip() {
        return true;
    }

    public List<MultiblockShapeInfo> getMatchingShapes() {
        return Collections.emptyList();
    }

    /**
     * The new(and better) way of getting shapes for in world, jei, and autobuild. Default impl just converts
     * {@link MultiblockControllerBase#getMatchingShapes()}, if not empty, to this. If getMatchingShapes is empty, uses
     * a default generated structure pattern, it's not very good which is why you should override this.
     * 
     * @param keyMap  A map for autobuild, or null if it is an in world or jei preview.
     * @param hatches This is whether you should put hatches, JEI previews need hatches, but autobuild and in world
     *                previews shouldn't(unless the hatch is necessary and only has one valid spot, such as EBF)
     */
    // todo add use for the keyMap with the multiblock builder
    public List<PreviewBlockPattern> getBuildableShapes(@Nullable Object2IntMap<String> keyMap, boolean hatches) {
        List<MultiblockShapeInfo> infos = getMatchingShapes();

        // if there is no overriden getMatchingShapes() just return the default one
        if (infos.isEmpty()) {
            if (defaultPattern == null) {

                // only generate for the first pattern, if you have more than 1 pattern you better override this
                defaultPattern = getSubstructure("MAIN").getDefaultShape();

                if (defaultPattern == null) return Collections.emptyList();
            }

            return Collections.singletonList(defaultPattern);
        }

        // otherwise just convert them all the preview block pattern and return
        return getMatchingShapes().stream().map(PreviewBlockPattern::new).collect(Collectors.toList());
    }

    @SideOnly(Side.CLIENT)
    public String[] getDescription() {
        String key = String.format("gregtech.multiblock.%s.description", metaTileEntityId.getPath());
        return I18n.hasKey(key) ? new String[] { I18n.format(key) } : new String[0];
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    public void explodeMultiblock(float explosionPower) {
        ;
        for (IMultiblockPart part : multiblockParts) {
            part.removeFromMultiBlock(this);
            ((MetaTileEntity) part).doExplosion(explosionPower);
        }
        doExplosion(explosionPower);
    }

    /**
     * @param part the part to check
     * @return if the multiblock part is terrain and weather resistant
     */
    public boolean isMultiblockPartWeatherResistant(@NotNull IMultiblockPart part) {
        return false;
    }
}
