package com.aresstack.keepassrpc.ui;

import com.aresstack.keepassrpc.client.KeePassRpcPairingDialog;
import com.aresstack.keepassrpc.config.KeePassRpcSettings;
import com.aresstack.keepassrpc.config.KeePassRpcSettingsRepository;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.UUID;

/**
 * Optional Swing settings panel for KeePassRPC configuration and pairing.
 * <p>
 * The panel is an adapter around the configuration and pairing APIs. Applications
 * can embed it in a Swing settings dialog, or ignore it and call the pairing
 * service directly.
 */
public class KeePassRpcSettingsPanel extends JPanel {
    private final Component parentComponent;
    private final KeePassRpcSettingsRepository repository;

    private final JTextField databaseField;
    private final JTextField entryTitleField;
    private final JComboBox<String> accessMethodBox;
    private final JTextField rpcHostField;
    private final JSpinner rpcPortSpinner;
    private final JPasswordField rpcKeyField;
    private final JComboBox<String> rpcOriginSchemeBox;
    private final JTextField rpcOriginIdField;
    private final JPanel rpcPanel;
    private final JButton browseDatabaseButton;

    public KeePassRpcSettingsPanel(Component parentComponent, KeePassRpcSettingsRepository repository) {
        this.parentComponent = parentComponent;
        this.repository = repository;

        KeePassRpcSettings settings = repository.load();
        this.databaseField = new JTextField(settings.getDatabasePath(), 30);
        this.entryTitleField = new JTextField(settings.getEntryTitle(), 20);
        this.accessMethodBox = new JComboBox<String>(new String[]{"PowerShell", "KeePassRPC"});
        this.rpcHostField = new JTextField(settings.getEffectiveRpcHost(), 15);
        this.rpcPortSpinner = new JSpinner(new SpinnerNumberModel(settings.getRpcPort(), 1, 65535, 1));
        this.rpcKeyField = new JPasswordField(settings.getRpcKey(), 30);
        this.rpcOriginSchemeBox = createOriginSchemeBox(settings);
        this.rpcOriginIdField = new JTextField(settings.getRpcOriginId(), 18);
        this.rpcPanel = new JPanel(new GridBagLayout());
        this.browseDatabaseButton = new JButton("…");

        this.accessMethodBox.setSelectedIndex(settings.isRpcAccessMethod() ? 1 : 0);
        buildLayout();
        updateAccessMethodVisibility();
    }

    public KeePassRpcSettings collectSettings() {
        KeePassRpcSettings settings = repository.load();
        settings.setDatabasePath(databaseField.getText().trim());
        settings.setEntryTitle(entryTitleField.getText().trim());
        settings.setAccessMethod(accessMethodBox.getSelectedIndex() == 1 ? "RPC" : "POWERSHELL");
        settings.setRpcHost(rpcHostField.getText().trim());
        settings.setRpcPort(((Number) rpcPortSpinner.getValue()).intValue());
        settings.setRpcKey(new String(rpcKeyField.getPassword()).trim());
        settings.setRpcOriginScheme(String.valueOf(rpcOriginSchemeBox.getSelectedItem()).trim());
        settings.setRpcOriginId(rpcOriginIdField.getText().trim());
        return settings;
    }

    public void applySettings() {
        repository.save(collectSettings());
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Sicherheit · KeePassRPC"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 6, 4, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 0;

        addAccessMethodRow(form, constraints);
        addDatabaseRow(form, constraints);
        addEntryTitleRow(form, constraints);
        addRpcPanel(form, constraints);
        add(form, BorderLayout.NORTH);
    }

    private void addAccessMethodRow(JPanel form, GridBagConstraints constraints) {
        constraints.gridx = 0;
        constraints.weightx = 0;
        form.add(new JLabel("Zugriffsmethode:"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        form.add(accessMethodBox, constraints);

        accessMethodBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updateAccessMethodVisibility();
            }
        });
    }

    private void addDatabaseRow(JPanel form, GridBagConstraints constraints) {
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.weightx = 0;
        form.add(new JLabel("Datenbank (.kdbx):"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        form.add(databaseField, constraints);

        constraints.gridx = 2;
        constraints.weightx = 0;
        browseDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                browseDatabase();
            }
        });
        form.add(browseDatabaseButton, constraints);
    }

    private void addEntryTitleRow(JPanel form, GridBagConstraints constraints) {
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.weightx = 0;
        form.add(new JLabel("Eintragstitel:"), constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        form.add(entryTitleField, constraints);
        constraints.gridwidth = 1;
    }

    private void addRpcPanel(JPanel form, GridBagConstraints constraints) {
        buildRpcPanel();
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 3;
        constraints.weightx = 1;
        form.add(rpcPanel, constraints);
        constraints.gridwidth = 1;
    }

    private void buildRpcPanel() {
        rpcPanel.setBorder(BorderFactory.createTitledBorder("KeePassRPC"));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(3, 6, 3, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 0;

        addRpcHostRow(constraints);
        addRpcPortRow(constraints);
        addRpcKeyRow(constraints);
        addRpcOriginRow(constraints);
        addPairingButtonRow(constraints);
    }

    private void addRpcHostRow(GridBagConstraints constraints) {
        constraints.gridx = 0;
        constraints.weightx = 0;
        rpcPanel.add(new JLabel("Host:"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        rpcPanel.add(rpcHostField, constraints);
    }

    private void addRpcPortRow(GridBagConstraints constraints) {
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.weightx = 0;
        rpcPanel.add(new JLabel("Port:"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        rpcPanel.add(rpcPortSpinner, constraints);
    }

    private void addRpcKeyRow(GridBagConstraints constraints) {
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.weightx = 0;
        rpcPanel.add(new JLabel("SRP-Schlüssel:"), constraints);

        JPanel keyPanel = new JPanel(new BorderLayout(4, 0));
        keyPanel.add(rpcKeyField, BorderLayout.CENTER);
        keyPanel.add(createShowKeyButton(), BorderLayout.EAST);

        constraints.gridx = 1;
        constraints.weightx = 1;
        rpcPanel.add(keyPanel, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        JLabel hint = new JLabel("<html><small>Den Schlüssel erhalten Sie aus KeePass beim ersten Verbindungsaufbau.</small></html>");
        hint.setForeground(Color.GRAY);
        rpcPanel.add(hint, constraints);
        constraints.gridwidth = 1;
    }

    private void addRpcOriginRow(GridBagConstraints constraints) {
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.weightx = 0;
        rpcPanel.add(new JLabel("Origin:"), constraints);

        JPanel originPanel = new JPanel(new GridBagLayout());
        GridBagConstraints originConstraints = new GridBagConstraints();
        originConstraints.insets = new Insets(0, 0, 0, 3);
        originConstraints.gridy = 0;
        originConstraints.gridx = 0;
        originPanel.add(rpcOriginSchemeBox, originConstraints);

        originConstraints.gridx = 1;
        originConstraints.weightx = 1;
        originConstraints.fill = GridBagConstraints.HORIZONTAL;
        originPanel.add(rpcOriginIdField, originConstraints);

        originConstraints.gridx = 2;
        originConstraints.weightx = 0;
        originConstraints.fill = GridBagConstraints.NONE;
        originPanel.add(createOriginUuidButton(), originConstraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        rpcPanel.add(originPanel, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        JLabel hint = new JLabel("<html><small>KeePassRPC prüft den Origin-Header. Browser-Extension-Schemata sind üblich.</small></html>");
        hint.setForeground(Color.GRAY);
        rpcPanel.add(hint, constraints);
        constraints.gridwidth = 1;
    }

    private void addPairingButtonRow(GridBagConstraints constraints) {
        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        JButton pairingButton = new JButton("Pairing starten…");
        pairingButton.setToolTipText("Startet den KeePassRPC-Pairing-Dialog zum automatischen Setzen des SRP-Schlüssels");
        pairingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                startPairing();
            }
        });
        rpcPanel.add(pairingButton, constraints);
        constraints.gridwidth = 1;
    }

    private JButton createShowKeyButton() {
        final char defaultEcho = rpcKeyField.getEchoChar();
        final JButton button = new JButton("Anzeigen");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (rpcKeyField.getEchoChar() == 0) {
                    rpcKeyField.setEchoChar(defaultEcho);
                    button.setText("Anzeigen");
                } else {
                    rpcKeyField.setEchoChar((char) 0);
                    button.setText("Verbergen");
                }
            }
        });
        return button;
    }

    private JButton createOriginUuidButton() {
        JButton button = new JButton("UUID");
        button.setToolTipText("Zufällige Origin-ID generieren");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                rpcOriginIdField.setText(UUID.randomUUID().toString());
            }
        });
        return button;
    }

    private void browseDatabase() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("KeePass-Datenbank auswählen");
        chooser.setFileFilter(new FileNameExtensionFilter("KeePass-Datenbank (*.kdbx)", "kdbx"));
        String currentPath = databaseField.getText().trim();
        if (!currentPath.isEmpty()) {
            File currentFile = new File(currentPath);
            if (currentFile.getParentFile() != null && currentFile.getParentFile().isDirectory()) {
                chooser.setCurrentDirectory(currentFile.getParentFile());
            }
        }
        if (chooser.showOpenDialog(parentComponent) == JFileChooser.APPROVE_OPTION) {
            databaseField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void startPairing() {
        applySettings();
        String key = KeePassRpcPairingDialog.showAndPair(repository);
        if (key != null && !key.trim().isEmpty()) {
            rpcKeyField.setText(key);
            applySettings();
        }
    }

    private void updateAccessMethodVisibility() {
        boolean rpc = accessMethodBox.getSelectedIndex() == 1;
        rpcPanel.setVisible(rpc);
        databaseField.setEnabled(!rpc);
        browseDatabaseButton.setEnabled(!rpc);
        revalidate();
        repaint();
    }

    private JComboBox<String> createOriginSchemeBox(KeePassRpcSettings settings) {
        JComboBox<String> comboBox = new JComboBox<String>(new String[]{
                "chrome-extension://",
                "moz-extension://",
                "safari-web-extension://",
                "ms-browser-extension://",
                "resource://gre-resources/"
        });
        comboBox.setEditable(true);
        comboBox.setSelectedItem(settings.getRpcOriginScheme());
        return comboBox;
    }
}
