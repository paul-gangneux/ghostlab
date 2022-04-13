package src.launcher;

public class Launcher {
    public static void main(String [] args)  {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LauncherWindow().setVisible(true);
            }
        });
    }
}