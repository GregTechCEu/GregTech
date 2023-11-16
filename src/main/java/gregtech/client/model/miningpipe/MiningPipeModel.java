package gregtech.client.model.miningpipe;

import net.minecraft.client.renderer.block.model.IBakedModel;

import javax.annotation.Nonnull;

public interface MiningPipeModel {

    @Nonnull
    IBakedModel getBaseModel();

    @Nonnull
    IBakedModel getBottomModel();
}
