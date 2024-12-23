package src.Frame;

import src.Interface.RegisterFrameInterface;

import javax.swing.*;
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
 * A class representing the frame that allows the users to register for this application.
 * All users must register before being able to login to the application.
 * Note that the user ID they set for the first time will be permanent throughout - they cannot change their user ID
 * by editing their account details.
 * All the account details must conform to the respective validation rules.
 *
 * @author Tharun Kumar Senthilkumar & Eashan & Abdullah Haris
 * @version Dec 8, 2024
 */
public class RegisterFrame extends JComponent implements Runnable, RegisterFrameInterface {
    Socket socket;
    BufferedReader bufferedReader;
    PrintWriter printWriter;
    JFrame registerFrame;
    JLabel userIdLabel;
    JTextField userIdTextField;
    JLabel passwordLabel;
    JPasswordField passwordField;
    JLabel realNameLabel;
    JTextField realNameTextField;
    JLabel emailLabel;
    JTextField emailTextField;
    JButton registerButton;
    JButton backButton;

    final int fieldWidth = 150;

    ActionListener actionListener = new ActionListener() {
        /**
         * @param e New action listener for listening to button press.
         *          Invoked when either backButton or registerButton is pressed.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == backButton) {
                SwingUtilities.invokeLater(new LoginFrame(socket));
                registerFrame.dispose();
            }
            if (e.getSource() == registerButton) {
                String userId = userIdTextField.getText();
                StringBuilder rawPassword = new StringBuilder();
                rawPassword.append(passwordField.getPassword());
                String realName = realNameTextField.getText();
                String email = emailTextField.getText();
                if (!contentCheck(userId, rawPassword.toString(), realName, email)) {
                    return;
                }
                printWriter.println("UniqueIdCheck");
                printWriter.println(userId);
                printWriter.flush();
                String result = "";
                try {
                    result = bufferedReader.readLine();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                if (!result.equals("Unique")) {
                    JOptionPane.showMessageDialog(null, "UserID exists",
                            "UserID Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                //Pass the data to server
                printWriter.println("Register");
                printWriter.printf("%s, %s, %s, %s\n", userId, rawPassword.toString(), realName, email);
                printWriter.flush();
                String success = "";
                try {
                    success = bufferedReader.readLine();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                if (success.equals("Success")) {
                    JOptionPane.showMessageDialog(null, "Congratulation! " +
                                    "You have successfully registered",
                            "Register Successfully", JOptionPane.INFORMATION_MESSAGE);
                    SwingUtilities.invokeLater(new LoginFrame(socket));
                    registerFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Oops! " +
                                    "Unsuccessful register.\nPlease retry.",
                            "Register Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    };

    /**
     * The constructor of the Frame.RegisterFrame
     * @param socket The socket that connects this local machine with the server
     */
    public RegisterFrame(Socket socket) {
        this.socket = socket;
    }

    /**
     * Sets up the appearance of the Register Frame by initializing GUIs.
     * BufferedReader and PrintWriter is created with the socket that is being transferred from other frame.
     */
    @Override
    public void run() {
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Unable to initialize in Register frame", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
        registerFrame = new JFrame("Register Frame");
        Container registerFrameContentPane = registerFrame.getContentPane();
        registerFrameContentPane.setLayout(null);

        //Initialize components
        userIdLabel = new JLabel("User ID");
        userIdTextField = new JTextField();
        passwordLabel = new JLabel("Password");
        passwordField = new JPasswordField();
        realNameLabel = new JLabel("Real Name");
        realNameTextField = new JTextField();
        emailLabel = new JLabel("Email");
        emailTextField = new JTextField();
        registerButton = new JButton("Register");
        backButton = new JButton("Back to login");

        //Set component location
        userIdLabel.setBounds(110, 20, fieldWidth, 30);
        userIdTextField.setBounds(200, 20, fieldWidth, 30);
        passwordLabel.setBounds(110, 60, fieldWidth, 30);
        passwordField.setBounds(200, 60, fieldWidth, 30);
        realNameLabel.setBounds(110, 100, fieldWidth, 30);
        realNameTextField.setBounds(200, 100, fieldWidth, 30);
        emailLabel.setBounds(110, 140, fieldWidth, 30);
        emailTextField.setBounds(200, 140, fieldWidth, 30);
        registerButton.setBounds(140, 190, fieldWidth, 30);
        backButton.setBounds(140, 225, fieldWidth, 30);

        //Add actionLister
        registerButton.addActionListener(actionListener);
        backButton.addActionListener(actionListener);

        //Add all components into the Frame;
        registerFrameContentPane.add(userIdLabel);
        registerFrameContentPane.add(userIdTextField);
        registerFrameContentPane.add(passwordLabel);
        registerFrameContentPane.add(passwordField);
        registerFrameContentPane.add(realNameLabel);
        registerFrameContentPane.add(realNameTextField);
        registerFrameContentPane.add(emailLabel);
        registerFrameContentPane.add(emailTextField);
        registerFrameContentPane.add(registerButton);
        registerFrameContentPane.add(backButton);

        //Finalize the Frame
        registerFrame.setSize(400, 300);
        registerFrame.setLocationRelativeTo(null);
        registerFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        //add a window listener to close the bufferedReader and printWriter to prevent socket close exception
        registerFrame.addWindowListener(new WindowAdapter() {
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
                    registerFrame.dispose();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        registerFrame.setVisible(true);
    }

    /**
     * The content check method
     * Checks the format of text in JTextField and JPasswordField for user id, password, real name, and email
     * The functionality is implemented using String.matches() method which requires a regex as its parameter.
     * The content has to fully match the regex in order to set boolean correct to true.
     *
     * @param userId The text inside userIdTextField
     * @param password The text inside passwordField
     * @param realName The text inside realNameTextField
     * @param email The text inside emailTextField
     * @return A boolean in which 1 indicates passed the test while 0 indicates failed the test
     */
    @Override
    public boolean contentCheck(String userId, String password, String realName, String email) {
        boolean correct = true;
        if (!userId.matches("^[a-zA-Z0-9_-][^%+\\\\/#@*:;`~<>?!.,'\"]+$")) {
            JOptionPane.showMessageDialog(null, "UserID should at least contain" +
                            " two characters and only contain alphabets and numbers.",
                    "UserID Error", JOptionPane.WARNING_MESSAGE);
            correct = false;
        }

        /*
        Regex used for password validation:
        ^                 # start-of-string
        (?=.*[0-9])       # a digit must occur at least once
        (?=.*[a-z])       # a lower case letter must occur at least once
        (?=.*[A-Z])       # an upper case letter must occur at least once
        (?=.*[@#$%^&+=])  # a special character must occur at least once
        (?=\S+$)          # no whitespace allowed in the entire string
        .{8,}             # anything, at least eight places though
        $                 # end-of-string
         */
        if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$!?%^&+=])(?=\\S+$).{8,}$")) {
            JOptionPane.showMessageDialog(null, "Password must have a length"
                            + " greater than 8 and contain at least one uppercase, one lower case, one digit" +
                            " and one special character.",
                    "Password Error", JOptionPane.WARNING_MESSAGE);
            correct = false;
        }
        if (!realName.matches("[A-Za-z]+?[\\-]+?[A-Za-z]+ ?[A-Za-z]+") &&
                !realName.matches("[A-Za-z]+ [A-Za-z]+ ?[A-Za-z]+")) {
            JOptionPane.showMessageDialog(null, "Real Name must have a first"
                            + " name and a last name.\nA space needs to appear between the first name and next"
                            + " name\nMiddle name can be included.",
                    "Real Name Error", JOptionPane.WARNING_MESSAGE);
            correct = false;
        }
        if (!email.matches("\\w+@\\w+.\\w+")) {
            JOptionPane.showMessageDialog(null, "The email"
                            + " must have one '@' sign and one '.', no other special sign allowed",
                    "Email", JOptionPane.WARNING_MESSAGE);
            correct = false;
        }
        return correct;
    }
}
