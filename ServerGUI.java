import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The GUI for the chat server.  It displays the server IP, server Port, and a list of the clients that are connected
 * to the server.
 *
 */
public class ServerGUI {

    /**
     * Text area to display connected clients
     */
    private static JTextArea clientListDisplay;

    /**
     * Access to the main server in order to close the socket and I/O streams when the server is closed
     */
    private MainServer server;

    /**
     * The constructor for the server's GUI.  It calls the method that displays the
     * server information
     *
     * @param ip        The IP address of the server
     * @param portNum   The port number the server is listening on
     */
    public ServerGUI(String ip, int portNum, MainServer server){
        this.server = server;
        String port = "Port: " + Integer.toString(portNum);
        displayGUI(ip, port);
    }

    /**
     * The GUI for the server.  Is displays a box of the Ip and Port
     */
    public void displayGUI(String ip, String port){
        //Create the frame for the GUI
        JFrame frame = new JFrame("Server");
        frame.setSize(400, 250);
        frame.setLocation(100, 100);
        //If the port or IP is null display an error message.  If not display the data
        String text;
        if((ip!=null)&&(port!=null)) {
            text = "   " + ip + "      " + port;
        }else {
            text = "There was an error retrieving IP or Port number.";
        }
        //Create a text area to display connected clients
        clientListDisplay = new JTextArea();
        JScrollPane scrollPaneClientList = new JScrollPane(clientListDisplay);
        clientListDisplay.setRows(10);
        clientListDisplay.setEditable(false);
        clientListDisplay.setLineWrap(true);
        clientListDisplay.setWrapStyleWord(true);
        //Create a display to show the IP Address and port of the server
        JLabel serverInfo = new JLabel(text, JLabel.LEFT);
        serverInfo.setSize(new Dimension(200, 20));

        //Change the exit operations to have the client disconnect from the server before exiting
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                server.closeSocket();
                System.exit(0);
            }
        });
        //add everything to the frame
        frame.add(serverInfo,BorderLayout.NORTH);
        frame.add(scrollPaneClientList, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    /**
     * Displays the client information for each new client that connects
     * to the server on the server GUI.
     *
     * @param newText   The clients information
     */
    public void displayText(String newText){

        clientListDisplay.append(newText + "\n");
    }
}
