package gregtech.api.block;

import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import codechicken.lib.vec.Vector3;

public interface ICustomParticleBlock {

    void handleCustomParticle(World worldObj, BlockPos blockPos, ParticleManager particleManager, Vector3 entityPos,
                              int numberOfParticles);
}
