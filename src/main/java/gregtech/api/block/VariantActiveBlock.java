package gregtech.api.block;

import gregtech.api.GTValues;
import gregtech.api.util.GTUtility;
import gregtech.client.model.IModelSupplier;
import gregtech.client.model.modelfactories.ActiveVariantBlockBakedModel;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.chisel.ctm.client.state.CTMExtendedState;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class VariantActiveBlock<T extends Enum<T> & IStringSerializable> extends VariantBlock<T> implements IModelSupplier {

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

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == getRenderLayer() ||
                layer == (isBloomEnabled(getState(state)) ? BloomEffectUtil.getRealBloomLayer() : BlockRenderLayer.CUTOUT);
    }

    @Nonnull
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
        return meta + state.getValue(VARIANT).ordinal();
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        Class<T> enumClass = GTUtility.getActualTypeParameter(getClass(), VariantActiveBlock.class, 0);
        this.VARIANT = PropertyEnum.create("variant", enumClass);
        this.VALUES = enumClass.getEnumConstants();
        return new ExtendedBlockState(this, new IProperty[]{VARIANT, ACTIVE_DEPRECATED}, new IUnlistedProperty[]{ACTIVE});
    }

    @Override
    public IExtendedBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState ext = ((IExtendedBlockState) state)
                .withProperty(ACTIVE, Minecraft.getMinecraft().world != null &&
                        isBlockActive(Minecraft.getMinecraft().world.provider.getDimension(), pos));

        if (Loader.isModLoaded(GTValues.MODID_CTM)) {
            //if the Connected Textures Mod is loaded we wrap our IExtendedBlockState with their wrapper,
            //so that the CTM renderer can render the block properly.
            return new CTMExtendedState(ext, world, pos);
        }
        return ext;
    }

    @Override
    public void onTextureStitch(TextureStitchEvent.Pre event) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onModelRegister() {
        Map<T, ModelResourceLocation> models = new EnumMap<>(VALUES[0].getDeclaringClass());
        for (T value : VALUES) {
            ModelResourceLocation inactiveModel = model(false, value);
            ModelResourceLocation activeModel = model(true, value);

            ActiveVariantBlockBakedModel model = new ActiveVariantBlockBakedModel(inactiveModel, activeModel, () -> isBloomEnabled(value));
            models.put(value, model.getModelLocation());

            Item item = Item.getItemFromBlock(this);
            ModelLoader.setCustomModelResourceLocation(item, value.ordinal(), inactiveModel);
            ModelLoader.registerItemVariants(item, activeModel);
        }
        ModelLoader.setCustomStateMapper(this, b -> b.getBlockState().getValidStates().stream().collect(Collectors.toMap(
                s -> s,
                s -> models.get(s.getValue(VARIANT))
        )));
    }

    private ModelResourceLocation model(boolean active, T variant) {
        return new ModelResourceLocation(
                Objects.requireNonNull(getRegistryName()),
                "active=" + active + ",variant=" + VARIANT.getName(variant));
    }

    @SideOnly(Side.CLIENT)
    protected boolean isBloomEnabled(T value) {
        return ConfigHolder.client.machinesEmissiveTextures;
    }
}
