package Server;

import javax.swing.*;

import Model.Data;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

public class Server extends JFrame {

    private static final long serialVersionUID = 1L;
    private Container container;
    private JButton btnSend, btnStart;
    private JTextField tfChat, tfPort, tfIP;
    private JPanel panelNorth, panelSouth, panelEast;
    private ServerSocket serverSocket;
    private JTextArea textArea;
    private boolean isStart = false;

    public static void main(String[] args) {
        new Server("Server Caro");
    }

    public Server(String s) {
        super(s);

        container = this.getContentPane();
        container.setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setColumns(50);
        textArea.setEditable(false);
        container.add(textArea, "Center");

        panelNorth = new JPanel();
        btnStart = new JButton("Start Server");
        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });
        try {
            tfIP = new JTextField(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        tfIP.setColumns(20);
        tfPort = new JTextField("9000");
        tfPort.setColumns(5);
        panelNorth.add(tfIP);
        panelNorth.add(tfPort);
        panelNorth.add(btnStart);
        container.add(panelNorth, "North");

        panelSouth = new JPanel();
        tfChat = new JTextField();
        tfChat.setColumns(30);
        panelSouth.add(tfChat);
        container.add(panelSouth, "South");

        panelEast = new JPanel();
        panelEast.setLayout(new BoxLayout(panelEast, BoxLayout.Y_AXIS));
        btnSend = new JButton("Send");
        panelEast.add(btnSend);
        container.add(panelEast, "East");


        setSize(600,400);
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Server Game Caro");
    }

    private void startServer() {
        if(isStart) {
//      stop server
            try {
                serverSocket.close();
                isStart = false;
                btnStart.setText("Start");
                textArea.append("Server stop!.\n");
            } catch (Exception e) {
                textArea.append("Try again.\n");
                Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
            }
        } else {
//      start server
            try {
                int port = Integer.parseInt(tfPort.getText());
                serverSocket = new ServerSocket(port);
                textArea.append("Start server ...\n");
                btnStart.setText("Stop");
                isStart = true;
                // tạo luồng lắng nghe thêm client khi connect
                ListenClient lClient = new ListenClient(serverSocket, textArea);
                lClient.start();
                // luồng đọc và gửi tin nhắn về cho các client

            } catch(NumberFormatException ne) {
                textArea.append("Port là số nguyên\n");
            } catch (Exception e) {
                textArea.append("Server not start\n");
                Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
            }
        }
    }
    
}
// luồng lắng nghe khi có client tham gia và thêm vào listArray
class ListenClient extends Thread {

    private ServerSocket server;
    private JTextArea textArea;
    private HashMap<Integer, Socket> listClient;
    private DataOutputStream dos;

    public ListenClient(ServerSocket server, JTextArea textArea) {
        this.server = server;
        this.textArea = textArea;
        this.listClient = new HashMap<>();
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            while(true) {
                if(listClient.size() < 2) {
                    socket = server.accept();
                    int port = socket.getPort();
                    listClient.put(port, socket);
                    textArea.append(socket.getInetAddress().getHostAddress() + " : " + socket.getPort() + " connecting...\n");
                    // System.out.println(socket.getInetAddress());
                    // tạo luồng đọc và gửi tin cho client đã join
                    ReadServer read = new ReadServer(socket, textArea,dos ,listClient);
                    read.start();
                }
            }
        } catch (EOFException ee) {
            textArea.append("Client ngắt kết nối");
        } catch (Exception e) {
            try {
                socket.close();
            } catch (Exception e1) {
                
            }
            Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
            e.printStackTrace();
        }
    }
}
// luồng đọc tin nhắn
class ReadServer extends Thread {

    private Socket socket;
    private JTextArea textArea;
    private DataOutputStream dos;
    private HashMap<Integer, Socket> listClient;

	public ReadServer(Socket socket,JTextArea textArea, DataOutputStream dos , HashMap<Integer, Socket> listClient) {
        this.socket = socket;
        this.textArea = textArea;
        this.dos = dos;
        this.listClient = listClient;
	}

	@Override
	public void run() {
        DataInputStream dis = null;
		try {
            dis = new DataInputStream(socket.getInputStream());
			while(true) {
                String sms = dis.readUTF();
                Set<Integer> keySet = listClient.keySet();
                textArea.append(socket.getPort() + ": " + sms+"\n");
                if(sms.contains(Data.SMS)) {
                    textArea.append(socket.getPort() + ": " + sms+"\n");
                    for(Integer key : keySet) {
                        // System.out.println(key);
                        if(key != socket.getPort()) {
                            dos = new DataOutputStream(listClient.get(key).getOutputStream());
                            dos.writeUTF(sms);
                            dos.flush();
                        }
                    }
                } else if(sms.contains(Data.CARO)) {
                    textArea.append(socket.getPort() + ": " + sms+"\n");
                    for(Integer key : keySet) {
                        if(key != socket.getPort()) {
                            dos = new DataOutputStream(listClient.get(key).getOutputStream());
                            dos.writeUTF(sms);
                            dos.flush();
                        }
                    }
                } else if(sms.contains(Data.WIN)) {
                    for(Integer key : keySet) {
                        if(key != socket.getPort()) {
                            dos = new DataOutputStream(listClient.get(key).getOutputStream());
                            dos.writeUTF(sms);
                            dos.flush();
                        }
                    }
                }
                
            }
        } catch (EOFException et) {
            return;
        } catch (Exception e) {
            try {
                dis.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
		}
    }

}
