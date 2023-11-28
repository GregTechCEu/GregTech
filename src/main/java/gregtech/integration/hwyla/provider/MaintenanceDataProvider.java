package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.metatileentity.multiblock.IMaintenance;
import gregtech.api.unification.material.Materials;
import gregtech.common.items.ToolItems;
import gregtech.integration.hwyla.HWYLAModule;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MaintenanceDataProvider extends CapabilityDataProvider<IMaintenance> {

    public static final MaintenanceDataProvider INSTANCE = new MaintenanceDataProvider();

    private static final ItemStack WRENCH = ToolItems.WRENCH.get(Materials.Neutronium);
    private static final ItemStack SCREWDRIVER = ToolItems.SCREWDRIVER.get(Materials.Neutronium);
    private static final ItemStack SOFT_MALLET = ToolItems.SOFT_MALLET.get(Materials.Neutronium);
    private static final ItemStack HARD_HAMMER = ToolItems.HARD_HAMMER.get(Materials.Neutronium);
    private static final ItemStack WIRE_CUTTERS = ToolItems.WIRE_CUTTER.get(Materials.Neutronium);
    private static final ItemStack CROWBAR = ToolItems.CROWBAR.get(Materials.Neutronium);

    @Override
    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig(GTValues.MODID, "gregtech.maintenance");
    }

    @Override
    protected @NotNull Capability<IMaintenance> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_MAINTENANCE;
    }

    @Override
    protected NBTTagCompound getNBTData(IMaintenance capability, NBTTagCompound tag) {
        NBTTagCompound subTag = new NBTTagCompound();
        subTag.setBoolean("HasProblems", capability.hasMaintenanceProblems());
        subTag.setInteger("Problems", capability.getMaintenanceProblems());
        tag.setTag("gregtech.IMaintenance", subTag);
        return tag;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.maintenance") || accessor.getTileEntity() == null) {
            return tooltip;
        }

        if (accessor.getNBTData().hasKey("gregtech.IMaintenance")) {
            NBTTagCompound tag = accessor.getNBTData().getCompoundTag("gregtech.IMaintenance");

            IMultiblockController controller = accessor.getTileEntity()
                    .getCapability(GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER, null);
            if (controller == null || !controller.isStructureFormed()) {
                return tooltip;
            }

            if (tag.getBoolean("HasProblems")) {
                if (accessor.getPlayer().isSneaking()) {
                    int problems = tag.getInteger("Problems");
                    for (byte i = 0; i < 6; i++) {
                        if (((problems >> i) & 1) == 0) {
                            ItemStack stack = ItemStack.EMPTY;
                            String text = "";
                            switch (i) {
                                case 0 -> {
                                    stack = WRENCH;
                                    text = "gregtech.top.maintenance.wrench";
                                }
                                case 1 -> {
                                    stack = SCREWDRIVER;
                                    text = "gregtech.top.maintenance.screwdriver";
                                }
                                case 2 -> {
                                    stack = SOFT_MALLET;
                                    text = "gregtech.top.maintenance.soft_mallet";
                                }
                                case 3 -> {
                                    stack = HARD_HAMMER;
                                    text = "gregtech.top.maintenance.hard_hammer";
                                }
                                case 4 -> {
                                    stack = WIRE_CUTTERS;
                                    text = "gregtech.top.maintenance.wire_cutter";
                                }
                                case 5 -> {
                                    stack = CROWBAR;
                                    text = "gregtech.top.maintenance.crowbar";
                                }
                            }
                            tooltip.add(HWYLAModule.wailaStackWithName(stack, I18n.format(text)));
                        }
                    }
                } else {
                    tooltip.add(I18n.format("gregtech.top.maintenance_broken"));
                }
            } else {
                tooltip.add(I18n.format("gregtech.top.maintenance_fixed"));
            }
        }
        return tooltip;
    }
}
