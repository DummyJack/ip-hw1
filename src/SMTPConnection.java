// SMTP 狀態碼
// DATA(354), HELO (250), MAIL FROM (250), QUIT (221), RCPT TO (250)

import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/*
 * 實現一個 SMTP 連接，用於發送單封郵件。
 * 使用 SSL 連接以確保安全性。
 */
public class SMTPConnection implements AutoCloseable {

    // 伺服器連接相關的變數
    private static final int SMTP_PORT = 465; // SSL端口
    private static final String CRLF = "\r\n";

    private final SSLSocket connection;
    // Streams for reading and writing the socket
    private final BufferedReader fromServer;
    private final DataOutputStream toServer;

    // Are we connected? Used in close() to determine what to do.
    private boolean isConnected = false;

    /*
     * 創建 SMTPConnection 對象，建立 SSL 連接並初始化用於與服務器通信的輸入輸出流，進行身份驗證
     */
    public SMTPConnection(String SMTP_HOST, String username, String password) throws IOException {
        System.out.println("連接 " + SMTP_HOST  + ":" + SMTP_PORT);
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        connection = (SSLSocket) sslSocketFactory.createSocket(SMTP_HOST, SMTP_PORT);
        
        fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        toServer = new DataOutputStream(connection.getOutputStream());

        // SMTP 連接，包括驗證伺服器響應、發送 EHLO 命令
        verifyServerResponse(readResponse(), 220);
        sendCommand("EHLO " + InetAddress.getLocalHost().getHostName(), 250);

        // 進行 SMTP 身份驗證
        try {
            sendCommand("AUTH LOGIN", 334);
            sendCommand(Base64.getEncoder().encodeToString(username.getBytes()), 334);
            sendCommand(Base64.getEncoder().encodeToString(password.getBytes()), 235);
        } catch (IOException e) {
            throw new IOException("身份驗證失敗", e);
        }

        isConnected = true;
        System.out.println("連接成功並完成驗證");
    }

    /*
     * 發送郵件
     */
    public void send(Envelope envelope) throws IOException {
        sendCommand("MAIL FROM:<" + envelope.Sender + ">", 250);
        sendCommand("RCPT TO:<" + envelope.Recipient + ">", 250);
        sendCommand("DATA", 354);
        
        // 發送內容，使用 UTF-8 編碼
        byte[] content = (envelope.Message.Headers + CRLF + envelope.Message.Body + CRLF + "." + CRLF)
                         .getBytes(StandardCharsets.UTF_8);
        toServer.write(content);
        toServer.flush();
        verifyServerResponse(readResponse(), 250);
    }

    /*
     * 實現 close()，發送 QUIT 命令並關閉連接
     */
    @Override
    public void close() throws IOException {
        if (isConnected) {
            try {
                sendCommand("QUIT", 221);
            } finally {
                connection.close();
                isConnected = false;
                System.out.println("連接已關閉");
            }
        }
    }

    /*
     * 向伺服器發送 SMTP 命令並驗證響應
     */
    private void sendCommand(String command, int expectedCode) throws IOException {
        // 創建一個用於日誌記錄的 string，如果 string 是 AUTH LOGIN，則只記錄 AUTH LOGIN
        String logCommand = command.startsWith("AUTH LOGIN") ? "AUTH LOGIN" : command;
        System.out.println("客戶端: " + logCommand);
        
        toServer.write((command + CRLF).getBytes());
        toServer.flush();
        verifyServerResponse(readResponse(), expectedCode);
    }

    /*
     * 讀取服務器多行響應
     */
    private String readResponse() throws IOException {
        StringBuilder response = new StringBuilder(); // 創建一個 StringBuilder 對象來存儲完整的服務器響應
        String line; // 每一行響應
        /* 
         * 繼續讀取直到遇到 null(表示流程結束)
         * 或者直到遇到一個長度至少為 4 且第 4 個字符不是 '-' 的行
         */
        do {
            line = fromServer.readLine();
            if (line != null) {
                response.append(line).append(CRLF);
            }
        } while (line != null && (line.length() < 4 || line.charAt(3) == '-'));
        return response.toString().trim();
    }

    /*
     * 驗證 SMTP 服務器的響應
     */
    private void verifyServerResponse(String response, int expectedCode) throws IOException {
        System.out.println("伺服器: " + response);
        // 檢查響應是否以預期程式碼開始
        if (!response.startsWith(String.valueOf(expectedCode))) {
            throw new IOException(response);
        }
    }
}
