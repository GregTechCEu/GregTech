package gregtech.common.terminal.app.worldprospector.matcher;

import net.minecraft.block.state.IBlockState;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/09/16
 * @Description:
 */
public interface IMatcher {
    boolean match(final IBlockState state);
    int getColor();
}
