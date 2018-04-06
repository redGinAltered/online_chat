package netchat.server;

import netchat.network.TCPConnection;
import netchat.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;


//При запуске через джарник сервер повисает в процессах и закрывается только через диспетчер
//Добавить какую-нибудь форму-окно с логами и возможность отключить сервер при выходе

public class ChatServer implements TCPConnectionListener{

    public static void main(String[] args){
        new ChatServer();
    }

    //List of connection -- реализовать через linked list
    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private ChatServer() {

        System.out.println("Server is running...");

        try (ServerSocket serverSocket = new ServerSocket(5555)){

            while(true){

                try{
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e){
                    System.out.println("TCPConnection exception: "+e);
                }
            }
        } catch(IOException e){
            throw new RuntimeException(e);
        }

    }

    //После реализации интерфейса класс становится одновременно и ЧатСервером и
    //ТиСиПиКоннекшонЛисенером, поэтому при создании нового ТСР-соединение он может передать сам себя в его конструктор
    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {

        connections.add(tcpConnection);
        sendToAllConnections(tcpConnection + " connected");
    }
    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value){

        sendToAllConnections(value);
    }
    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection){

        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnected: "+tcpConnection);
    }
    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e){
        System.out.println("TCPConnection exception: "+e);
    }

    private void sendToAllConnections(String value){

        System.out.println(value);
        for(TCPConnection connection : connections){

            connection.sendMessage(value);
        }
    }

}
