package messaging;

import java.io.Serializable;

public class SearchResponse implements Serializable {
    public String bookTitle;
    public Double price;
    public String filePath;

    public SearchResponse(String bookTitle, Double price, String path) {
        this.bookTitle = bookTitle;
        this.price = price;
        this.filePath = path;
    }

    public SearchResponse() {
    }
}
