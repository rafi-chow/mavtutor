package rating;
import people.Person;
import java.util.ArrayList;

public class Comment {
    private String text;
    private Person author;
    private Comment inReplyTo;              
    private ArrayList<Comment> replies;     

    public Comment(String text, Person author, Comment inReplyTo) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Invalid text");
        }
        if (author == null) {
            throw new IllegalArgumentException("Invalid author");
        }

        this.text = text;
        this.author = author;
        this.inReplyTo = inReplyTo;
        this.replies = new ArrayList<>();
    }
    public void addReply(String text, Person author) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Invalid text");
        }
        if (author == null) {
            throw new IllegalArgumentException("Invalid author");
        }
        Comment reply = new Comment(text, author, this); 
        replies.add(reply);                              
    }

    public int numReplies() {
        return replies.size();
    }

    public Comment getReply(int index) {
        return replies.get(index); 
    }
    
    public Comment getInReplyTo() {
        return inReplyTo;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Comment by ").append(author.getName());
        if (inReplyTo != null) {
            sb.append(" in reply to ").append(inReplyTo.author.getName());
        }

        if (!replies.isEmpty()) {
            sb.append("\n");
            for (int i = 0; i < replies.size(); i++) {
                Comment c = replies.get(i);
                sb.append(i).append(". ").append(c.author.getName());
                if (i < replies.size() - 1) sb.append("\n");
            }
        }
        return sb.toString();
    }
}