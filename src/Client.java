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
        frame.setSize(600, 800);
        JTextArea textArea = new JTextArea(40, 50);
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

                                if(inetAddress.isReachable(port)){
                                    client = new ServerClient(datagramSocket, inetAddress, username, textArea);
                                    client.setHasJoined(true);
                                    client.setPort(port);
                                    text=("Connection to the Message Board\n" +
                                            "Server is successful!\n");
                                    client.sendMessage(text);
                                }else {
                                    textArea.append("Error: Connection to the Message\n" +
                                            "Board Server has failed! Please\n" +
                                            "check IP Address and Port Number.\n");
                                }
                            } catch (UnknownHostException ex) {
                                ex.printStackTrace();
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }else {
                            textArea.append("Not a valid server.\n");
                        }
                    }else if (command.startsWith("leave")) {
                            if (client.isHasJoined()) {
                                if(words.length==1){
                                    client = new ServerClient(datagramSocket, inetAddress, username, textArea);
                                    textArea.append("Connection closed. Thank you!\n");
                                }else{
                                    textArea.append("Error: Command not found.\n");
                                }
                            } else {
                                textArea.append("Error: Disconnection failed. Please\n" +
                                        "connect to the server first.\n");
                            }

                    } else if (command.startsWith("register")) {
                            if (client.isHasJoined()&&!client.isRegistered) {
                                if (words.length==2) {
                                    client.setUsername(words[1]);
                                    client.setRegistered(true);
                                    client.sendMessage(text);
                                }
                                else{
                                    textArea.append("Error: Command not found.\n");
                                }
                            }else{
                                textArea.append("Error: Client is registered\n");
                            }
                    } else if (command.startsWith("msg")) {
                        if(client.isHasJoined()&&client.isRegistered()) {
                            if (words.length >= 3) {
                                String substring = text.substring((words[0].length()+words[1].length()+2));
                                client.sendMessage(words[1], text);
                                textArea.append("[to "+words[1]+"] "+ substring+"\n");
                            } else {
                                textArea.append("Not a valid message.\n");
                            }
                        }
                    } else if (command.startsWith("?")) {
                        if(words.length==1){
                            textArea.append("/join <server_ip_add> <port>\n/leave\n/register <handle>\n/all <message>\n/msg <handle> <message>");
                        }else{
                            textArea.append("Error: Command not found.\n");
                        }
                    } else if (command.startsWith("all")) {
                        if(client.isHasJoined()&&client.isRegistered()) {
                            if(words.length>=2){
                                text = text.substring(5);
                                client.sendMessage(text);
                            }else{
                                textArea.append("Error: Command not found.\n");
                            }
                        }
                    } else {
                        textArea.append("Unknown command: " + command+"\n");
                    }
                } else {
                    textArea.append("No command was inputted.\n");
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
    private int port;
    private String username;
    public boolean isRegistered=false;
    private JTextArea textArea;
    private boolean hasJoined = false;

    public ServerClient(DatagramSocket datagramSocket, InetAddress inetAddress, String username, JTextArea textArea) {
        this.datagramSocket = datagramSocket;
        this.inetAddress = inetAddress;
        this.username = username;
        this.textArea = textArea;
    }

    public ServerClient() {

    }

    public void setHasJoined(boolean hasJoined) {
        this.hasJoined = hasJoined;
    }

    public void setUsername(String username){
        this.username=username;
    }

    public void setPort(int port){this.port=port;}
    public void setRegistered(boolean registered) {
        isRegistered = true;
    }

    public void sendMessage(String message) {

        Thread sendThread = new Thread(() -> {
            try {
                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("username", username);
                jsonMessage.put("message", message);
                String messageToSend = jsonMessage.toString();
                buffer = messageToSend.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, inetAddress, port);
                datagramSocket.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        sendThread.start();
    }
    boolean isHasJoined(){
        if (!hasJoined) {
            textArea.append("You must join the server before sending messages.\n");
            return false;
        }else return true;

    }
    boolean isRegistered(){
        if(!isRegistered){
            textArea.append("You must Register before sending messages.\n");
            return false;
        } else return true;
    }
    public void sendMessage(String recipient, String message) {
        Thread sendThread = new Thread(() -> {
            try {
                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("username", username);
                jsonMessage.put("recipient", recipient);
                jsonMessage.put("message", message);
                String messageToSend = jsonMessage.toString();
                buffer = messageToSend.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, inetAddress, port);
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
                    if(username==this.username){
                        textArea.append(message + "\n");
                    }else {
                        textArea.append("["+username + "]: " + message + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        receiveThread.start();

    }

}