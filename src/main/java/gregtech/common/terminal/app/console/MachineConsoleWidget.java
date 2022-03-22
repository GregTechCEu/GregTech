package gregtech.common.terminal.app.console;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.RecipeLogicSteam;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.Size;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMaintenanceHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.ItemStackHandler;
import org.lwjgl.opengl.GL11;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/25
 * @Description:
 */
public class MachineConsoleWidget extends WidgetGroup {
    private BlockPos pos;
    private EnumFacing facing;
    private MetaTileEntity mte;
    private UIWidgetGroup uiWidgetGroup;

    public MachineConsoleWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void setFocus(BlockPos pos, EnumFacing facing) {
        this.pos = new BlockPos(pos);
        this.facing = facing;
        this.mte = null;
        checkMachine();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        checkMachine();
    }

    private void checkMachine() {
        if (mte != null && mte.isValid()) {
            return;
        }
        if (pos != null && facing != null) {
            TileEntity te = gui.entityPlayer.world.getTileEntity(pos);
            clearAllWidgets();
            if (te instanceof IGregTechTileEntity && ((IGregTechTileEntity) te).isValid()) {
                mte = ((IGregTechTileEntity) te).getMetaTileEntity();
                initWidgets();
                if (isRemote()) {
                    writeClientAction(5, buf->{
                        buf.writeBlockPos(pos);
                        buf.writeByte(facing.getIndex());
                    });
                }
            }
        }
    }

    private void initWidgets() {
        if (mte != null) {
            uiWidgetGroup = new UIWidgetGroup();
            uiWidgetGroup.setActive(false);
            uiWidgetGroup.setVisible(false);
            Size size = getSize();
            addWidget(new ImageWidget(0, 0, size.width, size.height, GuiTextures.BACKGROUND));
            addWidget(new SimpleTextWidget(size.width / 2, 12, "", -1, ()->facing.toString().toUpperCase()).setShadow(true));
            int y = 20;
            if (mte.hasFrontFacing()) {
                addWidget(new RectButtonWidget(10, y, size.width - 20, 20, 1)
                        .setClickListener(clickData -> {
                            if (!isRemote() && mte.isValidFrontFacing(facing)) {
                                mte.setFrontFacing(facing);
                            }
                        })
                        .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_B_2.getColor())
                        .setIcon(new TextTexture("terminal.console.front", -1)));
                y += 25;
            }

            // IControllable
            IControllable controllable = mte.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, facing);
            if (controllable != null) {
                addWidget(new RectButtonWidget(10, y, 20, 20, 1)
                        .setToggleButton(GuiTextures.BUTTON_WORKING_ENABLE.getSubArea(0, 0, 1, 0.5), (c, p)->{
                            if (!isRemote()) {
                                controllable.setWorkingEnabled(p);
                            }
                        })
                        .setValueSupplier(false, controllable::isWorkingEnabled)
                        .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), 0)
                        .setHoverText("terminal.console.controllable")
                        .setIcon(GuiTextures.BUTTON_WORKING_ENABLE.getSubArea(0, 0.5, 1, 0.5)));
                // AbstractRecipeLogic
                AbstractRecipeLogic recipeLogic = mte.getCapability(GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC, facing);
                if (recipeLogic != null) {
                    addWidget(new CycleButtonWidget(35, y, 20, 20,
                            recipeLogic.getAvailableOverclockingTiers(), recipeLogic::getOverclockTier, recipeLogic::setOverclockTier)
                            .setTooltipHoverString("gregtech.gui.overclock.description")
                            .setButtonTexture(GuiTextures.BUTTON_OVERCLOCK));
                    addWidget(new ProgressWidget(recipeLogic::getProgressPercent, 60, y, 63, 20, GuiTextures.PROGRESS_BAR_ARC_FURNACE, ProgressWidget.MoveType.HORIZONTAL));
                    if (recipeLogic instanceof RecipeLogicSteam) {
                        y += 25;
                        addWidget(new RectButtonWidget(10, y, size.width - 20, 20, 1)
                                .setClickListener(clickData -> {
                                    EnumFacing currentVentingSide = ((RecipeLogicSteam) recipeLogic).getVentingSide();
                                    if (currentVentingSide == facing || mte.getFrontFacing() == facing) return;
                                    ((RecipeLogicSteam) recipeLogic).setVentingSide(facing);
                                })
                                .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_B_2.getColor())
                                .setIcon(new TextTexture("terminal.console.venting", -1)));
                    }
                }
                y += 25;
            }

            // SimpleMachineMetaTileEntity
            if (mte instanceof SimpleMachineMetaTileEntity) {
                SimpleMachineMetaTileEntity simpleMTE = (SimpleMachineMetaTileEntity) mte;
                // items output
                if (simpleMTE.getExportItems().getSlots() > 0) {
                    addWidget(new ImageWidget(10, y, 20 ,20, new ItemStackTexture(Items.GLOWSTONE_DUST)));
                    addWidget(new RectButtonWidget(33, y, 50, 20, 1)
                            .setClickListener(clickData -> {
                                if (!isRemote() && mte.getFrontFacing() != facing) {
                                    simpleMTE.setOutputFacingItems(facing);
                                }
                            })
                            .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_B_2.getColor())
                            .setHoverText("terminal.console.items"));
                    addWidget(new SimpleTextWidget(58, y + 10, "", -1, ()->simpleMTE.getOutputFacingItems().toString()));
                    addWidget(new RectButtonWidget(83, y, 20, 20, 1)
                            .setToggleButton(GuiTextures.BUTTON_ITEM_OUTPUT.getSubArea(0, 0.5, 1, 0.5), (c, p)->{
                                if (!isRemote()) {
                                    simpleMTE.setAutoOutputItems(p);
                                }
                            })
                            .setInitValue(simpleMTE.isAutoOutputItems())
                            .setValueSupplier(false, simpleMTE::isAutoOutputItems)
                            .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), 0)
                            .setHoverText("terminal.console.auto_output")
                            .setIcon(GuiTextures.BUTTON_ITEM_OUTPUT.getSubArea(0, 0, 1, 0.5)));
                    addWidget(new RectButtonWidget(103, y, 20, 20, 1)
                            .setToggleButton(GuiTextures.BUTTON_ALLOW_IMPORT_EXPORT.getSubArea(0, 0.5, 1, 0.5), (c, p)->{
                                if (!isRemote()) {
                                    simpleMTE.setAllowInputFromOutputSideItems(p);
                                }
                            })
                            .setInitValue(simpleMTE.isAllowInputFromOutputSideItems())
                            .setValueSupplier(false, simpleMTE::isAllowInputFromOutputSideItems)
                            .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), 0)
                            .setHoverText("terminal.console.input")
                            .setIcon(GuiTextures.BUTTON_ALLOW_IMPORT_EXPORT.getSubArea(0, 0, 1, 0.5)));
                    y += 20;
                }

                // fluids output
                if (simpleMTE.getExportFluids().getTanks() > 0) {
                    addWidget(new ImageWidget(10, y, 20 ,20, new ItemStackTexture(Items.WATER_BUCKET)));
                    addWidget(new RectButtonWidget(33, y, 50, 20, 1)
                            .setClickListener(clickData -> {
                                if (!isRemote() && simpleMTE.getFrontFacing() != facing) {
                                    simpleMTE.setOutputFacingFluids(facing);
                                }
                            })
                            .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_B_2.getColor())
                            .setHoverText("terminal.console.fluids"));
                    addWidget(new SimpleTextWidget(58, y + 10, "", -1, ()->simpleMTE.getOutputFacingFluids().toString()));
                    addWidget(new RectButtonWidget(83, y, 20, 20, 1)
                            .setToggleButton(GuiTextures.BUTTON_FLUID_OUTPUT.getSubArea(0, 0.5, 1, 0.5), (c, p)->{
                                if (!isRemote()) {
                                    simpleMTE.setAutoOutputFluids(p);
                                }
                            })
                            .setInitValue(simpleMTE.isAutoOutputFluids())
                            .setValueSupplier(false, simpleMTE::isAutoOutputFluids)
                            .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), 0)
                            .setHoverText("terminal.console.auto_output")
                            .setIcon(GuiTextures.BUTTON_FLUID_OUTPUT.getSubArea(0, 0, 1, 0.5)));
                    addWidget(new RectButtonWidget(103, y, 20, 20, 1)
                            .setToggleButton(GuiTextures.BUTTON_ALLOW_IMPORT_EXPORT.getSubArea(0, 0.5, 1, 0.5), (c, p)->{
                                if (!isRemote()) {
                                    simpleMTE.setAllowInputFromOutputSideFluids(p);
                                }
                            })
                            .setInitValue(simpleMTE.isAllowInputFromOutputSideFluids())
                            .setValueSupplier(false, simpleMTE::isAllowInputFromOutputSideFluids)
                            .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), 0)
                            .setHoverText("terminal.console.input")
                            .setIcon(GuiTextures.BUTTON_ALLOW_IMPORT_EXPORT.getSubArea(0, 0, 1, 0.5)));
                    y += 20;
                }
                y += 5;
            }

            // MetaTileEntityQuantumTank
            if (mte instanceof MetaTileEntityQuantumChest) {
                MetaTileEntityQuantumChest chest = (MetaTileEntityQuantumChest) mte;
                addWidget(new ImageWidget(10, y, 20 ,20, new ItemStackTexture(Items.GLOWSTONE_DUST)));
                addWidget(new RectButtonWidget(33, y, 50, 20, 1)
                        .setClickListener(clickData -> {
                            if (!isRemote() && mte.getFrontFacing() != facing) {
                                chest.setOutputFacing(facing);
                            }
                        })
                        .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_B_2.getColor())
                        .setHoverText("terminal.console.items"));
                addWidget(new SimpleTextWidget(58, y + 10, "", -1, ()->chest.getOutputFacing().toString()));
                addWidget(new RectButtonWidget(83, y, 20, 20, 1)
                        .setToggleButton(GuiTextures.BUTTON_ITEM_OUTPUT.getSubArea(0, 0.5, 1, 0.5), (c, p)->{
                            if (!isRemote()) {
                                chest.setAutoOutputItems(p);
                            }
                        })
                        .setInitValue(chest.isAutoOutputItems())
                        .setValueSupplier(false, chest::isAutoOutputItems)
                        .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), 0)
                        .setHoverText("terminal.console.auto_output")
                        .setIcon(GuiTextures.BUTTON_ITEM_OUTPUT.getSubArea(0, 0, 1, 0.5)));
                addWidget(new RectButtonWidget(103, y, 20, 20, 1)
                        .setToggleButton(GuiTextures.BUTTON_ALLOW_IMPORT_EXPORT.getSubArea(0, 0.5, 1, 0.5), (c, p)->{
                            if (!isRemote()) {
                                chest.setAllowInputFromOutputSide(p);
                            }
                        })
                        .setInitValue(chest.isAllowInputFromOutputSideItems())
                        .setValueSupplier(false, chest::isAllowInputFromOutputSideItems)
                        .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), 0)
                        .setHoverText("terminal.console.input")
                        .setIcon(GuiTextures.BUTTON_ALLOW_IMPORT_EXPORT.getSubArea(0, 0, 1, 0.5)));
                y += 25;
            } else if (mte instanceof MetaTileEntityQuantumTank) {
                MetaTileEntityQuantumTank tank = (MetaTileEntityQuantumTank) mte;
                addWidget(new ImageWidget(10, y, 20 ,20, new ItemStackTexture(Items.WATER_BUCKET)));
                addWidget(new RectButtonWidget(33, y, 50, 20, 1)
                        .setClickListener(clickData -> {
                            if (!isRemote() && tank.getFrontFacing() != facing) {
                                tank.setOutputFacing(facing);
                            }
                        })
                        .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_B_2.getColor())
                        .setHoverText("terminal.console.fluids"));
                addWidget(new SimpleTextWidget(58, y + 10, "", -1, ()->tank.getOutputFacing().toString()));
                addWidget(new RectButtonWidget(83, y, 20, 20, 1)
                        .setToggleButton(GuiTextures.BUTTON_FLUID_OUTPUT.getSubArea(0, 0.5, 1, 0.5), (c, p)->{
                            if (!isRemote()) {
                                tank.setAutoOutputFluids(p);
                            }
                        })
                        .setInitValue(tank.isAutoOutputFluids())
                        .setValueSupplier(false, tank::isAutoOutputFluids)
                        .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), 0)
                        .setHoverText("terminal.console.auto_output")
                        .setIcon(GuiTextures.BUTTON_FLUID_OUTPUT.getSubArea(0, 0, 1, 0.5)));
                addWidget(new RectButtonWidget(103, y, 20, 20, 1)
                        .setToggleButton(GuiTextures.BUTTON_ALLOW_IMPORT_EXPORT.getSubArea(0, 0.5, 1, 0.5), (c, p)->{
                            if (!isRemote()) {
                                tank.setAllowInputFromOutputSide(p);
                            }
                        })
                        .setInitValue(tank.isAllowInputFromOutputSideFluids())
                        .setValueSupplier(false, tank::isAllowInputFromOutputSideFluids)
                        .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), 0)
                        .setHoverText("terminal.console.input")
                        .setIcon(GuiTextures.BUTTON_ALLOW_IMPORT_EXPORT.getSubArea(0, 0, 1, 0.5)));
                y += 25;
            }

            // MultiBlockPart
            if (mte instanceof MetaTileEntityMultiblockPart) {
                // MetaTileEntityMaintenanceHatch
                if (mte instanceof MetaTileEntityMaintenanceHatch) {
                    addWidget(new RectButtonWidget(10, y, size.width - 20, 20, 1)
                            .setClickListener(clickData -> {
                              if (!isRemote()) {
                                  ((MetaTileEntityMaintenanceHatch) mte).fixAllMaintenanceProblems();
                              }
                            })
                            .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_B_2.getColor())
                            .setIcon(new TextTexture("terminal.console.maintenance", -1)));
                    y += 25;
                }
            }

            // CoverBehavior
            CoverBehavior cover = mte.getCoverAtSide(facing);
            if (cover != null) {
                this.addWidget(new SlotWidget(new ItemStackHandler(NonNullList.withSize(1, cover.getPickItem())), 0,
                        10, y, false, false));
                addWidget(new SimpleTextWidget(58, y + 10, "terminal.console.cover_rs", -1,
                        ()-> String.valueOf(cover.getRedstoneSignalOutput())).setShadow(true).setCenter(true));
                if (cover instanceof CoverWithUI) {
                    addWidget(new RectButtonWidget(83, y, 40, 20, 1)
                            .setClickListener(clickData -> uiWidgetGroup.openUI(((CoverWithUI) cover).createUI(gui.entityPlayer)))
                            .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_B_2.getColor())
                            .setIcon(new TextTexture("terminal.console.cover_gui", -1)));
                }
                y += 25;
            }
            addWidget(new RectButtonWidget(10, y, size.width - 20, 20, 1)
                    .setClickListener(clickData -> uiWidgetGroup.openUI(mte.getModularUI(gui.entityPlayer)))
                    .setColors(TerminalTheme.COLOR_B_1.getColor(), TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_B_2.getColor())
                    .setIcon(new TextTexture("terminal.console.gui", -1)));

            addWidget(uiWidgetGroup);
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 5) {
            setFocus(buffer.readBlockPos(), EnumFacing.VALUES[buffer.readByte()]);
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (uiWidgetGroup != null && uiWidgetGroup.isVisible()) {
            return;
        }
        super.drawInForeground(mouseX, mouseY);
    }

    private static class UIWidgetGroup extends WidgetGroup{
        private IGuiTexture background;

        public void clearUI() {
            background = null;
            clearAllWidgets();
            setActive(false);
            setVisible(false);
        }

        public void openUI(ModularUI ui) {
            clearUI();
            if (ui != null) {
                background = ui.backgroundPath;
                for (Widget widget : ui.guiWidgets.values()) {
                    if (widget instanceof SlotWidget) {
                        SlotWidget slotWidget = (SlotWidget) widget;
                        if (slotWidget.getHandle().getStack() == gui.entityPlayer.getHeldItem(EnumHand.MAIN_HAND)) {
                            slotWidget.setActive(false);
                        }
                    }
                    addWidget(widget);
                }
                setSize(new Size(ui.getWidth(), ui.getHeight()));
                addSelfPosition(gui.getScreenWidth() / 2 - getPosition().x - ui.getWidth() / 2,
                        gui.getScreenHeight() / 2 - getPosition().y - ui.getHeight() / 2);
                setActive(true);
                setVisible(true);
            }
        }

        @Override
        public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
            drawSolidRect(0, 0, gui.getScreenWidth(), gui.getScreenHeight(), 0xbf000000);
            if (background != null) {
                background.draw(getPosition().x, getPosition().y, getSize().width, getSize().height);
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GlStateManager.disableDepth();
            super.drawInBackground(mouseX, mouseY, partialTicks, context);
            GlStateManager.enableDepth();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }

        @Override
        public boolean keyTyped(char charTyped, int keyCode) {
            if (keyCode == 1) {
                clearUI();
                writeClientAction(5, buffer -> buffer.writeBoolean(true));
                return true;
            }
            super.keyTyped(charTyped, keyCode);
            return true;
        }

        @Override
        public void handleClientAction(int id, PacketBuffer buffer) {
            if (id == 5) {
                if (buffer.readBoolean()) {
                    clearUI();
                }
            } else {
                super.handleClientAction(id, buffer);
            }
        }

        @Override
        public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
            super.mouseWheelMove(mouseX, mouseY, wheelDelta);
            return true;
        }

        @Override
        public boolean mouseClicked(int mouseX, int mouseY, int button) {
            super.mouseClicked(mouseX, mouseY, button);
            return true;
        }

        @Override
        public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
            super.mouseDragged(mouseX, mouseY, button, timeDragged);
            return true;
        }

        @Override
        public boolean mouseReleased(int mouseX, int mouseY, int button) {
            super.mouseReleased(mouseX, mouseY, button);
            return true;
        }
    }
}
