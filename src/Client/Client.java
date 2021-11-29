package Client;

import javax.swing.*;
import Model.Board;
import Model.Data;

import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.*;
import java.util.logging.Logger;

public class Client extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Container container;
    private Board board;
    private JPanel panelBoard, panelRight, panelSend, panelConnect;
    private JTextField tfChat, tfIP, tfPort, tfNamePlayer;
    private JTextArea textArea;
    private JButton btnSend, btnConnect;
    private Socket client;
    private boolean isConnect = false;

    public Client(String s) {
        super(s);
        container = this.getContentPane();
        container.setLayout(new BorderLayout());

        panelConnect = new JPanel();
        try {
            tfIP = new JTextField(InetAddress.getLocalHost().getHostAddress());
            tfIP.setColumns(10);
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
        }
        tfPort = new JTextField("9000");
        tfPort.setColumns(10);
        tfNamePlayer = new JTextField("");
        tfNamePlayer.setColumns(10);
        btnConnect = new JButton("Connect");
        btnConnect.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!tfNamePlayer.getText().isEmpty() && !tfIP.getText().isEmpty() && !tfPort.getText().isEmpty()) {
                    Connect();
                    board = new Board(client, textArea);
                    board.setPreferredSize(new Dimension(600, 600));
                    panelBoard.add(board);
                    container.add(panelBoard, "Center");
                    container.repaint();
                } else {
                    textArea.append("Vui lòng nhập đầy đủ thông tin. \n");
                }
            }

        });
        panelConnect.add(tfNamePlayer);
        panelConnect.add(tfIP);
        panelConnect.add(tfPort);
        panelConnect.add(btnConnect);
        container.add(panelConnect, "North");

        panelBoard = new JPanel();

        panelRight = new JPanel();
        panelRight.setLayout(new BorderLayout());
        textArea = new JTextArea();
        textArea.setColumns(30);
        textArea.setEditable(false);

        panelRight.add(textArea, "Center");
        panelSend = new JPanel();
        tfChat = new JTextField();
        tfChat.setColumns(20);
        btnSend = new JButton("Send");

        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Send();
            }
        });
        panelSend.add(tfChat);
        panelSend.add(btnSend);
        panelRight.add(panelSend, "South");
        container.add(panelRight, "East");
        setVisible(true);
        setSize(new Dimension(1000, 600));
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void Connect() {
        if (!isConnect) {
            // connect
            try {
                String strIP = tfIP.getText();
                int port = Integer.parseInt(tfPort.getText());
                client = new Socket(strIP, port);
                isConnect = true;
                btnConnect.setText("Stop");
                textArea.append("Connect successfull!\n");
                // tạo luồng nhận tin nhấn từ client khác
                ReadClient read = new ReadClient(new DataInputStream(client.getInputStream()), textArea, panelBoard);
                read.start();
            } catch (NumberFormatException ne) {
                textArea.append("Port is number.\n");
            } catch (ConnectException ce) {
                textArea.append("Server is not work.\n");
            } catch (Exception e) {
                Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
                textArea.append("Can't not server!\n");
            }
        } else {
            // disconnect
            try {
                System.out.println(client.getPort());
                client.close();
                isConnect = false;
                btnConnect.setText("Connect");
                textArea.append("Disconnect ...\n");
            } catch (SocketException se) {
                se.printStackTrace();
            } catch (Exception e) {
                textArea.append("Try again.\n");
                Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
            }
        }
    }

    private void Send() {
        if (isConnect && !tfChat.getText().isEmpty()) {
            try {
                // lấy string send
                String sms = tfNamePlayer.getText() + ": " + tfChat.getText();
                // send
                DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                dos.writeUTF(Data.SMS + sms);
                textArea.append(sms + "\n");
                tfChat.setText("");
                dos.flush();
            } catch (Exception e) {
                textArea.append("Không gửi được tin.\n");
                Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, e);
            }
        }
    }

}

// luồng nhận tin nhắn gửi về
class ReadClient extends Thread {

    private DataInputStream dis;
    private JTextArea textArea;
    private JPanel p;

    public ReadClient(DataInputStream dis, JTextArea textArea, JPanel p) {
        this.dis = dis;
        this.textArea = textArea;
        this.p = p;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String sms = dis.readUTF();
                if (sms.contains(Data.SMS)) {
                    textArea.append(sms.replace(Data.SMS, "") + "\n");
                } else if (sms.contains(Data.CARO)) {
                    // textArea.append("SMS: "+ sms);
                    sms = sms.replace(Data.CARO, "");
                    String player = "";
                    textArea.append("SMS last: " + sms + "\n");
                    if (sms.contains(Data.O_VALUE)) {
                        player = Data.O_VALUE;
                        sms = sms.replace(Data.O_VALUE, "");
                    } else {
                        player = Data.X_VALUE;
                        sms = sms.replace(Data.X_VALUE, "");
                    }
                    String[] arrOfStr = sms.split(":", 5);
                    int x = Integer.parseInt(arrOfStr[0]);
                    int y = Integer.parseInt(arrOfStr[1]);
                    // textArea.append("x: "+ x + ",y:" + y + "\n");
                    // Data.round = true;
                    Board.drawChess(x, y, p, player);
                } else if (sms.contains(Data.WIN)) {
                    textArea.append("You are lose!\n");
                } else if (sms.contains(Data.DRAW)) {
                    textArea.append("Game Draw!\n");
                }

            }
        } catch (SocketException se) {
            return;
        } catch (EOFException ee) {
            return;
        } catch (Exception e) {
            try {
                dis.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
            Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, e);

        }
    }

}