package main;

import com.jfoenix.controls.JFXSpinner;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;

public class StatusController {
    @FXML
    private JFXSpinner progressMeter;

    @FXML
    private Button updateButton;

    @FXML
    private Text statusText;

    @FXML
    private Button cancelButton;

    private Thread download = new Thread(this::download);

    @FXML
    void cancelAction() {
        statusText.setText("Cancelling operations");
        download.interrupt();
        File file = new File("data/download.jar");
        if (file.exists()) {
            file.delete();
        }
        File ktbooks = new File("KTBooks.jar");
        if (Desktop.isDesktopSupported() && ktbooks.exists()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(ktbooks);
            } catch (IOException e) {
                System.out.println("Critical error");
            }
        }
        System.exit(0);
    }

    @FXML
    void updateAction() {
        updateButton.setDisable(true);
        download.start();
    }

    private void download() {
        double fileSize;
        String destination = "data/download.jar";
        String source = "KTBooks.jar";
        try {
            statusText.setText("Getting latest version");
            String latest = "https://github.com/tuspar/ktbooks/releases/latest";
            URL obj = new URL(latest);
            HttpURLConnection redirect = (HttpURLConnection) obj.openConnection();
            redirect.setReadTimeout(8000);
            redirect.getInputStream();
            String update = redirect.getURL().toString();
            redirect.disconnect();
            URL url = new URL("https://github.com/tuspar/ktbooks/releases/download/" + update.substring(update.length() - 5) + "/KTBooks.jar"); //New File Link

            //Get file size
            statusText.setText("Getting meta-data");
            URLConnection conn = null;
            try {
                conn = url.openConnection();
                fileSize = conn.getContentLength();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (conn instanceof HttpURLConnection) {
                    ((HttpURLConnection) conn).disconnect();
                }
            }

            //Download File
            statusText.setText("Downloading application");
            BufferedInputStream in = new BufferedInputStream(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(destination);
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            double counter = 0;

            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                counter += 1024;
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                progressMeter.setProgress((counter / fileSize));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        statusText.setText("Updating binaries");
        cancelButton.setDisable(true);
        new File(source).delete();
        try {
            FileChannel sfc = new RandomAccessFile(destination, "r").getChannel();
            FileChannel dfc = new RandomAccessFile(source, "rw").getChannel();

            dfc.transferFrom(sfc, 0, sfc.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        new File(destination).delete();
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(new File(source));
            } catch (IOException e) {
                System.out.println("Critical error");
            }
        }
        System.exit(0);
    }
}
