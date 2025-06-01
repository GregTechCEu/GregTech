package gtqt.common.items.behaviors;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ProgrammableCircuit implements IItemBehaviour {
    String name;
    int type;
    MetaItem<?>.MetaValueItem item;

    public ProgrammableCircuit(int type, String name) {
        this.name = name;
        this.type = type;

    }

    public ProgrammableCircuit(MetaItem<?>.MetaValueItem item, String name) {
        this.name = name;
        this.item = item;
    }

    public static ProgrammableCircuit getInstanceFor(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof MetaItem)) return null;
        MetaItem<?>.MetaValueItem valueItem = ((MetaItem<?>) itemStack.getItem()).getItem(itemStack);
        if (valueItem == null) return null;
        for (IItemBehaviour behaviour : valueItem.getBehaviours()) {
            if (behaviour instanceof ProgrammableCircuit) {
                return (ProgrammableCircuit) behaviour;
            }
        }

        return null;
    }

    public void addInformation(ItemStack stack, List<String> lines) {
        if (name.equals("programmable_circuit")) {
            lines.add(I18n.format("请配合可编程覆盖板使用"));
            lines.add(I18n.format("安装有可编程覆盖板的输入总线/输入总成/单方块机器接收到本物品会自动将虚拟电路槽设置为当前电路值并消耗本物品"));
            lines.add(I18n.format("如果该多方块拥有输出总线/总成且输出总线/总成有空槽位，则会尝试返还编程电路"));
            lines.add(I18n.format("如果该多单方块机器输出有空槽位，则会尝试返还编程电路"));
        }
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public MetaItem<?>.MetaValueItem getItem() {
        return item;
    }

}
