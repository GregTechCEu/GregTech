package gregtech.api.newgui.widgets;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import gregtech.api.pipenet.tile.PipeCoverableImplementation;
import gregtech.api.util.FacingPos;
import gregtech.common.covers.CoverDigitalInterface;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ProxyDisplayWidget extends ButtonWidget<ProxyDisplayWidget> {

    private ItemStack item;
    private FacingPos facingPos;
    private IKey posTexture;

    public static ProxyDisplayWidget make(CoverDigitalInterface digitalInterface) {
        ItemStack itemStack = digitalInterface.coverHolder.getStackForm();
        BlockPos pos = digitalInterface.coverHolder.getPos();
        if (digitalInterface.coverHolder instanceof PipeCoverableImplementation) {
            itemStack = null;
            pos = pos.offset(digitalInterface.attachedSide);
            TileEntity tileEntity = digitalInterface.coverHolder.getWorld().getTileEntity(pos);
            IBlockState state = digitalInterface.coverHolder.getWorld().getBlockState(pos);
            if (tileEntity != null) {
                itemStack = tileEntity.getBlockType().getItem(digitalInterface.coverHolder.getWorld(), pos, state);
            }
            if (itemStack == null) return null;
        }
        return new ProxyDisplayWidget(itemStack, new FacingPos(pos, digitalInterface.attachedSide));
    }

    public ProxyDisplayWidget(ItemStack item, FacingPos facingPos) {
        this.item = item;
        this.facingPos = facingPos;
        BlockPos pos = facingPos.getPos();
        this.posTexture = IKey.str(String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ()));
        size(86, 18);
    }

    public ProxyDisplayWidget() {
        this.item = new ItemStack(Blocks.BARRIER);
        this.posTexture = IKey.str(" - - - ");
        size(86, 18);
    }

    public void set(ProxyDisplayWidget widget) {
        this.item = widget.item;
        this.facingPos = widget.facingPos;
        this.posTexture = widget.posTexture;
    }

    @Override
    public void draw(GuiContext context) {
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.zLevel = 200;
        renderItem.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().player, this.item, 1, 1);
        renderItem.zLevel = 0;
        GlStateManager.disableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.disableLighting();
        this.posTexture.draw(context, 18, 0, getArea().width - 18, 18);
    }

    public FacingPos getFacingPos() {
        return this.facingPos;
    }
}
