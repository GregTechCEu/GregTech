package gregtech.common.terminal.app.ar;

import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.render.scene.WorldSceneRenderer;
import gregtech.api.terminal.app.ARApplication;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.util.RelativeDirection;
import gregtech.integration.jei.GTJeiPlugin;
import gregtech.integration.jei.multiblock.MultiblockInfoRecipeWrapper;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
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
import net.minecraft.item.ItemStack;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/09/13
 * @Description:
 */
public class MultiBlockPreviewARApp extends ARApplication {

    public MultiBlockPreviewARApp() {
        super("multiblock_ar");
    }

    @Override
    public AbstractApplication initApp() {
        addWidget(new RectButtonWidget(100, 100, 50, 20).setClickListener(clickData -> openAR()));
        return this;
    }

    @SideOnly(Side.CLIENT)
    private static final Map<MultiblockControllerBase, WorldSceneRenderer> controllerList = new HashMap<>();
    @SideOnly(Side.CLIENT)
    private static final Set<MultiblockControllerBase> find = new HashSet<>();
    @SideOnly(Side.CLIENT)
    private static BlockPos lastPos;


    private boolean inRange(BlockPos playerPos, BlockPos controllerPos) {
        return Math.abs(playerPos.getX() - controllerPos.getX()) < 30 &&
                Math.abs(playerPos.getY() - controllerPos.getY()) < 30 &&
                Math.abs(playerPos.getZ() - controllerPos.getZ()) < 30;
    }

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
            for (MultiblockControllerBase controllerBase : find) {
                if (!controllerList.containsKey(controllerBase)) {
                    WorldSceneRenderer worldSceneRenderer = getWorldSceneRenderer(controllerBase);
                    if (worldSceneRenderer != null) {
                        controllerList.put(controllerBase, worldSceneRenderer);
                    }
                }
            }
            find.clear();
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
                    find.add((MultiblockControllerBase) ((MetaTileEntityHolder) tileEntity).getMetaTileEntity());
                }
            }
        }
    }

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

    private static void renderControllerInList(MultiblockControllerBase controllerBase, WorldSceneRenderer worldSceneRenderer) {
        BlockPos mbpPos = controllerBase.getPos();
        EnumFacing frontFacing, previewFacing;
        previewFacing = controllerBase.getFrontFacing();
        List<BlockPos> renderedBlocks = worldSceneRenderer.renderedBlocksMap.keySet().stream().flatMap(Collection::stream).collect(Collectors.toList());
        BlockPos controllerPos = BlockPos.ORIGIN;
        MultiblockControllerBase mte = null;

        for (BlockPos blockPos : renderedBlocks) {
            MetaTileEntity metaTE = BlockMachine.getMetaTileEntity(worldSceneRenderer.world, blockPos);
            if (metaTE instanceof MultiblockControllerBase && metaTE.metaTileEntityId.equals(controllerBase.metaTileEntityId)) {
                controllerPos = blockPos;
                previewFacing = metaTE.getFrontFacing();
                mte = (MultiblockControllerBase) metaTE;
                break;
            }
        }

        EnumFacing facing = controllerBase.getFrontFacing();
        EnumFacing spin = EnumFacing.NORTH;
        BlockPattern pattern = controllerBase.structurePattern;
        RelativeDirection[] structureDir = pattern.structureDir;

        if (structureDir == null) {
            return;
        }

        // TODO SIDEWAYS ONE DAY
        //  spin = controllerBase.getSpin();

        frontFacing = facing.getYOffset() == 0 ? facing : facing.getYOffset() < 0 ? spin : spin.getOpposite();
        Rotation rotatePreviewBy = Rotation.values()[(4 + frontFacing.getHorizontalIndex() - previewFacing.getHorizontalIndex()) % 4];

        Minecraft mc = Minecraft.getMinecraft();
        BlockRendererDispatcher brd = mc.getBlockRendererDispatcher();
        World world = worldSceneRenderer.world;
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
        for (BlockPos pos : renderedBlocks) {
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

    private static WorldSceneRenderer getWorldSceneRenderer(MultiblockControllerBase controllerBase) {
        IRecipeRegistry rr = GTJeiPlugin.jeiRuntime.getRecipeRegistry();
        IFocus<ItemStack> focus = rr.createFocus(IFocus.Mode.INPUT, controllerBase.getStackForm());
        return rr.getRecipeCategories(focus)
                .stream()
                .map(c -> (IRecipeCategory<IRecipeWrapper>) c)
                .map(c -> rr.getRecipeWrappers(c, focus))
                .flatMap(List::stream)
                .filter(MultiblockInfoRecipeWrapper.class::isInstance)
                .findFirst()
                .map(MultiblockInfoRecipeWrapper.class::cast)
                .map(MultiblockInfoRecipeWrapper::getCurrentRenderer)
                .orElse(null);
    }

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
