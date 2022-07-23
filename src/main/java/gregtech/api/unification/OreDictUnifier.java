package gregtech.api.unification;

import com.google.common.base.Joiner;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.*;
import gregtech.api.util.CustomModPriorityComparator;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;

import javax.annotation.Nullable;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import static gregtech.api.GTValues.M;

public class OreDictUnifier {

    private OreDictUnifier() {
    }

    //simple version of material registry for marker materials
    private static final Map<String, MarkerMaterial> markerMaterialRegistry = new Object2ObjectOpenHashMap<>();
    private static final Map<ItemAndMetadata, ItemMaterialInfo> materialUnificationInfo = new WildcardAwareHashMap<>();
    private static final Map<ItemAndMetadata, UnificationEntry> stackUnificationInfo = new WildcardAwareHashMap<>();
    private static final Map<UnificationEntry, ArrayList<ItemAndMetadata>> stackUnificationItems = new Object2ObjectOpenHashMap<>();
    private static final Map<ItemAndMetadata, Set<String>> stackOreDictName = new WildcardAwareHashMap<>();
    private static final Map<String, List<ItemStack>> oreDictNameStacks = new Object2ObjectOpenHashMap<>();

    @Nullable
    private static Comparator<ItemAndMetadata> stackComparator;

    public static Comparator<ItemAndMetadata> getSimpleItemStackComparator() {
        if (stackComparator == null) {
            List<String> modPriorities = Arrays.asList(ConfigHolder.compat.modPriorities);
            if (modPriorities.isEmpty()) {
                //noinspection ConstantConditions
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

    public static void registerMarkerMaterial(MarkerMaterial markerMaterial) {
        if (markerMaterialRegistry.containsKey(markerMaterial.toString())) {
            throw new IllegalArgumentException(("Marker material with id " + markerMaterial.toString() + " is already registered!"));
        }
        markerMaterialRegistry.put(markerMaterial.toString(), markerMaterial);
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
        ItemAndMetadata simpleItemStack = new ItemAndMetadata(event.getOre());
        String oreName = event.getName();
        //cache this registration by name
        stackOreDictName.computeIfAbsent(simpleItemStack, k -> new HashSet<>()).add(oreName);
        List<ItemStack> itemStackListForOreDictName = oreDictNameStacks.computeIfAbsent(oreName, k -> new ArrayList<>());
        addAndSort(itemStackListForOreDictName, event.getOre().copy(), getItemStackComparator());

        //and try to transform registration name into OrePrefix + Material pair
        OrePrefix orePrefix = OrePrefix.getPrefix(oreName);
        Material material = null;
        if (orePrefix == null) {
            //split ore dict name to parts
            //oreBasalticMineralSand -> ore, Basaltic, Mineral, Sand
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
            //try to combine in different manners
            //oreBasaltic MineralSand , ore BasalticMineralSand
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < splits.size(); i++) {
                buffer.append(splits.get(i));
                OrePrefix maybePrefix = OrePrefix.getPrefix(buffer.toString()); //ore -> OrePrefix.ore
                String possibleMaterialName = Joiner.on("").join(splits.subList(i + 1, splits.size())); //BasalticMineralSand
                String underscoreName = GTUtility.toLowerCaseUnderscore(possibleMaterialName); //basaltic_mineral_sand
                Material possibleMaterial = GregTechAPI.MATERIAL_REGISTRY.getObject(underscoreName); //Materials.BasalticSand
                if (possibleMaterial == null) {
                    //if we didn't found real material, try using marker material registry
                    possibleMaterial = markerMaterialRegistry.get(underscoreName);
                }
                if (maybePrefix != null && possibleMaterial != null) {
                    orePrefix = maybePrefix;
                    material = possibleMaterial;
                    break;
                }
            }
        }

        //finally register item
        if (orePrefix != null && (material != null || orePrefix.isSelfReferencing)) {
            UnificationEntry unificationEntry = new UnificationEntry(orePrefix, material);
            ArrayList<ItemAndMetadata> itemListForUnifiedEntry = stackUnificationItems.computeIfAbsent(unificationEntry, p -> new ArrayList<>());
            addAndSort(itemListForUnifiedEntry, simpleItemStack, getSimpleItemStackComparator());

            if (!unificationEntry.orePrefix.isMarkerPrefix()) {
                stackUnificationInfo.put(simpleItemStack, unificationEntry);
            }
            orePrefix.processOreRegistration(material);
        }
    }

    public static Set<String> getOreDictionaryNames(ItemStack itemStack) {
        if (itemStack.isEmpty()) return Collections.emptySet();
        ItemAndMetadata simpleItemStack = new ItemAndMetadata(itemStack);
        if (stackOreDictName.containsKey(simpleItemStack))
            return Collections.unmodifiableSet(stackOreDictName.get(simpleItemStack));
        return Collections.emptySet();
    }

    public static List<ItemStack> getAllWithOreDictionaryName(String oreDictionaryName) {
        return oreDictNameStacks.get(oreDictionaryName).stream()
                .map(ItemStack::copy)
                .collect(Collectors.toList());
    }

    @Nullable
    public static MaterialStack getMaterial(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        ItemAndMetadata simpleItemStack = new ItemAndMetadata(itemStack);
        UnificationEntry entry = stackUnificationInfo.get(simpleItemStack);
        if (entry != null) {
            Material entryMaterial = entry.material;
            if (entryMaterial == null) {
                entryMaterial = entry.orePrefix.materialType;
            }
            if (entryMaterial != null) {
                return new MaterialStack(entryMaterial, entry.orePrefix.getMaterialAmount(entryMaterial));
            }
        }
        ItemMaterialInfo info = materialUnificationInfo.get(simpleItemStack);
        return info == null ? null : info.getMaterial().copy();
    }

    @Nullable
    public static ItemMaterialInfo getMaterialInfo(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        ItemAndMetadata simpleItemStack = new ItemAndMetadata(itemStack);
        return materialUnificationInfo.get(simpleItemStack);
    }

    @Nullable
    public static OrePrefix getPrefix(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        ItemAndMetadata simpleItemStack = new ItemAndMetadata(itemStack);
        UnificationEntry entry = stackUnificationInfo.get(simpleItemStack);
        if (entry != null) return entry.orePrefix;
        return null;
    }

    public static OrePrefix getPrefix(Block block) {
        return getPrefix(new ItemStack(block));
    }

    @Nullable
    public static UnificationEntry getUnificationEntry(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        return stackUnificationInfo.get(new ItemAndMetadata(itemStack));
    }

    public static ItemStack getUnificated(ItemStack itemStack) {
        if (itemStack.isEmpty()) return ItemStack.EMPTY;
        UnificationEntry unificationEntry = getUnificationEntry(itemStack);
        if (unificationEntry == null || !stackUnificationItems.containsKey(unificationEntry) || !unificationEntry.orePrefix.isUnificationEnabled)
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
        if (material == null || !material.hasProperty(PropertyKey.INGOT) || materialAmount < (M / 9))
            return ItemStack.EMPTY;
        if (materialAmount % (M * 9) == 0)
            return get(OrePrefix.block, material, (int) (materialAmount / (M * 9)));
        if (materialAmount % M == 0 || materialAmount >= M * 16)
            return get(OrePrefix.ingot, material, (int) (materialAmount / M));
        if (materialAmount % (M / 4) == 0 || materialAmount >= M * 8) {
            int stackCount = (int) (materialAmount * 4 / M);
            return get(OrePrefix.chunk, material, stackCount);
        }
        return get(OrePrefix.nugget, material, (int) (materialAmount * 9 / M));
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
        if (materialStack.material.hasProperty(PropertyKey.GEM)
                && !OrePrefix.gem.isIgnored(materialStack.material)
                && materialStack.amount == OrePrefix.gem.getMaterialAmount(materialStack.material)) {
            return get(OrePrefix.gem, materialStack.material, (int) (materialStack.amount / M));
        }
        return getDust(materialStack);
    }

    synchronized private static <T> void addAndSort(List<T> list, T itemToAdd, Comparator<T> comparator) {
        list.add(itemToAdd);

        if (list.size() > 1)
            list.sort(comparator);
    }
}
