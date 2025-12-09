package session;    
import java.util.Objects;
import java.util.Scanner;
import java.io.PrintStream;

public class Course {
    private String dept;
    private int number;

    public Course(String dept, int number) {

        if (dept == null || dept.length() != 3 && dept.length() != 4) {
            throw new InvalidCourseException(dept); }
        if (number < 1000 || number > 9999) {
            throw new InvalidCourseException(dept, number);
        }

        this.dept = dept;
        this.number = number;

    }
    /**
     * Writes the date, start time, and end time to a PrintStream.
     * @param out the stream to write to
     */
    public void save(PrintStream out) {
        out.println(dept);
        out.println(number);
    }
    /**
     * Restores a DateRange by reading data from a Scanner.
     * Must read fields in the same order they were saved.
     * @param in the scanner to read from
     */
    public Course(Scanner in) {
        this(in.nextLine(), Integer.parseInt(in.nextLine()));
    }
 
    @Override
    public String toString() {
        return dept + number;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != getClass()) return false;
        Course other = (Course) o;
        return number == other.number && dept.equals(other.dept);

    }

    @Override
    public int hashCode() {
        return Objects.hash(dept, number);
    }
}