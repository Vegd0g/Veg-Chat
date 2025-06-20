package ui;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientChatFrame extends JFrame {
    public JTextArea smsContent = new JTextArea(23, 50);
    private JTextArea smsSend = new JTextArea(4, 40);
    public JList<String> onLineUsers = new JList<>();
    private JButton sendBn = new JButton("发送");
    private Socket socket;

    public ClientChatFrame() {
        initView();
        this.setVisible(true);
    }

    public ClientChatFrame(String name, Socket socket){
        this();
        this.setTitle(name);
        this.socket = socket;
        new ClientReaderThread(socket, this).start();
    }

    private void initView() {
        this.setSize(700, 600);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 关闭窗口，退出程序
        this.setLocationRelativeTo(null); // 窗口居中

        // 设置窗口背景色
        this.getContentPane().setBackground(new Color(0xf0, 0xf0, 0xf0));

        // 设置字体
        Font font = new Font("SimKai", Font.PLAIN, 14);

        // 消息内容框
        smsContent.setFont(font);
        smsContent.setBackground(new Color(0xdd, 0xdd, 0xdd));
        smsContent.setEditable(false);

        // 发送消息框
        smsSend.setFont(font);
        smsSend.setWrapStyleWord(true);
        smsSend.setLineWrap(true);

        // 在线用户列表
        onLineUsers.setFont(font);
        onLineUsers.setFixedCellWidth(120);
        onLineUsers.setVisibleRowCount(13);

        // 创建底部面板
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(0xf0, 0xf0, 0xf0));

        // 消息输入框
        JScrollPane smsSendScrollPane = new JScrollPane(smsSend);
        smsSendScrollPane.setBorder(BorderFactory.createEmptyBorder());
        smsSendScrollPane.setPreferredSize(new Dimension(500, 50));

        // 发送按钮
        sendBn.setFont(font);
        sendBn.setBackground(Color.decode("#009688"));
        sendBn.setForeground(Color.WHITE);

        // 按钮面板
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        btns.setBackground(new Color(0xf0, 0xf0, 0xf0));
        btns.add(sendBn);
        //给发送按钮绑定点击事件
        sendBn.addActionListener(e -> {
            String msg = smsSend.getText();
            smsSend.setText("");
            sendMsg(msg);
        });

        // 添加组件
        bottomPanel.add(smsSendScrollPane, BorderLayout.CENTER);
        bottomPanel.add(btns, BorderLayout.EAST);

        // 用户列表面板
        JScrollPane userListScrollPane = new JScrollPane(onLineUsers);
        userListScrollPane.setBorder(BorderFactory.createEmptyBorder());
        userListScrollPane.setPreferredSize(new Dimension(120, 500));

        // 中心消息面板
        JScrollPane smsContentScrollPane = new JScrollPane(smsContent);
        smsContentScrollPane.setBorder(BorderFactory.createEmptyBorder());

        // 添加所有组件
        this.add(smsContentScrollPane, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);
        this.add(userListScrollPane, BorderLayout.EAST);
    }

    public static void main(String[] args) {
        new ClientChatFrame();
    }

    public void updateOnlineUsers(String[] users){
        onLineUsers.setListData(users);
    }

    public void setMsgToWin(String msg){
        smsContent.append(msg);
    }

    private void sendMsg(String msg){
        // 发送消息给服务器
        try {
            DataOutputStream dos =
                    new DataOutputStream(socket.getOutputStream());
            dos.writeInt(2);
            dos.writeUTF(msg);
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}