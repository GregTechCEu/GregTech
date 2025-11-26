package gregtech.api.block;

import gregtech.api.util.Mods;
import gregtech.client.model.ActiveVariantBlockBakedModel;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;
import team.chisel.ctm.client.state.CTMExtendedState;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public abstract class VariantActiveBlock<T extends IStringSerializable & Comparable<T>> extends VariantBlock<T> {

    private static final Int2ObjectMap<ObjectSet<BlockPos>> ACTIVE_BLOCKS = new Int2ObjectOpenHashMap<>();
    private static final ReadWriteLock ACTIVE_BLOCKS_LOCK = new ReentrantReadWriteLock();

    public static final PropertyBool ACTIVE_DEPRECATED = PropertyBool.create("active");
    public static final UnlistedBooleanProperty ACTIVE = new UnlistedBooleanProperty("active");

    public static boolean isBlockActive(int dimension, BlockPos pos) {
        ACTIVE_BLOCKS_LOCK.readLock().lock();
        try {
            ObjectSet<BlockPos> set = ACTIVE_BLOCKS.get(dimension);
            return set != null && set.contains(pos);
        } finally {
            ACTIVE_BLOCKS_LOCK.readLock().unlock();
        }
    }

    public static void setBlockActive(int dimension, BlockPos pos, boolean active) {
        ACTIVE_BLOCKS_LOCK.writeLock().lock();
        try {
            ObjectSet<BlockPos> set = ACTIVE_BLOCKS.get(dimension);
            if (active) {
                if (set == null) {
                    ACTIVE_BLOCKS.put(dimension, set = new ObjectOpenHashSet<>());
                }
                set.add(pos);
            } else {
                if (set != null) set.remove(pos);
            }
        } finally {
            ACTIVE_BLOCKS_LOCK.writeLock().unlock();
        }
    }

    public VariantActiveBlock(Material materialIn) {
        super(materialIn);
    }

    @Override
    public IBlockState getState(T variant) {
        return super.getState(variant).withProperty(ACTIVE_DEPRECATED, false);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected boolean canSilkHarvest() {
        return false;
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return getRenderLayer(VALUES[0]);
    }

    @NotNull
    public BlockRenderLayer getRenderLayer(T value) {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        return layer == getRenderLayer(getState(state)) ||
                layer == BloomEffectUtil.getEffectiveBloomLayer(isBloomEnabled(getState(state)));
    }

    @NotNull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return super.getStateFromMeta(meta).withProperty(ACTIVE_DEPRECATED, false);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        if (state.getValue(ACTIVE_DEPRECATED)) {
            meta += 8;
        }
        return meta + state.getValue(VARIANT);
    }

    @NotNull
    @Override
    protected BlockStateContainer createBlockState() {
        super.createBlockState();
        return new ExtendedBlockState(this,
                new IProperty[] { VARIANT, ACTIVE_DEPRECATED },
                new IUnlistedProperty[] { ACTIVE });
    }

    @NotNull
    @Override
    public IExtendedBlockState getExtendedState(@NotNull IBlockState state, @NotNull IBlockAccess world,
                                                @NotNull BlockPos pos) {
        IExtendedBlockState ext = ((IExtendedBlockState) state)
                .withProperty(ACTIVE, Minecraft.getMinecraft().world != null &&
                        isBlockActive(Minecraft.getMinecraft().world.provider.getDimension(), pos));

        if (Mods.CTM.isModLoaded()) {
            // if the Connected Textures Mod is loaded we wrap our IExtendedBlockState with their wrapper,
            // so that the CTM renderer can render the block properly.
            return new CTMExtendedState(ext, world, pos);
        }
        return ext;
    }

    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        Int2ObjectMap<ModelResourceLocation> models = new Int2ObjectArrayMap<>();
        for (T value : VALUES) {
            int index = VARIANT.getIndexOf(value);
            ModelResourceLocation inactiveModel = model(false, value);
            ModelResourceLocation activeModel = model(true, value);

            ActiveVariantBlockBakedModel model = new ActiveVariantBlockBakedModel(inactiveModel, activeModel,
                    () -> isBloomEnabled(value));
            models.put(index, model.getModelLocation());

            Item item = Item.getItemFromBlock(this);
            ModelLoader.setCustomModelResourceLocation(item, index, inactiveModel);
            ModelLoader.registerItemVariants(item, activeModel);
        }
        ModelLoader.setCustomStateMapper(this,
                b -> b.getBlockState().getValidStates().stream().collect(Collectors.toMap(
                        s -> s,
                        s -> models.get(s.getValue(VARIANT)))));
    }

    private ModelResourceLocation model(boolean active, T variant) {
        return new ModelResourceLocation(
                Objects.requireNonNull(getRegistryName()),
                "active=" + active + ",variant=" + variant.getName());
    }

    @SideOnly(Side.CLIENT)
    public boolean isBloomEnabled(T value) {
        return ConfigHolder.client.machinesEmissiveTextures;
    }
}
