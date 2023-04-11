import java.net.*;
import java.util.HashMap;
import java.util.Map;
import org.json.*;
public class Server {
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[1024];
    private Map<SocketAddress, String> clients = new HashMap<>();

    public Server(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }
    public void receiveThenSend() {
        while (true) {
            try {
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);
                SocketAddress clientAddress = datagramPacket.getSocketAddress();
                String jsonMessage = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                JSONObject messageFromClient = new JSONObject(jsonMessage);
                String username = messageFromClient.getString("username");
                String recipient = messageFromClient.optString("recipient", null);
                String message = messageFromClient.getString("message");
                if (!clients.containsKey(clientAddress)) {
                    clients.put(clientAddress, username);
                    message = "User " + username + " has joined";
                    recipient = null;
                } else if (message.startsWith("/username")) {
                    String oldName = clients.get(clientAddress);
                    String newName = message.substring(9);
                    clients.put(clientAddress, newName);
                    message = "User " + oldName + " changed name to " + newName;
                    recipient = null;
                }

                JSONObject jsonMessageToSend = new JSONObject();
                jsonMessageToSend.put("username", username);
                jsonMessageToSend.put("message", message);
                String messageToSend = jsonMessageToSend.toString();

                if (recipient == null) {
                    for (SocketAddress client : clients.keySet()) {
                        byte[] messageBytes = messageToSend.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length, client);
                        datagramSocket.send(sendPacket);
                    }
                } else {
                    for (Map.Entry<SocketAddress, String> entry : clients.entrySet()) {
                        if (entry.getValue().equals(recipient)) {
                            byte[] messageBytes = messageToSend.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length, entry.getKey());
                            datagramSocket.send(sendPacket);
                            break;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }


    public static void main(String[] args) throws SocketException {
        DatagramSocket datagramSocket = new DatagramSocket(4000);
        Server server = new Server(datagramSocket);
        server.receiveThenSend();
    }
}