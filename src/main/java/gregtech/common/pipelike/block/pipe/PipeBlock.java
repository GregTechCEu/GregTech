package gregtech.common.pipelike.block.pipe;

import gregtech.api.graphnet.gather.GatherStructuresEvent;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.physical.IBurnable;
import gregtech.api.graphnet.pipenet.physical.block.PipeMaterialBlock;

import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.unification.material.registry.MaterialRegistry;

import gregtech.api.util.EntityDamageUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.common.FMLCommonHandler;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PipeBlock extends PipeMaterialBlock implements IBurnable {

    public PipeBlock(PipeStructure structure, MaterialRegistry registry) {
        super(structure, registry);
    }

    public static Set<PipeStructure> gatherStructures() {
        GatherStructuresEvent<PipeStructure> event = new GatherStructuresEvent<>(PipeStructure.class);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getGathered();
    }
}
