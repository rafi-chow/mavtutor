package session;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import people.Tutor;
import people.Student;
import rating.Rating;
import rating.Rateable;
import java.io.PrintStream;

/*
 * initializes course, daterange, tutor, student
 */

public class Session implements Rateable {
    private Course course;
    private DateRange dates;
    private Tutor tutor;
    private List<Student> students;
    private ArrayList<Rating> ratings = new ArrayList<>();

    /*
    * consructs course, tutor, students
    */
public Session(Course course, Tutor tutor) {
    this.course = course;
    this.tutor = tutor;
    this.students = new ArrayList<>();
}
public void save(PrintStream out) {
    course.save(out);
    dates.save(out);
    tutor.save(out);

    out.println(students.size());
    for(Student s : students) {
        s.save(out);
    }
}

public Session(Scanner in) {
    this.course = new Course(in);
    this.dates = new DateRange(in);
    this.tutor = new Tutor(in);
    this.students = new ArrayList<>();

    int size = Integer.parseInt(in.nextLine());
    for(int i=0; i < size; i++) {
        students.add(new Student(in));
    }
}

    /**
     * sets the schedule using a DateRange
     * @param date date of session
     * @param startTime time it starts (24h)
     * @param duration how long in minutes
     */
public void setSchedule(String date, String startTime, long duration) {
    this.dates = new DateRange(date, startTime, duration);
}
    /**
     * adds a student to the session
     * @param student who joins
     */

public void addStudent(Student student) {
    students.add(student);
}

@Override
public void addRating(Rating rating) {
    ratings.add(rating);
}

@Override
public double getAverageRating() {
    if (ratings.isEmpty()) {
        return Double.NaN;
    }
    double total = 0.0;
    for (Rating r: ratings) {
        total += r.getStars();
    }
    return total / ratings.size();
}

@Override
public Rating[] getRatings() {
    return ratings.toArray(new Rating[0]);
}

    /**
     * prints course, time, tutor, and students
     */
@Override
public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("Session on ");
    sb.append(course);
    sb.append(" at ");
    sb.append(dates.toString());
    sb.append("\n");
    sb.append(" Tutor: ");
    sb.append(tutor.getName());
    sb.append(" Students: ");
    sb.append("\n");
    for (Student s : students) {
        sb.append(s);
        sb.append("\n");
    }
    return sb.toString();



}
}