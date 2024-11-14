package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.lookup.property.BiomeInhabitedProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.lookup.property.filter.IPropertyFilter;
import gregtech.api.recipes.lookup.property.filter.RecipePropertyWithFilter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Biomes;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.List;

// TODO add to RecipeBuilder
public final class BiomeProperty extends RecipePropertyWithFilter<BiomeProperty.BiomePropertyList> {

    public static final String KEY = "dimension";

    private static BiomeProperty INSTANCE;

    private BiomeProperty() {
        super(KEY, BiomePropertyList.class);
    }

    public static BiomeProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BiomeProperty();
            GregTechAPI.RECIPE_PROPERTIES.register(KEY, INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public @NotNull NBTBase serialize(@NotNull Object value) {
        BiomePropertyList prop = castValue(value);
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (Biome biome : prop.whiteListBiomes) {
            ResourceLocation loc = Biome.REGISTRY.getNameForObject(biome);
            if (loc == null) continue;
            list.appendTag(new NBTTagString(loc.toString()));
        }
        for (BiomeDictionary.Type type : prop.whiteListBiomeTypes) {
            list.appendTag(new NBTTagString(type.getName()));
        }
        tag.setTag("whiteList", list);
        list = new NBTTagList();
        for (Biome biome : prop.blackListBiomes) {
            ResourceLocation loc = Biome.REGISTRY.getNameForObject(biome);
            if (loc == null) continue;
            list.appendTag(new NBTTagString(loc.toString()));
        }
        for (BiomeDictionary.Type type : prop.blackListBiomeTypes) {
            list.appendTag(new NBTTagString(type.getName()));
        }
        tag.setTag("blackList", list);
        return tag;
    }

    @Override
    public @NotNull Object deserialize(@NotNull NBTBase nbt) {
        NBTTagCompound tag = (NBTTagCompound) nbt;
        BiomePropertyList prop = new BiomePropertyList();
        NBTTagList list = tag.getTagList("whiteList", Constants.NBT.TAG_STRING);
        for (int i = 0; i < list.tagCount(); i++) {
            String[] split = ResourceLocation.splitObjectName(list.getStringTagAt(i));
            if (split[0] == null) prop.add(BiomeDictionary.Type.getType(split[1]), false);
            else prop.add(Biome.REGISTRY.getObject(new ResourceLocation(split[0], split[1])), false);
        }
        list = tag.getTagList("blackList", Constants.NBT.TAG_STRING);
        for (int i = 0; i < list.tagCount(); i++) {
            String[] split = ResourceLocation.splitObjectName(list.getStringTagAt(i));
            if (split[0] == null) prop.add(BiomeDictionary.Type.getType(split[1]), true);
            else prop.add(Biome.REGISTRY.getObject(new ResourceLocation(split[0], split[1])), true);
        }
        return tag;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        BiomePropertyList list = castValue(value);

        if (list.whiteListBiomes.size() > 0)
            minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.biomes",
                    getBiomesForRecipe(castValue(value).whiteListBiomes)), x, y, color);
        if (list.blackListBiomes.size() > 0)
            minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.biomes_blocked",
                    getBiomesForRecipe(castValue(value).blackListBiomes)), x, y, color);
    }

    private static String getBiomesForRecipe(List<Biome> value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.size(); i++) {
            builder.append(value.get(i).biomeName);
            if (i != value.size() - 1)
                builder.append(", ");
        }
        String str = builder.toString();

        if (str.length() >= 13) {
            str = str.substring(0, 10) + "..";
        }
        return str;
    }

    @Override
    public boolean filterEquals(@Nullable IPropertyFilter<?> other) {
        return other instanceof BiomeProperty;
    }

    @Override
    public int filterHash() {
        return 7;
    }

    @Override
    public @NotNull Filter<BiomePropertyList> getNewFilter() {
        return new BiomeFilter();
    }

    // It would've been better to have one list and swap between blacklist and whitelist, but that would've been
    // a bit awkward to apply to the property in practice.
    public static class BiomePropertyList {

        public static BiomePropertyList EMPTY_LIST = new BiomePropertyList();

        public final List<Biome> whiteListBiomes = new ObjectArrayList<>();
        public final List<BiomeDictionary.Type> whiteListBiomeTypes = new ObjectArrayList<>();
        public final List<Biome> blackListBiomes = new ObjectArrayList<>();
        public final List<BiomeDictionary.Type> blackListBiomeTypes = new ObjectArrayList<>();

        public void add(BiomeDictionary.Type type, boolean toBlacklist) {
            if (toBlacklist) {
                blackListBiomeTypes.add(type);
                whiteListBiomeTypes.remove(type);
            } else {
                whiteListBiomeTypes.add(type);
                blackListBiomeTypes.remove(type);
            }
        }

        public void add(Biome biome, boolean toBlacklist) {
            if (toBlacklist) {
                blackListBiomes.add(biome);
                whiteListBiomes.remove(biome);
            } else {
                whiteListBiomes.add(biome);
                blackListBiomes.remove(biome);
            }
        }

        public void merge(@NotNull BiomeProperty.BiomePropertyList list) {
            this.whiteListBiomes.addAll(list.whiteListBiomes);
            this.blackListBiomes.addAll(list.blackListBiomes);
            this.whiteListBiomeTypes.addAll(list.whiteListBiomeTypes);
            this.blackListBiomeTypes.addAll(list.blackListBiomeTypes);
        }

        public boolean checkBiome(Biome biome) {
            if (blackListBiomes.contains(biome) || !whiteListBiomes.contains(biome)) return false;
            boolean foundWhitelist = whiteListBiomeTypes.isEmpty();
            for (BiomeDictionary.Type type : BiomeDictionary.getTypes(biome)) {
                if (blackListBiomeTypes.contains(type)) return false;
                if (!foundWhitelist && whiteListBiomeTypes.contains(type)) foundWhitelist = true;
            }
            return foundWhitelist;
        }
    }

    @Override
    public boolean matches(PropertySet properties, BiomePropertyList value) {
        BiomeInhabitedProperty inhabited = properties.getNullable(BiomeFilter.MATCHER);
        if (inhabited == null) return value.whiteListBiomes.isEmpty();
        Biome biome = inhabited.biome();
        return value.checkBiome(biome);
    }

    private static class BiomeFilter implements Filter<BiomePropertyList> {

        private static final BiomeInhabitedProperty MATCHER = new BiomeInhabitedProperty(Biomes.PLAINS);

        Object2ObjectOpenHashMap<Biome, BitSet> whiteList = new Object2ObjectOpenHashMap<>();
        Object2ObjectOpenHashMap<Biome, BitSet> blackList = new Object2ObjectOpenHashMap<>();

        @Override
        public void accumulate(short recipeID, @NotNull BiomeProperty.BiomePropertyList filterInformation) {
            for (Biome i : filterInformation.whiteListBiomes) {
                whiteList.computeIfAbsent(i, v -> new BitSet()).set(recipeID);
            }
            for (Biome i : filterInformation.blackListBiomes) {
                blackList.computeIfAbsent(i, v -> new BitSet()).set(recipeID);
            }
        }

        @Override
        public void filter(@NotNull BitSet recipeMask, @NotNull PropertySet properties) {
            BiomeInhabitedProperty inhabited = properties.getNullable(MATCHER);
            Biome biome = inhabited == null ? null : inhabited.biome();
            for (var entry : whiteList.object2ObjectEntrySet()) {
                if (!entry.getKey().equals(biome)) {
                    recipeMask.or(entry.getValue());
                }
            }
            if (biome == null) return;
            BitSet fetch = blackList.get(biome);
            if (fetch != null) recipeMask.or(fetch);
        }
    }
}
