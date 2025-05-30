package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public class ServerReaderThread extends Thread {

    private Socket socket;

    public ServerReaderThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            int type = dis.readInt();
            while (true) {
                switch (type) {
                    case 1: //登录
                        String username = dis.readUTF();
                        Server.onlineUsers.put(socket, username);
                        updateUserList();
                        break;
                    case 2: //群聊消息
                        String msg = dis.readUTF();
                        sendMsgToAll(msg);
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("客户端下线了：" + socket.getInetAddress().getHostAddress());
            Server.onlineUsers.remove(socket);
            updateUserList();
        }

    }

    private void updateUserList() {
        Collection<String> onlineUserList = Server.onlineUsers.values();
        for (Socket socket : Server.onlineUsers.keySet()) {
            try {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(1);
                dos.writeInt(onlineUserList.size());
                for (String user : onlineUserList) {
                    dos.writeUTF(user);
                }
                dos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void sendMsgToAll(String msg) {
        StringBuilder sb = new StringBuilder();
        String name = Server.onlineUsers.get(socket);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss EEE a");
        String time = now.format(dtf);
        String msgRes = sb.append(name).append(" ").append(time).append(":")
                .append("\r\n").append(msg).append("\r\n").toString();
        System.out.println(msgRes);
        for (Socket socket : Server.onlineUsers.keySet()) {
            try {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(2);
                dos.writeUTF(msgRes);
                dos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
