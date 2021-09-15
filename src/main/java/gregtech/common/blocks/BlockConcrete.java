package gregtech.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class BlockConcrete extends StoneBlock<BlockConcrete.ConcreteVariant> {

    public BlockConcrete() {
        super(Material.ROCK);
        setTranslationKey("concrete");
        setHardness(2.0f);
        setResistance(3.0f);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 1);
        setDefaultState(withVariant(
                ConcreteVariant.LIGHT_CONCRETE,
                ChiselingVariant.NORMAL));
    }

    public enum ConcreteVariant implements IStringSerializable {

        LIGHT_CONCRETE("light_concrete"),
        LIGHT_BRICKS("light_bricks"),
        DARK_CONCRETE("dark_concrete"),
        DARK_BRICKS("dark_bricks");

        private final String name;

        ConcreteVariant(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

    }

    private static MovementInput manualInputCheck;

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        if (manualInputCheck == null) {
            manualInputCheck = new MovementInputFromOptions(Minecraft.getMinecraft().gameSettings);
        }
        IBlockState below = entityIn.getEntityWorld().getBlockState(new BlockPos(entityIn.posX, entityIn.posY - (1 / 16D), entityIn.posZ));
        if (below.getBlock() instanceof BlockConcrete) {
            manualInputCheck.updatePlayerMoveState();
            if ((manualInputCheck.moveForward != 0 || manualInputCheck.moveStrafe != 0) && !entityIn.isInWater()) {
                entityIn.motionX *= 1.6;
                entityIn.motionZ *= 1.6;
            }
        }
    }

}
