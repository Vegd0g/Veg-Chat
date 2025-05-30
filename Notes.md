# 即时通讯系统

架构分析：采用CS架构，即客户端-服务端的架构

主要实现以下功能：

1. 输入用户名登录
2. 局域网群聊功能（信息发送，更新）

## 客户端

客户端接收服务端传来的通信，可能包含以下几种：

1. 服务端处理后的用户信息，展示在ui上
2. 服务端处理完的更新，比如消息的更新和聊天室人数的更新

## 服务端

服务端接收来自客户端的信息：

1. 登录信息，包含登录名
2. 消息信息，包含消息内容

> 对于每个客户端，服务端都应该创建一个与之对应的线程处理信息

## 服务端代码实现

### 服务端架构

chat-server
├── src
│   └── server
│       ├── Constant.java
│       ├── Server.java
│       └── ServerReaderThread.java
└── chat-server.iml

对于每个上线客户端，都为其分配一个线程类，用来处理业务逻辑

### Server.java----服务端核心代码：

```java
public class Server {
    public static final Map<Socket, String> onLineSockets = new HashMap<>();
    public static void main(String[] args) {
        System.out.println("启动服务端系统.....");
        try {
            ServerSocket serverSocket = 
                new ServerSocket(Constant.PORT);
            while (true) {
                System.out.println("等待客户端的连接.....");
                Socket socket = serverSocket.accept();
                new ServerReaderThread(socket).start();
                System.out.println
                    ("一个客户端连接成功.....");
            }
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
}
```

+ `public static final Map<Socket, String> onLineSockets = new HashMap<>();`：定义一个Map集合，用来存放所有的上线线程，也就是上线的客户端
+ `ServerSocket serverSocket = new ServerSocket(Constant.PORT);`：创建了一个绑定到本地 8080 端口的 ServerSocket 对象，之后该端口就会等待客户端的连接请求

+ `Socket socket = serverSocket.accept();`建立一个Socket管道，与ServerSocket收到的客户端管道连接，获取输入输出流，从而实现通讯

+ `new ServerReaderThread(socket).start();`：new一个线程类，会触发构造函数，并调用了start()方法启动线程

### ServerReaderThread.java----线程类

```java
public class ServerReaderThread extends Thread{
    private Socket socket;
    public ServerReaderThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {            
            DataInputStream dis = new 
                DataInputStream(socket.getInputStream());
            while (true) {
                int type = dis.readInt();
                switch (type){
                    case 1:
                        String nickname = dis.readUTF();
                        Server.onLineSockets.put
                            (socket, nickname);
                        updateClientOnLineUserList();
                        break;
                    case 2:
                        String msg = dis.readUTF();
                        sendMsgToAll(msg);
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("客户端下线了："+                           socket.getInetAddress().getHostAddress());
            Server.onLineSockets.remove(socket); 
            updateClientOnLineUserList(); 
        }
    }
```

+ `构造器`：new的时候传入一个socket，并把ServerReaderThread类自身的socket字段设为传入的客户端连接socket

+ `DataInputStream dis = new DataInputStream (socket.getInputStream());`：包装getInputStream()成为DataInputStream

  > 包装成DataInputStream的好处：封装数据流的读取
  >
  > - 它封装了底层的字节流读取操作，使得开发者可以更方便地处理基于数据块的输入。例如，在处理网络通信中的数据包时，可以使用 `DataInputStream` 依次读取数据包中的各个字段，每个字段可以是不同的数据类型。
  >
  >   提高读取效率
  >
  > - `DataInputStream` 在内部进行了优化。它在**缓冲区**缓冲输入流中的数据，减少了底层 I/O 操作的次数，从而提高了读取的效率。

+ `while (true)`：无限循环，持续接收客户端消息

+ `int type = dis.readInt()`：读取客户端传来的信号信息，用来判断是登录操作还是发送消息操作

+ `case 1`（登录操作）:
  + `String nickname = dis.readUTF();`：读取客户端输入的用户名
  + `Server.onLineSockets.put(socket, nickname);`：把这个客户端线程加入Map集合，socket作为键，nickname作为值
  + `updateClientOnLineUserList();`：更新在线客户端列表，后面给出该函数具体实现

+ `case 2`（发送消息操作）:
  + `String msg = dis.readUTF();`：读取客户端输入信息
  + `sendMsgToAll(msg);`：把消息更新给所有的在线客户端，后面给出该函数具体实现

+ 捕捉到异常后，打印客户端下线信息，把该客户端从Map集合中移除，再更新当前在线客户端列表

### updateClientOnLineUserList()

该函数作用是更新在线客户端列表

```java
private void updateClientOnLineUserList() {
    Collection<String> onLineUsers = 
        Server.onLineSockets.values();
    for (Socket socket : 
         Server.onLineSockets.keySet()) {
        try {
            DataOutputStream dos = 
                new DataOutputStream
                (socket.getOutputStream());
            dos.writeInt(1); 
            dos.writeInt(onLineUsers.size()); 
            for (String onLineUser : onLineUsers) {
                dos.writeUTF(onLineUser);
            }
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
```

+ `Collection<String>onLineUsers = Server.onLineSockets.values();`：定义一个Collection集合，存放Map集合中所有的值，也就是代表在线客户端列表，用来把所有用户名发送给客户端

+ `for (Socket socket : Server.onLineSockets.keySet())`：遍历Map集合中的所有Socket管道，也就是对每个客户端依次进行操作

+ `dos.writeInt(1);dos.writeInt(onLineUsers.size()); `向客户端发送1，告诉客户端这是一个在线用户列表更新操作；同时告诉客户端在线的人数

+ `for (String onLineUser : onLineUsers) {dos.writeUTF(onLineUser);}`：遍历Collection，把每个用户的名字写入输出流，传给客户端

+ `dos.flush();`：刷新缓冲区，将缓冲区中已有的数据全部发送到网络上

### sendMsgToAll()

该函数作用是把消息更新给所有的在线客户端

```java
private void sendMsgToAll(String msg) {
    StringBuilder sb = new StringBuilder();
    String name = Server.onLineSockets.get(socket);
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter dtf = 
        DateTimeFormatter.ofPattern
        ("yyyy-MM-dd HH:mm:ss EEE a");
    String nowStr = dtf.format(now);
    String msgResult = sb.append(name).
        append(" ").append(nowStr).append("\r\n")
            .append(msg).append("\r\n").toString();
    for (Socket socket : 
         Server.onLineSockets.keySet()){
        try {
            DataOutputStream dos = new 
               DataOutputStream(socket.getOutputStream());
            dos.writeInt(2); 
            dos.writeUTF(msgResult);
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

+ 通过`StringBuilder`，`LocalDateTime.now()`，`DateTimeFormatter`来拼接消息字符串

+ `for (Socket socket : Server.onLineSockets.keySet())`：

  遍历所有Socket，为每个客户端发送信号2，表示这是一个发送信息请求，再把拼接好的信息写入输出流，传给客户端

  > `onLineSockets.keySet()` 会返回一个包含所有 `Socket` 对象的 `Set`集合

## 客户端代码实现

客户端ui部分代码通过ai得出，我们只需要做一下通讯部分的逻辑

### 客户端架构

chat-client/
├── src/
│   └── client.ui/
│       ├── ChatEntryFrame.java
│       ├── ClientChatFrame.java
│       ├── ClientReaderThread.java
│       ├── Constant.java
│       └── App.java
└── chat-client.iml

### ChatEntryFrame.java

登录进入界面

```java
enterButton.addActionListener(e -> {
    String nickname = nicknameField.getText();
    nicknameField.setText("");
    if (!nickname.isEmpty()) {
        try {
            login(nickname);。
            new ClientChatFrame(nickname, socket);
            this.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    } else {
        JOptionPane.showMessageDialog(this, "请输入昵称!");
    }
});
```

```java
public void login(String nickname) throws Exception {
        socket = new Socket
            (Constant.SERVER_IP, Constant.SERVER_PORT );
        DataOutputStream dos = new 
            DataOutputStream(socket.getOutputStream());
        dos.writeInt(1);
        dos.writeUTF(nickname);
        dos.flush();
    }
```

+ 为登录按钮添加监视器，若输入名字不为空，执行`login`函数，并创建一个`ClientChatFrame`聊天界面类

+ `login()`:
  + 创建Socket管道，用于后续和服务端通信
  + 发送数据1，表示这是一个登陆消息，并向服务端发送用户名

### ClientChatFrame.java

聊天界面，关于ui的部分不再展示

```java
public class ClientChatFrame extends JFrame {
	
    ......
        
    private Socket socket;

    public ClientChatFrame() {
        initView();
        this.setVisible(true);
    }

    public ClientChatFrame
        (String nickname, Socket socket) {
        this();
        this.setTitle(nickname + "的聊天窗口");
        this.socket = socket;
        new ClientReaderThread(socket, this).start();
    }

	......
    ......    

    private void sendMsgToServer(String msg) {
        try {
            DataOutputStream dos = new 
               DataOutputStream(socket.getOutputStream());
            dos.writeInt(2);
            dos.writeUTF(msg);
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ClientChatFrame();
    }

    public void updateOnlineUsers(String[] onLineNames) {
        onLineUsers.setListData(onLineNames);
    }

    public void setMsgToWin(String msg) {
        smsContent.append(msg);
    }
}
```

+ `有参构造器`：接收ChatEntryFrame传来的用户名和socket，并new一个ClientReaderThread线程类，把socket管道传给它，让它来处理聊天逻辑
+ `sendMsgToServer()`：发送信号2和消息内容给服务端
+ `updateOnlineUsers()`和`setMsgToWin()`：用于更新在线用户列表和群聊消息

### ClientReaderThread.java

用于接收服务端返回的用户列表更新或者是消息更新，道理与上面相同，不再一一解释

```java
public class ClientReaderThread extends Thread{
    private Socket socket;
    private DataInputStream dis;
    private ClientChatFrame win;
    public ClientReaderThread(Socket socket,
                              ClientChatFrame win) {
        this.win = win;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream
                (socket.getInputStream());
            while (true) {
                int type = dis.readInt();
                switch (type){
                    case 1:
                        updateClientOnLineUserList();
                        break;
                    case 2:
                        getMsgToWin();
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getMsgToWin() throws Exception {
        String msg = dis.readUTF();
        win.setMsgToWin(msg);
    }

    private void updateClientOnLineUserList() throws Exception {
        int count = dis.readInt();
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            String nickname = dis.readUTF();
            names[i] = nickname;
        }
        win.updateOnlineUsers(names);
    }
}
```

