package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.metatileentity.multiblock.IMaintenance;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;

public class MaintenanceInfoProvider extends CapabilityInfoProvider<IMaintenance> {

    private static final ItemStack WRENCH = MetaItems.WRENCH.getStackForm();
    private static final ItemStack SCREWDRIVER = MetaItems.SCREWDRIVER.getStackForm();
    private static final ItemStack SOFT_MALLET = MetaItems.SOFT_HAMMER.getStackForm();
    private static final ItemStack HARD_HAMMER = MetaItems.HARD_HAMMER.getStackForm();
    private static final ItemStack WIRE_CUTTERS = MetaItems.WIRE_CUTTER.getStackForm();
    private static final ItemStack CROWBAR = MetaItems.CROWBAR.getStackForm();

    @Override
    public String getID() {
        return GTValues.MODID + ":multiblock_maintenance_provider";
    }

    @Nonnull
    @Override
    protected Capability<IMaintenance> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_MAINTENANCE;
    }

    @Override
    protected void addProbeInfo(IMaintenance capability, IProbeInfo probeInfo, EntityPlayer player, TileEntity tileEntity, IProbeHitData data) {
        if (ConfigHolder.machines.enableMaintenance && capability.hasMaintenanceMechanics()) {
            if (tileEntity.hasCapability(GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER, null)) {
                //noinspection ConstantConditions
                if (tileEntity.getCapability(GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER, null).isStructureFormed()) {
                    if (capability.hasMaintenanceProblems()) {
                        if (player.isSneaking()) {
                            int problems = capability.getMaintenanceProblems();
                            for (byte i = 0; i < 6; i++) {
                                if (((problems >> i) & 1) == 0) {
                                    IProbeInfo horizontal = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                                    ItemStack stack = ItemStack.EMPTY;
                                    String text = "";
                                    switch (i) {
                                        case 0: {
                                            stack = WRENCH;
                                            text = "gregtech.top.maintenance.wrench";
                                            break;
                                        }
                                        case 1: {
                                            stack = SCREWDRIVER;
                                            text = "gregtech.top.maintenance.screwdriver";
                                            break;
                                        }
                                        case 2: {
                                            stack = SOFT_MALLET;
                                            text = "gregtech.top.maintenance.soft_mallet";
                                            break;
                                        }
                                        case 3: {
                                            stack = HARD_HAMMER;
                                            text = "gregtech.top.maintenance.hard_hammer";
                                            break;
                                        }
                                        case 4: {
                                            stack = WIRE_CUTTERS;
                                            text = "gregtech.top.maintenance.wire_cutter";
                                            break;
                                        }
                                        case 5: {
                                            stack = CROWBAR;
                                            text = "gregtech.top.maintenance.crowbar";
                                            break;
                                        }
                                    }
                                    horizontal.item(stack).text(TextFormatting.RED + IProbeInfo.STARTLOC + text + IProbeInfo.ENDLOC);
                                }
                            }
                        } else {
                            probeInfo.text(TextFormatting.RED + "{*gregtech.top.maintenance_broken*}");
                        }
                    } else {
                        probeInfo.text(TextStyleClass.OK + "{*gregtech.top.maintenance_fixed*}");
                    }
                }
            }
        }
    }
}
