import java.io.*;
import java.net.*;

/**
 * This class is waiting to receive a message from a client using UDP protocol and then starting a new UDP thread to
 * handle the response.  The response is forwarding the message to all of the clients or disconnecting the client.
 *
 */
public class UDPServer extends Thread {

    /**
     * The socket to accept client connections
     */
    private DatagramSocket datagramSocket;

    /**
     * The object to access the main server class
     */
    private MainServer mainServer;

    /**
     * the object to access the GUI for the server to update the client list
     */
    private ServerGUI serverGUI;
    
    /**
     * the size of the buf for datagram packets
     */
    private final int bufSize = 256;

    /**
     * The constructor which is storing the socket the client is communicating through and the object to access the
     * main web server
     *
     * @param datagramSocket    The socket UDP clients will connect to
     * @param mainServer        Object to access the main server
     */
    public UDPServer(DatagramSocket datagramSocket, MainServer mainServer, ServerGUI serverGUI){
        this.datagramSocket = datagramSocket;
        this.mainServer = mainServer;
        this.serverGUI = serverGUI;
    }

    /**
     * This method is called when the thread is started in the constructor of the main server class.  It waits to
     * receive a message from a UDP client and then starts a new thread to handle disconnecting the client or forwarding
     * the clients message
     */
    public void run(){
        //wait for clients to connect to the server and start a new thread,
        //then continue waiting.
        while(true) {
            //create the packet to accept a message
            byte[] buf = new byte[bufSize];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                //receive a message from the client
                datagramSocket.receive(packet);
            } catch (IOException e) {
                System.err.println("Error receiving datagram packet");
            } catch (Exception e){
                serverGUI.displayText("Error: There is an issue with your connection. Please restart the program.");
            }
            //Start a new thread for this client
            ServerThread servant = new ServerThread(mainServer, packet, serverGUI);
            servant.start();
        }
    }
}
