package gregtech.api.pattern.pattern;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.GreggyBlockPos;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;

import net.minecraft.block.state.IBlockState;
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

public class BlockPattern implements IBlockPattern {

    /**
     * In the form of [ aisleDir, stringDir, charDir ]
     */
    protected final RelativeDirection[] directions;

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
    protected final BlockWorldState worldState;
    protected final Object2IntMap<TraceabilityPredicate.SimplePredicate> globalCount = new Object2IntOpenHashMap<>();
    protected final Object2IntMap<TraceabilityPredicate.SimplePredicate> layerCount = new Object2IntOpenHashMap<>();
    protected final PatternState state = new PatternState();
    protected final Long2ObjectMap<BlockInfo> cache = new Long2ObjectOpenHashMap<>();

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
        this.directions = directions;
        this.predicates = predicates;
        hasStartOffset = startOffset != null;

        if (startOffset == null) {
            this.startOffset = new int[3];
            legacyStartOffset(centerChar);
        } else {
            this.startOffset = startOffset;
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
                moveStartOffset(directions[0], -aisleI);
                moveStartOffset(directions[1], -result[0]);
                moveStartOffset(directions[2], -result[1]);
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
        this.globalCount.clear();
        this.layerCount.clear();
        cache.clear();

        worldState.setWorld(world);

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
                    cache.put(charPos.toLong(), new BlockInfo(worldState.getBlockState(),
                            !(te instanceof IGregTechTileEntity gtTe) || gtTe.isValid() ? te : null, predicate));
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

    @Override
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

    @Override
    public PatternState getPatternState() {
        return state;
    }

    @Override
    public boolean legacyBuilderError() {
        return !hasStartOffset;
    }

    private GreggyBlockPos startPos(GreggyBlockPos controllerPos, EnumFacing frontFacing, EnumFacing upFacing,
                                    boolean flip) {
        GreggyBlockPos start = controllerPos.copy();
        for (int i = 0; i < 3; i++) {
            start.offset(RelativeDirection.VALUES[2 * i].getRelativeFacing(frontFacing, upFacing, flip),
                    startOffset[i]);
        }
        return start;
    }

    /**
     * Moves the start offset in the given direction and amount, use {@link BlockPattern#clearCache()} after to prevent
     * the cache from being stuck in the old offset.
     * 
     * @param dir    The direction, relative to controller.
     * @param amount The amount to offset.
     */
    public void moveStartOffset(RelativeDirection dir, int amount) {
        // reverse amount if its in the opposite direction
        amount *= (dir.ordinal() % 2 == 0) ? 1 : -1;
        startOffset[dir.ordinal() / 2] += amount;
    }

    /**
     * Get the start offset in the given direction.
     * 
     * @param dir The direction, relative to controller.
     * @return The amount, can be negative.
     */
    public int getStartOffset(RelativeDirection dir) {
        return startOffset[dir.ordinal() / 2] * (dir.ordinal() % 2 == 0 ? 1 : -1);
    }
}
