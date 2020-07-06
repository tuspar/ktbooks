package main.services.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.controlsfx.control.Notifications;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;
import org.dizitart.no2.objects.ObjectRepository;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

import static org.dizitart.no2.objects.filters.ObjectFilters.eq;

@Indices({
        @Index(value = "id", type = IndexType.Unique)
})
public class Settings {
    public static final String VERSION = "3.0";
    private static Settings settings;
    //Google Drive Settings
    private boolean driveBackupStatus;
    private boolean driveSignedIn;
    private String driveUsername;
    private int driveUpdateFrequency;
    private String driveRefreshToken;
    //Auto Backup Settings
    private String autoBackupPath;
    private boolean autoBackupStatus;

    //Password
    private String username;
    private String password;

    //Global
    private DecimalFormat numberFormat;

    @Id
    private int id;

    //Getters & Setters

    public static Settings getInstance() {
        if (settings == null) {
            Nitrite db = Database.getInstance();
            ObjectRepository<Settings> repo = db.getRepository(Settings.class);
            settings = repo.find().firstOrDefault();
            if (settings == null) {
                settings = new Settings();
                settings.setDriveBackupStatus(false);
                settings.setAutoBackupPath("");
                settings.setAutoBackupStatus(false);
                settings.setDriveUpdateFrequency(14);
                settings.setUsername("");
                settings.setPassword("");
                settings.setDriveSignedIn(false);
                settings.setDriveUsername("");
                settings.setDriveRefreshToken("");
                settings.setNumberFormat(new DecimalFormat("#0.00"));
                settings.save();
            }
        }
        return settings;
    }

    private static JsonNode request(String url, String urlParameters, String type) {
        HttpURLConnection con = null;
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        JsonNode node = null;
        try {
            URL myurl = new URL(url);
            con = (HttpURLConnection) myurl.openConnection();

            con.setDoOutput(true);
            con.setRequestMethod(type);
            con.setRequestProperty("User-Agent", "Java client");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postData);
            }

            StringBuilder content;

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {

                String line;
                content = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }
            node = new ObjectMapper().readTree(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert con != null;
            con.disconnect();
        }
        return node;
    }

    //Update
    public static boolean updateRequired() {
        try {
            String url = "https://github.com/tuspar/ktbooks/releases/latest";
            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setReadTimeout(8000);
            conn.getInputStream();
            String update = conn.getURL().toString();
            return !update.endsWith(VERSION);
        } catch (Exception e) {
            DisplayInterface.confirmDialog("Error", "Error contacting update server");
            return false;
        }
    }

    public boolean isDriveBackupStatus() {
        return driveBackupStatus;
    }

    public void setDriveBackupStatus(boolean driveBackupStatus) {
        this.driveBackupStatus = driveBackupStatus;
    }

    public int getDriveUpdateFrequency() {
        return driveUpdateFrequency;
    }

    public void setDriveUpdateFrequency(int driveUpdateFrequency) {
        this.driveUpdateFrequency = driveUpdateFrequency;
    }

    public String getAutoBackupPath() {
        return autoBackupPath;
    }

    public void setAutoBackupPath(String autoBackupPath) {
        this.autoBackupPath = autoBackupPath;
    }

    public boolean isAutoBackupStatus() {
        return autoBackupStatus;
    }

    public void setAutoBackupStatus(boolean autoBackupStatus) {
        this.autoBackupStatus = autoBackupStatus;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isDriveSignedIn() {
        return driveSignedIn;
    }

    public void setDriveSignedIn(boolean driveSignedIn) {
        this.driveSignedIn = driveSignedIn;
    }

    public String getDriveUsername() {
        return driveUsername;
    }

    public void setDriveUsername(String driveUsername) {
        this.driveUsername = driveUsername;
    }

    public String getDriveRefreshToken() {
        return driveRefreshToken;
    }

    public void setDriveRefreshToken(String driveRefreshToken) {
        this.driveRefreshToken = driveRefreshToken;
    }

    public DecimalFormat getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(DecimalFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public void save() {
        Nitrite db = Database.getInstance();
        ObjectRepository<Settings> repo = db.getRepository(Settings.class);
        this.id = 0;
        if (repo.find().firstOrDefault() == null) {
            repo.insert(this);
            Notifications.create().title("Saved Settings")
                    .text("Settings were successfully saved!").showInformation();
        } else {
            repo.update(eq("id", this.id), this);
            Notifications.create().title("Saved Settings")
                    .text("Settings were successfully saved!").showInformation();
        }
    }

    //Gdrive
    public void gdriveLogin() {
        //Backend Processing
        new Thread(() -> {
            ServerSocket serverSocket;
            String authorizationCode = null;
            //Get authentication code
            try {
                serverSocket = new ServerSocket(9004);
                serverSocket.setSoTimeout(300000); //5 minute login timeout
                Socket socket = serverSocket.accept();

                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

                int _byte;
                StringBuilder requestBuilder = new StringBuilder();
                while ((_byte = inputStream.read()) >= 0) {
                    requestBuilder.append((char) _byte);
                    int indexOfCode = requestBuilder.indexOf("code");
                    if (indexOfCode != -1 && requestBuilder.indexOf("&", indexOfCode) != -1) {
                        authorizationCode = requestBuilder.substring(indexOfCode + 5, requestBuilder.indexOf("&", indexOfCode));
                        break;
                    }
                }

                //Auto close webpage
                String html = "<!doctype html><html><head><script>window.onload = function getInstance() {" +
                        "window.open('', '_self', '');window.close();};</script></head><body><h2>" +
                        "Please close this webpage</h2></body></html>";
                final String CRLF = "\n\r";
                String response = "HTTP/1.1 200 OK" + CRLF +
                        "Content-Length: " + html.getBytes().length + CRLF +
                        CRLF +
                        html +
                        CRLF + CRLF;

                outputStream.write(response.getBytes());

                inputStream.close();
                outputStream.close();
                socket.close();
                serverSocket.close();
            } catch (IOException e) {
                DisplayInterface.confirmDialog("Google Drive Sign-In", "Request timed out");
            }
            //Get refresh token
            if (authorizationCode != null) {
                setDriveSignedIn(true);
                String url = "https://oauth2.googleapis.com/token";
                String urlParameters = "code=" + authorizationCode +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&redirect_uri=http://127.0.0.1:9004&grant_type=authorization_code";
                JsonNode node = request(url, urlParameters, "POST");

                String accessToken = node.get("access_token").asText();
                setDriveRefreshToken(node.get("refresh_token").asText());

                String emailUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
                String emailUrlParameters = "access_token=" + accessToken;
                JsonNode emailNode = request(emailUrl, emailUrlParameters, "GET");
                //System.out.println("Email: " + node.get("email"));
                System.out.println(emailNode.toString());
                save();
            }
        }).start();
        //Opens login page in the browser
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(URI.create("https://accounts.google.com/o/oauth2/v2/auth?" +
                        "scope=https://www.googleapis.com/auth/drive.file%20email&" +
                        "access_type=offline&" +
                        "include_granted_scopes=true&" +
                        "response_type=code&" +
                        "state=state_parameter_passthrough_value&" +
                        "redirect_uri=http%3A//127.0.0.1%3A9004&" +
                        "client_id=" + clientId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //ID Management
    public int getCount(Class c) {
        ObjectRepository repo = Database.getInstance().getRepository(c);
        return 99 + repo.find().size();
    }

    public enum Template {
        INVOICE("Invoice", "assets/invoice.docx"),
        QUOTATION("Quotation", "assets/quotation"),
        DO("Delivery Order", "assets/do.docx"),
        SOA_PERSONAL("Personal Statement of Account", "assets/soaPersonal.docx"),
        SOA_NOMINAL("Nominal Statement of Account", "assets/soaNominal.docx"),
        RECEIPT("Receipt", "assets/receipt.docx");

        private String label;
        private String path;

        Template(String label, String path) {
            this.label = label;
            this.path = path;
        }

        @Override
        public String toString() {
            return label;
        }

        public String getLabel() {
            return label;
        }

        public String getPath() {
            return path;
        }
    }
}
