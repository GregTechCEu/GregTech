package gregtech.client.renderer.pipe;

import gregtech.api.block.UnlistedPropertyMaterial;
import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import gregtech.api.graphnet.pipenet.physical.block.WorldPipeBlock;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.cache.BlockableSQC;
import gregtech.client.renderer.pipe.cache.RestrictiveSQC;
import gregtech.client.renderer.pipe.cache.StructureQuadCache;
import gregtech.client.renderer.pipe.quad.PipeQuadHelper;
import gregtech.client.renderer.pipe.util.PipeSpriteWoodClarifier;
import gregtech.client.renderer.pipe.util.WoodCacheKey;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SideOnly(Side.CLIENT)
public class PipeModel extends AbstractPipeModel<WoodCacheKey> {

    public static final UnlistedPropertyMaterial MATERIAL_PROPERTY = new UnlistedPropertyMaterial("material");

    public static final PipeModel[] INSTANCES = new PipeModel[7];
    public static final PipeModel[] RESTRICTIVE_INSTANCES = new PipeModel[INSTANCES.length];

    private static final ResourceLocation loc = GTUtility.gregtechId("block/pipe_material");

    static {
        model(0, wood -> Textures.PIPE_TINY.get());
        model(1, wood -> wood ? Textures.PIPE_SMALL_WOOD.get() : Textures.PIPE_SMALL.get());
        model(2, wood -> wood ? Textures.PIPE_NORMAL_WOOD.get() : Textures.PIPE_NORMAL.get());
        model(3, wood -> wood ? Textures.PIPE_LARGE_WOOD.get() : Textures.PIPE_LARGE.get());
        model(4, wood -> Textures.PIPE_HUGE.get());
        model(5, wood -> Textures.PIPE_QUADRUPLE.get());
        model(6, wood -> Textures.PIPE_NONUPLE.get());
    }

    private static void model(int i, PipeSpriteWoodClarifier clarifier) {
        INSTANCES[i] = new PipeModel(clarifier, false, i + "_standard");
        RESTRICTIVE_INSTANCES[i] = new PipeModel(clarifier, true, i + "_restrictive");
    }

    private final @NotNull PipeSpriteWoodClarifier inTex;
    private final @NotNull PipeSpriteWoodClarifier sideTex;
    private final @Nullable PipeSpriteWoodClarifier restrictiveTex;
    private final @NotNull PipeSpriteWoodClarifier blockedTex;

    public PipeModel(@NotNull PipeSpriteWoodClarifier inTex, @NotNull PipeSpriteWoodClarifier sideTex,
                     @Nullable PipeSpriteWoodClarifier restrictiveTex,
                     @NotNull PipeSpriteWoodClarifier blockedTex, String variant) {
        super(new ModelResourceLocation(loc, variant));
        this.inTex = inTex;
        this.sideTex = sideTex;
        this.restrictiveTex = restrictiveTex;
        this.blockedTex = blockedTex;
    }

    public PipeModel(@NotNull PipeSpriteWoodClarifier inTex, @NotNull PipeSpriteWoodClarifier sideTex,
                     boolean restrictive, String variant) {
        this(inTex, sideTex, restrictive ? wood -> Textures.RESTRICTIVE_OVERLAY.get() : null,
                wood -> Textures.PIPE_BLOCKED_OVERLAY_UP.get(), variant);
    }

    public PipeModel(@NotNull PipeSpriteWoodClarifier inTex, boolean restrictive, String variant) {
        this(inTex, wood -> wood ? Textures.PIPE_SIDE_WOOD.get() : Textures.PIPE_SIDE.get(), restrictive, variant);
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return sideTex.getSprite(false).sprite();
    }

    public @NotNull TextureAtlasSprite getParticleTexture(Material material) {
        return sideTex.getSprite(material.hasProperty(PropertyKey.WOOD)).sprite();
    }

    @Override
    protected @NotNull WoodCacheKey toKey(@NotNull IExtendedBlockState state) {
        return new WoodCacheKey(state.getValue(THICKNESS_PROPERTY),
                state.getValue(MATERIAL_PROPERTY).hasProperty(PropertyKey.WOOD));
    }

    @Override
    protected StructureQuadCache constructForKey(WoodCacheKey key) {
        if (restrictiveTex != null) {
            return RestrictiveSQC.create(PipeQuadHelper.create(key.getThickness()), inTex.getSprite(key.isWood()),
                    sideTex.getSprite(key.isWood()), blockedTex.getSprite(key.isWood()),
                    restrictiveTex.getSprite(key.isWood()));
        } else {
            return BlockableSQC.create(PipeQuadHelper.create(key.getThickness()), inTex.getSprite(key.isWood()),
                    sideTex.getSprite(key.isWood()), blockedTex.getSprite(key.isWood()));
        }
    }

    @Override
    @Nullable
    protected PipeItemModel<WoodCacheKey> getItemModel(@NotNull ItemStack stack, World world, EntityLivingBase entity) {
        WorldPipeBlock block = WorldPipeBlock.getBlockFromItem(stack);
        if (block == null) return null;
        Material mater = null;
        boolean wood = block instanceof PipeMaterialBlock mat && (mater = mat.getMaterialForStack(stack)) != null &&
                mater.hasProperty(PropertyKey.WOOD);
        return new PipeItemModel<>(this, new WoodCacheKey(block.getStructure().getRenderThickness(), wood), mater != null ? GTUtility.convertRGBtoARGB(mater.getMaterialRGB()) : PipeTileEntity.DEFAULT_COLOR);
    }

    public static void registerModels(IRegistry<ModelResourceLocation, IBakedModel> registry) {
        for (PipeModel model : INSTANCES) {
            registry.putObject(model.getLoc(), model);
        }
    }
}
