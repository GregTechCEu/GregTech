package gregtech.api.items.materialitem;

import gnu.trove.map.hash.TShortObjectHashMap;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MaterialIconSet;
import gregtech.api.unification.material.type.DustMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.material.type.SolidMaterial;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaPrefixItem extends StandardMetaItem {

    private final ArrayList<Short> generatedItems = new ArrayList<>();
    private final ArrayList<ItemStack> items = new ArrayList<>();
    private final OrePrefix prefix;

    public static final Map<OrePrefix, OrePrefix> purifyMap = new HashMap<OrePrefix, OrePrefix>() {{
        put(OrePrefix.crushed, OrePrefix.crushedPurified);
        put(OrePrefix.dustImpure, OrePrefix.dust);
        put(OrePrefix.dustPure, OrePrefix.dust);
    }};

    public MetaPrefixItem(OrePrefix orePrefix) {
        super();
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

    public List<ItemStack> getEntries() {
        return items;
    }

    protected boolean canGenerate(OrePrefix orePrefix, Material material) {
        return orePrefix.doGenerateItem(material);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack itemStack) {
        Material material = Material.MATERIAL_REGISTRY.getObjectById(itemStack.getItemDamage());
        if (material == null || prefix == null) return "";
        return prefix.getLocalNameForItem(material);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            Material material = Material.MATERIAL_REGISTRY.getObjectById(stack.getMetadata());
            if (material == null)
                return 0xFFFFFF;
            return material.materialRGB;
        }
        return super.getColorForItemStack(stack, tintIndex);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("ConstantConditions")
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

        // Make some default model for meta prefix items without any materials associated
        if (generatedItems.isEmpty()) {
            MaterialIconSet defaultIcon = MaterialIconSet.DULL;
            ResourceLocation defaultLocation = OrePrefix.ingot.materialIconType.getItemModelPath(defaultIcon);
            ModelBakery.registerItemVariants(this, defaultLocation);
        }
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        if (prefix == null)
            return 64;
        return prefix.maxStackSize;
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
        if (generatedItems.contains((short) itemStack.getItemDamage()) && entityIn instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) entityIn;
            if (worldIn.getTotalWorldTime() % 20 == 0) {
                if (prefix.heatDamage != 0.0 && prefix.heatDamage > 0.0) {
                    entity.attackEntityFrom(DamageSources.getHeatDamage(), prefix.heatDamage);
                } else if (prefix.heatDamage < 0.0) {
                    entity.attackEntityFrom(DamageSources.getFrostDamage(), -prefix.heatDamage);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, @Nullable World worldIn, List<String> lines, ITooltipFlag tooltipFlag) {
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);
        int damage = itemStack.getItemDamage();
        Material material = Material.MATERIAL_REGISTRY.getObjectById(damage);
        if (prefix == null || material == null) return;
        addMaterialTooltip(lines);
    }

    public Material getMaterial(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        return Material.MATERIAL_REGISTRY.getObjectById(damage);
    }

    public OrePrefix getOrePrefix() {
        return this.prefix;
    }

    @Override
    public int getItemBurnTime(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        Material material = Material.MATERIAL_REGISTRY.getObjectById(damage);
        if (material instanceof DustMaterial) {
            DustMaterial dustMaterial = (DustMaterial) material;
            return (int) (dustMaterial.burnTime * prefix.materialAmount / GTValues.M);
        }
        return super.getItemBurnTime(itemStack);

    }

    @Override
    public boolean isBeaconPayment(ItemStack stack) {
        int damage = stack.getMetadata();

        Material material = Material.MATERIAL_REGISTRY.getObjectById(damage);
        if (this.prefix != null && material != null) {
            boolean isSolidState = this.prefix == OrePrefix.ingot || this.prefix == OrePrefix.gem;
            boolean isMaterialTiered = material instanceof SolidMaterial && ((SolidMaterial) material).harvestLevel >= 2;
            return isSolidState && isMaterialTiered;
        }
        return false;
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem itemEntity) {
        int damage = itemEntity.getItem().getMetadata();
        if (itemEntity.getEntityWorld().isRemote)
            return false;

        Material material = Material.MATERIAL_REGISTRY.getObjectById(damage);
        if (!purifyMap.containsKey(this.prefix))
            return false;

        BlockPos blockPos = new BlockPos(itemEntity);
        IBlockState blockState = itemEntity.getEntityWorld().getBlockState(blockPos);

        if (!(blockState.getBlock() instanceof BlockCauldron))
            return false;

        int waterLevel = blockState.getValue(BlockCauldron.LEVEL);
        if (waterLevel == 0)
            return false;

        itemEntity.getEntityWorld().setBlockState(blockPos,
                blockState.withProperty(BlockCauldron.LEVEL, waterLevel - 1));
        ItemStack replacementStack = OreDictUnifier.get(purifyMap.get(prefix), material,
                itemEntity.getItem().getCount());
        itemEntity.setItem(replacementStack);
        return false;
    }

    protected void addMaterialTooltip(List<String> lines) {
        if (this.prefix == OrePrefix.dustImpure || this.prefix == OrePrefix.dustPure) {
            lines.add(I18n.format("metaitem.dust.tooltip.purify"));
        }
    }
}
