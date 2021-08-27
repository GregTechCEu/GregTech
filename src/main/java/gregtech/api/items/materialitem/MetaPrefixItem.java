package gregtech.api.items.materialitem;

import gnu.trove.map.hash.TShortObjectHashMap;
import gregtech.GregTechRegistries;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaPrefixItem extends StandardMetaItem {

    public static final Map<OrePrefix, OrePrefix> PURIFY_MAP;

    static {
        PURIFY_MAP = new Object2ObjectArrayMap<>(3);
        PURIFY_MAP.put(OrePrefix.crushed, OrePrefix.crushedPurified);
        PURIFY_MAP.put(OrePrefix.dustImpure, OrePrefix.dust);
        PURIFY_MAP.put(OrePrefix.dustPure, OrePrefix.dust);
    }

    private final ShortList generatedItems = new ShortArrayList();
    private final OrePrefix prefix;

    public MetaPrefixItem(OrePrefix orePrefix) {
        super();
        this.prefix = orePrefix;
        for (Material material : GregTechRegistries.getMaterialRegistry()) {
            short i = (short) GregTechRegistries.getMaterialRegistry().getIDForObject(material);
            if (orePrefix != null && canGenerate(orePrefix, material)) {
                generatedItems.add(i);
            }
        }
    }

    public void registerOreDict() {
        for (short metaItem : generatedItems) {
            Material material = GregTechRegistries.getMaterialRegistry().getObjectById(metaItem);
            ItemStack item = new ItemStack(this, 1, metaItem);
            OreDictUnifier.registerOre(item, prefix, material);
            registerSpecialOreDict(item, material, prefix);
        }
    }

    private void registerSpecialOreDict(ItemStack item, Material material, OrePrefix prefix) {
        if (prefix.getAlternativeOreName() != null) {
            OreDictUnifier.registerOre(item, prefix.getAlternativeOreName(), material);
        }
        if (prefix.equals(OrePrefix.dust)) OreDictUnifier.registerOre(item, OrePrefix.DUST_REGULAR, material);

        if (material == Materials.Plutonium239) {
            OreDictUnifier.registerOre(item, prefix.name() + material.toCamelCaseString() + "239");
        } else if (material == Materials.Uranium238) {
            OreDictUnifier.registerOre(item, prefix.name() + material.toCamelCaseString() + "238");
        }
    }

    protected boolean canGenerate(OrePrefix orePrefix, Material material) {
        return orePrefix.doGenerateItem(material);
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        Material material = GregTechRegistries.getMaterialRegistry().getObjectById(itemStack.getItemDamage());
        if (material == null || prefix == null) return "";
        return prefix.getLocalNameForItem(material);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            Material material = GregTechRegistries.getMaterialRegistry().getObjectById(stack.getMetadata());
            if (material == null) {
                return 0xFFFFFF;
            }
            return material.getMaterialRGB();
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
            MaterialIconSet materialIconSet = GregTechRegistries.getMaterialRegistry().getObjectById(metaItem).getMaterialIconSet();

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
        Material material = GregTechRegistries.getMaterialRegistry().getObjectById(damage);
        if (prefix == null || material == null) return;
        addMaterialTooltip(lines);
    }

    public Material getMaterial(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        return GregTechRegistries.getMaterialRegistry().getObjectById(damage);
    }

    public OrePrefix getOrePrefix() {
        return this.prefix;
    }

    @Override
    public int getItemBurnTime(ItemStack itemStack) {
        int damage = itemStack.getItemDamage();
        Material material = GregTechRegistries.getMaterialRegistry().getObjectById(damage);
        DustProperty property = material == null ? null : material.getProperty(PropertyKey.DUST);
        if (property != null) return (int) (property.getBurnTime() * prefix.materialAmount / GTValues.M);
        return super.getItemBurnTime(itemStack);

    }

    @Override
    public boolean isBeaconPayment(ItemStack stack) {
        int damage = stack.getMetadata();

        Material material = GregTechRegistries.getMaterialRegistry().getObjectById(damage);
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
        if (itemEntity.getEntityWorld().isRemote) {
            return false;
        }
        OrePrefix purifyInto = PURIFY_MAP.get(this.prefix);
        if (purifyInto == null) {
            return false;
        }
        Material material = GregTechRegistries.getMaterialRegistry().getObjectById(itemEntity.getItem().getMetadata());
        BlockPos blockPos = new BlockPos(itemEntity);
        IBlockState state = itemEntity.getEntityWorld().getBlockState(blockPos);
        if (!(state.getBlock() instanceof BlockCauldron)) {
            return false;
        }
        int waterLevel = state.getValue(BlockCauldron.LEVEL);
        if (waterLevel == 0) {
            return false;
        }
        itemEntity.getEntityWorld().setBlockState(blockPos, state.withProperty(BlockCauldron.LEVEL, waterLevel - 1));
        ItemStack replacementStack = OreDictUnifier.get(purifyInto, material, itemEntity.getItem().getCount());
        itemEntity.setItem(replacementStack);
        return false;
    }

    protected void addMaterialTooltip(List<String> lines) {
        if (this.prefix == OrePrefix.dustImpure || this.prefix == OrePrefix.dustPure) {
            lines.add(I18n.format("metaitem.dust.tooltip.purify"));
        }
    }
}
