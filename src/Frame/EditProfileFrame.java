package src.Frame;

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
 * A class representing the frame to edit the details of the user profile.
 * All the changed profile details must conform to the respective validation rules.
 *
 * @author Tharun Kumar Senthilkumar & Eashan & Abdullah Haris
 * @version Dec 8, 2024
 */
public class EditProfileFrame extends JComponent implements Runnable {
    Socket socket;
    String userId;
    BufferedReader bufferedReader;
    PrintWriter printWriter;
    JFrame editProfileFrame;
    JLabel userPhoneNoLabel;
    JTextField userPhoneNoTextField;
    JLabel currentOccupationLabel;
    JTextField currentOccupationTextField;
    JComboBox<String> genderList;
    JComboBox<String> relationshipList;
    JLabel aboutMeLabel;
    JTextField aboutMeTextField;
    JLabel genderLabel;
    JLabel relationshipLabel;
    JLabel interestLabel;
    JTextField interestField;
    JButton editProfileButton;
    JButton backButton;

    final int fieldWidth = 150;

    ActionListener actionListener = new ActionListener() {
        /**
         *@param e Invoked when any of the button in the frame is selected.
         *         There are two button choices: Back and Edit Profile.
         *         Back button will lead to the Frame.ProfileMenuFrame while Edit Profile button
         *         would perform the functionality by sending the user phone number, current occupation,
         *         about user, interest, gender, and relationship status to the server.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == backButton) {
                SwingUtilities.invokeLater(new ProfileMenuFrame(socket, userId));
                editProfileFrame.dispose();
            }
            if (e.getSource() == editProfileButton) {
                String userPhoneNo = userPhoneNoTextField.getText().trim();
                String currentOccupation = currentOccupationTextField.getText().trim();
                String aboutMe = aboutMeTextField.getText().trim();
                String interest = interestField.getText().trim();
                String gender = String.valueOf(genderList.getSelectedItem());
                String relationship = String.valueOf(relationshipList.getSelectedItem());
                if (!contentCheck(userPhoneNo, currentOccupation, aboutMe, interest, gender, relationship)) {
                    return;
                }
                /*printWriter.println("UniquePhoneNoCheck");
                printWriter.println(userPhoneNo);
                printWriter.flush();
                String result = "";
                try {
                    result = bufferedReader.readLine();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                if (!result.equals("Unique")) {
                    JOptionPane.showMessageDialog(null, "User Phone Number exists",
                            "User Phone Number Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }*/
                //Pass the data to server
                printWriter.println("EditOwnProfile");
                printWriter.println(userId);
                printWriter.printf("%s/ %s/ %s/ %s/ %s/ %s\n", userPhoneNo, relationship, gender, currentOccupation,
                        interest, aboutMe);
                printWriter.flush();
                String success = "";
                try {
                    success = bufferedReader.readLine();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                if (success.equals("Success")) {
                    JOptionPane.showMessageDialog(null, "Congratulations!\n" +
                                    "You have successfully created your profile!",
                            "Profile Creation Successful", JOptionPane.INFORMATION_MESSAGE);
                    SwingUtilities.invokeLater(new ProfileMenuFrame(socket, userId));
                    editProfileFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Oops!" +
                                    "Unsuccessful creation.\nPlease retry.",
                            "EditProfile Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    };

    /**
     * The constructor of Frame.EditProfileFrame which uses two parameters : socket and userId
     *
     * @param socket The socket that connects this local machine with the server
     * @param userId The userId of the login user
     */
    public EditProfileFrame(Socket socket, String userId) {
        this.socket = socket;
        this.userId = userId;
    }

    /**
     *  Sets up the appearance of the Edit Profile Frame by initializing GUIs.
     *  BufferedReader and PrintWriter is created with the socket that is being transferred from other frame.
     */
    @Override
    public void run() {
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Unable to initialize in Edit Profile frame", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
        editProfileFrame = new JFrame("Profile Frame");
        Container editProfileFrameContentPane = editProfileFrame.getContentPane();
        editProfileFrameContentPane.setLayout(null);

        //Initialize components
        userPhoneNoLabel = new JLabel("Phone Number");
        userPhoneNoTextField = new JTextField();
        currentOccupationLabel = new JLabel("Current Occupation");
        currentOccupationTextField = new JTextField();
        aboutMeLabel = new JLabel("About Me");
        aboutMeTextField = new JTextField();
        interestLabel = new JLabel("Interests");
        interestField = new JTextField();
        relationshipLabel = new JLabel("Relationship Status");
        relationshipList = new JComboBox<>(new String[]{"Single", "In relationship"});
        genderLabel = new JLabel("Gender");
        genderList = new JComboBox<>(new String[]{"Male", "Female", "I do not wish to identify"});
        editProfileButton = new JButton("Edit Profile");
        backButton = new JButton("Back to menu");

        //Set component location
        userPhoneNoLabel.setBounds(90, 10, fieldWidth, 30);
        userPhoneNoTextField.setBounds(220, 10, fieldWidth, 30);
        currentOccupationLabel.setBounds(90, 50, fieldWidth, 30);
        currentOccupationTextField.setBounds(220, 50, fieldWidth, 30);
        aboutMeLabel.setBounds(90, 90, fieldWidth, 30);
        aboutMeTextField.setBounds(220, 90, fieldWidth, 30);
        genderLabel.setBounds(90, 130, fieldWidth, 30);
        genderList.setBounds(220, 130, fieldWidth, 30);
        relationshipLabel.setBounds(90, 170, fieldWidth, 30);
        relationshipList.setBounds(220, 170, fieldWidth, 30);
        interestLabel.setBounds(90, 210, fieldWidth, 30);
        interestField.setBounds(220, 210, fieldWidth, 30);
        editProfileButton.setBounds(140, 270, fieldWidth, 30);
        backButton.setBounds(140, 310, fieldWidth, 30);

        //Add actionListener
        editProfileButton.addActionListener(actionListener);
        backButton.addActionListener(actionListener);
        printWriter.println("GetProfileContent");
        printWriter.println(userId);
        printWriter.flush();
        String phoneNumber = "";
        String currentOccupation = "";
        String gender = "";
        String aboutMe = "";
        String interest = "";
        String relationship = "";
        try {
            phoneNumber = bufferedReader.readLine();
            currentOccupation = bufferedReader.readLine();
            gender = bufferedReader.readLine();
            aboutMe = bufferedReader.readLine();
            interest = bufferedReader.readLine();
            relationship = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Unable to get previous profile", "Error", JOptionPane.ERROR_MESSAGE);
        }
        userPhoneNoTextField.setText(phoneNumber);
        currentOccupationTextField.setText(currentOccupation);
        aboutMeTextField.setText(aboutMe);
        interestField.setText(interest);
        if (gender.equals("")) {
            genderList.setSelectedIndex(-1);
        } else {
            if (gender.equals("Male")) {
                genderList.setSelectedIndex(0);
            }
            if (gender.equals("Female")) {
                genderList.setSelectedIndex(1);
            }
            if (gender.equals("I do not wish to identify")) {
                genderList.setSelectedIndex(2);
            }
        }
        if (relationship.equals("")) {
            relationshipList.setSelectedIndex(-1);
        } else {
            if (relationship.equals("Single")) {
                relationshipList.setSelectedIndex(0);
            }
            if (relationship.equals("In relationship")) {
                relationshipList.setSelectedIndex(1);
            }
        }

        //Add all components into the Frame
        editProfileFrameContentPane.add(userPhoneNoLabel);
        editProfileFrameContentPane.add(userPhoneNoTextField);
        editProfileFrameContentPane.add(currentOccupationLabel);
        editProfileFrameContentPane.add(currentOccupationTextField);
        editProfileFrameContentPane.add(aboutMeLabel);
        editProfileFrameContentPane.add(aboutMeTextField);
        editProfileFrameContentPane.add(relationshipLabel);
        editProfileFrameContentPane.add(relationshipList);
        editProfileFrameContentPane.add(genderLabel);
        editProfileFrameContentPane.add(genderList);
        editProfileFrameContentPane.add(interestLabel);
        editProfileFrameContentPane.add(interestField);
        editProfileFrameContentPane.add(editProfileButton);
        editProfileFrameContentPane.add(backButton);

        //Finalize the Frame
        editProfileFrame.setSize(400, 400);
        editProfileFrame.setLocationRelativeTo(null);
        editProfileFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        editProfileFrame.addWindowListener(new WindowAdapter() {
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
                    editProfileFrame.dispose();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
        editProfileFrame.setVisible(true);
    }

    /**
     * Checks inserted information to make sure it doesn't contain forbidden characters and it isn't empty.
     *
     * @param userPhoneNo the phone number the user enters  
     * @param currentOccupation the job that the user enters
     * @param aboutMe the aboutMe page that user enters
     * @param interest the interests page that user enters
     * @param gender the gender that user selects
     * @param relationship the relationship status that user selects
     * @return true if checks passes, false otherwise.
     */
    public boolean contentCheck(String userPhoneNo, String currentOccupation, String aboutMe, String interest,
                                String gender, String relationship) {
        boolean correct = true;
        try {
            long number = Long.parseLong(userPhoneNo);
            if (userPhoneNo.equals("")) {
                JOptionPane.showMessageDialog(null, "Phone Number cannot be empty.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                correct = false;
            }
            if (currentOccupation.equals("")) {
                JOptionPane.showMessageDialog(null, "Current Occupation cannot be empty.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                correct = false;
            }
            if (aboutMe.equals("")) {
                JOptionPane.showMessageDialog(null, "About me cannot be empty.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                correct = false;
            }
            if (interest.equals("")) {
                JOptionPane.showMessageDialog(null, "Interest cannot be empty.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                correct = false;
            }
            if (gender.equals("")) {
                JOptionPane.showMessageDialog(null, "Please select your gender.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                correct = false;
            }
            if (relationship.equals("")) {
                JOptionPane.showMessageDialog(null, "Please select your relationship status.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
                correct = false;
            }
        } catch (NumberFormatException | HeadlessException e) {
            JOptionPane.showMessageDialog(null, "Phone Number should only contain numbers.\n" +
                    "Do not write anything else than numbers.", "Input Error", JOptionPane.WARNING_MESSAGE);
            correct = false;
        }
        return correct;
    }
}
