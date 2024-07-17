package gregtech.api.pattern;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTLog;
import gregtech.api.util.RelativeDirection;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockPattern {

    /**
     * In the form of [ aisleDir, stringDir, charDir ]
     */
    public final RelativeDirection[] structureDir;

    /**
     * In the form of [ num aisles, num string per aisle, num char per string ]
     */
    protected final int[] dimensions;

    /**
     * In the form of { pair 1 -> amount, pair 2 -> amount, pair 3 -> amount }.
     * Each pair is relative directions, such as FRONT, BACK, or RIGHT, LEFT.
     * The amount is the offset in the first of each pair, can be negative.
     * {@link RelativeDirection} of structure directions, stored as ordinals
     */
    protected final int[] startOffset;

    /**
     * True if startOffset was passed in, false if it was null and automatically detected
     */
    protected final boolean hasStartOffset;
    protected final PatternAisle[] aisles;
    protected final Char2ObjectMap<TraceabilityPredicate> predicates;
    protected final StructureInfo info;
    protected final BlockWorldState worldState;
    protected final PatternMatchContext matchContext = new PatternMatchContext();
    protected final Object2IntMap<TraceabilityPredicate.SimplePredicate> globalCount = new Object2IntOpenHashMap<>();
    protected final Object2IntMap<TraceabilityPredicate.SimplePredicate> layerCount = new Object2IntOpenHashMap<>();

    public Long2ObjectMap<BlockInfo> cache = new Long2ObjectOpenHashMap<>();

    /**
     * The repetitions per aisle along the axis of repetition
     */
    public int[] formedRepetitionCount;

    // how many not nulls to keep someone from not passing in null?
    public BlockPattern(@NotNull PatternAisle @NotNull [] aisles, int @NotNull [] dimensions,
                        @NotNull RelativeDirection @NotNull [] directions,
                        int @Nullable [] startOffset, @NotNull Char2ObjectMap<TraceabilityPredicate> predicates,
                        char centerChar) {
        this.aisles = aisles;
        this.dimensions = dimensions;
        this.structureDir = directions;
        this.predicates = predicates;
        hasStartOffset = startOffset != null;

        if (startOffset == null) {
            this.startOffset = new int[3];
            legacyStartOffset(centerChar);
        } else {
            this.startOffset = startOffset;
        }

        this.info = new StructureInfo(matchContext, null);
        this.worldState = new BlockWorldState(info);
    }

    /**
     * For legacy compat only,
     * 
     * @param center The center char to look for
     */
    private void legacyStartOffset(char center) {
        // don't do anything if center char isn't specified, this allows
        // MultiblockControllerBase#validateStructurePatterns to do its thing while not logging an error here
        if (center == 0) return;
        // could also use aisles.length but this is cooler
        for (int aisleI = 0; aisleI < dimensions[0]; aisleI++) {
            int[] result = aisles[aisleI].firstInstanceOf(center);
            if (result != null) {
                // when legacyStartOffset() is called, aisles have been reversed, so don't reverse it manually here
                // the scuffed ternary is so that if the structure dir is the first thing, then don't reverse it
                startOffset[0] = aisleI * (structureDir[0] == RelativeDirection.VALUES[0] ? 1 : -1);
                startOffset[1] = (dimensions[1] - 1 - result[0]) * (structureDir[1] == RelativeDirection.VALUES[2] ? 1 : -1);
                startOffset[2] = (dimensions[2] - 1 - result[1]) * (structureDir[2] == RelativeDirection.VALUES[4] ? 1 : -1);
                return;
            }
        }

        throw new IllegalStateException("Failed to find center char: '" + center + "'");
    }

    public PatternError getError() {
        return info.getError();
    }

    public PatternMatchContext checkPatternFastAt(World world, BlockPos centerPos, EnumFacing frontFacing,
                                                  EnumFacing upwardsFacing, boolean allowsFlip) {
        if (!cache.isEmpty()) {
            boolean pass = true;
            GreggyBlockPos gregPos = new GreggyBlockPos();
            for (Long2ObjectMap.Entry<BlockInfo> entry : cache.long2ObjectEntrySet()) {
                BlockPos pos = gregPos.fromLong(entry.getLongKey()).immutable();
                IBlockState blockState = world.getBlockState(pos);

                if (blockState != entry.getValue().getBlockState()) {
                    pass = false;
                    break;
                }

                TileEntity cachedTileEntity = entry.getValue().getTileEntity();

                if (cachedTileEntity != null) {
                    TileEntity tileEntity = world.getTileEntity(pos);
                    if (tileEntity != cachedTileEntity) {
                        pass = false;
                        break;
                    }
                }
            }
            if (pass) return info.hasError() ? null : matchContext;
        }

        // First try normal pattern, and if it fails, try flipped (if allowed).
        boolean valid = checkPatternAt(world, centerPos, frontFacing, upwardsFacing, false);
        if (valid) return matchContext;

        if (allowsFlip) {
            valid = checkPatternAt(world, centerPos, frontFacing, upwardsFacing, true);
        }
        if (!valid) clearCache(); // we don't want a random cache of a partially formed multi
        return null;
    }

    public void clearCache() {
        cache.clear();
    }

    private boolean checkPatternAt(World world, BlockPos centerPos, EnumFacing frontFacing,
                                   EnumFacing upwardsFacing, boolean isFlipped) {
        this.matchContext.reset();
        this.globalCount.clear();
        this.layerCount.clear();
        cache.clear();

        worldState.world = world;

        int aisleOffset = -1;
        GreggyBlockPos controllerPos = new GreggyBlockPos(centerPos);

        for (int aisleI = 0; aisleI < aisles.length; aisleI++) {
            PatternAisle aisle = aisles[aisleI];
            // if this doesn't get set in the inner loop, then it means all repeats passed
            int actualRepeats = aisle.maxRepeats;

            for (int repeats = aisle.minRepeats; repeats <= aisle.maxRepeats; repeats++) {
                boolean aisleResult = checkAisle(controllerPos, frontFacing, upwardsFacing, aisleI,
                        aisleOffset + repeats, isFlipped);

                // greedy search, tries to make the current aisle repeat as much as possible
                if (!aisleResult) {
                    // if the min repetition is invalid then the whole pattern is invalid
                    if (repeats == aisle.minRepeats) {
                        return false;
                    }
                    // otherwise this is the max repeats
                    actualRepeats = repeats - 1;
                }
            }

            aisleOffset += actualRepeats;
        }

        // global minimum checks
        for (Object2IntMap.Entry<TraceabilityPredicate.SimplePredicate> entry : globalCount.object2IntEntrySet()) {
            if (entry.getIntValue() < entry.getKey().minGlobalCount) {
                info.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 1));
                return false;
            }
        }

        info.setError(null);
        matchContext.setNeededFlip(isFlipped);
        return true;
    }

    /**
     * Checks a specific aisle for validity
     * 
     * @param controllerPos The position of the controller
     * @param frontFacing   The front facing of the controller
     * @param upFacing      The up facing of the controller
     * @param aisleIndex    The index of the aisle, this is where the pattern is gotten from, treats repeatable aisles
     *                      as only 1
     * @param aisleOffset   The offset of the aisle, how much offset in aisleDir to check the blocks in world, for
     *                      example, if the first aisle is repeated 2 times, aisleIndex is 1 while this is 2
     * @param flip          Whether to flip or not
     * @return True if the check passed, otherwise the {@link StructureInfo} would have been updated with an error
     */
    public boolean checkAisle(GreggyBlockPos controllerPos, EnumFacing frontFacing, EnumFacing upFacing, int aisleIndex,
                              int aisleOffset, boolean flip) {
        // absolute facings from the relative facings
        EnumFacing absoluteAisle = structureDir[0].getRelativeFacing(frontFacing, upFacing, flip);
        EnumFacing absoluteString = structureDir[1].getRelativeFacing(frontFacing, upFacing, flip);
        EnumFacing absoluteChar = structureDir[2].getRelativeFacing(frontFacing, upFacing, flip);

        // where the aisle would start in world
        GreggyBlockPos aisleStart = startPos(controllerPos, frontFacing, upFacing, flip)
                .offset(absoluteAisle, aisleOffset);
        // where the current string would start in world
        GreggyBlockPos stringStart = aisleStart.copy();
        // where the char being checked is
        GreggyBlockPos charPos = aisleStart.copy();
        PatternAisle aisle = aisles[aisleIndex];

        layerCount.clear();

        for (int stringI = 0; stringI < dimensions[1]; stringI++) {
            for (int charI = 0; charI < dimensions[2]; charI++) {
                worldState.setPos(charPos);
                TraceabilityPredicate predicate = predicates.get(aisle.charAt(stringI, charI));

                if (predicate != TraceabilityPredicate.ANY) {
                    TileEntity te = worldState.getTileEntity();
                    cache.put(charPos.toLong(), new BlockInfo(worldState.getBlockState(),
                            !(te instanceof IGregTechTileEntity gtTe) || gtTe.isValid() ? te : null, predicate));
                }

                GTLog.logger.info("Checked pos at " + charPos + " with flip " + flip);

                boolean result = predicate.test(worldState, info, globalCount, layerCount);
                if (!result) return false;

                charPos.offset(absoluteChar);
            }

            // offset the string start once after every string
            stringStart.offset(absoluteString);
            charPos.from(stringStart);

            // layer minimum checks
            for (Object2IntMap.Entry<TraceabilityPredicate.SimplePredicate> entry : layerCount.object2IntEntrySet()) {
                if (entry.getIntValue() < entry.getKey().minLayerCount) {
                    info.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 3));
                    return false;
                }
            }
        }
        return true;
    }

    public void autoBuild(EntityPlayer player, MultiblockControllerBase controllerBase) {
        // World world = player.world;
        // BlockWorldState worldState = new BlockWorldState();
        // int minZ = -centerOffset[4];
        // EnumFacing facing = controllerBase.getFrontFacing().getOpposite();
        // BlockPos centerPos = controllerBase.getPos();
        // Map<TraceabilityPredicate.SimplePredicate, BlockInfo[]> cacheInfos = new HashMap<>();
        // Map<TraceabilityPredicate.SimplePredicate, Integer> cacheGlobal = new HashMap<>();
        // Map<BlockPos, Object> blocks = new HashMap<>();
        // blocks.put(controllerBase.getPos(), controllerBase);
        // for (int c = 0, z = minZ++, r; c < this.fingerLength; c++) {
        // for (r = 0; r < aisleRepetitions[c][0]; r++) {
        // Map<TraceabilityPredicate.SimplePredicate, Integer> cacheLayer = new HashMap<>();
        // for (int b = 0, y = -centerOffset[1]; b < this.thumbLength; b++, y++) {
        // for (int a = 0, x = -centerOffset[0]; a < this.palmLength; a++, x++) {
        // TraceabilityPredicate predicate = this.blockMatches[c][b][a];
        // BlockPos pos = setActualRelativeOffset(x, y, z, facing, controllerBase.getUpwardsFacing(),
        // controllerBase.isFlipped())
        // .add(centerPos.getX(), centerPos.getY(), centerPos.getZ());
        // worldState.update(world, pos, matchContext, globalCount, layerCount, predicate);
        // if (!world.getBlockState(pos).getMaterial().isReplaceable()) {
        // blocks.put(pos, world.getBlockState(pos));
        // for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
        // limit.testLimited(worldState);
        // }
        // } else {
        // boolean find = false;
        // BlockInfo[] infos = new BlockInfo[0];
        // for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
        // if (limit.minLayerCount > 0) {
        // if (!cacheLayer.containsKey(limit)) {
        // cacheLayer.put(limit, 1);
        // } else
        // if (cacheLayer.get(limit) < limit.minLayerCount && (limit.maxLayerCount == -1 ||
        // cacheLayer.get(limit) < limit.maxLayerCount)) {
        // cacheLayer.put(limit, cacheLayer.get(limit) + 1);
        // } else {
        // continue;
        // }
        // } else {
        // continue;
        // }
        // if (!cacheInfos.containsKey(limit)) {
        // cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
        // }
        // infos = cacheInfos.get(limit);
        // find = true;
        // break;
        // }
        // if (!find) {
        // for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
        // if (limit.minGlobalCount > 0) {
        // if (!cacheGlobal.containsKey(limit)) {
        // cacheGlobal.put(limit, 1);
        // } else if (cacheGlobal.get(limit) < limit.minGlobalCount &&
        // (limit.maxGlobalCount == -1 ||
        // cacheGlobal.get(limit) < limit.maxGlobalCount)) {
        // cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
        // } else {
        // continue;
        // }
        // } else {
        // continue;
        // }
        // if (!cacheInfos.containsKey(limit)) {
        // cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
        // }
        // infos = cacheInfos.get(limit);
        // find = true;
        // break;
        // }
        // }
        // if (!find) { // no limited
        // for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
        // if (limit.maxLayerCount != -1 &&
        // cacheLayer.getOrDefault(limit, Integer.MAX_VALUE) == limit.maxLayerCount)
        // continue;
        // if (limit.maxGlobalCount != -1 &&
        // cacheGlobal.getOrDefault(limit, Integer.MAX_VALUE) == limit.maxGlobalCount)
        // continue;
        // if (!cacheInfos.containsKey(limit)) {
        // cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
        // }
        // if (cacheLayer.containsKey(limit)) {
        // cacheLayer.put(limit, cacheLayer.get(limit) + 1);
        // } else {
        // cacheLayer.put(limit, 1);
        // }
        // if (cacheGlobal.containsKey(limit)) {
        // cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
        // } else {
        // cacheGlobal.put(limit, 1);
        // }
        // infos = ArrayUtils.addAll(infos, cacheInfos.get(limit));
        // }
        // for (TraceabilityPredicate.SimplePredicate common : predicate.common) {
        // if (!cacheInfos.containsKey(common)) {
        // cacheInfos.put(common,
        // common.candidates == null ? null : common.candidates.get());
        // }
        // infos = ArrayUtils.addAll(infos, cacheInfos.get(common));
        // }
        // }
        //
        // List<ItemStack> candidates = Arrays.stream(infos)
        // .filter(info -> info.getBlockState().getBlock() != Blocks.AIR).map(info -> {
        // IBlockState blockState = info.getBlockState();
        // MetaTileEntity metaTileEntity = info
        // .getTileEntity() instanceof IGregTechTileEntity ?
        // ((IGregTechTileEntity) info.getTileEntity())
        // .getMetaTileEntity() :
        // null;
        // if (metaTileEntity != null) {
        // return metaTileEntity.getStackForm();
        // } else {
        // return new ItemStack(Item.getItemFromBlock(blockState.getBlock()), 1,
        // blockState.getBlock().damageDropped(blockState));
        // }
        // }).collect(Collectors.toList());
        // if (candidates.isEmpty()) continue;
        // // check inventory
        // ItemStack found = null;
        // if (!player.isCreative()) {
        // for (ItemStack itemStack : player.inventory.mainInventory) {
        // if (candidates.stream().anyMatch(candidate -> candidate.isItemEqual(itemStack)) &&
        // !itemStack.isEmpty() && itemStack.getItem() instanceof ItemBlock) {
        // found = itemStack.copy();
        // itemStack.setCount(itemStack.getCount() - 1);
        // break;
        // }
        // }
        // if (found == null) continue;
        // } else {
        // for (int i = candidates.size() - 1; i >= 0; i--) {
        // found = candidates.get(i).copy();
        // if (!found.isEmpty() && found.getItem() instanceof ItemBlock) {
        // break;
        // }
        // found = null;
        // }
        // if (found == null) continue;
        // }
        // ItemBlock itemBlock = (ItemBlock) found.getItem();
        // IBlockState state = itemBlock.getBlock()
        // .getStateFromMeta(itemBlock.getMetadata(found.getMetadata()));
        // blocks.put(pos, state);
        // world.setBlockState(pos, state);
        // TileEntity holder = world.getTileEntity(pos);
        // if (holder instanceof IGregTechTileEntity igtte) {
        // MTERegistry registry = GregTechAPI.mteManager
        // .getRegistry(found.getItem().getRegistryName().getNamespace());
        // MetaTileEntity sampleMetaTileEntity = registry.getObjectById(found.getItemDamage());
        // if (sampleMetaTileEntity != null) {
        // MetaTileEntity metaTileEntity = igtte.setMetaTileEntity(sampleMetaTileEntity);
        // metaTileEntity.onPlacement();
        // blocks.put(pos, metaTileEntity);
        // if (found.getTagCompound() != null) {
        // metaTileEntity.initFromItemStackData(found.getTagCompound());
        // }
        // }
        // }
        // }
        // }
        // }
        // z++;
        // }
        // }
        // EnumFacing[] facings = ArrayUtils.addAll(new EnumFacing[] { controllerBase.getFrontFacing() }, FACINGS); //
        // follow
        // // controller
        // // first
        // blocks.forEach((pos, block) -> { // adjust facing
        // if (block instanceof MetaTileEntity) {
        // MetaTileEntity metaTileEntity = (MetaTileEntity) block;
        // boolean find = false;
        // for (EnumFacing enumFacing : facings) {
        // if (metaTileEntity.isValidFrontFacing(enumFacing)) {
        // if (!blocks.containsKey(pos.offset(enumFacing))) {
        // metaTileEntity.setFrontFacing(enumFacing);
        // find = true;
        // break;
        // }
        // }
        // }
        // if (!find) {
        // for (EnumFacing enumFacing : FACINGS) {
        // if (world.isAirBlock(pos.offset(enumFacing)) && metaTileEntity.isValidFrontFacing(enumFacing)) {
        // metaTileEntity.setFrontFacing(enumFacing);
        // break;
        // }
        // }
        // }
        // }
        // });
    }

    public PreviewBlockPattern getDefaultShape() {
        char[][][] pattern = new char[dimensions[2]][dimensions[1]][dimensions[0]];

        for (PatternAisle aisle : aisles) {
            char[][] resultAisle = new char[dimensions[2]][dimensions[1]];

            for (String str : aisle.pattern) {
                for (char c : str.toCharArray()) {
                    // TraceabilityPredicate predicate =
                }
            }
        }

        return null;
    }

    public boolean hasStartOffset() {
        return hasStartOffset;
    }

    private GreggyBlockPos startPos(GreggyBlockPos controllerPos, EnumFacing frontFacing, EnumFacing upFacing,
                                    boolean flip) {
        GreggyBlockPos start = controllerPos.copy();
        for (int i = 0; i < 3; i++) {
            start.offset(RelativeDirection.VALUES[2 * i].getRelativeFacing(frontFacing, upFacing, flip), startOffset[i]);
        }
        return start;
    }

    /**
     * Moves the start offset in the given direction and amount, use {@link BlockPattern#clearCache()} after to prevent the cache from being stuck in the old offset.
     * @param dir The direction, relative to controller.
     * @param amount The amount to offset.
     */
    public void moveStartOffset(RelativeDirection dir, int amount) {
        // reverse amount if its in the opposite direction
        amount *= (dir.ordinal() % 2 == 0) ? 1 : -1;
        startOffset[dir.ordinal() / 2] += amount;
    }

    /**
     * Gets the start offset. You probably should use {@link BlockPattern#moveStartOffset(RelativeDirection, int)} instead of mutating the result, but I can't stop you.
     * @return The start offset.
     */
    public int[] getStartOffset() {
        return startOffset;
    }

    /**
     * Get the start offset in the given direction.
     * @param dir The direction, relative to controller.
     * @return The amount, can be negative.
     */
    public int getStartOffset(RelativeDirection dir) {
        return startOffset[dir.ordinal() / 2] * (dir.ordinal() % 2 == 0 ? 1 : -1);
    }
}
