package gregtech.api.util;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import org.jetbrains.annotations.NotNull;

public class BlockUtility {

    private static final BlockWrapper WRAPPER = new BlockWrapper();

    private static class BlockWrapper extends Block {

        public BlockWrapper() {
            super(Material.AIR);
        }

        @NotNull
        @Override
        public NonNullList<ItemStack> captureDrops(boolean start) {
            return super.captureDrops(start);
        }
    }

    public static void startCaptureDrops() {
        WRAPPER.captureDrops(true);
    }

    public static NonNullList<ItemStack> stopCaptureDrops() {
        return WRAPPER.captureDrops(false);
    }
}
