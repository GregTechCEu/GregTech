package gregtech.integration.exnihilo.recipes;

import gregtech.api.unification.OreDictUnifier;
import gregtech.integration.IntegrationModule;
import gregtech.integration.exnihilo.ExNihiloConfig;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.oredict.OreIngredient;

import exnihilocreatio.registries.manager.ExNihiloRegistryManager;
import exnihilocreatio.registries.types.Siftable;
import exnihilocreatio.util.ItemInfo;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Used for adding/removing recipes to the Sieve for CEu (and addons), drops will be added at
 * {@link net.minecraftforge.fml.common.event.FMLInitializationEvent} to the Ex Nihilo sifting list.
 */
public class SieveDrops {

    private static Object2ObjectOpenHashMap<Ingredient, List<Siftable>> siftables = new Object2ObjectOpenHashMap<>();

    private static boolean validateDrops(ItemStack output, int meshlevel, float chance) {
        if (output == null) {
            IntegrationModule.logger.error("Output stack is null!", new Throwable());
            return false;
        } else if (output.isEmpty()) {
            IntegrationModule.logger.error("Output stack is empty!", new Throwable());
            return false;
        }
        if (chance > 1F) {
            IntegrationModule.logger.error("Chance for {} can't be higher than 1!", output.getDisplayName(),
                    new Throwable());
            return false;
        }
        if (meshlevel > 4) {
            IntegrationModule.logger.error("Mesh Level for {} out of range!", output.getDisplayName(), new Throwable());
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    public static void removeDrop(ItemStack input, ItemStack output) {
        if (input.isEmpty()) {
            IntegrationModule.logger.error("Input stack is empty!", new Throwable());
            return;
        }
        if (output.isEmpty()) {
            IntegrationModule.logger.error("Output stack is empty!", new Throwable());
            return;
        }
        Optional<Ingredient> optionalIngredient = siftables.keySet().stream()
                .filter(ingredient -> ingredient.apply(input)).findFirst();
        if (optionalIngredient.isPresent()) {
            Ingredient ingredient = optionalIngredient.get();
            if (!siftables.get(ingredient)
                    .removeIf(siftable -> siftable.getDrop().getItemStack().isItemEqual(output))) {
                IntegrationModule.logger.error("Cannot find Output for stack {} and Input stack {}",
                        output.getDisplayName(), input.getDisplayName(), new Throwable());
            }
        } else {
            IntegrationModule.logger.error("Cannot find Input for stack {}", input.getDisplayName(), new Throwable());
        }
    }

    public static void addDrop(Block input, ItemStack output, int meshLevel, float chance) {
        addDrop(Ingredient.fromStacks(new ItemStack(input)), output, meshLevel, chance);
    }

    @SuppressWarnings("unused")
    public static void addDrop(ItemStack input, ItemStack output, int meshLevel, float chance) {
        if (input.isEmpty()) {
            IntegrationModule.logger.error("Input stack is empty!", new Throwable());
            return;
        }
        addDrop(Ingredient.fromStacks(input), output, meshLevel, chance);
    }

    public static void addDrop(String oredict, ItemStack output, int meshLevel, float chance) {
        List<ItemStack> stacks = OreDictUnifier.getAllWithOreDictionaryName(oredict);
        if (stacks.isEmpty()) {
            IntegrationModule.logger.error("Cannot find oredict {}!", oredict, new Throwable());
            return;
        }
        addDrop(new OreIngredient(oredict), output, meshLevel, chance);
    }

    public static void addDrop(Ingredient ingredient, ItemStack output, int meshLevel, float chance) {
        addDrop(ingredient, new Siftable(new ItemInfo(output), chance, meshLevel));
    }

    public static void addDrop(Ingredient ingredient, Siftable siftable) {
        if (!validateDrops(siftable.getDrop().getItemStack(), siftable.getMeshLevel(), siftable.getChance())) {
            return;
        }
        ItemStack[] matchingStacks = ingredient.getMatchingStacks();
        Optional<Ingredient> optionalIngredient = siftables.keySet().stream()
                .filter(ingredient1 -> Arrays.stream(ingredient1.getMatchingStacks()).allMatch(stack -> Arrays
                        .stream(matchingStacks).anyMatch(stack1 -> ItemStack.areItemsEqual(stack, stack1))))
                .findFirst();
        if (!optionalIngredient.isPresent()) {
            siftables.put(ingredient, new ArrayList<>());
        } else {
            ingredient = optionalIngredient.get();
        }
        siftables.get(ingredient).add(siftable);
    }

    public static void registerSiftingRecipes() {
        // If all recipes are getting removed, backup and readd the dirt sifting table so a void world playthrough is
        // doable.
        if (ExNihiloConfig.overrideAllSiftDrops) {
            List<Siftable> siftablesDirt = ExNihiloRegistryManager.SIEVE_REGISTRY.getDrops(OreDictUnifier.get("dirt"));
            ExNihiloRegistryManager.SIEVE_REGISTRY.getRegistry().clear();
            if (!siftablesDirt.isEmpty()) {
                ExNihiloRegistryManager.SIEVE_REGISTRY.register(
                        Ingredient.fromStacks(
                                OreDictUnifier.getAllWithOreDictionaryName("dirt").toArray(new ItemStack[0])),
                        siftablesDirt);
            }
        }
        siftables.forEach(ExNihiloRegistryManager.SIEVE_REGISTRY::register);

        siftables = null; // let this get GC'd, no more eating my memory
    }
}
