package gregtech.client.renderer.handler;

import gregtech.api.items.toolitem.ItemGTToolbelt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;

@SideOnly(Side.CLIENT)
public class ToolbeltRenderer implements IItemRenderer {

    private final IBakedModel toolbeltModel;

    public ToolbeltRenderer(IBakedModel toolbeltModel) {
        this.toolbeltModel = toolbeltModel;
    }

    @Override
    public void renderItem(ItemStack stack, ItemCameraTransforms.TransformType transformType) {
        if (stack.getItem() instanceof ItemGTToolbelt toolbelt) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5F, 0.5F, 0.5F);

            RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

            ItemStack selected = toolbelt.getSelectedTool(stack);
            if (!selected.isEmpty()) {
                IBakedModel selectedModel = renderItem.getItemModelWithOverrides(selected, null, null);
                renderItem.renderItem(selected, selectedModel);
            }
            renderItem.renderItem(stack, toolbeltModel);

            GlStateManager.popMatrix();
        }
    }

    @Override
    public IModelState getTransforms() {
        return TransformUtils.DEFAULT_TOOL;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }
}
