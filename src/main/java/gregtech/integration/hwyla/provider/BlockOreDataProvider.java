package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.unification.ore.StoneType;
import gregtech.common.blocks.BlockOre;
import gregtech.integration.hwyla.HWYLAModule;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import mcp.mobius.waila.api.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockOreDataProvider implements IWailaDataProvider {

    public static final BlockOreDataProvider INSTANCE = new BlockOreDataProvider();

    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, BlockOre.class);
        registrar.addConfig(GTValues.MODID, "gregtech.block_ore");
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.block_ore")) {
            return tooltip;
        }

        if (accessor.getBlock() instanceof BlockOre ore) {
            StoneType type = accessor.getBlockState().getValue(ore.STONE_TYPE);
            if (accessor.getPlayer().isSneaking() && !type.shouldBeDroppedAsItem) {
                tooltip.add(I18n.format("gregtech.top.block_drops") + ":");
                ItemStack itemDropped = ore.getItem(accessor.getWorld(), accessor.getPosition(),
                        accessor.getBlockState());
                tooltip.add(HWYLAModule.wailaStackWithName(itemDropped));
            }
        }
        return tooltip;
    }
}
