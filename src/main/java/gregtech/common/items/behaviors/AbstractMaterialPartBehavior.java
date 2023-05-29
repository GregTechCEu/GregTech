package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemColorProvider;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.items.metaitem.stats.IItemNameProvider;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.MaterialHelpers;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.LocalizationUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class AbstractMaterialPartBehavior implements IItemBehaviour, IItemDurabilityManager, IItemColorProvider, IItemNameProvider {

    protected static NBTTagCompound getPartStatsTag(ItemStack itemStack) {
        return itemStack.getSubCompound("GT.PartStats");
    }

    protected static NBTTagCompound getOrCreatePartStatsTag(ItemStack itemStack) {
        return itemStack.getOrCreateSubCompound("GT.PartStats");
    }

    public static Material getPartMaterial(ItemStack itemStack) {
        NBTTagCompound compound = getPartStatsTag(itemStack);
        Material defaultMaterial = Materials.Neutronium;
        if (compound == null || !compound.hasKey("Material", NBT.TAG_STRING)) {
            return defaultMaterial;
        }
        String materialName = compound.getString("Material");
        Material material = MaterialHelpers.getMaterial(materialName);
        if (material == null || !material.hasProperty(PropertyKey.INGOT)) {
            return defaultMaterial;
        }
        return material;
    }

    public static void setPartMaterial(ItemStack itemStack, @Nonnull Material material) {
        if (!material.hasProperty(PropertyKey.INGOT))
            throw new IllegalArgumentException("Part material must have an Ingot!");
        NBTTagCompound compound = getOrCreatePartStatsTag(itemStack);
        compound.setString("Material", material.getUnlocalizedName());
    }

    public abstract int getPartMaxDurability(ItemStack itemStack);

    public static int getPartDamage(ItemStack itemStack) {
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
        lines.add(I18n.format("metaitem.tool.tooltip.primary_material", material.getLocalizedName()));
    }

    @Override
    public int getItemStackColor(ItemStack itemStack, int tintIndex) {
        Material material = getPartMaterial(itemStack);
        return material.getMaterialRGB();
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        int maxDurability = getPartMaxDurability(itemStack);
        return (double) (maxDurability - getPartDamage(itemStack)) / (double) maxDurability;
    }
}
