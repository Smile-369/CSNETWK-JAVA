import java.net.*;
import java.util.HashMap;
import java.util.Map;
import org.json.*;
public class Server {
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[1024];
    private HashMap<SocketAddress, String> clients = new HashMap<>();

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
                String recipient =new String();
                String message = messageFromClient.getString("message");
                if (!clients.containsKey(clientAddress)) {
                    clients.put(clientAddress, username);
                    recipient = username;
                } else if (message.startsWith("/register")) {
                    if(!clients.containsValue(username)){
                        clients.put(clientAddress, username);
                        message = "Welcome " + username;
                        recipient = null;
                    }else {
                        message="Error: Registration failed. Handle\n" +
                                "or alias already exists.";
                        recipient = null;
                    }
                }
                if(message.startsWith("/msg")){
                    System.out.println(recipient);
                   recipient = messageFromClient.optString("recipient", null);
                   message = message.substring(6);
                }else {
                    recipient=null;
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
                    for (HashMap.Entry<SocketAddress, String> entry : clients.entrySet()) {
                            System.out.println(entry.getKey() + " "+ entry.getValue()+" "+ recipient+ " "+ messageToSend);
                            if(entry.getValue().equals(recipient)){
                                byte[] messageBytes = messageToSend.getBytes();
                                DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length, entry.getKey());
                                datagramSocket.send(sendPacket);
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
        DatagramSocket datagramSocket = new DatagramSocket(null);
        InetSocketAddress address = new InetSocketAddress("localhost", 12345);
        datagramSocket.bind(address);
        Server server = new Server(datagramSocket);
        server.receiveThenSend();
    }
}