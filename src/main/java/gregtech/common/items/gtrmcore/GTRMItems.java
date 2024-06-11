package gregtech.common.items.gtrmcore;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

public class GTRMItems {

    public static final Item COBBLESTONE_SAW;
    public static final Item WOODEN_HARD_HAMMER;

    static {
        COBBLESTONE_SAW = new CobblestoneSaw();
        WOODEN_HARD_HAMMER = new WoodenHardHammer();
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        registerItemModel(COBBLESTONE_SAW);
        registerItemModel(WOODEN_HARD_HAMMER);
    }

    @SideOnly(Side.CLIENT)
    private static void registerItemModel(@NotNull Item item) {
        // noinspection ConstantConditions
        ModelLoader.setCustomModelResourceLocation(item,
                0,
                new ModelResourceLocation(item.getRegistryName(),
                        "inventory"));
    }
}
