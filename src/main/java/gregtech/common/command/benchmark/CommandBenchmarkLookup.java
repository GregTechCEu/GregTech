package gregtech.common.command.benchmark;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.GTLog;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import com.github.bsideup.jabel.Desugar;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToLongFunction;

public class CommandBenchmarkLookup extends CommandBase {

    @Override
    public String getName() {
        return "lookup";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "gregtech.command.benchmark.lookup.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        int trials = 100;
        if (args.length != 0) {
            try {
                trials = Integer.parseInt(args[0]);
                if (trials <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ignored) {
                throw new WrongUsageException("gregtech.command.benchmark.lookup.usage");
            }
        }
        Object2ObjectOpenHashMap<RecipeMap<?>, ObjectArrayList<TrialResults>> nsTrialTimes = new Object2ObjectOpenHashMap<>();
        Object2ObjectOpenHashMap<RecipeMap<?>, List<Recipe>> recipeLists = new Object2ObjectOpenHashMap<>();
        AtomicInteger failureCount = new AtomicInteger();
        GTLog.logger.info("[Benchmarking] Starting recipe lookup benchmarking...");
        for (int i = 0; i < trials; i++) {
            for (RecipeMap<?> map : RecipeMap.getRecipeMaps()) {
                if (recipeLists.computeIfAbsent(map, m -> new ObjectArrayList<>(m.getRecipeList())).isEmpty()) continue;
                nsTrialTimes.computeIfAbsent(map, m -> new ObjectArrayList<>())
                        .add(trial(recipeLists.get(map), (v, it, f) -> {
                            Recipe r = map.findRecipe(v, it, f);
                            return r == null ? Collections.emptyList() : ImmutableList.of(r);
                        }, failureCount));
            }
            if ((i > 1) && ((i & (i - 1)) == 0)) {
                GTLog.logger.info("[Benchmarking] {}th trial complete...", i);
            }
        }
        GTLog.logger.info("[Benchmarking] Benchmarking complete. Outputting results:");
        Object2ObjectOpenHashMap<RecipeMap<?>, BenchmarkResults> resultsCache = new Object2ObjectOpenHashMap<>();
        for (var entry : nsTrialTimes.entrySet()) {
            BenchmarkResults results = composeResults(entry.getValue());
            resultsCache.put(entry.getKey(), results);
            GTLog.logger.info("[Benchmarking] Recipe Map {}, measurements in nanoseconds:",
                    entry.getKey().getLocalizedName());
            GTLog.logger.info("[Benchmarking] - Characteristic numbers for No Match: {}", results.noMatchTest());
            GTLog.logger.info("[Benchmarking] - Characteristic numbers for One Match Exact: {}",
                    results.oneMatchExactTest());
            GTLog.logger.info("[Benchmarking] - Characteristic numbers for Three Match Excess: {}",
                    results.threeMatchExcessTest());
        }
        sender.sendMessage(new TextComponentTranslation("gregtech.command.benchmark.lookup.success")
                .setStyle(new Style().setColor(TextFormatting.GREEN)));
        sender.sendMessage(
                new TextComponentTranslation("gregtech.command.benchmark.lookup.failures", failureCount.get())
                        .setStyle(new Style().setColor(TextFormatting.RED)));
        try (FileWriter writer = new FileWriter("benchmark-lookup-results.csv")) {
            writer.append("Recipe Map,Trial Type,Minimum,Q1,Median,Q3,Maximum\n");
            for (var entry : resultsCache.entrySet()) {
                writer.append(entry.getKey().getLocalizedName()).append(',').append("No Match");
                for (double d : entry.getValue().noMatchTest()) {
                    writer.append(',').append(String.valueOf(d));
                }
                writer.append('\n');
                writer.append(entry.getKey().getLocalizedName()).append(',').append("One Match Exact");
                for (double d : entry.getValue().oneMatchExactTest()) {
                    writer.append(',').append(String.valueOf(d));
                }
                writer.append('\n');
                writer.append(entry.getKey().getLocalizedName()).append(',').append("Three Match Excess");
                for (double d : entry.getValue().threeMatchExcessTest()) {
                    writer.append(',').append(String.valueOf(d));
                }
                writer.append('\n');
            }
            GTLog.logger.info("[Benchmarking] Output saved to csv file 'benchmark-lookup-results.csv'");
            sender.sendMessage(new TextComponentTranslation("gregtech.command.benchmark.lookup.written")
                    .setStyle(new Style().setColor(TextFormatting.GREEN)));
        } catch (IOException e) {
            GTLog.logger.info("[Benchmarking] Failed to output to csv file 'benchmark-lookup-results.csv'");
        }
    }

    private TrialResults trial(List<Recipe> recipeSpace, RecipeLookupFunction function,
                               AtomicInteger failureCount) throws CommandException {
        Recipe[] sample = new Recipe[10];
        for (int i = 0; i < 10; i++) {
            double r = Math.random();
            sample[i] = recipeSpace.get((int) (r * recipeSpace.size()));
        }
        // no match trial
        Set<GTRecipeInput> seen = new ObjectOpenHashSet<>();
        List<ItemStack> items = new ObjectArrayList<>();
        List<FluidStack> fluids = new ObjectArrayList<>();
        for (Recipe r : sample) {
            // if adding an item input would lead to recipe matching, do not add it.
            if (r.getInputs().size() > 1) {
                r.getInputs().stream().filter(i -> !seen.contains(i)).findAny()
                        .map(GTRecipeInput::getInputStacks)
                        .map(arr -> arr[0])
                        .ifPresent(items::add);
                for (Recipe value : sample) {
                    if (value.matches(false, items, fluids)) {
                        items.remove(items.size() - 1);
                    }
                }
            }
            // if adding a fluid input would lead to recipe matching, do not add it
            if (r.getFluidInputs().size() > 1) {
                r.getFluidInputs().stream().filter(i -> !seen.contains(i)).findAny()
                        .map(GTRecipeInput::getInputFluidStack)
                        .ifPresent(fluids::add);
                for (Recipe recipe : sample) {
                    if (recipe.matches(false, items, fluids)) {
                        fluids.remove(fluids.size() - 1);
                    }
                }
            }
            // prevent randomly adding inputs in the future that would lead to matching a recipe
            seen.addAll(r.getInputs());
            seen.addAll(r.getFluidInputs());
        }
        long start = System.nanoTime();
        Collection<Recipe> out = function.find(Long.MAX_VALUE, items, fluids);
        // in a real situation, any outputs would be run through a count match until the first success if found.
        // run this matching while timing to penalize returning too many possible matches
        for (Recipe r : out) {
            if (r.matches(false, items, fluids)) {
                break;
            }
        }
        long timeNoMatch = System.nanoTime() - start;
        for (Recipe r : sample) {
            if (out.contains(r)) {
                failureCount.incrementAndGet();
                GTLog.logger.info("[Benchmarking] Recipe sample {} has failed the no match test.", (Object) sample);
            }
        }

        // three match excess trial
        for (int i = 0; i < 3; i++) {
            for (GTRecipeInput input : sample[i].getInputs()) {
                ItemStack stack = input.getInputStacks()[0].copy();
                stack.setCount(input.getAmount());
                items.add(stack);
            }
            for (GTRecipeInput input : sample[i].getFluidInputs()) {
                FluidStack stack = input.getInputFluidStack().copy();
                stack.amount = input.getAmount();
                fluids.add(stack);
            }
        }
        start = System.nanoTime();
        out = function.find(Long.MAX_VALUE, items, fluids);
        // in a real situation, any outputs would be run through a count match.
        // run this matching while timing to penalize returning too many possible matches
        for (Recipe r : out) {
            if (r.matches(false, items, fluids)) {
                break;
            }
        }
        long timeThreeExcess = System.nanoTime() - start;
        // re-enable this code block once lookup returns proper lists of matching recipes
        // for (int i = 0; i < 3; i++) {
        // Recipe r = sample[i];
        // if (!out.contains(r)) {
        // throw new CommandException("Something in the benchmark's three match test is wrong! Report this to mod
        // authors with context.");
        // }
        // }

        // one match exact trial
        items.clear();
        fluids.clear();
        for (GTRecipeInput input : sample[0].getInputs()) {
            ItemStack stack = input.getInputStacks()[0].copy();
            stack.setCount(input.getAmount());
            items.add(stack);
        }
        for (GTRecipeInput input : sample[0].getFluidInputs()) {
            FluidStack stack = input.getInputFluidStack().copy();
            stack.amount = input.getAmount();
            fluids.add(stack);
        }
        start = System.nanoTime();
        out = function.find(Long.MAX_VALUE, items, fluids);
        // in a real situation, any outputs would be run through a count match.
        // run this matching while timing to penalize returning too many possible matches
        for (Recipe r : out) {
            if (r.matches(false, items, fluids)) {
                break;
            }
        }
        long timeOneExact = System.nanoTime() - start;
        if (!out.contains(sample[0])) {
            failureCount.incrementAndGet();
            GTLog.logger.info("[Benchmarking] Recipe sample {} has failed the exact match test.", (Object) sample);
        }
        return new TrialResults(timeNoMatch, timeOneExact, timeThreeExcess);
    }

    private BenchmarkResults composeResults(List<TrialResults> results) {
        return new BenchmarkResults(representativeNumbers(results, TrialResults::noMatchTest),
                representativeNumbers(results, TrialResults::oneMatchExactTest),
                representativeNumbers(results, TrialResults::threeMatchExcessTest));
    }

    private double[] representativeNumbers(List<TrialResults> results, ToLongFunction<TrialResults> func) {
        results.sort(Comparator.comparingLong(func));
        double[] numbers = new double[5];
        numbers[0] = func.applyAsLong(results.get(0));
        numbers[4] = func.applyAsLong(results.get(results.size() - 1));
        int offset = 0;
        if (results.size() % 2 == 1) {
            numbers[2] = func.applyAsLong(results.get(results.size() / 2));
            offset = 1;
        } else {
            numbers[2] = (func.applyAsLong(results.get(results.size() / 2 - 1)) +
                    func.applyAsLong(results.get(results.size() / 2))) / 2d;
        }
        int s = results.size() / 2;
        if (s % 2 == 1) {
            numbers[1] = func.applyAsLong(results.get(s / 2));
            numbers[3] = func.applyAsLong(results.get(s + offset + s / 2));
        } else {
            numbers[1] = (func.applyAsLong(results.get(s / 2 - 1)) + func.applyAsLong(results.get(s / 2))) / 2d;
            numbers[3] = (func.applyAsLong(results.get(s / 2 - 1)) + func.applyAsLong(results.get(s / 2))) / 2d;
        }
        return numbers;
    }

    interface RecipeLookupFunction {

        Collection<Recipe> find(long voltage, List<ItemStack> items, List<FluidStack> fluids);
    }

    @Desugar
    record TrialResults(long noMatchTest, long oneMatchExactTest, long threeMatchExcessTest) {}

    @Desugar
    record BenchmarkResults(double[] noMatchTest, double[] oneMatchExactTest, double[] threeMatchExcessTest) {}
}
