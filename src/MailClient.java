import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/* $Id: MailClient.java,v 1.7 1999/07/22 12:07:30 kangasha Exp $ */

/**
 * A simple mail client with a GUI for sending mail.
 *
 * @author Jussi Kangasharju
 */
public class MailClient extends JFrame {

    /* The stuff for the GUI. */
    private JButton btSend = new JButton("Send");
    private JButton btClear = new JButton("Clear");
    private JButton btQuit = new JButton("Quit");
    private JLabel serverLabel = new JLabel("SMTP Server:");
    private JTextField serverField = new JTextField("", 40);
    private JLabel usernameLabel = new JLabel("Username:");
    private JTextField usernameField = new JTextField("", 20);
    private JLabel passwordLabel = new JLabel("Password:");
    private JPasswordField passwordField = new JPasswordField("", 20);
    private JLabel fromLabel = new JLabel("From:");
    private JTextField fromField = new JTextField("", 40);
    private JLabel toLabel = new JLabel("To: (,: 分隔)");
    private JTextField toField = new JTextField("", 40);
    private JLabel subjectLabel = new JLabel("Subject:");
    private JTextField subjectField = new JTextField("", 40);
    private JLabel messageLabel = new JLabel("Message:");
    private JTextArea messageText = new JTextArea(10, 40);

    /**
     * Create a new MailClient window with fields for entering all
     * the relevant information (From, To, Subject, and message).
     */
    public MailClient() {
        super("Java Mailclient");
        
        /* Create panels for holding the fields. To make it look nice,
        create an extra panel for holding all the child panels. */
        JPanel serverPanel = new JPanel(new BorderLayout(5, 0));
        JPanel credentialsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        JPanel usernamePanel = new JPanel(new BorderLayout(5, 0));
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        JPanel fromPanel = new JPanel(new BorderLayout(5, 0));
        JPanel toPanel = new JPanel(new BorderLayout(5, 0));
        JPanel subjectPanel = new JPanel(new BorderLayout(5, 0));
        JPanel messagePanel = new JPanel(new BorderLayout(5, 5));

        serverPanel.add(serverLabel, BorderLayout.WEST);
        serverPanel.add(serverField, BorderLayout.CENTER);

        usernamePanel.add(usernameLabel, BorderLayout.WEST);
        usernamePanel.add(usernameField, BorderLayout.CENTER);
        passwordPanel.add(passwordLabel, BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        credentialsPanel.add(usernamePanel);
        credentialsPanel.add(passwordPanel);

        fromPanel.add(fromLabel, BorderLayout.WEST);
        fromPanel.add(fromField, BorderLayout.CENTER);
        toPanel.add(toLabel, BorderLayout.WEST);
        toPanel.add(toField, BorderLayout.CENTER);
        subjectPanel.add(subjectLabel, BorderLayout.WEST);
        subjectPanel.add(subjectField, BorderLayout.CENTER);
        messagePanel.add(messageLabel, BorderLayout.NORTH);    
        messagePanel.add(new JScrollPane(messageText), BorderLayout.CENTER);

        JPanel fieldPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        fieldPanel.add(serverPanel);
        fieldPanel.add(credentialsPanel);
        fieldPanel.add(fromPanel);
        fieldPanel.add(toPanel);
        fieldPanel.add(subjectPanel);

        /* Create a panel for the buttons and add listeners to the buttons. */
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0));
        btSend.addActionListener(new SendListener());
        btClear.addActionListener(new ClearListener());
        btQuit.addActionListener(new QuitListener());
        buttonPanel.add(btSend);
        buttonPanel.add(btClear);
        buttonPanel.add(btQuit);
        
        /* Add, pack, and show. */
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));  // 添加垂直間距
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // 添加邊距
        mainPanel.add(fieldPanel, BorderLayout.NORTH);
        mainPanel.add(messagePanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setVisible(true);
    }

    static public void main(String argv[]) {
        new MailClient();
    }

    /* Handler for the Send-button. */
    class SendListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            System.out.println("Sending mail");
            
            /* Check that we have the local mailserver */
            if ((serverField.getText()).equals("")) {
                showResultDialog(false, "Need name of local mailserver!");
                return;
            }

            /* Check that we have the sender and recipient. */
            if((fromField.getText()).equals("")) {
                showResultDialog(false, "Need sender!");
                return;
            }
            if((toField.getText()).equals("")) {
                showResultDialog(false, "Need recipient!");
                return;
            }

            // 檢查收件人數量
            String[] recipients = toField.getText().split(",");
            if (recipients.length > 2) {
                showResultDialog(false, "警告：您輸入了超過兩位收件人。請減少收件人數量。");
                return;
            }

            // 檢查寄件人地址的有效性
            Message senderMessage = new Message(fromField.getText(), "test@test.com", "", "");
            if (!senderMessage.isValid()) {
                showResultDialog(false, "無效的寄件人地址：" + fromField.getText());
                return;
            }

            // 檢查每個收件人地址的有效性
            for (String recipient : recipients) {
                Message recipientMessage = new Message("test@test.com", recipient.trim(), "", "");
                if (!recipientMessage.isValid()) {
                    showResultDialog(false, "無效的收件人地址：" + recipient.trim());
                    return;
                }
            }

            // 繼續處理郵件發送
            for (String recipient : recipients) {
                Message mailMessage = new Message(fromField.getText(), 
                                        recipient.trim(), 
                                        subjectField.getText(), 
                                        messageText.getText());

                // 創建信封並發送郵件
                try {
                    Envelope envelope = new Envelope(mailMessage, serverField.getText());
                    SMTPConnection connection = new SMTPConnection(
                        serverField.getText(),
                        usernameField.getText(),
                        new String(passwordField.getPassword())
                    );
                    connection.send(envelope);
                    connection.close();
                } catch (UnknownHostException e) {
                    showResultDialog(false, "Unknown host: " + e.getMessage());
                    return;
                } catch (IOException error) {
                    showResultDialog(false, "Sending failed: " + error.getMessage());
                    return;
                }
            }

            showResultDialog(true, "Mail sent successfully!");
        }
    }

    /* Clear the fields on the GUI. */
    class ClearListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.out.println("Clearing fields");
			usernameField.setText("");
			passwordField.setText("");
            fromField.setText("");
            toField.setText("");
            subjectField.setText("");
            messageText.setText("");
        }
    }

    /* Quit. */
    class QuitListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    private void showResultDialog(boolean success, String message) {
        String title = success ? "Success" : "Error";
        int messageType = success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
}
