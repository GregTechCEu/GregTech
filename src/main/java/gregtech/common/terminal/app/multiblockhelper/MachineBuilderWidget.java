package gregtech.common.terminal.app.multiblockhelper;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternError;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.terminal.gui.widgets.MachineSceneWidget;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.BlockInfo;
import gregtech.client.utils.RenderBufferHelper;
import gregtech.common.inventory.handlers.CycleItemStackHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    TerminalOSWidget os;
    @SideOnly(Side.CLIENT)
    private MachineSceneWidget sceneWidget;
    @SideOnly(Side.CLIENT)
    private Set<BlockPos> highLightBlocks;
    private final MultiblockControllerBase controllerBase;
    private int selected = -1;
    @SideOnly(Side.CLIENT)
    private DraggableScrollableWidgetGroup candidates;
    @SideOnly(Side.CLIENT)
    private List<CycleItemStackHandler> handlers;

    public MachineBuilderWidget(int x, int y, int width, int height, MultiblockControllerBase controllerBase,
                                TerminalOSWidget os) {
        super(x, y, width, height);
        this.os = os;
        this.controllerBase = controllerBase;
        addWidget(new ImageWidget(0, 0, width, height, GuiTextures.BACKGROUND));
        addWidget(new RectButtonWidget(12, 125, width - 24, 20, 1)
                .setClickListener(this::autoBuildButton)
                .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(),
                        TerminalTheme.COLOR_B_2.getColor())
                .setIcon(new TextTexture("terminal.multiblock_ar.builder.auto", -1)));
        addWidget(new RectButtonWidget(12, 125 + 25, width - 24, 20, 1)
                .setClickListener(this::placeButton)
                .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(),
                        TerminalTheme.COLOR_B_2.getColor())
                .setIcon(new TextTexture("terminal.multiblock_ar.builder.place", -1)));
        addWidget(new RectButtonWidget(12, 125 + 50, width - 24, 20, 1)
                .setClickListener(this::debugButton)
                .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(),
                        TerminalTheme.COLOR_B_2.getColor())
                .setIcon(new TextTexture("terminal.multiblock_ar.builder.debug", -1)));
        if (os.isRemote()) {
            candidates = new DraggableScrollableWidgetGroup(-20, 0, 20, 180);
            addWidget(candidates);
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -2) { // select
            this.selected = buffer.readVarInt();
        } else if (id == -3) { // update pos facing
            this.pos = buffer.readBlockPos();
            this.facing = EnumFacing.VALUES[buffer.readByte()];
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    /**
     * I had to add slotWidget after parent widget be added, because of gtce's {@link gregtech.api.gui.INativeWidget}
     * interface.
     * Hopefully one day I can remove this worse interface.
     */
    public void addPlayerInventory() {
        InventoryPlayer inventoryPlayer = gui.entityPlayer.inventory;
        slotWidgets = new SlotWidget[36];
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                int index = col + row * 6;
                boolean isActive = inventoryPlayer.getStackInSlot(index).getItem() instanceof ItemBlock;
                slotWidgets[index] = new SlotWidget(inventoryPlayer, index, 12 + col * 18, 12 + row * 18, false,
                        false) {

                    @Override
                    public boolean mouseClicked(int mouseX, int mouseY, int button) {
                        if (isMouseOverElement(mouseX, mouseY) && isActive()) {
                            if (selected != index) {
                                selected = index;
                                MachineBuilderWidget.this.writeClientAction(-2, buf -> buf.writeVarInt(index));
                            }
                        }
                        return super.mouseClicked(mouseX, mouseY, button);
                    }

                    @Override
                    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
                        super.drawInBackground(mouseX, mouseY, partialTicks, context);
                        if (selected == index) {
                            drawSolidRect(getPosition().x, getPosition().y, getSize().width, getSize().height,
                                    0x4f00ff00);
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
        this.highLightBlocks = new HashSet<>();
        MachineSceneWidget.getWorldSceneRenderer().addRenderedBlocks(highLightBlocks, this::highLightRender);
        sceneWidget.setOnSelected(this::setFocus);
        sceneWidget.getAround().clear();
        Set<BlockPos> cores = sceneWidget.getCores();
        int rX = 5;
        int rY = 5;
        int rZ = 5;
        for (MultiblockShapeInfo shapeInfo : controllerBase.getMatchingShapes()) {
            BlockInfo[][][] blockInfos = shapeInfo.getBlocks();
            rX = Math.max(blockInfos.length, rX);
            rY = Math.max(blockInfos[0].length, rY);
            rZ = Math.max(blockInfos[0][0].length, rZ);
        }
        for (int x = -rX; x <= rX; x++) {
            for (int y = -rY; y <= rY; y++) {
                for (int z = -rZ; z <= rZ; z++) {
                    cores.add(controllerBase.getPos().add(x, y, z));
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void setFocus(BlockPos pos, EnumFacing facing) {
        this.pos = new BlockPos(pos);
        this.facing = facing;
        writeClientAction(-3, buf -> {
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
                    Block block = itemBlock.getBlock();
                    IBlockState state = block.getStateFromMeta(itemBlock.getMetadata(itemStack.getMetadata()));
                    if (block instanceof BlockBush) {
                        // Prevent placing lilypads, grass, etc where they should not be
                        if (!((BlockBush) block).canBlockStay(world, offset, state)) {
                            if (clickData.isClient) {
                                TerminalDialogWidget.showInfoDialog(os, "terminal.component.error",
                                        "This Block cannot be placed here").setClientSide().open();
                            }
                            return;
                        }
                    }

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
                    pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
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
            if (controllerBase.structurePattern.checkPatternFastAt(
                    controllerBase.getWorld(), controllerBase.getPos(), controllerBase.getFrontFacing().getOpposite(),
                    controllerBase.getUpwardsFacing(), controllerBase.allowsFlip()) == null) {
                PatternError error = controllerBase.structurePattern.getError();
                highLightBlocks.add(new BlockPos(error.getPos()));
                List<List<ItemStack>> candidatesItemStack = error.getCandidates();
                candidates.clearAllWidgets();
                int y = 1;
                handlers = new ArrayList<>();
                for (List<ItemStack> candidate : candidatesItemStack) {
                    CycleItemStackHandler handler = new CycleItemStackHandler(
                            NonNullList.from(ItemStack.EMPTY, candidate.toArray(new ItemStack[0])));
                    handlers.add(handler);
                    candidates.addWidget(new SlotWidget(handler, 0, 1, y, false, false)
                            .setBackgroundTexture(TerminalTheme.COLOR_B_2));
                    y += 20;
                }
                TerminalDialogWidget.showInfoDialog(os, "terminal.component.error", error.getErrorInfo())
                        .setClientSide().open();
            }
        }
    }

    private void autoBuildButton(ClickData clickData) {
        if (controllerBase != null) {
            if (!clickData.isClient && controllerBase.structurePattern != null) {
                controllerBase.structurePattern.autoBuild(gui.entityPlayer, controllerBase);
            }
        }
    }
}
