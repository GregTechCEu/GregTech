package gregtech.api.multiblock;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gregtech.api.util.IntRange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class BlockPattern {

    private final Predicate<BlockWorldState>[][][] blockMatches; //[z][y][x]
    private final TIntObjectMap<Predicate<PatternMatchContext>> layerMatchers = new TIntObjectHashMap<>();
    private final Predicate<PatternMatchContext>[] validators;
    private final int fingerLength; //z size
    private final int thumbLength; //y size
    private final int palmLength; //x size
    private final RelativeDirection[] structureDir;
    private final int[][] aisleRepetitions;
    private final Pair<Predicate<BlockWorldState>, IntRange>[] countMatches;

    // x, y, z, minZ, maxZ
    private int[] centerOffset = null;

    private final BlockWorldState worldState = new BlockWorldState();
    private final MutableBlockPos blockPos = new MutableBlockPos();
    private final PatternMatchContext matchContext = new PatternMatchContext();
    private final PatternMatchContext layerContext = new PatternMatchContext();

    public BlockPattern(Predicate<BlockWorldState>[][][] predicatesIn,
                        List<Pair<Predicate<BlockWorldState>, IntRange>> countMatches,
                        TIntObjectMap<Predicate<PatternMatchContext>> layerMatchers,
                        List<Predicate<PatternMatchContext>> validators,
                        RelativeDirection[] structureDir,
                        int[][] aisleRepetitions) {
        this.blockMatches = predicatesIn;
        this.countMatches = countMatches.toArray(new Pair[0]);
        this.layerMatchers.putAll(layerMatchers);
        this.validators = validators.toArray(new Predicate[0]);
        this.fingerLength = predicatesIn.length;

        if (this.fingerLength > 0) {
            this.thumbLength = predicatesIn[0].length;

            if (this.thumbLength > 0) {
                this.palmLength = predicatesIn[0][0].length;
            } else {
                this.palmLength = 0;
            }
        } else {
            this.thumbLength = 0;
            this.palmLength = 0;
        }
        this.structureDir = structureDir;
        this.aisleRepetitions = aisleRepetitions;

        initializeCenterOffsets();
    }

    private void initializeCenterOffsets() {
        loop:
        for (int x = 0; x < this.palmLength; x++) {
            for (int y = 0; y < this.thumbLength; y++) {
                for (int z = 0, minZ = 0, maxZ = 0; z < this.fingerLength; minZ += aisleRepetitions[z][0], maxZ += aisleRepetitions[z][1], z++) {
                    Predicate<BlockWorldState> predicate = this.blockMatches[z][y][x];
                    if (predicate instanceof IPatternCenterPredicate) {
                        centerOffset = new int[]{x, y, z, minZ, maxZ};
                        break loop;
                    }
                }
            }
        }
        if (centerOffset == null) {
            throw new IllegalArgumentException("Didn't found center predicate");
        }
    }

    public int getFingerLength() {
        return this.fingerLength;
    }

    public int getThumbLength() {
        return this.thumbLength;
    }

    public int getPalmLength() {
        return this.palmLength;
    }

    public PatternMatchContext checkPatternAt(World world, BlockPos centerPos, EnumFacing facing) {
        int[] countMatchesCache = new int[countMatches.length];
        boolean findFirstAisle = false;
        int minZ = -centerOffset[4];

        this.matchContext.reset();
        this.layerContext.reset();

        //Checking aisles
        for (int c = 0, z = minZ++, r; c < this.fingerLength; c++) {
            //Checking repeatable slices
            loop:
            for (r = 0; (findFirstAisle ? r < aisleRepetitions[c][1] : z <= -centerOffset[3]); r++) {
                //Checking single slice
                this.layerContext.reset();

                for (int b = 0, y = -centerOffset[1]; b < this.thumbLength; b++, y++) {
                    for (int a = 0, x = -centerOffset[0]; a < this.palmLength; a++, x++) {
                        Predicate<BlockWorldState> predicate = this.blockMatches[c][b][a];
                        setActualRelativeOffset(blockPos, x, y, z, facing);
                        blockPos.setPos(blockPos.getX() + centerPos.getX(), blockPos.getY() + centerPos.getY(), blockPos.getZ() + centerPos.getZ());
                        worldState.update(world, blockPos, matchContext, layerContext);

                        if (!predicate.test(worldState)) {
                            if (findFirstAisle) {
                                if (r < aisleRepetitions[c][0]) {//retreat to see if the first aisle can start later
                                    r = c = 0;
                                    z = minZ++;
                                    matchContext.reset();
                                    findFirstAisle = false;
                                }
                            } else {
                                z++;//continue searching for the first aisle
                            }
                            continue loop;
                        }
                        for (int i = 0; i < countMatchesCache.length; i++) {
                            if (countMatches[i].getLeft().test(worldState)) {
                                countMatchesCache[i]++;
                            }
                        }
                    }
                }
                findFirstAisle = true;
                z++;

                //Check layer-local matcher predicate
                Predicate<PatternMatchContext> layerPredicate = layerMatchers.get(c);
                if (layerPredicate != null && !layerPredicate.test(layerContext)) {
                    return null;
                }
            }
            //Repetitions out of range
            if (r < aisleRepetitions[c][0]) {
                return null;
            }
        }

        //Check count matches amount
        for (int i = 0; i < countMatchesCache.length; i++) {
            IntRange intRange = countMatches[i].getRight();
            if (!intRange.isInsideOf(countMatchesCache[i])) {
                return null; //count matches didn't match
            }
        }

        //Check general match predicates
        for (Predicate<PatternMatchContext> validator : validators) {
            if (!validator.test(matchContext)) {
                return null;
            }
        }

        return matchContext;
    }

    private MutableBlockPos setActualRelativeOffset(MutableBlockPos pos, int x, int y, int z, EnumFacing facing) {
        //if (!ArrayUtils.contains(ALLOWED_FACINGS, facing))
        //    throw new IllegalArgumentException("Can rotate only horizontally");
        int[] c0 = new int[]{x, y, z}, c1 = new int[3];
        for (int i = 0; i < 3; i++) {
            switch (structureDir[i].getActualFacing(facing)) {
                case UP:
                    c1[1] = c0[i];
                    break;
                case DOWN:
                    c1[1] = -c0[i];
                    break;
                case WEST:
                    c1[0] = -c0[i];
                    break;
                case EAST:
                    c1[0] = c0[i];
                    break;
                case NORTH:
                    c1[2] = -c0[i];
                    break;
                case SOUTH:
                    c1[2] = c0[i];
                    break;
            }
        }
        return pos.setPos(c1[0], c1[1], c1[2]);
    }

    /**
     * Relative direction when facing horizontally
     */
    public enum RelativeDirection {
        UP(f -> EnumFacing.UP),
        DOWN(f -> EnumFacing.DOWN),
        LEFT(EnumFacing::rotateYCCW),
        RIGHT(EnumFacing::rotateY),
        FRONT(Function.identity()),
        BACK(EnumFacing::getOpposite);

        final Function<EnumFacing, EnumFacing> actualFacing;

        RelativeDirection(Function<EnumFacing, EnumFacing> actualFacing) {
            this.actualFacing = actualFacing;
        }

        public EnumFacing getActualFacing(EnumFacing facing) {
            return actualFacing.apply(facing);
        }
    }

}
