package gregtech.common.blocks.wood;

import gregtech.api.block.VariantBlock;
import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

public class BlockGregPlanks extends VariantBlock<BlockGregPlanks.BlockType> {

    public BlockGregPlanks() {
        super(Material.WOOD);
        setTranslationKey("planks");
        setHardness(2.0F);
        setResistance(5.0F);
        setSoundType(SoundType.WOOD);
        setHarvestLevel(ToolClasses.AXE, 0);
        setDefaultState(getState(BlockType.RUBBER_PLANK));
    }

    public enum BlockType implements IStringSerializable {

        RUBBER_PLANK("rubber"),
        TREATED_PLANK("treated");

        private final String name;

        BlockType(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }
    }
}
