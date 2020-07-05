package main.controllers.ui;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import main.services.backend.Database;
import main.services.backend.DisplayInterface;
import main.services.backend.Settings;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.dialog.ExceptionDialog;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    @FXML
    private JFXTextField gdriveUserField;

    @FXML
    private ToggleSwitch gdriveBackupToggle;

    @FXML
    private JFXTextField gdriveFrequencyField;

    @FXML
    private Button gdriveSignInButton;

    @FXML
    private Button gdriveLogoutButton;

    @FXML
    private JFXTextField usernameField;

    @FXML
    private JFXPasswordField oldPasswordField;

    @FXML
    private JFXPasswordField newPasswordField;

    @FXML
    private JFXPasswordField confirmPasswordField;

    @FXML
    private JFXTextField autoBackupPathField;

    @FXML
    private ToggleSwitch autoBackupToggle;

    @FXML
    private TableView<Settings.Template> templateTable;

    @FXML
    private TableColumn<Settings.Template, String> templateLabelColumn;

    @FXML
    private TableColumn<Settings.Template, String> templatePathColumn;

    @FXML
    private Text versionText;

    @FXML
    void changeUsername() {
        Settings settings = Settings.getInstance();
        settings.setUsername(usernameField.getText());
        settings.save();
    }

    @FXML
    void changePasswordAction() {
        Settings settings = Settings.getInstance();
        if (oldPasswordField.getText().equals(settings.getPassword())) {
            if (newPasswordField.getText().equals(confirmPasswordField.getText())) {
                settings.setPassword(newPasswordField.getText());
                settings.save();
            } else {
                DisplayInterface.confirmDialog("Change Password", "New Password and Confirm Password do not match");
            }
        } else {
            DisplayInterface.confirmDialog(DisplayInterface.ConfirmType.LOGIN);
        }
        oldPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    @FXML
    void changePathAction(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Auto-Backup");
        File selection = directoryChooser.showDialog(templateTable.getScene().getWindow());
        if (selection != null) {
            autoBackupPathField.setText(selection.getAbsolutePath());
        }
    }

    @FXML
    void editFormatAction(ActionEvent event) {
        if (templateTable.getSelectionModel().getSelectedIndex() != -1) {
            try {
                Desktop.getDesktop().open(new File(templateTable.getSelectionModel().getSelectedItem().getPath()));
            } catch (IOException e) {
                new ExceptionDialog(e).showAndWait();
            }
        } else {
            DisplayInterface.confirmDialog(DisplayInterface.ConfirmType.SELECT_ROW);
        }

    }

    @FXML
    void gdriveLogoutAction(ActionEvent event) {

    }

    @FXML
    void gdriveSignInAction(ActionEvent event) {
        Settings.getInstance().gdriveLogin();
    }

    @FXML
    void recoveryAction(ActionEvent event) {

    }

    @FXML
    void saveBackupAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup");
        fileChooser.setInitialFileName(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".db");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Database File", "*.db"));
        File file = fileChooser.showSaveDialog(templateTable.getScene().getWindow());
        if (file != null) {
            try {
                FileChannel sfc = new RandomAccessFile(Database.getPath(), "r").getChannel();
                FileChannel dfc = new RandomAccessFile(file, "rw").getChannel();

                dfc.transferFrom(sfc, 0, sfc.size());
            } catch (IOException e) {
                DisplayInterface.confirmDialog("Error", "Unable to save file to location");
            }

        }
        Notifications.create().title("Saved").text("Backup was Saved!").show();
    }

    @FXML
    void importBackupAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Backup");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database File", "*.db"));
        File file = fileChooser.showOpenDialog(templateTable.getScene().getWindow());
        if (file != null) {
            Database.exit();
            new File(Database.getPath()).delete();
            try {
                FileChannel sfc = new RandomAccessFile(file, "r").getChannel();
                FileChannel dfc = new RandomAccessFile(Database.getPath(), "rw").getChannel();

                dfc.transferFrom(sfc, 0, sfc.size());

            } catch (IOException e) {
                DisplayInterface.confirmDialog("Error", "Unable to import backup");
            }
        }
        DisplayInterface.exit(event);
    }


    @FXML
    void applyBackupSettings(ActionEvent event) {
        Settings settings = Settings.getInstance();
        settings.setDriveBackupStatus(gdriveBackupToggle.isSelected());
        settings.setDriveUpdateFrequency(Integer.parseInt(gdriveFrequencyField.getText()));
        settings.setAutoBackupStatus(autoBackupToggle.isSelected());
        settings.setAutoBackupPath(autoBackupPathField.getText());
        settings.save();
    }

    @FXML
    void exitAction(ActionEvent event) {
        DisplayInterface.confirmExit(event);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Initialize Template Table
        templateLabelColumn.setCellValueFactory(new PropertyValueFactory<>("label"));
        templatePathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        templateTable.getItems().addAll(Settings.Template.values());

        //Load Settings
        Settings settings = Settings.getInstance();
        usernameField.setText(settings.getUsername());
        gdriveBackupToggle.setSelected(settings.isDriveBackupStatus());
        gdriveFrequencyField.setText(String.valueOf(settings.getDriveUpdateFrequency()));
        autoBackupToggle.setSelected(settings.isAutoBackupStatus());
        autoBackupPathField.setText(settings.getAutoBackupPath());
        versionText.setText("Version: " + Settings.VERSION);
    }

    @FXML
    void updateAction() {
        if (Settings.updateRequired()) {
            if (DisplayInterface.confirmDialog("Update", "Update available. Proceed?")) {
                File updater = new File("updater.jar");
                if (updater.exists()) {
                    try {
                        Desktop.getDesktop().open(updater);
                        System.exit(0);
                    } catch (IOException e) {
                        DisplayInterface.confirmDialog("Update", "Updater is corrupt, please contact your system administrator");
                    }
                } else {
                    DisplayInterface.confirmDialog("Update", "Updater file is missing");
                }
            }
        } else {
            DisplayInterface.confirmDialog("Update", "Latest version in use");
        }
    }

}
