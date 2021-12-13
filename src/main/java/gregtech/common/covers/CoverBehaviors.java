package gregtech.common.covers;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.ICoverable;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.client.renderer.texture.Textures;
import gregtech.api.util.GTLog;
import gregtech.common.covers.detector.CoverDetectorEnergy;
import gregtech.common.covers.detector.CoverDetectorFluid;
import gregtech.common.covers.detector.CoverDetectorItem;
import gregtech.common.covers.filter.OreDictionaryItemFilter;
import gregtech.common.covers.filter.SimpleFluidFilter;
import gregtech.common.covers.filter.SimpleItemFilter;
import gregtech.common.covers.filter.SmartItemFilter;
import gregtech.common.items.MetaItems;
import gregtech.common.items.behaviors.CoverDigitalInterfaceWirelessPlaceBehaviour;
import gregtech.common.items.behaviors.CoverPlaceBehavior;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.util.Locale;
import java.util.function.BiFunction;

public class CoverBehaviors {

    private static final int COVERS_PER_ITEM = 8;

    public static void init() {
        GTLog.logger.info("Registering cover behaviors...");
        registerBehavior(0, new ResourceLocation(GTValues.MODID, "conveyor.lv"), MetaItems.CONVEYOR_MODULE_LV, (tile, side) -> new CoverConveyor(tile, side, GTValues.LV, 8));
        registerBehavior(1, new ResourceLocation(GTValues.MODID, "conveyor.mv"), MetaItems.CONVEYOR_MODULE_MV, (tile, side) -> new CoverConveyor(tile, side, GTValues.MV, 32));
        registerBehavior(2, new ResourceLocation(GTValues.MODID, "conveyor.hv"), MetaItems.CONVEYOR_MODULE_HV, (tile, side) -> new CoverConveyor(tile, side, GTValues.HV, 64));
        registerBehavior(3, new ResourceLocation(GTValues.MODID, "conveyor.ev"), MetaItems.CONVEYOR_MODULE_EV, (tile, side) -> new CoverConveyor(tile, side, GTValues.EV, 3 * 64));
        registerBehavior(4, new ResourceLocation(GTValues.MODID, "conveyor.iv"), MetaItems.CONVEYOR_MODULE_IV, (tile, side) -> new CoverConveyor(tile, side, GTValues.IV, 8 * 64));
        registerBehavior(5, new ResourceLocation(GTValues.MODID, "conveyor.luv"), MetaItems.CONVEYOR_MODULE_LUV, (tile, side) -> new CoverConveyor(tile, side, GTValues.LuV, 16 * 64));
        registerBehavior(6, new ResourceLocation(GTValues.MODID, "conveyor.zpm"), MetaItems.CONVEYOR_MODULE_ZPM, (tile, side) -> new CoverConveyor(tile, side, GTValues.ZPM, 16 * 64));
        registerBehavior(7, new ResourceLocation(GTValues.MODID, "conveyor.uv"), MetaItems.CONVEYOR_MODULE_UV, (tile, side) -> new CoverConveyor(tile, side, GTValues.UV, 16 * 64));

        registerBehavior(10, new ResourceLocation(GTValues.MODID, "robotic_arm.lv"), MetaItems.ROBOT_ARM_LV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.LV, 8));
        registerBehavior(11, new ResourceLocation(GTValues.MODID, "robotic_arm.mv"), MetaItems.ROBOT_ARM_MV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.MV, 32));
        registerBehavior(12, new ResourceLocation(GTValues.MODID, "robotic_arm.hv"), MetaItems.ROBOT_ARM_HV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.HV, 64));
        registerBehavior(13, new ResourceLocation(GTValues.MODID, "robotic_arm.ev"), MetaItems.ROBOT_ARM_EV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.EV, 3 * 64));
        registerBehavior(14, new ResourceLocation(GTValues.MODID, "robotic_arm.iv"), MetaItems.ROBOT_ARM_IV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.IV, 8 * 64));
        registerBehavior(15, new ResourceLocation(GTValues.MODID, "robotic_arm.luv"), MetaItems.ROBOT_ARM_LUV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.LuV, 16 * 64));
        registerBehavior(16, new ResourceLocation(GTValues.MODID, "robotic_arm.zpm"), MetaItems.ROBOT_ARM_ZPM, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.ZPM, 16 * 64));
        registerBehavior(17, new ResourceLocation(GTValues.MODID, "robotic_arm.uv"), MetaItems.ROBOT_ARM_UV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.UV, 16 * 64));

        registerBehavior(30, new ResourceLocation(GTValues.MODID, "ore_dictionary_filter"), MetaItems.ORE_DICTIONARY_FILTER, (tile, side) -> new CoverItemFilter(tile, side, "cover.ore_dictionary_filter.title", Textures.ORE_DICTIONARY_FILTER_OVERLAY, new OreDictionaryItemFilter()));
        registerBehavior(31, new ResourceLocation(GTValues.MODID, "item_filter"), MetaItems.ITEM_FILTER, (tile, side) -> new CoverItemFilter(tile, side, "cover.item_filter.title", Textures.ITEM_FILTER_FILTER_OVERLAY, new SimpleItemFilter()));
        registerBehavior(32, new ResourceLocation(GTValues.MODID, "fluid_filter"), MetaItems.FLUID_FILTER, (tile, side) -> new CoverFluidFilter(tile, side, "cover.fluid_filter.title", Textures.FLUID_FILTER_OVERLAY, new SimpleFluidFilter()));
        registerBehavior(33, new ResourceLocation(GTValues.MODID, "shutter"), MetaItems.COVER_SHUTTER, CoverShutter::new);

        registerBehavior(34, new ResourceLocation(GTValues.MODID, "solar_panel.basic"), MetaItems.COVER_SOLAR_PANEL, (tile, side) -> new CoverSolarPanel(tile, side, 1));
        registerBehavior(35, new ResourceLocation(GTValues.MODID, "solar_panel.ulv"), MetaItems.COVER_SOLAR_PANEL_ULV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[0]));
        registerBehavior(36, new ResourceLocation(GTValues.MODID, "solar_panel.lv"), MetaItems.COVER_SOLAR_PANEL_LV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[1]));
        registerBehavior(37, new ResourceLocation(GTValues.MODID, "solar_panel.mv"), MetaItems.COVER_SOLAR_PANEL_MV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[2]));
        registerBehavior(38, new ResourceLocation(GTValues.MODID, "solar_panel.hv"), MetaItems.COVER_SOLAR_PANEL_HV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[3]));
        registerBehavior(39, new ResourceLocation(GTValues.MODID, "solar_panel.ev"), MetaItems.COVER_SOLAR_PANEL_EV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[4]));
        registerBehavior(40, new ResourceLocation(GTValues.MODID, "solar_panel.iv"), MetaItems.COVER_SOLAR_PANEL_IV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[5]));
        registerBehavior(41, new ResourceLocation(GTValues.MODID, "solar_panel.luv"), MetaItems.COVER_SOLAR_PANEL_LUV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[6]));
        registerBehavior(42, new ResourceLocation(GTValues.MODID, "solar_panel.zpm"), MetaItems.COVER_SOLAR_PANEL_ZPM, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[7]));
        registerBehavior(43, new ResourceLocation(GTValues.MODID, "solar_panel.uv"), MetaItems.COVER_SOLAR_PANEL_UV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[8]));

        registerBehavior(45, new ResourceLocation(GTValues.MODID, "machine_controller"), MetaItems.COVER_MACHINE_CONTROLLER, CoverMachineController::new);
        registerBehavior(46, new ResourceLocation(GTValues.MODID, "smart_filter"), MetaItems.SMART_FILTER, (tile, side) -> new CoverItemFilter(tile, side, "cover.smart_item_filter.title", Textures.SMART_FILTER_FILTER_OVERLAY, new SmartItemFilter()));
        registerBehavior(47, new ResourceLocation(GTValues.MODID, "facade"), MetaItems.COVER_FACADE, CoverFacade::new);

        registerBehavior(49, new ResourceLocation(GTValues.MODID, "screen"), MetaItems.COVER_SCREEN, CoverScreen::new);
        registerBehavior(50, new ResourceLocation(GTValues.MODID, "energy_detector"), MetaItems.COVER_ENERGY_DETECTOR, CoverDetectorEnergy::new);
        registerBehavior(51, new ResourceLocation(GTValues.MODID, "fluid_detector"), MetaItems.COVER_FLUID_DETECTOR, CoverDetectorFluid::new);
        registerBehavior(52, new ResourceLocation(GTValues.MODID, "item_detector"), MetaItems.COVER_ITEM_DETECTOR, CoverDetectorItem::new);
        registerBehavior(53, new ResourceLocation(GTValues.MODID, "crafting_table"), MetaItems.COVER_CRAFTING, CoverCraftingTable::new);
        registerBehavior(54, new ResourceLocation(GTValues.MODID, "infinite_water"), MetaItems.COVER_INFINITE_WATER, CoverInfiniteWater::new);
        registerBehavior(55, new ResourceLocation(GTValues.MODID, "ender_fluid_link"), MetaItems.COVER_ENDER_FLUID_LINK, CoverEnderFluidLink::new);
        registerBehavior(56, new ResourceLocation(GTValues.MODID, "cover.digital"), MetaItems.COVER_DIGITAL_INTERFACE, CoverDigitalInterface::new);

        // Custom cover behaviour
        MetaItems.COVER_DIGITAL_INTERFACE_WIRELESS.addComponents(new CoverDigitalInterfaceWirelessPlaceBehaviour(registerCover(57, new ResourceLocation(GTValues.MODID, "cover.digital.wireless"), MetaItems.COVER_DIGITAL_INTERFACE_WIRELESS, CoverDigitalInterfaceWireless::new)));


        for (int i = 0; i < COVERS_PER_ITEM; i++) {
            int throughput = (int) (Math.pow(4, i) * 1280);
            final int coverTier = i + 1;
            String tierShortName = GTValues.VN[coverTier].toLowerCase(Locale.ROOT);

            registerBehavior(20 + i, new ResourceLocation(GTValues.MODID, "pump." + tierShortName), MetaItems.PUMPS[i], (tile, side) -> new CoverPump(tile, side, coverTier, throughput));
            registerBehavior(60 + i, new ResourceLocation(GTValues.MODID, "fluid.regulator." + tierShortName), MetaItems.FLUID_REGULATORS[i], (tile, side) -> new CoverFluidRegulator(tile, side, coverTier, throughput));
        }



    }

    public static void registerBehavior(int coverNetworkId, ResourceLocation coverId, MetaValueItem placerItem, BiFunction<ICoverable, EnumFacing, CoverBehavior> behaviorCreator) {
        placerItem.addComponents(new CoverPlaceBehavior(registerCover(coverNetworkId, coverId, placerItem, behaviorCreator)));
    }

    public static CoverDefinition registerCover(int coverNetworkId, ResourceLocation coverId, MetaValueItem itemStack, BiFunction<ICoverable, EnumFacing, CoverBehavior> behaviorCreator) {
        CoverDefinition coverDefinition = new CoverDefinition(coverId, behaviorCreator, itemStack.getStackForm());
        GregTechAPI.COVER_REGISTRY.register(coverNetworkId, coverId, coverDefinition);
        return coverDefinition;
    }
}
