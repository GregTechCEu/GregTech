package gregtech.common.blocks.clipboard;
/*

import net.minecraft.util.math.AxisAlignedBB;

public class BlockClipboard extends BiblioBlock {
    public static final String name = "Clipboard";

    public static final BlockClipboard instance = new BlockClipboard();

    public BlockClipboard() {
        super(Material.WOOD, SoundType.WOOD, null, "Clipboard");
    }

    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return new ArrayList<>();
    }

    public boolean onBlockActivatedCustomCommands(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
        if (player.isSneaking() && !world.isRemote) {
            dropStackInSlot(world, pos, 0, pos);
            world.setBlockToAir(pos);
            return true;
        }
        if (!player.isSneaking() && world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile != null && tile instanceof MetaTileEntityClipboard) {
                MetaTileEntityClipboard clipboard = (MetaTileEntityClipboard)tile;
                int updatePos = getSelectionPointFromFace(face, hitX, hitY, hitZ);
                ByteBuf buffer = Unpooled.buffer();
                buffer.writeInt(pos.getX());
                buffer.writeInt(pos.getY());
                buffer.writeInt(pos.getZ());
                buffer.writeInt(updatePos);
                BiblioCraft.ch_BiblioClipboard.sendToServer(new FMLProxyPacket(new PacketBuffer(buffer), "BiblioClipboard"));
                return true;
            }
        }
        return false;
    }

    private int getSelectionPointFromFace(EnumFacing face, float hitx, float hity, float hitz) {
        switch (face) {
            case NORTH:
                return getSelectionPoint(1.0F - hitx, 1.0F - hity);
            case SOUTH:
                return getSelectionPoint(hitx, 1.0F - hity);
            case WEST:
                return getSelectionPoint(hitz, 1.0F - hity);
            case EAST:
                return getSelectionPoint(1.0F - hitz, 1.0F - hity);
        }
        return -1;
    }

    private int getSelectionPoint(float x, float y) {
        if (x > 0.21F && x < 0.272F) {
            float spacing = 0.0655F;
            for (int i = 0; i < 9; i++) {
                if (y > 0.23D + (i * spacing) && y < 0.285F + i * spacing)
                    return i;
            }
        }
        if (y > 0.83D && y < 0.868F) {
            if (x > 0.296F && x < 0.387F)
                return 10;
            if (x > 0.599F && x < 0.843F)
                return 11;
        }
        return -1;
    }

    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return (TileEntity)new MetaTileEntityClipboard();
    }

    public List<String> getModelParts(BiblioTileEntity tile) {
        List<String> modelParts = new ArrayList<>();
        modelParts.add("Clipboard");
        if (tile instanceof MetaTileEntityClipboard) {
            MetaTileEntityClipboard clipboard = (MetaTileEntityClipboard)tile;
            clipboard.getNBTData();
            switch (clipboard.button0state) {
                case 1:
                    modelParts.add("box1c");
                    break;
                case 2:
                    modelParts.add("box1x");
                    break;
            }
            switch (clipboard.button1state) {
                case 1:
                    modelParts.add("box2c");
                    break;
                case 2:
                    modelParts.add("box2x");
                    break;
            }
            switch (clipboard.button2state) {
                case 1:
                    modelParts.add("box3c");
                    break;
                case 2:
                    modelParts.add("box3x");
                    break;
            }
            switch (clipboard.button3state) {
                case 1:
                    modelParts.add("box4c");
                    break;
                case 2:
                    modelParts.add("box4x");
                    break;
            }
            switch (clipboard.button4state) {
                case 1:
                    modelParts.add("box5c");
                    break;
                case 2:
                    modelParts.add("box5x");
                    break;
            }
            switch (clipboard.button5state) {
                case 1:
                    modelParts.add("box6c");
                    break;
                case 2:
                    modelParts.add("box6x");
                    break;
            }
            switch (clipboard.button6state) {
                case 1:
                    modelParts.add("box7c");
                    break;
                case 2:
                    modelParts.add("box7x");
                    break;
            }
            switch (clipboard.button7state) {
                case 1:
                    modelParts.add("box8c");
                    break;
                case 2:
                    modelParts.add("box8x");
                    break;
            }
        }
        return modelParts;
    }

    public void additionalPlacementCommands(BiblioTileEntity biblioTile, EntityLivingBase player) {}

    public ItemStack getPickBlockExtras(ItemStack stack, World world, BlockPos pos) {
        return stack;
    }

    public ExtendedBlockState getExtendedBlockStateAlternate(ExtendedBlockState state) {
        return state;
    }

    public IExtendedBlockState getIExtendedBlockStateAlternate(BiblioTileEntity biblioTile, IExtendedBlockState state) {
        return state;
    }

    public TRSRTransformation getAdditionalTransforms(TRSRTransformation transform, BiblioTileEntity tile) {
        return transform;
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccess, BlockPos pos) {
        AxisAlignedBB output = getBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        TileEntity tile = blockAccess.getTileEntity(pos);
        if (tile != null && tile instanceof MetaTileEntityClipboard) {
            MetaTileEntityClipboard clipboard = (MetaTileEntityClipboard)tile;
            switch (clipboard.getAngle()) {
                case SOUTH:
                    output = getBlockBounds(0.97F, 0.08F, 0.15F, 1.0F, 0.92F, 0.85F);
                    break;
                case WEST:
                    output = getBlockBounds(0.15F, 0.08F, 0.97F, 0.85F, 0.92F, 1.0F);
                    break;
                case NORTH:
                    output = getBlockBounds(0.0F, 0.08F, 0.15F, 0.03F, 0.92F, 0.85F);
                    break;
                case EAST:
                    output = getBlockBounds(0.15F, 0.08F, 0.0F, 0.85F, 0.92F, 0.03F);
                    break;
            }
        }
        return output;
    }

    public IBlockState getFinalBlockstate(IBlockState state, IBlockState newState) {
        return newState;
    }
}
*/
