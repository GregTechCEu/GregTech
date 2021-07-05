package gregtech.api.items.materialitem;

import com.google.common.base.Preconditions;
import gnu.trove.map.hash.TShortObjectHashMap;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MaterialIconSet;
import gregtech.api.unification.material.type.DustMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MaterialMetaItem extends StandardMetaItem {

    protected OrePrefix[] orePrefixes;
    private final ArrayList<Short> generatedItems = new ArrayList<>();
    private final ArrayList<ItemStack> items = new ArrayList<>();

    public MaterialMetaItem(OrePrefix... orePrefixes) {
        super((short) (1000 * orePrefixes.length));
        Preconditions.checkArgument(orePrefixes.length <= 32, "Max allowed OrePrefix count on MaterialMetaItem is 32.");
        this.orePrefixes = orePrefixes;
        for (Material material : Material.MATERIAL_REGISTRY) {
            int i = Material.MATERIAL_REGISTRY.getIDForObject(material);
            for (int j = 0; j < orePrefixes.length; j++) {
                OrePrefix orePrefix = orePrefixes[j];
                if (orePrefix != null && canGenerate(orePrefix, material)) {
                    short metadata = (short) (j * 1000 + i);
                    generatedItems.add(metadata);
                }
            }
        }
    }

    public void registerOreDict() {
        for (short metaItem : generatedItems) {
            OrePrefix prefix = this.orePrefixes[metaItem / 1000];
            Material material = Material.MATERIAL_REGISTRY.getObjectById(metaItem % 1000);
            ItemStack item = new ItemStack(this, 1, metaItem);
            OreDictUnifier.registerOre(item, prefix, material);
            registerSpecialOreDict(item, material, prefix);
            items.add(item);
        }
    }

    private void registerSpecialOreDict(ItemStack item, Material material, OrePrefix prefix) {
        switch (prefix) {
            case dust: OreDictUnifier.registerOre(item, OrePrefix.DUST_REGULAR, material); break;
            case oreChunk: OreDictUnifier.registerOre(item, OrePrefix.oreGravel.name(), material); break;
            case oreEnderChunk: OreDictUnifier.registerOre(item, OrePrefix.oreEndstone.name(), material); break;
            case oreNetherChunk: OreDictUnifier.registerOre(item, OrePrefix.oreNetherrack.name(), material); break;
            case oreSandyChunk: OreDictUnifier.registerOre(item, OrePrefix.oreSand.name(), material); break;
        }
    }

    public List<ItemStack> getEntries() {
        return items;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        super.registerModels();
        TShortObjectHashMap<ModelResourceLocation> alreadyRegistered = new TShortObjectHashMap<>();
        for (short metaItem : generatedItems) {
            OrePrefix prefix = this.orePrefixes[metaItem / 1000];
            MaterialIconSet materialIconSet = Material.MATERIAL_REGISTRY.getObjectById(metaItem % 1000).materialIconSet;
            short registrationKey = (short) (metaItem / 1000 * 1000 + materialIconSet.ordinal());
            if (!alreadyRegistered.containsKey(registrationKey)) {
                ResourceLocation resourceLocation = prefix.materialIconType.getItemModelPath(materialIconSet);
                ModelBakery.registerItemVariants(this, resourceLocation);
                alreadyRegistered.put(registrationKey, new ModelResourceLocation(resourceLocation, "inventory"));
            }
            ModelResourceLocation resourceLocation = alreadyRegistered.get(registrationKey);
            metaItemsModels.put(metaItem, resourceLocation);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        if (tintIndex == 0 && stack.getMetadata() < metaItemOffset) {
            Material material = Material.MATERIAL_REGISTRY.getObjectById(stack.getMetadata() % 1000);
            if (material == null) return 0xFFFFFF;
            return material.materialRGB;
        }
        return super.getColorForItemStack(stack, tintIndex);
    }

    protected boolean canGenerate(OrePrefix orePrefix, Material material) {
        return orePrefix.doGenerateItem(material);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack itemStack) {
        if (itemStack.getItemDamage() < metaItemOffset) {
            Material material = Material.MATERIAL_REGISTRY.getObjectById(itemStack.getItemDamage() % 1000);
            OrePrefix prefix = orePrefixes[itemStack.getItemDamage() / 1000];
            if (material == null || prefix == null) return "";
            return prefix.getLocalNameForItem(material);
        }
        return super.getItemStackDisplayName(itemStack);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        if (stack.getItemDamage() < metaItemOffset) {
            OrePrefix prefix = orePrefixes[stack.getItemDamage() / 1000];
            if(prefix == null) return 64;
            return prefix.maxStackSize;
        }
        return super.getItemStackLimit(stack);
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

    @Override
    public void onUpdate(ItemStack itemStack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.onUpdate(itemStack, worldIn, entityIn, itemSlot, isSelected);
        if (itemStack.getItemDamage() < metaItemOffset && generatedItems.contains((short) itemStack.getItemDamage()) && entityIn instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) entityIn;
            OrePrefix prefix = orePrefixes[itemStack.getItemDamage() / 1000];
            if (worldIn.getTotalWorldTime() % 20 == 0) {
                if (prefix.heatDamage != 0.0) {
                    if (prefix.heatDamage > 0.0) {
                        entity.attackEntityFrom(DamageSources.getHeatDamage(), prefix.heatDamage);
                    } else if (prefix.heatDamage < 0.0) {
                        entity.attackEntityFrom(DamageSources.getFrostDamage(), -prefix.heatDamage);
                    }
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, @Nullable World worldIn, List<String> lines, ITooltipFlag tooltipFlag) {
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);
        int damage = itemStack.getItemDamage();
        if (damage < this.metaItemOffset) {
            Material material = Material.MATERIAL_REGISTRY.getObjectById(damage % 1000);
            OrePrefix prefix = this.orePrefixes[(damage / 1000)];
            if (prefix == null || material == null) return;
            addMaterialTooltip(itemStack, prefix, material, lines, tooltipFlag);
        }
    }

    public Material getMaterial(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        if (damage < this.metaItemOffset) {
            return Material.MATERIAL_REGISTRY.getObjectById(damage % 1000);
        }
        return null;
    }

    public OrePrefix getOrePrefix(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        if (damage < this.metaItemOffset) {
            return this.orePrefixes[(damage / 1000)];
        }
        return null;
    }

    @Override
    public int getItemBurnTime(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        if (damage < this.metaItemOffset) {
            Material material = Material.MATERIAL_REGISTRY.getObjectById(damage % 1000);
            OrePrefix prefix = this.orePrefixes[(damage / 1000)];
            if (material instanceof DustMaterial) {
                DustMaterial dustMaterial = (DustMaterial) material;
                return (int) (dustMaterial.burnTime * prefix.materialAmount / GTValues.M);
            }
        }
        return super.getItemBurnTime(itemStack);

    }

    protected void addMaterialTooltip(ItemStack itemStack, OrePrefix prefix, Material material, List<String> lines, ITooltipFlag tooltipFlag) {
    }
}
