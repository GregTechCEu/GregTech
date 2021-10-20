package gregtech.api.util;

import codechicken.lib.vec.Vector3;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.PatternMatchContext;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BlockPatternChecker {
    private static Predicate<BlockWorldState>[][][] blockMatches; //[z][y][x]
    private static TIntObjectMap<Predicate<PatternMatchContext>> layerMatchers = new TIntObjectHashMap<>();
    private static Predicate<PatternMatchContext>[] validators;
    public static RelativeDirection[] structureDir;
    private static int[][] aisleRepetitions;
    private static Pair<Predicate<BlockWorldState>, IntRange>[] countMatches;

    // x, y, z, minZ, maxZ
    private static int[] centerOffset;

    private static BlockWorldState worldState;
    private static MutableBlockPos blockPos;
    private static PatternMatchContext matchContext;
    private static PatternMatchContext layerContext;

    private static <T> T getBlockPatternPrivateValue(BlockPattern blockPattern, String srgName) {
        return ObfuscationReflectionHelper.getPrivateValue(BlockPattern.class, blockPattern, srgName);
    }

    public static boolean updateAllValue(BlockPattern blockPattern) {
        try {
            blockMatches = getBlockPatternPrivateValue(blockPattern, "blockMatches");
            layerMatchers = getBlockPatternPrivateValue(blockPattern, "layerMatchers");
            validators = getBlockPatternPrivateValue(blockPattern, "validators");
            structureDir = getBlockPatternPrivateValue(blockPattern, "structureDir");
            aisleRepetitions = getBlockPatternPrivateValue(blockPattern, "aisleRepetitions");
            countMatches = getBlockPatternPrivateValue(blockPattern, "countMatches");
            structureDir = getBlockPatternPrivateValue(blockPattern, "structureDir");

            centerOffset = getBlockPatternPrivateValue(blockPattern, "centerOffset");

            worldState = getBlockPatternPrivateValue(blockPattern, "worldState");
            blockPos = getBlockPatternPrivateValue(blockPattern, "blockPos");
            matchContext = getBlockPatternPrivateValue(blockPattern, "matchContext");
            layerContext = getBlockPatternPrivateValue(blockPattern, "layerContext");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static PatternMatchContext checkPatternAt(MultiblockControllerBase entity) {
        return BlockPatternChecker.checkPatternAt(entity.structurePattern, entity.getWorld(), entity.getPos(), entity.getFrontFacing().getOpposite());
    }


    public static PatternMatchContext checkPatternAt(BlockPattern blockPattern, World world, BlockPos centerPos, EnumFacing facing) {
        if (blockPattern == null || !updateAllValue(blockPattern)) return null;

        List<BlockPos> validPos = new ArrayList<>();

        int[] countMatchesCache = new int[countMatches.length];
        boolean findFirstAisle = false;
        int minZ = -centerOffset[4];

        matchContext.reset();
        layerContext.reset();

        //Checking aisles
        for (int c = 0, z = minZ++, r; c < blockPattern.getFingerLength(); c++) {
            //Checking repeatable slices
            loop:
            for (r = 0; (findFirstAisle ? r < aisleRepetitions[c][1] : z <= -centerOffset[3]); r++) {
                //Checking single slice
                layerContext.reset();

                for (int b = 0, y = -centerOffset[1]; b < blockPattern.getThumbLength(); b++, y++) {
                    for (int a = 0, x = -centerOffset[0]; a < blockPattern.getPalmLength(); a++, x++) {
                        Predicate<BlockWorldState> predicate = blockMatches[c][b][a];
                        setActualRelativeOffset(blockPos, x, y, z, facing);
                        blockPos.setPos(blockPos.getX() + centerPos.getX(), blockPos.getY() + centerPos.getY(), blockPos.getZ() + centerPos.getZ());
                        worldState.update(world, blockPos, matchContext, layerContext);

                        if (!predicate.test(worldState)) {
                            if (findFirstAisle) {
                                if (r < aisleRepetitions[c][0]) {//retreat to see if the first aisle can start later
                                    r = c = 0;
                                    z = minZ++;
                                    matchContext.reset();
                                    validPos.clear();
                                    findFirstAisle = false;
                                }
                            } else {
                                z++;//continue searching for the first aisle
                            }
                            continue loop;
                        } else if (worldState.getBlockState().getBlock() != Blocks.AIR) {
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

        matchContext.getOrPut("validPos", validPos);
        return matchContext;
    }

    private static void setActualRelativeOffset(MutableBlockPos pos, int x, int y, int z, EnumFacing facing) {
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
        pos.setPos(c1[0], c1[1], c1[2]);
    }
}
