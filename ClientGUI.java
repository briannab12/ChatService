import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

/**
 * The GUI for the chat client.  It displays an area to enter the server information, select protocol, view chat
 * messages, and send a custom message.
 *
 */
public class ClientGUI{

	/**
     *the text the client is sending
     */
    private JTextField messageSendingField;
    
    /**
     * the area the user enters the server IP
     */
    private JTextField serverTextField;
    
    /**
     *the area the user enters the server port
     */
    private JTextField portTextField;

    /**
     *The area where the chat messages will be displayed
     */
    private JTextArea chatDisplayArea;
    
    /**
     * Drop down box to choose UDP or TCP
     */
    private JComboBox<String> protocolSelection;
    
    /**
     * Object to access the client information
     */
    private Client client;

    /**
     *Create button to submit selection
     */
    private JButton confirmButton;

    /**
     * stores the client that this GUI is displaying
     * @param client the client this GUI is displaying
     */
    public ClientGUI(Client client){
        this.client = client;
        displayGUI();
    }

    /**
     *  The GUI provides text boxes to enter the server IP and Port number. There is also a drop down menu to select
     *  TCP/IP or UDP.  There is a button to submit that info.  In the middle of the screen, is a display text area
     *  showing all of the sent and received messages.At the bottom of the screen there is a text box to enter a message
     *  and a button to send that message.
     */
    private void displayGUI(){

        //create the main frame
        JFrame frame = new JFrame("Client");
        frame.setLocation(490, 100);

        //Create each section of the GUI: the area to submit server information, the chat display area, and the area
        //to type and send a new message
        JPanel serverInfoPanel = createServerInfoPanel();
        JScrollPane chatTextArea = createChatDisplayPanel();
        JPanel newMessagePanel = createNewMessagePanel();

        //Change the exit operations to have the client disconnect from the server before exiting
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.disconnectFromServer();
                client.closeSocket();
                System.exit(0);
            }
        });

        //Make JFrame Visible
        frame.add(serverInfoPanel, BorderLayout.NORTH);
        frame.add(chatTextArea, BorderLayout.CENTER);
        frame.add(newMessagePanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * This is a helper method to the displayGUI method. This is the top panel in the main panel.
     * It displays:
     *      instructions
     *      the boxes to enter the server port and server IP
     *      labels for the above boxes
     *      drop down menu to select protocol (UDP or TCP/IP)
     *      button to submit the above information
     *
     * @return the panel with the information needed to connect to the server
     */
    private JPanel createServerInfoPanel(){

        JPanel serverInfoPanel = new JPanel(new BorderLayout());

        //Grid panel of server info
        JPanel serverLabels = new JPanel();
        serverLabels.setLayout(new GridLayout(3, 2));

        //display instructions for selection TCP or UDP
        String instruct = "Please enter server information and select UDP or TCP/IP " +
                "from the drop menu below. Then confirm your selection. \nWarning: Messages "
                + "greater that 1000 characters may be cut off.";
        JTextArea instructions = new JTextArea(instruct);
        instructions.setEditable(false);
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setSize(new Dimension(200, 20));

        //Server Ip box label
        String serverField = "Server IP Address:";
        JLabel serverLabel = new JLabel(serverField);
        serverLabel.setSize(new Dimension(200,20));
        serverLabels.add(serverLabel);

        //Server Port box label
        String portField = "Server Port:";
        JLabel portLabel = new JLabel(portField);
        portLabel.setSize(new Dimension(200, 20));
        serverLabels.add(portLabel);

        //Add the text boxes and enter button
        serverTextField = new JTextField();
        serverTextField.setPreferredSize(new Dimension(200, 20));
        serverLabels.add(serverTextField);

        portTextField = new JTextField();
        portTextField.setPreferredSize(new Dimension(200, 20));
        serverLabels.add(portTextField);
        //texts boxes and buttons created

        //create drop down menu
        String[] options = { "Select One", "TCP/IP", "UDP" };
        protocolSelection = new JComboBox<>(options);
        //add drop down to selection panel
        serverLabels.add(protocolSelection);
        //Create button to submit selection
        confirmButton = new JButton("Confirm Selection");
        //add button to submit panel
        serverLabels.add(confirmButton);

        //create action listener for confirm button to store communication method of UDP or TCP
        confirmButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String select = (String) protocolSelection.getSelectedItem();
                        //connect through either TCP or UDP, whichever user selected
                        switch (select) {
                            case "UDP":
                                client.setProtocol("UDP");
                                try {
                                    displayNewMessage("Now contacting Server " + serverTextField.getText()
                                        + " with UDP communication from " + InetAddress.getLocalHost().getHostAddress());
                                } catch (UnknownHostException e1) {
                                    e1.printStackTrace();
                                }
                                client.connectToServer();
                                break;
                            case "TCP/IP":
                                client.setProtocol("TCP/IP");
                                try {
                                    displayNewMessage("Now contacting Server " + serverTextField.getText()
                                            + " with TCP/IP communication from " + InetAddress.getLocalHost().getHostAddress());
                                } catch (UnknownHostException e1) {
                                    e1.printStackTrace();
                                }
                                client.connectToServer();
                                break;
                            default:
                                client.setProtocol("Select One");
                                displayNewMessage("Please enter server information and select UDP or TCP/IP " +
                                        "from the drop menu above. Then confirm your selection.");
                                break;
                        }
                    }
                }
        );


        //create drop down menu to select UDP or TCP/IP
        //create panel for dropdown menu and submit button
        JPanel disconnecPanel = new JPanel();
        disconnecPanel.setLayout(new FlowLayout());

        //Add the server info, selection instruction, and submit button to top panel
        serverInfoPanel.add(serverLabels, BorderLayout.CENTER);
        serverInfoPanel.add(instructions, BorderLayout.NORTH);

        return serverInfoPanel;
    }

    /**
     * This is a helper method to the displayGUI method. This is the middle panel in the main panel.
     * It displays:
     *      box that contains all the messages send and recieved in the chat
     *
     * @return the panel containing the chat messages
     */
    private JScrollPane createChatDisplayPanel(){
        chatDisplayArea = new JTextArea();
        JScrollPane scrollPaneChat = new JScrollPane(chatDisplayArea);
        chatDisplayArea.setRows(15);
        chatDisplayArea.setEditable(false);
        chatDisplayArea.setLineWrap(true);
        chatDisplayArea.setWrapStyleWord(true);

        return scrollPaneChat;
    }

    /**
     * This is a helper method to the displayGUI method. This is the bottom panel in the main panel.
     * It displays:
     *      text field area for the user to type the message they would like to send
     *      button to send the user's message
     *
     * @return the panel containing the text field and button for the user to send messages
     */
    private JPanel createNewMessagePanel(){

        JPanel createNewMessagePanel = new JPanel(new BorderLayout());
        //create the input box
        messageSendingField = new JTextField();
        messageSendingField.setPreferredSize(new Dimension(100, 30));
        //add input box to bottom panel
        createNewMessagePanel.add(messageSendingField, BorderLayout.CENTER);

        //created a button to send message
        JButton sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(100, 30));
        //add send button to bottom panel
        createNewMessagePanel.add(sendButton, BorderLayout.EAST);

        //Action listener for send button.  It calls the appropriate method for sending
        //the message using either UDP or TCP
        sendButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
	                    if(!getClientMessage().equals("")){    
                    		switch (client.getProtocol()) {
	                            case "UDP":
	                                client.sendMessage();
	                                break;
	                            case "TCP/IP":
	                                client.sendMessage();
	                                break;
	                            default:
	                                displayNewMessage("Please enter server information and select UDP or TCP/IP " +
	                                        "\"from the drop menu above. Then press \"Confirm Selection\"");
	                                break;
	                        }
                    	}
                    }
                }
        );
        
        //Action listener for send button so the user can hit the enter key.  
        //It calls the appropriate method for sending the message using either UDP or TCP
        messageSendingField.addKeyListener(
            new KeyListener() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode()==KeyEvent.VK_ENTER){
						if(!getClientMessage().equals("")){    
	                		switch (client.getProtocol()) {
	                            case "UDP":
	                                client.sendMessage();
	                                break;
	                            case "TCP/IP":
	                                client.sendMessage();
	                                break;
	                            default:
	                                displayNewMessage("Please enter server information and select UDP or TCP/IP " +
	                                        "\"from the drop menu above. Then press \"Confirm Selection\"");
	                                break;
	                        }
	                	}
					}
				}
				@Override
				public void keyReleased(KeyEvent e) {}
				@Override
				public void keyTyped(KeyEvent e) {}
            }
        );
        return createNewMessagePanel;
    }

    /**
     *Appends a new message to the chat display are
     *
     * @param newText the new message being added to chat display
     */
    public void displayNewMessage(String newText){
        chatDisplayArea.append(newText + "\n");
        chatDisplayArea.scrollRectToVisible(chatDisplayArea.getBounds());
        chatDisplayArea.setCaretPosition(chatDisplayArea.getText().length());
    }

    /**
     * Sets the String of the text field where the user enters the message to be sent
     *
     * @param text the String the text field is set to
     */
    public void setSendingField(String text) {

        messageSendingField.setText(text);
    }

    /**
     * get the message the user wants to send
     *
     * @return the message the user wants to send
     */
    public String getClientMessage(){

        return messageSendingField.getText();
    }

    /**
     * get the server name the user enter
     *
     * @return the server name
     */
    public String getServerTextField(){

        return serverTextField.getText();
    }

    /**
     * get the server port number the user entered
     *
     * @return the server port number
     */
    public String getPortTextField(){

        return portTextField.getText();
    }

    /**
     * Disables the confirm button on the GUI, it is after the user connnects.
     */
    public void disableConfirmButton(){

        confirmButton.setEnabled(false);
    }

}
