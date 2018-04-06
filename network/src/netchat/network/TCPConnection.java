package netchat.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

//Взаимодействие с сетью: два основных класса - сервер СерверСокет и класс Сокет
//СерверСовет - слушает входящее соединение, приминать его, создавать объект сокет
//И готов этот объект отдавать. Класс СОКЕТ устанавливает соединение

public class TCPConnection { //Main class of connection

    private final Socket socket;
    private final Thread listenThread;
    private final TCPConnectionListener eventListener;
    private final BufferedReader in;
    private final BufferedWriter out;

    //Второй Конструктор - сокет будет создан внутри, сокет  собирается из  ip адреса и номера порта
    public TCPConnection(TCPConnectionListener eventListener, String ipAdress, int port) throws IOException{

        //вызываем первый конструктор из второго
        this(eventListener, new Socket(ipAdress, port));
    }

    //Первый конструктор рассчитан, что кто-то извне создаёт соединение, с уже готовым сокетом
    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException{

        this.eventListener = eventListener;
        this.socket=socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));

        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    eventListener.onConnectionReady(TCPConnection.this);
                    while(!listenThread.isInterrupted()){

                        eventListener.onReceiveString(TCPConnection.this, in.readLine());
                    }
                } catch (IOException e){
                    eventListener.onException(TCPConnection.this, e);
                } finally{
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        listenThread.start();

    }


    public synchronized void sendMessage(String value){

        try {
            out.write(value+ "\r\n");//явный перенос стоки и возрат коретки
            out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect(){

        listenThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {

        return "IP -->" +socket.getInetAddress() + ": " + socket.getPort();
    }
}
