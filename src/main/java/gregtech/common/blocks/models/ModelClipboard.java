package gregtech.common.blocks.models;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ModelClipboard implements IBakedModel {
    private IModel model = null;

    private IBakedModel baseModel;

    public static final ModelResourceLocation modelResourceLocation = new ModelResourceLocation("bibliocraft:Clipboard");

    public static final ModelResourceLocation clipboardBasicModel = new ModelResourceLocation("bibliocraft:clipboardsimple");

    private CustomItemOverrideList overrides = new CustomItemOverrideList();

    public IBakedModel wrapper;

    private FontRenderer textRender;

    private ModelCache cache;

    private boolean gotOBJ = false;

    protected Function<ResourceLocation, TextureAtlasSprite> textureGetter;

    private void getModel(IBlockState state, ItemStack stack) {
        if (this.model == null || (this.model != null && !this.model.toString().contains("obj.OBJModel")))
            try {
                this.model = ModelLoaderRegistry.getModel(new ResourceLocation("bibliocraft:block/clipboard.obj"));
                this.model = this.model.process(ImmutableMap.of("flip-v", "true"));
                this.gotOBJ = true;
            } catch (Exception e) {
                this.model = ModelLoaderRegistry.getMissingModel();
                this.gotOBJ = false;
            }
        OBJModel.OBJState modelState = new OBJModel.OBJState(Lists.newArrayList("OBJModel.Group.All.Key"), true);
        if (state != null && state instanceof IExtendedBlockState) {
            IExtendedBlockState exState = (IExtendedBlockState) state;
            if (exState.getUnlistedNames().contains(OBJModel.OBJProperty.INSTANCE)) {
                modelState = exState.getValue(OBJModel.OBJProperty.INSTANCE);
                IBakedModel bakedModel = this.model.bake(modelState, DefaultVertexFormats.ITEM, this.textureGetter);
                this.baseModel = bakedModel;
            }
            if (modelState == null)
                return;
        }
        if (state == null /*&& stack.getItem() == ItemClipboard.instance*/) {
            String cacheName = "clipboard" + getModelPartsNumberString(stack);
            if (this.cache.findModel(cacheName)) {
                this.baseModel = this.cache.getCurrentMatch();
            } else {
                modelState = new OBJModel.OBJState(getModelParts(stack), true);
                IBakedModel bakedModel = this.model.bake(new OBJModel.OBJState(getModelParts(stack), true), DefaultVertexFormats.ITEM, (Function<ResourceLocation, TextureAtlasSprite>) this.textureGetter);
                if (this.gotOBJ)
                    this.cache.addToCache(bakedModel, cacheName);
                this.baseModel = bakedModel;
            }
        }
    }

    private String getModelPartsNumberString(ItemStack stack) {
        NBTTagCompound tags = stack.getTagCompound();
        String value = "";
        if (tags != null) {
            int currentPage = tags.getInteger("currentPage");
            int totalPages = tags.getInteger("totalPages");
            String pagenum = "page" + currentPage;
            NBTTagCompound pagetag = tags.getCompoundTag(pagenum);
            if (pagetag != null) {
                int[] states = pagetag.getIntArray("taskStates");
                if (states != null && states.length == 9)
                    for (int i = 0; i < states.length; i++)
                        value = value + states[i];
            }
        }
        return value;
    }

    public List<String> getModelParts(ItemStack stack) {
        List<String> modelParts = new ArrayList<>();
        NBTTagCompound tags = stack.getTagCompound();
        if (tags != null) {
            int currentPage = tags.getInteger("currentPage");
            int totalPages = tags.getInteger("totalPages");
            String pagenum = "page" + currentPage;
            NBTTagCompound pagetag = tags.getCompoundTag(pagenum);
            if (pagetag != null) {
                int[] states = pagetag.getIntArray("taskStates");
                if (states != null && states.length == 9) {
                    for (int i = 0; i < 9; i++) {
                        modelParts.add(states[i] == 1 ? String.format("box%sc", i) : String.format("box%sx", i));
                    } // Look at that, Nuchaz. 70 lines reduced into one ternary. What were you thinking??????
                }
            }
        }
        modelParts.add("Clipboard");
        return modelParts;
    }

    public ModelClipboard() {
        this.textureGetter = location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("bibliocraft:models/clipboard");
        this.textRender = (Minecraft.getMinecraft()).fontRenderer;
        this.wrapper = this;
        this.cache = new ModelCache();
    }

    public boolean isAmbientOcclusion() {
        return false;
    }

    public boolean isGui3d() {
        return false;
    }

    public boolean isBuiltInRenderer() {
        return false;
    }

    public TextureAtlasSprite getParticleTexture() {
        if (this.baseModel.getParticleTexture() == null)
            return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/planks_oak");
        return this.baseModel.getParticleTexture();
    }

    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        TRSRTransformation transform = new TRSRTransformation(new Vector3f(0.0F, 0.0F, 0.0F), new Quat4f(0.0F, 0.0F, 0.0F, 1.0F), new Vector3f(1.0F, 1.0F, 1.0F), new Quat4f(0.0F, 0.0F, 0.0F, 1.0F));
        switch (cameraTransformType) {
            case FIRST_PERSON_RIGHT_HAND:
                transform = new TRSRTransformation(new Vector3f(-0.05F, 0.25F, 0.3F), new Quat4f(0.0F, -1.0F, 0.0F, 1.0F), new Vector3f(0.5F, 0.5F, 0.5F), new Quat4f(0.0F, 0.0F, 0.0F, 1.0F));
                break;
            case FIRST_PERSON_LEFT_HAND:
                transform = new TRSRTransformation(new Vector3f(0.0F, 0.25F, 0.3F), new Quat4f(0.0F, 1.0F, 0.0F, 1.0F), new Vector3f(0.5F, 0.5F, 0.5F), new Quat4f(0.0F, 0.0F, 0.0F, 1.0F));
                break;
            case THIRD_PERSON_RIGHT_HAND:
                transform = new TRSRTransformation(new Vector3f(0.0F, 0.2F, 0.4F), new Quat4f(0.0F, -1.0F, 0.0F, 1.0F), new Vector3f(0.75F, 0.75F, 0.75F), new Quat4f(0.0F, 0.0F, 0.0F, 1.0F));
                break;
            case THIRD_PERSON_LEFT_HAND:
                transform = new TRSRTransformation(new Vector3f(0.0F, 0.2F, 0.4F), new Quat4f(0.0F, 1.0F, 0.0F, 1.0F), new Vector3f(0.75F, 0.75F, 0.75F), new Quat4f(0.0F, 0.0F, 0.0F, 1.0F));
                break;
            case GUI:
                transform = new TRSRTransformation(new Vector3f(0.0F, 0.0F, 0.0F), new Quat4f(0.0F, -1.0F, 0.0F, 1.0F), new Vector3f(1.0F, 1.0F, 1.0F), new Quat4f(0.0F, 0.0F, 0.0F, 1.0F));
                break;
            case GROUND:
                transform = new TRSRTransformation(new Vector3f(0.35F, 0.15F, 0.0F), new Quat4f(0.0F, 0.0F, 0.0F, 1.0F), new Vector3f(0.75F, 0.75F, 0.75F), new Quat4f(0.0F, 0.0F, 0.0F, 1.0F));
                break;
        }
        return Pair.of(this, transform.getMatrix());
    }

    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        getModel(state, ItemStack.EMPTY);
        try {
            List<BakedQuad> q = this.baseModel.getQuads(state, side, rand);
            return q;
        } catch (NullPointerException e) {
            return new ArrayList<>();
        }
    }

    public ItemOverrideList getOverrides() {
        return this.overrides;
    }

    private class CustomItemOverrideList extends ItemOverrideList {
        private CustomItemOverrideList() {
            super(ImmutableList.of());
        }

        @Nonnull
        public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nonnull World world, @Nonnull EntityLivingBase entity) {
            ModelClipboard.this.getModel(null, stack);
            return ModelClipboard.this.wrapper;
        }
    }
}
