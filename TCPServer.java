import java.io.IOException;
import java.net.*;

/**
 * This class is waiting to receive a message from a client using TCP/IP protocol and then starting a new TCP thread to
 * handle the response.  The response is forwarding the message to all of the clients or disconnecting the client
 *
 */
public class TCPServer extends Thread{

    /**
     * The socket to accept client connections
     */
    private ServerSocket serverSocket;

    /**
     * The object to access the main server
     */
    private MainServer mainServer;

    /**
     * the object to access the GUI for the server to update the client list
     */
    private ServerGUI serverGUI;

    /**
     * The constructor which is storing the socket the client is communicating through and the object to access the
     * main web server
     *
     * @param serverSocket  The socket new clients will connect to
     * @param mainServer    Object to access the main server
     */
    public TCPServer(ServerSocket serverSocket, MainServer mainServer, ServerGUI serverGUI){
        this.serverSocket = serverSocket;
        this.mainServer = mainServer;
        this.serverGUI = serverGUI;
    }

    /**
     * This method is called when the thread is started in the constructor of the main server class.  It waits to
     * receive a message from a TCP client and then starts a new thread to handle disconnecting the client or forwarding
     * the clients message.  The client stays connected to this socket
     */
    public void run(){
        //wait for clients to connect to the server and start a new thread,
        //then continue waiting.
        while(true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                serverGUI.displayText("Error: There is an issue with the socket connection. Please restart the program.");
                return;
            } catch (Exception e) {
                serverGUI.displayText("Error: There is an issue with your connection. Please restart the program.");
                return;
            }
            ServerThread servant = new ServerThread(socket, mainServer, serverGUI);
            servant.start();
        }
    }
}
