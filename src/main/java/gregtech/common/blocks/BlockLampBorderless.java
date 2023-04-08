package gregtech.common.blocks;

import gregtech.client.model.lamp.LampModelType;
import gregtech.client.shader.Shaders;
import gregtech.client.utils.BloomEffectUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockRenderLayer;

public class BlockLampBorderless extends BlockLamp {

    public BlockLampBorderless(EnumDyeColor color) {
        super(color);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return isLightActive(state) && state.getValue(BLOOM) && !Shaders.isOptiFineShaderPackLoaded() ?
                layer == BloomEffectUtil.BLOOM : layer == BlockRenderLayer.SOLID;
    }

    @Override
    protected LampModelType getModelType() {
        return LampModelType.BORDERLESS_LAMP;
    }
}
