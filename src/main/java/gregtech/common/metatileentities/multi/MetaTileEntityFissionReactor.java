package gregtech.common.metatileentities.multi;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.nuclear.fission.FissionReactor;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.util.GTLog;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockFissionCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class MetaTileEntityFissionReactor extends MultiblockWithDisplayBase {

    private FissionReactor fissionReactor;

    public MetaTileEntityFissionReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFissionReactor(metaTileEntityId);
    }

    public boolean isBlockEdge(@Nonnull World world, @Nonnull BlockPos.MutableBlockPos pos, @Nonnull EnumFacing direction) {
        return this.isBlockEdge(world, pos, direction, 1);
    }

    public boolean isBlockEdge(@Nonnull World world, @Nonnull BlockPos.MutableBlockPos pos, @Nonnull EnumFacing direction, int steps) {
        return world.getBlockState(pos.move(direction, steps)).getBlock() != MetaBlocks.FISSION_CASING;
    }

    /**
     * Uses the layer the controller is on to determine the diameter of the structure
     */
    public int findDiameter() {
        int i = 1;
        while (i <= 15) {
            if (this.isBlockEdge(this.getWorld(), new BlockPos.MutableBlockPos(this.getPos()), this.getFrontFacing().getOpposite(), i)) break;
            i++;
        }
        return i;
    }

    /**
     * Checks for casings on top or bottom of the controller to determine the height of the reactor
     */
    public int findHeight(boolean top) {
        int i = 1;
        while (i <= 15) {
            if (this.isBlockEdge(this.getWorld(), new BlockPos.MutableBlockPos(this.getPos()), top ? EnumFacing.UP : EnumFacing.DOWN, i)) break;
            i++;
        }
        return i;
    }

    @Override
    protected void updateFormedValid() {
    }

    public boolean updateStructureDimensions() {
        return false;
    }

    @Nonnull
    @Override
    protected BlockPattern createStructurePattern() {

        int heightTop = this.getWorld() != null ? this.findHeight(true) : 4;
        int heightBottom = this.getWorld() != null ? this.findHeight(false) : 0;

        int height = heightTop + heightBottom + 1;

        int diameter = this.getWorld() != null ? Math.max(Math.min(this.findDiameter(), 15), 5) : 7;

        int radius = diameter % 2 == 0 ? (int) Math.floor(diameter / 2.f) : Math.round((diameter - 1)/2.f);

        StringBuilder interiorBuilder = new StringBuilder();

        String[] interiorSlice = new String[diameter];
        String[] controllerSlice = new String[diameter];
        String[] topSlice = new String[diameter];
        String[] bottomSlice = new String[diameter];

        // First loop over the matrix
        for (int i = 0; i < diameter; i++) {
            for (int j = 0; j < diameter; j++) {

                if (Math.pow(i - Math.floor(diameter/2.), 2) + Math.pow(j - Math.floor(diameter/2.), 2) < Math.pow(radius + 0.5f, 2)) {
                    interiorBuilder.append('A');
                } else {
                    interiorBuilder.append(' ');
                }
            }

            interiorSlice[i] = interiorBuilder.toString();
            GTLog.logger.info(interiorSlice[0].length());
            interiorBuilder.setLength(0);
        }
/*
        //Second loop is to detect where to put walls, the controller and I/O, two less iterations are needed because two strings always represent two walls on opposite sides
        interiorSlice[diameter - 1] = interiorSlice[0] = interiorSlice[0].replace('A', 'B');
        for (int i = 1; i < diameter - 1; i++) {
            for (int j = 0; j < diameter; j++) {
                if (j > 0 && j + 1 < diameter) {
                    if ((interiorSlice[i].charAt(j) == 'A' && interiorSlice[i].charAt(j - 1) == ' ') || (interiorSlice[i].charAt(j) == 'A' && interiorSlice[i].charAt(j + 1) == ' ')) {
                        interiorSlice[i] = interiorSlice[i].substring(0, j) + 'B' + interiorSlice[i].substring(j + 1);
                    }
                } else if (j == 0 && interiorSlice[i].charAt(0) == 'A') {
                    interiorSlice[i] = 'B' + interiorSlice[i].substring(1);
                } else if (j == diameter - 1 && interiorSlice[i].charAt(diameter - 1) == 'A') {
                    interiorSlice[i] = interiorSlice[i].substring(0, diameter - 1) + 'B';
                }
            }
        }
*/

        return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.FRONT, RelativeDirection.UP)
                .aisle("BBBSBBB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBBBBBB", "BBBBBBB")
                .aisle(interiorSlice).setRepeatable(heightTop)
                .where('S', selfPredicate())
                .where('A', states(getFuelChannelState()))
                .where('B', states(getVesselState()))
                .build();
    }

    @Nonnull
    protected IBlockState getVesselState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.REACTOR_VESSEL);
    }

    @Nonnull
    protected IBlockState getFuelChannelState() {
        return MetaBlocks.FISSION_CASING.getState(BlockFissionCasing.FissionCasingType.FUEL_CHANNEL);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.FISSION_REACTOR_TEXTURE;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.ASSEMBLER_OVERLAY;
    }
}
