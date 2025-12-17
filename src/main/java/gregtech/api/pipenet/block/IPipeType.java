package gregtech.api.pipenet.block;

import gregtech.client.renderer.pipe.PipeModelRedirector;

import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPipeType<NodeDataType> extends IStringSerializable {

    float getThickness();

    NodeDataType modifyProperties(NodeDataType baseProperties);

    boolean isPaintable();

    @SideOnly(Side.CLIENT)
    PipeModelRedirector getModel();
}
