package launcher;

public class Launcher {
    public static void main(String [] args)  {
        javax.swing.SwingUtilities.invokeLater( () -> {
                new LauncherWindow().setVisible(true);
        });
    }
}