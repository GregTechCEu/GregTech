package gregtech.client.renderer.texture.custom;

import gregtech.api.gui.resources.TextTexture;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer.RenderSide;
import gregtech.client.texture.IconRegistrar;
import gregtech.client.utils.RenderUtil;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumStorage;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

public class QuantumStorageRenderer implements IconRegistrar {

    private static final Cuboid6 glassBox = new Cuboid6(1 / 16.0, 1 / 16.0, 1 / 16.0, 15 / 16.0, 15 / 16.0, 15 / 16.0);

    private static final EnumMap<EnumFacing, Cuboid6> boxFacingMap = new EnumMap<>(EnumFacing.class);

    private static final TextTexture textRenderer = new TextTexture().setWidth(32);

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
    public void registerIcons(@NotNull TextureMap textureMap) {
        this.glassTexture = textureMap
                .registerSprite(new ResourceLocation("gregtech:blocks/overlay/machine/overlay_screen_glass"));
    }

    public <T extends MetaTileEntityQuantumStorage<?> & ITieredMetaTileEntity> void renderMachine(CCRenderState renderState,
                                                                                                  Matrix4 translation,
                                                                                                  IVertexOperation[] pipeline,
                                                                                                  T mte) {
        EnumFacing frontFacing = mte.getFrontFacing();
        int tier = mte.getTier();
        Textures.renderFace(renderState, translation, pipeline, frontFacing, glassBox, glassTexture,
                BlockRenderLayer.CUTOUT_MIPPED);

        TextureAtlasSprite hullTexture = Textures.VOLTAGE_CASINGS[tier]
                .getSpriteOnSide(RenderSide.bySide(EnumFacing.NORTH));

        if (mte.isConnected()) {
            hullTexture = Textures.QUANTUM_CASING.getParticleSprite();
        }

        for (var facing : boxFacingMap.keySet()) {
            // do not render the box at the front face when "facing" is "frontFacing"
            if (facing == frontFacing) continue;

            // render when the box face matches facing
            Textures.renderFace(renderState, translation, pipeline, facing, boxFacingMap.get(facing),
                    hullTexture, BlockRenderLayer.CUTOUT_MIPPED);

            // render when the box face is opposite of facing
            Textures.renderFace(renderState, translation, pipeline, facing.getOpposite(), boxFacingMap.get(facing),
                    hullTexture, BlockRenderLayer.CUTOUT_MIPPED);
        }

        // render the sides of the box that face the front face
        if (frontFacing.getAxis() == EnumFacing.Axis.Y) return;
        Textures.renderFace(renderState, translation, pipeline, frontFacing, boxFacingMap.get(EnumFacing.DOWN),
                hullTexture, BlockRenderLayer.CUTOUT_MIPPED);
        Textures.renderFace(renderState, translation, pipeline, frontFacing, boxFacingMap.get(EnumFacing.UP),
                hullTexture, BlockRenderLayer.CUTOUT_MIPPED);

        EnumFacing facing = frontFacing.rotateYCCW();
        Textures.renderFace(renderState, translation, pipeline, frontFacing, boxFacingMap.get(facing),
                hullTexture, BlockRenderLayer.CUTOUT_MIPPED);
        Textures.renderFace(renderState, translation, pipeline, frontFacing, boxFacingMap.get(facing.getOpposite()),
                hullTexture, BlockRenderLayer.CUTOUT_MIPPED);
    }

    public static void renderChestStack(double x, double y, double z, MetaTileEntityQuantumChest machine,
                                        ItemStack stack, long count, float partialTicks) {
        if (!ConfigHolder.client.enableFancyChestRender || stack.isEmpty() || count == 0)
            return;

        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
        World world = machine.getWorld();
        setLightingCorrectly(world, machine.getPos());
        EnumFacing frontFacing = machine.getFrontFacing();

        if (canRender(x, y, z, 8 *
                MathHelper.clamp((double) Minecraft.getMinecraft().gameSettings.renderDistanceChunks / 8, 1.0, 2.5))) {
            RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
            float tick = world.getTotalWorldTime() + partialTicks;
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.translate(0.5D, 0.5D, 0.5D);
            GlStateManager.rotate(tick * (float) Math.PI * 2 / 40, 0, 1, 0);
            GlStateManager.scale(0.6f, 0.6f, 0.6f);
            itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
        }

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        renderAmountText(x, y, z, count, frontFacing);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
    }

    public static void renderTankFluid(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                       FluidTank tank, IBlockAccess world, BlockPos pos, EnumFacing frontFacing) {
        FluidStack stack = tank.getFluid();
        if (stack == null || stack.amount == 0 || !ConfigHolder.client.enableFancyChestRender) {
            return;
        }

        Fluid fluid = stack.getFluid();
        if (fluid == null) {
            return;
        }

        if (world != null) {
            renderState.setBrightness(world, pos);
        }

        Cuboid6 partialFluidBox = new Cuboid6(1.0625 / 16.0, 2.0625 / 16.0, 1.0625 / 16.0, 14.9375 / 16.0,
                14.9375 / 16.0, 14.9375 / 16.0);

        double fillFraction = (double) stack.amount / tank.getCapacity();
        boolean gas = fluid.isGaseous(stack);
        if (gas) {
            partialFluidBox.min.y = Math.max(13.9375 - (11.875 * fillFraction), 2.0) / 16.0;
        } else {
            partialFluidBox.max.y = Math.min((11.875 * fillFraction) + 2.0625, 14.0) / 16.0;
        }

        renderState.setFluidColour(stack);
        ResourceLocation fluidStill = fluid.getStill(stack);
        TextureAtlasSprite fluidStillSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(fluidStill.toString());

        Textures.renderFace(renderState, translation, pipeline, frontFacing, partialFluidBox, fluidStillSprite,
                BlockRenderLayer.CUTOUT_MIPPED);

        Textures.renderFace(renderState, translation, pipeline, gas ? EnumFacing.DOWN : EnumFacing.UP, partialFluidBox,
                fluidStillSprite,
                BlockRenderLayer.CUTOUT_MIPPED);

        GlStateManager.resetColor();

        renderState.reset();
    }

    /**
     * Takes in the difference in x, y, and z from the camera to the rendering TE and
     * calculates the squared distance and checks if it's within the range squared
     * 
     * @param x     the difference in x from entity to this rendering TE
     * @param y     the difference in y from entity to this rendering TE
     * @param z     the difference in z from entity to this rendering TE
     * @param range distance needed to be rendered
     * @return true if the camera is within the given range, otherwise false
     */
    public static boolean canRender(double x, double y, double z, double range) {
        double distance = (x * x) + (y * y) + (z * z);
        return distance < range * range;
    }

    public static void renderTankAmount(double x, double y, double z, EnumFacing frontFacing, long amount) {
        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

        renderAmountText(x, y, z, amount, frontFacing);

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
    }

    public static void renderAmountText(double x, double y, double z, long amount, EnumFacing frontFacing) {
        if (!ConfigHolder.client.enableFancyChestRender || !canRender(x, y, z, 64))
            return;

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
        textRenderer.setText(amountText);
        textRenderer.draw(0, 24, 64, 28);
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
