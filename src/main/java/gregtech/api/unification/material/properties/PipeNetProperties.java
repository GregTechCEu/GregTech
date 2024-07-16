package gregtech.api.unification.material.properties;

import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;

import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.IOreRegistrationHandler;

import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.function.TriConsumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static gregtech.api.unification.material.info.MaterialFlags.NO_UNIFICATION;

public class PipeNetProperties implements IMaterialProperty, IPipeNetNodeHandler {

    protected final Map<String, IPipeNetMaterialProperty> properties = new Object2ObjectOpenHashMap<>();

    public void setProperty(IPipeNetMaterialProperty property) {
        this.properties.put(property.getName(), property);
    }

    public boolean hasProperty(String propertyName) {
        return this.properties.containsKey(propertyName);
    }

    public <T extends IPipeNetMaterialProperty> T getProperty(String propertyName) {
        return (T) this.properties.get(propertyName);
    }

    public void removeProperty(String propertyName) {
        this.properties.remove(propertyName);
    }

    public boolean generatesStructure(IPipeStructure structure) {
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.generatesStructure(structure)) return true;
        }
        return false;
    }

    @Override
    public void addToNets(World world, BlockPos pos, IPipeStructure structure) {
        for (IPipeNetMaterialProperty p : properties.values()) {
            p.addToNet(world, pos, structure);
        }
    }

    @Override
    public Collection<WorldPipeNetNode> getFromNets(World world, BlockPos pos, IPipeStructure structure) {
        List<WorldPipeNetNode> list = new ObjectArrayList<>();
        for (IPipeNetMaterialProperty p : properties.values()) {
            WorldPipeNetNode node = p.getFromNet(world, pos, structure);
            if (node != null) list.add(node);
        }
        return list;
    }

    @Override
    public void removeFromNets(World world, BlockPos pos, IPipeStructure structure) {
        for (IPipeNetMaterialProperty p : properties.values()) {
            p.removeFromNet(world, pos, structure);
        }
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        for (IPipeNetMaterialProperty p : this.properties.values()) {
            p.verifyProperty(properties);
        }
    }

    public interface IPipeNetMaterialProperty extends IMaterialProperty, IStringSerializable {

        void addToNet(World world, BlockPos pos, IPipeStructure structure);

        @Nullable
        WorldPipeNetNode getFromNet(World world, BlockPos pos, IPipeStructure structure);

        void mutateData(NetLogicData data, IPipeStructure structure);

        void removeFromNet(World world, BlockPos pos, IPipeStructure structure);

        boolean generatesStructure(IPipeStructure structure);
    }
}
