package gregtech.common.terminal.app.console.widget;

import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.RenderUtil;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.render.scene.WorldSceneRenderer;
import gregtech.api.render.scene.FBOWorldSceneRenderer;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.BlockInfo;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/23/19:21
 * @Description:
 */
public class MachineSceneWidget extends Widget {
    @SideOnly(Side.CLIENT)
    private static WorldSceneRenderer worldSceneRenderer;
    protected MetaTileEntity mte;
    protected final BlockPos pos;
    private boolean dragging;
    private int lastMouseX;
    private int lastMouseY;
    private float rotateY;
    private float rotateX;


    public MachineSceneWidget(int x, int y, int width, int height, BlockPos pos, boolean isClient) {
        super(x, y, width, height);
        this.pos = pos;
        if (isClient) {
            if (worldSceneRenderer instanceof  FBOWorldSceneRenderer) {
                ((FBOWorldSceneRenderer) worldSceneRenderer).releaseFBO();
            }
            worldSceneRenderer = new FBOWorldSceneRenderer(1080, 1080);
            worldSceneRenderer.setBeforeWorldRender(()->{
                Vector3 centerPosition = new Vector3(0.5, 0.5, 0.5);
                GlStateManager.translate(centerPosition.x, centerPosition.y, centerPosition.z);
                GlStateManager.scale(2.0, 2.0, 2.0);
                GlStateManager.rotate(0, 1.0f, 0.0f, 0.0f);
                GlStateManager.rotate(rotateY, 0.0f, 1.0f, 0.0f);
                GlStateManager.rotate(rotateX, 0.0f, 0.0f, 1.0f);
                GlStateManager.translate(-centerPosition.x, -centerPosition.y, -centerPosition.z);
            });
            worldSceneRenderer.setOnLookingAt(this::renderBlockOverLay);
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderBlockOverLay(WorldSceneRenderer.BlockPosFace pos) {
        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager.disableTexture2D();
        CCRenderState renderState = CCRenderState.instance();
        renderState.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR, tessellator.getBuffer());
        ColourMultiplier multiplier = new ColourMultiplier(0);
        renderState.setPipeline(new Translation(pos), multiplier);
        BlockRenderer.BlockFace blockFace = new BlockRenderer.BlockFace();
        renderState.setModel(blockFace);
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            float diffuse = LightUtil.diffuseLight(renderSide);
            int color = (int) (255 * diffuse);
            multiplier.colour = RenderUtil.packColor(color, color, color, 100);
            blockFace.loadCuboidFace(Cuboid6.full, renderSide.getIndex());
            renderState.render();
        }
        renderState.draw();
        GlStateManager.enableTexture2D();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (mte == null && pos != null) {
            World world = this.gui.entityPlayer.world;
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof MetaTileEntityHolder && ((MetaTileEntityHolder) tileEntity).getMetaTileEntity() != null) {
                mte = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
                for (int x = -1; x < 2; x++) {
                    for (int y = -1; y < 2; y++) {
                        for (int z = -1; z < 2; z++) {
                            BlockPos aroundPos = pos.add(x, y, z);
                            TileEntity te = world.getTileEntity(aroundPos);
                            if (te instanceof MetaTileEntityHolder && ((MetaTileEntityHolder) te).getMetaTileEntity() != null) {
                                MetaTileEntityHolder holder = (MetaTileEntityHolder) te;
                                MetaTileEntityHolder newHolder = new MetaTileEntityHolder();

                                newHolder.setMetaTileEntity(holder.getMetaTileEntity().createMetaTileEntity(newHolder));
                                worldSceneRenderer.addBlock(new BlockPos(x, y ,z), new BlockInfo(MetaBlocks.MACHINE.getDefaultState(), newHolder));
                                newHolder.getMetaTileEntity().setFrontFacing(holder.getMetaTileEntity().getFrontFacing());

                            } else {
                                worldSceneRenderer.addBlock(new BlockPos(x, y ,z), new BlockInfo(world.getBlockState(aroundPos)));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            dragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        dragging = false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (dragging) {
            rotateY += mouseX - lastMouseX;
            rotateX = MathHelper.clamp(rotateX - (mouseY - lastMouseY), -90, 90);
            lastMouseY = mouseY;
            lastMouseX = mouseX;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, timeDragged);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        TerminalTheme.COLOR_B_2.draw(x, y, width, height);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        worldSceneRenderer.render(x, y, width, height, mouseX - x, mouseY - y);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }
}
