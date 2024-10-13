package gregtech.api.pattern.pattern;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.GreggyBlockPos;
import gregtech.api.pattern.OriginOffset;
import gregtech.api.pattern.PatternError;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.function.QuadFunction;

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

import java.util.Map;
import java.util.function.BiFunction;

public class ExpandablePattern implements IBlockPattern {

    protected final QuadFunction<World, GreggyBlockPos, EnumFacing, EnumFacing, int[]> boundsFunction;
    protected final BiFunction<GreggyBlockPos, int[], TraceabilityPredicate> predicateFunction;
    protected final OriginOffset offset = new OriginOffset();

    /**
     * In the form of [ aisleDir, stringDir, charDir ]
     */
    protected final RelativeDirection[] directions;
    protected final BlockWorldState worldState;
    protected final Object2IntMap<TraceabilityPredicate.SimplePredicate> globalCount = new Object2IntOpenHashMap<>();
    protected final PatternState state = new PatternState();
    protected final Long2ObjectMap<BlockInfo> cache = new Long2ObjectOpenHashMap<>();

    /**
     * New expandable pattern normally you would use {@link FactoryExpandablePattern} instead.
     * 
     * @param boundsFunction    A function to supply bounds, order in the way .values() are ordered in
     *                          RelativeDirection.
     * @param predicateFunction Given a pos and bounds(the one you just passed in, not mutated), return a predicate. The
     *                          pos is offset as explained in the builder method.
     * @param directions        The structure directions, explained in the builder method.
     */
    public ExpandablePattern(@NotNull QuadFunction<World, GreggyBlockPos, EnumFacing, EnumFacing, int[]> boundsFunction,
                             @NotNull BiFunction<GreggyBlockPos, int[], TraceabilityPredicate> predicateFunction,
                             @NotNull RelativeDirection[] directions) {
        this.boundsFunction = boundsFunction;
        this.predicateFunction = predicateFunction;
        this.directions = directions;

        this.worldState = new BlockWorldState();
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

        // doesn't support flipping yet so its always false
        state.setFlipped(false);
        boolean valid = checkPatternAt(world, centerPos, frontFacing, upwardsFacing, false);
        if (valid) {
            state.setState(PatternState.EnumCheckState.VALID_UNCACHED);
            return state;
        }

        clearCache(); // we don't want a random cache of a partially formed multi
        state.setState(PatternState.EnumCheckState.INVALID_UNCACHED);
        return state;
    }

    @Override
    public boolean checkPatternAt(World world, BlockPos centerPos, EnumFacing frontFacing, EnumFacing upwardsFacing,
                                  boolean isFlipped) {
        int[] bounds = boundsFunction.apply(world, new GreggyBlockPos(centerPos), frontFacing, upwardsFacing);
        if (bounds == null) return false;

        globalCount.clear();

        // where the iteration starts, in octant 7
        GreggyBlockPos negativeCorner = new GreggyBlockPos();
        // where the iteration ends, in octant 1
        GreggyBlockPos positiveCorner = new GreggyBlockPos();

        // [ absolute aisle, absolute string, absolute dir ]
        EnumFacing[] absolutes = new EnumFacing[3];

        for (int i = 0; i < 3; i++) {
            RelativeDirection selected = directions[i];

            absolutes[i] = selected.getRelativeFacing(frontFacing, upwardsFacing, isFlipped);

            negativeCorner.set(i, -bounds[selected.oppositeOrdinal()]);
            positiveCorner.set(i, bounds[selected.ordinal()]);
        }

        worldState.setWorld(world);
        // this translates from the relative coordinates to world coordinates
        GreggyBlockPos translation = new GreggyBlockPos(centerPos);

        // SOUTH, UP, EAST means point is +z, line is +y, plane is +x. this basically means the x val of the iter is
        // aisle count, y is str count, and z is char count.
        for (GreggyBlockPos pos : GreggyBlockPos.allInBox(negativeCorner, positiveCorner, EnumFacing.SOUTH,
                EnumFacing.UP, EnumFacing.EAST)) {

            // test first before using .add() which mutates the pos
            TraceabilityPredicate predicate = predicateFunction.apply(pos, bounds);

            // cache the pos here so that the offsets don't mess it up
            int[] arr = pos.getAll();
            // this basically reshuffles the coordinates into absolute form from relative form
            pos.zero().offset(absolutes[0], arr[0]).offset(absolutes[1], arr[1]).offset(absolutes[2], arr[2]);
            // translate from the origin to the center
            // set the pos with world coordinates
            worldState.setPos(pos.add(translation));

            if (predicate != TraceabilityPredicate.ANY) {
                TileEntity te = worldState.getTileEntity();
                cache.put(pos.toLong(), new BlockInfo(worldState.getBlockState(),
                        !(te instanceof IGregTechTileEntity gtTe) || gtTe.isValid() ? te : null, predicate));
            }

            PatternError result = predicate.test(worldState, globalCount, null);
            if (result != null) {
                state.setError(result);
                return false;
            }
        }

        for (Object2IntMap.Entry<TraceabilityPredicate.SimplePredicate> entry : globalCount.object2IntEntrySet()) {
            if (entry.getIntValue() < entry.getKey().minGlobalCount) {
                state.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 1));
                return false;
            }
        }
        return true;
    }

    @Override
    public char @Nullable [] @NotNull [] @NotNull [] getDefaultShape(Char2ObjectMap<TraceabilityPredicate.SimplePredicate> map,
                                                                     RelativeDirection[] directions) {
        // todo maybe add this and autobuild
        return null;
    }

    @Override
    public void autoBuild(EntityPlayer player, Map<String, String> map) {}

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
    public OriginOffset getOffset() {
        return offset;
    }
}
