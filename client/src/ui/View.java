package ui;

import javax.swing.JFrame;

import client.Client;

public class View {
    private Client c;

    private JFrame currentWindow;

    public View(Client c) {
        this.c = c;
    }

    public void switchToWindow(JFrame window) {
        if (currentWindow != null) { // First window of the UI
            currentWindow.dispose();
        }
        currentWindow = window;
        currentWindow.setVisible(true);
    }
}