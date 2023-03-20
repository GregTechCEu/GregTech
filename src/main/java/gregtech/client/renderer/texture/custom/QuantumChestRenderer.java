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
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class QuantumChestRenderer implements TextureUtils.IIconRegister {
    private static final Cuboid6 upBox = new Cuboid6(0 / 16.0, 16 / 16.0, 0 / 16.0, 16 / 16.0, 16 / 16.0, 16 / 16.0);
    private static final Cuboid6 downBox = new Cuboid6(0 / 16.0, 0 / 16.0, 0 / 16.0, 16 / 16.0, 0 / 16.0, 16 / 16.0);
    private static final Cuboid6 westBox = new Cuboid6(0 / 16.0, 0 / 16.0, 0 / 16.0, 0 / 16.0, 16 / 16.0, 16 / 16.0);
    private static final Cuboid6 eastBox = new Cuboid6(16 / 16.0, 0 / 16.0, 0 / 16.0, 16 / 16.0, 16 / 16.0, 16 / 16.0);
    private static final Cuboid6 southBox = new Cuboid6(0 / 16.0, 0 / 16.0, 16 / 16.0, 16 / 16.0, 16 / 16.0, 16 / 16.0);
    private static final Cuboid6 northBox = new Cuboid6(0 / 16.0, 0 / 16.0, 0 / 16.0, 16 / 16.0, 16 / 16.0, 0 / 16.0);

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

    public QuantumChestRenderer(int tier) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            textures = new TextureAtlasSprite[2];
        }
        this.tier = tier;
        Textures.iconRegisters.add(this);
    }

    @Override
    public void registerIcons(TextureMap textureMap) {
        this.textures[0] = textureMap.registerSprite(new ResourceLocation("gregtech:blocks/casings/voltage/" + GTValues.VN[tier].toLowerCase() + "/side"));
    }

    public void renderMachine(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, EnumFacing frontFacing) {
/*
        translation.rotate(Math.toRadians(rotation.getHorizontalAngle()), Rotation.axes[1]);
*/

        boxFacingMap.keySet().forEach(facing -> {
            if (facing != frontFacing) {
                Textures.renderFace(renderState, translation, pipeline, facing, boxFacingMap.get(facing), textures[0], BlockRenderLayer.CUTOUT_MIPPED);
            }
            Textures.renderFace(renderState, translation, pipeline, facing, boxFacingMap.get(facing.getOpposite()), textures[0], BlockRenderLayer.CUTOUT_MIPPED);

        });
    }

    public void render(double x, double y, double z, MetaTileEntityQuantumChest machine, float partialTicks) {
        ItemStack itemStack = new ItemStack(Items.APPLE, 3);
        if (!itemStack.isEmpty()) {
            World level = machine.getWorld();
            EnumFacing frontFacing = machine.getFrontFacing();
            RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
            float tick = level.getWorldTime() + partialTicks;
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.translate(0.5D, 0.5d, 0.5D);
            GlStateManager.rotate(tick * (float) Math.PI * 2 / 40, 0, 1, 0);
            GlStateManager.scale(0.6f, 0.6f, 0.6f);
            itemRenderer.renderItem(itemStack, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();


            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.disableDepth();
            GlStateManager.translate(frontFacing.getXOffset() * -1 / 16f, frontFacing.getYOffset() * -1 / 16f, frontFacing.getZOffset() * -1 / 16f);
            RenderUtil.moveToFace(0, 0, 0, frontFacing);
            if (frontFacing.getAxis() == EnumFacing.Axis.Y) {
                RenderUtil.rotateToFace(frontFacing, EnumFacing.SOUTH);
            } else {
                RenderUtil.rotateToFace(frontFacing, null);
            }
            String amount = TextFormattingUtil.formatLongToCompactString(itemStack.getCount(), 4);
            GlStateManager.scale(1f / 64, 1f / 64, 0);
            GlStateManager.translate(-32, -32, 0);
            new TextTexture(amount, 0xFFFFFF).draw(0, 24, 64, 28);
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }

}
