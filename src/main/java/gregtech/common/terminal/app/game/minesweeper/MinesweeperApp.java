package gregtech.common.terminal.app.game.minesweeper;

import gregtech.api.guiOld.widgets.SimpleTextWidget;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.common.terminal.app.game.minesweeper.widget.MineMapWidget;

public class MinesweeperApp extends AbstractApplication {
    private MineMapWidget mineField;
    private int timer;
    private int resetCountdown = 100;

    public MinesweeperApp() {
        super("minesweeper");
    }

    @Override
    public AbstractApplication initApp() {
        mineField = new MineMapWidget(20, 12, 40);
        this.addWidget(mineField);
        this.addWidget(new SimpleTextWidget(333 / 6, 10, "", 0xFFCCCCCC, this::getFlagsPercentage, true));
        this.addWidget(new SimpleTextWidget(333 / 8 * 5, 10, "", 0xFFCCCCCC, this::getStatus, true));

        return this;
    }

    @Override
    public boolean canOpenMenuOnEdge() {
        return false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if(mineField.hasWon() || mineField.hasLost()) {
            if(mineField.hasWon()) {
                mineField.notifyWon();
            }
            resetCountdown--;
        } else
            timer++;
        if (resetCountdown == 0) {
            mineField.resetData();
            resetCountdown = 100;
            timer = 0;
        }
    }

    public String getFlagsPercentage() {
        return mineField.flagsPlaced + "/" + mineField.mineCount;
    }

    public String getStatus() {
        return resetCountdown == 100 ?
                (timer / 20) + " seconds elapsed" : // Normal
                mineField.hasLost() ?
                        "You lost. Game will restart in " + resetCountdown / 20 + " seconds." : // Losing condition
                        "You won in " + (timer / 20) + " seconds! Game will restart in " + resetCountdown / 20; // Winning condition
    }

    @Override
    public boolean isClientSideApp() {
        return true;
    }
}
