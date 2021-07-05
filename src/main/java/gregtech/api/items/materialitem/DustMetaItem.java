package gregtech.api.items.materialitem;

import gnu.trove.map.hash.TShortObjectHashMap;
import gregtech.api.GregTechAPI;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MaterialIconSet;
import gregtech.api.unification.material.type.SimpleDustMaterial;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

import static gregtech.api.unification.material.type.SimpleDustMaterial.GENERATE_SMALL_TINY;

public class DustMetaItem extends StandardMetaItem {

    private final OrePrefix[] orePrefixes = {OrePrefix.dustTiny, OrePrefix.dustSmall, OrePrefix.dust};
    private final ArrayList<Short> generatedItems = new ArrayList<>();
    //private final ArrayList<ItemStack> items = new ArrayList<>(); TODO For MetaBracketHandler?

    public DustMetaItem() {
        super((short) 0);
        for (SimpleDustMaterial material : SimpleDustMaterial.MATERIAL_REGISTRY) {
            int i = SimpleDustMaterial.MATERIAL_REGISTRY.getIDForObject(material);
            for (int j = 0; j < orePrefixes.length; j++) {
                if (j == 2 || material.hasFlag(GENERATE_SMALL_TINY)) {
                    generatedItems.add((short) (i * 3 + j));
                }
            }
        }
    }

    public void registerOreDict() {
        for (short metaItem : generatedItems) {
            OrePrefix prefix = orePrefixes[metaItem % 3];
            SimpleDustMaterial material = SimpleDustMaterial.MATERIAL_REGISTRY.getObjectById(metaItem / 3);
            ItemStack item = new ItemStack(this, 1, metaItem);
            OreDictUnifier.registerOre(item, prefix, material);
            if (prefix == OrePrefix.dust)
                OreDictUnifier.registerOre(item, OrePrefix.DUST_REGULAR, material);
            //items.add(item);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        super.registerModels();
        TShortObjectHashMap<ModelResourceLocation> alreadyRegistered = new TShortObjectHashMap<>();
        for (short metaItem : generatedItems) {
            OrePrefix prefix = orePrefixes[metaItem % 3];
            MaterialIconSet materialIconSet = SimpleDustMaterial.MATERIAL_REGISTRY.getObjectById((metaItem - (metaItem % 3)) / 3).iconSet;
            if (!alreadyRegistered.containsKey(metaItem)) {
                ResourceLocation resourceLocation = prefix.materialIconType.getItemModelPath(materialIconSet);
                ModelBakery.registerItemVariants(this, resourceLocation);
                alreadyRegistered.put(metaItem, new ModelResourceLocation(resourceLocation, "inventory"));
            }
            ModelResourceLocation resourceLocation = alreadyRegistered.get(metaItem);
            metaItemsModels.put(metaItem, resourceLocation);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            SimpleDustMaterial material = SimpleDustMaterial.MATERIAL_REGISTRY.getObjectById(stack.getMetadata() / 3);
            if (material == null) return 0xFFFFFF;
            return material.materialRGB;
        }
        return super.getColorForItemStack(stack, tintIndex);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack itemStack) {
        SimpleDustMaterial material = SimpleDustMaterial.MATERIAL_REGISTRY.getObjectById(itemStack.getItemDamage() / 3);
        OrePrefix prefix = orePrefixes[itemStack.getItemDamage() % 3];
        if (material == null || prefix == null) return "";
        return prefix.getLocalNameForItem(material);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        super.getSubItems(tab, subItems);
        if (tab == GregTechAPI.TAB_GREGTECH_MATERIALS || tab == CreativeTabs.SEARCH) {
            for (short metadata : generatedItems) {
                subItems.add(new ItemStack(this, 1, metadata));
            }
        }
    }
}
