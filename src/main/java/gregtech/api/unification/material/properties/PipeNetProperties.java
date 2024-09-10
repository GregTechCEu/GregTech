package gregtech.api.unification.material.properties;

import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PipeNetProperties implements IMaterialProperty, IPipeNetNodeHandler {

    protected final Map<IPipeNetMaterialProperty.MaterialPropertyKey<?>, IPipeNetMaterialProperty> properties = new Object2ObjectRBTreeMap<>(
            Comparator.comparing(IPipeNetMaterialProperty.MaterialPropertyKey::getName));

    public void setProperty(IPipeNetMaterialProperty property) {
        this.properties.put(property.getKey(), property);
    }

    public boolean hasProperty(IPipeNetMaterialProperty.MaterialPropertyKey<?> key) {
        return this.properties.containsKey(key);
    }

    public Collection<IPipeNetMaterialProperty> getRegisteredProperties() {
        return properties.values();
    }

    public <T extends IPipeNetMaterialProperty> T getProperty(IPipeNetMaterialProperty.MaterialPropertyKey<T> key) {
        return key.cast(this.properties.get(key));
    }

    public void removeProperty(IPipeNetMaterialProperty.MaterialPropertyKey<?> key) {
        this.properties.remove(key);
    }

    public boolean generatesStructure(IPipeStructure structure) {
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.generatesStructure(structure)) return true;
        }
        return false;
    }

    @Override
    public @NotNull Collection<WorldPipeNetNode> getOrCreateFromNets(World world, BlockPos pos,
                                                                     IPipeStructure structure) {
        List<WorldPipeNetNode> list = new ObjectArrayList<>();
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.supportsStructure(structure)) {
                WorldPipeNetNode node = p.getOrCreateFromNet(world, pos, structure);
                if (node != null) list.add(node);
            }
        }
        return list;
    }

    @Override
    public @NotNull Collection<WorldPipeNetNode> getFromNets(World world, BlockPos pos, IPipeStructure structure) {
        List<WorldPipeNetNode> list = new ObjectArrayList<>();
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.supportsStructure(structure)) {
                WorldPipeNetNode node = p.getFromNet(world, pos, structure);
                if (node != null) list.add(node);
            }
        }
        return list;
    }

    @Override
    public void removeFromNets(World world, BlockPos pos, IPipeStructure structure) {
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.supportsStructure(structure)) p.removeFromNet(world, pos, structure);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn, IPipeStructure structure) {
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.supportsStructure(structure))
                p.addInformation(stack, worldIn, tooltip, flagIn, (IPipeMaterialStructure) structure);
        }
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        for (IPipeNetMaterialProperty p : this.properties.values()) {
            p.verifyProperty(properties);
        }
    }

    public interface IPipeNetMaterialProperty extends IMaterialProperty {

        @Nullable
        WorldPipeNetNode getOrCreateFromNet(World world, BlockPos pos, IPipeStructure structure);

        @Nullable
        WorldPipeNetNode getFromNet(World world, BlockPos pos, IPipeStructure structure);

        void mutateData(NetLogicData data, IPipeStructure structure);

        void removeFromNet(World world, BlockPos pos, IPipeStructure structure);

        boolean generatesStructure(IPipeStructure structure);

        boolean supportsStructure(IPipeStructure structure);

        void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> tooltip,
                            @NotNull ITooltipFlag flagIn, IPipeMaterialStructure structure);

        MaterialPropertyKey<?> getKey();

        class MaterialPropertyKey<T extends IPipeNetMaterialProperty> implements IStringSerializable {

            private final @NotNull String name;

            public MaterialPropertyKey(@NotNull String name) {
                this.name = name;
            }

            @Override
            public @NotNull String getName() {
                return name;
            }

            T cast(IPipeNetMaterialProperty property) {
                return (T) property;
            }
        }
    }
}
