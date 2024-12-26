package gregtech.api.metatileentity.multiblock;

import gregtech.api.GregTechAPI;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.capability.IMultipleRecipeMaps;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.GreggyBlockPos;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternError;
import gregtech.api.pattern.PatternStringError;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.pattern.pattern.IBlockPattern;
import gregtech.api.pattern.pattern.PatternState;
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
import net.minecraft.util.math.MathHelper;
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
import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static gregtech.api.capability.GregtechDataCodes.*;

public abstract class MultiblockControllerBase extends MetaTileEntity implements IMultiblockController {

    protected final Comparator<IMultiblockPart> partComparator = Comparator.comparingLong(part -> {
        MetaTileEntity mte = (MetaTileEntity) part;
        return ((long) multiblockPartSorter().apply(mte.getPos()) << 32) | mte.getPos().hashCode();
    });

    private final Map<MultiblockAbility<Object>, List<Object>> multiblockAbilities = new HashMap<>();

    private final NavigableSet<IMultiblockPart> multiblockParts = new TreeSet<>(partComparator);

    protected EnumFacing upwardsFacing = EnumFacing.UP;
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
    @NotNull
    protected abstract IBlockPattern createStructurePattern();

    protected void createStructurePatterns() {
        structures.put("MAIN", createStructurePattern());
    }

    public EnumFacing getUpwardsFacing() {
        return upwardsFacing;
    }

    public void setUpwardsFacing(EnumFacing upwardsFacing) {
        if (!allowsExtendedFacing()) return;
        if (upwardsFacing == null || upwardsFacing.getAxis() == frontFacing.getAxis()) {
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
        return getSubstructure().getPatternState().isFlipped();
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

    public static TraceabilityPredicate tilePredicate(@NotNull BiPredicate<BlockWorldState, MetaTileEntity> predicate,
                                                      @Nullable Function<Map<String, String>, BlockInfo[]> candidates) {
        return new TraceabilityPredicate(worldState -> {
            TileEntity tileEntity = worldState.getTileEntity();
            if (!(tileEntity instanceof IGregTechTileEntity)) return PatternError.PLACEHOLDER;
            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
            return predicate.test(worldState, metaTileEntity) ? null : PatternError.PLACEHOLDER;
        }, candidates);
    }

    public static TraceabilityPredicate metaTileEntities(MetaTileEntity... metaTileEntities) {
        ResourceLocation[] ids = Arrays.stream(metaTileEntities).filter(Objects::nonNull)
                .map(tile -> tile.metaTileEntityId).toArray(ResourceLocation[]::new);
        return tilePredicate((state, tile) -> ArrayUtils.contains(ids, tile.metaTileEntityId),
                getCandidates(() -> EnumFacing.NORTH, metaTileEntities));
    }

    @SafeVarargs
    public static <
            T extends MetaTileEntity & ITieredMetaTileEntity> TraceabilityPredicate tieredMTEs(BiPredicate<Map<String, String>, T> pred,
                                                                                               T... metaTileEntities) {
        ResourceLocation[] ids = Arrays.stream(metaTileEntities).filter(Objects::nonNull)
                .map(tile -> tile.metaTileEntityId).toArray(ResourceLocation[]::new);
        return tilePredicate((state, tile) -> ArrayUtils.contains(ids, tile.metaTileEntityId),
                getCandidates(pred, metaTileEntities));
    }

    public static Function<Map<String, String>, BlockInfo[]> getCandidates(Supplier<EnumFacing> facing,
                                                                           MetaTileEntity... metaTileEntities) {
        return map -> Arrays.stream(metaTileEntities).filter(Objects::nonNull).map(tile -> {
            MetaTileEntityHolder holder = new MetaTileEntityHolder();
            holder.setMetaTileEntity(tile);
            holder.getMetaTileEntity().onPlacement();
            holder.getMetaTileEntity().setFrontFacing(facing.get());
            return new BlockInfo(tile.getBlock().getDefaultState(), holder);
        }).toArray(BlockInfo[]::new);
    }

    // generic hell
    @SafeVarargs
    public static <
            T extends MetaTileEntity & ITieredMetaTileEntity> Function<Map<String, String>, BlockInfo[]> getCandidates(BiPredicate<Map<String, String>, T> pred,
                                                                                                                       T... metaTileEntities) {
        return map -> Arrays.stream(metaTileEntities).filter(Objects::nonNull)
                .filter(i -> pred.test(map, i))
                .map(tile -> {
                    MetaTileEntityHolder holder = new MetaTileEntityHolder();
                    holder.setMetaTileEntity(tile);
                    holder.getMetaTileEntity().onPlacement();
                    holder.getMetaTileEntity().setFrontFacing(EnumFacing.SOUTH);
                    return new BlockInfo(tile.getBlock().getDefaultState(), holder);
                }).toArray(BlockInfo[]::new);
    }

    public static Function<Map<String, String>, BlockInfo[]> getCandidates(String key, IBlockState... allowedStates) {
        return map -> {
            if (map.containsKey(key)) {
                return new BlockInfo[] { new BlockInfo(allowedStates[MathHelper.clamp(GTUtility.parseInt(map.get(key)),
                        0, allowedStates.length - 1)]) };
            }
            return Arrays.stream(allowedStates).map(BlockInfo::new).toArray(BlockInfo[]::new);
        };
    }

    public static TraceabilityPredicate abilities(Supplier<EnumFacing> facing,
                                                  MultiblockAbility<?>... allowedAbilities) {
        return tilePredicate((state, tile) -> tile instanceof IMultiblockAbilityPart<?> &&
                ArrayUtils.contains(allowedAbilities, ((IMultiblockAbilityPart<?>) tile).getAbility()),
                getCandidates(facing, Arrays.stream(allowedAbilities)
                        .flatMap(ability -> MultiblockAbility.REGISTRY.get(ability).stream())
                        .toArray(MetaTileEntity[]::new)));
    }

    public static TraceabilityPredicate abilities(MultiblockAbility<?>... allowedAbilities) {
        return abilities(() -> EnumFacing.NORTH, allowedAbilities);
    }

    public static TraceabilityPredicate states(IBlockState... allowedStates) {
        return states(null, allowedStates);
    }

    public static TraceabilityPredicate states(String key, IBlockState... allowedStates) {
        return new TraceabilityPredicate(
                worldState -> ArrayUtils.contains(allowedStates, worldState.getBlockState()) ? null :
                        PatternError.PLACEHOLDER,
                getCandidates(key, allowedStates));
    }

    /**
     * Use this predicate for Frames in your Multiblock. Allows for Framed Pipes as well as normal Frame blocks.
     */
    public static TraceabilityPredicate frames(Material... frameMaterials) {
        return states(Arrays.stream(frameMaterials).map(m -> MetaBlocks.FRAMES.get(m).getBlock(m))
                .toArray(IBlockState[]::new))
                        .or(new TraceabilityPredicate(worldState -> {
                            TileEntity tileEntity = worldState.getTileEntity();
                            if (!(tileEntity instanceof IPipeTile<?, ?>pipeTile)) {
                                return PatternError.PLACEHOLDER;
                            }
                            return ArrayUtils.contains(frameMaterials, pipeTile.getFrameMaterial()) ? null :
                                    PatternError.PLACEHOLDER;
                        }));
    }

    public static TraceabilityPredicate blocks(Block... block) {
        return blocks(null, block);
    }

    public static TraceabilityPredicate blocks(String key, Block... block) {
        return new TraceabilityPredicate(
                worldState -> ArrayUtils.contains(block, worldState.getBlockState().getBlock()) ? null :
                        PatternError.PLACEHOLDER,
                getCandidates(key, Arrays.stream(block).map(Block::getDefaultState).toArray(IBlockState[]::new)));
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
     * they don't(or none match). Because this method sets an error in the structure, the pattern state will be
     * INVALID_CACHED,
     * so the pattern will not have its cache cleared, and the controller will not attempt to form the pattern again
     * unless the cache is invalidated(either through code or through it failing).
     * Example: {@code allSameType(GregTechAPI.HEATING_COILS, getSubstructure())}
     * 
     * @param info    The info, such as GregTechAPI.HEATING_COILS
     * @param pattern Pattern, used to get the cache. It will also be used to set the error.
     * @param error   The error, this is only set if the types don't match using
     *                {@link gregtech.api.pattern.PatternStringError}.
     */
    public static <V> V allSameType(Object2ObjectMap<IBlockState, V> info, IBlockPattern pattern, String error) {
        V type = null;
        for (Long2ObjectMap.Entry<BlockInfo> entry : pattern.getCache().long2ObjectEntrySet()) {
            V state = info.get(entry.getValue().getBlockState());
            if (state != null) {
                if (type != state) {
                    if (type == null) type = state;
                    else {
                        pattern.getPatternState()
                                .setError(new PatternStringError(BlockPos.fromLong(entry.getLongKey()), error));
                        return null;
                    }
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
            double rad = 0;
            if (frontFacing.getAxis() == EnumFacing.Axis.Y) {
                rad = -Math.PI / 2 * (upwardsFacing.getHorizontalIndex() - 2);
                if (frontFacing == EnumFacing.DOWN) rad += Math.PI;
            } else {
                EnumFacing rotated = EnumFacing.UP.rotateAround(frontFacing.getAxis());
                if (frontFacing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE)
                    rotated = rotated.getOpposite();

                if (upwardsFacing == EnumFacing.DOWN) rad = Math.PI;
                else if (upwardsFacing == rotated) rad = -Math.PI / 2;
                else if (upwardsFacing == rotated.getOpposite()) rad = Math.PI / 2;
            }

            translation.translate(0.5, 0.5, 0.5);
            translation.rotate(
                    new Rotation(rad, frontFacing.getXOffset(), frontFacing.getYOffset(), frontFacing.getZOffset()));
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
        if (!pattern.getPatternState().shouldUpdate() || getWorld() == null) return;

        long time = System.nanoTime();
        PatternState result = pattern.checkPatternFastAt(getWorld(), getPos(),
                getFrontFacing(), getUpwardsFacing(), allowsFlip());
        GTLog.logger.info(
                "structure check for " + getClass().getSimpleName() + " took " + (System.nanoTime() - time) + " nanos");

        if (result.getState().isValid()) {
            if (result.isFormed()) {
                // fast rebuild parts
                if (result.getState() == PatternState.EnumCheckState.VALID_UNCACHED) {
                    forEachMultiblockPart(name, part -> {
                        if (multiblockParts.contains(part)) return true;

                        if (part.isAttachedToMultiBlock() && !part.canPartShare(this, name)) {
                            invalidateStructure(name);
                            return false;
                        }
                        return true;
                    });

                    // add any new parts, because removal of parts is impossible
                    // it is possible for old parts to persist, so check that
                    List<IMultiblockAbilityPart<Object>> addedParts = new ArrayList<>();

                    forEachMultiblockPart(name, part -> {
                        if (multiblockParts.contains(part)) return true;

                        part.addToMultiBlock(this, name);
                        if (part instanceof IMultiblockAbilityPart<?>abilityPart) {
                            // noinspection unchecked
                            addedParts.add((IMultiblockAbilityPart<Object>) abilityPart);
                        }
                        return true;
                    });

                    // another bandaid fix
                    addedParts.sort(partComparator);

                    for (IMultiblockAbilityPart<Object> part : addedParts) {
                        registerMultiblockAbility(part);
                    }

                    formStructure(name);
                }
                return;
            }

            boolean[] valid = new boolean[1];
            valid[0] = true;

            forEachMultiblockPart(name, part -> {
                if (part.isAttachedToMultiBlock() && !part.canPartShare(this, name)) {
                    valid[0] = false;
                    return false;
                }
                return true;
            });

            // since the structure isn't formed, don't invalidate, instead just don't form it
            if (!valid[0]) return;

            forEachMultiblockPart(name, part -> {
                // parts *should* not have this controller added
                multiblockParts.add(part);
                part.addToMultiBlock(this, name);
                return true;
            });

            // maybe bandaid fix
            for (IMultiblockPart part : multiblockParts) {
                if (name.equals(part.getSubstructureName()) && part instanceof IMultiblockAbilityPart<?>abilityPart) {
                    // noinspection unchecked
                    registerMultiblockAbility((IMultiblockAbilityPart<Object>) abilityPart);
                }
            }

            formStructure(name);
        } else {
            if (result.isFormed()) {
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

    protected void forEachFormed(String name, BiConsumer<BlockInfo, GreggyBlockPos> action) {
        Long2ObjectMap<BlockInfo> cache = getSubstructure(name).getCache();
        GreggyBlockPos pos = new GreggyBlockPos();
        for (Long2ObjectMap.Entry<BlockInfo> entry : cache.long2ObjectEntrySet()) {
            action.accept(entry.getValue(), pos.fromLong(entry.getLongKey()));
        }
    }

    protected void formStructure(String name) {
        getSubstructure(name).getPatternState().setFormed(true);
        setFlipped(getSubstructure(name).getPatternState().isFlipped(), name);
        writeCustomData(STRUCTURE_FORMED, buf -> buf.writeString(name).writeBoolean(true));
    }

    protected void formStructure() {
        formStructure("MAIN");
    }

    public void invalidateStructure() {
        invalidateStructure("MAIN");
    }

    public void invalidateStructure(String name) {
        if (!getSubstructure(name).getPatternState().isFormed()) return;
        // i am sorry
        Object[] added = { null };
        List<Object> dummyList = new ArrayList<>() {

            @Override
            public boolean add(Object e) {
                added[0] = e;
                return true;
            }
        };

        multiblockParts.removeIf(part -> {
            if (name.equals(part.getSubstructureName())) {
                if (part instanceof IMultiblockAbilityPart<?>) {
                    // noinspection unchecked
                    IMultiblockAbilityPart<Object> ability = (IMultiblockAbilityPart<Object>) part;
                    added[0] = null;
                    ability.registerAbilities(dummyList);
                    if (added[0] != null) multiblockAbilities.get(ability.getAbility()).remove(added[0]);
                }
                part.removeFromMultiBlock(this);
                return true;
            }
            return false;
        });

        getSubstructure(name).getPatternState().setFormed(false);
        writeCustomData(STRUCTURE_FORMED, buf -> buf.writeString(name).writeBoolean(false));
    }

    protected void invalidStructureCaches() {
        for (IBlockPattern pattern : structures.values()) {
            pattern.clearCache();
        }
    }

    public IBlockPattern getSubstructure(String name) {
        return structures.get(name);
    }
    
    public IBlockPattern getSubstructure() {
        return getSubstructure("MAIN");
    }

    public String trySubstructure(String name) {
        if (structures.get(name) != null) return name;
        return "MAIN";
    }

    public Set<String> trySubstructure(Map<String, String> map) {
        // maybe lang?
        Set<String> set = new HashSet<>();
        for (String key : map.keySet()) {
            if (key.startsWith("substructure")) set.add(trySubstructure(map.get(key)));
        }
        if (set.isEmpty()) set.add("MAIN");
        return set;
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

    public NavigableSet<IMultiblockPart> getMultiblockParts() {
        return Collections.unmodifiableNavigableSet(multiblockParts);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("UpwardsFacing")) {
            this.upwardsFacing = EnumFacing.VALUES[data.getByte("UpwardsFacing")];
            // old up facing is absolute when front facing is up/down
            if (frontFacing.getAxis() != EnumFacing.Axis.Y) {
                this.upwardsFacing = switch (upwardsFacing) {
                    case NORTH -> EnumFacing.UP;
                    case SOUTH -> EnumFacing.DOWN;
                    case EAST -> frontFacing.rotateYCCW();
                    default -> frontFacing.rotateY();
                };
            }
        } else if (data.hasKey("UpFacing")) {
            this.upwardsFacing = EnumFacing.VALUES[data.getByte("UpFacing")];
        }
        this.reinitializeStructurePattern();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setByte("UpFacing", (byte) upwardsFacing.getIndex());
        return data;
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(upwardsFacing.getIndex());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.upwardsFacing = EnumFacing.VALUES[buf.readByte()];
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_UPWARDS_FACING) {
            this.upwardsFacing = EnumFacing.VALUES[buf.readByte()];
            scheduleRenderUpdate();
        } else if (dataId == STRUCTURE_FORMED) {
            String name = buf.readString(Short.MAX_VALUE);
            getSubstructure(name).getPatternState().setFormed(buf.readBoolean());

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
        return isStructureFormed();
    }

    public boolean isStructureFormed(String name) {
        return getWorld() != null && getSubstructure(name) != null &&
                getSubstructure(name).getPatternState().isFormed();
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
            MultiblockPreviewRenderer.renderMultiBlockPreview(this, playerIn, 60000);
            return true;
        }
        return false;
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing wrenchSide,
                                 CuboidRayTraceResult hitResult) {
        if (wrenchSide == getFrontFacing() && allowsExtendedFacing()) {
            if (!getWorld().isRemote) {
                EnumFacing rot = upwardsFacing.rotateAround(getFrontFacing().getAxis());
                if (frontFacing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ^ playerIn.isSneaking()) {
                    rot = rot.getOpposite();
                }

                setUpwardsFacing(rot);
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
     * Called in either JEI or in-world previews to specify what maps they should be autobuilt with.
     * Default impl returns a singleton iterator with {@code null}.
     * 
     * @return An iterator, you can return the same map but mutated. Iterator can be empty but why would you.
     */
    @NotNull
    public Iterator<Map<String, String>> getPreviewBuilds() {
        return new AbstractIterator<>() {

            private boolean used;

            @Override
            protected Map<String, String> computeNext() {
                if (!used) {
                    used = true;
                    return Collections.emptyMap();
                }
                return endOfData();
            }
        };
    }

    /**
     * Autobuilds the multiblock, using the {@code substructure} string to select the substructure, or the main
     * structure if invalid.
     */
    public void autoBuild(EntityPlayer player, Map<String, String> map, String substructure) {
        if (getWorld().isRemote) throw new IllegalArgumentException("client side call wuh");

        IBlockPattern structure = getSubstructure(trySubstructure(substructure));

        Long2ObjectMap<TraceabilityPredicate> predicates = structure.getDefaultShape(this, map);
        if (predicates == null) return;

        autoBuild(player, map, predicates);
    }

    /**
     * Autobuild the multiblock, this is like {@link MultiblockControllerBase#autoBuild(EntityPlayer, Map, String)} but
     * if
     * you have the predicate map for other uses. This does mutate the map passed in.
     */
    public void autoBuild(EntityPlayer player, Map<String, String> map,
                          Long2ObjectMap<TraceabilityPredicate> predicates) {
        if (getWorld().isRemote) throw new IllegalArgumentException("client side call wuh");
        modifyAutoBuild(map);
        // for each symbol, which simple predicate is being used
        // this advances whenever a minimum has been satisfied(if any), or a maximum has been reached(if any)
        // preview counts are treated as exactly that many
        Object2IntMap<TraceabilityPredicate> simpleIndex = new Object2IntOpenHashMap<>();
        Object2IntMap<TraceabilityPredicate.SimplePredicate> globalCache = new Object2IntOpenHashMap<>();
        Map<TraceabilityPredicate.SimplePredicate, BlockInfo> cache = new HashMap<>();

        BiPredicate<Long, BlockInfo> place = (l, info) -> {
            BlockPos pos = BlockPos.fromLong(l);

            // don't stop build if its air
            if (!getWorld().isAirBlock(pos)) return true;

            if (info.getTileEntity() instanceof MetaTileEntityHolder holder) {
                ItemStack removed = hasAndRemoveItem(player, holder.getMetaTileEntity().getStackForm());
                if (removed.isEmpty()) return false;

                MetaTileEntityHolder newHolder = new MetaTileEntityHolder();
                newHolder.setMetaTileEntity(holder.getMetaTileEntity());
                newHolder.getMetaTileEntity().onPlacement();
                newHolder.getMetaTileEntity().setFrontFacing(holder.getMetaTileEntity().getFrontFacing());
                if (removed.hasTagCompound())
                    newHolder.getMetaTileEntity().initFromItemStackData(removed.getTagCompound());

                if (predicates.containsKey(pos.offset(newHolder.getMetaTileEntity().getFrontFacing()).toLong())) {
                    EnumFacing valid = null;
                    for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                        if (!predicates.containsKey(pos.offset(facing).toLong())) {
                            valid = facing;
                            break;
                        }
                    }
                    if (valid != null) newHolder.getMetaTileEntity().setFrontFacing(valid);
                    else {
                        if (!predicates.containsKey(pos.offset(EnumFacing.UP).toLong())) {
                            newHolder.getMetaTileEntity().setFrontFacing(EnumFacing.UP);
                        } else if (!predicates.containsKey(pos.offset(EnumFacing.DOWN).toLong())) {
                            newHolder.getMetaTileEntity().setFrontFacing(EnumFacing.DOWN);
                        }
                    }
                }

                getWorld().setBlockState(pos, holder.getMetaTileEntity().getBlock().getDefaultState());
                getWorld().setTileEntity(pos, newHolder);
            } else {
                if (!hasAndRemoveItem(player, GTUtility.toItem(info.getBlockState())).isEmpty())
                    getWorld().setBlockState(pos, info.getBlockState());
                else return false;
            }

            return true;
        };

        for (Long2ObjectMap.Entry<TraceabilityPredicate> entry : predicates.long2ObjectEntrySet()) {
            TraceabilityPredicate pred = entry.getValue();
            if (simpleIndex.getInt(pred) >= pred.simple.size()) continue;

            int pointer = simpleIndex.getInt(pred);
            TraceabilityPredicate.SimplePredicate simple = pred.simple.get(pointer);
            int count = globalCache.getInt(simple);

            try {
                while ((simple.previewCount == -1 || count == simple.previewCount) &&
                        (simple.minGlobalCount == -1 || count == simple.minGlobalCount)) {
                    // if the current predicate is used, move until the next free one
                    pointer++;
                    simple = pred.simple.get(pointer);
                    count = globalCache.getInt(simple);
                }
                simpleIndex.put(pred, pointer);
            } catch (IndexOutOfBoundsException e) {
                continue;
            }

            globalCache.put(simple, globalCache.getInt(simple) + 1);

            if (simple.candidates == null) continue;

            TraceabilityPredicate.SimplePredicate finalSimple = simple;
            cache.computeIfAbsent(simple, k -> finalSimple.candidates.apply(map)[0]);

            if (!place.test(entry.getLongKey(), cache.get(simple))) return;

            entry.setValue(null);
        }

        simpleIndex.clear();

        for (Long2ObjectMap.Entry<TraceabilityPredicate> entry : predicates.long2ObjectEntrySet()) {
            TraceabilityPredicate pred = entry.getValue();
            if (pred == null || simpleIndex.getInt(pred) >= pred.simple.size()) continue;

            TraceabilityPredicate.SimplePredicate simple = pred.simple.get(simpleIndex.getInt(pred));
            int count = globalCache.getInt(simple);

            while (count == simple.previewCount || count == simple.maxGlobalCount) {
                // if the current predicate is used, move until the next free one
                int newIndex = simpleIndex.put(pred, simpleIndex.getInt(pred) + 1) + 1;
                if (newIndex >= pred.simple.size()) {
                    GTLog.logger.warn("Failed to generate default structure pattern.",
                            new Throwable());
                    return;
                }
                simple = pred.simple.get(newIndex);
                count = globalCache.getInt(simple);
            }
            globalCache.put(simple, globalCache.getInt(simple) + 1);

            if (simple.candidates == null) continue;

            TraceabilityPredicate.SimplePredicate finalSimple = simple;
            cache.computeIfAbsent(simple, k -> finalSimple.candidates.apply(map)[0]);

            if (!place.test(entry.getLongKey(), cache.get(simple))) return;
        }
    }

    /**
     * Called right before the autobuild code starts, modify the map like if you want it to be "height"
     * instead of "multi.1.0"
     */
    protected void modifyAutoBuild(Map<String, String> map) {}

    /**
     * @return The item stack that is removed from the player's inventory(or AE system, satchels, etc).
     *         If the player is in creative mode, return a copy of the input stack.
     *         Currently only removes from the player's main inventory. The count of the passed in stack does not
     *         matter, only 1 is removed from the player.
     */
    protected static ItemStack hasAndRemoveItem(EntityPlayer player, ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (player.isCreative()) return stack.copy();

        for (ItemStack ztack : player.inventory.mainInventory) {
            if (!ztack.isEmpty() && ztack.isItemEqual(stack)) {
                ztack.setCount(ztack.getCount() - 1);
                return ztack.copy();
            }
        }

        return ItemStack.EMPTY;
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
