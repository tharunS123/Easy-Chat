package src.Frame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import src.Interface.UserFrameInterface;

/**
 * A class representing the frame displaying the friend list of the user in the
 * application.
 *
 * The user is able to see the profile of users in the friend list and delete
 * them from the friend list.
 * Use a JTable to display all your friends and a search bar to find certain
 * friends.
 *
 * @author Tharun Kumar Senthilkumar & Eashan & Abdullah Haris
 * @version Dec 8, 2024
 */
public class UserFrame extends JComponent implements Runnable, UserFrameInterface {
    Socket socket;
    BufferedReader bufferedReader;
    PrintWriter printWriter;
    String userId;

    String[] columnName = { "Title", "User", "Post", "Date" };
    String[][] rowData = new String[4][4];

    DefaultTableModel model;
    JTable jTable;
    JFrame userFrame;
    TableRowSorter<TableModel> rowSorter;
    JTextField jtfFilter = new JTextField(10);
    JScrollPane jScrollPane;
    JButton add;
    JButton account;
    JButton back;
    JButton createPost;
    JPopupMenu popupMenu;
    JMenuItem viewProfile;
    JMenuItem deleteFriend;
    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");

    ArrayList<String> listOfUsersPosts = new ArrayList<>();

    ActionListener popupItemListener = new ActionListener() {
        /**
         * @param e Invoked when any of the choice of the popup menu is selected.
         *          There are two menu choices: view profile and delete friend.
         *          An if statement is used to prevent no row is selected
         *          The functionality would be be done by send a String order and a
         *          userId to the server.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem menuChoice = (JMenuItem) e.getSource();
            if (menuChoice == viewProfile) {
                int selectedRow = jTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null,
                            "You must first select a line! ", "No selection", JOptionPane.WARNING_MESSAGE);
                    return;
                } else {
                    String profileOwnerId = String.valueOf(jTable.getValueAt(selectedRow, 1));
                    SwingUtilities
                            .invokeLater(new ProfileDisplayFrame(socket, userId, profileOwnerId, "Frame.UserFrame"));
                    userFrame.dispose();
                }
            }
            if (menuChoice == deleteFriend) {
                int selectedRow = jTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null,
                            "You must first select a line! ", "No selection", JOptionPane.WARNING_MESSAGE);
                } else {
                    String deleteFriendId = String.valueOf(jTable.getValueAt(selectedRow, 1));
                    printWriter.println("DeleteFriend");
                    printWriter.println(userId);
                    printWriter.println(deleteFriendId);
                    printWriter.flush();
                    String success = null;
                    try {
                        success = bufferedReader.readLine();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    assert success != null;
                    if (success.equals("Success")) {
                        JOptionPane.showMessageDialog(null,
                                "You have successfully deleted a friend",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Unable to delete friend", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    model = updateModel(userId);
                    jTable.setModel(model);
                    rowSorter.setModel(jTable.getModel());
                    jTable.setRowSorter(rowSorter);
                    userFrame.repaint();
                }
            }
        }
    };

    ActionListener buttonActionListener = new ActionListener() {
        /**
         * @param e This is the listener for buttons.
         *          There are three buttons:
         *          One is the logout button which back to Frame.LoginFrame.
         *          The second is the addFriend button which leads you to AddFriend
         *          Frame.
         *          The third would be the account and profile button which would lead
         *          you to Frame.AccountProfileFrame.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == back) {
                SwingUtilities.invokeLater(new LoginFrame(socket));
                userFrame.dispose();
            }
            if (e.getSource() == add) {
                SwingUtilities.invokeLater(new AddFriendFrame(socket, userId));
                userFrame.dispose();
            }
            if (e.getSource() == account) {
                SwingUtilities.invokeLater(new AccountProfileFrame(socket, userId));
                userFrame.dispose();
            }
            if (e.getSource() == createPost) {
                SwingUtilities.invokeLater(new CreatePostFrame(socket, userId));
                userFrame.dispose();
            }
        }
    };

    /**
     * The constructor of Frame.UserFrame which use two parameters : socket and
     * userId
     *
     * @param socket The socket that connects this local machine with the server
     * @param userId The userId of the login user
     */
    public UserFrame(Socket socket, String userId) {
        this.socket = socket;
        this.userId = userId;
    }

    /**
     * In run method, the GUIs are initialized.
     * BufferedReader and PrintWriter is created with the socket that being
     * transferred from other frame.
     * A search bar is implemented by using documentLister and a JTextField
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
        // Initialize GUI
        popupMenu = new JPopupMenu();
        viewProfile = new JMenuItem("View profile");
        deleteFriend = new JMenuItem("Delete Friend");

        viewProfile.addActionListener(popupItemListener);
        deleteFriend.addActionListener(popupItemListener);
        popupMenu.add(viewProfile);
        popupMenu.add(deleteFriend);

        model = updateModel(userId);
        jTable = new JTable(model);
        jTable.setComponentPopupMenu(popupMenu);
        rowSorter = new TableRowSorter<>(jTable.getModel());
        userFrame = new JFrame("User Frame");
        userFrame.setLayout(new BorderLayout());
        jTable.setRowSorter(rowSorter);
        JPanel panel = new JPanel();
        JPanel panel3 = new JPanel();
        add = new JButton("Add Friend");
        account = new JButton("Create/Edit Profile & Account");
        back = new JButton("Log Out");
        createPost = new JButton("Create Post");
        back.addActionListener(buttonActionListener);
        add.addActionListener(buttonActionListener);
        account.addActionListener(buttonActionListener);
        createPost.addActionListener(buttonActionListener);

        panel.add(add);
        panel.add(new JLabel("Find a specific friend"));
        panel.add(jtfFilter);
        panel.add(account);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.setVisible(true);
        userFrame.add(panel, BorderLayout.NORTH);
        panel3.add(back);
        panel3.add(createPost);
        userFrame.add(panel3, BorderLayout.SOUTH);
        jScrollPane = new JScrollPane(jTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(jScrollPane, BorderLayout.CENTER);
        panel2.setVisible(true);
        userFrame.add(panel2, BorderLayout.CENTER);
        // This implements the functionality of search bar.
        jtfFilter.getDocument().addDocumentListener(new DocumentListener() {
            /**
             * Document listener for inserting text
             * 
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
             * 
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
             * 
             * @param e DocumentEvent e which is when user change the text inside the
             *          jtfFilter JTextField
             */
            @Override
            public void changedUpdate(DocumentEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        userFrame.pack();
        userFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        userFrame.setLocationRelativeTo(null);
        userFrame.addWindowListener(new WindowAdapter() {
            /**
             * Window closing listener to close the socket when the user close the software.
             * 
             * @param e Invoked when a window is in the process of being closed.
             *          The close operation can be overridden at this point.
             */
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    bufferedReader.close();
                    printWriter.close();
                    socket.close();
                    userFrame.dispose();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        userFrame.setVisible(true);
    }

    /**
     * The method that communicate with the server and get the updated userInfo in
     * the login user's friendList
     * 
     * @param loginId The id of the login user
     * @return return the updated model with the content of current friendList
     */
    @Override
    public DefaultTableModel updateModel(String loginId) {
        printWriter.println("LoadPosts");
        printWriter.println(loginId);
        System.out.println("Made it to updateModel");
        printWriter.flush();
        try {
            int num = Integer.parseInt(bufferedReader.readLine());
            System.out.println(num);
            rowData = new String[num][4];
            for (int i = 0; i < num; i++) {
                String response = bufferedReader.readLine();
                String[] data = response.split("\\|");
                // System.out.println(response);
                Date date = new Date(Long.parseLong(data[2]));
                rowData[i][0] = data[0];
                if (data[3].equals(loginId)) {
                    rowData[i][1] = data[3] + " (you)";
                } else {
                    rowData[i][1] = data[3];
                }
                rowData[i][2] = data[1];
                rowData[i][3] = sdf.format(date);
                listOfUsersPosts.add(data[3]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new DefaultTableModel(rowData, columnName) {
            /**
             * @param row    Row of Data
             * @param column The column head
             * @return return a DefaultTableModel with the parameter and override its
             *         isCellEditable to return false
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}
