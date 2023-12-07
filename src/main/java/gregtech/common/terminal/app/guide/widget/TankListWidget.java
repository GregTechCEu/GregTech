package gregtech.common.terminal.app.guide.widget;

import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.api.util.Size;
import gregtech.common.terminal.app.guideeditor.widget.configurator.FluidStackConfigurator;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class TankListWidget extends GuideWidgetGroup {

    public final static String NAME = "tanks";

    // config
    public List<FluidStackInfo> fluid_list;

    protected transient Rectangle scissor;

    @Override
    public Widget initFixed() {
        this.clearAllWidgets();
        IGuiTexture background = new ColorRectTexture(0x4f000000);
        int size = fluid_list.size();
        int maxXSize = getSize().width / 18;
        int xPos;
        if (maxXSize < 1) {
            maxXSize = 1;
            xPos = 0;
        } else {
            xPos = (getSize().width - (Math.min(size, maxXSize)) * 18) / 2;
        }
        int maxYSize = size / maxXSize + ((size % maxXSize) == 0 ? 0 : 1);
        for (int y = 0; y <= size / maxXSize; y++) {
            for (int x = 0; x < maxXSize; x++) {
                int i = x + y * maxXSize;
                if (i < size) {
                    FluidStack fluidStack = fluid_list.get(i).getInstance();
                    TankWidget widget = new TankWidget(new FluidTank(fluidStack, fluid_list.get(i).amount),
                            xPos + x * 18, y * 18, 18, 18);
                    widget.setBackgroundTexture(background).setAlwaysShowFull(true).setClient();
                    this.addWidget(widget);
                }
            }
        }
        setSize(new Size(getSize().width / 18 > 0 ? getSize().width : 18, maxYSize * 18));
        return this;
    }

    @Override
    public String getRegistryName() {
        return NAME;
    }

    @Override
    public JsonObject getTemplate(boolean isFixed) {
        JsonObject template = super.getTemplate(isFixed);
        template.add("fluid_list",
                new Gson().toJsonTree(Collections.singletonList(new FluidStackInfo("distilled_water", 1))));
        return template;
    }

    @Override
    public void loadConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, boolean isFixed,
                                 Consumer<String> needUpdate) {
        super.loadConfigurator(group, config, isFixed, needUpdate);
        group.addWidget(new FluidStackConfigurator(group, config, "fluid_list").setOnUpdated(needUpdate));
    }

    public static class FluidStackInfo {

        // config
        public String id;
        public int amount = 1;

        private transient FluidStack fluidStack;

        public FluidStackInfo() {}

        public void update(FluidStack itemStack) {
            if (itemStack != null) {
                id = FluidRegistry.getFluidName(itemStack.getFluid());
                amount = itemStack.amount;
            } else {
                id = null;
                fluidStack = null;
                amount = 0;
            }
        }

        public FluidStackInfo(String id, int amount) {
            this.id = id;
            this.amount = amount;
        }

        public FluidStack getInstance() {
            if (fluidStack == null && id != null) {
                Fluid fluid = FluidRegistry.getFluid(id);
                if (fluid != null) {
                    fluidStack = new FluidStack(fluid, amount);
                } else {
                    id = null;
                }
            }
            return fluidStack;
        }
    }
}
