package gregtech.common.covers;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.ICoverable;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.covers.detector.*;
import gregtech.common.covers.filter.OreDictionaryItemFilter;
import gregtech.common.covers.filter.SimpleFluidFilter;
import gregtech.common.covers.filter.SimpleItemFilter;
import gregtech.common.covers.filter.SmartItemFilter;
import gregtech.common.items.MetaItems;
import gregtech.common.items.behaviors.CoverDigitalInterfaceWirelessPlaceBehaviour;
import gregtech.common.items.behaviors.CoverPlaceBehavior;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

import static gregtech.api.util.GTUtility.gregtechId;

public class CoverBehaviors {

    private static int rollingId = 0;

    public static void init() {
        GTLog.logger.info("Registering cover behaviors...");
        registerBehavior(gregtechId("conveyor.lv"), MetaItems.CONVEYOR_MODULE_LV, (tile, side) -> new CoverConveyor(tile, side, GTValues.LV, 8));
        registerBehavior(gregtechId("conveyor.mv"), MetaItems.CONVEYOR_MODULE_MV, (tile, side) -> new CoverConveyor(tile, side, GTValues.MV, 32));
        registerBehavior(gregtechId("conveyor.hv"), MetaItems.CONVEYOR_MODULE_HV, (tile, side) -> new CoverConveyor(tile, side, GTValues.HV, 64));
        registerBehavior(gregtechId("conveyor.ev"), MetaItems.CONVEYOR_MODULE_EV, (tile, side) -> new CoverConveyor(tile, side, GTValues.EV, 3 * 64));
        registerBehavior(gregtechId("conveyor.iv"), MetaItems.CONVEYOR_MODULE_IV, (tile, side) -> new CoverConveyor(tile, side, GTValues.IV, 8 * 64));
        registerBehavior(gregtechId("conveyor.luv"), MetaItems.CONVEYOR_MODULE_LuV, (tile, side) -> new CoverConveyor(tile, side, GTValues.LuV, 16 * 64));
        registerBehavior(gregtechId("conveyor.zpm"), MetaItems.CONVEYOR_MODULE_ZPM, (tile, side) -> new CoverConveyor(tile, side, GTValues.ZPM, 16 * 64));
        registerBehavior(gregtechId("conveyor.uv"), MetaItems.CONVEYOR_MODULE_UV, (tile, side) -> new CoverConveyor(tile, side, GTValues.UV, 16 * 64));

        registerBehavior(gregtechId("robotic_arm.lv"), MetaItems.ROBOT_ARM_LV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.LV, 8));
        registerBehavior(gregtechId("robotic_arm.mv"), MetaItems.ROBOT_ARM_MV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.MV, 32));
        registerBehavior(gregtechId("robotic_arm.hv"), MetaItems.ROBOT_ARM_HV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.HV, 64));
        registerBehavior(gregtechId("robotic_arm.ev"), MetaItems.ROBOT_ARM_EV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.EV, 3 * 64));
        registerBehavior(gregtechId("robotic_arm.iv"), MetaItems.ROBOT_ARM_IV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.IV, 8 * 64));
        registerBehavior(gregtechId("robotic_arm.luv"), MetaItems.ROBOT_ARM_LuV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.LuV, 16 * 64));
        registerBehavior(gregtechId("robotic_arm.zpm"), MetaItems.ROBOT_ARM_ZPM, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.ZPM, 16 * 64));
        registerBehavior(gregtechId("robotic_arm.uv"), MetaItems.ROBOT_ARM_UV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.UV, 16 * 64));

        registerBehavior(gregtechId("ore_dictionary_filter"), MetaItems.ORE_DICTIONARY_FILTER, (tile, side) -> new CoverItemFilter(tile, side, "cover.ore_dictionary_filter.title", Textures.ORE_DICTIONARY_FILTER_OVERLAY, new OreDictionaryItemFilter()));
        registerBehavior(gregtechId("item_filter"), MetaItems.ITEM_FILTER, (tile, side) -> new CoverItemFilter(tile, side, "cover.item_filter.title", Textures.ITEM_FILTER_FILTER_OVERLAY, new SimpleItemFilter()));
        registerBehavior(gregtechId("fluid_filter"), MetaItems.FLUID_FILTER, (tile, side) -> new CoverFluidFilter(tile, side, "cover.fluid_filter.title", Textures.FLUID_FILTER_OVERLAY, new SimpleFluidFilter()));
        registerBehavior(gregtechId("shutter"), MetaItems.COVER_SHUTTER, CoverShutter::new);

        registerBehavior(gregtechId("solar_panel.basic"), MetaItems.COVER_SOLAR_PANEL, (tile, side) -> new CoverSolarPanel(tile, side, 1));
        registerBehavior(gregtechId("solar_panel.ulv"), MetaItems.COVER_SOLAR_PANEL_ULV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[0]));
        registerBehavior(gregtechId("solar_panel.lv"), MetaItems.COVER_SOLAR_PANEL_LV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[1]));
        registerBehavior(gregtechId("solar_panel.mv"), MetaItems.COVER_SOLAR_PANEL_MV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[2]));
        registerBehavior(gregtechId("solar_panel.hv"), MetaItems.COVER_SOLAR_PANEL_HV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[3]));
        registerBehavior(gregtechId("solar_panel.ev"), MetaItems.COVER_SOLAR_PANEL_EV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[4]));
        registerBehavior(gregtechId("solar_panel.iv"), MetaItems.COVER_SOLAR_PANEL_IV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[5]));
        registerBehavior(gregtechId("solar_panel.luv"), MetaItems.COVER_SOLAR_PANEL_LUV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[6]));
        registerBehavior(gregtechId("solar_panel.zpm"), MetaItems.COVER_SOLAR_PANEL_ZPM, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[7]));
        registerBehavior(gregtechId("solar_panel.uv"), MetaItems.COVER_SOLAR_PANEL_UV, (tile, side) -> new CoverSolarPanel(tile, side, GTValues.V[8]));

        registerBehavior(gregtechId("machine_controller"), MetaItems.COVER_MACHINE_CONTROLLER, CoverMachineController::new);
        registerBehavior(gregtechId("smart_filter"), MetaItems.SMART_FILTER, (tile, side) -> new CoverItemFilter(tile, side, "cover.smart_item_filter.title", Textures.SMART_FILTER_FILTER_OVERLAY, new SmartItemFilter()));
        registerBehavior(gregtechId("facade"), MetaItems.COVER_FACADE, CoverFacade::new);

        registerBehavior(gregtechId("screen"), MetaItems.COVER_SCREEN, CoverScreen::new);
        registerBehavior(gregtechId("energy_detector"), MetaItems.COVER_ENERGY_DETECTOR, CoverDetectorEnergy::new);
        registerBehavior(gregtechId("energy_detector_advanced"), MetaItems.COVER_ENERGY_DETECTOR_ADVANCED, CoverDetectorEnergyAdvanced::new);
        registerBehavior(gregtechId("fluid_detector"), MetaItems.COVER_FLUID_DETECTOR, CoverDetectorFluid::new);
        registerBehavior(gregtechId("fluid_detector_advanced"), MetaItems.COVER_FLUID_DETECTOR_ADVANCED, CoverDetectorFluidAdvanced::new);
        registerBehavior(gregtechId("item_detector"), MetaItems.COVER_ITEM_DETECTOR, CoverDetectorItem::new);
        registerBehavior(gregtechId("item_detector_advanced"), MetaItems.COVER_ITEM_DETECTOR_ADVANCED, CoverDetectorItemAdvanced::new);
        registerBehavior(gregtechId("activity_detector"), MetaItems.COVER_ACTIVITY_DETECTOR, CoverDetectorActivity::new);
        registerBehavior(gregtechId("activity_detector_advanced"), MetaItems.COVER_ACTIVITY_DETECTOR_ADVANCED, CoverDetectorActivityAdvanced::new);
        registerBehavior(gregtechId("crafting_table"), MetaItems.COVER_CRAFTING, CoverCraftingTable::new);
        registerBehavior(gregtechId("infinite_water"), MetaItems.COVER_INFINITE_WATER, CoverInfiniteWater::new);
        registerBehavior(gregtechId("ender_fluid_link"), MetaItems.COVER_ENDER_FLUID_LINK, CoverEnderFluidLink::new);
        registerBehavior(gregtechId("cover.digital"), MetaItems.COVER_DIGITAL_INTERFACE, CoverDigitalInterface::new);

        // Custom cover behaviour
        MetaItems.COVER_DIGITAL_INTERFACE_WIRELESS.addComponents(new CoverDigitalInterfaceWirelessPlaceBehaviour(registerCover(gregtechId("cover.digital.wireless"), MetaItems.COVER_DIGITAL_INTERFACE_WIRELESS, CoverDigitalInterfaceWireless::new)));

        registerBehavior(gregtechId("pump.lv"), MetaItems.ELECTRIC_PUMP_LV, (tile, side) -> new CoverPump(tile, side, GTValues.LV, 1280));
        registerBehavior(gregtechId("pump.mv"), MetaItems.ELECTRIC_PUMP_MV, (tile, side) -> new CoverPump(tile, side, GTValues.MV, 1280 * 4));
        registerBehavior(gregtechId("pump.hv"), MetaItems.ELECTRIC_PUMP_HV, (tile, side) -> new CoverPump(tile, side, GTValues.HV, 1280 * 16));
        registerBehavior(gregtechId("pump.ev"), MetaItems.ELECTRIC_PUMP_EV, (tile, side) -> new CoverPump(tile, side, GTValues.EV, 1280 * 64));
        registerBehavior(gregtechId("pump.iv"), MetaItems.ELECTRIC_PUMP_IV, (tile, side) -> new CoverPump(tile, side, GTValues.IV, 1280 * 64 * 4));
        registerBehavior(gregtechId("pump.luv"), MetaItems.ELECTRIC_PUMP_LuV, (tile, side) -> new CoverPump(tile, side, GTValues.LuV, 1280 * 64 * 16));
        registerBehavior(gregtechId("pump.zpm"), MetaItems.ELECTRIC_PUMP_ZPM, (tile, side) -> new CoverPump(tile, side, GTValues.ZPM, 1280 * 64 * 64));
        registerBehavior(gregtechId("pump.uv"), MetaItems.ELECTRIC_PUMP_UV, (tile, side) -> new CoverPump(tile, side, GTValues.UV, 1280 * 64 * 64 * 4));

        registerBehavior(gregtechId("fluid.regulator.lv"), MetaItems.FLUID_REGULATOR_LV, (tile, side) -> new CoverFluidRegulator(tile, side, GTValues.LV, 1280));
        registerBehavior(gregtechId("fluid.regulator.mv"), MetaItems.FLUID_REGULATOR_MV, (tile, side) -> new CoverFluidRegulator(tile, side, GTValues.MV, 1280 * 4));
        registerBehavior(gregtechId("fluid.regulator.hv"), MetaItems.FLUID_REGULATOR_HV, (tile, side) -> new CoverFluidRegulator(tile, side, GTValues.HV, 1280 * 16));
        registerBehavior(gregtechId("fluid.regulator.ev"), MetaItems.FLUID_REGULATOR_EV, (tile, side) -> new CoverFluidRegulator(tile, side, GTValues.EV, 1280 * 64));
        registerBehavior(gregtechId("fluid.regulator.iv"), MetaItems.FLUID_REGULATOR_IV, (tile, side) -> new CoverFluidRegulator(tile, side, GTValues.IV, 1280 * 64 * 4));
        registerBehavior(gregtechId("fluid.regulator.luv"), MetaItems.FLUID_REGULATOR_LUV, (tile, side) -> new CoverFluidRegulator(tile, side, GTValues.LuV, 1280 * 64 * 16));
        registerBehavior(gregtechId("fluid.regulator.zpm"), MetaItems.FLUID_REGULATOR_ZPM, (tile, side) -> new CoverFluidRegulator(tile, side, GTValues.ZPM, 1280 * 64 * 64));
        registerBehavior(gregtechId("fluid.regulator.uv"), MetaItems.FLUID_REGULATOR_UV, (tile, side) -> new CoverFluidRegulator(tile, side, GTValues.UV, 1280 * 64 * 64 * 4));

        // UHV+
        registerBehavior(gregtechId("conveyor.uhv"), MetaItems.CONVEYOR_MODULE_UHV, (tile, side) -> new CoverConveyor(tile, side, GTValues.UHV, 16 * 64));
        registerBehavior(gregtechId("conveyor.uev"), MetaItems.CONVEYOR_MODULE_UEV, (tile, side) -> new CoverConveyor(tile, side, GTValues.UEV, 16 * 64));
        registerBehavior(gregtechId("conveyor.uiv"), MetaItems.CONVEYOR_MODULE_UIV, (tile, side) -> new CoverConveyor(tile, side, GTValues.UIV, 16 * 64));
        registerBehavior(gregtechId("conveyor.uxv"), MetaItems.CONVEYOR_MODULE_UXV, (tile, side) -> new CoverConveyor(tile, side, GTValues.UXV, 16 * 64));
        registerBehavior(gregtechId("conveyor.opv"), MetaItems.CONVEYOR_MODULE_OpV, (tile, side) -> new CoverConveyor(tile, side, GTValues.OpV, 16 * 64));

        registerBehavior(gregtechId("robotic_arm.uhv"), MetaItems.ROBOT_ARM_UHV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.UHV, 16 * 64));
        registerBehavior(gregtechId("robotic_arm.uev"), MetaItems.ROBOT_ARM_UEV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.UEV, 16 * 64));
        registerBehavior(gregtechId("robotic_arm.uiv"), MetaItems.ROBOT_ARM_UIV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.UIV, 16 * 64));
        registerBehavior(gregtechId("robotic_arm.uxv"), MetaItems.ROBOT_ARM_UXV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.UXV, 16 * 64));
        registerBehavior(gregtechId("robotic_arm.opv"), MetaItems.ROBOT_ARM_OpV, (tile, side) -> new CoverRoboticArm(tile, side, GTValues.OpV, 16 * 64));

        registerBehavior(gregtechId("pump.uhv"), MetaItems.ELECTRIC_PUMP_UHV, (tile, side) -> new CoverPump(tile, side, GTValues.UHV, 1280 * 64 * 64 * 4));
        registerBehavior(gregtechId("pump.uev"), MetaItems.ELECTRIC_PUMP_UEV, (tile, side) -> new CoverPump(tile, side, GTValues.UEV, 1280 * 64 * 64 * 4));
        registerBehavior(gregtechId("pump.uiv"), MetaItems.ELECTRIC_PUMP_UIV, (tile, side) -> new CoverPump(tile, side, GTValues.UIV, 1280 * 64 * 64 * 4));
        registerBehavior(gregtechId("pump.uxv"), MetaItems.ELECTRIC_PUMP_UXV, (tile, side) -> new CoverPump(tile, side, GTValues.UXV, 1280 * 64 * 64 * 4));
        registerBehavior(gregtechId("pump.opv"), MetaItems.ELECTRIC_PUMP_OpV, (tile, side) -> new CoverPump(tile, side, GTValues.OpV, 1280 * 64 * 64 * 4));

        registerBehavior(gregtechId("fluid_voiding"), MetaItems.COVER_FLUID_VOIDING, CoverFluidVoiding::new);
        registerBehavior(gregtechId("fluid_voiding.advanced"), MetaItems.COVER_FLUID_VOIDING_ADVANCED, CoverFluidVoidingAdvanced::new);
        registerBehavior(gregtechId("item_voiding"), MetaItems.COVER_ITEM_VOIDING, CoverItemVoiding::new);
        registerBehavior(gregtechId("item_voiding.advanced"), MetaItems.COVER_ITEM_VOIDING_ADVANCED, CoverItemVoidingAdvanced::new);
        registerBehavior(gregtechId("storage"), MetaItems.COVER_STORAGE, CoverStorage::new);
    }

    /**
     * @deprecated See {@link CoverBehaviors#registerBehavior(ResourceLocation, MetaItem.MetaValueItem, BiFunction)}
     *
     * </p> This method was deprecated in 2.6 and will be removed in 2.8
     */
    @Deprecated
    public static void registerBehavior(int coverNetworkId, @Nonnull ResourceLocation coverId, @Nonnull MetaValueItem placerItem,
                                        @Nonnull BiFunction<ICoverable, EnumFacing, CoverBehavior> behaviorCreator) {
        registerBehavior(coverId, placerItem, behaviorCreator);
    }

    /**
     * Register a cover behavior
     *
     * @param coverId         the id of the cover
     * @param placerItem      the item which places the cover
     * @param behaviorCreator a function creating the cover behavior
     */
    public static void registerBehavior(@Nonnull ResourceLocation coverId, @Nonnull MetaValueItem placerItem,
                                        @Nonnull BiFunction<ICoverable, EnumFacing, CoverBehavior> behaviorCreator) {
        placerItem.addComponents(new CoverPlaceBehavior(registerCover(coverId, placerItem, behaviorCreator)));
    }

    /**
     * @deprecated See {@link CoverBehaviors#registerCover(ResourceLocation, MetaItem.MetaValueItem, BiFunction)}
     *
     * </p> This method was deprecated in 2.6 and will be removed in 2.8
     */
    @Nonnull
    @Deprecated
    public static CoverDefinition registerCover(int coverNetworkId, @Nonnull ResourceLocation coverId, @Nonnull MetaValueItem itemStack,
                                                @Nonnull BiFunction<ICoverable, EnumFacing, CoverBehavior> behaviorCreator) {
        return registerCover(coverId, itemStack, behaviorCreator);
    }

    /**
     * Register a cover
     *
     * @param coverId         the id of the cover
     * @param itemStack       the item which places the cover
     * @param behaviorCreator a function creating the cover behavior
     * @return the registered cover definition
     */
    @Nonnull
    public static CoverDefinition registerCover(@Nonnull ResourceLocation coverId, @Nonnull MetaValueItem itemStack,
                                                @Nonnull BiFunction<ICoverable, EnumFacing, CoverBehavior> behaviorCreator) {
        CoverDefinition coverDefinition = new CoverDefinition(coverId, behaviorCreator, itemStack.getStackForm());
        GregTechAPI.COVER_REGISTRY.register(rollingId++, coverId, coverDefinition);
        return coverDefinition;
    }
}
