package gregtech.common.terminal.app.console.widget;

import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Translation;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.RenderUtil;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.render.scene.FBOWorldSceneRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.vecmath.Vector3f;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/23/19:21
 * @Description:
 */
public class MachineSceneWidget extends Widget {
    @SideOnly(Side.CLIENT)
    private FBOWorldSceneRenderer worldSceneRenderer;
    protected MetaTileEntity mte;
    protected final BlockPos pos;
    private boolean dragging;
    private int lastMouseX;
    private int lastMouseY;
    private float rotationYaw;
    private float rotationPitch;
    private float zoom;


    public MachineSceneWidget(int x, int y, int width, int height, BlockPos pos, boolean isClient) {
        super(x, y, width, height);
        this.pos = pos;
        if (isClient) {
            zoom = 5;
            rotationYaw = 45;
        }
    }

    @SideOnly(Side.CLIENT)
    private void renderBlockOverLay(RayTraceResult rayTraceResult) {
        BlockPos pos = rayTraceResult.getBlockPos();
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
        if (mte == null) {
            World world = this.gui.entityPlayer.world;
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof MetaTileEntityHolder && ((MetaTileEntityHolder) tileEntity).getMetaTileEntity() != null) {
                mte = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
                if (worldSceneRenderer != null) {
                    worldSceneRenderer.releaseFBO();
                }
                worldSceneRenderer = new FBOWorldSceneRenderer(world,1080, 1080);
                worldSceneRenderer.setCameraLookAt(new Vector3f(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f), zoom, Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
                worldSceneRenderer.setOnLookingAt(this::renderBlockOverLay);
                worldSceneRenderer.addRenderedBlocks(Collections.singletonList(pos), null);
                Set<BlockPos> around = new HashSet<>();
                for (EnumFacing facing : EnumFacing.VALUES) {
                    around.add(pos.offset(facing));
                }
                worldSceneRenderer.addRenderedBlocks(around, this::aroundBlocksRenderHook);
            }
        }
    }

    private boolean aroundBlocksRenderHook(boolean isTESR, int pass, BlockRenderLayer layer) {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_CONSTANT_COLOR);
        GL14.glBlendColor(1f, 1f, 1f, 1f);
        return true;
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
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (isMouseOverElement(mouseX, mouseY)) {
            zoom = (float) MathHelper.clamp(zoom + (wheelDelta < 0 ? 0.5 : -0.5), 3, 10);
            worldSceneRenderer.setCameraLookAt(new Vector3f(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f), zoom, Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (dragging) {
            rotationPitch += mouseX - lastMouseX + 360;
            rotationPitch = rotationPitch % 360;
            rotationYaw = (float) MathHelper.clamp(rotationYaw + (mouseY - lastMouseY), -89.9, 89.9);
            lastMouseY = mouseY;
            lastMouseX = mouseX;
            worldSceneRenderer.setCameraLookAt(new Vector3f(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f), zoom, Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
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
//        TerminalTheme.COLOR_B_2.draw(x, y, width, height);
        drawSolidRect(x, y, width, height, 0xaf000000);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        worldSceneRenderer.render(x, y, width, height, mouseX - x, mouseY - y);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }
}
