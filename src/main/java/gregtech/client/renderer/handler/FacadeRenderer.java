package gregtech.client.renderer.handler;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourARGB;
import codechicken.lib.render.CCQuad;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import gregtech.api.cover.ICoverable;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.util.ModCompatibility;
import gregtech.client.model.pipeline.VertexLighterFlatSpecial;
import gregtech.client.model.pipeline.VertexLighterSmoothAoSpecial;
import gregtech.client.utils.AdvCCRSConsumer;
import gregtech.client.utils.FacadeBlockAccess;
import gregtech.common.covers.facade.FacadeHelper;
import gregtech.common.items.behaviors.FacadeItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Mostly based on and (copied from) ThermalDynamics with minor tweaks
 * https://github.com/CoFH/ThermalDynamics/
 */
@SideOnly(Side.CLIENT)
public class FacadeRenderer implements IItemRenderer {

    final static int[] sideOffsets = {1, 1, 2, 2, 0, 0};
    final static float[] sideSoftBounds = {0, 1, 0, 1, 0, 1};

    private final static float FACADE_RENDER_OFFSET = 2.0f / 512.0f;
    private final static float FACADE_RENDER_OFFSET2 = 1 - FACADE_RENDER_OFFSET;

    public static final ThreadLocal<VertexLighterFlat> lighterFlat = ThreadLocal.withInitial(() -> new VertexLighterFlatSpecial(Minecraft.getMinecraft().getBlockColors()));
    public static final ThreadLocal<VertexLighterFlat> lighterSmooth = ThreadLocal.withInitial(() -> new VertexLighterSmoothAoSpecial(Minecraft.getMinecraft().getBlockColors()));

    public static final Cache<String, List<CCQuad>> itemQuadCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();

    public static void init() {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(resourceManager -> itemQuadCache.invalidateAll());
    }

    @Override
    public void renderItem(ItemStack rawStack, ItemCameraTransforms.TransformType transformType) {
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

    public static boolean renderBlockCover(CCRenderState ccrs, Matrix4 translation, IBlockAccess world, BlockPos pos, int side, IBlockState state, Cuboid6 bounds, BlockRenderLayer layer) {

        EnumFacing face = EnumFacing.VALUES[side];
        IBlockAccess coverAccess = new FacadeBlockAccess(world, pos, face, state);
        if (layer != null && !state.getBlock().canRenderInLayer(state, layer)) {
            return false;
        }
        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        try {
            state = state.getActualState(coverAccess, pos);
        } catch (Exception ignored) {
        }

        IBakedModel model = dispatcher.getModelForState(state);

        try {
            state = state.getBlock().getExtendedState(state, coverAccess, pos);
        } catch (Exception ignored) {
        }
        long posRand = net.minecraft.util.math.MathHelper.getPositionRandom(pos);
        List<BakedQuad> bakedQuads = new LinkedList<>(model.getQuads(state, null, posRand));

        for (EnumFacing face2 : EnumFacing.VALUES) {
            bakedQuads.addAll(model.getQuads(state, face2, posRand));
        }

        List<CCQuad> quads = CCQuad.fromArray(bakedQuads);
        quads = sliceQuads(quads, side, bounds);

        if (!quads.isEmpty()) {
            VertexLighterFlat lighter = setupLighter(ccrs, translation, state, coverAccess, pos, model);
            return renderBlockQuads(lighter, coverAccess, state, quads, pos);
        }
        return false;
    }

    public static void renderItemCover(CCRenderState ccrs, int side, ItemStack renderStack, Cuboid6 bounds) {
        Minecraft minecraft = Minecraft.getMinecraft();
        RenderItem renderItem = minecraft.getRenderItem();
        IBakedModel model = renderItem.getItemModelWithOverrides(renderStack, null, null);

        IBlockState state = FacadeHelper.lookupBlockForItem(renderStack);
        String cacheKey = state.getBlock().getRegistryName() + "|" + state.getBlock().getMetaFromState(state);

        List<CCQuad> renderQuads = itemQuadCache.getIfPresent(cacheKey);
        if (renderQuads == null) {

            List<BakedQuad> quads = new ArrayList<>(model.getQuads(null, null, 0));
            for (EnumFacing face : EnumFacing.VALUES) {
                quads.addAll(model.getQuads(null, face, 0));
            }

            renderQuads = applyItemTint(sliceQuads(CCQuad.fromArray(quads), side, bounds), renderStack);
            itemQuadCache.put(cacheKey, renderQuads);
        }

        AdvCCRSConsumer consumer = new AdvCCRSConsumer(ccrs);
        consumer.setTranslation(new Matrix4()
                .translate(Vector3.center.copy().subtract(bounds.center()))
                .scale(1.05, 1.05, 1.05));
        for (CCQuad quad : renderQuads) {
            quad.pipe(consumer);
        }

    }

    public static List<CCQuad> applyItemTint(List<CCQuad> quads, ItemStack stack) {
        List<CCQuad> retQuads = new LinkedList<>();
        for (CCQuad quad : quads) {
            int colour = -1;

            if (quad.hasTint()) {
                colour = Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, quad.tintIndex);

                if (EntityRenderer.anaglyphEnable) {
                    colour = TextureUtil.anaglyphColor(colour);
                }
                colour = colour | 0xFF000000;
            }
            CCQuad copyQuad = quad.copy();

            Colour c = new ColourARGB(colour);
            for (Colour qC : copyQuad.colours) {
                qC.multiply(c);
            }
            retQuads.add(copyQuad);
        }

        return retQuads;
    }


    public static VertexLighterFlat setupLighter(CCRenderState ccrs, Matrix4 translation, IBlockState state, IBlockAccess access, BlockPos pos, IBakedModel model) {
        boolean renderAO = Minecraft.isAmbientOcclusionEnabled() && state.getLightValue(access, pos) == 0 && model.isAmbientOcclusion();
        VertexLighterFlat lighter = renderAO ? lighterSmooth.get() : lighterFlat.get();

        AdvCCRSConsumer consumer = new AdvCCRSConsumer(ccrs);
        lighter.setParent(consumer);
        consumer.setTranslation(translation);
        return lighter;
    }

    public static boolean renderBlockQuads(VertexLighterFlat lighter, IBlockAccess access, IBlockState state, List<CCQuad> quads, BlockPos pos) {
        if (!quads.isEmpty()) {
            lighter.setWorld(access);
            lighter.setState(state);
            lighter.setBlockPos(pos);
            lighter.updateBlockInfo();
            for (CCQuad quad : quads) {
                quad.pipe(lighter);
            }
            return true;
        }
        return false;
    }

    public static List<CCQuad> sliceQuads(List<CCQuad> quads, int side, Cuboid6 bounds) {
        boolean flag, flag2;

        double[][] quadPos = new double[4][3];
        boolean[] flat = new boolean[3];
        int verticesPerFace = 4;
        List<CCQuad> finalQuads = new LinkedList<>();

        for (CCQuad quad : quads) {

            flag = flag2 = false;
            for (int i = 0; i < 3; i++) {
                flat[i] = true;
            }

            Vector3 first = quad.vertices[0].vec;

            for (int v = 0; v < 4; v++) {
                quadPos[v] = quad.vertices[v].vec.toArrayD();

                flag = flag || quadPos[v][sideOffsets[side]] != sideSoftBounds[side];
                flag2 = flag2 || quadPos[v][sideOffsets[side]] != (1 - sideSoftBounds[side]);

                if (v != 0) {
                    flat[0] = flat[0] && quad.vertices[v].vec.x == first.x;
                    flat[1] = flat[1] && quad.vertices[v].vec.y == first.y;
                    flat[2] = flat[2] && quad.vertices[v].vec.z == first.z;
                }
            }

            int s = -1;

            if (flag && flag2) {
                for (int vi = 0; vi < 3; vi++) {
                    if (flat[vi]) {
                        if (vi != sideOffsets[side]) {
                            s = vi;
                            break;
                        } else {
                            flag = false;
                        }
                    }
                }
            }

            for (int k2 = 0; k2 < verticesPerFace; k2++) {
                boolean flag3 = quadPos[k2][sideOffsets[side]] != sideSoftBounds[side];
                for (int j = 0; j < 3; j++) {
                    if (j == sideOffsets[side]) {
                        quadPos[k2][j] = clampF(quadPos[k2][j], bounds, j);
                    } else {
                        if (flag && flag2 && flag3) {
                            quadPos[k2][j] = MathHelper.clamp(quadPos[k2][j], FACADE_RENDER_OFFSET, FACADE_RENDER_OFFSET2);
                        }
                    }
                }

                if (s != -1) {
                    double u, v;

                    if (s == 0) {
                        u = quadPos[k2][1];
                        v = quadPos[k2][2];
                    } else if (s == 1) {
                        u = quadPos[k2][0];
                        v = quadPos[k2][2];
                    } else {
                        u = quadPos[k2][0];
                        v = quadPos[k2][1];
                    }

                    u = MathHelper.clamp(u, 0, 1) * 16;
                    v = MathHelper.clamp(v, 0, 1) * 16;

                    TextureAtlasSprite sideTexture = quad.sprite;
                    u = sideTexture.getInterpolatedU(u);
                    v = sideTexture.getInterpolatedV(v);
                    quad.vertices[k2].uv.set(u, v);
                    quad.tintIndex = -1;
                }
                quad.vertices[k2].vec.set(quadPos[k2]);
            }
            finalQuads.add(quad);
        }

        return finalQuads;
    }

    private final static EnumFacing[][] sides = {
            {EnumFacing.WEST, EnumFacing.EAST},
            {EnumFacing.DOWN, EnumFacing.UP},
            {EnumFacing.NORTH, EnumFacing.SOUTH}
    };

    private static double clampF(double x, Cuboid6 b, int j) {

        double l = b.getSide(sides[j][0]);
        double u = b.getSide(sides[j][1]);

        if (x < l) {
            return l - (l - x) * 0.001953125f;
        } else if (x > u) {
            return u + (x - u) * 0.001953125f;
        } else {
            return x;
        }
    }
}
