package gregtech.api.graphnet.pipenet.physical.block;

import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import gregtech.api.graphnet.pipenet.physical.tile.PipeMaterialTileEntity;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.util.GTUtility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public abstract class PipeMaterialBlock extends WorldPipeBlock {

    public final MaterialRegistry registry;

    public PipeMaterialBlock(IPipeMaterialStructure structure, MaterialRegistry registry) {
        super(structure);
        this.registry = registry;
    }

    @Override
    public IPipeMaterialStructure getStructure() {
        return (IPipeMaterialStructure) super.getStructure();
    }

    @NotNull
    public ItemStack getItem(@NotNull Material material) {
        return new ItemStack(this, 1, registry.getIDForObject(material));
    }

    @Nullable
    public Material getMaterialForStack(@NotNull ItemStack stack) {
        return registry.getObjectById(stack.getMetadata());
    }

    @Override
    public ItemStack getDrop(IBlockAccess world, BlockPos pos, IBlockState state) {
        PipeMaterialTileEntity tile = getTileEntity(world, pos);
        Material material;
        if (tile != null) material = tile.getMaterial();
        else material = Materials.Aluminium;
        return getItem(material);
    }

    @Override
    protected @NotNull IPipeNetNodeHandler getHandler(IBlockAccess world, BlockPos pos) {
        PipeMaterialTileEntity tile = getTileEntity(world, pos);
        if (tile != null) tile.getMaterial().getProperty(PropertyKey.PIPENET_PROPERTIES);
        return Materials.Aluminium.getProperty(PropertyKey.PIPENET_PROPERTIES);
    }

    // tile entity //

    @Override
    public @Nullable PipeMaterialTileEntity getTileEntity(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        if (GTUtility.arePosEqual(lastTilePos, pos)) {
            PipeTileEntity tile = lastTile.get();
            if (tile != null) return (PipeMaterialTileEntity) tile;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof PipeMaterialTileEntity pipe) {
            lastTilePos = pos;
            lastTile = new WeakReference<>(pipe);
            return pipe;
        } else return null;
    }

    @Override
    public @NotNull PipeTileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state) {
        return new PipeMaterialTileEntity(this);
    }

    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos) {
        PipeMaterialTileEntity tile = getTileEntity(world, blockPos);
        if (tile != null) {
            return ImmutablePair.of(getStructure().getModel().getParticleTexture(tile.getMaterial()),
                    tile.getPaintingColor());
        }
        return null;
    }
}
