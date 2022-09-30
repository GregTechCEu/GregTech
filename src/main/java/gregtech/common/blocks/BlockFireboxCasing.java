package gregtech.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantActiveBlock;
import gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockFireboxCasing extends VariantActiveBlock<FireboxCasingType> {

    public BlockFireboxCasing() {
        super(Material.IRON);
        setTranslationKey("boiler_casing");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, FireboxCasingType.BRONZE_FIREBOX));
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull SpawnPlacementType type) {
        return false;
    }

    @Override
    public int getLightValue(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        if (ACTIVE_BLOCKS.get(Minecraft.getMinecraft().world.provider.getDimension()).contains(pos)) {
            return 15;
        }
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getPackedLightmapCoords(IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
        ACTIVE_BLOCKS.putIfAbsent(Minecraft.getMinecraft().world.provider.getDimension(), new ObjectOpenHashSet<>());
        if (ACTIVE_BLOCKS.get(Minecraft.getMinecraft().world.provider.getDimension()).contains(pos)) {
            return 0b10100000 << 16 | 0b10100000;
        }
        return source.getCombinedLight(pos, state.getLightValue(source, pos));
    }

    public enum FireboxCasingType implements IStringSerializable, IStateHarvestLevel {

        BRONZE_FIREBOX("bronze_firebox", 1),
        STEEL_FIREBOX("steel_firebox", 2),
        TITANIUM_FIREBOX("titanium_firebox", 2),
        TUNGSTENSTEEL_FIREBOX("tungstensteel_firebox", 3);

        private final String name;
        private final int harvestLevel;

        FireboxCasingType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return harvestLevel;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }
    }

}
