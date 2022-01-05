package gregtech.client.model;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IModelSupplier {

    @SideOnly(Side.CLIENT)
    void onTextureStitch(TextureStitchEvent.Pre event);

    @SideOnly(Side.CLIENT)
    void onModelRegister();
}
