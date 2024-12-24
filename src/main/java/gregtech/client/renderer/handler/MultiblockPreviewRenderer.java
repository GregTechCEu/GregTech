package gregtech.client.renderer.handler;

import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.GreggyBlockPos;
import gregtech.api.util.GregFakePlayer;
import gregtech.client.renderer.scene.ImmediateWorldSceneRenderer;
import gregtech.client.utils.TrackedDummyWorld;
import gregtech.common.ConfigHolder;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import java.util.Iterator;
import java.util.Map;

import static gregtech.integration.jei.multiblock.MultiblockInfoRecipeWrapper.SOURCE;

@SideOnly(Side.CLIENT)
public class MultiblockPreviewRenderer {

    private static BlockPos mbpPos;
    private static long mbpEndTime;
    private static int opList = -1;
    private static int layer;

    public static void renderWorldLastEvent(RenderWorldLastEvent event) {
        if (mbpPos != null) {
            Minecraft mc = Minecraft.getMinecraft();
            long time = System.currentTimeMillis();
            if (opList == -1 || time > mbpEndTime || !(mc.world.getTileEntity(mbpPos) instanceof IGregTechTileEntity)) {
                resetMultiblockRender();
                layer = 0;
                return;
            }
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
            GlStateManager.color(1F, 1F, 1F, 1F);

        }
    }

    public static void renderMultiBlockPreview(MultiblockControllerBase src, EntityPlayer player,
                                               long durTimeMillis) {
        if (!src.getPos().equals(mbpPos)) {
            layer = 0;
        } else {
            if (mbpEndTime - System.currentTimeMillis() < 200) return;
            layer++;
        }
        resetMultiblockRender();
        mbpPos = src.getPos();
        mbpEndTime = System.currentTimeMillis() + durTimeMillis;
        opList = GLAllocation.generateDisplayLists(1); // allocate op list
        GlStateManager.glNewList(opList, GL11.GL_COMPILE);
        Iterator<Map<String, String>> iter = src.getPreviewBuilds();
        if (iter.hasNext()) {
            renderControllerInList(src, iter.next(), layer);
            // todo add dots again
            // shapes.get(0).sendDotMessage(player);
        }
        GlStateManager.glEndList();
    }

    public static void resetMultiblockRender() {
        mbpPos = null;
        mbpEndTime = 0;
        if (opList != -1) {
            GlStateManager.glDeleteLists(opList, 1);
            opList = -1;
        }
    }

    public static void renderControllerInList(MultiblockControllerBase src, Map<String, String> keyMap,
                                              int layer) {
        TrackedDummyWorld world = new TrackedDummyWorld();
        ImmediateWorldSceneRenderer worldSceneRenderer = new ImmediateWorldSceneRenderer(world);
        worldSceneRenderer.setClearColor(ConfigHolder.client.multiblockPreviewColor);

        MetaTileEntityHolder holder = new MetaTileEntityHolder();
        holder.setMetaTileEntity(src);
        holder.getMetaTileEntity().onPlacement();
        holder.getMetaTileEntity().setFrontFacing(src.getFrontFacing());
        ((MultiblockControllerBase) holder.getMetaTileEntity()).setUpwardsFacing(src.getUpwardsFacing());

        world.setBlockState(SOURCE, src.getBlock().getDefaultState());
        world.setTileEntity(SOURCE, holder);

        ((MultiblockControllerBase) holder.getMetaTileEntity()).autoBuild(new GregFakePlayer(world), keyMap);
        ((MultiblockControllerBase) holder.getMetaTileEntity()).checkStructurePattern();

        int finalMaxY = (int) (layer % (world.getMaxPos().y - world.getMinPos().y + 2));
        world.setRenderFilter(pos -> pos.getY() - (int) world.getMinPos().y + 1 == finalMaxY || finalMaxY == 0);

        Minecraft mc = Minecraft.getMinecraft();
        BlockRendererDispatcher brd = mc.getBlockRendererDispatcher();
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buff = tes.getBuffer();

        BlockRenderLayer oldLayer = MinecraftForgeClient.getRenderLayer();
        TargetBlockAccess targetBA = new TargetBlockAccess(world, BlockPos.ORIGIN);

        GreggyBlockPos greg = new GreggyBlockPos();
        GreggyBlockPos offset = new GreggyBlockPos(SOURCE);
        GreggyBlockPos temp = new GreggyBlockPos();

        for (BlockPos pos : world.renderedBlocks) {
            targetBA.setPos(pos);
            greg.from(src.getPos()).add(temp.from(pos)).subtract(offset);

            GlStateManager.pushMatrix();
            GlStateManager.translate(greg.x(), greg.y(), greg.z());
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
        ForgeHooksClient.setRenderLayer(oldLayer);
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
