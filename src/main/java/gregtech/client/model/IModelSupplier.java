package gregtech.api.model;

import net.minecraftforge.client.event.TextureStitchEvent;

public interface IModelSupplier {

    void onTextureStitch(TextureStitchEvent.Pre event);

    void onModelRegister();
}
