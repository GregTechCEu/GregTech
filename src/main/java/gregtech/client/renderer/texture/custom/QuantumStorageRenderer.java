package gregtech.client.renderer.texture.custom;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import gregtech.api.GTValues;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class QuantumStorageRenderer implements TextureUtils.IIconRegister {
    private static final Cuboid6 upBox = new Cuboid6(0 / 16.0, 14 / 16.0, 0 / 16.0, 16 / 16.0, 16 / 16.0, 16 / 16.0);
    private static final Cuboid6 downBox = new Cuboid6(0 / 16.0, 0 / 16.0, 0 / 16.0, 16 / 16.0, 2 / 16.0, 16 / 16.0);
    private static final Cuboid6 westBox = new Cuboid6(0 / 16.0, 0 / 16.0, 0 / 16.0, 2 / 16.0, 16 / 16.0, 16 / 16.0);
    private static final Cuboid6 eastBox = new Cuboid6(14 / 16.0, 0 / 16.0, 0 / 16.0, 16 / 16.0, 16 / 16.0, 16 / 16.0);
    private static final Cuboid6 southBox = new Cuboid6(0 / 16.0, 0 / 16.0, 14 / 16.0, 16 / 16.0, 16 / 16.0, 16 / 16.0);
    private static final Cuboid6 northBox = new Cuboid6(0 / 16.0, 0 / 16.0, 0 / 16.0, 16 / 16.0, 16 / 16.0, 2 / 16.0);
    private static final Cuboid6 glassBox = new Cuboid6(1 / 16.0, 1 / 16.0, 1 / 16.0, 15 / 16.0, 15 / 16.0, 15 / 16.0);
    private static final Cuboid6 fluidBox = new Cuboid6(2 / 16.0, 2 / 16.0, 2 / 16.0, 14 / 16.0, 14 / 16.0, 14 / 16.0);


    private static Map<EnumFacing, Cuboid6> boxFacingMap = new HashMap<>();

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite[] textures;

    private int tier;

    private static Transformation[] rotations = {new Rotation(90, 0, 0, 1), new Rotation(-90, 0, 0, 1), Rotation.quarterRotations[1], Rotation.quarterRotations[2], Rotation.quarterRotations[3], Rotation.quarterRotations[0]};

    static {
        boxFacingMap.put(EnumFacing.UP, upBox);
        boxFacingMap.put(EnumFacing.DOWN, downBox);
        boxFacingMap.put(EnumFacing.WEST, westBox);
        boxFacingMap.put(EnumFacing.EAST, eastBox);
        boxFacingMap.put(EnumFacing.SOUTH, southBox);
        boxFacingMap.put(EnumFacing.NORTH, northBox);
    }

    public QuantumStorageRenderer(int tier) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            textures = new TextureAtlasSprite[2];
        }
        this.tier = tier;
        Textures.iconRegisters.add(this);
    }

    @Override
    public void registerIcons(TextureMap textureMap) {
        this.textures[0] = textureMap.registerSprite(new ResourceLocation("gregtech:blocks/casings/voltage/" + GTValues.VN[tier].toLowerCase() + "/side"));
        this.textures[1] = textureMap.registerSprite(new ResourceLocation("gregtech:blocks/overlay/machine/overlay_screen_glass"));
    }

    public void renderMachine(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, EnumFacing frontFacing) {
        // Rotate a Cuboid6??? Somehow???
        Textures.renderFace(renderState, translation, pipeline, frontFacing, glassBox, textures[1], BlockRenderLayer.CUTOUT_MIPPED);

        boxFacingMap.keySet().forEach(facing -> {
            for (EnumFacing box : EnumFacing.VALUES) {
                if ((facing != frontFacing || box != frontFacing) && (facing != EnumFacing.DOWN || (box == EnumFacing.DOWN || box == EnumFacing.UP))) { // Don't render the front facing box from the front, nor allow Z-fighting to occur on the bottom
                    Textures.renderFace(renderState, translation, pipeline, facing, boxFacingMap.get(box), textures[0], BlockRenderLayer.CUTOUT_MIPPED);
                } else {
                }
            }
        });
    }

    public void renderChest(double x, double y, double z, MetaTileEntityQuantumChest machine, ItemStack stack, long count, float partialTicks) {
        if (!stack.isEmpty() || count == 0) {
            World level = machine.getWorld();
            EnumFacing frontFacing = machine.getFrontFacing();
            RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
            float tick = level.getWorldTime() + partialTicks;
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.translate(0.5D, 0.5D, 0.5D);
            GlStateManager.rotate(tick * (float) Math.PI * 2 / 40, 0, 1, 0);
            GlStateManager.scale(0.6f, 0.6f, 0.6f);
            itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();


            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.translate(frontFacing.getXOffset() * -1 / 16f, frontFacing.getYOffset() * -1 / 16f, frontFacing.getZOffset() * -1 / 16f);
            RenderUtil.moveToFace(0, 0, 0, frontFacing);
            if (frontFacing.getAxis() == EnumFacing.Axis.Y) {
                RenderUtil.rotateToFace(frontFacing, EnumFacing.SOUTH);
            } else {
                RenderUtil.rotateToFace(frontFacing, null);
            }
            String amount = TextFormattingUtil.formatLongToCompactString(count, 4);
            GlStateManager.scale(1f / 64, 1f / 64, 0);
            GlStateManager.translate(-32, -32, 0);
            new TextTexture(amount, 0xFFFFFF).draw(0, 24, 64, 28);
            GlStateManager.popMatrix();
        }
    }

    public void renderTankFluid(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, EnumFacing frontFacing, FluidStack stack) {
        if (stack == null || stack.amount == 0)
            return;
        renderState.setFluidColour(stack);
        ResourceLocation fluidStill = stack.getFluid().getStill(stack);
        TextureAtlasSprite fluidStillSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidStill.toString());
        Textures.renderFace(renderState, translation, pipeline, frontFacing, fluidBox, fluidStillSprite, BlockRenderLayer.CUTOUT_MIPPED);
        GlStateManager.resetColor();
    }

    public void renderTankAmount(double x, double y, double z, EnumFacing frontFacing, float partialTicks, FluidStack stack) {
        if (stack == null || stack.amount == 0)
            return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(frontFacing.getXOffset() * -1 / 16f, frontFacing.getYOffset() * -1 / 16f, frontFacing.getZOffset() * -1 / 16f);
        RenderUtil.moveToFace(0, 0, 0, frontFacing);
        if (frontFacing.getAxis() == EnumFacing.Axis.Y) {
            RenderUtil.rotateToFace(frontFacing, EnumFacing.SOUTH);
        } else {
            RenderUtil.rotateToFace(frontFacing, null);
        }
        String amount = TextFormattingUtil.formatLongToCompactString(stack.amount, 4);
        GlStateManager.scale(1f / 64, 1f / 64, 0);
        GlStateManager.translate(-32, -32, 0);
        GlStateManager.disableLighting();
        new TextTexture(amount, 0xFFFFFF).draw(0, 24, 64, 28);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

}
