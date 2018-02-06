import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class is run to start the chat server, which is a multi-threaded web server.
 * The class starts the UDP and TCP/IP server.  This class handles keeping track of all of the UDP and TCP clients.  It
 * keeps a client list which is all of the currently connected clients. It also handles forwarding client messages to
 * all of the clients on the client list.
 *
 */
public class MainServer {

    /**
     * the object to access the GUI for the server to update the client list
     */
    private ServerGUI serverGUI;

    /**
     * the list of TCP clients connected to the server
     */
    private ArrayList<Socket> clientsTCP = new ArrayList<>();

    /**
     * the list of UDP client ports on sever
     */
    private ArrayList<ServerThread> clientsUDP = new ArrayList<>();

    /**
     * the special message sent when the disconnect button is hit by the client to to remove a client from the client
     */
    protected static String removeMe = "Please remove me from the client list (*%$(#&%(*&$#";

    /**
     * The current ID number to be assigned, it increments as each client connects
     */
    private int clientID = 1;

    /**
     * create a TCP socket for server for clients to connect to
     */
    private ServerSocket serverSocket = null;

    /**
     * create the object to send the message
     */
    private PrintWriter out = null;

    /**
     *Constructor starts the GUI and starts the TCP and UDP servers
     */
    public MainServer(){
        boolean a = true;
        while (a){
            a = false;
            try {
                //get local IP address
                String ip = InetAddress.getLocalHost().getHostAddress();
                serverSocket = new ServerSocket(0);
                //get the port number for the server
                int port = serverSocket.getLocalPort();
                //create UDP socket
                DatagramSocket datagramSocket = new DatagramSocket(port);

                //start the GUI for the server
                serverGUI = new ServerGUI(ip, port, this);

                //Start server for TCP server socket
                TCPServer tcpThread = new TCPServer(serverSocket, this, serverGUI);
                tcpThread.start();
                //Start thread for UDP server socket
                UDPServer udpThread = new UDPServer(datagramSocket, this, serverGUI);
                udpThread.start();

            } catch (IOException e) {
                System.err.println("Could not create sockets.");
            } catch (Exception e) {
                serverGUI.displayText("Error: There is an issue with your connection. Please restart the program.");
            }
        }
    }

    /**
     * Sends a message to all of the UDP and TCP/IP clients connected to the server
     *
     * @param message the message that is being sent
     */
    public void sendToAll(String message){
        //send the message to all UDP clients
        //create a packet to send the data
        DatagramPacket packet;
        for(ServerThread thread : clientsUDP){
            //get all the data to send the packet(socket to send, destination IP and port)
            DatagramSocket soc = thread.getUDPSocket();
            InetAddress IP = thread.getPacket().getAddress();
            Integer port = thread.getPacket().getPort();
            //create the data
            byte[] buf = message.getBytes();
            //instantiate the packet with the data and destination
            packet = new DatagramPacket(buf, buf.length, IP, port);
            //send the packet
            try {
                soc.send(packet);
            } catch (IOException e) {
                System.err.println("Error forwarding message to UDP client.");
            } catch (Exception e){
                serverGUI.displayText("Error: There is an issue with your connection. Please restart the program.");
            }
        }

        //send the message to all TCP clients
        for (Socket client : clientsTCP) {
            try {
                out = new PrintWriter( new OutputStreamWriter(client.getOutputStream()), true);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                serverGUI.displayText("Error: There is an issue with your connection. Please restart the program.");
            }
            if (out != null) {
                out.println(message);
            }
        }
    }

    /**
     * Returns the client ID which is a unique number
     *
     * @return the client ID number
     */
    public int getClientID(){

        return clientID++;
    }

    /**
     * remove a client from the client list
     *
     * @param clientThread the client being removed from the list
     */
    public void removeClient(ServerThread clientThread){

        //remove a UDP client
        if(clientThread.isProtocolUDP()){
            clientsUDP.remove(clientThread);
        }
        //remove a TCP/IP client
        else{
            clientsTCP.remove(clientThread.getTCPSocket());
        }
    }

    /**
     * add a client to the client list
     *
     * @param clientThread the client being added
     */
    public void addClient(ServerThread clientThread){
        //add a UDP client
        if(clientThread.isProtocolUDP()){
            //Check to see if this UDP socket has already been added to the client list
            boolean exists = false;
            for(ServerThread client: clientsUDP){
                if(client.getUDPSocket().equals(clientThread.getUDPSocket())){
                    exists = true;
                }
            }
            //If it hasn't been added, add it
            if(!exists){
                clientsUDP.add(clientThread);
                //Display new client on server text window
                serverGUI.displayText(clientThread.getPacket().getAddress() + " is connected using UDP.");
            }
        }

        //add a TCP/IP client
        else{
            clientsTCP.add(clientThread.getTCPSocket());
            //Display new client on server text window
            serverGUI.displayText(clientThread.getTCPSocket().getInetAddress() + " is connected using TCP/IP.");
        }
    }

    /**
     * Closes the current socket. Is called when the client exits the window.
     */
    public void closeSocket(){
        try {
            if(serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            System.err.println("The server crashed while trying to exit");
            System.exit(0);
        }
        if(out != null)
            out.close();
    }

    /**
     * Calls the constructor to create a new instance of the main server
     *
     * @param args not used
     */
    public static void main(String []args){
        new MainServer();
    }
}
