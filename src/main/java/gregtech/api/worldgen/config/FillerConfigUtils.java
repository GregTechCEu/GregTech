package gregtech.api.worldgen.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.unification.ore.StoneTypes;
import gregtech.api.util.GTUtility;
import gregtech.api.util.WorldBlockPredicate;
import gregtech.api.worldgen.filler.FillerEntry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class FillerConfigUtils {

    public static FillerEntry createBlockStateFiller(JsonElement element) {
        if (element instanceof JsonPrimitive) {
            String stringDeclaration = element.getAsString();
            return createSimpleFiller(stringDeclaration);

        } else if (element instanceof JsonObject) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("block")) {
                IBlockState stateDefinition = PredicateConfigUtils.parseBlockStateDefinition(object);
                return FillerEntry.createSimpleFiller(stateDefinition);
            }
            Preconditions.checkArgument(object.has("type"), "Missing required type for block state predicate");
            String predicateType = object.get("type").getAsString();
            switch (predicateType) {
                case "weight_random":
                    return createWeightRandomStateFiller(object);
                case "state_match":
                    return createStateMatchFiller(object);
                default:
                    throw new IllegalArgumentException("Unknown filler match type: " + predicateType);
            }
        } else {
            throw new IllegalArgumentException("Unknown block state type " + element);
        }
    }

    private static FillerEntry createSimpleFiller(String stringDeclaration) {
        if (stringDeclaration.startsWith("block:")) {
            Block block = OreConfigUtils.getBlockByName(stringDeclaration.substring(6));
            return FillerEntry.createSimpleFiller(block.getDefaultState());

        } else if (stringDeclaration.startsWith("fluid:")) {
            String fluidName = stringDeclaration.substring(6);
            Fluid fluid = FluidRegistry.getFluid(fluidName);
            Preconditions.checkNotNull(fluid, "Fluid not found with name %s", fluidName);
            Preconditions.checkNotNull(fluid.getBlock(), "Block is not defined for fluid %s", fluidName);
            return FillerEntry.createSimpleFiller(fluid.getBlock().getDefaultState());

        } else if (stringDeclaration.startsWith("ore:")) {
            Map<StoneType, IBlockState> blockStateMap = OreConfigUtils.getOreStateMap(stringDeclaration);
            return new OreFilterEntry(blockStateMap);

        } else if (stringDeclaration.startsWith("ore_dict:")) {
            String oreDictName = stringDeclaration.substring(9);
            IBlockState firstBlock = OreConfigUtils.getOreDictBlocks(oreDictName).get(0);
            return FillerEntry.createSimpleFiller(firstBlock);

        } else {
            throw new IllegalArgumentException("Unknown string block state declaration: " + stringDeclaration);
        }
    }

    private static FillerEntry createStateMatchFiller(JsonObject object) {
        JsonArray valuesArray = object.get("values").getAsJsonArray();
        JsonElement defaultElement = object.get("default");
        ArrayList<Pair<WorldBlockPredicate, FillerEntry>> matchers = new ArrayList<>();

        for (JsonElement valueDefinition : valuesArray) {
            Preconditions.checkArgument(valueDefinition.isJsonObject(), "Found invalid value definition: %s", valueDefinition.toString());
            JsonObject valueObject = valueDefinition.getAsJsonObject();
            WorldBlockPredicate predicate = PredicateConfigUtils.createBlockStatePredicate(valueObject.get("predicate"));
            FillerEntry filler = createBlockStateFiller(valueObject.get("value"));
            matchers.add(Pair.of(predicate, filler));
        }

        if (!defaultElement.isJsonNull()) {
            FillerEntry filler = createBlockStateFiller(defaultElement);
            WorldBlockPredicate predicate = (state, world, pos) -> true;
            matchers.add(Pair.of(predicate, filler));
        } else {
            WorldBlockPredicate predicate = (state, world, pos) -> true;
            FillerEntry fillerEntry = matchers.iterator().next().getRight();
            matchers.add(Pair.of(predicate, fillerEntry));
        }
        return new BlockStateMatcherEntry(matchers);
    }

    private static FillerEntry createWeightRandomStateFiller(JsonObject object) {
        JsonArray values = object.get("values").getAsJsonArray();
        ArrayList<Pair<Integer, FillerEntry>> randomList = new ArrayList<>();

        for (JsonElement randomElement : values) {
            JsonObject randomObject = randomElement.getAsJsonObject();
            int weight = randomObject.get("weight").getAsInt();
            Preconditions.checkArgument(weight > 0, "Invalid weight: %d", weight);
            FillerEntry filler = createBlockStateFiller(randomObject.get("value"));
            randomList.add(Pair.of(weight, filler));
        }

        return new WeightRandomMatcherEntry(randomList);
    }

    private static class OreFilterEntry implements FillerEntry {

        private final Map<StoneType, IBlockState> blockStateMap;
        private final ImmutableSet<IBlockState> allowedStates;
        private final StoneType defaultValue;

        public OreFilterEntry(Map<StoneType, IBlockState> blockStateMap) {
            this.blockStateMap = blockStateMap;
            this.defaultValue = blockStateMap.containsKey(StoneTypes.STONE) ? StoneTypes.STONE : blockStateMap.keySet().iterator().next();
            this.allowedStates = ImmutableSet.copyOf(blockStateMap.values());
        }

        @Override
        public IBlockState apply(IBlockState source, IBlockAccess blockAccess, BlockPos blockPos) {
            StoneType stoneType = StoneType.computeStoneType(source, blockAccess, blockPos);
            return blockStateMap.get(stoneType == null ? defaultValue : stoneType);
        }

        @Override
        public List<FillerEntry> getSubEntries() {
            return Collections.emptyList();
        }

        @Override
        public Set<IBlockState> getPossibleResults() {
            return allowedStates;
        }
    }

    private static class BlockStateMatcherEntry implements FillerEntry {

        private final List<Pair<WorldBlockPredicate, FillerEntry>> matchers;
        private final ImmutableList<FillerEntry> subEntries;
        private final ImmutableList<IBlockState> blockStates;

        public BlockStateMatcherEntry(List<Pair<WorldBlockPredicate, FillerEntry>> matchers) {
            this.matchers = matchers;
            ImmutableList.Builder<FillerEntry> entryBuilder = ImmutableList.builder();
            ImmutableList.Builder<IBlockState> stateBuilder = ImmutableList.builder();
            for (Pair<WorldBlockPredicate, FillerEntry> matcher : matchers) {
                entryBuilder.add(matcher.getRight());
                stateBuilder.addAll(matcher.getRight().getPossibleResults());
            }
            this.subEntries = entryBuilder.build();
            this.blockStates = stateBuilder.build();
        }

        @Override
        public IBlockState apply(IBlockState source, IBlockAccess blockAccess, BlockPos blockPos) {
            for (Pair<WorldBlockPredicate, FillerEntry> matcher : matchers) {
                if (matcher.getLeft().test(source, blockAccess, blockPos)) {
                    return matcher.getRight().apply(source, blockAccess, blockPos);
                }
            }
            return Blocks.AIR.getDefaultState();
        }

        @Override
        public List<FillerEntry> getSubEntries() {
            return subEntries;
        }

        @Override
        public Collection<IBlockState> getPossibleResults() {
            return blockStates;
        }
    }

    private static class WeightRandomMatcherEntry implements FillerEntry {

        private final List<Pair<Integer, FillerEntry>> randomList;
        private final ImmutableList<FillerEntry> subEntries;
        private final ImmutableList<IBlockState> blockStates;

        public WeightRandomMatcherEntry(List<Pair<Integer, FillerEntry>> randomList) {
            this.randomList = randomList;
            ImmutableList.Builder<FillerEntry> entryBuilder = ImmutableList.builder();
            ImmutableList.Builder<IBlockState> stateBuilder = ImmutableList.builder();
            for (Pair<Integer, FillerEntry> randomEntry : randomList) {
                entryBuilder.add(randomEntry.getRight());
                stateBuilder.addAll(randomEntry.getRight().getPossibleResults());
            }
            this.subEntries = entryBuilder.build();
            this.blockStates = stateBuilder.build();
        }

        @Override
        public IBlockState apply(IBlockState source, IBlockAccess blockAccess, BlockPos blockPos) {
            int functionIndex = GTUtility.getRandomItem(randomList, randomList.size());
            FillerEntry randomFunction = randomList.get(functionIndex).getValue();
            return randomFunction.apply(source, blockAccess, blockPos);
        }

        @Override
        public List<FillerEntry> getSubEntries() {
            return subEntries;
        }

        @Override
        public Collection<IBlockState> getPossibleResults() {
            return blockStates;
        }

        @Override
        public List<Pair<Integer, FillerEntry>> getEntries() {
            return randomList;
        }
    }
}
