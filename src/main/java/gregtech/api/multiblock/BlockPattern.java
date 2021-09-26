package gregtech.api.multiblock;

import codechicken.lib.vec.Vector3;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gregtech.api.util.IntRange;
import gregtech.api.util.RelativeDirection;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BlockPattern {

    public final Predicate<BlockWorldState>[][][] blockMatches; //[z][y][x]
    public final TIntObjectMap<Predicate<PatternMatchContext>> layerMatchers = new TIntObjectHashMap<>();
    public final Predicate<PatternMatchContext>[] validators;
    public final int fingerLength; //z size
    public final int thumbLength; //y size
    public final int palmLength; //x size
    public final RelativeDirection[] structureDir;
    public final int[][] aisleRepetitions;
    public final Pair<Predicate<BlockWorldState>, IntRange>[] countMatches;

    // x, y, z, minZ, maxZ
    private int[] centerOffset = null;

    public final BlockWorldState worldState = new BlockWorldState();
    public final MutableBlockPos blockPos = new MutableBlockPos();
    public final PatternMatchContext matchContext = new PatternMatchContext();
    public final PatternMatchContext layerContext = new PatternMatchContext();

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
        return checkPatternAt(world, centerPos, facing, null);
    }

    public PatternMatchContext checkPatternAt(World world, BlockPos centerPos, EnumFacing facing, List<BlockPos> validPos) {
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
                                    if (validPos != null) {
                                        validPos.clear();
                                    }
                                    findFirstAisle = false;
                                }
                            } else {
                                z++;//continue searching for the first aisle
                            }
                            continue loop;
                        } else if (validPos != null && worldState.getBlockState().getBlock() != Blocks.AIR) {
                            validPos.add(new BlockPos(worldState.getPos()));
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

    public static BlockPos getActualPos(EnumFacing ref, EnumFacing facing, EnumFacing spin, int x, int y, int z) {
        Vector3 vector3 = new Vector3(x, y, z);
        double degree = Math.PI/2 * (spin == EnumFacing.EAST? 1: spin == EnumFacing.SOUTH? 2: spin == EnumFacing.WEST? -1:0);
        if (ref != facing) {
            if (facing.getAxis() != EnumFacing.Axis.Y) {
                vector3.rotate(Math.PI/2 * ((4 + facing.getHorizontalIndex() - ref.getHorizontalIndex()) % 4), new Vector3(0, -1, 0));
            } else {
                vector3.rotate(-Math.PI/2 * facing.getYOffset(), new Vector3(-ref.rotateY().getXOffset(), 0, -ref.rotateY().getZOffset()));
                degree = facing.getYOffset() * Math.PI/2 * ((4 + spin.getHorizontalIndex() - (facing.getYOffset() > 0 ? ref.getOpposite() : ref).getHorizontalIndex()) % 4);
            }
        }
        vector3.rotate(degree, new Vector3(-facing.getXOffset(), -facing.getYOffset(), -facing.getZOffset()));
        return new BlockPos(Math.round(vector3.x), Math.round(vector3.y), Math.round(vector3.z));
    }

    public static EnumFacing getActualFrontFacing(EnumFacing ref, EnumFacing facing, EnumFacing spin, EnumFacing frontFacing) {
        BlockPos pos = getActualPos(ref, facing, spin, frontFacing.getXOffset(), frontFacing.getYOffset(), frontFacing.getZOffset());
        return pos.getX() < 0 ? EnumFacing.WEST : pos.getX() > 0 ? EnumFacing.EAST
                : pos.getY() < 0 ? EnumFacing.DOWN : pos.getY() > 0 ? EnumFacing.UP
                : pos.getZ() < 0 ? EnumFacing.NORTH : EnumFacing.SOUTH;
    }
}
