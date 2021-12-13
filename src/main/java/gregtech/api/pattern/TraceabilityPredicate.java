package gregtech.api.pattern;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.BlockInfo;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.BlockWireCoil2;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TraceabilityPredicate {

    // Allow any block.
    public static Supplier<TraceabilityPredicate> ANY = () -> new TraceabilityPredicate((state)->true);
    // Allow the air block.
    public static Supplier<TraceabilityPredicate> AIR = () -> new TraceabilityPredicate(blockWorldState -> blockWorldState.getBlockState().getBlock().isAir(blockWorldState.getBlockState(), blockWorldState.getWorld(), blockWorldState.getPos()));
    // Allow all heating coils, and require them to have the same type.
    public static Supplier<TraceabilityPredicate> HEATING_COILS = () -> new TraceabilityPredicate(blockWorldState -> {
        IBlockState blockState = blockWorldState.getBlockState();
        if ((blockState.getBlock() instanceof BlockWireCoil)) {
            BlockWireCoil blockWireCoil = (BlockWireCoil) blockState.getBlock();
            BlockWireCoil.CoilType coilType = blockWireCoil.getState(blockState);
            Object currentCoilType = blockWorldState.getMatchContext().getOrPut("CoilType", coilType);
            if (!currentCoilType.toString().equals(coilType.getName())) {
                blockWorldState.setError(new PatternStringError("gregtech.multiblock.pattern.error.coils"));
                return false;
            }
            blockWorldState.getMatchContext().getOrPut("VABlock", new LinkedList<>()).add(blockWorldState.getPos());
            return true;
        } else if ((blockState.getBlock() instanceof BlockWireCoil2)) {
            BlockWireCoil2 blockWireCoil = (BlockWireCoil2) blockState.getBlock();
            BlockWireCoil2.CoilType2 coilType = blockWireCoil.getState(blockState);
            Object currentCoilType = blockWorldState.getMatchContext().getOrPut("CoilType", coilType);
            if (!currentCoilType.toString().equals(coilType.getName())) {
                blockWorldState.setError(new PatternStringError("gregtech.multiblock.pattern.error.coils"));
                return false;
            }
            blockWorldState.getMatchContext().getOrPut("VABlock", new LinkedList<>()).add(blockWorldState.getPos());
            return true;
        }
        return false;
    }, ()-> ArrayUtils.addAll(
            Arrays.stream(BlockWireCoil.CoilType.values()).map(type->new BlockInfo(MetaBlocks.WIRE_COIL.getState(type), null)).toArray(BlockInfo[]::new),
            Arrays.stream(BlockWireCoil2.CoilType2.values()).map(type->new BlockInfo(MetaBlocks.WIRE_COIL2.getState(type), null)).toArray(BlockInfo[]::new)))
            .addTooltips("gregtech.multiblock.pattern.error.coils");


    public final List<SimplePredicate> common = new ArrayList<>();
    public final List<SimplePredicate> limited = new ArrayList<>();
    protected boolean isCenter;

    public TraceabilityPredicate() {}

    public TraceabilityPredicate(TraceabilityPredicate predicate) {
        common.addAll(predicate.common);
        limited.addAll(predicate.limited);
        isCenter = predicate.isCenter;
    }

    public TraceabilityPredicate(Predicate<BlockWorldState> predicate, Supplier<BlockInfo[]> candidates) {
        common.add(new SimplePredicate(predicate, candidates));
    }

    public TraceabilityPredicate(Predicate<BlockWorldState> predicate) {
        this(predicate, null);
    }

    /**
     * Mark it as the controller of this multi. Normally you won't call it yourself. Use {@link MultiblockControllerBase#selfPredicate()} plz.
     */
    public TraceabilityPredicate setCenter() {
        isCenter = true;
        return this;
    }

    public TraceabilityPredicate sort() {
        limited.sort((a, b)->{
            return ((a.minLayerCount + 1) * 100 + a.minGlobalCount) - ((b.minLayerCount + 1) * 100 + b.minGlobalCount);
        });
        return this;
    }

    /**
     * Add tooltips for candidates. They are shown in JEI Pages.
     */
    public TraceabilityPredicate addTooltips(String... tips) {
        if (tips.length > 0) {
            List<String> tooltips = Arrays.stream(tips).collect(Collectors.toList());
            common.forEach(predicate -> {
                if (predicate.toolTips == null) {
                    predicate.toolTips = new ArrayList<>();
                }
                predicate.toolTips.addAll(tooltips);
            });
            limited.forEach(predicate -> {
                if (predicate.toolTips == null) {
                    predicate.toolTips = new ArrayList<>();
                }
                predicate.toolTips.addAll(tooltips);
            });
        }
        return this;
    }

    /**
     * Set the minimum number of candidate blocks.
     */
    public TraceabilityPredicate setMinGlobalLimited(int min) {
        limited.addAll(common);
        common.clear();
        for (SimplePredicate predicate : limited) {
            predicate.minGlobalCount = min;
        }
        return this;
    }

    public TraceabilityPredicate setMinGlobalLimited(int min, int previewCount) {
        return this.setMinGlobalLimited(min).setPreviewCount(previewCount);
    }

    /**
     * Set the maximum number of candidate blocks.
     */
    public TraceabilityPredicate setMaxGlobalLimited(int max) {
        limited.addAll(common);
        common.clear();
        for (SimplePredicate predicate : limited) {
            predicate.maxGlobalCount = max;
        }
        return this;
    }

    public TraceabilityPredicate setMaxGlobalLimited(int max, int previewCount) {
        return this.setMaxGlobalLimited(max).setPreviewCount(previewCount);
    }

    /**
     * Set the minimum number of candidate blocks for each aisle layer.
     */
    public TraceabilityPredicate setMinLayerLimited(int min) {
        limited.addAll(common);
        common.clear();
        for (SimplePredicate predicate : limited) {
            predicate.minLayerCount = min;
        }
        return this;
    }

    public TraceabilityPredicate setMinLayerLimited(int min, int previewCount) {
        return this.setMinLayerLimited(min).setPreviewCount(previewCount);
    }

    /**
     * Set the maximum number of candidate blocks for each aisle layer.
     */
    public TraceabilityPredicate setMaxLayerLimited(int max) {
        limited.addAll(common);
        common.clear();
        for (SimplePredicate predicate : limited) {
            predicate.maxLayerCount = max;
        }
        return this;
    }

    public TraceabilityPredicate setMaxLayerLimited(int max, int previewCount) {
        return this.setMaxLayerLimited(max).setPreviewCount(previewCount);
    }

    /**
     * Set the number of it appears in JEI pages. It only affects JEI preview. (The specific number)
     */
    public TraceabilityPredicate setPreviewCount(int count) {
        common.forEach(predicate -> predicate.previewCount = count);
        limited.forEach(predicate -> predicate.previewCount = count);
        return this;
    }

    public boolean test(BlockWorldState blockWorldState) {
        boolean flag = false;
        for (SimplePredicate predicate : limited) {
            if (predicate.testLimited(blockWorldState)) {
                flag = true;
            }
        }
        return flag || common.stream().anyMatch(predicate->predicate.test(blockWorldState));
    }

    public TraceabilityPredicate or(TraceabilityPredicate other) {
        if (other != null) {
            TraceabilityPredicate newPredicate = new TraceabilityPredicate(this);
            newPredicate.common.addAll(other.common);
            newPredicate.limited.addAll(other.limited);
            return newPredicate;
        }
        return this;
    }

    public static class SimplePredicate{
        public final Supplier<BlockInfo[]> candidates;

        public final Predicate<BlockWorldState> predicate;

        private List<String> toolTips;

        public int minGlobalCount = -1;
        public int maxGlobalCount = -1;
        public int minLayerCount = -1;
        public int maxLayerCount = -1;

        public int previewCount = -1;

        public SimplePredicate(Predicate<BlockWorldState> predicate, Supplier<BlockInfo[]> candidates) {
            this.predicate = predicate;
            this.candidates = candidates;
        }

        @SideOnly(Side.CLIENT)
        public List<String> getToolTips() {
            List<String> result = new ArrayList<>();
            if (toolTips != null) {
                toolTips.forEach(tip->result.add(I18n.format(tip)));
            }
            if (minGlobalCount == maxGlobalCount && maxGlobalCount != -1) {
                result.add(I18n.format("gregtech.multiblock.pattern.error.limited_exact", minGlobalCount));
            } else if (minGlobalCount != maxGlobalCount && minGlobalCount != -1 && maxGlobalCount != -1) {
                result.add(I18n.format("gregtech.multiblock.pattern.error.limited_within", minGlobalCount, maxGlobalCount));
            } else {
                if (minGlobalCount != -1) {
                    result.add(I18n.format("gregtech.multiblock.pattern.error.limited.1", minGlobalCount));
                }
                if (maxGlobalCount != -1) {
                    result.add(I18n.format("gregtech.multiblock.pattern.error.limited.0", maxGlobalCount));
                }
            }
            if (minLayerCount != -1) {
                result.add(I18n.format("gregtech.multiblock.pattern.error.limited.3", minLayerCount));
            }
            if (maxLayerCount != -1) {
                result.add(I18n.format("gregtech.multiblock.pattern.error.limited.2", maxLayerCount));
            }
            return result;
        }

        public boolean test(BlockWorldState blockWorldState) {
            return predicate.test(blockWorldState);
        }

        public boolean testLimited(BlockWorldState blockWorldState) {
            return testGlobal(blockWorldState) && testLayer(blockWorldState);
        }

        public boolean testGlobal(BlockWorldState blockWorldState) {
            if (minGlobalCount == -1 && maxGlobalCount == -1) return true;
            Integer count = blockWorldState.globalCount.get(this);
            boolean base = predicate.test(blockWorldState);
            count = (count == null ? 0 : count) + (base ? 1 : 0);
            blockWorldState.globalCount.put(this, count);
            if (maxGlobalCount == -1 || count <= maxGlobalCount) return base;
            blockWorldState.setError(new SinglePredicateError(this, 0));
            return false;
        }

        public boolean testLayer(BlockWorldState blockWorldState) {
            if (minLayerCount == -1 && maxLayerCount == -1) return true;
            Integer count = blockWorldState.layerCount.get(this);
            boolean base = predicate.test(blockWorldState);
            count = (count == null ? 0 : count) + (base ? 1 : 0);
            blockWorldState.layerCount.put(this, count);
            if (maxLayerCount == -1 || count <= maxLayerCount) return base;
            blockWorldState.setError(new SinglePredicateError(this, 2));
            return false;
        }

        public List<ItemStack> getCandidates() {
            return Arrays.stream(this.candidates.get()).filter(info -> info.getBlockState().getBlock() != Blocks.AIR).map(info->{
                IBlockState blockState = info.getBlockState();
                MetaTileEntity metaTileEntity = info.getTileEntity() instanceof MetaTileEntityHolder ? ((MetaTileEntityHolder) info.getTileEntity()).getMetaTileEntity() : null;
                if (metaTileEntity != null) {
                    return metaTileEntity.getStackForm();
                } else {
                    return new ItemStack(Item.getItemFromBlock(blockState.getBlock()), 1, blockState.getBlock().damageDropped(blockState));
                }
            }).collect(Collectors.toList());
        }
    }

    public static class SinglePredicateError extends PatternError {
        public final SimplePredicate predicate;
        public final int type;

        public SinglePredicateError(SimplePredicate predicate, int type) {
            this.predicate = predicate;
            this.type = type;
        }

        @Override
        public List<List<ItemStack>> getCandidates() {
            return Collections.singletonList(predicate.getCandidates());
        }

        @SideOnly(Side.CLIENT)
        @Override
        public String getErrorInfo() {
            int number = -1;
            if (type == 0) number = predicate.maxGlobalCount;
            if (type == 1) number = predicate.minGlobalCount;
            if (type == 2) number = predicate.maxLayerCount;
            if (type == 3) number = predicate.minLayerCount;
            return I18n.format("gregtech.multiblock.pattern.error.limited." + type, number);
        }
    }

}
