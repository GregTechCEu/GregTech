package gregtech.core.hooks;

import gregtech.api.items.armor.IArmorItem;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class ArmorRenderHooks {

    public static boolean shouldNotRenderHeadItem(EntityLivingBase entityLivingBase) {
        ItemStack itemStack = entityLivingBase.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        return isArmorItem(itemStack, EntityEquipmentSlot.HEAD);
    }

    public static boolean isArmorItem(ItemStack itemStack, EntityEquipmentSlot slot) {
        return (itemStack.getItem() instanceof IArmorItem && itemStack.getItem().getEquipmentSlot(itemStack) == slot);
    }

    public static void renderArmorLayer(LayerArmorBase<ModelBase> layer, EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn) {
        ItemStack itemStack = entity.getItemStackFromSlot(slotIn);

        if (isArmorItem(itemStack, slotIn)) {
            IArmorItem armorItem = (IArmorItem) itemStack.getItem();
            ModelBase armorModel = layer.getModelFromSlot(slotIn);
            if (armorModel instanceof ModelBiped) {
                armorModel = ForgeHooksClient.getArmorModel(entity, itemStack, slotIn, (ModelBiped) armorModel);
            }
            armorModel.setModelAttributes(layer.renderer.getMainModel());
            armorModel.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
            layer.setModelSlotVisible(armorModel, slotIn);

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);

            int layers = armorItem.getArmorLayersAmount(itemStack);
            for (int layerIndex = 0; layerIndex < layers; layerIndex++) {
                int i = armorItem.getArmorLayerColor(itemStack, layerIndex);
                float f = (float) (i >> 16 & 255) / 255.0F;
                float f1 = (float) (i >> 8 & 255) / 255.0F;
                float f2 = (float) (i & 255) / 255.0F;
                GlStateManager.color(f, f1, f2, 1.0f);
                String type = layerIndex == 0 ? null : "layer_" + layerIndex;
                layer.renderer.bindTexture(getArmorTexture(entity, itemStack, slotIn, type));
                armorModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            }
            if (itemStack.hasEffect()) {
                LayerArmorBase.renderEnchantedGlint(layer.renderer, entity, armorModel, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
            }
        }
    }

    private static boolean isLegSlot(EntityEquipmentSlot equipmentSlot) {
        return equipmentSlot == EntityEquipmentSlot.LEGS;
    }

    private static ResourceLocation getArmorTexture(EntityLivingBase entity, ItemStack itemStack, EntityEquipmentSlot slot, String type) {
        ResourceLocation registryName = itemStack.getItem().getRegistryName();
        String s1 = String.format("%s:textures/models/armor/%s_layer_%d%s.png", registryName.getNamespace(), registryName.getPath(),
                (isLegSlot(slot) ? 2 : 1), type == null ? "" : String.format("_%s", type));
        return new ResourceLocation(ForgeHooksClient.getArmorTexture(entity, itemStack, s1, slot, type));
    }
}
