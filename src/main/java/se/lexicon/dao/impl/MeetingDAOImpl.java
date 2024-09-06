package se.lexicon.dao.impl;

import se.lexicon.dao.MeetingDAO;
import se.lexicon.exception.MySQLException;
import se.lexicon.model.Meeting;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MeetingDAOImpl implements MeetingDAO {

    private Connection connection;

    public MeetingDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Meeting createMeeting(Meeting meeting) {
        String insertQuery = "INSERT INTO meetings (title, start_time, end_time, _description, calendar_id) VALUES (?, ?, ?, ?, ?)";

        try (
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)

        ) {
            preparedStatement.setString(1, meeting.getTitle());
            // Convert the LocalDateTime (startTime) to a Timestamp and set it in the SQL query.
            // The Timestamp class is used to represent SQL TIMESTAMP values, which includes date and time.
            preparedStatement.setTimestamp(2, Timestamp.valueOf(meeting.getStartTime()));
            preparedStatement.setTimestamp(3, Timestamp.valueOf(meeting.getEndTime()));
            preparedStatement.setString(4, meeting.getDescription());
            preparedStatement.setInt(5, meeting.getCalendar().getId());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                String errorMessage = "Creating meeting failed, no rows affected.";
                throw new MySQLException(errorMessage);
            }
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int meetingId = generatedKeys.getInt(1);
                    return new Meeting(meetingId, meeting.getTitle(), meeting.getStartTime(), meeting.getEndTime(), meeting.getDescription(), meeting.getCalendar());
                } else {
                    String errorMessage = "Creating meeting failed, no ID obtained.";
                    throw new MySQLException(errorMessage);
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error occurred while creating a meeting";
            throw new MySQLException(errorMessage, e);
        }
    }

    @Override
    public Optional<Meeting> findById(int meetingId) {
        String selectQuery = "SELECT * FROM meetings WHERE id=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setInt(1, meetingId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                Timestamp startTime = resultSet.getTimestamp("start_time");
                Timestamp endTime = resultSet.getTimestamp("end_time");
                String description = resultSet.getString("_description");

                LocalDateTime startDateTime = startTime.toLocalDateTime();
                LocalDateTime endDateTime = endTime.toLocalDateTime();

                // Create a Meeting object with retrieved data
                Meeting meeting = new Meeting(id, title, startDateTime, endDateTime, description);

                return Optional.of(meeting);
            }
        } catch (SQLException e) {
            String errorMessage = "Error occurred while finding a meeting by ID " + meetingId;
            throw new MySQLException(errorMessage, e);
        }
        return Optional.empty();
    }

    @Override
    public Collection<Meeting> findAllMeetingsByCalendarId(int calendarId) {
        List<Meeting> meetings = new ArrayList<>();
        String selectQuery = "SELECT * FROM meetings WHERE calendar_id = ?";
        try (

                PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)
        ) {
            preparedStatement.setInt(1, calendarId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {

                    int meetingId = resultSet.getInt("id");
                    String title = resultSet.getString("title");
                    Timestamp startTime = resultSet.getTimestamp("start_time");
                    Timestamp endTime = resultSet.getTimestamp("end_time");
                    String description = resultSet.getString("_description");

                    LocalDateTime startDateTime = startTime.toLocalDateTime();
                    LocalDateTime endDateTime = endTime.toLocalDateTime();
                    Meeting meeting = new Meeting(meetingId, title, startDateTime, endDateTime, description);

                    meetings.add(meeting);
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error occurred while retrieving all meetings";
            throw new MySQLException(errorMessage, e);
        }
        return meetings;
    }

    @Override
    public boolean deleteMeeting(int meetingId) {
        String deleteQuery = "DELETE FROM meetings WHERE id = ?";

        try (
                PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)
        ) {
            preparedStatement.setInt(1, meetingId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            String errorMessage = "Error occurred while deleting meeting with ID: " + meetingId;
            throw new MySQLException(errorMessage, e);
        }
    }
}
