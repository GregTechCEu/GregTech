package gregtech.api.items.materialitem;

import gnu.trove.map.hash.TShortObjectHashMap;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaPrefixItem extends StandardMetaItem {

    private final OrePrefix prefix;

    public static final Map<OrePrefix, OrePrefix> purifyMap = new HashMap<OrePrefix, OrePrefix>() {{
        put(OrePrefix.crushed, OrePrefix.crushedPurified);
        put(OrePrefix.dustImpure, OrePrefix.dust);
        put(OrePrefix.dustPure, OrePrefix.dust);
    }};

    public MetaPrefixItem(OrePrefix orePrefix) {
        super();
        this.prefix = orePrefix;
    }

    @Override
    public void registerSubItems() {
        for (Material material : GregTechAPI.MATERIAL_REGISTRY) {
            short i = (short) GregTechAPI.MATERIAL_REGISTRY.getIDForObject(material);
            if (prefix != null && canGenerate(prefix, material)) {
                addItem(i, new UnificationEntry(prefix, material).toString());
            }
        }
    }

    public void registerOreDict() {
        for (short metaItem : metaItems.keySet()) {
            Material material = GregTechAPI.MATERIAL_REGISTRY.getObjectById(metaItem);
            ItemStack item = new ItemStack(this, 1, metaItem);
            OreDictUnifier.registerOre(item, prefix, material);
            registerSpecialOreDict(item, material, prefix);
        }
    }

    private void registerSpecialOreDict(ItemStack item, Material material, OrePrefix prefix) {
        if (prefix.getAlternativeOreName() != null) {
            OreDictUnifier.registerOre(item, prefix.getAlternativeOreName(), material);
        }

        if (material == Materials.Plutonium239) {
            OreDictUnifier.registerOre(item, prefix.name() + material.toCamelCaseString() + "239");
        } else if (material == Materials.Uranium238) {
            OreDictUnifier.registerOre(item, prefix.name() + material.toCamelCaseString() + "238");
        } else if (material == Materials.Saltpeter) {
            OreDictUnifier.registerOre(item, prefix.name() + material.toCamelCaseString());
        }
    }

    protected boolean canGenerate(OrePrefix orePrefix, Material material) {
        return orePrefix.doGenerateItem(material);
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        Material material = GregTechAPI.MATERIAL_REGISTRY.getObjectById(itemStack.getItemDamage());
        if (material == null || prefix == null) return "";
        return prefix.getLocalNameForItem(material);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            Material material = GregTechAPI.MATERIAL_REGISTRY.getObjectById(stack.getMetadata());
            if (material == null)
                return 0xFFFFFF;
            return material.getMaterialRGB();
        }
        return super.getColorForItemStack(stack, tintIndex);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void registerModels() {
        TShortObjectHashMap<ModelResourceLocation> alreadyRegistered = new TShortObjectHashMap<>();
        for (short metaItem : metaItems.keySet()) {
            MaterialIconSet materialIconSet = GregTechAPI.MATERIAL_REGISTRY.getObjectById(metaItem).getMaterialIconSet();

            short registrationKey = (short) (prefix.id + materialIconSet.id);
            if (!alreadyRegistered.containsKey(registrationKey)) {
                ResourceLocation resourceLocation = prefix.materialIconType.getItemModelPath(materialIconSet);
                ModelBakery.registerItemVariants(this, resourceLocation);
                alreadyRegistered.put(registrationKey, new ModelResourceLocation(resourceLocation, "inventory"));
            }
            ModelResourceLocation resourceLocation = alreadyRegistered.get(registrationKey);
            metaItemsModels.put(metaItem, resourceLocation);
        }

        // Make some default model for meta prefix items without any materials associated
        if (metaItems.keySet().isEmpty()) {
            MaterialIconSet defaultIcon = MaterialIconSet.DULL;
            ResourceLocation defaultLocation = OrePrefix.ingot.materialIconType.getItemModelPath(defaultIcon);
            ModelBakery.registerItemVariants(this, defaultLocation);
        }
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        if (prefix == null)
            return 64;
        return prefix.maxStackSize;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> subItems) {
        if (tab == GregTechAPI.TAB_GREGTECH_MATERIALS || tab == CreativeTabs.SEARCH) {
            for (MetaItem<?>.MetaValueItem enabledItem : metaItems.values()) {
                if (!enabledItem.isVisible())
                    continue;
                ItemStack itemStack = enabledItem.getStackForm();
                enabledItem.getSubItemHandler().getSubItems(itemStack, tab, subItems);
            }
        }
    }

    @Override
    public void onUpdate(@Nonnull ItemStack itemStack, @Nonnull World worldIn, @Nonnull Entity entityIn, int itemSlot, boolean isSelected) {
        super.onUpdate(itemStack, worldIn, entityIn, itemSlot, isSelected);
        if (metaItems.containsKey((short) itemStack.getItemDamage()) && entityIn instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) entityIn;
            if (entityIn.ticksExisted % 20 == 0) {
                if (prefix.heatDamageFunction == null) return;

                Material material = getMaterial(itemStack);
                if (material == null || !material.hasProperty(PropertyKey.BLAST)) return;

                float heatDamage = prefix.heatDamageFunction.apply(material.getBlastTemperature());
                ItemStack armor = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                if (!armor.isEmpty() && armor.getItem() instanceof ArmorMetaItem<?>) {
                    heatDamage *= ((ArmorMetaItem<?>) armor.getItem()).getItem(armor).getArmorLogic().getHeatResistance();
                }

                if (heatDamage > 0.0) {
                    entity.attackEntityFrom(DamageSources.getHeatDamage().setDamageBypassesArmor(), heatDamage);
                } else if (heatDamage < 0.0) {
                    entity.attackEntityFrom(DamageSources.getFrostDamage().setDamageBypassesArmor(), -heatDamage);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemStack, @Nullable World worldIn, @Nonnull List<String> lines, @Nonnull ITooltipFlag tooltipFlag) {
        super.addInformation(itemStack, worldIn, lines, tooltipFlag);
        int damage = itemStack.getItemDamage();
        Material material = GregTechAPI.MATERIAL_REGISTRY.getObjectById(damage);
        if (prefix == null || material == null) return;
        addMaterialTooltip(lines, itemStack);
    }

    public Material getMaterial(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        return GregTechAPI.MATERIAL_REGISTRY.getObjectById(damage);
    }

    public OrePrefix getOrePrefix() {
        return this.prefix;
    }

    @Override
    public int getItemBurnTime(@Nonnull ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        Material material = GregTechAPI.MATERIAL_REGISTRY.getObjectById(damage);
        DustProperty property = material == null ? null : material.getProperty(PropertyKey.DUST);
        if (property != null) return (int) (property.getBurnTime() * prefix.getMaterialAmount(material) / GTValues.M);
        return super.getItemBurnTime(itemStack);

    }

    @Override
    public boolean isBeaconPayment(ItemStack stack) {
        int damage = stack.getMetadata();

        Material material = GregTechAPI.MATERIAL_REGISTRY.getObjectById(damage);
        if (this.prefix != null && material != null) {
            boolean isSolidState = this.prefix == OrePrefix.ingot || this.prefix == OrePrefix.gem;
            DustProperty property = material.getProperty(PropertyKey.DUST);
            boolean isMaterialTiered = property != null && property.getHarvestLevel() >= 2;
            return isSolidState && isMaterialTiered;
        }
        return false;
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem itemEntity) {
        int damage = itemEntity.getItem().getMetadata();
        if (itemEntity.getEntityWorld().isRemote)
            return false;

        Material material = GregTechAPI.MATERIAL_REGISTRY.getObjectById(damage);
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

    protected void addMaterialTooltip(List<String> lines, ItemStack itemStack) {
        if (this.prefix.tooltipFunc != null) {
            lines.addAll(this.prefix.tooltipFunc.apply(this.getMaterial(itemStack)));
        }
    }
}
