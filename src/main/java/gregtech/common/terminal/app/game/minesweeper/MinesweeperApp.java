package gregtech.common.terminal.app.game.minesweeper;

import gregtech.api.terminal.app.AbstractApplication;
import gregtech.common.terminal.app.game.minesweeper.widget.MineMapWidget;

public class MinesweeperApp extends AbstractApplication {
    MineMapWidget mineField;

    public MinesweeperApp() {
        super("minesweeper");
    }

    @Override
    public AbstractApplication initApp() {
        mineField = new MineMapWidget(20, 20, 30);

        return this;
    }
}
