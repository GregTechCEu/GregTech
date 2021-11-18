package gregtech.api.pattern;

import codechicken.lib.vec.Vector3;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class BlockPattern {

    public final TraceabilityPredicate[][][] blockMatches; //[z][y][x]
    public final int fingerLength; //z size
    public final int thumbLength; //y size
    public final int palmLength; //x size
    public final RelativeDirection[] structureDir;
    public final int[][] aisleRepetitions;

    // x, y, z, minZ, maxZ
    private int[] centerOffset = null;

    public final BlockWorldState worldState = new BlockWorldState();
    public final MutableBlockPos blockPos = new MutableBlockPos();
    public final PatternMatchContext matchContext = new PatternMatchContext();
    public final Map<TraceabilityPredicate.SimplePredicate, Integer> globalCount;
    public final Map<TraceabilityPredicate.SimplePredicate, Integer> layerCount;

    public List<Tuple<BlockPos, BlockInfo>> cache = new LinkedList<>();
//    private Iterator<Tuple<BlockPos, BlockInfo>> iterator;

    public BlockPattern(TraceabilityPredicate[][][] predicatesIn, RelativeDirection[] structureDir, int[][] aisleRepetitions) {
        this.blockMatches = predicatesIn;
        this.globalCount = new HashMap<>();
        this.layerCount = new HashMap<>();
        this.fingerLength = predicatesIn.length;
        this.structureDir = structureDir;
        this.aisleRepetitions = aisleRepetitions;

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

        initializeCenterOffsets();
    }

    private void initializeCenterOffsets() {
        loop:
        for (int x = 0; x < this.palmLength; x++) {
            for (int y = 0; y < this.thumbLength; y++) {
                for (int z = 0, minZ = 0, maxZ = 0; z < this.fingerLength; minZ += aisleRepetitions[z][0], maxZ += aisleRepetitions[z][1], z++) {
                    TraceabilityPredicate predicate = this.blockMatches[z][y][x];
                    if (predicate.isCenter) {
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

//    public PatternMatchContext checkPatternFastLoadBalanceAt(World world, BlockPos centerPos, EnumFacing facing) {
//        if (cache.isEmpty()) {
//            return checkPatternAt(world, centerPos, facing);
//        }
//        if (iterator == null) iterator = cache.iterator();
//        int count = 1;
//        while (iterator.hasNext()) {
//            Tuple<BlockPos, BlockInfo> tuple = iterator.next();
//            IBlockState blockState = world.getBlockState(tuple.getFirst());
//            if (blockState != tuple.getSecond().getBlockState() || world.getTileEntity(tuple.getFirst()) != tuple.getSecond().getTileEntity()) {
//                iterator = null;
//                return checkPatternAt(world, centerPos, facing);
//            }
//            if (count++ == cache.size() / 20) {
//                return worldState.hasError() ? null : matchContext;
//            }
//        }
//        iterator = cache.iterator();
//        return worldState.hasError() ? null : matchContext;
//    }

    public PatternMatchContext checkPatternFastAt(World world, BlockPos centerPos, EnumFacing facing) {
        if (!cache.isEmpty()) {
            boolean pass = true;
            for (Tuple<BlockPos, BlockInfo> tuple : cache) {
                IBlockState blockState = world.getBlockState(tuple.getFirst());
                if (blockState != tuple.getSecond().getBlockState()) {
                   pass = false;
                   break;
                }
                TileEntity tileEntity = world.getTileEntity(tuple.getFirst());
                if (tileEntity != tuple.getSecond().getTileEntity()) {
                    pass = false;
                    break;
                }
            }
            if (pass) return worldState.hasError() ? null : matchContext;
        }
        return checkPatternAt(world, centerPos, facing);
    }

    private PatternMatchContext checkPatternAt(World world, BlockPos centerPos, EnumFacing facing) {
        boolean findFirstAisle = false;
        int minZ = -centerOffset[4];

        this.matchContext.reset();
        this.globalCount.clear();
        this.layerCount.clear();
        cache.clear();

        //Checking aisles
        for (int c = 0, z = minZ++, r; c < this.fingerLength; c++) {
            //Checking repeatable slices
            loop:
            for (r = 0; (findFirstAisle ? r < aisleRepetitions[c][1] : z <= -centerOffset[3]); r++) {
                //Checking single slice
                this.layerCount.clear();

                for (int b = 0, y = -centerOffset[1]; b < this.thumbLength; b++, y++) {
                    for (int a = 0, x = -centerOffset[0]; a < this.palmLength; a++, x++) {
                        TraceabilityPredicate predicate = this.blockMatches[c][b][a];
                        setActualRelativeOffset(x, y, z, facing);
                        blockPos.setPos(blockPos.getX() + centerPos.getX(), blockPos.getY() + centerPos.getY(), blockPos.getZ() + centerPos.getZ());
                        worldState.update(world, blockPos, matchContext, globalCount, layerCount);
                        cache.add(new Tuple<>(new BlockPos(worldState.pos), new BlockInfo(worldState.getBlockState(), worldState.getTileEntity())));
                        if (!predicate.test(worldState)) {
                            worldState.setError(predicate);
                            if (findFirstAisle) {
                                if (r < aisleRepetitions[c][0]) {//retreat to see if the first aisle can start later
                                    r = c = 0;
                                    z = minZ++;
                                    matchContext.reset();
                                    cache.clear();
                                    findFirstAisle = false;
                                }
                            } else {
                                z++;//continue searching for the first aisle
                            }
                            continue loop;
                        }
                    }
                }
                findFirstAisle = true;
                z++;

                //Check layer-local matcher predicate
                for (Map.Entry<TraceabilityPredicate.SimplePredicate, Integer> entry : layerCount.entrySet()) {
                    if (entry.getValue() < entry.getKey().minLayerCount) {
                        worldState.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 2));
                        return null;
                    }
                }
            }
            //Repetitions out of range
            if (r < aisleRepetitions[c][0]) {
                if (!worldState.hasError()) {
                    worldState.setError("unknown error");
                }
                return null;
            }
        }

        //Check count matches amount
        for (Map.Entry<TraceabilityPredicate.SimplePredicate, Integer> entry : globalCount.entrySet()) {
            if (entry.getValue() < entry.getKey().minGlobalCount) {
                worldState.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 3));
                return null;
            }
        }

        worldState.setError(null);
        return matchContext;
    }

    public void autoBuild(EntityPlayer player, MultiblockControllerBase controllerBase) {
        World world = player.world;
        BlockWorldState worldState = new BlockWorldState();
        int minZ = -centerOffset[4];
        EnumFacing facing = controllerBase.getFrontFacing().getOpposite();
        BlockPos centerPos = controllerBase.getPos();
        Map<TraceabilityPredicate.SimplePredicate, BlockInfo[]> cacheInfos = new HashMap<>();
        Map<TraceabilityPredicate.SimplePredicate, Integer> cacheGlobal = new HashMap<>();
        for (int c = 0, z = minZ++, r; c < this.fingerLength; c++) {
            for (r = 0; r < aisleRepetitions[c][0]; r++) {
                Map<TraceabilityPredicate.SimplePredicate, Integer> cacheLayer = new HashMap<>();
                for (int b = 0, y = -centerOffset[1]; b < this.thumbLength; b++, y++) {
                    for (int a = 0, x = -centerOffset[0]; a < this.palmLength; a++, x++) {
                        TraceabilityPredicate predicate = this.blockMatches[c][b][a];
                        setActualRelativeOffset(x, y, z, facing);
                        blockPos.setPos(blockPos.getX() + centerPos.getX(), blockPos.getY() + centerPos.getY(), blockPos.getZ() + centerPos.getZ());
                        worldState.update(world, blockPos, matchContext, cacheGlobal, cacheLayer);
                        if (world.getBlockState(blockPos).getBlock() != Blocks.AIR) {
                            for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
                                limit.testLimited(worldState);
                            }
                        } else {
                            boolean find = false;
                            BlockInfo[] infos = new BlockInfo[0];
                            for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
                                if (limit.minLayerCount > 0) {
                                    if (!cacheLayer.containsKey(limit)) {
                                        cacheLayer.put(limit, 1);
                                    } else if(cacheLayer.get(limit) < limit.minLayerCount){
                                        cacheLayer.put(limit, cacheLayer.get(limit) + 1);
                                    } else {
                                        continue;
                                    }
                                }
                                if (limit.minGlobalCount > 0) {
                                    if (!cacheGlobal.containsKey(limit)) {
                                        cacheGlobal.put(limit, 1);
                                    } else if (cacheGlobal.get(limit) < limit.minGlobalCount){
                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                    } else {
                                        continue;
                                    }
                                }
                                if (!cacheInfos.containsKey(limit)) {
                                    cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                                }
                                infos = cacheInfos.get(limit);
                                find = true;
                                break;
                            }
                            if (!find) { // no limited
                                for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
                                    if (limit.maxLayerCount != -1 && cacheLayer.getOrDefault(limit, Integer.MAX_VALUE) < limit.maxLayerCount) continue;
                                    if (limit.maxGlobalCount != -1 && cacheGlobal.getOrDefault(limit, Integer.MAX_VALUE) < limit.maxGlobalCount) continue;
                                    if (!cacheInfos.containsKey(limit)) {
                                        cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                                    }
                                    if (cacheLayer.containsKey(limit)) {
                                        cacheLayer.put(limit, cacheLayer.get(limit) + 1);
                                    }
                                    if (cacheGlobal.containsKey(limit)) {
                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                    }
                                    infos = ArrayUtils.addAll(infos, cacheInfos.get(limit));
                                }
                                for (TraceabilityPredicate.SimplePredicate common : predicate.common) {
                                    if (!cacheInfos.containsKey(common)) {
                                        cacheInfos.put(common, common.candidates == null ? null : common.candidates.get());
                                    }
                                    infos = ArrayUtils.addAll(infos, cacheInfos.get(common));
                                }
                            }

                            List<ItemStack> candidates = Arrays.stream(infos).filter(info -> info.getBlockState().getBlock() != Blocks.AIR).map(info->{
                                IBlockState blockState = info.getBlockState();
                                MetaTileEntity metaTileEntity = info.getTileEntity() instanceof MetaTileEntityHolder ? ((MetaTileEntityHolder) info.getTileEntity()).getMetaTileEntity() : null;
                                if (metaTileEntity != null) {
                                    return metaTileEntity.getStackForm();
                                } else {
                                    return new ItemStack(Item.getItemFromBlock(blockState.getBlock()), 1, blockState.getBlock().damageDropped(blockState));
                                }
                            }).collect(Collectors.toList());
                            // check inventory
                            ItemStack found = null;
                            if (!player.isCreative()) {
                                for (ItemStack itemStack : player.inventory.mainInventory) {
                                    if (candidates.stream().anyMatch(candidate->candidate.isItemEqual(itemStack)) && !itemStack.isEmpty()) {
                                        found = itemStack.copy();
                                        itemStack.setCount(itemStack.getCount() - 1);
                                        break;
                                    }
                                }
                                if (found == null) continue;
                            } else {
                                found = candidates.get(0).copy();
                            }
                            ItemBlock itemBlock = (ItemBlock) found.getItem();
                            IBlockState state = itemBlock.getBlock().getStateFromMeta(itemBlock.getMetadata(found.getMetadata()));
                            BlockPos pos = new BlockPos(blockPos);
                            world.setBlockState(pos, state);
                            TileEntity holder = world.getTileEntity(pos);
                            if (holder instanceof MetaTileEntityHolder) {
                                MetaTileEntity sampleMetaTileEntity = GregTechAPI.MTE_REGISTRY.getObjectById(found.getItemDamage());
                                if (sampleMetaTileEntity != null) {
                                    MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) holder).setMetaTileEntity(sampleMetaTileEntity);
                                    if (found.hasTagCompound()) {
                                        metaTileEntity.initFromItemStackData(found.getTagCompound());
                                    }
                                    for (EnumFacing enumFacing : FACINGS) {
                                        if(world.getBlockState(pos.offset(enumFacing)).getBlock() == Blocks.AIR) {
                                            if (metaTileEntity.isValidFrontFacing(enumFacing)) {
                                                metaTileEntity.setFrontFacing(enumFacing);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                z++;
            }
        }
    }

    public BlockInfo[][][] getTraceabilityBlocks(int[] repetition){
        Map<TraceabilityPredicate.SimplePredicate, BlockInfo[]> cacheInfos = new HashMap<>();
        Map<TraceabilityPredicate.SimplePredicate, Integer> cacheGlobal = new HashMap<>();
        Map<BlockPos, BlockInfo> blocks = new HashMap<>();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (int l = 0, x = 0; l < this.fingerLength; l++) {
            for (int r = 0; r < repetition[l]; r++) {
                //Checking single slice
                Map<TraceabilityPredicate.SimplePredicate, Integer> cacheLayer = new HashMap<>();
                for (int y = 0; y < this.thumbLength; y++) {
                    for (int z = 0; z < this.palmLength; z++) {
                        TraceabilityPredicate predicate = this.blockMatches[l][y][z];
                        boolean find = false;
                        BlockInfo[] infos = null;
                        for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
                            if (limit.minLayerCount > 0) {
                                if (!cacheLayer.containsKey(limit)) {
                                    cacheLayer.put(limit, 1);
                                } else if(cacheLayer.get(limit) < limit.minLayerCount){
                                    cacheLayer.put(limit, cacheLayer.get(limit) + 1);
                                } else {
                                    continue;
                                }
                            }
                            if (limit.minGlobalCount > 0) {
                                if (!cacheGlobal.containsKey(limit)) {
                                    cacheGlobal.put(limit, 1);
                                } else if (cacheGlobal.get(limit) < limit.minGlobalCount){
                                    cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
                                } else {
                                    continue;
                                }
                            }
                            if (!cacheInfos.containsKey(limit)) {
                                cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                            }
                            infos = cacheInfos.get(limit);
                            find = true;
                            break;
                        }
                        if (!find) {
                            if (predicate.common.isEmpty()) {
                                for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
                                    if (limit.maxGlobalCount == -1 && limit.maxLayerCount == -1) {
                                        if (!cacheInfos.containsKey(limit)) {
                                            cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
                                        }
                                        infos = cacheInfos.get(limit);
                                    }
                                }
                            } else {
                                TraceabilityPredicate.SimplePredicate common = predicate.common.getFirst();
                                if (!cacheInfos.containsKey(common)) {
                                    cacheInfos.put(common, common.candidates == null ? null : common.candidates.get());
                                }
                                infos = cacheInfos.get(common);
                            }
                        }
                        setActualRelativeOffset(z, y, x, EnumFacing.NORTH);
                        BlockInfo info = infos == null || infos.length == 0 ? BlockInfo.EMPTY : infos[0];
                        BlockPos pos = new BlockPos(blockPos);
                        if (info.getTileEntity() instanceof MetaTileEntityHolder) {
                            MetaTileEntityHolder holder = new MetaTileEntityHolder();
                            holder.setMetaTileEntity(((MetaTileEntityHolder) info.getTileEntity()).getMetaTileEntity());
                            info =  new BlockInfo(MetaBlocks.MACHINE.getDefaultState(), holder);
                            for (EnumFacing facing : FACINGS) {
                                BlockInfo nearby = blocks.get(pos.offset(facing));
                                if (nearby == null || nearby.getBlockState().getBlock() == Blocks.AIR) {
                                    if (holder.getMetaTileEntity().isValidFrontFacing(facing)) {
                                        holder.getMetaTileEntity().setFrontFacing(facing);
                                        blocks.put(pos, info);
                                        break;
                                    }

                                }
                            }
                        } else {
                            blocks.put(pos, info);
                        }
                        for (EnumFacing facing : FACINGS) {
                            BlockInfo nearby = blocks.get(pos.offset(facing));
                            if (nearby != null && nearby.getTileEntity() instanceof MetaTileEntityHolder) {
                                MetaTileEntity mte =((MetaTileEntityHolder) nearby.getTileEntity()).getMetaTileEntity();
                                BlockPos pos2 = pos.offset(facing);
                                for (EnumFacing facing2 : FACINGS) {
                                    BlockInfo nearby2 = blocks.get(pos2.offset(facing2));
                                    if (nearby2 == null || nearby2.getBlockState().getBlock() == Blocks.AIR) {
                                        if (mte.isValidFrontFacing(facing2)) {
                                            mte.setFrontFacing(facing2);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        minX = Math.min(pos.getX(), minX); minY = Math.min(pos.getY(), minY); minZ = Math.min(pos.getZ(), minZ);
                        maxX = Math.max(pos.getX(), maxX); maxY = Math.max(pos.getY(), maxY); maxZ = Math.max(pos.getZ(), maxZ);
                    }
                }
                x++;
            }
        }
        BlockInfo[][][] result = (BlockInfo[][][]) Array.newInstance(BlockInfo.class, maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
        int finalMinX = minX;
        int finalMinY = minY;
        int finalMinZ = minZ;
        blocks.forEach((pos, info)-> result[pos.getX() - finalMinX][pos.getY() - finalMinY][pos.getZ() - finalMinZ] = info);
        return result;
    }

    static EnumFacing[] FACINGS = {EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.UP, EnumFacing.DOWN};

    private void setActualRelativeOffset(int x, int y, int z, EnumFacing facing) {
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
        blockPos.setPos(c1[0], c1[1], c1[2]);
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
