package gregtech.common.blocks.modelfactories;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import gregtech.api.model.AbstractBlockModelFactory;
import gregtech.api.model.ResourcePackHook;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.Material;
import gregtech.common.blocks.BlockCompressed;
import net.minecraft.block.Block;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public class BlockCompressedFactory extends AbstractBlockModelFactory {

    private static final String VARIANT_DEFINITION =
        "\"$MATERIAL$\": {\n" +
            "        \"textures\": {\n" +
            "          \"all\": \"$TEXTURE$\",\n" +
            "          \"particle\": \"$TEXTURE$\"\n" +
            "        }\n" +
            "      }";

    private static final Joiner COMMA_JOINER = Joiner.on(',');

    public static void init() {
        BlockCompressedFactory factory = new BlockCompressedFactory();
        ResourcePackHook.addResourcePackFileHook(factory);
    }

    private BlockCompressedFactory() {
        super("compressed_block", "meta_block_compressed_");
    }

    @Override
    protected String fillSample(Block block, String blockStateSample) {
        ImmutableList<Material> allowedValues = ((BlockCompressed) block).variantProperty.getAllowedValues();
        ArrayList<String> variants = new ArrayList<>();
        for (Material material : allowedValues) {
            variants.add(VARIANT_DEFINITION
                .replace("$MATERIAL$", material.toString())
                .replace("$TEXTURE$", MaterialIconType.block.getBlockPath(material.getMaterialIconSet()).toString())
            );
        }
        return blockStateSample.replace("$VARIANTS$", COMMA_JOINER.join(variants));
    }


}
