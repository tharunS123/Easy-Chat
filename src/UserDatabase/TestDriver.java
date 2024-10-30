import java.util.Map;
import java.util.Scanner;

public class Driver {
    public static void main(String[] args) {
        // Initialize the UserDatabase with a file name
        UserDatabase userDatabase = new UserDatabase("user_database.txt");

        // Load and display all users currently in the database
        loadAndDisplayUsers(userDatabase);

        // Prompt to add a new user
        createUserPrompt(userDatabase);

        // Display updated users in the database
        loadAndDisplayUsers(userDatabase);
    }

    // Method to load and display users from the database
    private static void loadAndDisplayUsers(UserDatabase userDatabase) {
        Map<String, User> users = userDatabase.loadUsers();

        if (users.isEmpty()) {
            System.out.println("No users found in the database.");
        } else {
            System.out.println("Loaded users from the database:");
            for (User user : users.values()) {
                System.out.println("Username: " + user.getUsername());
                System.out.println("UUID: " + user.getUuid());
                System.out.println("Friends: " + user.getFriends());
                System.out.println("------------");
            }
        }
    }

    // Method to prompt the user to create a new user
    private static void createUserPrompt(UserDatabase userDatabase) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a username: ");
        String username = scanner.nextLine();

        System.out.print("Enter a password: ");
        String password = scanner.nextLine();

        User newUser = new User(username, password);

        // Optionally add friends
        System.out.print("Would you like to add friends? (yes/no): ");
        String addFriends = scanner.nextLine();

        if (addFriends.equalsIgnoreCase("yes")) {
            boolean addingFriends = true;
            while (addingFriends) {
                System.out.print("Enter a friend's username to add (or type 'done' to finish): ");
                String friendUsername = scanner.nextLine();
                if (friendUsername.equalsIgnoreCase("done")) {
                    addingFriends = false;
                } else {
                    newUser.addFriend(friendUsername);
                }
            }
        }

        // Add the user to the database
        if (userDatabase.addUser(newUser)) {
            System.out.println("User added successfully!");
        } else {
            System.out.println("Username already exists. User was not added.");
        }
    }
}
