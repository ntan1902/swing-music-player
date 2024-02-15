import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(SwingAudioPlayer::new);
    }
}
