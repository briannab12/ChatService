import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/**
 *The UDPThread class should extend the Thread class, because a UDPThread
 * will be created by MyWebServer every time a client connects to MyWebServer.
 *
 */
public class ServerThread extends Thread {

    /**
     * the socket this thread was forwarded to to use to communicate through UDP
     */
    private DatagramSocket socketUDP;

    /**
     * the socket this thread is using to communicate through TCP
     */
    private Socket socketTCP;

    /**
     * object to access the main server class
     */
    private MainServer mainServer;

    /**
     * the packet received from the client
     */
    private DatagramPacket packet;

    /**
     * the protocol type
     */
    private boolean isUDP = false;

    /**
     * the unique identifier for the client
     */
    private int clientID;

    /**
     * the object to access the GUI for the server to update the client list
     */
    ServerGUI serverGUI;
    
    /**
     * the size of the buf for datagram packets
     */
    private final int bufSize = 256;

    /**
     * the constructor for a UDP thread
     *
     * @param mainServer the object to access the server
     */
    public ServerThread(MainServer mainServer, DatagramPacket packet, ServerGUI serverGUI){
        //store reference to server class
        this.mainServer = mainServer;
        //The packet received from client
        this.packet = packet;
        //assign the protocol
        isUDP = true;
        //add client to client list
        mainServer.addClient(this);
        //get the client ID
        clientID = mainServer.getClientID();
        //store the GUI server
        this.serverGUI = serverGUI;
        //create a new socket for this client
        try {
            socketUDP = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (Exception e){
            serverGUI.displayText("Error: There is an issue with your connection. Please restart the program.");
        }
    }

    /**
     * the constructor for a TCP thread
     *
     * @param socketTCP the socket to connect by TCP/IP
     * @param mainServer the object to access the server
     */
    public ServerThread(Socket socketTCP, MainServer mainServer, ServerGUI serverGUI){
        //store the tcp socket to communicate to client
        this.socketTCP = socketTCP;
        //store reference to server class
        this.mainServer = mainServer;
        //Add new client to client list
        mainServer.addClient(this);
        //store the GUI server
        this.serverGUI = serverGUI;
        //get the clientID
        clientID = mainServer.getClientID();
    }

    /**
     * Waits for the client to send a message and then forwards it to all of the clients.
     */
    public void run(){
        if(isUDP){
            runUDP();
        }else{
            runTCP();
        }
    }//end run

    /**
     * This method is called to communicate with a UDP client
     * It receives the initial message from the client establish communication.  It then send the client a new port
     * to communicate on. It then infinitely waits to receive messages from the client.
     */
    private void runUDP(){
        //the message the client sent
        String message = new String(packet.getData(), 0, packet.getLength());
        mainServer.sendToAll(formatMessage(message));
        //get the new port that the server is forwarding the client to
        String newPort = Integer.toString(socketUDP.getLocalPort());
        //create the data
        byte[] buf = newPort.getBytes();
        //create a packet to send output
        DatagramPacket packet = new DatagramPacket(buf, buf.length, getPacket().getAddress(), getPacket().getPort());
        //send the packet
        try {
            socketUDP.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            serverGUI.displayText("Error: There is an issue with your connection. Please restart the program.");
        }

        while(true) {
            //create the packet to accept a message
            buf = new byte[bufSize];
            packet = new DatagramPacket(buf, buf.length);
            try {
                //receive a message from the client
                socketUDP.receive(packet);
            } catch (IOException e) {
                System.err.println("Error receiving datagram packet");
            } catch (Exception e){
                serverGUI.displayText("Error: There is an issue with your connection. Please restart the program.");
            }
            //the message the client sent
            message = new String(packet.getData(), 0, packet.getLength());
            //If client requested to disconnect, disconnect him
            if (message.equals(MainServer.removeMe)) {
                mainServer.removeClient(this);
                if(socketUDP != null)
                    socketUDP.close();
                return;
            }
            //Forward the message to all other clients
            else {
                mainServer.sendToAll(formatMessage(message));
            }
        }
    }

    /**
     * This method is called to communicate with a TCP/IP client
     * It receives the initial message from the client establish communication.  It then infinitely waits to receive
     * messages from the client.
     */
    private void runTCP(){
        //create a BufferedReader to read from the client
        // and a PrintWriter to write to the client.
        BufferedReader in = null;
        PrintWriter out = null;

        //Try to create objects to send and receive from the socket
        try {
            in = new BufferedReader(new InputStreamReader(socketTCP.getInputStream())) ;
            out = new PrintWriter( new OutputStreamWriter(socketTCP.getOutputStream()), true);
            //send a confirmation message
            out.println("Receiving communication from server using IP address " +
                    socketTCP.getInetAddress().getHostAddress() + " and Port "
                    + socketTCP.getLocalPort() + ".");
        } catch(IOException e){
            System.err.println("I/O error getting InputStream");
            serverGUI.displayText("Error: There is an issue with your connection. Please restart the program.");
        } catch (Exception e){
            serverGUI.displayText("Error: There is an issue with your connection. Please restart the program.");
        }

        //Infinitely wait for clients to send a message and then distribute it to all clients
        while(true){
            try {
                //read the message from client. If it is a request to disconnect,
                // remove the client from the list and close the socket
                String message = in.readLine();
                if (message.equals(MainServer.removeMe)) {
                    mainServer.removeClient(this);
                    if(socketTCP != null)
                        socketTCP.close();
                    if(in != null)
                        in.close();
                    if(out != null)
                        out.close();
                    return;
                } else {
                    //Forward the message to all other clients
                    mainServer.sendToAll(formatMessage(message));
                }//end else
            } catch (IOException e) {
                break;
            } catch (Exception e){
                serverGUI.displayText("Error: There is an issue with your connection. Please restart the program.");
            }//end catch
        }//end while
    }

    /**
     * A helper method for run to format the message
     *
     * @param message the message being formatted
     * @return the formatted message
     */
    private String formatMessage(String message){
        //create a timestamp
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        String time = sdf.format(date);
        String formattedTime = "(" + time + ") ";
        //create string of user name
        String name = "Client" + clientID + ": ";
        //combine client number, timestamp, and message
        return formattedTime + name + message;
    }

    /**
     * Returns true if the client protocol us UDP and false if it is TCP/IP
     *
     * @return  Returns true if the client protocol us UDP and false if it is TCP/IP
     */
    public boolean isProtocolUDP(){

        return isUDP;
    }

    /**
     * Returns the socket for UDP clients
     *
     * @return  the socket
     */
    public DatagramSocket getUDPSocket(){

        return socketUDP;
    }

    /**
     * Returns the socket for TCP/IP clients
     *
     * @return  the socket
     */
    public Socket getTCPSocket(){

        return socketTCP;
    }

    /**
     * Returns the datagram packet that the client sent
     *
     * @return  the datagram packet the client sent
     */
    public DatagramPacket getPacket(){

        return packet;
    }

}//end class
