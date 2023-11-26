package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.common.blocks.BlockLamp;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LampDataProvider implements IWailaDataProvider {

    public static final LampDataProvider INSTANCE = new LampDataProvider();

    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, BlockLamp.class);
        registrar.addConfig(GTValues.MODID, "gregtech.block_lamp");
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.block_lamp")) {
            return tooltip;
        }

        if (accessor.getBlock() instanceof BlockLamp lamp) {
            IBlockState state = accessor.getBlockState();
            boolean inverted = lamp.isInverted(state);
            boolean bloomEnabled = lamp.isBloomEnabled(state);
            boolean lightEnabled = lamp.isLightEnabled(state);

            if (inverted) tooltip.add(I18n.format("tile.gregtech_lamp.tooltip.inverted"));
            if (!bloomEnabled) tooltip.add(I18n.format("tile.gregtech_lamp.tooltip.no_bloom"));
            if (!lightEnabled) tooltip.add(I18n.format("tile.gregtech_lamp.tooltip.no_light"));
        }
        return tooltip;
    }
}
