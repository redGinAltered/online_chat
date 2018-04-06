package netchat.client;

import netchat.network.TCPConnection;
import netchat.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener{

    private static final String IP_ADRESS = "192.168.1.7";
    private static final int PORT = 5555;
    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;

    public static void main(String[] args){

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run(){
                new ClientWindow();
            }
        });
    }


    private final JTextArea logArea = new JTextArea();
    private final JTextField fieldNickname = new JTextField("Field to Nickmane // field for message on the bottom");
    private final JTextField fieldInput = new JTextField();

    private TCPConnection connection;

    private ClientWindow(){

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);    //добавть возможность выбора

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        add(logArea, BorderLayout.CENTER);

        fieldInput.addActionListener(this);
        add(fieldInput, BorderLayout.SOUTH);
        add(fieldNickname, BorderLayout.NORTH);


        setVisible(true);
        try {
            connection = new TCPConnection(this, IP_ADRESS, PORT);
        } catch (IOException e) {
            printLogMessage("Connection exception: " + e);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e){
        String message = fieldInput.getText();
        if(message.equals("")) return;
        fieldInput.setText(null);
        connection.sendMessage(fieldNickname.getText() + ": " + message);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection){

        printLogMessage("Connection is ready...");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value){
        printLogMessage(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection){
        printLogMessage("You are disconnected");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e){
        printLogMessage("Connection exception: " + e);
    }

    private synchronized void printLogMessage(String message){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                logArea.append(message+"\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }

}
