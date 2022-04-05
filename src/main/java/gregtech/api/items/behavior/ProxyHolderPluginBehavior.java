package gregtech.api.items.behavior;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Objects;

public abstract class ProxyHolderPluginBehavior extends MonitorPluginBaseBehavior {
    protected IGregTechTileEntity holder;
    private BlockPos pos;

    @Override
    public void update() {
        if (pos != null && holder == null) {
            holder = this.screen.getHolderFromPos(pos);
            if (holder != null) {
                onHolderChanged(null);
            }
        }
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        super.addInformation(itemStack, lines);
        lines.add(I18n.format("metaitem.plugin.proxy.tooltips.1"));
    }

    public void onHolderPosUpdated(BlockPos pos) {
        if (Objects.equals(this.pos, pos)) return;
        this.pos = pos;
        IGregTechTileEntity lastHolder = holder;
        holder = this.screen.getHolderFromPos(pos);
        if (!Objects.equals(lastHolder, holder)) onHolderChanged(lastHolder);
    }

    protected abstract void onHolderChanged(IGregTechTileEntity lastHolder);

    public IGregTechTileEntity getHolder() {
        return holder;
    }
}
