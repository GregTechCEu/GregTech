package gregtech.api.unification;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.*;
import gregtech.api.util.CustomModPriorityComparator;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import static gregtech.api.GTValues.M;

public class OreDictUnifier {

    private OreDictUnifier() {}

    private static final Map<ItemAndMetadata, ItemMaterialInfo> materialUnificationInfo = new Object2ObjectOpenHashMap<>();
    private static final Map<ItemAndMetadata, UnificationEntry> stackUnificationInfo = new Object2ObjectOpenHashMap<>();
    private static final Map<UnificationEntry, ArrayList<ItemAndMetadata>> stackUnificationItems = new Object2ObjectOpenHashMap<>();
    private static final Map<Item, ItemVariantMap.Mutable<Set<String>>> stackOreDictName = new Object2ObjectOpenHashMap<>();
    private static final Map<String, List<ItemStack>> oreDictNameStacks = new Object2ObjectOpenHashMap<>();

    @Nullable
    private static Comparator<ItemAndMetadata> stackComparator;

    public static Comparator<ItemAndMetadata> getSimpleItemStackComparator() {
        if (stackComparator == null) {
            List<String> modPriorities = Arrays.asList(ConfigHolder.compat.modPriorities);
            if (modPriorities.isEmpty()) {
                // noinspection ConstantConditions
                Function<ItemAndMetadata, String> modIdExtractor = stack -> stack.item.getRegistryName().getNamespace();
                stackComparator = Comparator.comparing(modIdExtractor);
            } else {
                stackComparator = Collections.reverseOrder(new CustomModPriorityComparator(modPriorities));
            }
        }
        return stackComparator;
    }

    public static Comparator<ItemStack> getItemStackComparator() {
        Comparator<ItemAndMetadata> comparator = getSimpleItemStackComparator();
        return (first, second) -> comparator.compare(new ItemAndMetadata(first), new ItemAndMetadata(second));
    }

    public static void registerOre(ItemStack itemStack, ItemMaterialInfo materialInfo) {
        if (itemStack.isEmpty()) return;
        materialUnificationInfo.put(new ItemAndMetadata(itemStack), materialInfo);
    }

    public static void registerOre(ItemStack itemStack, OrePrefix orePrefix, @Nullable Material material) {
        registerOre(itemStack, orePrefix.name(), material);
    }

    public static void registerOre(ItemStack itemStack, String customOrePrefix, @Nullable Material material) {
        if (itemStack.isEmpty()) return;
        OreDictionary.registerOre(customOrePrefix + (material == null ? "" : material.toCamelCaseString()), itemStack);
    }

    public static void registerOre(ItemStack itemStack, String oreDict) {
        if (itemStack.isEmpty()) return;
        OreDictionary.registerOre(oreDict, itemStack);
    }

    public static void init() {
        for (String registeredOreName : OreDictionary.getOreNames()) {
            NonNullList<ItemStack> theseOres = OreDictionary.getOres(registeredOreName);
            for (ItemStack itemStack : theseOres) {
                onItemRegistration(new OreRegisterEvent(registeredOreName, itemStack));
            }
        }
        MinecraftForge.EVENT_BUS.register(OreDictUnifier.class);
    }

    @SubscribeEvent
    public static void onItemRegistration(OreRegisterEvent event) {
        String oreName = event.getName();
        // cache this registration by name
        ItemVariantMap.Mutable<Set<String>> entry = stackOreDictName.computeIfAbsent(event.getOre().getItem(),
                item -> item.getHasSubtypes() ? new MultiItemVariantMap<>() : new SingleItemVariantMap<>());
        Set<String> set = entry.get(event.getOre());
        if (set == null) {
            set = new ObjectOpenHashSet<>();
            entry.put(event.getOre(), set);
        }
        set.add(oreName);
        List<ItemStack> itemStackListForOreDictName = oreDictNameStacks.computeIfAbsent(oreName,
                k -> new ArrayList<>());
        addAndSort(itemStackListForOreDictName, event.getOre().copy(), getItemStackComparator());

        // and try to transform registration name into OrePrefix + Material pair
        OrePrefix orePrefix = OrePrefix.getPrefix(oreName);
        Material material = null;
        if (orePrefix == null) {
            // split ore dict name to parts
            // oreBasalticMineralSand -> ore, Basaltic, Mineral, Sand
            ArrayList<String> splits = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            for (char character : oreName.toCharArray()) {
                if (Character.isUpperCase(character)) {
                    if (builder.length() > 0) {
                        splits.add(builder.toString());
                        builder = new StringBuilder().append(character);
                    } else splits.add(Character.toString(character));
                } else builder.append(character);
            }
            if (builder.length() > 0) {
                splits.add(builder.toString());
            }
            for (MaterialRegistry registry : GregTechAPI.materialManager.getRegistries()) {
                // try to combine in different manners
                // oreBasaltic MineralSand , ore BasalticMineralSand
                StringBuilder buffer = new StringBuilder();
                for (int i = 0; i < splits.size(); i++) {
                    buffer.append(splits.get(i));
                    OrePrefix maybePrefix = OrePrefix.getPrefix(buffer.toString()); // ore -> OrePrefix.ore
                    String possibleMaterialName = Joiner.on("").join(splits.subList(i + 1, splits.size())); // BasalticMineralSand
                    String underscoreName = GTUtility.toLowerCaseUnderscore(possibleMaterialName); // basaltic_mineral_sand
                    Material possibleMaterial = registry.getObject(underscoreName); // Materials.BasalticSand
                    if (possibleMaterial == null) {
                        // if we didn't find real material, try using marker material registry
                        possibleMaterial = GregTechAPI.markerMaterialRegistry.getMarkerMaterial(underscoreName);
                    }
                    if (maybePrefix != null && possibleMaterial != null) {
                        orePrefix = maybePrefix;
                        material = possibleMaterial;
                        break;
                    }
                }
                if (material != null) break;
            }
        }

        // finally register item
        if (orePrefix != null && (material != null || orePrefix.isSelfReferencing)) {
            ItemAndMetadata key = new ItemAndMetadata(event.getOre());
            UnificationEntry unificationEntry = new UnificationEntry(orePrefix, material);
            ArrayList<ItemAndMetadata> itemListForUnifiedEntry = stackUnificationItems.computeIfAbsent(unificationEntry,
                    p -> new ArrayList<>());
            addAndSort(itemListForUnifiedEntry, key, getSimpleItemStackComparator());

            if (!unificationEntry.orePrefix.isMarkerPrefix()) {
                stackUnificationInfo.put(key, unificationEntry);
            }
            orePrefix.processOreRegistration(material);
        }
    }

    @NotNull
    public static Set<String> getOreDictionaryNames(@NotNull ItemStack itemStack) {
        if (itemStack.isEmpty()) return Collections.emptySet();
        ItemVariantMap<Set<String>> nameEntry = stackOreDictName.get(itemStack.getItem());
        if (nameEntry == null) return Collections.emptySet();
        short itemDamage = (short) itemStack.getItemDamage();
        Set<String> names = nameEntry.get(itemDamage);
        Set<String> wildcardNames = itemDamage == GTValues.W ? null : nameEntry.get(GTValues.W);
        if (names == null) {
            return wildcardNames == null ? Collections.emptySet() : Collections.unmodifiableSet(wildcardNames);
        } else if (wildcardNames == null || names == wildcardNames) { // single variant items have identical entries
            return Collections.unmodifiableSet(names);
        } else {
            return Sets.union(names, wildcardNames);
        }
    }

    @Nullable
    public static ItemVariantMap<Set<String>> getOreDictionaryEntry(@NotNull Item item) {
        ItemVariantMap.Mutable<Set<String>> entry = stackOreDictName.get(item);
        return entry == null ? null : ItemVariantMap.unmodifiableSetView(entry);
    }

    @NotNull
    public static ItemVariantMap<Set<String>> getOreDictionaryEntryOrEmpty(@NotNull Item item) {
        ItemVariantMap.Mutable<Set<String>> entry = stackOreDictName.get(item);
        return entry == null ? ItemVariantMap.empty() : ItemVariantMap.unmodifiableSetView(entry);
    }

    public static boolean hasOreDictionaryEntry(@NotNull Item item) {
        return stackOreDictName.containsKey(item);
    }

    public static boolean hasOreDictionary(@NotNull ItemStack itemStack, @NotNull String oreDictName) {
        if (itemStack.isEmpty()) return false;
        ItemVariantMap<Set<String>> nameEntry = stackOreDictName.get(itemStack.getItem());
        if (nameEntry == null) return false;

        short itemDamage = (short) itemStack.getItemDamage();
        Set<String> names = nameEntry.get(itemDamage);
        if (names != null && names.contains(oreDictName)) return true;

        if (itemDamage == GTValues.W) return false;

        Set<String> wildcardNames = nameEntry.get(GTValues.W);
        return wildcardNames != null && wildcardNames != names && wildcardNames.contains(oreDictName);
    }

    public static List<ItemStack> getAllWithOreDictionaryName(String oreDictionaryName) {
        return oreDictNameStacks.get(oreDictionaryName).stream()
                .map(ItemStack::copy)
                .collect(Collectors.toList());
    }

    @Nullable
    public static MaterialStack getMaterial(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        ItemAndMetadata key = new ItemAndMetadata(itemStack);
        UnificationEntry entry = getOrWildcard(stackUnificationInfo, key);
        if (entry != null) {
            Material entryMaterial = entry.material;
            if (entryMaterial == null) {
                entryMaterial = entry.orePrefix.materialType;
            }
            if (entryMaterial != null) {
                return new MaterialStack(entryMaterial, entry.orePrefix.getMaterialAmount(entryMaterial));
            }
        }
        ItemMaterialInfo info = getOrWildcard(materialUnificationInfo, key);
        return info == null ? null : info.getMaterial().copy();
    }

    @Nullable
    public static ItemMaterialInfo getMaterialInfo(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        return getOrWildcard(materialUnificationInfo, new ItemAndMetadata(itemStack));
    }

    @Nullable
    public static OrePrefix getPrefix(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        UnificationEntry entry = getOrWildcard(stackUnificationInfo, new ItemAndMetadata(itemStack));
        return entry != null ? entry.orePrefix : null;
    }

    @Nullable
    public static UnificationEntry getUnificationEntry(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        return getOrWildcard(stackUnificationInfo, new ItemAndMetadata(itemStack));
    }

    public static ItemStack getUnificated(ItemStack itemStack) {
        if (itemStack.isEmpty()) return ItemStack.EMPTY;
        UnificationEntry unificationEntry = getUnificationEntry(itemStack);
        if (unificationEntry == null || !stackUnificationItems.containsKey(unificationEntry) ||
                !unificationEntry.orePrefix.isUnificationEnabled)
            return itemStack;
        ArrayList<ItemAndMetadata> keys = stackUnificationItems.get(unificationEntry);
        return keys.size() > 0 ? keys.get(0).toItemStack(itemStack.getCount()) : itemStack;
    }

    public static ItemStack get(UnificationEntry unificationEntry) {
        return get(unificationEntry.orePrefix, unificationEntry.material);
    }

    public static ItemStack get(OrePrefix orePrefix, Material material) {
        return get(orePrefix, material, 1);
    }

    public static ItemStack get(OrePrefix orePrefix, Material material, int stackSize) {
        UnificationEntry unificationEntry = new UnificationEntry(orePrefix, material);
        if (!stackUnificationItems.containsKey(unificationEntry))
            return ItemStack.EMPTY;
        ArrayList<ItemAndMetadata> keys = stackUnificationItems.get(unificationEntry);
        return keys.size() > 0 ? keys.get(0).toItemStack(stackSize) : ItemStack.EMPTY;
    }

    public static ItemStack get(String oreDictName) {
        List<ItemStack> itemStacks = oreDictNameStacks.get(oreDictName);
        if (itemStacks == null || itemStacks.size() == 0) return ItemStack.EMPTY;
        return itemStacks.get(0).copy();
    }

    public static List<Entry<ItemStack, ItemMaterialInfo>> getAllItemInfos() {
        return materialUnificationInfo.entrySet().stream()
                .map(entry -> new SimpleEntry<>(entry.getKey().toItemStack(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public static List<ItemStack> getAll(UnificationEntry unificationEntry) {
        if (!stackUnificationItems.containsKey(unificationEntry))
            return Collections.emptyList();
        ArrayList<ItemAndMetadata> keys = stackUnificationItems.get(unificationEntry);
        return keys.stream().map(ItemAndMetadata::toItemStack).collect(Collectors.toList());
    }

    public static ItemStack getDust(Material material, long materialAmount) {
        if (!material.hasProperty(PropertyKey.DUST) || materialAmount <= 0)
            return ItemStack.EMPTY;
        if (materialAmount % M == 0 || materialAmount >= M * 16)
            return get(OrePrefix.dust, material, (int) (materialAmount / M));
        else if ((materialAmount * 4) % M == 0 || materialAmount >= M * 8)
            return get(OrePrefix.dustSmall, material, (int) ((materialAmount * 4) / M));
        else if ((materialAmount * 9) >= M)
            return get(OrePrefix.dustTiny, material, (int) ((materialAmount * 9) / M));
        return ItemStack.EMPTY;
    }

    public static ItemStack getDust(MaterialStack materialStack) {
        return getDust(materialStack.material, materialStack.amount);
    }

    public static ItemStack getIngot(Material material, long materialAmount) {
        if (!material.hasProperty(PropertyKey.INGOT) || materialAmount <= 0)
            return ItemStack.EMPTY;
        if (materialAmount % (M * 9) == 0)
            return get(OrePrefix.block, material, (int) (materialAmount / (M * 9)));
        if (materialAmount % M == 0 || materialAmount >= M * 16)
            return get(OrePrefix.ingot, material, (int) (materialAmount / M));
        else if ((materialAmount * 9) >= M)
            return get(OrePrefix.nugget, material, (int) ((materialAmount * 9) / M));
        return ItemStack.EMPTY;
    }

    public static ItemStack getIngot(MaterialStack materialStack) {
        return getIngot(materialStack.material, materialStack.amount);
    }

    /**
     * Returns an Ingot of the material if it exists. Otherwise it returns a Dust.
     * Returns ItemStack.EMPTY if neither exist.
     */
    public static ItemStack getIngotOrDust(Material material, long materialAmount) {
        ItemStack ingotStack = getIngot(material, materialAmount);
        if (ingotStack != ItemStack.EMPTY) return ingotStack;
        return getDust(material, materialAmount);
    }

    public static ItemStack getIngotOrDust(MaterialStack materialStack) {
        return getIngotOrDust(materialStack.material, materialStack.amount);
    }

    public static ItemStack getGem(MaterialStack materialStack) {
        if (materialStack.material.hasProperty(PropertyKey.GEM) && !OrePrefix.gem.isIgnored(materialStack.material) &&
                materialStack.amount == OrePrefix.gem.getMaterialAmount(materialStack.material)) {
            return get(OrePrefix.gem, materialStack.material, (int) (materialStack.amount / M));
        }
        return getDust(materialStack);
    }

    synchronized private static <T> void addAndSort(List<T> list, T itemToAdd, Comparator<T> comparator) {
        list.add(itemToAdd);

        if (list.size() > 1)
            list.sort(comparator);
    }

    /**
     * Get the value corresponding to given key or its wildcard counterpart.
     *
     * @param map Map
     * @param key Key
     * @return value corresponding to given key or its wildcard counterpart
     */
    @Nullable
    private static <T> T getOrWildcard(@NotNull Map<ItemAndMetadata, T> map,
                                       @NotNull ItemAndMetadata key) {
        T t = map.get(key);
        if (t != null) return t;
        if (key.isWildcard()) return null;
        return map.get(key.toWildcard());
    }
}
