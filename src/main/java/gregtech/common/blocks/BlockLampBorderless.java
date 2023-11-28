package gregtech.common.blocks;

import gregtech.client.model.lamp.LampModelType;
import gregtech.client.utils.BloomEffectUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockRenderLayer;

import org.jetbrains.annotations.NotNull;

public class BlockLampBorderless extends BlockLamp {

    public BlockLampBorderless(EnumDyeColor color) {
        super(color);
    }

    @Override
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        return layer == BloomEffectUtil.getEffectiveBloomLayer(isLightActive(state) && state.getValue(BLOOM),
                BlockRenderLayer.SOLID);
    }

    @NotNull
    @Override
    protected LampModelType getModelType() {
        return LampModelType.BORDERLESS_LAMP;
    }
}
