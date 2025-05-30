package ui;

import java.io.DataInputStream;
import java.net.Socket;

public class ClientReaderThread extends Thread {

    private Socket socket;
    private DataInputStream dis;
    private ClientChatFrame chatFrame;

    public ClientReaderThread(Socket socket, ClientChatFrame chatFrame) {
        this.chatFrame = chatFrame;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            int type = dis.readInt();
            while (true) {
                switch (type) {
                    case 1: //服务端发来，在线人数更新
                        updateUserList();
                        break;
                    case 2: //服务端发来群聊消息
                        getMsgToWin();
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateUserList() throws Exception {
        int count = dis.readInt();
        String[] namelist = new String[count];
        for (int i = 0; i < count; i++) {
            String username = dis.readUTF();
            namelist[i] = username;
        }
        chatFrame.updateOnlineUsers(namelist);
    }

    private void getMsgToWin() throws Exception {
        String msg=dis.readUTF();
        System.out.println("收到服务端消息："+msg);
        chatFrame.setMsgToWin(msg);
    }
}