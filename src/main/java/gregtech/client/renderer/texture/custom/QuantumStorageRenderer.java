package gregtech.client.renderer.texture.custom;

import gregtech.api.gui.resources.TextTexture;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer.RenderSide;
import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;

import java.util.EnumMap;

public class QuantumStorageRenderer implements TextureUtils.IIconRegister {

    private static final Cuboid6 glassBox = new Cuboid6(1 / 16.0, 1 / 16.0, 1 / 16.0, 15 / 16.0, 15 / 16.0, 15 / 16.0);

    private static final EnumMap<EnumFacing, Cuboid6> boxFacingMap = new EnumMap<>(EnumFacing.class);

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite glassTexture;

    static {
        boxFacingMap.put(EnumFacing.UP, new Cuboid6(0 / 16.0, 14 / 16.0, 0 / 16.0, 16 / 16.0, 16 / 16.0, 16 / 16.0));
        boxFacingMap.put(EnumFacing.DOWN, new Cuboid6(0 / 16.0, 0 / 16.0, 0 / 16.0, 16 / 16.0, 2 / 16.0, 16 / 16.0));
        boxFacingMap.put(EnumFacing.WEST, new Cuboid6(0 / 16.0, 0 / 16.0, 0 / 16.0, 2 / 16.0, 16 / 16.0, 16 / 16.0));
        boxFacingMap.put(EnumFacing.EAST, new Cuboid6(14 / 16.0, 0 / 16.0, 0 / 16.0, 16 / 16.0, 16 / 16.0, 16 / 16.0));
        boxFacingMap.put(EnumFacing.SOUTH, new Cuboid6(0 / 16.0, 0 / 16.0, 14 / 16.0, 16 / 16.0, 16 / 16.0, 16 / 16.0));
        boxFacingMap.put(EnumFacing.NORTH, new Cuboid6(0 / 16.0, 0 / 16.0, 0 / 16.0, 16 / 16.0, 16 / 16.0, 2 / 16.0));
    }

    public QuantumStorageRenderer() {
        Textures.iconRegisters.add(this);
    }

    @Override
    public void registerIcons(TextureMap textureMap) {
        this.glassTexture = textureMap
                .registerSprite(new ResourceLocation("gregtech:blocks/overlay/machine/overlay_screen_glass"));
    }

    public void renderMachine(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                              EnumFacing frontFacing, int tier) {
        Textures.renderFace(renderState, translation, pipeline, frontFacing, glassBox, glassTexture,
                BlockRenderLayer.CUTOUT_MIPPED);

        TextureAtlasSprite hullTexture = Textures.VOLTAGE_CASINGS[tier]
                .getSpriteOnSide(RenderSide.bySide(EnumFacing.NORTH));
        boxFacingMap.keySet().forEach(facing -> {
            for (EnumFacing box : EnumFacing.VALUES) {
                if ((facing != frontFacing || box != frontFacing) &&
                        (facing != EnumFacing.DOWN || box.getAxis().isVertical())) { // Don't render the front facing
                                                                                     // box from the front, nor allow
                                                                                     // Z-fighting to occur on the
                                                                                     // bottom
                    Textures.renderFace(renderState, translation, pipeline, facing, boxFacingMap.get(box), hullTexture,
                            BlockRenderLayer.CUTOUT_MIPPED);
                }
            }
        });
    }

    public static void renderChestStack(double x, double y, double z, MetaTileEntityQuantumChest machine,
                                        ItemStack stack, long count, float partialTicks) {
        if (stack.isEmpty() || count == 0)
            return;

        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
        World world = machine.getWorld();
        setLightingCorrectly(world, machine.getPos());
        EnumFacing frontFacing = machine.getFrontFacing();
        RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
        float tick = world.getWorldTime() + partialTicks;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0.5D, 0.5D, 0.5D);
        GlStateManager.rotate(tick * (float) Math.PI * 2 / 40, 0, 1, 0);
        GlStateManager.scale(0.6f, 0.6f, 0.6f);
        itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        GlStateManager.popMatrix();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        renderAmountText(x, y, z, count, frontFacing);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
    }

    public static void renderTankFluid(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                       FluidTank tank, IBlockAccess world, BlockPos pos, EnumFacing frontFacing) {
        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
        if (world != null) {
            renderState.setBrightness(world, pos);
        }
        FluidStack stack = tank.getFluid();
        if (stack == null || stack.amount == 0)
            return;

        Cuboid6 partialFluidBox = new Cuboid6(1.0625 / 16.0, 2.0625 / 16.0, 1.0625 / 16.0, 14.9375 / 16.0,
                14.9375 / 16.0, 14.9375 / 16.0);

        double fillFraction = (double) stack.amount / tank.getCapacity();
        if (tank.getFluid().getFluid().isGaseous()) {
            partialFluidBox.min.y = Math.max(13.9375 - (11.875 * fillFraction), 2.0) / 16.0;
        } else {
            partialFluidBox.max.y = Math.min((11.875 * fillFraction) + 2.0625, 14.0) / 16.0;
        }

        renderState.setFluidColour(stack);
        ResourceLocation fluidStill = stack.getFluid().getStill(stack);
        TextureAtlasSprite fluidStillSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(fluidStill.toString());
        for (EnumFacing facing : EnumFacing.VALUES) {
            Textures.renderFace(renderState, translation, pipeline, facing, partialFluidBox, fluidStillSprite,
                    BlockRenderLayer.CUTOUT_MIPPED);
        }
        GlStateManager.resetColor();

        renderState.reset();
    }

    public static void renderTankAmount(double x, double y, double z, EnumFacing frontFacing, long amount) {
        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

        renderAmountText(x, y, z, amount, frontFacing);

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
    }

    public static void renderAmountText(double x, double y, double z, long amount, EnumFacing frontFacing) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(frontFacing.getXOffset() * -1 / 16f, frontFacing.getYOffset() * -1 / 16f,
                frontFacing.getZOffset() * -1 / 16f);
        RenderUtil.moveToFace(0, 0, 0, frontFacing);
        if (frontFacing.getAxis() == EnumFacing.Axis.Y) {
            RenderUtil.rotateToFace(frontFacing, EnumFacing.SOUTH);
        } else {
            RenderUtil.rotateToFace(frontFacing, null);
        }
        String amountText = TextFormattingUtil.formatLongToCompactString(amount, 4);
        GlStateManager.scale(1f / 64, 1f / 64, 0);
        GlStateManager.translate(-32, -32, 0);
        GlStateManager.disableLighting();
        new TextTexture(amountText, 0xFFFFFF).draw(0, 24, 64, 28);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    public static void setLightingCorrectly(IBlockAccess world, BlockPos pos) {
        // Evil bit hackery from net.minecraft.client.renderer.ItemRenderer to actually get the right light coords
        // This makes about as much sense as the fast inverse square root algorithm
        int actualLight = world.getCombinedLight(pos, 0);
        float lightmapXCoord = actualLight & 65535;
        float lightmapYCoord = actualLight >> 16;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapXCoord, lightmapYCoord);
    }
}
