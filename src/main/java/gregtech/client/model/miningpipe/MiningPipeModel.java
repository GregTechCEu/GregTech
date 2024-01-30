package gregtech.client.model.miningpipe;

import net.minecraft.client.renderer.block.model.IBakedModel;

import org.jetbrains.annotations.NotNull;

public interface MiningPipeModel {

    @NotNull
    IBakedModel getBaseModel();

    @NotNull
    IBakedModel getBottomModel();
}
