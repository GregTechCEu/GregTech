package gregtech.api.pattern;

import gregtech.api.util.BlockInfo;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.BlockWireCoil2;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.VariantActiveBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TraceabilityPredicate {

    public static TraceabilityPredicate ANY = new TraceabilityPredicate((state)->true);
    public static TraceabilityPredicate AIR = new TraceabilityPredicate(blockWorldState -> blockWorldState.getBlockState().getBlock().isAir(blockWorldState.getBlockState(), blockWorldState.getWorld(), blockWorldState.getPos()));
    public static TraceabilityPredicate HEATING_COILS = new TraceabilityPredicate(blockWorldState -> {
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
            Arrays.stream(BlockWireCoil2.CoilType2.values()).map(type->new BlockInfo(MetaBlocks.WIRE_COIL2.getState(type), null)).toArray(BlockInfo[]::new)));


    protected final LinkedList<SimplePredicate> common = new LinkedList<>();
    protected final LinkedList<SimplePredicate> limited = new LinkedList<>();
    protected boolean isCenter;

    public TraceabilityPredicate(Predicate<BlockWorldState> predicate, Supplier<BlockInfo[]> candidates) {
        common.add(new SimplePredicate(predicate, candidates));
    }

    public TraceabilityPredicate(Predicate<BlockWorldState> predicate) {
        this(predicate, null);
    }

    public TraceabilityPredicate setCenter() {
        isCenter = true;
        return this;
    }

    public TraceabilityPredicate setMinGlobalLimited(int min) {
        limited.addAll(common);
        common.clear();
        for (SimplePredicate predicate : limited) {
            predicate.minGlobalCount = min;
        }
        return this;
    }

    public TraceabilityPredicate setMaxGlobalLimited(int max) {
        limited.addAll(common);
        common.clear();
        for (SimplePredicate predicate : limited) {
            predicate.maxGlobalCount = max;
        }
        return this;
    }

    public TraceabilityPredicate setMinLayerLimited(int layer) {
        limited.addAll(common);
        common.clear();
        for (SimplePredicate predicate : limited) {
            predicate.minLayerCount = layer;
        }
        return this;
    }

    public TraceabilityPredicate setMaxLayerLimited(int layer) {
        limited.addAll(common);
        common.clear();
        for (SimplePredicate predicate : limited) {
            predicate.maxLayerCount = layer;
        }
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
            common.addAll(other.common);
            limited.addAll(other.limited);
        }
        return this;
    }

    protected static class SimplePredicate{
        public final Supplier<BlockInfo[]> candidates;

        public final Predicate<BlockWorldState> predicate;

        public int minGlobalCount = -1;
        public int maxGlobalCount = -1;
        public int minLayerCount = -1;
        public int maxLayerCount = -1;

        public SimplePredicate(Predicate<BlockWorldState> predicate, Supplier<BlockInfo[]> candidates) {
            this.predicate = predicate;
            this.candidates = candidates;
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
    }

    public static class SinglePredicateError extends PatternError {
        public final SimplePredicate predicate;
        public final int type;

        public SinglePredicateError(SimplePredicate predicate, int type) {
            this.predicate = predicate;
            this.type = type;
        }

        @Override
        public List<ItemStack> getCandidates() {
            return getCandidates(predicate);
        }

        @SideOnly(Side.CLIENT)
        @Override
        public String getErrorInfo() {
            int number = -1;
            if (type == 0) number = predicate.maxGlobalCount;
            if (type == 1) number = predicate.minGlobalCount;
            if (type == 2) number = predicate.maxLayerCount;
            if (type == 3) number = predicate.minLayerCount;
            List<ItemStack> candidates = getCandidates();
            StringBuilder builder = new StringBuilder();
            if (!candidates.isEmpty()) {
                builder.append(candidates.get(0).getDisplayName());
            }
            if (candidates.size() > 1) {
                builder.append("...");
            }
            return I18n.format("gregtech.multiblock.pattern.error.limited." + type, builder.toString(), number);
        }
    }

}
