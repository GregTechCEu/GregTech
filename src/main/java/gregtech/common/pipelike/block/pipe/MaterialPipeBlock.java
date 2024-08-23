package gregtech.common.pipelike.block.pipe;

import gregtech.api.graphnet.pipenet.physical.IBurnable;
import gregtech.api.graphnet.pipenet.physical.IFreezable;
import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.common.creativetab.GTCreativeTabs;

public class MaterialPipeBlock extends PipeMaterialBlock implements IBurnable, IFreezable {

    public MaterialPipeBlock(MaterialPipeStructure structure, MaterialRegistry registry) {
        super(structure, registry);
        setCreativeTab(GTCreativeTabs.TAB_GREGTECH_PIPES);
    }

    @Override
    public MaterialPipeStructure getStructure() {
        return (MaterialPipeStructure) super.getStructure();
    }
}
