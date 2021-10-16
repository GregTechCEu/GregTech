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
    private static Field SPIN_FIELD = ObfuscationReflectionHelper.findField(MetaTileEntity.class, "spin");
    public Predicate<BlockWorldState>[][][] blockMatches; //[z][y][x]
    public TIntObjectMap<Predicate<PatternMatchContext>> layerMatchers = new TIntObjectHashMap<>();
    public Predicate<PatternMatchContext>[] validators;
    public RelativeDirection[] structureDir;
    public int[][] aisleRepetitions;
    public Pair<Predicate<BlockWorldState>, IntRange>[] countMatches;
    // x, y, z, minZ, maxZ
    public int[] centerOffset;

    public BlockWorldState worldState;
    public MutableBlockPos blockPos;
    public PatternMatchContext matchContext;
    public PatternMatchContext layerContext;

    private static <T> T getBlockPatternPrivateValue(BlockPattern blockPattern, String srgName) {
        return ObfuscationReflectionHelper.getPrivateValue(BlockPattern.class, blockPattern, srgName);
    }

    public boolean updateAllValue(BlockPattern blockPattern) {
        try{
            blockMatches = getBlockPatternPrivateValue(blockPattern, "blockMatches");
            layerMatchers = getBlockPatternPrivateValue(blockPattern, "layerMatchers");
            validators = getBlockPatternPrivateValue(blockPattern, "validators");
            structureDir = getBlockPatternPrivateValue(blockPattern, "structureDir");
            aisleRepetitions = getBlockPatternPrivateValue(blockPattern, "aisleRepetitions");
            countMatches = getBlockPatternPrivateValue(blockPattern, "countMatches");
            centerOffset = getBlockPatternPrivateValue(blockPattern, "centerOffset");

            worldState = new BlockWorldState();
            blockPos = new BlockPos.MutableBlockPos();
            matchContext = new PatternMatchContext();
            layerContext = new PatternMatchContext();
        } catch (Exception e){
            return false;
        }
        return true;
    }

    public static BlockPos getPatternErrorPos(MultiblockControllerBase controllerBase) {
        try{
            BlockPattern structurePattern = ObfuscationReflectionHelper.getPrivateValue(MultiblockControllerBase.class, controllerBase, "structurePattern");
            BlockPatternChecker checker = new BlockPatternChecker();
            if(checker.checkPatternAt(structurePattern, controllerBase.getWorld(), controllerBase.getPos(), controllerBase.getFrontFacing().getOpposite()) == null) {
                return new BlockPos(checker.blockPos);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PatternMatchContext checkPatternAt(MultiblockControllerBase controllerBase) {
        try{
            BlockPattern structurePattern = ObfuscationReflectionHelper.getPrivateValue(MultiblockControllerBase.class, controllerBase, "structurePattern");
            BlockPatternChecker checker = new BlockPatternChecker();
            return checker.checkPatternAt(structurePattern, controllerBase.getWorld(), controllerBase.getPos(), controllerBase.getFrontFacing().getOpposite());
        } catch (Throwable e) {
            return null;
        }
    }

    public PatternMatchContext checkPatternAt(BlockPattern blockPattern, World world, BlockPos centerPos, EnumFacing facing) {
        if (blockPattern == null || !updateAllValue(blockPattern)) return null;

        EnumFacing spin = getSpin(world, centerPos);

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
                        setActualRelativeOffset(blockPos, x, y, z, facing, spin, structureDir);
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
                        } else if(worldState.getBlockState().getBlock() != Blocks.AIR){
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

    public static EnumFacing getSpin(MetaTileEntity controllerBase) {
        EnumFacing result = EnumFacing.NORTH;
        try {
            result = (EnumFacing) SPIN_FIELD.get(controllerBase);
            if (result == null) {
                result = EnumFacing.NORTH;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void setSpin(MetaTileEntity controllerBase, EnumFacing spin) {
        try {
            SPIN_FIELD.set(controllerBase, spin);
            if (controllerBase.getWorld() != null) {
                if (!controllerBase.getWorld().isRemote) {
                    controllerBase.notifyBlockUpdate();
                    controllerBase.markDirty();
                    controllerBase.writeCustomData(-9, packetBuffer -> packetBuffer.writeByte(spin.getIndex()));
                } else {
                    controllerBase.scheduleRenderUpdate();
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static EnumFacing getSpin(World world, BlockPos pos) {
        return getSpin(((MetaTileEntityHolder)world.getTileEntity(pos)).getMetaTileEntity());
    }

    public static MutableBlockPos setActualRelativeOffset(MutableBlockPos pos, int x, int y, int z, EnumFacing facing, EnumFacing spin, RelativeDirection[] structureDir) {
        int[] c0 = new int[]{x, y, z}, c1 = new int[3];
        if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
            EnumFacing of = facing == EnumFacing.DOWN ? spin : spin.getOpposite();
            for (int i = 0; i < 3; i++) {
                switch (structureDir[i].getActualFacing(of)) {
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
            int xOffset = spin.getXOffset();
            int zOffset = spin.getZOffset();
            int tmp;
            if (xOffset == 0) {
                tmp = c1[2];
                c1[2] = zOffset > 0 ? c1[1] : -c1[1];
                c1[1] = zOffset > 0 ? -tmp : tmp;
            } else {
                tmp = c1[0];
                c1[0] = xOffset > 0 ? c1[1] : -c1[1];
                c1[1] = xOffset > 0 ? -tmp : tmp;
            }
        } else {
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
            if(spin == EnumFacing.WEST || spin == EnumFacing.EAST) {
                int xOffset = spin == EnumFacing.WEST ? facing.rotateY().getXOffset() : facing.rotateY().getOpposite().getXOffset();
                int zOffset = spin == EnumFacing.WEST ? facing.rotateY().getZOffset() : facing.rotateY().getOpposite().getZOffset();
                int tmp;
                if(xOffset == 0) {
                    tmp = c1[2];
                    c1[2] = zOffset > 0 ? -c1[1] : c1[1];
                    c1[1] = zOffset > 0 ? tmp : -tmp;
                } else {
                    tmp = c1[0];
                    c1[0] = xOffset > 0 ? -c1[1] : c1[1];
                    c1[1] = xOffset > 0 ? tmp : -tmp;
                }
            } else if(spin == EnumFacing.SOUTH){
                c1[1] = -c1[1];
                if (facing.getXOffset() == 0) {
                    c1[0] = -c1[0];
                } else {
                    c1[2] = -c1[2];
                }
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
