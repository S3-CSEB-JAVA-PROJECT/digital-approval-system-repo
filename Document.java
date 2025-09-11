public class Document {
    String title;
    String content;
    String status;

    public Document(String title, String content) {
        this.title = title;
        this.content = content;
        this.status = "Pending (Tutor Review)";
    }
}
