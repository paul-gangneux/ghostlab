package ui;

import javax.swing.JLabel;

import model.GameInfo;

public class GhostCounter extends JLabel {

    private int ghostsLeft;
    private Object lock = new Object();
    
    public GhostCounter() {
        super();
        ghostsLeft = GameInfo.getCurrentGameInfo().getNbGhosts();
        setText("Ghosts left : " + ghostsLeft);
    }

    public void decrease(int amount) {
        synchronized (lock) {
            ghostsLeft -= amount;
        }
        setText("Ghosts left : " + ghostsLeft);
    }
}
