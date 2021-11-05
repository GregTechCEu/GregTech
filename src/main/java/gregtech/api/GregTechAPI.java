package gregtech.api;

import com.google.common.collect.Lists;
import crafttweaker.annotations.ZenRegister;
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
import gregtech.api.util.IBlockOre;
import gregtech.common.items.MetaItems;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.ArrayList;
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

    @ZenClass("mods.gregtech.material.MaterialRegistry")
    @ZenRegister
    public static class MaterialRegistry extends GTControlledRegistry<String, Material> {

        private List<Material> DEFERRED_REGISTRY = new ArrayList<>();

        private MaterialRegistry() {
            super(Short.MAX_VALUE);
        }

        @Override
        public void register(int id, String key, Material value) {
            register(value); // id, key can be discarded since Material holds them
        }

        public void register(Material value) {
            DEFERRED_REGISTRY.add(value);
        }

        public void flush() {
            DEFERRED_REGISTRY.forEach(Material::verifyMaterial);
            DEFERRED_REGISTRY.forEach(m -> {
                m.postVerify();
                super.register(m.getId(), m.toString(), m);
            });
            DEFERRED_REGISTRY = new ArrayList<>();
        }

        @Override
        public void freeze() {
            if (!frozen) flush();
            super.freeze();
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
