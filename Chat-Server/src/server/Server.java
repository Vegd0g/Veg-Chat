package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Server {
    public static final Map<Socket,String> onlineUsers= Collections.synchronizedMap(new HashMap<>());
    public static void main(String[] args) {
        System.out.println("Server is running...");
        try {
            ServerSocket serverSocket=new ServerSocket(Const.PORT);
            while (true){
                System.out.println("Waiting for client...");
                Socket socket=serverSocket.accept();

                new ServerReaderThread(socket).start();
                System.out.println("1 Client connected");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
