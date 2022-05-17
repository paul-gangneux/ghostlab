package ui;

import javax.swing.JFrame;

import client.Client;
import ui.panels.launcher.AddressPortRequester;

public class LauncherWindow extends JFrame {

    private Client client;

    private static final int DEFAULT_LAUNCHER_WINDOW_WIDTH = 500;
    private static final int DEFAULT_LAUNCHER_WINDOW_HEIGHT = 300;
    
    public LauncherWindow(Client client) {
        this(client, DEFAULT_LAUNCHER_WINDOW_WIDTH, DEFAULT_LAUNCHER_WINDOW_HEIGHT);
    }

    public LauncherWindow(Client client, int width, int height) {
        super();
        this.client = client;
        add(new AddressPortRequester(client));

        setTitle("Launcher window");
        setSize(width, height);
        setLocationRelativeTo(null); // centers the window
        setResizable(false);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    }
}