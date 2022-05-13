package gregtech.api;

import com.google.common.collect.Lists;
import crafttweaker.annotations.ZenRegister;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.gui.UIFactory;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.util.BaseCreativeTab;
import gregtech.api.util.GTControlledRegistry;
import gregtech.api.util.GTLog;
import gregtech.api.util.IBlockOre;
import gregtech.common.items.MetaItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GregTechAPI {

    public static final GTControlledRegistry<ResourceLocation, MetaTileEntity> MTE_REGISTRY = new GTControlledRegistry<>(Short.MAX_VALUE);
    public static final GTControlledRegistry<ResourceLocation, UIFactory> UI_FACTORY_REGISTRY = new GTControlledRegistry<>(Short.MAX_VALUE);
    public static final GTControlledRegistry<ResourceLocation, CoverDefinition> COVER_REGISTRY = new GTControlledRegistry<>(Integer.MAX_VALUE);
    public static final MaterialRegistry MATERIAL_REGISTRY = new MaterialRegistry();

    public static BlockMachine MACHINE;
    public static final Map<Material, Map<StoneType, IBlockOre>> oreBlockTable = new HashMap<>();
    public static final Object2ObjectOpenHashMap<IBlockState, IHeatingCoilBlockStats> HEATING_COILS = new Object2ObjectOpenHashMap<>();

    public static final BaseCreativeTab TAB_GREGTECH =
            new BaseCreativeTab(GTValues.MODID + ".main", () -> MetaItems.BATTERY_HULL_HV.getStackForm(), true);
    public static final BaseCreativeTab TAB_GREGTECH_MATERIALS =
            new BaseCreativeTab(GTValues.MODID + ".materials", () -> OreDictUnifier.get(OrePrefix.ingot, Materials.Aluminium), true);
    public static final BaseCreativeTab TAB_GREGTECH_ORES =
            new BaseCreativeTab(GTValues.MODID + ".ores", () -> MetaItems.DRILL_MV.getStackForm(), true);

    public static class RegisterEvent<V> extends GenericEvent<V> {

        private final GTControlledRegistry<ResourceLocation, V> registry;

        public RegisterEvent(GTControlledRegistry<ResourceLocation, V> registry, Class<V> clazz) {
            super(clazz);
            this.registry = registry;
        }

        public void register(int id, ResourceLocation key, V value) {
            if (registry != null) registry.register(id, key, value);
        }

        public void register(int id, String key, V value) {
            if (registry != null) registry.register(id, new ResourceLocation(Loader.instance().activeModContainer().getModId(), key), value);
        }
    }

    public static class MaterialEvent extends GenericEvent<Material> {

        public MaterialEvent() {
            super(Material.class);
        }
    }

    public static class PostMaterialEvent extends GenericEvent<Material> {

        public PostMaterialEvent() {
            super(Material.class);
        }
    }

    @ZenClass("mods.gregtech.material.MaterialRegistry")
    @ZenRegister
    public static class MaterialRegistry extends GTControlledRegistry<String, Material> {

        private boolean isRegistryClosed = false;

        private MaterialRegistry() {
            super(Short.MAX_VALUE);
        }

        public void register(Material value) {
            register(value.getId(), value.toString(), value);
        }

        @Override
        public void register(int id, String key, Material value) {
            if (isRegistryClosed) {
                GTLog.logger.error("Materials cannot be registered in the PostMaterialEvent! Must be added in the MaterialEvent. Skipping material {}...", key);
                return;
            }
            super.register(id, key, value);
        }

        public void closeRegistry() {
            this.isRegistryClosed = true;
        }

        @ZenMethod
        @Nullable
        public static Material get(String name) {
            return MATERIAL_REGISTRY.getObject(name);
        }

        @ZenMethod
        public static List<Material> getAllMaterials() {
            return Lists.newArrayList(MATERIAL_REGISTRY);
        }
    }
}
