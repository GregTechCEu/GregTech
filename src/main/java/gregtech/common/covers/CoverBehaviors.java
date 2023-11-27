package gregtech.common.covers;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.items.behavior.CoverItemBehavior;
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

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import static gregtech.api.GTValues.*;
import static gregtech.api.util.GTUtility.gregtechId;

public final class CoverBehaviors {

    private static int rollingId = 0;

    private CoverBehaviors() {}

    public static void init() {
        GTLog.logger.info("Registering cover behaviors...");
        registerBehavior(gregtechId("conveyor.lv"), MetaItems.CONVEYOR_MODULE_LV,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.LV, 8));
        registerBehavior(gregtechId("conveyor.mv"), MetaItems.CONVEYOR_MODULE_MV,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.MV, 32));
        registerBehavior(gregtechId("conveyor.hv"), MetaItems.CONVEYOR_MODULE_HV,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.HV, 64));
        registerBehavior(gregtechId("conveyor.ev"), MetaItems.CONVEYOR_MODULE_EV,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.EV, 3 * 64));
        registerBehavior(gregtechId("conveyor.iv"), MetaItems.CONVEYOR_MODULE_IV,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.IV, 8 * 64));
        registerBehavior(gregtechId("conveyor.luv"), MetaItems.CONVEYOR_MODULE_LuV,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.LuV, 16 * 64));
        registerBehavior(gregtechId("conveyor.zpm"), MetaItems.CONVEYOR_MODULE_ZPM,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.ZPM, 16 * 64));
        registerBehavior(gregtechId("conveyor.uv"), MetaItems.CONVEYOR_MODULE_UV,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.UV, 16 * 64));

        registerBehavior(gregtechId("robotic_arm.lv"), MetaItems.ROBOT_ARM_LV,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.LV, 8));
        registerBehavior(gregtechId("robotic_arm.mv"), MetaItems.ROBOT_ARM_MV,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.MV, 32));
        registerBehavior(gregtechId("robotic_arm.hv"), MetaItems.ROBOT_ARM_HV,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.HV, 64));
        registerBehavior(gregtechId("robotic_arm.ev"), MetaItems.ROBOT_ARM_EV,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.EV, 3 * 64));
        registerBehavior(gregtechId("robotic_arm.iv"), MetaItems.ROBOT_ARM_IV,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.IV, 8 * 64));
        registerBehavior(gregtechId("robotic_arm.luv"), MetaItems.ROBOT_ARM_LuV,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.LuV, 16 * 64));
        registerBehavior(gregtechId("robotic_arm.zpm"), MetaItems.ROBOT_ARM_ZPM,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.ZPM, 16 * 64));
        registerBehavior(gregtechId("robotic_arm.uv"), MetaItems.ROBOT_ARM_UV,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.UV, 16 * 64));

        registerBehavior(gregtechId("ore_dictionary_filter"), MetaItems.ORE_DICTIONARY_FILTER,
                (def, tile, side) -> new CoverItemFilter(def, tile, side, "cover.ore_dictionary_filter.title",
                        Textures.ORE_DICTIONARY_FILTER_OVERLAY, new OreDictionaryItemFilter()));
        registerBehavior(gregtechId("item_filter"), MetaItems.ITEM_FILTER, (def, tile, side) -> new CoverItemFilter(def,
                tile, side, "cover.item_filter.title", Textures.ITEM_FILTER_FILTER_OVERLAY, new SimpleItemFilter()));
        registerBehavior(gregtechId("fluid_filter"), MetaItems.FLUID_FILTER,
                (def, tile, side) -> new CoverFluidFilter(def, tile, side, "cover.fluid_filter.title",
                        Textures.FLUID_FILTER_OVERLAY, new SimpleFluidFilter()));
        registerBehavior(gregtechId("shutter"), MetaItems.COVER_SHUTTER, CoverShutter::new);

        registerBehavior(gregtechId("solar_panel.basic"), MetaItems.COVER_SOLAR_PANEL,
                (def, tile, side) -> new CoverSolarPanel(def, tile, side, 1));
        registerBehavior(gregtechId("solar_panel.ulv"), MetaItems.COVER_SOLAR_PANEL_ULV,
                (def, tile, side) -> new CoverSolarPanel(def, tile, side, V[ULV]));
        registerBehavior(gregtechId("solar_panel.lv"), MetaItems.COVER_SOLAR_PANEL_LV,
                (def, tile, side) -> new CoverSolarPanel(def, tile, side, V[LV]));
        registerBehavior(gregtechId("solar_panel.mv"), MetaItems.COVER_SOLAR_PANEL_MV,
                (def, tile, side) -> new CoverSolarPanel(def, tile, side, V[MV]));
        registerBehavior(gregtechId("solar_panel.hv"), MetaItems.COVER_SOLAR_PANEL_HV,
                (def, tile, side) -> new CoverSolarPanel(def, tile, side, V[HV]));
        registerBehavior(gregtechId("solar_panel.ev"), MetaItems.COVER_SOLAR_PANEL_EV,
                (def, tile, side) -> new CoverSolarPanel(def, tile, side, V[EV]));
        registerBehavior(gregtechId("solar_panel.iv"), MetaItems.COVER_SOLAR_PANEL_IV,
                (def, tile, side) -> new CoverSolarPanel(def, tile, side, V[IV]));
        registerBehavior(gregtechId("solar_panel.luv"), MetaItems.COVER_SOLAR_PANEL_LUV,
                (def, tile, side) -> new CoverSolarPanel(def, tile, side, V[LuV]));
        registerBehavior(gregtechId("solar_panel.zpm"), MetaItems.COVER_SOLAR_PANEL_ZPM,
                (def, tile, side) -> new CoverSolarPanel(def, tile, side, V[ZPM]));
        registerBehavior(gregtechId("solar_panel.uv"), MetaItems.COVER_SOLAR_PANEL_UV,
                (def, tile, side) -> new CoverSolarPanel(def, tile, side, V[UV]));

        registerBehavior(gregtechId("machine_controller"), MetaItems.COVER_MACHINE_CONTROLLER,
                CoverMachineController::new);
        registerBehavior(gregtechId("smart_filter"), MetaItems.SMART_FILTER,
                (def, tile, side) -> new CoverItemFilter(def, tile, side, "cover.smart_item_filter.title",
                        Textures.SMART_FILTER_FILTER_OVERLAY, new SmartItemFilter()));
        registerBehavior(gregtechId("facade"), MetaItems.COVER_FACADE, CoverFacade::new);

        registerBehavior(gregtechId("screen"), MetaItems.COVER_SCREEN, CoverScreen::new);
        registerBehavior(gregtechId("energy_detector"), MetaItems.COVER_ENERGY_DETECTOR, CoverDetectorEnergy::new);
        registerBehavior(gregtechId("energy_detector_advanced"), MetaItems.COVER_ENERGY_DETECTOR_ADVANCED,
                CoverDetectorEnergyAdvanced::new);
        registerBehavior(gregtechId("fluid_detector"), MetaItems.COVER_FLUID_DETECTOR, CoverDetectorFluid::new);
        registerBehavior(gregtechId("fluid_detector_advanced"), MetaItems.COVER_FLUID_DETECTOR_ADVANCED,
                CoverDetectorFluidAdvanced::new);
        registerBehavior(gregtechId("item_detector"), MetaItems.COVER_ITEM_DETECTOR, CoverDetectorItem::new);
        registerBehavior(gregtechId("item_detector_advanced"), MetaItems.COVER_ITEM_DETECTOR_ADVANCED,
                CoverDetectorItemAdvanced::new);
        registerBehavior(gregtechId("activity_detector"), MetaItems.COVER_ACTIVITY_DETECTOR,
                CoverDetectorActivity::new);
        registerBehavior(gregtechId("activity_detector_advanced"), MetaItems.COVER_ACTIVITY_DETECTOR_ADVANCED,
                CoverDetectorActivityAdvanced::new);
        registerBehavior(gregtechId("maintenance_detector"), MetaItems.COVER_MAINTENANCE_DETECTOR,
                CoverDetectorMaintenance::new);
        registerCover(gregtechId("crafting_table"), ItemStack.EMPTY, CoverCraftingTable::new);
        registerBehavior(gregtechId("infinite_water"), MetaItems.COVER_INFINITE_WATER, CoverInfiniteWater::new);
        registerBehavior(gregtechId("ender_fluid_link"), MetaItems.COVER_ENDER_FLUID_LINK, CoverEnderFluidLink::new);
        registerBehavior(gregtechId("cover.digital"), MetaItems.COVER_DIGITAL_INTERFACE, CoverDigitalInterface::new);

        // Custom cover behaviour
        MetaItems.COVER_DIGITAL_INTERFACE_WIRELESS.addComponents(
                new CoverDigitalInterfaceWirelessPlaceBehaviour(registerCover(gregtechId("cover.digital.wireless"),
                        MetaItems.COVER_DIGITAL_INTERFACE_WIRELESS.getStackForm(),
                        CoverDigitalInterfaceWireless::new)));

        registerBehavior(gregtechId("pump.lv"), MetaItems.ELECTRIC_PUMP_LV,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.LV, 1280));
        registerBehavior(gregtechId("pump.mv"), MetaItems.ELECTRIC_PUMP_MV,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.MV, 1280 * 4));
        registerBehavior(gregtechId("pump.hv"), MetaItems.ELECTRIC_PUMP_HV,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.HV, 1280 * 16));
        registerBehavior(gregtechId("pump.ev"), MetaItems.ELECTRIC_PUMP_EV,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.EV, 1280 * 64));
        registerBehavior(gregtechId("pump.iv"), MetaItems.ELECTRIC_PUMP_IV,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.IV, 1280 * 64 * 4));
        registerBehavior(gregtechId("pump.luv"), MetaItems.ELECTRIC_PUMP_LuV,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.LuV, 1280 * 64 * 16));
        registerBehavior(gregtechId("pump.zpm"), MetaItems.ELECTRIC_PUMP_ZPM,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.ZPM, 1280 * 64 * 64));
        registerBehavior(gregtechId("pump.uv"), MetaItems.ELECTRIC_PUMP_UV,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.UV, 1280 * 64 * 64 * 4));

        registerBehavior(gregtechId("fluid.regulator.lv"), MetaItems.FLUID_REGULATOR_LV,
                (def, tile, side) -> new CoverFluidRegulator(def, tile, side, GTValues.LV, 1280));
        registerBehavior(gregtechId("fluid.regulator.mv"), MetaItems.FLUID_REGULATOR_MV,
                (def, tile, side) -> new CoverFluidRegulator(def, tile, side, GTValues.MV, 1280 * 4));
        registerBehavior(gregtechId("fluid.regulator.hv"), MetaItems.FLUID_REGULATOR_HV,
                (def, tile, side) -> new CoverFluidRegulator(def, tile, side, GTValues.HV, 1280 * 16));
        registerBehavior(gregtechId("fluid.regulator.ev"), MetaItems.FLUID_REGULATOR_EV,
                (def, tile, side) -> new CoverFluidRegulator(def, tile, side, GTValues.EV, 1280 * 64));
        registerBehavior(gregtechId("fluid.regulator.iv"), MetaItems.FLUID_REGULATOR_IV,
                (def, tile, side) -> new CoverFluidRegulator(def, tile, side, GTValues.IV, 1280 * 64 * 4));
        registerBehavior(gregtechId("fluid.regulator.luv"), MetaItems.FLUID_REGULATOR_LUV,
                (def, tile, side) -> new CoverFluidRegulator(def, tile, side, GTValues.LuV, 1280 * 64 * 16));
        registerBehavior(gregtechId("fluid.regulator.zpm"), MetaItems.FLUID_REGULATOR_ZPM,
                (def, tile, side) -> new CoverFluidRegulator(def, tile, side, GTValues.ZPM, 1280 * 64 * 64));
        registerBehavior(gregtechId("fluid.regulator.uv"), MetaItems.FLUID_REGULATOR_UV,
                (def, tile, side) -> new CoverFluidRegulator(def, tile, side, GTValues.UV, 1280 * 64 * 64 * 4));

        // UHV+
        registerBehavior(gregtechId("conveyor.uhv"), MetaItems.CONVEYOR_MODULE_UHV,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.UHV, 16 * 64));
        registerBehavior(gregtechId("conveyor.uev"), MetaItems.CONVEYOR_MODULE_UEV,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.UEV, 16 * 64));
        registerBehavior(gregtechId("conveyor.uiv"), MetaItems.CONVEYOR_MODULE_UIV,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.UIV, 16 * 64));
        registerBehavior(gregtechId("conveyor.uxv"), MetaItems.CONVEYOR_MODULE_UXV,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.UXV, 16 * 64));
        registerBehavior(gregtechId("conveyor.opv"), MetaItems.CONVEYOR_MODULE_OpV,
                (def, tile, side) -> new CoverConveyor(def, tile, side, GTValues.OpV, 16 * 64));

        registerBehavior(gregtechId("robotic_arm.uhv"), MetaItems.ROBOT_ARM_UHV,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.UHV, 16 * 64));
        registerBehavior(gregtechId("robotic_arm.uev"), MetaItems.ROBOT_ARM_UEV,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.UEV, 16 * 64));
        registerBehavior(gregtechId("robotic_arm.uiv"), MetaItems.ROBOT_ARM_UIV,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.UIV, 16 * 64));
        registerBehavior(gregtechId("robotic_arm.uxv"), MetaItems.ROBOT_ARM_UXV,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.UXV, 16 * 64));
        registerBehavior(gregtechId("robotic_arm.opv"), MetaItems.ROBOT_ARM_OpV,
                (def, tile, side) -> new CoverRoboticArm(def, tile, side, GTValues.OpV, 16 * 64));

        registerBehavior(gregtechId("pump.uhv"), MetaItems.ELECTRIC_PUMP_UHV,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.UHV, 1280 * 64 * 64 * 4));
        registerBehavior(gregtechId("pump.uev"), MetaItems.ELECTRIC_PUMP_UEV,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.UEV, 1280 * 64 * 64 * 4));
        registerBehavior(gregtechId("pump.uiv"), MetaItems.ELECTRIC_PUMP_UIV,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.UIV, 1280 * 64 * 64 * 4));
        registerBehavior(gregtechId("pump.uxv"), MetaItems.ELECTRIC_PUMP_UXV,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.UXV, 1280 * 64 * 64 * 4));
        registerBehavior(gregtechId("pump.opv"), MetaItems.ELECTRIC_PUMP_OpV,
                (def, tile, side) -> new CoverPump(def, tile, side, GTValues.OpV, 1280 * 64 * 64 * 4));

        registerBehavior(gregtechId("fluid_voiding"), MetaItems.COVER_FLUID_VOIDING, CoverFluidVoiding::new);
        registerBehavior(gregtechId("fluid_voiding.advanced"), MetaItems.COVER_FLUID_VOIDING_ADVANCED,
                CoverFluidVoidingAdvanced::new);
        registerBehavior(gregtechId("item_voiding"), MetaItems.COVER_ITEM_VOIDING, CoverItemVoiding::new);
        registerBehavior(gregtechId("item_voiding.advanced"), MetaItems.COVER_ITEM_VOIDING_ADVANCED,
                CoverItemVoidingAdvanced::new);
        registerBehavior(gregtechId("storage"), MetaItems.COVER_STORAGE, CoverStorage::new);
    }

    /**
     * Register a cover behavior
     *
     * @param coverId         the id of the cover
     * @param placerItem      the item which places the cover
     * @param behaviorCreator a function creating the cover behavior
     */
    public static void registerBehavior(@NotNull ResourceLocation coverId, @NotNull MetaValueItem placerItem,
                                        @NotNull CoverDefinition.CoverCreator behaviorCreator) {
        placerItem.addComponents(
                new CoverItemBehavior(registerCover(coverId, placerItem.getStackForm(), behaviorCreator)));
    }

    /**
     * Register a cover
     *
     * @param coverId         the id of the cover
     * @param itemStack       the item which places the cover
     * @param behaviorCreator a function creating the cover behavior
     * @return the registered cover definition
     */
    public static @NotNull CoverDefinition registerCover(@NotNull ResourceLocation coverId,
                                                         @NotNull ItemStack itemStack,
                                                         @NotNull CoverDefinition.CoverCreator behaviorCreator) {
        CoverDefinition coverDefinition = new CoverDefinition(coverId, behaviorCreator, itemStack);
        GregTechAPI.COVER_REGISTRY.register(rollingId++, coverId, coverDefinition);
        return coverDefinition;
    }
}
