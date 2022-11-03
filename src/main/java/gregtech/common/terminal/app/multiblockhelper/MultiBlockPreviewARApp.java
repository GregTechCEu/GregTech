package gregtech.common.terminal.app.multiblockhelper;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.ShaderTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.terminal.app.ARApplication;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.MachineSceneWidget;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.client.renderer.handler.MultiblockPreviewRenderer;
import gregtech.client.shader.Shaders;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.util.*;

public class MultiBlockPreviewARApp extends ARApplication {
    @SideOnly(Side.CLIENT)
    int lastMouseX;
    @SideOnly(Side.CLIENT)
    int lastMouseY;
    @SideOnly(Side.CLIENT)
    float partialTicks;

    public MultiBlockPreviewARApp() {
        super("multiblock_ar");
    }

    @Override
    public AbstractApplication initApp() { // 232 333
        int bW = 120;
        int bH = 120;

        addWidget(new ImageWidget(10, 10, 313, 212, new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor())));
        addWidget(new ImageWidget(333 / 2, 20, 1, 222 - 40, new ColorRectTexture(-1)));

        addWidget(new LabelWidget(10 + 313 / 4, 35, "terminal.multiblock_ar.tier.0", -1).setXCentered(true).setYCentered(true));
        addWidget(new RectButtonWidget(10 + (313 / 2 - bW) / 2, 50, bW, bH)
                .setIcon(TextureArea.fullImage("textures/gui/terminal/multiblock_ar/profile.png"))
                .setColors(-1, 0xff00ff00, 0)
                .setHoverText("terminal.ar.open")
                .setClickListener(clickData -> openAR()));

        addWidget(new LabelWidget(333 / 2 + 313 / 4, 35, "terminal.multiblock_ar.tier.1", getAppTier() == 0 ? 0xffff0000 : -1).setXCentered(true).setYCentered(true));
        addWidget(new RectButtonWidget(333 / 2 + (313 / 2 - bW) / 2, 50, bW, bH)
                .setIcon(this::drawBuilderButton)
                .setColors(getAppTier() == 0 ? 0xffff0000 : -1, getAppTier() == 0 ? 0xffff0000 : 0xff00ff00, 0)
                .setHoverText(getAppTier() > 0 ? "terminal.multiblock_ar.builder.hover" : "terminal.multiblock_ar.unlock")
                .setClickListener(clickData -> buildMode()));
        return this;
    }

    private void drawBuilderButton(double x, double y, int width, int height) {
        if (Shaders.allowedShader()) {
            float time = (gui.entityPlayer.ticksExisted + partialTicks) / 20f;

            MultiblockControllerBase controllerBase = getController();
            int color = controllerBase == null ? -1 : controllerBase.getPaintingColorForRendering();

            if (controllerBase != null) {
                GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
                GlStateManager.enableTexture2D();
                RenderUtil.bindTextureAtlasSprite(controllerBase.getFrontDefaultTexture());
                GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
                GlStateManager.enableTexture2D();
                RenderUtil.bindTextureAtlasSprite(controllerBase.getBaseTexture(null).getParticleSprite());
            }
            ShaderTexture.createShader("showcube.frag").draw(x, y, width, height, uniformCache -> {
                uniformCache.glUniform1I("faceTexture", 0);
                uniformCache.glUniform1I("baseTexture", 1);
                uniformCache.glUniform1F("u_time", time);
                uniformCache.glUniform3F("f_color", (color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
                uniformCache.glUniformBoolean("block", controllerBase != null);
                if (isMouseOver((int)x, (int)y, width, height, lastMouseX, lastMouseY)) {
                    uniformCache.glUniform2F("u_mouse",
                            (float) (((lastMouseX - x) / 2 + width / 3)  * ConfigHolder.client.resolution),
                            (float) (height / 2 * ConfigHolder.client.resolution));
                }
            });

            GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
            GlStateManager.bindTexture(0);

            GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
            GlStateManager.bindTexture(0);

        } else {
            GuiTextures.MULTIBLOCK_CATEGORY.draw(x, y, width, height);
        }
    }

    @Override
    public int getMaxTier() {
        return 1;
    }

    private MultiblockControllerBase getController() {
        if (os.clickPos != null) {
            TileEntity te = gui.entityPlayer.world.getTileEntity(os.clickPos);
            if (te instanceof IGregTechTileEntity && ((IGregTechTileEntity) te).getMetaTileEntity() instanceof MultiblockControllerBase) {
                return (MultiblockControllerBase) ((IGregTechTileEntity) te).getMetaTileEntity();
            }
        }
        return null;
    }

    private void buildMode() {
        if (getAppTier() == 0) {
            TerminalDialogWidget.showInfoDialog(getOs(), "terminal.dialog.notice", "terminal.multiblock_ar.unlock").open();
        } else if (getController() != null) {
            widgets.forEach(this::waitToRemoved);
            MultiblockControllerBase controllerBase = getController();
            MachineBuilderWidget builderWidget = new MachineBuilderWidget(200, 16, 133, 200, controllerBase, getOs());
            this.addWidget(builderWidget);
            builderWidget.addPlayerInventory();
            if (isClient) {
                MachineSceneWidget sceneWidget  = new MachineSceneWidget(0, 16, 200, 200, controllerBase);
                builderWidget.setSceneWidget(sceneWidget);
                this.addWidget(0, sceneWidget);
                this.addWidget(new ImageWidget(0, 0, 333, 16, GuiTextures.UI_FRAME_SIDE_UP));
                this.addWidget(new ImageWidget(0, 216, 333, 16, GuiTextures.UI_FRAME_SIDE_DOWN));
            } else {
                this.addWidget(0, new WidgetGroup()); // placeholder
            }
        } else {
            TerminalDialogWidget.showInfoDialog(getOs(), "terminal.dialog.notice", "terminal.console.notice").open();
        }
    }

    @Override
    protected void hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        this.partialTicks = partialTicks;
        super.hookDrawInBackground(mouseX, mouseY, partialTicks, context);

    }


    //////////////////////////////////////AR/////////////////////////////////////////

    @SideOnly(Side.CLIENT)
    private static Map<MultiblockControllerBase, MultiblockShapeInfo> controllerList;
    @SideOnly(Side.CLIENT)
    private static Set<MultiblockControllerBase> found;
    @SideOnly(Side.CLIENT)
    private static BlockPos lastPos;
    private static int opList = -1;

    @Override
    public void onAROpened() {
        controllerList = new HashMap<>();
        found = new HashSet<>();
    }

    @SideOnly(Side.CLIENT)
    private boolean inRange(BlockPos playerPos, BlockPos controllerPos) {
        return Math.abs(playerPos.getX() - controllerPos.getX()) < 30 &&
                Math.abs(playerPos.getY() - controllerPos.getY()) < 30 &&
                Math.abs(playerPos.getZ() - controllerPos.getZ()) < 30;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void tickAR(EntityPlayer player) {
        World world = player.world;
        int tick = Math.abs(player.ticksExisted % 27); // 0 - 26
        if (tick == 0) {
            boolean reRender = false;
            Iterator<MultiblockControllerBase> iterator = controllerList.keySet().iterator();
            if (iterator.hasNext()) {
                MultiblockControllerBase controller = iterator.next();
                if (!controller.isValid() || controller.isStructureFormed() || !inRange(player.getPosition(), controller.getPos())) {
                    iterator.remove();
                    reRender = true;
                }
            }
            for (MultiblockControllerBase controllerBase : found) {
                if (!controllerList.containsKey(controllerBase)) {
                    List<MultiblockShapeInfo> shapeInfos = controllerBase.getMatchingShapes();
                    if (!shapeInfos.isEmpty()) {
                        controllerList.put(controllerBase, shapeInfos.get(0));
                        reRender = true;
                    }
                }
            }
            found.clear();
            lastPos = player.getPosition();
            if (reRender) {
                opList = GLAllocation.generateDisplayLists(1); // allocate op list
                GlStateManager.glNewList(opList, GL11.GL_COMPILE);
                controllerList.forEach((controller, shapes) -> MultiblockPreviewRenderer.renderControllerInList(controller, shapes, 0));
                GlStateManager.glEndList();
            }
        }
        if (lastPos == null) {
            lastPos = player.getPosition();
        }
        for (int i = tick * 1000; i < (tick + 1) * 1000; i++) {
            int x = i % 30 - 15;
            int y = (i / 30) % 30 - 15;
            int z = (i / 900) - 15;
            TileEntity tileEntity = world.getTileEntity(lastPos.add(x, y, z));
            if (tileEntity instanceof IGregTechTileEntity) {
                if (((IGregTechTileEntity) tileEntity).getMetaTileEntity() instanceof MultiblockControllerBase) {
                    found.add((MultiblockControllerBase) ((IGregTechTileEntity) tileEntity).getMetaTileEntity());
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawARScreen(RenderWorldLastEvent event) {
        if (opList != -1) {
            MultiblockPreviewRenderer.resetMultiblockRender();
            Minecraft mc = Minecraft.getMinecraft();
            Entity entity = mc.getRenderViewEntity();
            if (entity == null) entity = mc.player;

            float partialTicks = event.getPartialTicks();

            double tx = entity.lastTickPosX + ((entity.posX - entity.lastTickPosX) * partialTicks);
            double ty = entity.lastTickPosY + ((entity.posY - entity.lastTickPosY) * partialTicks);
            double tz = entity.lastTickPosZ + ((entity.posZ - entity.lastTickPosZ) * partialTicks);

            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.pushMatrix();
            GlStateManager.translate(-tx, -ty, -tz);
            GlStateManager.enableBlend();

            GlStateManager.callList(opList);

            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
            GlStateManager.color(1F, 1F, 1F, 0F);
        }
    }

}
