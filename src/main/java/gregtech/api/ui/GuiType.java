package gregtech.api.ui;

import com.cleanroommc.modularui.manager.GuiInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class GuiType {

    private final GuiInfo guiInfo;

    public GuiType(@NotNull GuiInfo guiInfo) {
        this.guiInfo = guiInfo;
    }

    public void open(@NotNull EntityPlayer player) {
        this.guiInfo.open(player);
    }

    public void open(@NotNull EntityPlayer player, @NotNull World world, @NotNull BlockPos pos) {
        this.guiInfo.open(player, world, pos);
    }

    public void open(@NotNull EntityPlayer player, @NotNull World world, int x, int y, int z) {
        this.guiInfo.open(player, world, x, y ,z);
    }
}
