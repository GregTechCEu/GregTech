package gregtech.api.pattern.pattern;

import gregtech.api.pattern.GreggyBlockPos;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.RelativeDirection;
import gregtech.api.util.function.QuadFunction;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public class FactoryExpandablePattern {

    protected QuadFunction<World, GreggyBlockPos, EnumFacing, EnumFacing, int[]> boundsFunction;
    protected BiFunction<GreggyBlockPos, int[], TraceabilityPredicate> predicateFunction;
    protected final RelativeDirection[] structureDir = new RelativeDirection[3];

    private FactoryExpandablePattern(RelativeDirection aisleDir, RelativeDirection stringDir,
                                     RelativeDirection charDir) {
        structureDir[0] = aisleDir;
        structureDir[1] = stringDir;
        structureDir[2] = charDir;
        int flags = 0;
        for (int i = 0; i < 3; i++) {
            switch (structureDir[i]) {
                case UP:
                case DOWN:
                    flags |= 0x1;
                    break;
                case LEFT:
                case RIGHT:
                    flags |= 0x2;
                    break;
                case FRONT:
                case BACK:
                    flags |= 0x4;
                    break;
            }
        }
        if (flags != 0x7) throw new IllegalArgumentException("Must have 3 different axes!");
    }

    /**
     * Starts a new builder using the provided directions.
     */
    public static FactoryExpandablePattern start(RelativeDirection aisleDir, RelativeDirection stringDir,
                                                 RelativeDirection charDir) {
        return new FactoryExpandablePattern(aisleDir, stringDir, charDir);
    }

    /**
     * Same as calling {@link FactoryExpandablePattern#start(RelativeDirection, RelativeDirection, RelativeDirection)}
     * with BACK, UP, RIGHT
     */
    public static FactoryExpandablePattern start() {
        return new FactoryExpandablePattern(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.RIGHT);
    }

    /**
     * This supplies the bounds function. The inputs are: World, controller pos, front facing, up facing. The returned
     * array
     * is an int array of length 6, with how much to extend the multiblock in each direction. The order of the
     * directions is the same
     * as the ordinal of the enum.
     */
    public FactoryExpandablePattern boundsFunction(QuadFunction<World, GreggyBlockPos, EnumFacing, EnumFacing, int[]> function) {
        this.boundsFunction = function;
        return this;
    }

    /**
     * This supplies the predicate from offset pos and the bounds, which is not mutated. The pos is offset so that the
     * controller
     * is at the origin(0, 0, 0). The 3 axes are positive towards the way structure direction is handled. The pos starts
     * as usual,
     * which means it will always be in octant 7. It then ends in octant 1, in the opposite corner to the start corner
     * in the cube specified by the bounding box.
     * The pos travels as expected from the structure direction, traveling first in charDir, then upon going out of
     * bounds once in stringDir and resetting
     * its charDir pos. Same happens when stringDir goes out of bounds and reset, then aisleDir is incremented.
     */
    public FactoryExpandablePattern predicateFunction(BiFunction<GreggyBlockPos, int[], TraceabilityPredicate> function) {
        this.predicateFunction = function;
        return this;
    }

    public ExpandablePattern build() {
        if (boundsFunction == null)
            throw new IllegalStateException("Bound function is null! Use .boundsFunction(...) on the builder!");
        if (predicateFunction == null)
            throw new IllegalStateException("Predicate function is null! Use .predicateFunction(...) on the builder!");

        return new ExpandablePattern(boundsFunction, predicateFunction, structureDir);
    }
}
