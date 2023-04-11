import java.net.*;
import java.util.ArrayList;
import java.util.List;


public class Server {
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[1024];
    private List<SocketAddress> clients = new ArrayList<>();

    public Server(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public void receiveThenSend() {
        while (true) {
            try {
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(datagramPacket);
                SocketAddress clientAddress = datagramPacket.getSocketAddress();
                if (!clients.contains(clientAddress)) {
                    clients.add(clientAddress);
                }
                String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength());

                for (SocketAddress client : clients) {
                        byte[] messageBytes = message.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length, client);
                        datagramSocket.send(sendPacket);
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