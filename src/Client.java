import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client {
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[256];
    private InetAddress inetAddress;
    private String username;

    public Client(DatagramSocket datagramSocket, InetAddress inetAddress, String username) {
        this.datagramSocket = datagramSocket;
        this.inetAddress = inetAddress;
        this.username = username;
    }

    public void sendThenReceive() {
        Thread sendThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                try {
                    String messageToSend = username + ": " + scanner.nextLine();
                    buffer = messageToSend.getBytes();
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, inetAddress, 4000);
                    datagramSocket.send(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });

        Thread receiveThread = new Thread(() -> {
            while (true) {
                try {
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(datagramPacket);
                    String messageFromServer = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                    System.out.println(messageFromServer);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });

        sendThread.start();
        receiveThread.start();
    }

    public static void main(String[] args) throws SocketException, UnknownHostException {
        DatagramSocket datagramSocket = new DatagramSocket();
        InetAddress inetAddress = InetAddress.getByName("localhost");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        Client client = new Client(datagramSocket, inetAddress, username);
        client.sendThenReceive();
    }
}