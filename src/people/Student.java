package people;
import session.Course;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
public class Student extends Person {
    private static int nextStudentID = 0;
    private int studentID;
    private ArrayList<Course> courses;

    public Student(String name, String email) {
        super(name, email);
        this.studentID = nextStudentID++;
        this.courses = new ArrayList<>();

    }
    
    public void save(PrintStream out) {
        super.save(out);
        out.println(studentID);
        out.println(nextStudentID);
        out.println(courses.size());
        for (Course c : courses) {
            c.save(out);
        }

    }

    public Student(Scanner in) {
        super(in);
        this.studentID = Integer.parseInt(in.nextLine());
        Student.nextStudentID = Integer.parseInt(in.nextLine());
        int size = Integer.parseInt(in.nextLine());

        this.courses = new ArrayList<>();
        for(int i=0; i<size; i++) {
            this.courses.add(new Course(in));

        }
    }
    public void addCourse(Course course) {
        courses.add(course);
    }

    public Course[] getCourses() {
        return courses.toArray(new Course[0]);
    }

    @Override
    public String toString() {
        return super.toString().replace(")", ", #" + studentID + ")");
    }
}