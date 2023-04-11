import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;
import org.json.*;
public class Client {
    private static ServerClient client;

    public static void main(String[] args) throws IOException {
        byte[] buffer= new byte[10];
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
                if (text.startsWith("/")) {
                    String[] words = command.split(" ");
                    if (command.startsWith("join")) {
                        if (words.length == 3) {
                            String ipAddress = words[1];
                            int port = Integer.parseInt(words[2]);
                            try {
                                InetAddress inetAddress = InetAddress.getByName(ipAddress);
                                client = new ServerClient(datagramSocket, inetAddress, username, textArea);
                                client.setHasJoined(true);
                                client.receiveMessages();
                            } catch (UnknownHostException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }else if (command.startsWith("disconnect")) {
                        client.setHasJoined(false);
                    } else if (command.startsWith("username")) {
                        client.setUsername(words[1]);
                        client.sendMessage(text);
                    } else {
                        System.out.println("Unknown command: " + command);
                    }
                } else {
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
    private boolean hasJoined = false;

    public ServerClient(DatagramSocket datagramSocket, InetAddress inetAddress, String username, JTextArea textArea) {
        this.datagramSocket = datagramSocket;
        this.inetAddress = inetAddress;
        this.username = username;
        this.textArea = textArea;
    }

    public void setHasJoined(boolean hasJoined) {
        this.hasJoined = hasJoined;
    }

    public void setUsername(String username){
        this.username=username;
    }

    public void sendMessage(String message) {
        if (!hasJoined) {
            System.out.println("You must join the server before sending messages.");
            textArea.append("You must join the server before sending messages.\n");
            return;
        }
        Thread sendThread = new Thread(() -> {
            try {
                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("username", username);
                jsonMessage.put("message", message);
                String messageToSend = jsonMessage.toString();
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
                    String jsonMessage = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                    JSONObject messageFromServer = new JSONObject(jsonMessage);
                    String username = messageFromServer.getString("username");
                    String message = messageFromServer.getString("message");
                    System.out.println(username + ": " + message);
                    textArea.append(username + ": " + message + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        receiveThread.start();

    }
}