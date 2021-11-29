package Model;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;

public class Board extends JPanel {

    private static final long serialVersionUID = 1L;

    private Image imgX;
    private Image imgO;
    public static JTextArea textArea;
    public static Socket client;
    public static String currentPlayer = Data.O_VALUE;

    public Board(Socket client, JTextArea textArea) {
        Board.client = client;
        Board.textArea = textArea;
        this.initMatrix();

        try {
            imgO = ImageIO.read(new File("./src/Images/O.png"));
            imgX = ImageIO.read(new File("./src/Images/X.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        JPanel board = this;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // true là lượt của bạn, false là lượt đối thủ
                if (Data.round) {
                    int x = e.getX();
                    int y = e.getY();
                    for (int i = 0; i < Data.ROW; i++) {
                        for (int j = 0; j < Data.COL; j++) {
                            Cell cell = Data.matrix[i][j];

                            int cellXStart = cell.getX();
                            int cellYStart = cell.getY();

                            int cellXEnd = cellXStart + cell.getW();
                            int cellYEnd = cellYStart + cell.getH();

                            if (x >= cellXStart && x <= cellXEnd && y >= cellYStart && y <= cellYEnd) {
                                if (cell.getValue().equals(Data.EMPTY_VALUE)) {
                                    board.setEnabled(true);

                                    drawChess(x, y, board, currentPlayer);
                                    Data.round = false;
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public static void drawChess(int x, int y, JPanel p, String current) {
        // tính toán x, y rơi trong board
        for (int i = 0; i < Data.ROW; i++) {
            for (int j = 0; j < Data.COL; j++) {
                Cell cell = Data.matrix[i][j];

                int cellXStart = cell.getX();
                int cellYStart = cell.getY();

                int cellXEnd = cellXStart + cell.getW();
                int cellYEnd = cellYStart + cell.getH();

                if (x >= cellXStart && x <= cellXEnd && y >= cellYStart && y <= cellYEnd) {
                    if (cell.getValue().equals(Data.EMPTY_VALUE) && current.equals(currentPlayer)) {
                        cell.setValue(current);
                        String result = checkWin(currentPlayer, i, j);
                        p.repaint();
                        Data.round = true;
                        // trường hợp win
                        if (result.equals(Data.WIN)) {
                            System.out.println("Bạn đã thắng");
                            textArea.append("You are Win!\n");
                            send(Data.WIN);
                        } else if (result.equals(Data.NORMAL)) {
                            // đảo ngược lược chơi
                            String sms = Data.CARO + Data.EMPTY_VALUE + currentPlayer + x + ":" + y;
                            send(sms);
                            currentPlayer = currentPlayer.equals(Data.O_VALUE) ? Data.X_VALUE : Data.O_VALUE;
                        } else {
                            textArea.append("Game Draw!\n");
                            send(Data.DRAW);
                        }
                    }
                }
            }
        }
    }

    public static void send(String sms) {
        try {
            // send
            DataOutputStream dos = new DataOutputStream(client.getOutputStream());
            dos.writeUTF(sms);
            System.out.println(sms);
            dos.flush();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    // Khởi tạo matrix
    private void initMatrix() {
        for (int i = 0; i < Data.ROW; i++) {
            for (int j = 0; j < Data.COL; j++) {
                Cell cell = new Cell();
                Data.matrix[i][j] = cell;
            }
        }
    }

    // vẽ bàn cờ game caro bằng canvas
    @Override
    public void paint(Graphics g) {
        int a = getWidth() / Data.ROW;
        Graphics2D graphics2d = (Graphics2D) g;
        // xóa các đối tượng trước khi repaint
        g.clearRect(0, 0, getWidth(), getHeight());
        // int k = 0;
        for (int i = 0; i < Data.ROW; i++) {
            for (int j = 0; j < Data.COL; j++) {
                int x = a * j;
                int y = a * i;

                Cell cell = Data.matrix[i][j];
                cell.setX(x);
                cell.setY(y);
                cell.setH(a);
                cell.setW(a);

                graphics2d.drawRect(x, y, a, a);

                if (cell.getValue().equals(Data.X_VALUE)) {
                    graphics2d.drawImage(imgX, x, y, a, a, this);
                } else if (cell.getValue().equals(Data.O_VALUE)) {
                    graphics2d.drawImage(imgO, x, y, a, a, this);
                }
            }
        }

    }

    // 0: hòa(hết ô), 1: thắng, 2: chưa thắng
    public static String checkWin(String player, int i, int j) {

        // biến count đếm 5 dấu O or X liên tiếp, nếu khác với giá trị trước sẽ gán lại
        // == 0
        int count = 0;
        // i là hàng, j là cột
        int min = Math.min(i, j);
        int topI = 0;
        int topJ = 0;
        // check chiều ngang
        for (int col = 0; col < Data.COL; col++) {
            Cell cell = Data.matrix[i][col];
            System.out.println(cell.getValue());
            if (cell.getValue().equals(player)) {
                count++;
                if (count == 5)
                    return Data.WIN;
            } else {
                count = 0;
            }
        }
        // chiều dọc
        count = 0;
        for (int row = 0; row < Data.ROW; row++) {
            Cell cell = Data.matrix[row][j];
            if (cell.getValue().equals(player)) {
                count++;
                if (count == 5)
                    return Data.WIN;
            } else {
                count = 0;
            }
        }
        // chéo từ trái sang phải
        count = 0;
        topI = i - min;
        topJ = j - min;
        for (; topI < Data.ROW && topJ < Data.COL; topI++, topJ++) {
            Cell cell = Data.matrix[topI][topJ];
            if (cell.getValue().equals(player)) {
                count++;
                if (count == 5)
                    return Data.WIN;
            } else {
                count = 0;
            }
        }
        // chéo từ phải sang trái
        count = 0;
        topJ = j + min;
        topI = i - min;
        if (topJ >= Data.COL) {
            topI = topI + topJ - (Data.COL - 1);
            topJ = Data.COL - 1;
        }
        // System.out.println("i: " + topI + "j: " + topJ);
        for (; topI < Data.ROW && topJ >= 0; topI++, topJ--) {
            Cell cell = Data.matrix[topI][topJ];
            if (cell.getValue().equals(player)) {
                count++;
                // System.out.println("count: "+count);
                if (count == 5)
                    return Data.WIN;
            } else {
                count = 0;
            }
        }
        // trường hợp hòa
        if (isFullBoard())
            return Data.DRAW;
        //
        return Data.NORMAL;
    }

    // kiểm tra hết ô trống
    public static boolean isFullBoard() {
        int number = Data.COL * Data.ROW;
        int k = 0;
        for (int i = 0; i < Data.ROW; i++) {
            for (int j = 0; j < Data.COL; j++) {
                Cell cell = new Cell();
                if (!cell.getValue().equals(Data.EMPTY_VALUE))
                    k++;
            }
        }
        return k == number;
    }

}