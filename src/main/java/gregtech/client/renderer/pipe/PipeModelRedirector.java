package gregtech.client.renderer.pipe;

import gregtech.api.unification.material.Material;
import gregtech.client.renderer.pipe.util.MaterialModelSupplier;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class PipeModelRedirector implements IBakedModel {

    private final boolean ambientOcclusion;
    private final boolean gui3d;

    public final MaterialModelSupplier supplier;
    public final Function<ItemStack, Material> stackMaterialFunction;

    private final ModelResourceLocation loc;

    private final FakeItemOverrideList fakeItemOverrideList = new FakeItemOverrideList();

    public PipeModelRedirector(ModelResourceLocation loc, MaterialModelSupplier supplier,
                               Function<ItemStack, Material> stackMaterialFunction) {
        this(loc, supplier, stackMaterialFunction, true, true);
    }

    public PipeModelRedirector(ModelResourceLocation loc, MaterialModelSupplier supplier,
                               Function<ItemStack, Material> stackMaterialFunction,
                               boolean ambientOcclusion, boolean gui3d) {
        this.loc = loc;
        this.supplier = supplier;
        this.stackMaterialFunction = stackMaterialFunction;
        this.ambientOcclusion = ambientOcclusion;
        this.gui3d = gui3d;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        if (state instanceof IExtendedBlockState ext) {
            Optional<Material> mat = (Optional<Material>) ext.getUnlistedProperties()
                    .get(AbstractPipeModel.MATERIAL_PROPERTY);
            // noinspection OptionalAssignedToNull
            return supplier.getModel(mat == null ? null : mat.orElse(null)).getQuads(ext, side, rand);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return ambientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return gui3d;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    public Pair<TextureAtlasSprite, Integer> getParticleTexture(int paintColor, @Nullable Material material) {
        return supplier.getModel(material).getParticleTexture(paintColor, material);
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return Textures.WIRE.get().sprite();
    }

    @Override
    public @NotNull ItemOverrideList getOverrides() {
        return fakeItemOverrideList;
    }

    public ModelResourceLocation getLoc() {
        return loc;
    }

    @FunctionalInterface
    public interface Supplier {

        PipeModelRedirector create(ModelResourceLocation loc, MaterialModelSupplier supplier,
                                   Function<ItemStack, Material> stackMaterialFunction);
    }

    protected class FakeItemOverrideList extends ItemOverrideList {

        @Override
        public @NotNull IBakedModel handleItemState(@NotNull IBakedModel originalModel, @NotNull ItemStack stack,
                                                    World world, EntityLivingBase entity) {
            if (originalModel instanceof PipeModelRedirector model) {

                PipeItemModel<?> item = model.supplier.getModel(model.stackMaterialFunction.apply(stack))
                        .getItemModel(PipeModelRedirector.this, stack, world, entity);
                if (item != null) return item;
            }
            return originalModel;
        }
    }
}
