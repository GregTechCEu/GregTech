package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemColorProvider;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.items.metaitem.stats.IItemNameProvider;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.IngotMaterial;
import gregtech.api.unification.material.type.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class AbstractMaterialPartBehavior implements IItemBehaviour, IItemDurabilityManager, IItemColorProvider, IItemNameProvider {

    protected NBTTagCompound getPartStatsTag(ItemStack itemStack) {
        return itemStack.getSubCompound("GT.PartStats");
    }

    protected NBTTagCompound getOrCreatePartStatsTag(ItemStack itemStack) {
        return itemStack.getOrCreateSubCompound("GT.PartStats");
    }

    public IngotMaterial getPartMaterial(ItemStack itemStack) {
        NBTTagCompound compound = getPartStatsTag(itemStack);
        IngotMaterial defaultMaterial = Materials.Neutronium;
        if (compound == null || !compound.hasKey("Material", NBT.TAG_STRING)) {
            return defaultMaterial;
        }
        String materialName = compound.getString("Material");
        Material material = Material.MATERIAL_REGISTRY.getObject(materialName);
        if (!(material instanceof IngotMaterial)) {
            return defaultMaterial;
        }
        return (IngotMaterial) material;
    }

    public void setPartMaterial(ItemStack itemStack, IngotMaterial material) {
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

    public void setPartDamage(ItemStack itemStack, int rotorDamage) {
        NBTTagCompound compound = getOrCreatePartStatsTag(itemStack);
        compound.setInteger("Damage", Math.min(getPartMaxDurability(itemStack), rotorDamage));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack itemStack, String unlocalizedName) {
        IngotMaterial material = getPartMaterial(itemStack);
        return I18n.format(unlocalizedName, material.getLocalizedName());
    }

    @Override
    public void addInformation(ItemStack stack, List<String> lines) {
        IngotMaterial material = getPartMaterial(stack);
        int maxRotorDurability = getPartMaxDurability(stack);
        int rotorDamage = getPartDamage(stack);
        lines.add(I18n.format("metaitem.tool.tooltip.durability", maxRotorDurability - rotorDamage, maxRotorDurability));
        lines.add(I18n.format("metaitem.tool.tooltip.primary_material", material.getLocalizedName(), material.harvestLevel));
    }

    @Override
    public int getItemStackColor(ItemStack itemStack, int tintIndex) {
        IngotMaterial material = getPartMaterial(itemStack);
        return material.materialRGB;
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
