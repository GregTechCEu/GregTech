package gregtech.api.pattern.pattern;

import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.GreggyBlockPos;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.OriginOffset;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTLog;
import gregtech.api.util.RelativeDirection;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMaps;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2CharMap;
import it.unimi.dsi.fastutil.objects.Object2CharOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlockPattern implements IBlockPattern {

    /**
     * In the form of [ aisleDir, stringDir, charDir ]
     */
    protected final RelativeDirection[] directions;

    /**
     * In the form of [ num aisles, num string per aisle, num char per string ]
     */
    protected final int[] dimensions;
    protected final OriginOffset offset;

    /**
     * True if startOffset was passed in, false if it was null and automatically detected
     */
    protected final boolean hasStartOffset;
    protected final PatternAisle[] aisles;
    protected final AisleStrategy aisleStrategy;
    protected final Char2ObjectMap<TraceabilityPredicate> predicates;
    protected final BlockWorldState worldState;
    protected final Object2IntMap<TraceabilityPredicate.SimplePredicate> globalCount = new Object2IntOpenHashMap<>();
    protected final Object2IntMap<TraceabilityPredicate.SimplePredicate> layerCount = new Object2IntOpenHashMap<>();
    protected final PatternState state = new PatternState();
    protected final Long2ObjectMap<BlockInfo> cache = new Long2ObjectOpenHashMap<>();

    // how many not nulls to keep someone from not passing in null?
    public BlockPattern(@NotNull PatternAisle @NotNull [] aisles,
                        @NotNull AisleStrategy aisleStrategy,
                        int @NotNull [] dimensions,
                        @NotNull RelativeDirection @NotNull [] directions,
                        @Nullable OriginOffset offset,
                        @NotNull Char2ObjectMap<@NotNull TraceabilityPredicate> predicates,
                        char centerChar) {
        this.aisles = aisles;
        this.aisleStrategy = aisleStrategy;
        this.dimensions = dimensions;
        this.directions = directions;
        this.predicates = predicates;
        hasStartOffset = offset != null;

        if (offset == null) {
            this.offset = new OriginOffset();
            legacyStartOffset(centerChar);
        } else {
            this.offset = offset;
        }

        this.worldState = new BlockWorldState();
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
                // structure starts at aisle 0, string 0, char 0, think about it
                // so relative to the controller we need to offset by this to get to the start
                moveOffset(directions[0], -aisleI);
                moveOffset(directions[1], -result[0]);
                moveOffset(directions[2], -result[1]);
                return;
            }
        }

        throw new IllegalStateException("Failed to find center char: '" + center + "'");
    }

    @NotNull
    @Override
    public PatternState checkPatternFastAt(World world, BlockPos centerPos, EnumFacing frontFacing,
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
            if (pass) {
                if (state.hasError()) {
                    state.setState(PatternState.EnumCheckState.INVALID_CACHED);
                } else {
                    state.setState(PatternState.EnumCheckState.VALID_CACHED);
                }

                return state;
            }
        }

        // First try normal pattern, and if it fails, try flipped (if allowed).
        boolean valid = checkPatternAt(world, centerPos, frontFacing, upwardsFacing, false);
        if (valid) {
            // reaching here means the cache failed/empty
            state.setState(PatternState.EnumCheckState.VALID_UNCACHED);
            state.setFlipped(false);
            return state;
        }

        if (allowsFlip) {
            valid = checkPatternAt(world, centerPos, frontFacing, upwardsFacing, true);
        }
        if (!valid) { // we don't want a random cache of a partially formed multi
            clearCache();
            state.setState(PatternState.EnumCheckState.INVALID_UNCACHED);
            return state;
        }

        state.setState(PatternState.EnumCheckState.VALID_UNCACHED);
        state.setFlipped(true);
        return state;
    }

    @Override
    public Long2ObjectMap<BlockInfo> getCache() {
        return cache;
    }

    @Override
    public boolean checkPatternAt(World world, BlockPos centerPos, EnumFacing frontFacing,
                                  EnumFacing upwardsFacing, boolean isFlipped) {
        // todo still need to rework this to have a temporary global cache
        this.globalCount.clear();
        this.layerCount.clear();
        cache.clear();

        worldState.setWorld(world);

        GreggyBlockPos controllerPos = new GreggyBlockPos(centerPos);

        aisleStrategy.pattern = this;
        aisleStrategy.start(controllerPos, frontFacing, upwardsFacing);
        if (!aisleStrategy.check(isFlipped)) return false;

        // for (int aisleI = 0; aisleI < aisles.length; aisleI++) {
        // PatternAisle aisle = aisles[aisleI];
        //
        // // check everything below min repeats to ensure its valid
        // // don't check aisle.minRepeats itself since its checked below
        // for (int repeats = 1; repeats < aisle.minRepeats; repeats++) {
        // boolean aisleResult = checkAisle(controllerPos, frontFacing, upwardsFacing, aisleI,
        // aisleOffset + repeats, isFlipped);
        // if (!aisleResult) return false;
        // }
        //
        // // if this doesn't get set in the inner loop, then it means all repeats passed
        // int actualRepeats = aisle.maxRepeats;
        //
        // for (int repeats = aisle.minRepeats; repeats <= aisle.maxRepeats; repeats++) {
        // boolean aisleResult = checkAisle(controllerPos, frontFacing, upwardsFacing, aisleI,
        // aisleOffset + repeats, isFlipped);
        //
        // // greedy search, tries to make the current aisle repeat as much as possible
        // if (!aisleResult) {
        // // if the min repetition is invalid then the whole pattern is invalid
        // if (repeats == aisle.minRepeats) {
        // return false;
        // }
        // // otherwise this is the max repeats
        // actualRepeats = repeats - 1;
        // break;
        // }
        // }
        //
        // aisle.setActualRepeats(actualRepeats);
        // aisleOffset += actualRepeats;
        // }

        // global minimum checks
        for (Object2IntMap.Entry<TraceabilityPredicate.SimplePredicate> entry : globalCount.object2IntEntrySet()) {
            if (entry.getIntValue() < entry.getKey().minGlobalCount) {
                state.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 1));
                return false;
            }
        }

        state.setError(null);
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
     * @return True if the check passed
     */
    public boolean checkAisle(GreggyBlockPos controllerPos, EnumFacing frontFacing, EnumFacing upFacing, int aisleIndex,
                              int aisleOffset, boolean flip) {
        // absolute facings from the relative facings
        EnumFacing absoluteAisle = directions[0].getRelativeFacing(frontFacing, upFacing, flip);
        EnumFacing absoluteString = directions[1].getRelativeFacing(frontFacing, upFacing, flip);
        EnumFacing absoluteChar = directions[2].getRelativeFacing(frontFacing, upFacing, flip);

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
                    // todo it caused an exception twice but never reproduced, maybe figure out or just remove
                    try {
                        cache.put(charPos.toLong(), new BlockInfo(worldState.getBlockState(),
                                !(te instanceof IGregTechTileEntity gtTe) || gtTe.isValid() ? te : null, predicate));
                    } catch (IllegalArgumentException e) {
                        GTLog.logger.error("bruh");
                        throw e;
                    }
                }

                // GTLog.logger.info("Checked pos at " + charPos + " with flip " + flip);

                boolean result = predicate.test(worldState, state, globalCount, layerCount);
                if (!result) return false;

                charPos.offset(absoluteChar);
            }

            // offset the string start once after every string
            stringStart.offset(absoluteString);
            charPos.from(stringStart);

            // layer minimum checks
            for (Object2IntMap.Entry<TraceabilityPredicate.SimplePredicate> entry : layerCount.object2IntEntrySet()) {
                if (entry.getIntValue() < entry.getKey().minLayerCount) {
                    state.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 3));
                    return false;
                }
            }
        }
        return true;
    }

    public int getRepetitionCount(int aisleI) {
        return aisles[aisleI].actualRepeats;
    }

    @Override
    public MultiblockShapeInfo getDefaultShape(boolean skipMTEs) {
        // for each symbol, which simple predicate is being used
        // this advances whenever a minimum has been satisfied(if any), or a maximum has been reached(if any)
        // preview counts are treated as exactly that many
        Char2IntMap predicateIndex = new Char2IntOpenHashMap();
        // candidates to be passed into MultiblockShapeInfo
        Char2ObjectMap<BlockInfo> candidates = new Char2ObjectOpenHashMap<>();
        // cache for candidates
        Object2CharMap<TraceabilityPredicate.SimplePredicate> infos = new Object2CharOpenHashMap<>();
        Object2IntMap<TraceabilityPredicate.SimplePredicate> globalCache = new Object2IntOpenHashMap<>();
        Object2IntMap<TraceabilityPredicate.SimplePredicate> layerCache = new Object2IntOpenHashMap<>();

        List<char[][]> pattern = new ArrayList<>(dimensions[0]);

        // 0 is reserved for air
        char currentChar = 1;
        int aisleOffset = 0;

        // first pass fills in all the minimum counts
        for (int aisleI = 0; aisleI < dimensions[0]; aisleI++) {
            for (int repeats = 1; repeats <= aisles[aisleI].minRepeats; repeats++) {
                pattern.add(new char[dimensions[1]][dimensions[2]]);
                layerCache.clear();
                for (int stringI = 0; stringI < dimensions[1]; stringI++) {
                    for (int charI = 0; charI < dimensions[2]; charI++) {
                        char c = aisles[aisleI].charAt(stringI, charI);
                        TraceabilityPredicate predicate = predicates.get(c);
                        // we used up all the simple predicates, just let the second pass fill them in
                        if (predicateIndex.get(c) >= predicate.common.size()) continue;
                        TraceabilityPredicate.SimplePredicate simple = predicate.common.get(predicateIndex.get(c));

                        if (simple.candidates == null) continue;

                        // cache all the BlockInfo, so that we only need to get it once per simple predicate
                        if (!infos.containsKey(simple)) {
                            BlockInfo[] blockInfos = simple.candidates.get();
                            int pointer = 0;
                            if (blockInfos.length != 1 && skipMTEs) {
                                // move the pointer to the last pos where there is no MTE
                                try {
                                    while ((blockInfos[pointer].getTileEntity() instanceof MetaTileEntityHolder))
                                        pointer++;
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    // every candidate is a MTE, just do first one
                                    pointer = 0;
                                }
                            }
                            infos.put(simple, currentChar);
                            candidates.put(currentChar, blockInfos[pointer]);
                            currentChar++;
                        }

                        char info = infos.getChar(simple);
                        int layerCount = layerCache.put(simple, layerCache.getInt(simple) + 1) + 1;
                        int globalCount = globalCache.put(simple, globalCache.getInt(simple) + 1) + 1;

                        // replace all air with 0 instead of whatever char
                        pattern.get(aisleOffset)[stringI][charI] = candidates.get(info).getBlockState() ==
                                Blocks.AIR.getDefaultState() ? 0 : info;

                        TraceabilityPredicate.SimplePredicate next = simple;

                        // don't need inequalities since everything is incremented once at a time
                        // only put the minimum amount of parts possible
                        // missing parts will be filled in the second pass
                        while ((next.previewCount == -1 || globalCount == next.previewCount) &&
                                (next.minLayerCount == -1 || layerCount == next.minLayerCount) &&
                                (next.minGlobalCount == -1 || globalCount == next.minGlobalCount)) {
                            // if the current predicate is used, move until the next free one
                            int newIndex = predicateIndex.put(c, predicateIndex.get(c) + 1) + 1;
                            if (newIndex >= predicate.common.size()) break;
                            next = predicate.common.get(newIndex);
                            globalCount = globalCache.getInt(next);
                            layerCount = layerCache.getInt(next);
                        }
                    }
                }
                aisleOffset++;
            }
        }

        predicateIndex.clear();
        aisleOffset = 0;

        // second pass fills everything else
        for (int aisleI = 0; aisleI < dimensions[0]; aisleI++) {
            for (int repeats = 1; repeats <= aisles[aisleI].minRepeats; repeats++) {
                layerCache.clear();
                for (int stringI = 0; stringI < dimensions[1]; stringI++) {
                    for (int charI = 0; charI < dimensions[2]; charI++) {
                        // skip if populated by first pass
                        if (pattern.get(aisleOffset)[stringI][charI] != 0) continue;

                        char c = aisles[aisleI].charAt(stringI, charI);
                        TraceabilityPredicate predicate = predicates.get(c);
                        TraceabilityPredicate.SimplePredicate next = predicate.common.get(predicateIndex.get(c));

                        int layerCount = layerCache.getInt(next);
                        int globalCount = globalCache.getInt(next);

                        // don't need inequalities since everything is incremented once at a time
                        // do this first because the first pass could have left some predicates already used
                        while ((next.previewCount != -1 && globalCount == next.previewCount) ||
                                (next.maxLayerCount != -1 && layerCount == next.maxLayerCount) ||
                                (next.maxGlobalCount != -1 && globalCount == next.maxGlobalCount)) {
                            // if the current predicate is used, move until the next free one
                            int newIndex = predicateIndex.put(c, predicateIndex.get(c) + 1) + 1;
                            if (newIndex >= predicate.common.size())
                                GTLog.logger.warn("Failed to generate default structure pattern.",
                                        new Throwable());
                            next = predicate.common.get(newIndex);
                            globalCount = globalCache.getInt(next);
                            layerCount = layerCache.getInt(next);
                        }

                        if (next.candidates == null) continue;

                        if (!infos.containsKey(next)) {
                            BlockInfo info = next.candidates.get()[0];
                            infos.put(next, currentChar);
                            candidates.put(currentChar, info);
                            currentChar++;
                        }

                        char info = infos.getChar(next);
                        layerCache.put(next, layerCount + 1);
                        globalCache.put(next, globalCount + 1);

                        pattern.get(aisleOffset)[stringI][charI] = candidates.get(info).getBlockState() ==
                                Blocks.AIR.getDefaultState() ? 0 : info;
                    }
                }
                aisleOffset++;
            }
        }

        return new MultiblockShapeInfo(pattern.stream().map(a -> new PatternAisle(1, a)).toArray(PatternAisle[]::new),
                candidates, Char2ObjectMaps.emptyMap(), directions);
    }

    @Override
    public PatternState getPatternState() {
        return state;
    }

    /**
     * DO NOT MUTATE THIS
     */
    public AisleStrategy getAisleStrategy() {
        return aisleStrategy;
    }

    @Override
    public OriginOffset getOffset() {
        return offset;
    }

    private GreggyBlockPos startPos(GreggyBlockPos controllerPos, EnumFacing frontFacing, EnumFacing upFacing,
                                    boolean flip) {
        GreggyBlockPos start = controllerPos.copy();
        offset.apply(start, frontFacing, upFacing, flip);
        return start;
    }
}
