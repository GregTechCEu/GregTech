package gregtech.loaders.dungeon;

import gregtech.api.util.GTLog;
import gregtech.api.util.GTStringUtils;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.common.ConfigHolder;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public final class ChestGenHooks {

    private static final Map<ResourceLocation, List<GTLootEntryItem>> lootEntryItems = new Object2ObjectOpenHashMap<>();
    private static final Map<ResourceLocation, RandomValueRange> rollValues = new Object2ObjectOpenHashMap<>();

    private static final LootCondition[] NO_CONDITIONS = new LootCondition[0];

    private ChestGenHooks() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ChestGenHooks.class);
    }

    @SubscribeEvent
    public static void onWorldLoad(@NotNull LootTableLoadEvent event) {
        LootPool mainPool = event.getTable().getPool("main");
        // noinspection ConstantValue
        if (mainPool == null) return;

        ResourceLocation name = event.getName();
        if (lootEntryItems.containsKey(name)) {
            List<GTLootEntryItem> entryItems = lootEntryItems.get(name);
            for (GTLootEntryItem entry : entryItems) {
                if (ConfigHolder.misc.debug) {
                    GTLog.logger.info("adding {} to lootTable {}", entry, name);
                }

                try {
                    mainPool.addEntry(entry);
                } catch (RuntimeException e) {
                    GTLog.logger.error("Couldn't add {} to lootTable {}: {}", entry, name, e.getMessage());
                }
            }
        }

        if (rollValues.containsKey(event.getName())) {
            RandomValueRange rangeAdd = rollValues.get(event.getName());
            RandomValueRange range = mainPool.getRolls();
            mainPool.setRolls(
                    new RandomValueRange(range.getMin() + rangeAdd.getMin(), range.getMax() + rangeAdd.getMax()));
        }
    }

    public static void addItem(@NotNull ResourceLocation lootTable, @NotNull ItemStack stack, int minAmount,
                               int maxAmount, int weight) {
        RandomWeightLootFunction lootFunction = new RandomWeightLootFunction(stack, minAmount, maxAmount);
        String modid = Objects.requireNonNull(stack.getItem().getRegistryName()).getNamespace();
        String entryName = createEntryName(stack, modid, weight, lootFunction);
        GTLootEntryItem itemEntry = new GTLootEntryItem(stack, weight, lootFunction, entryName);
        if (lootEntryItems.containsKey(lootTable)) {
            lootEntryItems.get(lootTable).add(itemEntry);
        } else {
            lootEntryItems.put(lootTable, Lists.newArrayList(itemEntry));
        }
    }

    public static void addRolls(ResourceLocation tableLocation, int minAdd, int maxAdd) {
        rollValues.put(tableLocation, new RandomValueRange(minAdd, maxAdd));
    }

    private static final ItemStackHashStrategy HASH_STRATEGY = ItemStackHashStrategy.comparingAllButCount();

    private static @NotNull String createEntryName(@NotNull ItemStack stack, @NotNull String modid, int weight,
                                                   @NotNull RandomWeightLootFunction function) {
        int hashCode = Objects.hash(HASH_STRATEGY.hashCode(stack), modid, weight, function.getMinAmount(),
                function.getMaxAmount());
        return String.format("#%s:loot_%s", modid, hashCode);
    }

    private static class GTLootEntryItem extends LootEntryItem {

        private final ItemStack stack;

        public GTLootEntryItem(@NotNull ItemStack stack, int weight, LootFunction lootFunction,
                               @NotNull String entryName) {
            super(stack.getItem(), weight, 1, new LootFunction[] { lootFunction }, NO_CONDITIONS, entryName);
            this.stack = stack;
        }

        @Override
        public @NotNull String toString() {
            return "GTLootEntryItem{name=" + getEntryName() + ", stack=" + GTStringUtils.itemStackToString(stack) + '}';
        }
    }

    private static class RandomWeightLootFunction extends LootFunction {

        private final ItemStack stack;
        private final int minAmount;
        private final int maxAmount;

        public RandomWeightLootFunction(@NotNull ItemStack stack, int minAmount, int maxAmount) {
            super(NO_CONDITIONS);
            Preconditions.checkArgument(minAmount <= maxAmount, "minAmount must be <= maxAmount");
            this.stack = stack;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }

        public int getMinAmount() {
            return minAmount;
        }

        public int getMaxAmount() {
            return maxAmount;
        }

        @Override
        public @NotNull ItemStack apply(@NotNull ItemStack itemStack, @NotNull Random rand,
                                        @NotNull LootContext context) {
            itemStack.setItemDamage(stack.getItemDamage());
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound != null) {
                itemStack.setTagCompound(tagCompound.copy());
            }

            if (minAmount == maxAmount) {
                itemStack.setCount(minAmount);
                return itemStack;
            }

            int count = Math.min(minAmount + rand.nextInt(maxAmount - minAmount + 1), stack.getMaxStackSize());
            itemStack.setCount(count);
            return itemStack;
        }
    }
}
