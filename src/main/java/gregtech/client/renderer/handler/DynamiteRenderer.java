package gregtech.client.renderer.handler;

import gregtech.common.entities.DynamiteEntity;
import gregtech.common.items.MetaItems;

import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class DynamiteRenderer extends RenderSnowball<DynamiteEntity> {

    public DynamiteRenderer(RenderManager renderManagerIn, RenderItem itemRendererIn) {
        super(renderManagerIn, MetaItems.DYNAMITE.getMetaItem(), itemRendererIn);
    }

    @NotNull
    public ItemStack getStackToRender(@NotNull DynamiteEntity entityIn) {
        return MetaItems.DYNAMITE.getStackForm();
    }
}
