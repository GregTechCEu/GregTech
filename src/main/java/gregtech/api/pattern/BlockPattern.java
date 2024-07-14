package gregtech.api.pattern;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.registry.MTERegistry;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BlockPattern {

    static EnumFacing[] FACINGS = { EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.UP,
            EnumFacing.DOWN };

    /**
     * In the form of [ charDir, stringDir, aisleDir ]
     */
    public final RelativeDirection[] structureDir;

    /**
     * In the form of [ num aisles, num string per aisle, num char per string ]
     */
    protected final int[] dimensions;

    /**
     * In the form of [ aisleOffset, stringOffset, charOffset ] where the offsets are the opposite {@link RelativeDirection} of structure directions
     */
    protected final int[] startOffset;
    protected final PatternAisle[] aisles;
    protected final Char2ObjectMap<TraceabilityPredicate> predicates;
    protected final StructureInfo info;
    protected final BlockWorldState worldState;
    protected final PatternMatchContext matchContext = new PatternMatchContext();
    protected final Object2IntMap<TraceabilityPredicate.SimplePredicate> globalCount = new Object2IntOpenHashMap<>();
    protected final Object2IntMap<TraceabilityPredicate.SimplePredicate> layerCount = new Object2IntOpenHashMap<>();

    public Long2ObjectMap<BlockInfo> cache = new Long2ObjectOpenHashMap<>();

    /**
     * The repetitions per aisle along the axis of repetition
     */
    public int[] formedRepetitionCount;

    // how many not nulls to keep someone from not passing in null?
    public BlockPattern(@NotNull PatternAisle @NotNull [] aisles, int @NotNull [] dimensions, @NotNull RelativeDirection @NotNull [] directions,
                        int @Nullable [] startOffset, @NotNull Char2ObjectMap<TraceabilityPredicate> predicates, char centerChar) {
        this.aisles = aisles;
        this.dimensions = dimensions;
        this.structureDir = directions;
        this.predicates = predicates;
        this.startOffset = startOffset;

        if (this.startOffset == null) legacyStartOffset(centerChar);

        this.info = new StructureInfo(new PatternMatchContext(), null);
        this.worldState = new BlockWorldState(info);
    }

    /**
     * For legacy compat only,
     * @param center The center char to look for
     */
    private void legacyStartOffset(char center) {
        // could also use aisles.length but this is cooler
        for (int aisleI = 0; aisleI < dimensions[2]; aisleI++) {
            int[] result = aisles[aisleI].firstInstanceOf(center);
            if (result != null) {
                startOffset[0] = aisleI;
                startOffset[1] = result[0];
                startOffset[2] = result[2];
                return;
            }
        }

        throw new IllegalArgumentException("Didn't find center predicate");
    }

    public PatternError getError() {
        return info.getError();
    }

    public PatternMatchContext checkPatternFastAt(World world, BlockPos centerPos, EnumFacing frontFacing,
                                                  EnumFacing upwardsFacing, boolean allowsFlip) {
        if (!cache.isEmpty()) {
            boolean pass = true;
            for (Map.Entry<Long, BlockInfo> entry : cache.entrySet()) {
                BlockPos pos = BlockPos.fromLong(entry.getKey());
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
            if (pass) return worldState.hasError() ? null : matchContext;
        }

        // First try normal pattern, and if it fails, try flipped (if allowed).
        boolean valid = checkPatternAt(world, centerPos, frontFacing, upwardsFacing, false);
        if (valid) return matchContext;

        if (allowsFlip) {
            valid = checkPatternAt(world, centerPos, frontFacing, upwardsFacing, true);
        }
        if (!valid) clearCache(); // we don't want a random cache of a partially formed multi
        return null;
    }

    public void clearCache() {
        cache.clear();
    }

    private boolean checkPatternAt(World world, BlockPos centerPos, EnumFacing frontFacing,
                                               EnumFacing upwardsFacing, boolean isFlipped) {
        this.matchContext.reset();
        this.globalCount.clear();
        this.layerCount.clear();
        cache.clear();

        worldState.world = world;

        int aisleOffset = -1;
        GreggyBlockPos controllerPos = new GreggyBlockPos(centerPos);

        for (int aisleI = 0; aisleI < aisles.length; aisleI++) {
            PatternAisle aisle = aisles[aisleI];

            for (int repeats = aisle.minRepeats; repeats <= aisle.maxRepeats; repeats++) {
                boolean aisleResult = checkAisle(controllerPos, frontFacing, upwardsFacing, aisleI, aisleOffset + repeats, isFlipped);

                // since aisle repetitions goes up, if this amount of repetitions are invalid, and all smaller repetitions
                // have already been checked, then all larger repetitions will also be invalid(since they must also contain
                // the problematic section), so the pattern cannot be correct
                if (!aisleResult) return false;

                // if this isn't the last aisle, then check the next aisle after to see if this repetition matches with that
                // if not, then this repetition is invalid. This only checks the first aisle even if it has a min repeat > 1, but yeah
                if (aisleI != aisles.length - 1) {
                    boolean nextResult = checkAisle(controllerPos, frontFacing, upwardsFacing, aisleI + 1, aisleOffset + repeats + 1, isFlipped);

                    // if the next aisle is also valid, then move on
                    if (nextResult) {
                        aisleOffset += repeats;
                        break;
                    }
                }
            }
        }

        info.setError(null);
        matchContext.setNeededFlip(isFlipped);
        return true;
    }

    /**
     * Checks a specific aisle for validity
     * @param controllerPos The position of the controller
     * @param frontFacing The front facing of the controller
     * @param upFacing The up facing of the controller
     * @param aisleIndex The index of the aisle, this is where the pattern is gotten from, treats repeatable aisles as only 1
     * @param aisleOffset The offset of the aisle, how much offset in aisleDir to check the blocks in world, for example, if the first aisle is repeated 2 times, aisleIndex is 1 while this is 2
     * @param flip Whether to flip or not
     * @return True if the check passed, otherwise the {@link StructureInfo} would have been updated with an error
     */
    public boolean checkAisle(GreggyBlockPos controllerPos, EnumFacing frontFacing, EnumFacing upFacing, int aisleIndex, int aisleOffset, boolean flip) {
        // absolute facings from the relative facings
        EnumFacing absoluteAisle = structureDir[0].getRelativeFacing(frontFacing, upFacing, flip);
        EnumFacing absoluteString = structureDir[1].getRelativeFacing(frontFacing, upFacing, flip);
        EnumFacing absoluteChar = structureDir[2].getRelativeFacing(frontFacing, upFacing, flip);

        // where the aisle would start in world
        GreggyBlockPos aisleStart = startPos(controllerPos, frontFacing, upFacing, flip)
                .offset(absoluteAisle, aisleOffset);
        // where the current string would start in world
        GreggyBlockPos stringStart = aisleStart.copy();
        // where the char being checked is
        GreggyBlockPos charPos = aisleStart.copy();
        PatternAisle aisle = aisles[aisleIndex];

        layerCount.clear();

        for (int stringI = 0; stringI < dimensions[1]; stringI++) {
            for (int charI = 0; charI < dimensions[2]; charI++) {
                worldState.setPos(charPos);
                TraceabilityPredicate predicate = predicates.get(aisle.charAt(stringI, charI));
                boolean result = predicate.test(worldState, info, globalCount, layerCount);
                if (!result) return false;

                charPos.offset(absoluteChar);
            }

            // offset the string start once after every string
            stringStart.offset(absoluteString);
            charPos.from(stringStart);

            // layer minimum checks
            for (Object2IntMap.Entry<TraceabilityPredicate.SimplePredicate> entry : layerCount.object2IntEntrySet()) {
                if (entry.getIntValue() < entry.getKey().minLayerCount) {
                    info.setError(new TraceabilityPredicate.SinglePredicateError(entry.getKey(), 3));
                    return false;
                }
            }
        }
        return true;
    }

    public void autoBuild(EntityPlayer player, MultiblockControllerBase controllerBase) {
//        World world = player.world;
//        BlockWorldState worldState = new BlockWorldState();
//        int minZ = -centerOffset[4];
//        EnumFacing facing = controllerBase.getFrontFacing().getOpposite();
//        BlockPos centerPos = controllerBase.getPos();
//        Map<TraceabilityPredicate.SimplePredicate, BlockInfo[]> cacheInfos = new HashMap<>();
//        Map<TraceabilityPredicate.SimplePredicate, Integer> cacheGlobal = new HashMap<>();
//        Map<BlockPos, Object> blocks = new HashMap<>();
//        blocks.put(controllerBase.getPos(), controllerBase);
//        for (int c = 0, z = minZ++, r; c < this.fingerLength; c++) {
//            for (r = 0; r < aisleRepetitions[c][0]; r++) {
//                Map<TraceabilityPredicate.SimplePredicate, Integer> cacheLayer = new HashMap<>();
//                for (int b = 0, y = -centerOffset[1]; b < this.thumbLength; b++, y++) {
//                    for (int a = 0, x = -centerOffset[0]; a < this.palmLength; a++, x++) {
//                        TraceabilityPredicate predicate = this.blockMatches[c][b][a];
//                        BlockPos pos = setActualRelativeOffset(x, y, z, facing, controllerBase.getUpwardsFacing(),
//                                controllerBase.isFlipped())
//                                        .add(centerPos.getX(), centerPos.getY(), centerPos.getZ());
//                        worldState.update(world, pos, matchContext, globalCount, layerCount, predicate);
//                        if (!world.getBlockState(pos).getMaterial().isReplaceable()) {
//                            blocks.put(pos, world.getBlockState(pos));
//                            for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
//                                limit.testLimited(worldState);
//                            }
//                        } else {
//                            boolean find = false;
//                            BlockInfo[] infos = new BlockInfo[0];
//                            for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
//                                if (limit.minLayerCount > 0) {
//                                    if (!cacheLayer.containsKey(limit)) {
//                                        cacheLayer.put(limit, 1);
//                                    } else
//                                        if (cacheLayer.get(limit) < limit.minLayerCount && (limit.maxLayerCount == -1 ||
//                                                cacheLayer.get(limit) < limit.maxLayerCount)) {
//                                                    cacheLayer.put(limit, cacheLayer.get(limit) + 1);
//                                                } else {
//                                                    continue;
//                                                }
//                                } else {
//                                    continue;
//                                }
//                                if (!cacheInfos.containsKey(limit)) {
//                                    cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
//                                }
//                                infos = cacheInfos.get(limit);
//                                find = true;
//                                break;
//                            }
//                            if (!find) {
//                                for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
//                                    if (limit.minGlobalCount > 0) {
//                                        if (!cacheGlobal.containsKey(limit)) {
//                                            cacheGlobal.put(limit, 1);
//                                        } else if (cacheGlobal.get(limit) < limit.minGlobalCount &&
//                                                (limit.maxGlobalCount == -1 ||
//                                                        cacheGlobal.get(limit) < limit.maxGlobalCount)) {
//                                                            cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
//                                                        } else {
//                                                            continue;
//                                                        }
//                                    } else {
//                                        continue;
//                                    }
//                                    if (!cacheInfos.containsKey(limit)) {
//                                        cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
//                                    }
//                                    infos = cacheInfos.get(limit);
//                                    find = true;
//                                    break;
//                                }
//                            }
//                            if (!find) { // no limited
//                                for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
//                                    if (limit.maxLayerCount != -1 &&
//                                            cacheLayer.getOrDefault(limit, Integer.MAX_VALUE) == limit.maxLayerCount)
//                                        continue;
//                                    if (limit.maxGlobalCount != -1 &&
//                                            cacheGlobal.getOrDefault(limit, Integer.MAX_VALUE) == limit.maxGlobalCount)
//                                        continue;
//                                    if (!cacheInfos.containsKey(limit)) {
//                                        cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
//                                    }
//                                    if (cacheLayer.containsKey(limit)) {
//                                        cacheLayer.put(limit, cacheLayer.get(limit) + 1);
//                                    } else {
//                                        cacheLayer.put(limit, 1);
//                                    }
//                                    if (cacheGlobal.containsKey(limit)) {
//                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
//                                    } else {
//                                        cacheGlobal.put(limit, 1);
//                                    }
//                                    infos = ArrayUtils.addAll(infos, cacheInfos.get(limit));
//                                }
//                                for (TraceabilityPredicate.SimplePredicate common : predicate.common) {
//                                    if (!cacheInfos.containsKey(common)) {
//                                        cacheInfos.put(common,
//                                                common.candidates == null ? null : common.candidates.get());
//                                    }
//                                    infos = ArrayUtils.addAll(infos, cacheInfos.get(common));
//                                }
//                            }
//
//                            List<ItemStack> candidates = Arrays.stream(infos)
//                                    .filter(info -> info.getBlockState().getBlock() != Blocks.AIR).map(info -> {
//                                        IBlockState blockState = info.getBlockState();
//                                        MetaTileEntity metaTileEntity = info
//                                                .getTileEntity() instanceof IGregTechTileEntity ?
//                                                        ((IGregTechTileEntity) info.getTileEntity())
//                                                                .getMetaTileEntity() :
//                                                        null;
//                                        if (metaTileEntity != null) {
//                                            return metaTileEntity.getStackForm();
//                                        } else {
//                                            return new ItemStack(Item.getItemFromBlock(blockState.getBlock()), 1,
//                                                    blockState.getBlock().damageDropped(blockState));
//                                        }
//                                    }).collect(Collectors.toList());
//                            if (candidates.isEmpty()) continue;
//                            // check inventory
//                            ItemStack found = null;
//                            if (!player.isCreative()) {
//                                for (ItemStack itemStack : player.inventory.mainInventory) {
//                                    if (candidates.stream().anyMatch(candidate -> candidate.isItemEqual(itemStack)) &&
//                                            !itemStack.isEmpty() && itemStack.getItem() instanceof ItemBlock) {
//                                        found = itemStack.copy();
//                                        itemStack.setCount(itemStack.getCount() - 1);
//                                        break;
//                                    }
//                                }
//                                if (found == null) continue;
//                            } else {
//                                for (int i = candidates.size() - 1; i >= 0; i--) {
//                                    found = candidates.get(i).copy();
//                                    if (!found.isEmpty() && found.getItem() instanceof ItemBlock) {
//                                        break;
//                                    }
//                                    found = null;
//                                }
//                                if (found == null) continue;
//                            }
//                            ItemBlock itemBlock = (ItemBlock) found.getItem();
//                            IBlockState state = itemBlock.getBlock()
//                                    .getStateFromMeta(itemBlock.getMetadata(found.getMetadata()));
//                            blocks.put(pos, state);
//                            world.setBlockState(pos, state);
//                            TileEntity holder = world.getTileEntity(pos);
//                            if (holder instanceof IGregTechTileEntity igtte) {
//                                MTERegistry registry = GregTechAPI.mteManager
//                                        .getRegistry(found.getItem().getRegistryName().getNamespace());
//                                MetaTileEntity sampleMetaTileEntity = registry.getObjectById(found.getItemDamage());
//                                if (sampleMetaTileEntity != null) {
//                                    MetaTileEntity metaTileEntity = igtte.setMetaTileEntity(sampleMetaTileEntity);
//                                    metaTileEntity.onPlacement();
//                                    blocks.put(pos, metaTileEntity);
//                                    if (found.getTagCompound() != null) {
//                                        metaTileEntity.initFromItemStackData(found.getTagCompound());
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                z++;
//            }
//        }
//        EnumFacing[] facings = ArrayUtils.addAll(new EnumFacing[] { controllerBase.getFrontFacing() }, FACINGS); // follow
//                                                                                                                 // controller
//                                                                                                                 // first
//        blocks.forEach((pos, block) -> { // adjust facing
//            if (block instanceof MetaTileEntity) {
//                MetaTileEntity metaTileEntity = (MetaTileEntity) block;
//                boolean find = false;
//                for (EnumFacing enumFacing : facings) {
//                    if (metaTileEntity.isValidFrontFacing(enumFacing)) {
//                        if (!blocks.containsKey(pos.offset(enumFacing))) {
//                            metaTileEntity.setFrontFacing(enumFacing);
//                            find = true;
//                            break;
//                        }
//                    }
//                }
//                if (!find) {
//                    for (EnumFacing enumFacing : FACINGS) {
//                        if (world.isAirBlock(pos.offset(enumFacing)) && metaTileEntity.isValidFrontFacing(enumFacing)) {
//                            metaTileEntity.setFrontFacing(enumFacing);
//                            break;
//                        }
//                    }
//                }
//            }
//        });
    }

    public BlockInfo[][][] getPreview(int[] repetition) {
        return null;
//        Map<TraceabilityPredicate.SimplePredicate, BlockInfo[]> cacheInfos = new HashMap<>();
//        Map<TraceabilityPredicate.SimplePredicate, Integer> cacheGlobal = new HashMap<>();
//        Map<BlockPos, BlockInfo> blocks = new HashMap<>();
//        int minX = Integer.MAX_VALUE;
//        int minY = Integer.MAX_VALUE;
//        int minZ = Integer.MAX_VALUE;
//        int maxX = Integer.MIN_VALUE;
//        int maxY = Integer.MIN_VALUE;
//        int maxZ = Integer.MIN_VALUE;
//        for (int l = 0, x = 0; l < this.fingerLength; l++) {
//            for (int r = 0; r < repetition[l]; r++) {
//                // Checking single slice
//                Map<TraceabilityPredicate.SimplePredicate, Integer> cacheLayer = new HashMap<>();
//                for (int y = 0; y < this.thumbLength; y++) {
//                    for (int z = 0; z < this.palmLength; z++) {
//                        TraceabilityPredicate predicate = this.blockMatches[l][y][z];
//                        boolean find = false;
//                        BlockInfo[] infos = null;
//                        for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) { // check layer and
//                                                                                                // previewCount
//                            if (limit.minLayerCount > 0) {
//                                if (!cacheLayer.containsKey(limit)) {
//                                    cacheLayer.put(limit, 1);
//                                } else if (cacheLayer.get(limit) < limit.minLayerCount) {
//                                    cacheLayer.put(limit, cacheLayer.get(limit) + 1);
//                                } else {
//                                    continue;
//                                }
//                                if (cacheGlobal.getOrDefault(limit, 0) < limit.previewCount) {
//                                    if (!cacheGlobal.containsKey(limit)) {
//                                        cacheGlobal.put(limit, 1);
//                                    } else if (cacheGlobal.get(limit) < limit.previewCount) {
//                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
//                                    } else {
//                                        continue;
//                                    }
//                                }
//                            } else {
//                                continue;
//                            }
//                            if (!cacheInfos.containsKey(limit)) {
//                                cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
//                            }
//                            infos = cacheInfos.get(limit);
//                            find = true;
//                            break;
//                        }
//                        if (!find) { // check global and previewCount
//                            for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
//                                if (limit.minGlobalCount == -1 && limit.previewCount == -1) continue;
//                                if (cacheGlobal.getOrDefault(limit, 0) < limit.previewCount) {
//                                    if (!cacheGlobal.containsKey(limit)) {
//                                        cacheGlobal.put(limit, 1);
//                                    } else if (cacheGlobal.get(limit) < limit.previewCount) {
//                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
//                                    } else {
//                                        continue;
//                                    }
//                                } else if (limit.minGlobalCount > 0) {
//                                    if (!cacheGlobal.containsKey(limit)) {
//                                        cacheGlobal.put(limit, 1);
//                                    } else if (cacheGlobal.get(limit) < limit.minGlobalCount) {
//                                        cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
//                                    } else {
//                                        continue;
//                                    }
//                                } else {
//                                    continue;
//                                }
//                                if (!cacheInfos.containsKey(limit)) {
//                                    cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
//                                }
//                                infos = cacheInfos.get(limit);
//                                find = true;
//                                break;
//                            }
//                        }
//                        if (!find) { // check common with previewCount
//                            for (TraceabilityPredicate.SimplePredicate common : predicate.common) {
//                                if (common.previewCount > 0) {
//                                    if (!cacheGlobal.containsKey(common)) {
//                                        cacheGlobal.put(common, 1);
//                                    } else if (cacheGlobal.get(common) < common.previewCount) {
//                                        cacheGlobal.put(common, cacheGlobal.get(common) + 1);
//                                    } else {
//                                        continue;
//                                    }
//                                } else {
//                                    continue;
//                                }
//                                if (!cacheInfos.containsKey(common)) {
//                                    cacheInfos.put(common, common.candidates == null ? null : common.candidates.get());
//                                }
//                                infos = cacheInfos.get(common);
//                                find = true;
//                                break;
//                            }
//                        }
//                        if (!find) { // check without previewCount
//                            for (TraceabilityPredicate.SimplePredicate common : predicate.common) {
//                                if (common.previewCount == -1) {
//                                    if (!cacheInfos.containsKey(common)) {
//                                        cacheInfos.put(common,
//                                                common.candidates == null ? null : common.candidates.get());
//                                    }
//                                    infos = cacheInfos.get(common);
//                                    find = true;
//                                    break;
//                                }
//                            }
//                        }
//                        if (!find) { // check max
//                            for (TraceabilityPredicate.SimplePredicate limit : predicate.limited) {
//                                if (limit.previewCount != -1) {
//                                    continue;
//                                } else if (limit.maxGlobalCount != -1 || limit.maxLayerCount != -1) {
//                                    if (cacheGlobal.getOrDefault(limit, 0) < limit.maxGlobalCount) {
//                                        if (!cacheGlobal.containsKey(limit)) {
//                                            cacheGlobal.put(limit, 1);
//                                        } else {
//                                            cacheGlobal.put(limit, cacheGlobal.get(limit) + 1);
//                                        }
//                                    } else if (cacheLayer.getOrDefault(limit, 0) < limit.maxLayerCount) {
//                                        if (!cacheLayer.containsKey(limit)) {
//                                            cacheLayer.put(limit, 1);
//                                        } else {
//                                            cacheLayer.put(limit, cacheLayer.get(limit) + 1);
//                                        }
//                                    } else {
//                                        continue;
//                                    }
//                                }
//
//                                if (!cacheInfos.containsKey(limit)) {
//                                    cacheInfos.put(limit, limit.candidates == null ? null : limit.candidates.get());
//                                }
//                                infos = cacheInfos.get(limit);
//                                break;
//                            }
//                        }
//                        BlockInfo info = infos == null || infos.length == 0 ? BlockInfo.EMPTY : infos[0];
//                        BlockPos pos = setActualRelativeOffset(z, y, x, EnumFacing.NORTH, EnumFacing.UP, false);
//                        // TODO
//                        if (info.getTileEntity() instanceof MetaTileEntityHolder) {
//                            MetaTileEntityHolder holder = new MetaTileEntityHolder();
//                            holder.setMetaTileEntity(((MetaTileEntityHolder) info.getTileEntity()).getMetaTileEntity());
//                            holder.getMetaTileEntity().onPlacement();
//                            info = new BlockInfo(holder.getMetaTileEntity().getBlock().getDefaultState(), holder);
//                        }
//                        blocks.put(pos, info);
//                        minX = Math.min(pos.getX(), minX);
//                        minY = Math.min(pos.getY(), minY);
//                        minZ = Math.min(pos.getZ(), minZ);
//                        maxX = Math.max(pos.getX(), maxX);
//                        maxY = Math.max(pos.getY(), maxY);
//                        maxZ = Math.max(pos.getZ(), maxZ);
//                    }
//                }
//                x++;
//            }
//        }
//        BlockInfo[][][] result = (BlockInfo[][][]) Array.newInstance(BlockInfo.class, maxX - minX + 1, maxY - minY + 1,
//                maxZ - minZ + 1);
//        int finalMinX = minX;
//        int finalMinY = minY;
//        int finalMinZ = minZ;
//        blocks.forEach((pos, info) -> {
//            if (info.getTileEntity() instanceof MetaTileEntityHolder) {
//                MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) info.getTileEntity()).getMetaTileEntity();
//                boolean find = false;
//                for (EnumFacing enumFacing : FACINGS) {
//                    if (metaTileEntity.isValidFrontFacing(enumFacing)) {
//                        if (!blocks.containsKey(pos.offset(enumFacing))) {
//                            metaTileEntity.setFrontFacing(enumFacing);
//                            find = true;
//                            break;
//                        }
//                    }
//                }
//                if (!find) {
//                    for (EnumFacing enumFacing : FACINGS) {
//                        BlockInfo blockInfo = blocks.get(pos.offset(enumFacing));
//                        if (blockInfo != null && blockInfo.getBlockState().getBlock() == Blocks.AIR &&
//                                metaTileEntity.isValidFrontFacing(enumFacing)) {
//                            metaTileEntity.setFrontFacing(enumFacing);
//                            break;
//                        }
//                    }
//                }
//            }
//            result[pos.getX() - finalMinX][pos.getY() - finalMinY][pos.getZ() - finalMinZ] = info;
//        });
//        return result;
    }

    private GreggyBlockPos offsetFrom(GreggyBlockPos start, int aisleOffset, int stringOffset, int charOffset, @NotNull EnumFacing frontFacing,
                                @NotNull EnumFacing upFacing, boolean flip) {
        GreggyBlockPos pos = start.copy();
        pos.offset(structureDir[0].getRelativeFacing(frontFacing, upFacing, flip), aisleOffset);
        pos.offset(structureDir[1].getRelativeFacing(frontFacing, upFacing, flip), stringOffset);
        pos.offset(structureDir[2].getRelativeFacing(frontFacing, upFacing, flip), charOffset);
        return pos;
    }

    private GreggyBlockPos startPos(GreggyBlockPos controllerPos, EnumFacing frontFacing, EnumFacing upFacing, boolean flip) {
        // negate since the offsets are the opposite direction of structureDir
        return offsetFrom(controllerPos, -startOffset[0], -startOffset[1], -startOffset[2], frontFacing, upFacing, flip);
    }

    private BlockPos setActualRelativeOffset(int x, int y, int z, EnumFacing facing, EnumFacing upwardsFacing,
                                             boolean isFlipped) {
        int[] c0 = new int[] { x, y, z }, c1 = new int[3];
        if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
            EnumFacing of = facing == EnumFacing.DOWN ? upwardsFacing : upwardsFacing.getOpposite();
            for (int i = 0; i < 3; i++) {
                switch (structureDir[i].getActualFacing(of)) {
                    case UP -> c1[1] = c0[i];
                    case DOWN -> c1[1] = -c0[i];
                    case WEST -> c1[0] = -c0[i];
                    case EAST -> c1[0] = c0[i];
                    case NORTH -> c1[2] = -c0[i];
                    case SOUTH -> c1[2] = c0[i];
                }
            }
            int xOffset = upwardsFacing.getXOffset();
            int zOffset = upwardsFacing.getZOffset();
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
            if (isFlipped) {
                if (upwardsFacing == EnumFacing.NORTH || upwardsFacing == EnumFacing.SOUTH) {
                    c1[0] = -c1[0]; // flip X-axis
                } else {
                    c1[2] = -c1[2]; // flip Z-axis
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                switch (structureDir[i].getActualFacing(facing)) {
                    case UP -> c1[1] = c0[i];
                    case DOWN -> c1[1] = -c0[i];
                    case WEST -> c1[0] = -c0[i];
                    case EAST -> c1[0] = c0[i];
                    case NORTH -> c1[2] = -c0[i];
                    case SOUTH -> c1[2] = c0[i];
                }
            }
            if (upwardsFacing == EnumFacing.WEST || upwardsFacing == EnumFacing.EAST) {
                int xOffset = upwardsFacing == EnumFacing.WEST ? facing.rotateY().getXOffset() :
                        facing.rotateY().getOpposite().getXOffset();
                int zOffset = upwardsFacing == EnumFacing.WEST ? facing.rotateY().getZOffset() :
                        facing.rotateY().getOpposite().getZOffset();
                int tmp;
                if (xOffset == 0) {
                    tmp = c1[2];
                    c1[2] = zOffset > 0 ? -c1[1] : c1[1];
                    c1[1] = zOffset > 0 ? tmp : -tmp;
                } else {
                    tmp = c1[0];
                    c1[0] = xOffset > 0 ? -c1[1] : c1[1];
                    c1[1] = xOffset > 0 ? tmp : -tmp;
                }
            } else if (upwardsFacing == EnumFacing.SOUTH) {
                c1[1] = -c1[1];
                if (facing.getXOffset() == 0) {
                    c1[0] = -c1[0];
                } else {
                    c1[2] = -c1[2];
                }
            }
            if (isFlipped) {
                if (upwardsFacing == EnumFacing.NORTH || upwardsFacing == EnumFacing.SOUTH) {
                    if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
                        c1[0] = -c1[0]; // flip X-axis
                    } else {
                        c1[2] = -c1[2]; // flip Z-axis
                    }
                } else {
                    c1[1] = -c1[1]; // flip Y-axis
                }
            }
        }
        return new BlockPos(c1[0], c1[1], c1[2]);
    }
}
