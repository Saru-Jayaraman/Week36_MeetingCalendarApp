package se.lexicon.controller;

import se.lexicon.dao.CalendarDAO;
import se.lexicon.dao.MeetingDAO;
import se.lexicon.dao.UserDAO;
import se.lexicon.exception.CalendarExceptionHandler;
import se.lexicon.model.Calendar;
import se.lexicon.model.Meeting;
import se.lexicon.model.User;
import se.lexicon.view.CalendarView;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class CalendarController {

    //dependencies:
    private CalendarView view;
    private UserDAO userDAO;
    private CalendarDAO calendarDAO;
    private MeetingDAO meetingDAO;

    //fields:
    private boolean isLoggedIn;
    private String username;

    public CalendarController(CalendarView view, UserDAO userDAO, CalendarDAO calendarDAO, MeetingDAO meetingDAO) {
        this.view = view;
        this.userDAO = userDAO;
        this.calendarDAO = calendarDAO;
        this.meetingDAO = meetingDAO;
    }

    public void run() {
        while (true) {
            view.displayMenu();
            int choice = getUserChoice();

            switch (choice) {
                case 0:
                    register();
                    break;
                case 1:
                    login();
                    break;
                case 2:
                    createCalendar();
                    break;
                case 3:
                    createMeeting();
                    break;
                case 4:
                    deleteMeeting();
                    break;
                case 5:
                    deleteCalendar();
                    break;
                case 6:
                    displayCalendar();
                    break;
                case 7:
                    isLoggedIn = false;
                    view.displayMessage("Logged out.");
                    break;
                case 8:
                    System.exit(0);
                    break;

                default:
                    view.displayWarningMessage("Invalid choice. Please select a valid option.");
            }
        }
    }

    private int getUserChoice() {
        String operationType = view.promoteString();
        int choice = -1;
        try {
            choice = Integer.parseInt(operationType);
        } catch (NumberFormatException e) {
            view.displayErrorMessage("Invalid input, please enter a number.");
        }
        return choice;
    }

    private void register() {
        view.displayMessage("Enter your username");
        String username = view.promoteString();
        User registeredUser = userDAO.createUser(username);
        view.displayUser(registeredUser);
    }

    private void login() {
        if (isLoggedIn) {
            view.displayWarningMessage("Already logged in. Proceed with other operations.");
            return;
        }
        User user = view.promoteUserForm();
        try {
            isLoggedIn = userDAO.authenticate(user);
            username = user.getUsername();
            view.displaySuccessMessage("Login successful. Welcome " + username);
        } catch (Exception e) {
            CalendarExceptionHandler.handleException(e);
        }
    }

    private void createCalendar() {
        if (!isLoggedIn) {
            view.displayWarningMessage("You need to login first.");
            return;
        }
        String calendarTitle = view.promoteCalendarForm();
        Calendar createdCalendar = calendarDAO.createCalendar(calendarTitle, username);
        view.displaySuccessMessage("Calendar created successfully.");
        view.displayCalendar(createdCalendar);

    }

    private void createMeeting() {
        if (!isLoggedIn) {
            view.displayWarningMessage("You need to login first.");
            return;
        }
        printCalendarDetails("Calendars list:");
        view.displayMessage("Enter calendar title:");
        Scanner scanner = new Scanner(System.in);
        String calendarToCreateMeeting = scanner.nextLine();

        Optional<Calendar> calendarToCreateMeetingOptional = calendarDAO.findByTitleAndUsername(calendarToCreateMeeting, username);
        if (calendarToCreateMeetingOptional.isPresent()) {
            view.displayMessage("Enter meeting details to create:");
            Meeting meeting = view.promoteMeetingForm();
            meeting.setCalendar(calendarToCreateMeetingOptional.get());
            meetingDAO.createMeeting(meeting);
            view.displaySuccessMessage("Meeting created successfully.");
            displayMeeting(calendarToCreateMeetingOptional.get());
        } else
            view.displayWarningMessage("Calendar not found to create. Enter exact calendar title to create.");
    }

    private void deleteMeeting() {
        if (!isLoggedIn) {
            view.displayWarningMessage("You need to login first.");
            return;
        }
        printCalendarDetails("Calendars list:");
        view.displayMessage("Enter calendar title:");
        Scanner scanner = new Scanner(System.in);
        String calendarString = scanner.nextLine();

        Optional<Calendar> calendarObject = calendarDAO.findByTitleAndUsername(calendarString, username);
        if (calendarObject.isPresent()) {
            List<Meeting> allMeetingsByCalendarId = (List<Meeting>) meetingDAO.findAllMeetingsByCalendarId(calendarObject.get().getId());
            allMeetingsByCalendarId.forEach(meeting1 -> {
                meeting1.setCalendar(calendarObject.get());
                System.out.println(meeting1.getId() + "  " + meeting1.getTitle());
            });

            view.displayMessage("Enter meeting id to delete:");
            int deleteMeetingId = scanner.nextInt();
            meetingDAO.deleteMeeting(deleteMeetingId);
            view.displaySuccessMessage("Meeting deleted successfully.");
            displayMeeting(calendarObject.get());
        } else {
            view.displayWarningMessage("Calendar not found. Enter exact calendar title. ");
        }
    }

    private void deleteCalendar() {
        if (!isLoggedIn) {
            view.displayWarningMessage("You need to login first.");
            return;
        }
        printCalendarDetails("Calendars list:");
        view.displayMessage("Enter calendar title:");
        Scanner scanner = new Scanner(System.in);
        String titleToDelete = scanner.nextLine();

        Optional<Calendar> calendarToDelete = calendarDAO.findByTitleAndUsername(titleToDelete, username);
        if (calendarToDelete.isPresent()) {
            calendarDAO.deleteCalendar(calendarToDelete.get().getId());
            view.displaySuccessMessage("Calendar deleted successfully.");
            printCalendarDetails("Calendar list after deleting.");
        } else {
            view.displayWarningMessage("Calendar not found to delete. Enter exact calendar title to delete.");
        }
    }

    private void displayCalendar() {
        if (!isLoggedIn) {
            view.displayWarningMessage("You need to login first.");
            return;
        }
        printCalendarDetails("Calendars list:");
    }

    private void printCalendarDetails(String message) {
        view.displayMessage(message);
        Collection<Calendar> allCalendars = calendarDAO.findCalendarByUsername(username);
        if(allCalendars == null)
            System.out.println("No calendars found.");
        else
            allCalendars.forEach(calendar -> view.displaySuccessMessage(calendar.getTitle()));
    }

    private void displayMeeting(Calendar calendar) {
        List<Meeting> allMeetingsByCalendarId = (List<Meeting>) meetingDAO.findAllMeetingsByCalendarId(calendar.getId());
        allMeetingsByCalendarId.forEach(meeting1 -> meeting1.setCalendar(calendar));
        view.displayMeetings(allMeetingsByCalendarId);
    }

}
