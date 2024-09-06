package se.lexicon.controller;

import se.lexicon.dao.CalendarDAO;
import se.lexicon.dao.MeetingDAO;
import se.lexicon.dao.UserDAO;
import se.lexicon.exception.CalendarExceptionHandler;
import se.lexicon.model.Calendar;
import se.lexicon.model.Meeting;
import se.lexicon.model.User;
import se.lexicon.view.CalendarView;

import java.util.*;

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
        view.displayMessage("Calendars list:");
        Collection<Calendar> allCalendars = calendarDAO.findCalendarByUsername(username);
        if (allCalendars.isEmpty()) {
            view.displayWarningMessage("No calendar found.");
            return;
        }
        allCalendars.forEach(calendar -> view.displaySuccessMessage(calendar.getTitle()));

        view.displayMessage("Choose and enter a calendar title:");
        String calendarToCreateMeeting = view.promoteString();
        Optional<Calendar> calendarToCreateMeetingOptional = calendarDAO.findByTitleAndUsername(calendarToCreateMeeting, username);
        if (!calendarToCreateMeetingOptional.isPresent()) {
            view.displayWarningMessage("Calendar not found to create. Enter exact calendar title to create.");
            return;
        }

        view.displayMessage("Enter meeting details to create:");
        Meeting meeting = view.promoteMeetingForm();
        meeting.setCalendar(calendarToCreateMeetingOptional.get());

        // Validate the meeting times
        try {
            meeting.timeValidation();
        } catch (IllegalArgumentException e) {
            view.displayErrorMessage(e.getMessage());
            return;
        }

        Meeting createdMeeting = meetingDAO.createMeeting(meeting);
        view.displaySuccessMessage("Meeting created successfully.");
        view.displayMeetings(Collections.singletonList(createdMeeting));
    }

    private void deleteMeeting() {
        if (!isLoggedIn) {
            view.displayWarningMessage("You need to login first.");
            return;
        }
        view.displayMessage("Calendars list:");
        Collection<Calendar> allCalendars = calendarDAO.findCalendarByUsername(username);
        if (allCalendars.isEmpty()) {
            view.displayWarningMessage("No calendar found.");
            return;
        }
        allCalendars.forEach(calendar -> view.displaySuccessMessage(calendar.getTitle()));

        view.displayMessage("Choose and enter a calendar title:");
        String calendarString = view.promoteString();
        Optional<Calendar> calendarObject = calendarDAO.findByTitleAndUsername(calendarString, username);
        if (!calendarObject.isPresent()) {
            view.displayWarningMessage("Calendar not found. Enter exact calendar title. ");
            return;
        }

        //Before Deleting - Display list of meetings
        List<Meeting> allMeetingsBeforeDelete = (List<Meeting>) meetingDAO.findAllMeetingsByCalendarId(calendarObject.get().getId());
        if (allMeetingsBeforeDelete.isEmpty()) {
            view.displayWarningMessage("No meetings is available to delete.");
            return;
        }
        allMeetingsBeforeDelete.forEach(meeting -> {
            meeting.setCalendar(calendarObject.get());
            String meetingDisplay = meeting.getId() + "  " + meeting.getTitle();
            view.displaySuccessMessage(meetingDisplay);
        });

        //Delete by id
        view.displayMessage("Enter meeting id to delete:");
        int deleteMeetingId = Integer.parseInt(view.promoteString());
        boolean isDeleted = meetingDAO.deleteMeeting(deleteMeetingId);
        if (!isDeleted) {
            view.displayErrorMessage("Failed to delete meeting. Check the meeting id.");//2024-10-10 10:00
        } else {
            view.displaySuccessMessage("Meeting deleted successfully.");
            //After Deleting - Display list of meetings
            view.displayMessage("Meeting list after deleting:");
            List<Meeting> allMeetingsAfterDelete = (List<Meeting>) meetingDAO.findAllMeetingsByCalendarId(calendarObject.get().getId());
            allMeetingsAfterDelete.forEach(meeting -> meeting.setCalendar(calendarObject.get()));
            view.displayMeetings(allMeetingsAfterDelete);
        }
    }

    private void deleteCalendar() {
        if (!isLoggedIn) {
            view.displayWarningMessage("You need to login first.");
            return;
        }
        view.displayMessage("Calendars list:");
        Collection<Calendar> allCalendarsBeforeDelete = calendarDAO.findCalendarByUsername(username);
        if (allCalendarsBeforeDelete.isEmpty()) {
            view.displayWarningMessage("No calendar found.");
            return;
        }
        allCalendarsBeforeDelete.forEach(calendar -> view.displaySuccessMessage(calendar.getTitle()));

        view.displayMessage("Choose and enter a calendar title:");
        String titleToDelete = view.promoteString();
        Optional<Calendar> calendarToDelete = calendarDAO.findByTitleAndUsername(titleToDelete, username);
        if (!calendarToDelete.isPresent()) {
            view.displayWarningMessage("Calendar not found to delete. Enter exact calendar title to delete.");
            return;
        }

        // Delete associated meetings first
        for (Meeting meeting : meetingDAO.findAllMeetingsByCalendarId(calendarToDelete.get().getId())) {
            meetingDAO.deleteMeeting(meeting.getId());
        }
        calendarDAO.deleteCalendar(calendarToDelete.get().getId());
        view.displaySuccessMessage("Calendar deleted successfully.");
        view.displaySuccessMessage("All meetings in this calendar are also deleted successfully.");

        view.displayMessage("Calendars list after deleting:");
        Collection<Calendar> allCalendarsAfterDelete = calendarDAO.findCalendarByUsername(username);
        if (allCalendarsAfterDelete.isEmpty()) {
            view.displayWarningMessage("No calendar found.");
            return;
        }
        allCalendarsAfterDelete.forEach(calendar -> view.displaySuccessMessage(calendar.getTitle()));
    }

    private void displayCalendar() {
        if (!isLoggedIn) {
            view.displayWarningMessage("You need to login first.");
            return;
        }
        view.displayMessage("Calendars list:");
        Collection<Calendar> allCalendars = calendarDAO.findCalendarByUsername(username);
        if (allCalendars.isEmpty()) {
            view.displayWarningMessage("No calendar found.");
            return;
        }
        allCalendars.forEach(calendar -> view.displaySuccessMessage(calendar.getTitle()));

        view.displayMessage("Choose and enter a calendar title:");
        String calendarTitle = view.promoteString();
        Optional<Calendar> calendarObject = calendarDAO.findByTitleAndUsername(calendarTitle, username);
        if (!calendarObject.isPresent()) {
            view.displayWarningMessage("Calendar not found. Enter exact calendar title. ");
            return;
        }

        view.displayMessage("Calendar details:");
        view.displayCalendar(calendarObject.get());
        List<Meeting> meetings = (List<Meeting>) meetingDAO.findAllMeetingsByCalendarId(calendarObject.get().getId());
        meetings.forEach(meeting -> meeting.setCalendar(calendarObject.get()));
        view.displayMeetings(meetings);
    }
}
