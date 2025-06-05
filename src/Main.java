import Entities.*;
import Services.ChatService;
import Services.SessionService;
import Services.UserService;

import java.util.Scanner;

public class Main {
    public static ChatService service = new ChatService();
    public static SessionService sessionService = new SessionService();

    public static void main(String[] args) {
        // TODO: switch this back
        // initialise();

        Scanner scanner = new Scanner(System.in);
        CommandHandler handler = new CommandHandler(service, sessionService);

        // can add a Command class to handle command

        // COMMANDS:
        // SHOW_ALL USERS / ROOMS / ACTIVE_SESSIONS / INACTIVE_SESSIONS
        // REGISTER [username]
        // LOGIN [username]
        // LOGOUT [username]
        // SHOW ROOMS / MSG [room_id] / PARTICIPANTS [room_id] / EMPTY_SLOTS [room_id] -> for user currently logged in
        // SEND [room_id] [msg] -> send message
        // ADD_TO [room_id] [username] -> add to group
        // KICK [room_id] [username] -> kick from group
        // CREATE GROUP [name] / PRIVATE [username]
        // SEARCH [room_id] [keyword] -> search for keyword in room messages
        // MSG_STATUS [room_id] -> show status of room messages
        // READ [room_id] -> read all messages from the room
        // UNREAD [room_id] -> show all unread messages from the room
        // UNREAD_CNT [room_id] -> show number of unread messages
        // ADMIN [room_id] [username] -> make user admin
        // REM_ADMIN [room_id] [username] -> remove admin role from user
        // ROLES [room_id] -> show roles of all participants in a room
        // EDIT [message_id] -> edit the content of a message
        // DELETE GROUP [room_id] / MSG [message_id]

        while (true) {
            System.out.println("\nEnter command:");
            String input = scanner.nextLine();

            handler.readCommand(input);
            handler.handleCommand();
        }
    }
}
