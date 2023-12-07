package gregtech.common.worldgen;

import gregtech.api.util.GTLog;

import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.google.common.collect.Lists;
import com.google.gson.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;

public class LootTableHelper {

    private static final Map<String, LootTableEntrySerializer<?>> serializerMap = new HashMap<>();

    public interface LootTableEntrySerializer<T extends LootEntry> {

        T deserialize(JsonObject object, JsonDeserializationContext deserializationContext, int weightIn, int qualityIn,
                      LootCondition[] conditionsIn);
    }

    public interface SerializableLootEntry {

        String getType();

        int getWeight();

        int getQuality();

        LootCondition[] getConditions();

        void serializeEntry(JsonObject object, JsonSerializationContext context);
    }

    public static void registerLootEntry(String type, LootTableEntrySerializer<?> serializer) {
        serializerMap.put(type, serializer);
    }

    public static void initialize() {
        try {
            Field gsonField = Arrays.stream(LootTableManager.class.getDeclaredFields())
                    .filter(it -> Gson.class.isAssignableFrom(it.getType()))
                    .findFirst().orElseThrow(() -> new RuntimeException("Failed to find Gson field!"));
            gsonField.setAccessible(true);
            Gson gsonInstance = (Gson) gsonField.get(null);
            replaceGsonTypeHierarchySerializer(gsonInstance, LootEntry.class, LootTableEntrySerializerDelegate::new);
        } catch (Throwable exception) {
            GTLog.logger.fatal("Failed to initialize loot table helper", exception);
            throw new RuntimeException(exception);
        }
        registerLootEntry("gregtech:meta_item", LootEntryMetaItem::deserialize);
        registerLootEntry("gregtech:ore_dict", LootEntryOreDict::deserialize);
    }

    private static class LootTableEntrySerializerDelegate implements JsonSerializer<LootEntry>,
                                                          JsonDeserializer<LootEntry> {

        private final JsonSerializer<LootEntry> delegatedSerializer;
        private final JsonDeserializer<LootEntry> delegatedDeserializer;

        @SuppressWarnings("unchecked")
        public LootTableEntrySerializerDelegate(JsonSerializer<?> delegatedSerializer,
                                                JsonDeserializer<?> delegatedDeserializer) {
            this.delegatedSerializer = (JsonSerializer<LootEntry>) delegatedSerializer;
            this.delegatedDeserializer = (JsonDeserializer<LootEntry>) delegatedDeserializer;
        }

        @Override
        public LootEntry deserialize(JsonElement json, Type typeOfT,
                                     JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonobject = JsonUtils.getJsonObject(json, "loot item");
            String type = JsonUtils.getString(jsonobject, "type");
            if (serializerMap.containsKey(type)) {
                int weight = JsonUtils.getInt(jsonobject, "weight", 1);
                int quality = JsonUtils.getInt(jsonobject, "quality", 0);
                LootCondition[] lootConditions;
                if (jsonobject.has("conditions")) {
                    lootConditions = JsonUtils.deserializeClass(jsonobject, "conditions", context,
                            LootCondition[].class);
                } else {
                    lootConditions = new LootCondition[0];
                }
                LootTableEntrySerializer<?> serializer = serializerMap.get(type);
                return serializer.deserialize(jsonobject, context, weight, quality, lootConditions);
            }
            return delegatedDeserializer.deserialize(json, typeOfT, context);
        }

        @Override
        public JsonElement serialize(LootEntry src, Type typeOfSrc, JsonSerializationContext context) {
            if (src instanceof SerializableLootEntry) {
                SerializableLootEntry entry = (SerializableLootEntry) src;
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("weight", entry.getWeight());
                jsonObject.addProperty("quality", entry.getQuality());
                LootCondition[] conditions = entry.getConditions();
                if (conditions.length > 0) {
                    jsonObject.add("conditions", context.serialize(entry.getConditions()));
                }
                jsonObject.addProperty("type", entry.getType());
                entry.serializeEntry(jsonObject, context);
                return jsonObject;
            }
            return delegatedSerializer.serialize(src, typeOfSrc, context);
        }
    }

    @SuppressWarnings("unchecked")
    private static void replaceGsonTypeHierarchySerializer(Gson gson, Class<?> type,
                                                           BiFunction<JsonSerializer<?>, JsonDeserializer<?>, Object> replacer) {
        try {
            Field field = Gson.class.getDeclaredField("factories");
            field.setAccessible(true);
            Class<?> singleTypeFactoryClass = Class
                    .forName("com.google.gson.internal.bind.TreeTypeAdapter$SingleTypeFactory");
            Field hierarchyTypeField = singleTypeFactoryClass.getDeclaredField("hierarchyType");
            hierarchyTypeField.setAccessible(true);
            List<TypeAdapterFactory> factories = (List<TypeAdapterFactory>) field.get(gson);
            Object factoryObject = factories.stream()
                    .filter(factory -> singleTypeFactoryClass.isAssignableFrom(factory.getClass()))
                    .filter(factory -> {
                        try {
                            return hierarchyTypeField.get(factory) == type;
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }).findFirst().orElseThrow(() -> new IllegalArgumentException("Serializer not found for " + type));

            Field serializerField = singleTypeFactoryClass.getDeclaredField("serializer");
            Field deserializerField = singleTypeFactoryClass.getDeclaredField("deserializer");
            serializerField.setAccessible(true);
            deserializerField.setAccessible(true);
            JsonSerializer<?> serializer = (JsonSerializer<?>) serializerField.get(factoryObject);
            JsonDeserializer<?> deserializer = (JsonDeserializer<?>) deserializerField.get(factoryObject);
            Object replacedSerializer = replacer.apply(serializer, deserializer);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.set(serializerField, serializerField.getModifiers() & (~Modifier.FINAL));
            modifiersField.set(deserializerField, deserializerField.getModifiers() & (~Modifier.FINAL));
            serializerField.set(factoryObject, replacedSerializer);
            deserializerField.set(factoryObject, replacedSerializer);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void fillInventory(IItemHandlerModifiable inventory, Random rand, List<ItemStack> lootList) {
        List<Integer> randomSlots = getEmptySlotsRandomized(inventory, rand);
        shuffleItems(lootList, randomSlots.size(), rand);

        for (ItemStack itemstack : lootList) {
            if (randomSlots.isEmpty()) {
                return;
            }
            if (itemstack.isEmpty()) {
                inventory.setStackInSlot(randomSlots.remove(randomSlots.size() - 1), ItemStack.EMPTY);
            } else {
                inventory.setStackInSlot(randomSlots.remove(randomSlots.size() - 1), itemstack);
            }
        }
    }

    /**
     * shuffles items by changing their order and splitting stacks
     */
    private static void shuffleItems(List<ItemStack> stacks, int slotCount, Random rand) {
        List<ItemStack> list = Lists.newArrayList();
        Iterator<ItemStack> iterator = stacks.iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack = iterator.next();

            if (itemstack.isEmpty()) {
                iterator.remove();
            } else if (itemstack.getCount() > 4) {
                list.add(itemstack);
                iterator.remove();
            }
        }

        slotCount = slotCount - stacks.size();

        while (slotCount > 0 && !list.isEmpty()) {
            ItemStack itemstack2 = list.remove(MathHelper.getInt(rand, 0, list.size() - 1));
            int i = MathHelper.getInt(rand, itemstack2.getCount() / 3, itemstack2.getCount() / 2);
            ItemStack itemstack1 = itemstack2.splitStack(i);

            if (itemstack2.getCount() > 1 && rand.nextBoolean()) {
                list.add(itemstack2);
            } else {
                stacks.add(itemstack2);
            }

            if (itemstack1.getCount() > 1 && rand.nextBoolean()) {
                list.add(itemstack1);
            } else {
                stacks.add(itemstack1);
            }
        }

        stacks.addAll(list);
        Collections.shuffle(stacks, rand);
    }

    private static List<Integer> getEmptySlotsRandomized(IItemHandler inventory, Random rand) {
        List<Integer> list = Lists.newArrayList();
        for (int i = 0; i < inventory.getSlots(); ++i) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                list.add(i);
            }
        }
        Collections.shuffle(list, rand);
        return list;
    }
}
