package src.launcher;

import javax.swing.JFrame;

import src.ui.panels.launcher.AddressPortRequester;

public class LauncherWindow extends JFrame {

    private static final int DEFAULT_LAUNCHER_WINDOW_WIDTH = 500;
    private static final int DEFAULT_LAUNCHER_WINDOW_HEIGHT = 300;
    
    public LauncherWindow() {
        this(DEFAULT_LAUNCHER_WINDOW_WIDTH, DEFAULT_LAUNCHER_WINDOW_HEIGHT);
    }

    public LauncherWindow(int width, int height) {
        super();

        add(new AddressPortRequester());

        setTitle("Launcher window");
        setSize(width, height);
        setLocationRelativeTo(null); // centers the window
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}