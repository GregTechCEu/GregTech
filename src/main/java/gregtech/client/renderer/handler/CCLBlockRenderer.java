package gregtech.client.renderer.handler;

import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ModCompatibility;
import gregtech.client.renderer.ICCLBlockRenderer;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.block.BlockRenderingRegistry;
import codechicken.lib.render.block.ICCBlockRenderer;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;

@SideOnly(Side.CLIENT)
public class CCLBlockRenderer implements ICCBlockRenderer, IItemRenderer {

    public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(
            GTUtility.gregtechId("ccl_block"), "normal");
    public static final CCLBlockRenderer INSTANCE = new CCLBlockRenderer();
    public static EnumBlockRenderType BLOCK_RENDER_TYPE;
    public static Minecraft mc = Minecraft.getMinecraft();

    public static void preInit() {
        BLOCK_RENDER_TYPE = BlockRenderingRegistry.createRenderType("gregtech_ccl_block");
        BlockRenderingRegistry.registerRenderer(BLOCK_RENDER_TYPE, INSTANCE);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        TextureUtils.addIconRegister(Textures::register);
    }

    @SubscribeEvent
    public void onModelsBake(ModelBakeEvent event) {
        GTLog.logger.info("Injected ccl block render model");
        event.getModelRegistry().putObject(MODEL_LOCATION, this);
    }

    @Override
    public void renderItem(ItemStack rawStack, ItemCameraTransforms.TransformType transformType) {
        ItemStack stack = ModCompatibility.getRealItemStack(rawStack);
        if (stack.getItem() instanceof ItemBlock &&
                ((ItemBlock) stack.getItem()).getBlock() instanceof ICCLBlockRenderer) {
            ((ICCLBlockRenderer) ((ItemBlock) stack.getItem()).getBlock()).renderItem(stack, transformType);
        }
    }

    @Override
    public boolean renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, BufferBuilder buffer) {
        if (state != null && (state.getBlock() instanceof ICCLBlockRenderer)) {
            ((ICCLBlockRenderer) state.getBlock()).renderBlock(world, pos, state, buffer);
            return true;
        }
        return false;
    }

    @Override
    public IModelState getTransforms() {
        return TransformUtils.DEFAULT_BLOCK;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public void renderBrightness(IBlockState state, float brightness) {}

    @Override
    public void handleRenderBlockDamage(IBlockAccess world, BlockPos pos, IBlockState state, TextureAtlasSprite sprite,
                                        BufferBuilder buffer) {
        if (state == null || !(state.getBlock() instanceof ICCLBlockRenderer)) {
            return;
        }
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buffer);
        renderState.setPipeline(new Vector3(new Vec3d(pos)).translation(), new IconTransformation(sprite));
        codechicken.lib.render.BlockRenderer.renderCuboid(renderState, Cuboid6.full, 0);
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return TextureUtils.getMissingSprite();
    }

    @Override
    public void registerTextures(TextureMap map) {}

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }
}
