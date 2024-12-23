package src.Frame;

import src.Interface.AddFriendFrameInterface;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A class representing the frame to send friend requests to another users, view a list of all the application's users,
 * search a specific user among all the application's users, view the requested friend list and the pending friend list.
 * Three JScrollPane, each contains a JTable.
 * The center one would have a table that contains all users where you can choose users to send friend request
 * The left one would have a table that record all users which you have sent request to but not get respond
 * You can choose to resend request.
 * The right one would have a table that contains all users which have sent you a request. You can either accept or deny
 *
 * @author Tharun Kumar Senthilkumar & Eashan & Abdullah Haris
 * @version Dec 8, 2024
 */
public class AddFriendFrame extends JPanel implements Runnable, AddFriendFrameInterface {

    private final String[] columnName = {"Name", "ID", "About Me"};

    JFrame addFriendFrame;
    JButton back;
    JScrollPane jScrollPane;
    JScrollPane jScrollPane2;
    JScrollPane jScrollPane3;

    JPanel panel;

    JMenuItem accept;
    JMenuItem deny;

    DefaultTableModel allUserModel;
    DefaultTableModel requestModel;
    DefaultTableModel pendingModel;

    JTable allUserTable;
    JTable requestTable;
    JTable pendingTable;
    TableRowSorter<TableModel> rowSorter;
    JTextField jtfFilter;
    JMenuItem sendFriendRequest;
    JMenuItem viewProfile;

    JMenuItem resendRequest;

    Socket socket;
    BufferedReader bufferedReader;
    PrintWriter printWriter;
    String userId;

    ActionListener actionListener = new ActionListener() {
        /**
         * ActionLister for back button and menu items.
         *
         * @param e object of the ActionEvent
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == back) {
                SwingUtilities.invokeLater(new UserFrame(socket, userId));
                addFriendFrame.dispose();
                return;
            }
            if (e.getSource() == viewProfile) {
                int selectedRow = allUserTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null,
                            "You must first select a line! ", "No selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String profileOwnerId = String.valueOf(allUserTable.getValueAt(selectedRow, 1));
                SwingUtilities.invokeLater(new ProfileDisplayFrame(socket, userId,
                        profileOwnerId, "Frame.AddFriendFrame"));
                addFriendFrame.dispose();
            }
            if (e.getSource() == sendFriendRequest) {
                int selectedRow = allUserTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null,
                            "You must first select a line! ", "No selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String friendId = String.valueOf(allUserTable.getValueAt(selectedRow, 1));
                printWriter.println("RequestFriend");
                printWriter.println(userId);
                printWriter.println(friendId);
                printWriter.flush();
                String result = null;
                try {
                    result = bufferedReader.readLine();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Buffer reader error in request Friend",
                            "Request Friend Error", JOptionPane.ERROR_MESSAGE);
                }
                if (result != null) {
                    if (result.equals("RequestSuccess")) {
                        JOptionPane.showMessageDialog(null,
                                "You have successfully sent a friend request!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null,
                                result, "Wrong Function", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
            if (e.getSource() == accept) {
                int selectedRow = pendingTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null,
                            "You must first select a line! ", "No selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String friendId = String.valueOf(pendingTable.getValueAt(selectedRow, 1));
                printWriter.println("AcceptFriend");
                printWriter.println(userId);
                printWriter.println(friendId);
                printWriter.flush();
                String result = null;
                try {
                    result = bufferedReader.readLine();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Buffer reader error in accept Friend request",
                            "Accept Friend Error", JOptionPane.ERROR_MESSAGE);
                }
                assert result != null;
                if (result.equals("AcceptSuccess")) {
                    JOptionPane.showMessageDialog(null,
                            "You have successfully accepted a request!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                            result, "Wrong Function", JOptionPane.WARNING_MESSAGE);
                }
            }
            if (e.getSource() == deny) {
                int selectedRow = pendingTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null,
                            "You must first select a line! ", "No selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String friendId = String.valueOf(pendingTable.getValueAt(selectedRow, 1));
                printWriter.println("DenyFriend");
                printWriter.println(userId);
                printWriter.println(friendId);
                printWriter.flush();
                String result = null;
                try {
                    result = bufferedReader.readLine();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Buffer reader error in deny Friend request",
                            "Accept Friend Error", JOptionPane.ERROR_MESSAGE);
                }
                assert result != null;
                if (result.equals("DenySuccess")) {
                    JOptionPane.showMessageDialog(null,
                            "You have successfully denied a request!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                            result, "Wrong Function", JOptionPane.WARNING_MESSAGE);
                }
            }
            if (e.getSource() == resendRequest) {
                int selectedRow = requestTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null,
                            "You must first select a line! ", "No selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String friendId = String.valueOf(requestTable.getValueAt(selectedRow, 1));
                printWriter.println("ResendRequest");
                printWriter.println(userId);
                printWriter.println(friendId);
                printWriter.flush();
                String result = null;
                try {
                    result = bufferedReader.readLine();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Buffer reader error in deny Friend request",
                            "Accept Friend Error", JOptionPane.ERROR_MESSAGE);
                }
                assert result != null;
                if (result.equals("ResendSuccess")) {
                    JOptionPane.showMessageDialog(null,
                            "You have successfully resent a friend request!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else if (result.equals("RequestExisted")) {
                    JOptionPane.showMessageDialog(null,
                            "Be patient, your request is already received.",
                            "Request Exists", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                            result, "Wrong Function", JOptionPane.WARNING_MESSAGE);
                }
            }
            updateAll();
        }
    };

    /**
     * The constructor so that socket and the login userId can be passed.
     *
     * @param socket
     * @param userId
     */
    public AddFriendFrame(Socket socket, String userId) {
        this.socket = socket;
        this.userId = userId;
    }

    /**
     * Run method
     * Initialize three scrollPane, each contains a JTable
     * A search bar is implemented using a JTextField and a RowSorter.
     */
    @Override
    public void run() {

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Unable to initialize", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        allUserModel = updateAllUserModel();
        requestModel = updateRequestModel();
        pendingModel = updatePendingModel();

        allUserTable = new JTable(allUserModel);
        requestTable = new JTable(requestModel);
        pendingTable = new JTable(pendingModel);

        rowSorter = new TableRowSorter<>(allUserTable.getModel());
        jtfFilter = new JTextField(10);

        addFriendFrame = new JFrame("Add Friend");
        allUserTable.setRowSorter(rowSorter);

        jScrollPane = new JScrollPane(allUserTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane2 = new JScrollPane(pendingTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane3 = new JScrollPane(requestTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        //JPopupMenu on user list
        JPopupMenu popupMenu = new JPopupMenu();
        sendFriendRequest = new JMenuItem("Send Friend Request");
        viewProfile = new JMenuItem("View Profile");
        popupMenu.add(viewProfile);
        popupMenu.add(sendFriendRequest);
        allUserTable.setComponentPopupMenu(popupMenu);

        //JPopupMenu on pending list
        JPopupMenu popupMenu2 = new JPopupMenu();
        accept = new JMenuItem("Accept");
        deny = new JMenuItem("Deny");
        popupMenu2.add(accept);
        popupMenu2.add(deny);
        pendingTable.setComponentPopupMenu(popupMenu2);

        JPopupMenu popupMenu3 = new JPopupMenu();
        resendRequest = new JMenuItem("Resend request");
        popupMenu3.add(resendRequest);
        requestTable.setComponentPopupMenu(popupMenu3);

        JLabel friend = new JLabel("Requested Friend", SwingConstants.CENTER);
        JLabel sentRequest = new JLabel("Pending List", SwingConstants.CENTER);

        //JFrame Design
        addFriendFrame.setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout());
        back = new JButton("Back");
        top.add(new JLabel("Find a specific friend"));
        top.add(jtfFilter);
        top.add(back);

        top.setVisible(true);
        panel = new JPanel(new FlowLayout());
        jScrollPane.setPreferredSize(new Dimension(300, 400));
        jScrollPane2.setPreferredSize(new Dimension(300, 400));
        jScrollPane3.setPreferredSize(new Dimension(300, 400));
        JPanel left = new JPanel(new BorderLayout());
        left.add(friend, BorderLayout.NORTH);
        left.add(jScrollPane3, BorderLayout.CENTER);
        left.setVisible(true);
        JPanel center = new JPanel(new BorderLayout());
        center.add(top, BorderLayout.NORTH);
        center.add(jScrollPane, BorderLayout.CENTER);
        center.setVisible(true);
        JPanel right = new JPanel(new BorderLayout());
        right.add(sentRequest, BorderLayout.NORTH);
        right.add(jScrollPane2, BorderLayout.CENTER);
        right.setVisible(true);
        panel.setVisible(true);
        panel.add(left);
        panel.add(center);
        panel.add(right);
        addFriendFrame.add(panel, BorderLayout.CENTER);

        jtfFilter.getDocument().addDocumentListener(new DocumentListener() {
            /**
             * Document listener for inserting text
             * @param e invoked when the text in the jtfFilter JTextField is inserted.
             */
            @Override
            public void insertUpdate(DocumentEvent e) {
                String text = jtfFilter.getText();

                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            /**
             * Document listener for removing text
             * @param e invoked when the text in the jtfFilter JTextField is removed.
             */
            @Override
            public void removeUpdate(DocumentEvent e) {
                String text = jtfFilter.getText();

                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            /**
             * Document lister for changing of other attribute than text.
             * @param e DocumentEvent e which is when user change the text inside the jtfFilter JTextField
             */
            @Override
            public void changedUpdate(DocumentEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
                //To change body of generated methods, choose Tools | Templates.
            }
        });

        //ActionListener
        back.addActionListener(actionListener);
        accept.addActionListener(actionListener);
        deny.addActionListener(actionListener);
        viewProfile.addActionListener(actionListener);
        sendFriendRequest.addActionListener(actionListener);
        resendRequest.addActionListener(actionListener);

        addFriendFrame.pack();
        addFriendFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addFriendFrame.setLocationRelativeTo(null);
        // Add a windowListener to close the bufferedReader, printWriter, and socket when user close by pressing the "x"
        addFriendFrame.addWindowListener(new WindowAdapter() {
            /**
             * @param e Invoked when a window is in the process of being closed.
             *          The close operation can be overridden at this point.
             */
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    bufferedReader.close();
                    printWriter.close();
                    socket.close();
                    addFriendFrame.dispose();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        addFriendFrame.setVisible(true);
    }

    /**
     * updateAllUserModel method
     * Communicate with the server to get most update user list.
     * @return A DefaultTableModel with the updated data as its column.
     */
    @Override
    public DefaultTableModel updateAllUserModel() {
        String[][] rowData = new String[0][0];
        printWriter.println("GetUserList");
        printWriter.println(userId);
        printWriter.flush();
        String result = null;
        try {
            result = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert result != null;
        if (result.equals("Empty")) {
            rowData = null;
        } else {
            try {
                int i = Integer.parseInt(result);
                rowData = new String[i][3];
                for (int j = 0; j < i; j++) {
                    String name = bufferedReader.readLine();
                    String id = bufferedReader.readLine();
                    String aboutMe = bufferedReader.readLine();
                    rowData[j][0] = name;
                    rowData[j][1] = id;
                    rowData[j][2] = aboutMe;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new DefaultTableModel(rowData, columnName) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /**
     * updatePendingModel method
     * Communicate with the server to get the updated user info in the pending list
     * @return A DefaultTableModel with the updated data as its column.
     */
    @Override
    public DefaultTableModel updatePendingModel() {
        String[][] rowData = new String[0][0];
        printWriter.println("GetPendingList");
        printWriter.println(userId);
        printWriter.flush();
        String result = null;
        try {
            result = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert result != null;
        if (result.equals("Empty")) {
            rowData = null;

        } else if (result.equals("NotFound")) {
            rowData = null;
            JOptionPane.showMessageDialog(null,
                    "Unable to find Id", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                int i = Integer.parseInt(result);
                rowData = new String[i][3];
                for (int j = 0; j < i; j++) {
                    String name = bufferedReader.readLine();
                    String id = bufferedReader.readLine();
                    String aboutMe = bufferedReader.readLine();
                    rowData[j][0] = name;
                    rowData[j][1] = id;
                    rowData[j][2] = aboutMe;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new DefaultTableModel(rowData, columnName) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /**
     * updateRequestModel method
     * Communicate with the server to get the updated user info in the request list
     * @return A DefaultTableModel with the updated data as its column.
     */
    @Override
    public DefaultTableModel updateRequestModel() {
        String[][] rowData = new String[0][0];
        printWriter.println("GetRequestList");
        printWriter.println(userId);
        printWriter.flush();
        String result = null;
        try {
            result = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert result != null;
        if (result.equals("Empty")) {
            rowData = null;

        } else if (result.equals("NotFound")) {
            rowData = null;
            JOptionPane.showMessageDialog(null,
                    "Unable to find Id", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                int i = Integer.parseInt(result);
                rowData = new String[i][3];
                for (int j = 0; j < i; j++) {
                    String name = bufferedReader.readLine();
                    String id = bufferedReader.readLine();
                    String aboutMe = bufferedReader.readLine();
                    rowData[j][0] = name;
                    rowData[j][1] = id;
                    rowData[j][2] = aboutMe;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new DefaultTableModel(rowData, columnName) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /**
     * updateAll method
     * Update the date by changing the model of all three tables
     * reset the rowSorter to make sure the search bar keep working
     * Then repaint the JFrame addFriendFrame
     */
    @Override
    public void updateAll() {
        allUserTable.setModel(updateAllUserModel());
        requestTable.setModel(updateRequestModel());
        pendingTable.setModel(updatePendingModel());
        rowSorter.setModel(allUserTable.getModel());
        allUserTable.setRowSorter(rowSorter);
        addFriendFrame.repaint();
    }
}
