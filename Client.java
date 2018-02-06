import java.io.*;
import java.net.*;

/**
 *  The client will need to enter the server name and port to connect.  The client will also select whether to
 *  communicate using TCP/IP or UDP.
 *
 */
public class Client{

    /**
     * the name of the server the user inputed
     */
    private String serverName;

    /**
     * the client's unique port to send UDP messages
     */
    private int portUDP;

    /**
     * Initialize the client TCP/IP socket
     */
    private Socket socketTCP = null;

    /**
     * Initialize the client UDP socket
     */
    private DatagramSocket socketUDP = null;

    /**
     * Initialize the communication choice
     */
    private String protocol = "Select One";

    /**
     * the object to access the GUI for the client to update the current chat messages
     */
    private ClientGUI clientGUI;

    /**
     * the client is currently connect
     */
    private boolean connection = false;

    /**
     * the object to send messages to the TCP/IP server
     */
    private PrintWriter out = null;

    /**
     * create a BufferedReader to read from the client
     */
    private BufferedReader in = null;
    
    /**
     * the time in milliseconds that the program waits for server to respond during UDP
     */
    private final int shortWait = 2000;
    
    /**
     * the time in milliseconds that the program waits to receive a message during UDP
     */
    private final int longWait = 3600000;
    
    /**
     * the size of the buf for datagram packets
     */
    private final int bufSize = 256;

    /**
     * Constructor to start the GUI and wait to receive messages
     */
    public Client(){
        clientGUI = new ClientGUI(this);
        //wait to receive messages
        receiveMessages();
    }

    /**
     * This is called when the user submits the server information.  It connects the client to the server and waits
     * to receive messages.
     */
    public void connectToServer() {
        //get the server name the user inputed
        serverName = clientGUI.getServerTextField();
        if(serverName.equals("")){
            System.err.println("The user did not enter an IP address");
            clientGUI.displayNewMessage("Error: Please enter a Server IP Address.");
            return;
        }
        //get the port number the user inputed
        int serverPort;
        try {
            serverPort = Integer.parseInt(clientGUI.getPortTextField());
        } catch (NumberFormatException e) {
            System.err.println("Error 1: The entered port number is not a valid integer");
            clientGUI.displayNewMessage("Error: Invalid Server Port. Please reenter Server Port number");
            return;
        } catch (Exception e){
            System.err.println("Error 2: The entered port number is not a valid integer");
            clientGUI.displayNewMessage("Error: Invalid Server Port. Please reenter Server Port number.");
            return;
        }


        //If making a TCP/IP connect do this
        if(protocol.equals("TCP/IP")) {
            try {
                socketTCP = new Socket(InetAddress.getByName(serverName), serverPort);
                String joinMessage = "has joined the chat room.";
                try {
                    out = new PrintWriter(new OutputStreamWriter(socketTCP.getOutputStream()), true);
                    out.println(joinMessage);
                    //since the connection was created, disable the button to submit
                    clientGUI.disableConfirmButton();
                    connection = true;
                } catch (IOException e) {
                    System.err.println("I/O error getting InputStream.");
                    clientGUI.displayNewMessage("Error: Please reenter the server port number.");
                } catch (Exception e){
                    System.err.println("Issue with the data the user enter");
                    clientGUI.displayNewMessage("Error: Please reenter the server IP address and port number.");
                }
            } catch (UnknownHostException e) {
                clientGUI.displayNewMessage("Error: Invalid server name or port number.");
                System.err.println("Error getting InetAddress");
            } catch (IOException e) {
                    clientGUI.displayNewMessage("Error: Invalid server name or port number.");
                    System.err.println("Error creating socketTCP");
            } catch (Exception e){
                clientGUI.displayNewMessage("Error: Invalid server name or port number.");
                System.err.println("Error with the server IP or port number the user entered.");
            }
        }else {//UDP is selected
            try {
                //create UDP socket
                socketUDP = new DatagramSocket();
                //Send confirmation
                //confirmation message
                String joinMessage = "has joined the chat room.";
                //create the data
                byte[] buf = joinMessage.getBytes();
                //create a packet to send output
                DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(serverName), serverPort);
                //send the packet
                socketUDP.send(packet);
                //get the new port number to communicate on then
                //Infinitely read in what the server is sending
                buf = new byte[bufSize];
                packet = new DatagramPacket(buf, buf.length);
                try {
                    //create a timer to return if UDP server doesn't communicate back
                    //set the timer
                    socketUDP.setSoTimeout(shortWait);
                    try {
                        socketUDP.receive(packet);
                    } catch(SocketTimeoutException e){
                        clientGUI.displayNewMessage("Error: Invalid port number, please reenter valid information.");
                        return;
                    }
                    clientGUI.displayNewMessage("Receiving communication from server using IP address " +
                            InetAddress.getLocalHost().getHostAddress() + " and Port "
                            + socketUDP.getLocalPort() + ".");
                    String message = new String(packet.getData(), 0, packet.getLength());
                    clientGUI.displayNewMessage(message);
                    try {
                        socketUDP.receive(packet);
                    } catch(SocketTimeoutException e){
                        clientGUI.displayNewMessage("Error: Invalid port number, please reenter valid information.");
                        return;
                    }
                    message = new String(packet.getData(), 0, packet.getLength());
                    portUDP = Integer.parseInt(message);
                    //since the connection was created, disable the button to submit
                    clientGUI.disableConfirmButton();
                    connection = true;
                } catch (IOException e) {
                    System.err.println("Error receiving datagram packet to initiate communication with server.");
                } catch (Exception e) {
                    clientGUI.displayNewMessage("Error: There is an issue with your connection. Please restart the program.");
                }
            } catch (IOException e) {
                System.err.println("Error connecting to server via UDP");
                clientGUI.displayNewMessage("Error: Invalid server name or port number. Please try reentering the information.  "
                        + "If the program still does not work, please restart the program.");
            } catch (Exception e){
                clientGUI.displayNewMessage("Error: Invalid server name or port number. Please try reentering the information.  "
                        + "If the program still does not work, please restart the program.");
            }
        }
    }

    /**
     * Infinitely waits to receive a message using TCP/IP or UDP protocol
     */
    public void receiveMessages() {
        while(true){
            System.out.print("");
            if(connection) {
                String messageReceived;
                switch (protocol) {
                    case "TCP/IP":
                        try {
                            //try to create a buffer to read from the socket
                            in = new BufferedReader(new InputStreamReader(socketTCP.getInputStream()));
                            //Read in what the server sent and display it
                            messageReceived = in.readLine();
                            if(messageReceived != null) {
                                clientGUI.displayNewMessage(messageReceived);
                            }else{//the server has shut down
                                hault();
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading in from server. Server may have shut down.");
                            clientGUI.displayNewMessage("Error: The server has crashed or has been shut down.");
                            connection = false;
                        } catch (Exception e){
                            clientGUI.displayNewMessage("Error: There is an issue with your connection. Please restart the program.");
                            connection = false;
                        }
                        break;
                    case "UDP":
                        //Read in what the server sent and display it
                        byte[] buf = new byte[bufSize];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        try {
                            //set the timer for 60 minutes
                            socketUDP.setSoTimeout(longWait);
                            try {
                                socketUDP.receive(packet);
                            } catch(SocketTimeoutException e){
                                clientGUI.displayNewMessage("You have not received a message for an extended " +
                                        "period of time. The server may have shut down.  Your connection has been terminated. "
                                        + "Please restart the program to reconnect.");
                                return;
                            }
                            messageReceived = new String(packet.getData(), 0, packet.getLength());
                            clientGUI.displayNewMessage(messageReceived);
                        } catch (IOException e) {
                            System.err.println("Error receiving datagram packet");
                            clientGUI.displayNewMessage("Error: There is an issue with your connection. Please restart the program.");
                        } catch (Exception e){
                            clientGUI.displayNewMessage("Error: There is an issue with your connection. Please restart the program.");
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * disconnect from the server, this is called when the user exits
     */
    public void disconnectFromServer(){
        String alertMessage = "has left the chat";
        String removeMe = "Please remove me from the client list (*%$(#&%(*&$#";
        if(connection){
            switch (protocol) {
                case "TCP/IP":
                    try {
                        out = new PrintWriter(new OutputStreamWriter(socketTCP.getOutputStream()), true);
                        //remove this client from the client chat list
                        out.println(removeMe);
                        clientGUI.displayNewMessage("Ending Communications with server " + socketTCP.getInetAddress().getHostAddress());
                        connection = false;
                        //Send message to all other clients that this client is leaving
                        out.println(alertMessage);
                    }  catch (IOException e) {
                        System.err.println("Error sending TCP/IP message to Server");
                    } catch (Exception e){
                        clientGUI.displayNewMessage("Error: There is an issue with your connection. Please restart the program.");
                    }
                    break;
                case "UDP":
                    try {
                        //remove this client from the client chat list
                        byte[] buf = removeMe.getBytes();
                        DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(serverName), portUDP);
                        socketUDP.send(packet);
                        clientGUI.displayNewMessage("Ending Communications with server " + serverName);
                        connection = false;
                        //Send message to all other clients that this client is leaving
                        buf = alertMessage.getBytes();
                        packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(serverName), portUDP);
                        socketUDP.send(packet);
                    } catch (IOException e) {
                        System.err.println("Error sending UDP message to Server");
                    } catch (Exception e){
                        clientGUI.displayNewMessage("Error: There is an issue with your connection. Please restart the program.");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Send a message using TCP/IP or UDP protocol
     */
    public void sendMessage(){

        String messageSending = clientGUI.getClientMessage();
        clientGUI.setSendingField("");

        switch (protocol) {
            case "TCP/IP":
                try {
                    out = new PrintWriter(new OutputStreamWriter(socketTCP.getOutputStream()), true);
                    out.println(messageSending);
                } catch (IOException e) {
                    System.err.println("I/O error getting InputStream. Please restart program");
                    clientGUI.displayNewMessage("Error: There is an issue with your connection. Please restart the program.");
                } catch (Exception e){
                    clientGUI.displayNewMessage("Error: There is an issue with your connection. Please restart the program.");
                }
                break;
            case "UDP":
                try {
                    //create the data
                    byte[] buf = messageSending.getBytes();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(serverName), portUDP);
                    socketUDP.send(packet);
                } catch(IOException e){
                    System.err.println("I/O error sending confirmation. Please restart program.");
                    clientGUI.displayNewMessage("Error: There is an issue with your connection. Please restart the program.");
                } catch (Exception e){
                    clientGUI.displayNewMessage("Error: There is an issue with your connection. Please restart the program.");
                }
                break;
            default:
                clientGUI.displayNewMessage("Error: Please close this window and reconnect. +" +
                        "There is an issue with your current connection.");
                break;
        }
    }

    /**
     * Get the current communication protocol being used
     *
     * @return the current communication protocol
     */
    public String getProtocol(){

        return protocol;
    }

    /**
     * Set the current communication protocol being used
     *
     * @param protocol the current communication protocol (UDP or TCP/IP)
     */
    public void setProtocol(String protocol){

        this.protocol = protocol;
    }

    /**
     * Closes the current socket. Is called when the client exits the window.
     */
    public void closeSocket(){
        switch(protocol){
            case "TCP/IP":
                try {
                    if(socketTCP != null)
                        socketTCP.close();
                    if(in != null)
                        in.close();
                    if(out != null)
                        out.close();
                } catch (IOException e) {
                    System.err.println("Program crashed while trying to exit.");
                    System.exit(0);
                }
                break;
            case "UDP":
                if(socketUDP != null)
                    socketUDP.close();
                break;
        }
    }

    /**
     * Doesn't allow the client to do anything. This is called when the server crashes.
     */
    private void hault(){
    	System.err.println("Error reading in from server. Server may have shut down.");
        clientGUI.displayNewMessage("Error: The server has crashed or has been shut down.");
        connection = false;
        while(true){}
    }

    /**
     * Asks the Server to see a text file
     *
     * @param args  Server_name Server_port
     */
    public static void main(String [] args) {

        new Client();
    }
}
