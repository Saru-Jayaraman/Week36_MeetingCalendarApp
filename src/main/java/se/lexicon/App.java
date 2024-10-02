package se.lexicon;

import se.lexicon.dao.UserDAO;
import se.lexicon.dao.db.CalendarDBConnection;
import se.lexicon.dao.impl.UserDAOImpl;
import se.lexicon.exception.CalendarExceptionHandler;
import se.lexicon.model.User;

import java.util.Optional;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        try {
            UserDAO userDAO = new UserDAOImpl(CalendarDBConnection.getConnection());
            User userCreated = userDAO.createUser("admin8");
            System.out.println("User info: " + userCreated.userInfo());
            Optional<User> userOptional = userDAO.finByUsername("admin8");
            userOptional.ifPresent(user -> System.out.println("Hashed password: " + user.getHashedPassword()));
        } catch (Exception e) {
            CalendarExceptionHandler.handleException(e);
        }
    }
}
