package session;   
public class InvalidCourseException extends IllegalArgumentException {
    public InvalidCourseException(String dept) {
        super("invalid dept in new course" + dept); 
    }

    public InvalidCourseException(String dept, int number) {
        super("Invalid course number in new course: " + number);
    }
}