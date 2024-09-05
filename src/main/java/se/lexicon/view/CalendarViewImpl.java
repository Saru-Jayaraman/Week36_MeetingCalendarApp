package se.lexicon.view;

import se.lexicon.model.Calendar;
import se.lexicon.model.Meeting;
import se.lexicon.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class CalendarViewImpl implements CalendarView {
    @Override
    public void displayUser(User user) {
        System.out.println(user.userInfo());
        System.out.println("------------------------");
    }

    @Override
    public void displayCalendar(Calendar calendar) {
        System.out.println(calendar.calendarInfo());
        System.out.println("------------------------");
    }

    @Override
    public void displayMeetings(List<Meeting> meetings) {
        if (meetings.isEmpty()) {
            System.out.println("No meetings in this calendar");
        } else {
            System.out.println("Meetings in this calendar: ");
            meetings.forEach(meeting -> {
                System.out.println(meeting.meetingInfo());
                System.out.println("------------------------");
            });
        }
    }

    @Override
    public String promoteString() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    @Override
    public User promoteUserForm() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a username: ");
        String username = scanner.nextLine();
        System.out.println("Enter a password: ");
        String password = scanner.nextLine();
        return new User(username, password);
    }

    @Override
    public Meeting promoteMeetingForm() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter title:");
        String title = scanner.nextLine();
        System.out.println("Enter description:");
        String description = scanner.nextLine();

        System.out.println("Start Date & Time (yyyy-MM-dd HH:mm): ");
        String start = scanner.nextLine();
        LocalDateTime startLocalDateTime = LocalDateTime.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        if (startLocalDateTime.isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Enter a future date & time.");

        System.out.println("End Date & Time (yyyy-MM-dd HH:mm): ");
        String end = scanner.nextLine();
        LocalDateTime endLocalDateTime = LocalDateTime.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        if (endLocalDateTime.isBefore(startLocalDateTime))
            throw new IllegalArgumentException("Enter a end date & time after start date & time.");
        return new Meeting(title, startLocalDateTime, endLocalDateTime, description);

    }

    @Override
    public String promoteCalendarForm() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a title: ");
        return scanner.nextLine();
    }
}
