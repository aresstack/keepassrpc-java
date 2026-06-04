package com.aresstack.keepassrpc.demo;

import com.aresstack.keepassrpc.config.InMemoryKeePassRpcSettingsRepository;
import com.aresstack.keepassrpc.config.KeePassRpcSettings;
import com.aresstack.keepassrpc.ui.KeePassRpcSettingsPanel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Launch the extracted KeePassRPC settings panel without MainframeMate.
 */
public final class KeePassRpcSettingsDemo {
    private KeePassRpcSettingsDemo() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                start();
            }
        });
    }

    private static void start() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Keep Swing default look and feel.
        }

        final InMemoryKeePassRpcSettingsRepository repository = new InMemoryKeePassRpcSettingsRepository(KeePassRpcSettings.defaults());
        final KeePassRpcSettingsPanel panel = new KeePassRpcSettingsPanel(null, repository);

        JFrame frame = new JFrame("KeePassRPC Settings Extract");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));
        frame.add(panel, BorderLayout.CENTER);
        frame.add(createButtons(panel), BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JPanel createButtons(final KeePassRpcSettingsPanel panel) {
        JPanel buttons = new JPanel();
        JButton saveButton = new JButton("Übernehmen");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                panel.applySettings();
                JOptionPane.showMessageDialog(panel, "Einstellungen wurden übernommen.");
            }
        });
        buttons.add(saveButton);
        return buttons;
    }
}
