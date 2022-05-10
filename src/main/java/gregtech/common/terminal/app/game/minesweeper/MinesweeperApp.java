package gregtech.common.terminal.app.game.minesweeper;

import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.common.terminal.app.game.minesweeper.widget.MineMapWidget;

public class MinesweeperApp extends AbstractApplication {
    private MineMapWidget mineField;
    private WidgetGroup textGroup;
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
        textGroup = new WidgetGroup(0, 0, 200, 50);
        this.addWidget(textGroup);
        setTextStatus();

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
            setTextStatus();
        } else
            timer++;
        if (resetCountdown == 0) {
            mineField.resetData();
            resetCountdown = 100;
            timer = 0;
            setTextStatus();
        }
    }

    public String getFlagsPercentage() {
        return mineField.flagsPlaced + "/" + mineField.mineCount;
    }

    public void setTextStatus() { //swap widget for localization
        if (resetCountdown == 100) {
            textGroup.clearAllWidgets();
            textGroup.addWidget(new SimpleTextWidget(333 / 8 * 5, 10, "terminal.minesweeper.time", 0xFFCCCCCC, ()->String.valueOf(timer / 20), true)); // Normal
        }
        else if(resetCountdown==99){
            textGroup.clearAllWidgets();
            if(mineField.hasLost()){
                textGroup.addWidget(new SimpleTextWidget(333 / 8 * 5, 10, "terminal.minesweeper.lose", 0xFFCCCCCC, ()->String.valueOf(resetCountdown / 20), true)); // Losing condition
            } else{
                textGroup.addWidget(new SimpleTextWidget(333 / 8 * 5, 10, "terminal.minesweeper.win.1", 0xFFCCCCCC, ()->String.valueOf(timer / 20), true)); // Winning condition
                textGroup.addWidget(new SimpleTextWidget(333 / 8 * 5, 20, "terminal.minesweeper.win.2", 0xFFCCCCCC, ()->String.valueOf(resetCountdown / 20), true));
            }
        }

    }

    @Override
    public boolean isClientSideApp() {
        return true;
    }
}
