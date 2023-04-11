import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;

public class Client {
    private static ServerClient client;

    public static void main(String[] args) throws SocketException, UnknownHostException {
        DatagramSocket datagramSocket = new DatagramSocket();
        InetAddress inetAddress = InetAddress.getByName("localhost");
        String username = "user";

        JFrame frame = new JFrame("Chat app");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 400);

        JTextArea textArea = new JTextArea(20, 20);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        JLabel label = new JLabel("Enter text:");
        JTextField textField = new JTextField(20);
        JButton button= new JButton("Submit");
        JPanel panel = new JPanel();
        panel.add(scrollPane);
        panel.add(label);
        panel.add(textField);
        panel.add(button);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = textField.getText();
                String command = text.substring(1);
                // Here you can save the text to a file or database, for example
                if (text.startsWith("/")) {
                    String[] words = command.split(" ");
                    if (command.startsWith("join")) {

                        if (words.length == 3) {
                        }

                    } else if (command.startsWith("username")) {
                        client.setUsername(words[1]);
                    } else {
                        System.out.println("Unknown command: " + command);
                    }
                }else {
                    System.out.println("You entered: " + text);
                    client.sendMessage(text);
                }
                textField.setText("");
            }
        });
        frame.add(panel);
        frame.setVisible(true);

        client = new ServerClient(datagramSocket, inetAddress, username, textArea);
        client.receiveMessages();
    }
}

class ServerClient {
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[1024];
    private InetAddress inetAddress;
    private String username;
    private JTextArea textArea;

    public ServerClient(DatagramSocket datagramSocket, InetAddress inetAddress, String username, JTextArea textArea) {
        this.datagramSocket = datagramSocket;
        this.inetAddress = inetAddress;
        this.username = username;
        this.textArea = textArea;
    }
    public void setUsername(String username){
        this.username=username;
    }
    public void sendMessage(String message) {
        Thread sendThread = new Thread(() -> {
            try {
                String messageToSend = username + ": " + message;
                buffer = messageToSend.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, inetAddress, 4000);
                datagramSocket.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        sendThread.start();
    }

    public void receiveMessages() {
        Thread receiveThread = new Thread(() -> {
            while (true) {
                try {
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(datagramPacket);
                    String messageFromServer = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                    System.out.println(messageFromServer);
                    textArea.append(messageFromServer + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        receiveThread.start();
    }
}