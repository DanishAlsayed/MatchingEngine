public class Amend {
    public static final int QUANTITY_AMEND = 1;
    public static final int PRICE_AMEND = 0;

    private final int amendType;
    private final double price;
    private final int quantity;
    private final int id;

    public Amend(int id, double price, int amendType) {
        if (price >= 0.0 && amendType == PRICE_AMEND && id > 0) {
            this.id = id;
            this.amendType = amendType;
            this.price = price;
            this.quantity = -1;
        } else
            throw new IllegalArgumentException("Price amends must have the amendType as PRICE_AMEND and price & id cannot be <= 0");
    }

    public Amend(int id, int quantity, int amendType) {
        if (quantity >= 0 && amendType == QUANTITY_AMEND && id > 0) {
            this.id = id;
            this.amendType = amendType;
            this.quantity = quantity;
            this.price = -1.0;
        } else
            throw new IllegalArgumentException("Quantity amends must have the amendType as QUANTITY_AMEND and quantity & id cannot be <= 0");
    }

    public int getAmendType() {
        return amendType;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getId() {
        return id;
    }

}
