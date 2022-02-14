package gregtech.api.items.behavior;

import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.LocalizationUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Objects;

public abstract class ProxyHolderPluginBehavior extends MonitorPluginBaseBehavior {
    protected MetaTileEntityHolder holder;
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
        lines.add(LocalizationUtils.format("metaitem.plugin.proxy.tooltips.1"));
    }

    public void onHolderPosUpdated(BlockPos pos) {
        if (Objects.equals(this.pos, pos)) return;
        this.pos = pos;
        MetaTileEntityHolder lastHolder = holder;
        holder = this.screen.getHolderFromPos(pos);
        if (!Objects.equals(lastHolder, holder)) onHolderChanged(lastHolder);
    }

    protected abstract void onHolderChanged(MetaTileEntityHolder lastHolder);

    public MetaTileEntityHolder getHolder() {
        return holder;
    }
}
