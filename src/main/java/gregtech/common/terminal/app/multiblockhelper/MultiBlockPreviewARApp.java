package gregtech.common.terminal.app.multiblockhelper;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.ShaderTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.client.utils.TrackedDummyWorld;
import gregtech.client.shader.Shaders;
import gregtech.api.terminal.app.ARApplication;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.MachineSceneWidget;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.BlockInfo;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
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
            float time =(gui.entityPlayer.ticksExisted + partialTicks) / 20f;

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
            if (te instanceof MetaTileEntityHolder && ((MetaTileEntityHolder) te).getMetaTileEntity() instanceof MultiblockControllerBase) {
                return (MultiblockControllerBase) ((MetaTileEntityHolder) te).getMetaTileEntity();
            }
        }
        return null;
    }

    private void buildMode() {
        if (getAppTier() == 0) {
            TerminalDialogWidget.showInfoDialog(getOs(), "terminal.dialog.notice", "terminal.multiblock_ar.unlock").open();
        } else if (getController() != null){
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
            Iterator<MultiblockControllerBase> iterator = controllerList.keySet().iterator();
            if (iterator.hasNext()) {
                MultiblockControllerBase controller = iterator.next();
                if (!controller.isValid() || controller.isStructureFormed() || !inRange(player.getPosition(), controller.getPos())) {
                    iterator.remove();
                }
            }
            for (MultiblockControllerBase controllerBase : found) {
                if (!controllerList.containsKey(controllerBase)) {
                    List<MultiblockShapeInfo> shapeInfos = controllerBase.getMatchingShapes();
                    if (!shapeInfos.isEmpty()) {
                        controllerList.put(controllerBase, shapeInfos.get(0));
                    }
                }
            }
            found.clear();
            lastPos = player.getPosition();
        }
        if (lastPos == null) {
            lastPos = player.getPosition();
        }
        for (int i = tick * 1000; i < (tick + 1) * 1000; i++) {
            int x = i % 30 - 15;
            int y = (i / 30) % 30 - 15;
            int z = (i / 900) - 15;
            TileEntity tileEntity = world.getTileEntity(lastPos.add(x, y, z));
            if (tileEntity instanceof MetaTileEntityHolder) {
                if (((MetaTileEntityHolder) tileEntity).getMetaTileEntity() instanceof MultiblockControllerBase) {
                    found.add((MultiblockControllerBase) ((MetaTileEntityHolder) tileEntity).getMetaTileEntity());
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawARScreen(RenderWorldLastEvent event) {
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

        BlockRenderLayer oldLayer = MinecraftForgeClient.getRenderLayer();

        controllerList.forEach(MultiBlockPreviewARApp::renderControllerInList);

        ForgeHooksClient.setRenderLayer(oldLayer);
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
        GlStateManager.color(1F, 1F, 1F, 0F);
    }

    @SideOnly(Side.CLIENT)
    private static void renderControllerInList(MultiblockControllerBase controllerBase, MultiblockShapeInfo shapeInfo) {
        BlockPos mbpPos = controllerBase.getPos();
        EnumFacing frontFacing, previewFacing;
        previewFacing = controllerBase.getFrontFacing();
        BlockPos controllerPos = BlockPos.ORIGIN;
        MultiblockControllerBase mte = null;
        BlockInfo[][][] blocks = shapeInfo.getBlocks();
        Map<BlockPos, BlockInfo> blockMap = new HashMap<>();
        for (int x = 0; x < blocks.length; x++) {
            BlockInfo[][] aisle = blocks[x];
            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];
                for (int z = 0; z < column.length; z++) {
                    blockMap.put(new BlockPos(x, y, z), column[z]);
                    MetaTileEntity metaTE = column[z].getTileEntity() instanceof MetaTileEntityHolder ? ((MetaTileEntityHolder) column[z].getTileEntity()).getMetaTileEntity() : null;
                    if (metaTE instanceof MultiblockControllerBase && metaTE.metaTileEntityId.equals(controllerBase.metaTileEntityId)) {
                        controllerPos = new BlockPos(x, y, z);
                        previewFacing = metaTE.getFrontFacing();
                        mte = (MultiblockControllerBase) metaTE;
                        break;
                    }
                }
            }
        }
        TrackedDummyWorld world = new TrackedDummyWorld();
        world.addBlocks(blockMap);

        EnumFacing facing = controllerBase.getFrontFacing();
        EnumFacing spin = EnumFacing.NORTH;


        // TODO SIDEWAYS ONE DAY
        //  spin = controllerBase.getSpin();

        frontFacing = facing.getYOffset() == 0 ? facing : facing.getYOffset() < 0 ? spin : spin.getOpposite();
        Rotation rotatePreviewBy = Rotation.values()[(4 + frontFacing.getHorizontalIndex() - previewFacing.getHorizontalIndex()) % 4];

        Minecraft mc = Minecraft.getMinecraft();
        BlockRendererDispatcher brd = mc.getBlockRendererDispatcher();
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buff = tes.getBuffer();
        GlStateManager.pushMatrix();
        GlStateManager.translate(mbpPos.getX(), mbpPos.getY(), mbpPos.getZ());
        GlStateManager.translate(0.5, 0, 0.5);
        GlStateManager.rotate(rotatePreviewBy.ordinal() * 90, 0, -1, 0);
        GlStateManager.translate(-0.5, 0, -0.5);

        if (facing == EnumFacing.UP) {
            GlStateManager.translate(0.5, 0.5, 0.5);
            GlStateManager.rotate(90, -previewFacing.getZOffset(), 0, previewFacing.getXOffset());
            GlStateManager.translate(-0.5, -0.5, -0.5);
        } else if (facing == EnumFacing.DOWN) {
            GlStateManager.translate(0.5, 0.5, 0.5);
            GlStateManager.rotate(90, previewFacing.getZOffset(), 0, -previewFacing.getXOffset());
            GlStateManager.translate(-0.5, -0.5, -0.5);
        } else {
            int degree = 90 * (spin == EnumFacing.EAST ? -1 : spin == EnumFacing.SOUTH ? 2 : spin == EnumFacing.WEST ? 1 : 0);
            GlStateManager.translate(0.5, 0.5, 0.5);
            GlStateManager.rotate(degree, previewFacing.getXOffset(), 0, previewFacing.getZOffset());
            GlStateManager.translate(-0.5, -0.5, -0.5);
        }

        TargetBlockAccess targetBA = new TargetBlockAccess(world, BlockPos.ORIGIN);
        for (BlockPos pos : blockMap.keySet()) {
            targetBA.setPos(pos);
            GlStateManager.pushMatrix();
            BlockPos.MutableBlockPos tPos = new BlockPos.MutableBlockPos(pos.subtract(controllerPos));
            GlStateManager.translate(tPos.getX(), tPos.getY(), tPos.getZ());
            GlStateManager.translate(0.125, 0.125, 0.125);
            GlStateManager.scale(0.75, 0.75, 0.75);

            buff.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            IBlockState state = world.getBlockState(pos);
            for (BlockRenderLayer brl : BlockRenderLayer.values()) {
                if (state.getBlock().canRenderInLayer(state, brl)) {
                    ForgeHooksClient.setRenderLayer(brl);
                    brd.renderBlock(state, BlockPos.ORIGIN, targetBA, buff);
                }
            }
            tes.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        if (mte != null) {
            mte.checkStructurePattern();
        }
    }

    @SideOnly(Side.CLIENT)
    private static class TargetBlockAccess implements IBlockAccess {

        private final IBlockAccess delegate;
        private BlockPos targetPos;

        public TargetBlockAccess(IBlockAccess delegate, BlockPos pos) {
            this.delegate = delegate;
            this.targetPos = pos;
        }

        public void setPos(BlockPos pos) {
            targetPos = pos;
        }

        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            return pos.equals(BlockPos.ORIGIN) ? delegate.getTileEntity(targetPos) : null;
        }

        @Override
        public int getCombinedLight(BlockPos pos, int lightValue) {
            return 15;
        }

        @Override
        public IBlockState getBlockState(BlockPos pos) {
            return pos.equals(BlockPos.ORIGIN) ? delegate.getBlockState(targetPos) : Blocks.AIR.getDefaultState();
        }

        @Override
        public boolean isAirBlock(BlockPos pos) {
            return !pos.equals(BlockPos.ORIGIN) || delegate.isAirBlock(targetPos);
        }

        @Override
        public Biome getBiome(BlockPos pos) {
            return delegate.getBiome(targetPos);
        }

        @Override
        public int getStrongPower(BlockPos pos, EnumFacing direction) {
            return 0;
        }

        @Override
        public WorldType getWorldType() {
            return delegate.getWorldType();
        }

        @Override
        public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
            return pos.equals(BlockPos.ORIGIN) && delegate.isSideSolid(targetPos, side, _default);
        }

    }
}
