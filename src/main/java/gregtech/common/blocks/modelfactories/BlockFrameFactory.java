package gregtech.common.blocks.modelfactories;

import gregtech.api.model.AbstractBlockModelFactory;
import gregtech.api.model.ResourcePackHook;
import gregtech.api.unification.material.MaterialIconType;
import gregtech.api.unification.material.type.Material;
import gregtech.common.blocks.BlockFrame;
import net.minecraft.block.Block;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockFrameFactory extends AbstractBlockModelFactory {

    public static void init() {
        BlockFrameFactory factory = new BlockFrameFactory();
        ResourcePackHook.addResourcePackFileHook(factory);
    }

    private BlockFrameFactory() {
        super("frame_block", "frame_");
    }

    @Override
    protected String fillSample(Block block, String blockStateSample) {
        Material material = ((BlockFrame) block).frameMaterial;
        return blockStateSample.replace("$MATERIAL$", material.toString())
            .replace("$TEXTURE$", MaterialIconType.frameGt.getBlockPath(material.materialIconSet).toString());
    }


}
