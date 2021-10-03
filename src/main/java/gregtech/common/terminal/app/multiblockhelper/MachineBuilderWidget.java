package gregtech.common.terminal.app.multiblockhelper;

import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.render.scene.TrackedDummyWorld;
import gregtech.api.render.scene.WorldSceneRenderer;
import gregtech.api.terminal.gui.widgets.MachineSceneWidget;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.GTLog;
import gregtech.api.util.RenderBufferHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/09/15
 * @Description:
 */
public class MachineBuilderWidget extends WidgetGroup {
    private BlockPos pos;
    private EnumFacing facing;
    private SlotWidget[] slotWidgets;
    @SideOnly(Side.CLIENT)
    private MachineSceneWidget sceneWidget;
    @SideOnly(Side.CLIENT)
    private Set<BlockPos> highLightBlocks;
    private final MultiblockControllerBase controllerBase;
    private int selected = -1;

    public MachineBuilderWidget(int x, int y, int width, int height, MultiblockControllerBase controllerBase) {
        super(x, y, width, height);
        this.controllerBase = controllerBase;
        addWidget(new ImageWidget(0, 0, width, height, GuiTextures.BACKGROUND));
        addWidget(new RectButtonWidget(12, 125, width - 24, 20, 1)
                .setClickListener(this::autoBuildButton)
                .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_B_2.getColor())
                .setIcon(new TextTexture("terminal.multiblock_ar.builder.auto", -1)));
        addWidget(new RectButtonWidget(12, 125 + 25, width - 24, 20, 1)
                .setClickListener(this::placeButton)
                .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_B_2.getColor())
                .setIcon(new TextTexture("terminal.multiblock_ar.builder.place", -1)));
        addWidget(new RectButtonWidget(12, 125 + 50, width - 24, 20, 1)
                .setClickListener(this::debugButton)
                .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_B_2.getColor())
                .setIcon(new TextTexture("terminal.multiblock_ar.builder.debug", -1)));
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -1) { // auto build
            List<BlockInfo> blockInfos = new ArrayList<>();
            List<ItemStack> map = new ArrayList<>();
            int size = buffer.readVarInt();
            try {
                for (int i = 0; i < size; i++) {
                    map.add(buffer.readItemStack());
                }
            } catch (Exception e) {
                GTLog.logger.error("packets init error", e);
            }
            size = buffer.readVarInt();
            for (int i = 0; i < size; i++) {
                blockInfos.add(new BlockInfo(buffer));
            }
            autoBuild(blockInfos, map);
        } else if (id == -2) { // select
            this.selected = buffer.readVarInt();
        } else  if (id == -3) { // update pos facing
            this.pos = buffer.readBlockPos();
            this.facing = EnumFacing.VALUES[buffer.readByte()];
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    /**
     * I had to add slotWidget after parent widget be added, because of gtce's {@link gregtech.api.gui.INativeWidget} interface.
     * Hopefully one day I can remove this worse interface.
     */
    public void addPlayerInventory() {
        InventoryPlayer inventoryPlayer = gui.entityPlayer.inventory;
        slotWidgets = new SlotWidget[36];
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                int index = col + row * 6;
                boolean isActive = false;
                if (inventoryPlayer.getStackInSlot(index).getItem() instanceof ItemBlock) {
                    isActive = true;
                }
                slotWidgets[index] = new SlotWidget(inventoryPlayer, index, 12 + col * 18, 12 + row * 18, false, false) {
                    @Override
                    public boolean mouseClicked(int mouseX, int mouseY, int button) {
                        if (isMouseOverElement(mouseX, mouseY) && isActive()) {
                            if (selected != index) {
                                selected = index;
                                MachineBuilderWidget.this.writeClientAction(-2, buf->buf.writeVarInt(index));
                            }
                        }
                        return super.mouseClicked(mouseX, mouseY, button);
                    }


                    @Override
                    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
                        super.drawInBackground(mouseX, mouseY, context);
                        if (selected == index) {
                            drawSolidRect(getPosition().x, getPosition().y, getSize().width, getSize().height, 0x4f00ff00);
                        }
                    }
                }.setBackgroundTexture(GuiTextures.SLOT);
                slotWidgets[index].setActive(isActive);
                this.addWidget(slotWidgets[index]);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void setSceneWidget(MachineSceneWidget sceneWidget) {
        this.sceneWidget = sceneWidget;
        this.sceneWidget.setMaxZoom(15);
        this.highLightBlocks = new HashSet<>();
        sceneWidget.getWorldSceneRenderer().addRenderedBlocks(highLightBlocks, this::highLightRender);
        sceneWidget.setOnSelected(this::setFocus);
        sceneWidget.getAround().clear();
        Set<BlockPos> cores = sceneWidget.getCores();
        int r = 10;
        WorldSceneRenderer worldSceneRenderer = MultiBlockPreviewARApp.getWorldSceneRenderer(controllerBase);
        if (worldSceneRenderer != null && worldSceneRenderer.world instanceof TrackedDummyWorld) {
            Vector3f maxPos = ((TrackedDummyWorld) worldSceneRenderer.world).getMaxPos();
            Vector3f minPos = ((TrackedDummyWorld) worldSceneRenderer.world).getMinPos();
            r = (int) Math.max((maxPos.x - minPos.x), Math.max(maxPos.y- minPos.y, maxPos.z - minPos.y));
        }
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    cores.add(controllerBase.getPos().add(x, y, z));
                }
            }
        }
    }


    @SideOnly(Side.CLIENT)
    private void setFocus(BlockPos pos, EnumFacing facing) {
        this.pos = new BlockPos(pos);
        this.facing = facing;
        writeClientAction(-3, buf->{
            buf.writeBlockPos(pos);
            buf.writeByte(facing.getIndex());
        });
    }

    private void placeButton(ClickData clickData) {
        if (pos != null && facing != null && selected != -1) {
            World world = controllerBase.getWorld();
            BlockPos offset = pos.offset(facing);
            if (world.getBlockState(offset).getBlock() == Blocks.AIR) {
                ItemStack itemStack = slotWidgets[selected].getHandle().getStack();
                if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemBlock) {
                    ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
                    float hitX = pos.getX() + 0.5f;
                    float hitY = pos.getY() + 0.5f;
                    float hitZ = pos.getZ() + 0.5f;
                    IBlockState state = itemBlock.getBlock().getStateFromMeta(itemBlock.getMetadata(itemStack.getMetadata()));
                    itemBlock.placeBlockAt(itemStack, gui.entityPlayer, world, offset, facing, hitX, hitY, hitZ, state);
                    itemStack.shrink(1);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void highLightRender(boolean isTESR, int pass, BlockRenderLayer layer) {
        if (isTESR && pass == 0) return;

        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth(5);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for (BlockPos pos : highLightBlocks) {
            RenderBufferHelper.renderCubeFrame(buffer,
                    pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1,pos.getY() + 1, pos.getZ() + 1,
                    1, 0, 0, 1);
        }

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.color(1, 1, 1, 1);
    }

    private void debugButton(ClickData clickData) {
        if (clickData.isClient && controllerBase != null) {
            highLightBlocks.clear();
            if (controllerBase.structurePattern.checkPatternAt(controllerBase.getWorld(), controllerBase.getPos(), controllerBase.getFrontFacing().getOpposite()) == null) {
                highLightBlocks.add(new BlockPos(controllerBase.structurePattern.blockPos));
            }
        }
    }

    private void autoBuildButton(ClickData clickData) {
        if (controllerBase != null) {
            if (clickData.isClient) {
                World world = gui.entityPlayer.world;
                WorldSceneRenderer worldSceneRenderer = MultiBlockPreviewARApp.getWorldSceneRenderer(controllerBase);
                if (worldSceneRenderer != null) {
                    List<ItemStack> map = new ArrayList<>();
                    Set<BlockPos> exist = new HashSet<>();
                    List<BlockInfo> blockInfos = new ArrayList<>();
                    List<BlockPos> renderedBlocks = worldSceneRenderer.renderedBlocksMap.keySet().stream().flatMap(Collection::stream).collect(Collectors.toList());
                    EnumFacing refFacing = EnumFacing.EAST;
                    BlockPos refPos = BlockPos.ORIGIN;
                    for(BlockPos blockPos : renderedBlocks) {
                        MetaTileEntity metaTE = BlockMachine.getMetaTileEntity(worldSceneRenderer.world, blockPos);
                        if(metaTE instanceof MultiblockControllerBase && metaTE.metaTileEntityId.equals(controllerBase.metaTileEntityId)) {
                            refPos = blockPos;
                            refFacing = metaTE.getFrontFacing();
                            break;
                        }
                    }
                    for(BlockPos blockPos : renderedBlocks) {
                        if (blockPos.equals(refPos)) continue;
                        EnumFacing frontFacing = controllerBase.getFrontFacing();
                        // TODO SIDEWAYS IN THE FUTURE
//                                        EnumFacing spin = controllerBase.getSpin();
                        EnumFacing spin = EnumFacing.NORTH;
                        BlockPos realPos = BlockPattern.getActualPos(refFacing, frontFacing, spin
                                , blockPos.getX() - refPos.getX()
                                , blockPos.getY() - refPos.getY()
                                , blockPos.getZ() - refPos.getZ()).add(controllerBase.getPos());
                        if (!world.isAirBlock(realPos) || worldSceneRenderer.world.isAirBlock(blockPos)) continue;
                        IBlockState blockState = worldSceneRenderer.world.getBlockState(blockPos);
                        MetaTileEntity metaTileEntity = BlockMachine.getMetaTileEntity(worldSceneRenderer.world, blockPos);
                        ItemStack stack = blockState.getBlock().getItem(worldSceneRenderer.world, blockPos, blockState);
                        if (metaTileEntity != null) {
                            stack = metaTileEntity.getStackForm();
                        }
                        if (map.stream().noneMatch(stack::isItemEqual)) {
                            map.add(stack);
                        }
                        if(exist.contains(realPos)) continue;
                        exist.add(realPos);
                        blockInfos.add(new BlockInfo(realPos
                                , map.indexOf(map.stream().filter(stack::isItemEqual).findFirst().orElse(stack))
                                , metaTileEntity != null ? BlockPattern.getActualFrontFacing(refFacing, frontFacing, spin, metaTileEntity.getFrontFacing()): EnumFacing.SOUTH));
                    }
                    writeClientAction(-1, buf->{
                        buf.writeVarInt(map.size());
                        map.forEach(buf::writeItemStack);
                        buf.writeVarInt(blockInfos.size());
                        blockInfos.forEach(blockInfo -> blockInfo.writeBuf(buf));
                    });
                    for (BlockInfo succeed : autoBuild(blockInfos, map)) {
                        sceneWidget.getCores().add(succeed.pos);
                    }
                }
            }
        }
    }

    private List<BlockInfo> autoBuild(List<BlockInfo> blockInfos, List<ItemStack> map) {
        World world = gui.entityPlayer.world;
        List<BlockInfo> succeed = new ArrayList<>();
        for (BlockInfo blockInfo : blockInfos) {
            ItemStack stack = map.get(blockInfo.stack);
            if (stack.getItem() instanceof ItemBlock && world.isAirBlock(blockInfo.pos)) {
                if (!gui.entityPlayer.isCreative()) {
                    boolean find = false;
                    for (ItemStack itemStack :gui.entityPlayer.inventory.mainInventory) {
                        if (itemStack.isItemEqual(stack) && !itemStack.isEmpty()) {
                            itemStack.setCount(itemStack.getCount() - 1);
                            find = true;
                            break;
                        }
                    }
                    if (!find) { // match AbilityPart
                        for (ItemStack itemStack : gui.entityPlayer.inventory.mainInventory) {
                            if(itemStack.getItem() instanceof MachineItemBlock && stack.getItem() instanceof MachineItemBlock && !itemStack.isEmpty()) {
                                MetaTileEntity hold = MachineItemBlock.getMetaTileEntity(itemStack);
                                MetaTileEntity expect = MachineItemBlock.getMetaTileEntity(stack);
                                if (hold instanceof IMultiblockAbilityPart && expect instanceof IMultiblockAbilityPart){
                                    if (((IMultiblockAbilityPart<?>) hold).getAbility() == ((IMultiblockAbilityPart<?>) expect).getAbility()) {
                                        stack = itemStack.copy();
                                        itemStack.setCount(itemStack.getCount() - 1);
                                        find = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (!find) continue;
                }
                ItemBlock itemBlock = (ItemBlock) stack.getItem();
                IBlockState state = itemBlock.getBlock().getStateFromMeta(itemBlock.getMetadata(stack.getMetadata()));
                succeed.add(blockInfo);
                world.setBlockState(blockInfo.pos, state);
                TileEntity holder = world.getTileEntity(blockInfo.pos);
                if (holder instanceof MetaTileEntityHolder) {
                    MetaTileEntity sampleMetaTileEntity = GregTechAPI.MTE_REGISTRY.getObjectById(stack.getItemDamage());
                    if (sampleMetaTileEntity != null) {
                        MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) holder).setMetaTileEntity(sampleMetaTileEntity);
                        if (stack.hasTagCompound()) {
                            metaTileEntity.initFromItemStackData(stack.getTagCompound());
                        }
                        if (metaTileEntity.isValidFrontFacing(blockInfo.facing)) {
                            metaTileEntity.setFrontFacing(blockInfo.facing);
                        }
                    }
                }
            }
        }
        return succeed;
    }

    private static class BlockInfo {
        public BlockPos pos;
        public int stack;
        public EnumFacing facing;

        public BlockInfo(BlockPos pos, int stack, EnumFacing facing) {
            this.pos = pos;
            this.stack = stack;
            this.facing = facing;
        }

        public BlockInfo(PacketBuffer buffer) {
            this.pos = buffer.readBlockPos();
            this.stack = buffer.readVarInt();
            this.facing = EnumFacing.VALUES[buffer.readByte()];
        }

        public void writeBuf(PacketBuffer buffer) {
            buffer.writeBlockPos(pos);
            buffer.writeVarInt(stack);
            buffer.writeByte(facing.getIndex());
        }
    }
}
