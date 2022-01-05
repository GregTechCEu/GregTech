package gregtech.api.util;

import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.common.items.MetaItems;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for handling GT tool types instead of specific tools
 */
public class GTToolTypes {

    public static final List<ToolMetaItem<?>.MetaToolValueItem> wrenches = new ArrayList<ToolMetaItem<?>.MetaToolValueItem>() {{
        add(MetaItems.WRENCH);
        add(MetaItems.WRENCH_LV);
        add(MetaItems.WRENCH_MV);
        add(MetaItems.WRENCH_HV);
    }};

    public static final List<ToolMetaItem<?>.MetaToolValueItem> screwdrivers = new ArrayList<ToolMetaItem<?>.MetaToolValueItem>() {{
        add(MetaItems.SCREWDRIVER);
        add(MetaItems.SCREWDRIVER_LV);
    }};

    public static final List<ToolMetaItem<?>.MetaToolValueItem> softHammers = new ArrayList<ToolMetaItem<?>.MetaToolValueItem>() {{
        add(MetaItems.SOFT_HAMMER);
    }};

    public static final List<ToolMetaItem<?>.MetaToolValueItem> hardHammers = new ArrayList<ToolMetaItem<?>.MetaToolValueItem>() {{
        add(MetaItems.HARD_HAMMER);
    }};

    public static final List<ToolMetaItem<?>.MetaToolValueItem> wireCutters = new ArrayList<ToolMetaItem<?>.MetaToolValueItem>() {{
        add(MetaItems.WIRE_CUTTER);
    }};

    public static final List<ToolMetaItem<?>.MetaToolValueItem> crowbars = new ArrayList<ToolMetaItem<?>.MetaToolValueItem>() {{
        add(MetaItems.CROWBAR);
    }};
}
