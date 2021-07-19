package gregtech.common.items;

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

public class MetaOrePrefix extends StandardMetaItem {

    // TODO: Clear this up of basically copy pasted code from MaterialMetaItem.java
    private final ArrayList<Short> generatedItems = new ArrayList<>();
    private final ArrayList<ItemStack> items = new ArrayList<>();
    private OrePrefix prefix;

    public MetaOrePrefix(OrePrefix orePrefix) {
        super((short) 32767);
        this.prefix = orePrefix;
        for (Material material : Material.MATERIAL_REGISTRY) {
            short i = (short) Material.MATERIAL_REGISTRY.getIDForObject(material);
            if (orePrefix != null && canGenerate(orePrefix, material)) {
                generatedItems.add(i);
            }
        }
    }

    public void registerOreDict() {
        for (short metaItem : generatedItems) {
            Material material = Material.MATERIAL_REGISTRY.getObjectById(metaItem);
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

    protected boolean canGenerate(OrePrefix orePrefix, Material material) {
        return orePrefix.doGenerateItem(material);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack itemStack) {
        if (itemStack.getItemDamage() < metaItemOffset) {
            Material material = Material.MATERIAL_REGISTRY.getObjectById(itemStack.getItemDamage());
            if (material == null || prefix == null) return "";
            return prefix.getLocalNameForItem(material);
        }
        return super.getItemStackDisplayName(itemStack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        if (tintIndex == 0 && stack.getMetadata() < metaItemOffset) {
            Material material = Material.MATERIAL_REGISTRY.getObjectById(stack.getMetadata());
            if (material == null) return 0xFFFFFF;
            return material.materialRGB;
        }
        return super.getColorForItemStack(stack, tintIndex);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        super.registerModels();
        TShortObjectHashMap<ModelResourceLocation> alreadyRegistered = new TShortObjectHashMap<>();
        for (short metaItem : generatedItems) {
            MaterialIconSet materialIconSet = Material.MATERIAL_REGISTRY.getObjectById(metaItem).materialIconSet;
            short registrationKey = (short) (prefix.ordinal() + materialIconSet.ordinal());
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
    public int getItemStackLimit(ItemStack stack) {
        if (stack.getItemDamage() < metaItemOffset) {
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
            Material material = Material.MATERIAL_REGISTRY.getObjectById(damage);;
            if (prefix == null || material == null) return;
            addMaterialTooltip(itemStack, prefix, material, lines, tooltipFlag);
        }
    }

    public Material getMaterial(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        if (damage < this.metaItemOffset) {
            return Material.MATERIAL_REGISTRY.getObjectById(damage);
        }
        return null;
    }

    public OrePrefix getOrePrefix(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        if (damage < this.metaItemOffset) {
            return this.prefix;
        }
        return null;
    }

    @Override
    public int getItemBurnTime(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        if (damage < this.metaItemOffset) {
            Material material = Material.MATERIAL_REGISTRY.getObjectById(damage);
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
