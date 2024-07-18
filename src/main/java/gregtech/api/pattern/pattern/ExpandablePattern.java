package gregtech.api.pattern.pattern;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.GreggyBlockPos;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.StructureInfo;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.function.QuadFunction;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class ExpandablePattern implements IBlockPattern {

    protected final QuadFunction<World, GreggyBlockPos, EnumFacing, EnumFacing, int[]> boundsFunction;
    protected final BiFunction<GreggyBlockPos, int[], TraceabilityPredicate> predicateFunction;

    /**
     * In the form of [ aisleDir, stringDir, charDir ]
     */
    protected final RelativeDirection[] directions;
    protected final PatternMatchContext matchContext = new PatternMatchContext();
    protected final StructureInfo info;
    protected final BlockWorldState worldState;
    protected final Long2ObjectMap<BlockInfo> cache = new Long2ObjectOpenHashMap<>();
    protected final Object2IntMap<TraceabilityPredicate.SimplePredicate> globalCount = new Object2IntOpenHashMap<>();

    public ExpandablePattern(@NotNull QuadFunction<World, GreggyBlockPos, EnumFacing, EnumFacing, int[]> boundsFunction,
                             @NotNull BiFunction<GreggyBlockPos, int[], TraceabilityPredicate> predicateFunction,
                             @NotNull RelativeDirection[] directions) {
        this.boundsFunction = boundsFunction;
        this.predicateFunction = predicateFunction;
        this.directions = directions;

        this.info = new StructureInfo(matchContext, null);
        this.worldState = new BlockWorldState(info);
    }

    @Override
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

        boolean valid = checkPatternAt(world, centerPos, frontFacing, upwardsFacing, false);
        if (valid) return matchContext;

        clearCache(); // we don't want a random cache of a partially formed multi
        return null;
    }

    @Override
    public boolean checkPatternAt(World world, BlockPos centerPos, EnumFacing frontFacing, EnumFacing upwardsFacing,
                                  boolean isFlipped) {
        int[] bounds = boundsFunction.apply(world, new GreggyBlockPos(centerPos), frontFacing, upwardsFacing);
        if (bounds == null) return false;

        // where the iteration starts, in octant 7
        GreggyBlockPos negativeCorner = new GreggyBlockPos();
        // where the iteration ends, in octant 1
        GreggyBlockPos positiveCorner = new GreggyBlockPos();

        for (int i = 0; i < 3; i++) {
            // iteration in: [ char, string, aisle ]
            RelativeDirection selected = directions[2 - i];

            // this is which direction the start goes in relation to the origin
            // this progresses by the direction
            int positiveOrdinal = selected.ordinal();

            // opposite of the selected direction
            RelativeDirection opposite = selected.getOpposite();
            // this is in which direction the start is in relation to the origin
            int negativeOrdinal = opposite.ordinal();

            // todo maybe fix this to allow flipping and update the quadfunction
            negativeCorner.offset(opposite.getRelativeFacing(frontFacing, upwardsFacing, false),
                    bounds[negativeOrdinal]);
            positiveCorner.offset(selected.getRelativeFacing(frontFacing, upwardsFacing, false),
                    bounds[positiveOrdinal]);
        }

        // which way each direction progresses absolutely, in [ char, string, aisle ]
        EnumFacing[] facings = new EnumFacing[3];
        for (int i = 0; i < 3; i++) {
            facings[i] = directions[2 - i].getRelativeFacing(frontFacing, upwardsFacing, false);
        }

        worldState.setWorld(world);
        // this translates from the relative coordinates to world coordinates
        GreggyBlockPos translation = new GreggyBlockPos(centerPos);

        for (GreggyBlockPos pos : GreggyBlockPos.allInBox(negativeCorner, positiveCorner, facings)) {

            // test first before using .add() which mutates the pos
            TraceabilityPredicate predicate = predicateFunction.apply(pos, bounds);
            // set the pos with world coordinates
            worldState.setPos(pos.add(translation));

            if (predicate != TraceabilityPredicate.ANY) {
                TileEntity te = worldState.getTileEntity();
                cache.put(pos.toLong(), new BlockInfo(worldState.getBlockState(),
                        !(te instanceof IGregTechTileEntity gtTe) || gtTe.isValid() ? te : null, predicate));
            }

            // GTLog.logger.info("Checked pos at " + pos);

            boolean result = predicate.test(worldState, info, globalCount, null);
            if (!result) return false;
        }

        for (Object2IntMap.Entry<TraceabilityPredicate.SimplePredicate> entry : globalCount.object2IntEntrySet()) {
            if (entry.getIntValue() < entry.getKey().minGlobalCount) {
                info.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 1));
                return false;
            }
        }

        return true;
    }

    @Override
    public PreviewBlockPattern getDefaultShape() {
        return null;
    }

    @Override
    public void clearCache() {
        cache.clear();
    }
}
