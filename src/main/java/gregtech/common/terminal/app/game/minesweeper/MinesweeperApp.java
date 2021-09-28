package gregtech.common.terminal.app.game.minesweeper;

import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.common.terminal.app.game.minesweeper.widget.MineMapWidget;

public class MinesweeperApp extends AbstractApplication {
    MineMapWidget mineField;
    int timer;

    public MinesweeperApp() {
        super("minesweeper");
    }

    @Override
    public AbstractApplication initApp() {
        mineField = new MineMapWidget(20, 14, 50);
        this.addWidget(mineField);
        //this.addWidget(new SimpleTextWidget(333 / 2, 232 / 2, "", 0x00000000, () -> ""));
        return this;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if(mineField.hasWon() || mineField.hasLost()) {
            mineField = new MineMapWidget(20, 14, 50);
        }
    }

    public String getFlagsPercentage() {
        return "/" + mineField.mineCount;
    }
}
