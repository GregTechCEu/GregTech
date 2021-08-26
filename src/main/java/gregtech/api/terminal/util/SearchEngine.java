package gregtech.api.terminal.util;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public  class SearchEngine<T> {
    private final ISearch<T> search;
    private final Consumer<T> result;
    private Thread thread;

    public SearchEngine(@Nonnull ISearch<T> search, @Nonnull Consumer<T> result){
        this.search = search;
        this.result = result;
    }

    public void searchWord(String word) {
        dispose();
        thread = new Thread(()-> search.search(word, result));
        thread.start();
    }

    public boolean isSearching() {
        return thread != null && thread.isAlive();
    }

    public void dispose() {
        if (isSearching()) {
            if (search.isManualInterrupt()) {
                thread.interrupt();
            } else {
                thread.stop();
            }
        }
        thread = null;
    }

}
