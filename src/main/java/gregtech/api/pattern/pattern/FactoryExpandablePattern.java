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

    public static FactoryExpandablePattern start(RelativeDirection aisleDir, RelativeDirection stringDir,
                                                 RelativeDirection charDir) {
        return new FactoryExpandablePattern(aisleDir, stringDir, charDir);
    }

    public static FactoryExpandablePattern start() {
        return new FactoryExpandablePattern(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.RIGHT);
    }

    public FactoryExpandablePattern boundsFunction(QuadFunction<World, GreggyBlockPos, EnumFacing, EnumFacing, int[]> function) {
        this.boundsFunction = function;
        return this;
    }

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
