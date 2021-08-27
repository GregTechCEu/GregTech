package gregtech.common.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import gregtech.api.cover.ICoverable;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.util.ModCompatibility;
import gregtech.common.covers.facade.FacadeRenderer;
import gregtech.common.items.behaviors.FacadeItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.IModelState;
import org.lwjgl.opengl.GL11;

public class FacadeItemModel implements IItemRenderer {

    @Override
    public void renderItem(ItemStack rawStack, TransformType transformType) {
        ItemStack itemStack = ModCompatibility.getRealItemStack(rawStack);
        if (!(itemStack.getItem() instanceof MetaItem<?>)) {
            return;
        }
        ItemStack facadeStack = FacadeItem.getFacadeStack(itemStack);
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        try {
            FacadeRenderer.renderItemCover(renderState, EnumFacing.NORTH.getIndex(), facadeStack, ICoverable.getCoverPlateBox(EnumFacing.NORTH, 2.0 / 16.0));
        } catch (Throwable ignored) {
        }
        renderState.draw();
    }

    @Override
    public IModelState getTransforms() {
        return TransformUtils.DEFAULT_BLOCK;
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
