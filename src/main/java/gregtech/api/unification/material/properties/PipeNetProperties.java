package gregtech.api.unification.material.properties;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.pipenet.IPipeNetNodeHandler;

import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;

import gregtech.api.graphnet.worldnet.WorldNetNode;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PipeNetProperties implements IMaterialProperty, IPipeNetNodeHandler {

    protected final Map<String, IPipeNetMaterialProperty> properties = new Object2ObjectOpenHashMap<>();

    public void setProperty(IPipeNetMaterialProperty property) {
        this.properties.put(property.getName(), property);
    }

    public void removeProperty(String propertyName) {
        this.properties.remove(propertyName);
    }

    @Override
    public void addToNets(World world, BlockPos pos, IPipeStructure structure) {
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.supportedStructure(structure)) p.addToNet(world, pos, structure);
        }
    }

    @Override
    public Collection<WorldPipeNetNode> getFromNets(World world, BlockPos pos, IPipeStructure structure) {
        List<WorldPipeNetNode> list = new ObjectArrayList<>();
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.supportedStructure(structure)) {
                WorldPipeNetNode node = p.getFromNet(world, pos, structure);
                if (node != null) list.add(node);
            }
        }
        return list;
    }

    @Override
    public void removeFromNets(World world, BlockPos pos, IPipeStructure structure) {
        for (IPipeNetMaterialProperty p : properties.values()) {
            if (p.supportedStructure(structure)) p.removeFromNet(world, pos, structure);
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

        void removeFromNet(World world, BlockPos pos, IPipeStructure structure);

        boolean supportedStructure(IPipeStructure structure);
    }
}
