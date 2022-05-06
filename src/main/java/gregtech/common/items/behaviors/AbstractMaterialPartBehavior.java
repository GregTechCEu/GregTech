package gregtech.common.items.behaviors;

import gregtech.api.GregTechAPI;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemColorProvider;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.items.metaitem.stats.IItemNameProvider;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.LocalizationUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.List;

public abstract class AbstractMaterialPartBehavior implements IItemBehaviour, IItemDurabilityManager, IItemColorProvider, IItemNameProvider {

    protected NBTTagCompound getPartStatsTag(ItemStack itemStack) {
        return itemStack.getSubCompound("GT.PartStats");
    }

    protected NBTTagCompound getOrCreatePartStatsTag(ItemStack itemStack) {
        return itemStack.getOrCreateSubCompound("GT.PartStats");
    }

    public Material getPartMaterial(ItemStack itemStack) {
        NBTTagCompound compound = getPartStatsTag(itemStack);
        Material defaultMaterial = Materials.Neutronium;
        if (compound == null || !compound.hasKey("Material", NBT.TAG_STRING)) {
            return defaultMaterial;
        }
        String materialName = compound.getString("Material");
        Material material = GregTechAPI.MATERIAL_REGISTRY.getObject(materialName);
        if (material == null || !material.hasProperty(PropertyKey.INGOT)) {
            return defaultMaterial;
        }
        return material;
    }

    public void setPartMaterial(ItemStack itemStack, Material material) {
        if (!material.hasProperty(PropertyKey.INGOT))
            throw new IllegalArgumentException("Part material must have an Ingot!");
        NBTTagCompound compound = getOrCreatePartStatsTag(itemStack);
        compound.setString("Material", material.toString());
    }

    public abstract int getPartMaxDurability(ItemStack itemStack);

    public int getPartDamage(ItemStack itemStack) {
        NBTTagCompound compound = getPartStatsTag(itemStack);
        if (compound == null || !compound.hasKey("Damage", NBT.TAG_ANY_NUMERIC)) {
            return 0;
        }
        return compound.getInteger("Damage");
    }

    public void setPartDamage(ItemStack itemStack, int damage) {
        NBTTagCompound compound = getOrCreatePartStatsTag(itemStack);
        compound.setInteger("Damage", Math.min(getPartMaxDurability(itemStack), damage));
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack, String unlocalizedName) {
        Material material = getPartMaterial(itemStack);
        return LocalizationUtils.format(unlocalizedName, material.getLocalizedName());
    }

    @Override
    public void addInformation(ItemStack stack, List<String> lines) {
        Material material = getPartMaterial(stack);
        int maxDurability = getPartMaxDurability(stack);
        int damage = getPartDamage(stack);
        lines.add(I18n.format("metaitem.tool.tooltip.durability", maxDurability - damage, maxDurability));
        lines.add(I18n.format("metaitem.tool.tooltip.primary_material", material.getLocalizedName(), material.getToolHarvestLevel()));
    }

    @Override
    public int getItemStackColor(ItemStack itemStack, int tintIndex) {
        Material material = getPartMaterial(itemStack);
        return material.getMaterialRGB();
    }

    @Override
    public boolean showsDurabilityBar(ItemStack itemStack) {
        return getPartDamage(itemStack) > 0;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        return getPartDamage(itemStack) / (getPartMaxDurability(itemStack) * 1.0);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack itemStack) {
        return MathHelper.hsvToRGB((1.0f - (float) getDurabilityForDisplay(itemStack)) / 3.0f, 1.0f, 1.0f);
    }
}
