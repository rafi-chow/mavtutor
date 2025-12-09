package people; 
import rating.Rating;
import rating.Rateable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.io.PrintStream;
public class Person implements Rateable{
    protected String name;
    protected String email;
    private ArrayList<Rating> ratings = new ArrayList<>();

    public Person(String name, String email) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Invalid name");
        }

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Invalid email");
        }
        this.name = name;
        this.email = email; }

    public void save(PrintStream out) {
        out.println(name);
        out.println(email);
    }

    public Person(Scanner in) {
        this(in.nextLine(), in.nextLine());

    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person p = (Person) o;
        if (name.equals(p.name) == false) return false;
        if (email.equals(p.email) == false) return false;

        return true;
        
    }
    @Override
    public String toString() {
        return name + "(" + email + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email);
    }

    @Override
    public void addRating(Rating rating) {
        ratings.add(rating);
    }

    @Override
    public double getAverageRating() {
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
}







