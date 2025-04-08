package gregtech.api.pattern.pattern;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.GreggyBlockPos;
import gregtech.api.pattern.OriginOffset;
import gregtech.api.pattern.PatternError;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTUtility;
import gregtech.api.util.RelativeDirection;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Matrix4f;

import java.util.*;

public class BlockPattern implements IBlockPattern {

    /**
     * In the form of [ aisleDir, stringDir, charDir ]
     */
    protected final EnumFacing[] directions;

    /**
     * In the form of [ num aisles, num string per aisle, num char per string ]
     */
    protected final int[] dimensions;
    protected final PatternAisle[] aisles;
    protected final AisleStrategy aisleStrategy;
    protected final Char2ObjectMap<TraceabilityPredicate> predicates;
    protected final BlockWorldState worldState;
    protected final Object2IntMap<TraceabilityPredicate.SimplePredicate> globalCount = new Object2IntOpenHashMap<>();
    protected final Object2IntMap<TraceabilityPredicate.SimplePredicate> layerCount = new Object2IntOpenHashMap<>();
    protected final PatternState state = new PatternState();
    protected final Long2ObjectMap<BlockInfo> cache = new Long2ObjectOpenHashMap<>();
    protected final GreggyBlockPos startPos = new GreggyBlockPos();

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
        this.directions = Arrays.stream(directions).map(i -> DEFAULT_FACINGS[i.ordinal()]).toArray(EnumFacing[]::new);
        this.predicates = predicates;

        if (offset == null) {
            legacyStartOffset(centerChar);
        } else {
            offset.apply(this.startPos, EnumFacing.NORTH, EnumFacing.UP);
        }

        this.worldState = new BlockWorldState();
    }

    /**
     * For legacy compat only,
     *
     * @param center The center char to look for
     */
    private void legacyStartOffset(char center) {
        // don't do anything if center char isn't specified
        if (center == 0) return;
        for (int aisleI = 0; aisleI < dimensions[0]; aisleI++) {
            int[] result = aisles[aisleI].firstInstanceOf(center);
            if (result != null) {
                // structure starts at aisle 0, string 0, char 0, think about it
                moveOffset(directions[0], -aisleI);
                moveOffset(directions[1], -result[0]);
                moveOffset(directions[2], -result[1]);
                return;
            }
        }

        throw new IllegalStateException("Failed to find center char: '" + center + "'");
    }

    @Nullable
    @Override
    public PatternState cachedPattern(World world) {
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
                    state.setState(PatternState.EnumCheckState.INVALID);
                } else {
                    state.setState(PatternState.EnumCheckState.VALID_CACHED);
                }

                return state;
            }
        }

        clearCache();
        state.setError(null);
        return null;
    }

    @Override
    public Long2ObjectMap<BlockInfo> getCache() {
        return cache;
    }

    @Override
    public boolean checkPatternAt(World world, Matrix4f transform) {
        this.globalCount.clear();
        this.layerCount.clear();
        cache.clear();

        worldState.setWorld(world);

        aisleStrategy.pattern = this;
        aisleStrategy.start(transform);
        if (!aisleStrategy.check()) {
            clearCache();
            return false;
        }

        // global minimum checks
        for (Object2IntMap.Entry<TraceabilityPredicate.SimplePredicate> entry : globalCount.object2IntEntrySet()) {
            if (entry.getIntValue() < entry.getKey().minGlobalCount) {
                state.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 1));
                clearCache();
                return false;
            }
        }

        state.setError(null);
        return true;
    }

    /**
     * Checks a specific aisle for validity
     *
     * @param transform   Transformation matrix
     * @param aisleIndex  The index of the aisle, this is where the pattern is gotten from, treats repeatable aisles as
     *                    only 1
     * @param aisleOffset The offset of the aisle, how much offset in aisleDir to check the blocks in world, for
     *                    example, if the first aisle is repeated 2 times, aisleIndex is 1 while this is 2
     * @return True if the check passed
     */
    public boolean checkAisle(Matrix4f transform, int aisleIndex, int aisleOffset) {
        // where the aisle would start in world
        GreggyBlockPos pos = startPos.copy().offset(directions[0], aisleOffset);
        GreggyBlockPos transformed = new GreggyBlockPos();
        PatternAisle aisle = aisles[aisleIndex];

        layerCount.clear();

        for (int stringI = 0; stringI < dimensions[1]; stringI++) {
            for (int charI = 0; charI < dimensions[2]; charI++) {
                GTUtility.apply(transform, transformed.from(pos));
                worldState.setPos(transformed);
                TraceabilityPredicate predicate = predicates.get(aisle.charAt(stringI, charI));

                if (predicate != TraceabilityPredicate.ANY) {
                    TileEntity te = worldState.getTileEntity();
                    cache.put(transformed.toLong(), new BlockInfo(worldState.getBlockState(),
                            !(te instanceof IGregTechTileEntity gtTe) || gtTe.isValid() ? te : null));
                }

                // GTLog.logger.info("Checked pos at " + charPos + " with flip " + flip);

                PatternError result = predicate.test(worldState, globalCount, layerCount);
                if (result != null) {
                    state.setError(result);
                    return false;
                }

                pos.offset(directions[2]);
            }

            // offset the string start once after every string
            pos.offset(directions[2].getOpposite(), dimensions[2]);
            pos.offset(directions[1]);
        }

        // layer minimum checks
        // todo fix all minimum checks fr
        for (Object2IntMap.Entry<TraceabilityPredicate.SimplePredicate> entry : layerCount.object2IntEntrySet()) {
            if (entry.getIntValue() < entry.getKey().minLayerCount) {
                state.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 3));
                return false;
            }
        }

        for (Object2IntMap.Entry<TraceabilityPredicate.SimplePredicate> entry : layerCount.object2IntEntrySet()) {
            globalCount.put(entry.getKey(), globalCount.getInt(entry.getKey()) + entry.getIntValue());
        }

        return true;
    }

    public int getRepetitionCount(int aisleI) {
        return aisles[aisleI].actualRepeats;
    }

    @Override
    public Long2ObjectSortedMap<TraceabilityPredicate> getDefaultShape(Matrix4f transform,
                                                                       @NotNull Map<String, String> keyMap) {
        Long2ObjectSortedMap<TraceabilityPredicate> map = new Long2ObjectRBTreeMap<>();

        GreggyBlockPos pos = startPos.copy();
        GreggyBlockPos transformed = new GreggyBlockPos();

        int[] order = aisleStrategy.getDefaultAisles(keyMap);
        for (int i = 0; i < order.length; i++) {
            for (int j = 0; j < dimensions[1]; j++) {
                for (int k = 0; k < dimensions[2]; k++) {
                    TraceabilityPredicate pred = predicates.get(aisles[order[i]].charAt(j, k));
                    if (pred != TraceabilityPredicate.ANY && pred != TraceabilityPredicate.AIR)
                        map.put(GTUtility.apply(transform, transformed.from(pos)).toLong(),
                                predicates.get(aisles[order[i]].charAt(j, k)));
                    pos.offset(directions[2]);
                }
                pos.offset(directions[1]);
                pos.offset(directions[2].getOpposite(), dimensions[2]);
            }

            pos.from(startPos);
            pos.offset(directions[0], i + 1);
        }

        return map;
    }

    @Override
    public PatternState getPatternState() {
        return state;
    }

    /**
     * Probably shouldn't mutate this.
     */
    public AisleStrategy getAisleStrategy() {
        return aisleStrategy;
    }

    @Override
    public void moveOffset(RelativeDirection dir, int amount) {
        startPos.offset(DEFAULT_FACINGS[dir.ordinal()], amount);
    }

    public void moveOffset(EnumFacing dir, int amount) {
        startPos.offset(dir, amount);
    }
}
