package tickr.application.serialised.responses;

import java.util.List;

public class CategoriesResponse {
    public List<String> categories;

    public CategoriesResponse () {}

    public CategoriesResponse (List<String> categories) {
        this.categories = categories;
    }
}
