package tickr.application.apis.purchase;

public class LineItem {
    private String itemName;
    private long price;

    public LineItem (String itemName, float price) {
        this.itemName = itemName;
        this.price = (long)Math.floor(price * 100);
    }

    public String getItemName () {
        return itemName;
    }

    public long getPrice () {
        return price;
    }
}
