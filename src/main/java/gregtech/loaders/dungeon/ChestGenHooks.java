package gregtech.loaders.dungeon;

import com.google.common.collect.Lists;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ChestGenHooks {

    private static final HashMap<ResourceLocation, ArrayList<LootEntryItem>> lootEntryItems = new HashMap<>();
    private static final HashMap<ResourceLocation, RandomValueRange> rollVals = new HashMap<>();


    private static final LootCondition[] NO_CONDITIONS = new LootCondition[0];

    private static final ChestGenHooks instance = new ChestGenHooks();

    private ChestGenHooks() {
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(instance);
    }

    @SubscribeEvent
    public void onWorldLoad(LootTableLoadEvent event) {
        LootPool mainPool = event.getTable().getPool("main");
        if (mainPool != null && lootEntryItems.containsKey(event.getName())) {
            ArrayList<LootEntryItem> entryItems = lootEntryItems.get(event.getName());
            for (LootEntryItem entry : entryItems) {
                try {
                    if(ConfigHolder.misc.debug) {
                        GTLog.logger.info("adding " + entry.getEntryName() + " to lootTable");
                    }
                    mainPool.addEntry(entry);
                } catch (java.lang.RuntimeException e) {
                    GTLog.logger.error("Couldn't add " + entry.getEntryName() + " to lootTable");
                }
            }
        }
        if (mainPool != null && rollVals.containsKey(event.getName())) {
            RandomValueRange rangeAdd = rollVals.get(event.getName());
            RandomValueRange range = mainPool.getRolls();
            mainPool.setRolls(new RandomValueRange(range.getMin() + rangeAdd.getMin(), range.getMax() + rangeAdd.getMax()));
        }
    }

    public static void addItem(ResourceLocation lootTable, ItemStack item, int minAmount, int maxAmount, int weight) {
        LootEntryItem itemEntry = new LootEntryItem(item.getItem(), weight, 1, new LootFunction[]{
                new LootFunction(NO_CONDITIONS) {
                    @Nonnull
                    @Override
                    public ItemStack apply(@Nonnull ItemStack stack, @Nonnull Random rand, @Nonnull LootContext context) {
                        stack.setItemDamage(item.getItemDamage());
                        stack.setTagCompound(item.getTagCompound());

                        int count = minAmount + rand.nextInt(maxAmount - minAmount + 1);
                        count = Math.min(count, item.getMaxStackSize());

                        stack.setCount(count);
                        return stack;
                    }
                }
        }, NO_CONDITIONS, "#gregtech:loot_" + item.hashCode());
        if (lootEntryItems.containsKey(lootTable)) {
            lootEntryItems.get(lootTable).add(itemEntry);
        } else {
            lootEntryItems.put(lootTable, Lists.newArrayList(itemEntry));
        }
    }

    public static void addRolls(ResourceLocation tableLocation, int minAdd, int maxAdd) {
        rollVals.put(tableLocation, new RandomValueRange(minAdd, maxAdd));
    }


}
