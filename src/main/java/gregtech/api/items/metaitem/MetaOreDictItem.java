package gregtech.api.items.metaitem;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaOreDictItem extends StandardMetaItem {

    private static final Map<Short, OreDictValueItem> ITEMS = new HashMap<>();
    private static final List<MaterialIconType> DISALLOWED_TYPES = ImmutableList.of(
            MaterialIconType.block, MaterialIconType.foilBlock, MaterialIconType.wire,
            MaterialIconType.ore, MaterialIconType.frameGt, MaterialIconType.pipeHuge,
            MaterialIconType.pipeLarge, MaterialIconType.pipeSide, MaterialIconType.pipeSmall,
            MaterialIconType.pipeMedium, MaterialIconType.pipeTiny);

    public MetaOreDictItem(short metaItemOffset) {
        super(metaItemOffset);
    }

    @Override
    public void registerSubItems() {
        for (OreDictValueItem item : ITEMS.values()) {
            addItem(item.id, item.getName());
            OreDictUnifier.registerOre(new ItemStack(this, 1, item.id), item.getOre());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            OreDictValueItem item = ITEMS.get((short) stack.getItemDamage());
            return item == null ? 0xFFFFFF : item.materialRGB;
        }
        return super.getColorForItemStack(stack, tintIndex);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        super.registerModels();
        TIntObjectHashMap<ModelResourceLocation> alreadyRegistered = new TIntObjectHashMap<>();
        for (Map.Entry<Short, OreDictValueItem> metaItem : ITEMS.entrySet()) {
            OrePrefix prefix = metaItem.getValue().orePrefix;
            MaterialIconSet materialIconSet = metaItem.getValue().materialIconSet;
            if (prefix.materialIconType == null || DISALLOWED_TYPES.contains(prefix.materialIconType))
                continue;
            int registrationKey = prefix.id * 1000 + materialIconSet.id;
            if (!alreadyRegistered.containsKey(registrationKey)) {
                prefix.materialIconType.getItemModelPath(materialIconSet);
                ResourceLocation resourceLocation = prefix.materialIconType.getItemModelPath(materialIconSet);
                ModelBakery.registerItemVariants(this, resourceLocation);
                alreadyRegistered.put(registrationKey, new ModelResourceLocation(resourceLocation, "inventory"));
            }
            ModelResourceLocation resourceLocation = alreadyRegistered.get(registrationKey);
            metaItemsModels.put(metaItem.getKey(), resourceLocation);
        }
    }

    public static class OreDictValueItem {

        private final String materialName;
        private final int materialRGB;
        private final MaterialIconSet materialIconSet;
        private final short id;
        private final OrePrefix orePrefix;

        public OreDictValueItem(short id, String materialName, int materialRGB, MaterialIconSet materialIconSet, OrePrefix orePrefix) {
            this.id = id;
            this.materialName = materialName;
            this.materialRGB = materialRGB;
            this.materialIconSet = materialIconSet;
            this.orePrefix = orePrefix;
            ITEMS.put(id, this);
        }

        public String getOre() {
            return orePrefix.name() + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, materialName);
        }

        public ItemStack getItemStack(int amount) {
            ItemStack stack = OreDictUnifier.get(getOre());
            stack.setCount(amount);
            return stack;
        }

        public ItemStack getItemStack() {
            return getItemStack(1);
        }

        public String getName() {
            return materialName + '_' + GTUtility.toLowerCaseUnderscore(orePrefix.name());
        }
    }
}
