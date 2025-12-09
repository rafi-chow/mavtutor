package mdi;
import menu.Menu;
import menu.MenuItem;
import java.util.ArrayList;
import java.util.List;
import people.Student;
import people.Tutor;
import rating.Rateable;
import session.Course;
import session.Session;
import session.InvalidCourseException;
import people.Person;
import rating.Comment;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import rating.Rating;


/**
 * MavTutor: minimal menu-driven console app to manage courses, students, tutors, and sessions.
 * Uses the provided menu.Menu and menu.MenuItem. Exits when menu.result is set to null.
 * The menu prints the title, then menu and last result, then the current view (via toString), then a prompt.
 *
 * @author  Rafi Chowdhury
 * @version 1.0
 */
public class MavTutor {
    private File file;
    private final List<Course>  courses  = new ArrayList<>();
    private final List<Student> students = new ArrayList<>();
    private final List<Tutor>   tutors   = new ArrayList<>();
    private final List<Session> sessions = new ArrayList<>();
    /** Current data view: 0=courses, 1=students, 2=tutors, 3=sessions. */
    private int view = 0;

    private boolean dirty;

    private Menu menu;
    /**
     * Program entry point. Constructs MavTutor and starts the menu loop.
     */
    public static void main(String[] args) {
        new MavTutor();
    }
    /**
     * Builds the menu and starts the loop with Menu.run().
     * Passes this in the Menu post parameter so toString() renders the current view.
     */
    public MavTutor() {
        String clear = "\n".repeat(80);
        String title = "MavTutor";
        title += '\n' + "=".repeat(title.length()) + '\n';
        menu = new Menu(
            new Object[]{ clear, title },          
            new Object[]{ this, "\nSelection?" },  
            new MenuItem("Exit\n",        this::quit),
            new MenuItem("Create Course", this::newCourse),
            new MenuItem("Create Student",this::newStudent),
            new MenuItem("Create Tutor",  this::newTutor),
            new MenuItem("Create Session",this::newSession),
            new MenuItem("View Courses",  () -> view = 0),
            new MenuItem("View Students", () -> view = 1),
            new MenuItem("View Tutors",   () -> view = 2),
            new MenuItem("View Sessions", () -> view = 3),
            new MenuItem("New",    this::newz),
            new MenuItem("Save",   this::save),
            new MenuItem("Save As…", this::saveAs),
            new MenuItem("Open…",  this::open),
            new MenuItem("Review Student", () -> review(students)),
            new MenuItem("Review Tutor", () -> review(tutors)),
            new MenuItem("Review Session", () -> review(sessions))

           
        );
        file = null;
        dirty = false;
        menu.run();
    }


    private void review(List<? extends Rateable> items) {
        if (items == null || items.isEmpty()) {
            if (menu != null && menu.result != null) menu.result.append("Nothing to review.\n");
            return;
        }

        Integer idx = Menu.selectItemFromList("\nSelect item to review: ", items);
        if (idx == null || idx < 0 || idx >= items.size()) {
            if (menu != null && menu.result != null) menu.result.append("Canceled\n");
            return;
        }
        Rateable target = items.get(idx);

        double avg = target.getAverageRating();
        if (menu != null && menu.result != null) {
            menu.result.append("Average rating: ")
                    .append(Double.isNaN(avg) ? "NaN" : String.format("%.2f", avg))
                    .append("\n");
        }

        Person reviewer = login();

        String add = Menu.getString("Add a new rating and review? (y/N): ", null, "n");
        if ("y".equalsIgnoreCase(add)) {
            if (reviewer == null) {
                if (menu != null && menu.result != null) menu.result.append("Must be logged in to add a rating.\n");
            } else {
                Integer stars = Menu.getInt("Stars (1-5): ");
                if (stars == null || stars < 1 || stars > 5) {
                    if (menu != null && menu.result != null) menu.result.append("Invalid stars\n");
                } else {
                    String text = Menu.getString("Review text: ", null, "");
                    Comment root = new Comment(text == null ? "" : text, reviewer, null);
                    Rating rating = new Rating(stars, root);
                    target.addRating(rating);
                    dirty = true;
                    double newAvg = target.getAverageRating();
                    if (menu != null && menu.result != null) {
                        menu.result.append("Rating added. New average: ")
                                .append(Double.isNaN(newAvg) ? "NaN" : String.format("%.2f", newAvg))
                                .append("\n");
                    }
                }
            }
        }

        Rating[] ratings = target.getRatings();
        if (ratings == null || ratings.length == 0) {
            if (menu != null && menu.result != null) menu.result.append("No ratings to browse.\n");
            return;
        }

        Integer ri = Menu.selectItemFromArray("Select a rating to browse: ", ratings);
        if (ri == null || ri < 0 || ri >= ratings.length) {
            if (menu != null && menu.result != null) menu.result.append("Cnceled\n");
            return;
        }

        Comment current = ratings[ri].getReview();
        if (current == null) {
            if (menu != null && menu.result != null) menu.result.append("Trating has no review/comment tree.\n");
            return;
        }

        while (true) {
            if (menu != null && menu.result != null) {
                menu.result.append("\n--- Comments ---\n");
                printExpandedComments(current, 0);
                menu.result.append("\nOptions: (R)eply  (U)p  (D)own  (M)ain Menu\n");
            }

            String choice = Menu.getString("Choose: ", null, "");
            if (choice == null || choice.isBlank()) continue;
            char ch = Character.toLowerCase(choice.trim().charAt(0));

            if (ch == 'm') return;
            else if (ch == 'r') {
                if (reviewer == null) reviewer = login();
                if (reviewer == null) {
                    if (menu != null && menu.result != null) menu.result.append("Reply canceled (not logged in)\n");
                    continue;
                }
                String reply = Menu.getString("Reply text: ", null, "");
                if (reply != null) {
                    current.addReply(reply, reviewer);
                    dirty = true;
                }
            } else if (ch == 'u') {
                if (current.getInReplyTo() != null) current = current.getInReplyTo();
            } else if (ch == 'd') {
                if (current.numReplies() == 0) {
                    if (menu != null && menu.result != null) menu.result.append("No replies to go down to.\n");
                    continue;
                }
                Integer which = Menu.getInt("Reply index (0.." + (current.numReplies()-1) + "): ");
                if (which != null && which >= 0 && which < current.numReplies()) {
                    current = current.getReply(which);
                }
            }
        }
    }


    private Person login() {
    String[] options = new String[] { "Cancel login", "Tutor login", "Student login" };
    Integer sel = Menu.selectItemFromArray("Login options: ", options);
    if (sel == null) return null;

    switch (sel) {
        case 0:
            return null;
        case 1:
            if (tutors.isEmpty()) {
                if (menu != null && menu.result != null) menu.result.append("No tutors.\n");
                return null;
            }
            Integer ti = Menu.selectItemFromList("Select tutor: ", tutors);
            if (ti != null && ti >= 0 && ti < tutors.size()) return tutors.get(ti);
            return null;
        case 2:
            if (students.isEmpty()) {
                if (menu != null && menu.result != null) menu.result.append("No students.\n");
                return null;
            }
            Integer si = Menu.selectItemFromList("Select student: ", students);
            if (si != null && si >= 0 && si < students.size()) return students.get(si);
            return null;
        default:
            return null;
    }
}


    private void printExpandedComments(Comment c, int level) {
        printIndented(c.toString(), level);
        if (menu != null && menu.result != null) menu.result.append("\n");
        for (int i = 0; i < c.numReplies(); ++i) {
            printExpandedComments(c.getReply(i), level + 2);
        }
    }

    private void printIndented(String multiline, int level) {
        String[] lines = multiline.split("\n");
        for (String s : lines) {
            if (menu != null && menu.result != null) {
                menu.result.append(" ".repeat(Math.max(0, level))).append(s).append("\n");
            }
        }
    }


    private boolean safeToDiscardData() {
        if (!dirty) return true;

        while (true) {
            String answer = Menu.getString("Unsaved data: (S)ave, (D)iscard, (A)bort? ");
            if (answer == null || answer.isBlank()) {
                return false;
            }
            answer = answer.trim().toLowerCase();
            char ch = answer.charAt(0);
            if (ch == 's') {
                save();
                dirty = false;
            } else if (ch == 'd') {
                dirty = false;
            } else if (ch == 'a') {
                return false;
            } else {
                if (menu != null && menu.result != null) {
                    menu.result.append("Please enter S, D, or A.\n");
                }
                continue;
            }
            return !dirty;
        }
    }

    private void newz() {
        if (!safeToDiscardData()) return;

        courses.clear();
        students.clear();
        tutors.clear();
        sessions.clear();
        file = null;
        dirty = false;
        if (menu != null && menu.result != null) {
            menu.result.append("Started a new MavTutor (data cleared)\n");
        }
    }
    private void saveAs() {
        file = null;
        save();
    }

    private void save() {
        try {

            if (file == null) {
                String filename = Menu.getString("Filename to save as: ");
                if (filename == null || filename.isBlank()) {
                    if (menu != null && menu.result != null) {
                        menu.result.append("Save canceled\n");
                    }
                    return;
                }
                file = new File(filename.trim());
            }


            try (PrintStream out = new PrintStream(file)) {

                out.println(courses.size());
                for (Course c : courses) c.save(out);


                out.println(students.size());
                for (Student s : students) s.save(out);

                out.println(tutors.size());
                for (Tutor t : tutors) t.save(out);


                out.println(sessions.size());
                for (Session ses : sessions) ses.save(out);
            }

            dirty = false;

            if (menu != null && menu.result != null) {
                menu.result.append("Saved to ").append(file.getName()).append("\n");
            }

        } catch (IOException | RuntimeException e) {
            if (menu != null && menu.result != null) {
                menu.result.append("Error saving: ")
                        .append(e.getMessage() == null ? "unknown error" : e.getMessage())
                        .append("\n");
            }

        }
}
    private void open() {
        if (!safeToDiscardData()) return;

        try {
            String filename = Menu.getString("Filename to open: ");
            if (filename == null || filename.isBlank()) {
                if (menu != null && menu.result != null) {
                    menu.result.append("Open canceled\n");
                }
                return;
            }

            File chosen = new File(filename.trim());
            if (!chosen.exists()) {
                if (menu != null && menu.result != null) {
                    menu.result.append("File not found: ").append(chosen.getName()).append("\n");
                }
                return;
            }

            try (Scanner in = new Scanner(chosen)) {


                int nCourses = Integer.parseInt(in.nextLine());
                courses.clear();
                for (int i = 0; i < nCourses; ++i) {
                    courses.add(new Course(in));
                }


                int nStudents = Integer.parseInt(in.nextLine());
                students.clear();
                for (int i = 0; i < nStudents; ++i) {
                    students.add(new Student(in));
                }


                int nTutors = Integer.parseInt(in.nextLine());
                tutors.clear();
                for (int i = 0; i < nTutors; ++i) {
                    tutors.add(new Tutor(in));
                }


                int nSessions = Integer.parseInt(in.nextLine());
                sessions.clear();
                for (int i = 0; i < nSessions; ++i) {
                    sessions.add(new Session(in));
                }

                file = chosen; 
                dirty = false;

                if (menu != null && menu.result != null) {
                    menu.result.append("Opened ").append(file.getName()).append("\n");
                }
            }

        } catch (IOException | RuntimeException e) {
            if (menu != null && menu.result != null) {
                menu.result.append("Error opening: ")
                        .append(e.getMessage() == null ? "unknown error" : e.getMessage())
                        .append("\n");
                menu.result.append("Clearing partially loaded data.\n");
            }
            newz();  
        }
    }
    private void quit() {
        menu.result = null; 
    }

    private void newCourse() {
        try {
            String dept = Menu.getString("Department (e.g., CSE): ");
            if (dept == null) { menu.result.append("Canceled\n"); return; }
            Integer num = Menu.getInt("Number (e.g., 1325): ");
            if (num == null) { menu.result.append("Canceled\n"); return; }
            Course c = new Course(dept, num);
            if (courses.contains(c)) { menu.result.append("Course already exists\n"); return; }
            courses.add(c);
            dirty = true;
            menu.result.append("Added course ").append(c).append('\n');
        } catch (InvalidCourseException e) {
            menu.result.append("Error: ").append(e.getMessage()).append('\n');
        }
    }
    private void newTutor() {
        if (courses.isEmpty()) { menu.result.append("Add courses before tutors\n"); return; }

        String name  = Menu.getString("Tutor name: ");
        if (name == null) { menu.result.append("Canceled\n"); return; }
        String email = Menu.getString("Tutor email: ");
        if (email == null) { menu.result.append("Canceled\n"); return; }
        String bio   = Menu.getString("Short bio: ");
        if (bio == null) { menu.result.append("Canceled\n"); return; }
        String ssn   = Menu.getString("SSN: ");
        if (ssn == null) { menu.result.append("Canceled\n"); return; }

        int ssnInt;
        try {
            ssnInt = Integer.parseInt(ssn);
        } catch (NumberFormatException e) {
            menu.result.append("Invalid SSN\n");
            return;
        }

        Integer ci = Menu.selectItemFromList("\nSelect course index: ", courses);
        if (ci == null || ci < 0 || ci >= courses.size()) { menu.result.append("Canceled\n"); return; }

        Tutor t = new Tutor(name, email, ssnInt, bio, courses.get(ci));
        tutors.add(t);
        dirty = true;
        menu.result.append("Added tutor ").append(t).append('\n');
    }


    private void newStudent() {
        if (courses.isEmpty()) { menu.result.append("Add courses before students\n"); return; }

        String name  = Menu.getString("Student name: ");
        if (name == null) { menu.result.append("Canceled\n"); return; }
        String email = Menu.getString("Student email: ");
        if (email == null) { menu.result.append("Canceled\n"); return; }

        Student s = new Student(name, email);

     
        while (true) {
            Integer ci = Menu.selectItemFromList("\nSelect course index (Enter to stop): ", courses);
            if (ci == null || ci < 0 || ci >= courses.size()) break;
            s.addCourse(courses.get(ci));
            String more = Menu.getString("Add another course? (y/N): ", null, "n");
            if (!"y".equalsIgnoreCase(more)) break;
        }

        students.add(s);
        dirty = true;
        menu.result.append("Added student ").append(s).append('\n');
    }

    private void newSession() {
        if (courses.isEmpty())  { menu.result.append("Add courses first\n");  return; }
        if (tutors.isEmpty())   { menu.result.append("Add tutors first\n");   return; }
        if (students.isEmpty()) { menu.result.append("Add students first\n"); return; }

        Integer ci = Menu.selectItemFromList("\nSelect course index: ", courses);
        if (ci == null || ci < 0 || ci >= courses.size()) { menu.result.append("Canceled\n"); return; }
        Course c = courses.get(ci);

        Integer ti = Menu.selectItemFromList("\nSelect tutor index: ", tutors);
        if (ti == null || ti < 0 || ti >= tutors.size()) { menu.result.append("Canceled\n"); return; }
        Tutor t = tutors.get(ti);

        Session ses = new Session(c, t);

        String date = Menu.getString("Date (e.g., 2025-03-01): ");
        if (date == null) { menu.result.append("Canceled\n"); return; }
        String time = Menu.getString("Start time (e.g., 14:00): ");
        if (time == null) { menu.result.append("Canceled\n"); return; }
        Integer dur = Menu.getInt("Duration (minutes): ");
        if (dur == null) { menu.result.append("Canceled\n"); return; }
        ses.setSchedule(date, time, dur);


        while (true) {
            Integer si = Menu.selectItemFromList("\nAdd student index (Enter to stop): ", students);
            if (si == null || si < 0 || si >= students.size()) break;
            ses.addStudent(students.get(si));
            String more = Menu.getString("Add another student? (y/N): ", null, "n");
            if (!"y".equalsIgnoreCase(more)) break;
        }

        sessions.add(ses);
        dirty = true;
        menu.result.append("Added ").append(ses).append('\n');
    }
    /**
     * Returns the current data view text for the Menu to print.
     */
    @Override
    public String toString() {
        switch (view) {
            case 0: return Menu.listToString("Courses\n-------\n",  courses,  '•');
            case 1: return Menu.listToString("Students\n--------\n", students, '•');
            case 2: return Menu.listToString("Tutors\n------\n",   tutors,   '•');
            case 3: return Menu.listToString("Sessions\n--------\n", sessions, '•');
            default: return "";
        }
    }
}
