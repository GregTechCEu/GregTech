package gregtech.api.pattern.pattern;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.GreggyBlockPos;
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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Matrix4f;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class ExpandablePattern implements IBlockPattern {

    protected final Supplier<int[]> boundSupplier;
    protected final BiFunction<GreggyBlockPos, int[], TraceabilityPredicate> predicateFunction;
    protected final GreggyBlockPos offset = new GreggyBlockPos();

    /**
     * In the form of [ aisleDir, stringDir, charDir ]
     */
    protected final EnumFacing[] directions;
    protected final BlockWorldState worldState;
    protected final Object2IntMap<TraceabilityPredicate.SimplePredicate> globalCount = new Object2IntOpenHashMap<>();
    protected final PatternState state = new PatternState();
    protected final Long2ObjectMap<BlockInfo> cache = new Long2ObjectOpenHashMap<>();

    /**
     * New expandable pattern normally you would use {@link FactoryExpandablePattern} instead.
     * 
     * @param boundSupplier     Supplier for bounds, order in the way .values() are ordered in
     *                          RelativeDirection.
     * @param predicateFunction Given a pos and bounds(the one you just passed in, not mutated), return a predicate. The
     *                          pos is offset as explained in the builder method.
     * @param directions        The structure directions, explained in the builder method.
     */
    public ExpandablePattern(@NotNull Supplier<int[]> boundSupplier,
                             @NotNull BiFunction<GreggyBlockPos, int[], TraceabilityPredicate> predicateFunction,
                             @NotNull RelativeDirection[] directions) {
        this.boundSupplier = boundSupplier;
        this.predicateFunction = predicateFunction;
        this.directions = Arrays.stream(directions).map(i -> DEFAULT_FACINGS[i.ordinal()]).toArray(EnumFacing[]::new);

        this.worldState = new BlockWorldState();
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
    public boolean checkPatternAt(World world, Matrix4f transform) {
        int[] bounds = boundSupplier.get();

        globalCount.clear();

        // where the iteration starts, in octant 7
        GreggyBlockPos negativeCorner = new GreggyBlockPos();
        // where the iteration ends, in octant 1
        GreggyBlockPos positiveCorner = new GreggyBlockPos();

        for (int i = 0; i < 3; i++) {
            negativeCorner.set(i, -bounds[directions[i].ordinal() ^ 1]);
            positiveCorner.set(i, bounds[directions[i].ordinal()]);
        }

        worldState.setWorld(world);
        GreggyBlockPos transformed = new GreggyBlockPos();

        // SOUTH, UP, EAST means point is +z, line is +y, plane is +x. this basically means the x val of the iter is
        // aisle count, y is str count, and z is char count.
        for (GreggyBlockPos pos : GreggyBlockPos.allInBox(negativeCorner, positiveCorner, EnumFacing.SOUTH,
                EnumFacing.UP, EnumFacing.EAST)) {

            // test first before using .add() which mutates the pos
            TraceabilityPredicate predicate = predicateFunction.apply(pos, bounds);

            // cache the pos here so that the offsets don't mess it up
            int[] arr = pos.getAll();
            // this basically reshuffles the coordinates into absolute form from relative form
            pos.zero().offset(directions[0], arr[0]).offset(directions[1], arr[1]).offset(directions[2], arr[2]);

            GTUtility.apply(transform, transformed.from(pos));
            worldState.setPos(transformed);

            if (predicate != TraceabilityPredicate.ANY) {
                TileEntity te = worldState.getTileEntity();
                cache.put(transformed.toLong(), new BlockInfo(worldState.getBlockState(),
                        !(te instanceof IGregTechTileEntity gtTe) || gtTe.isValid() ? te : null));
            }

            PatternError result = predicate.test(worldState, globalCount, null);
            if (result != null) {
                state.setError(result);
                clearCache();
                return false;
            }
        }

        for (Object2IntMap.Entry<TraceabilityPredicate.SimplePredicate> entry : globalCount.object2IntEntrySet()) {
            if (entry.getIntValue() < entry.getKey().minGlobalCount) {
                state.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 1));
                clearCache();
                return false;
            }
        }
        return true;
    }

    @Override
    public Long2ObjectSortedMap<TraceabilityPredicate> getDefaultShape(Matrix4f transform,
                                                                       @NotNull Map<String, String> keyMap) {
        int[] bounds = boundSupplier.get();
        Long2ObjectSortedMap<TraceabilityPredicate> predicates = new Long2ObjectRBTreeMap<>();

        GreggyBlockPos negativeCorner = new GreggyBlockPos();
        GreggyBlockPos positiveCorner = new GreggyBlockPos();

        for (int i = 0; i < 3; i++) {
            negativeCorner.set(i, -bounds[directions[i].ordinal() ^ 1]);
            positiveCorner.set(i, bounds[directions[i].ordinal()]);
        }

        GreggyBlockPos translation = new GreggyBlockPos();

        for (GreggyBlockPos pos : GreggyBlockPos.allInBox(negativeCorner, positiveCorner, EnumFacing.SOUTH,
                EnumFacing.UP, EnumFacing.EAST)) {
            TraceabilityPredicate predicate = predicateFunction.apply(pos, bounds);

            int[] arr = pos.getAll();
            pos.zero().offset(directions[0], arr[0]).offset(directions[1], arr[1]).offset(directions[2], arr[2]);

            if (predicate != TraceabilityPredicate.ANY && predicate != TraceabilityPredicate.AIR) {
                predicates.put(GTUtility.apply(transform, translation.from(pos)).toLong(), predicate);
            }
        }

        return predicates;
    }

    @Override
    public PatternState getPatternState() {
        return state;
    }

    @Override
    public Long2ObjectMap<BlockInfo> getCache() {
        return this.cache;
    }

    @Override
    public void clearCache() {
        cache.clear();
    }

    @Override
    public void moveOffset(RelativeDirection dir, int amount) {
        offset.offset(DEFAULT_FACINGS[dir.ordinal()], amount);
    }
}
