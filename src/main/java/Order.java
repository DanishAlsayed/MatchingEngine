import java.util.Objects;

 class Order {
    private int id;
    private int quantity;
    private double price;
    private boolean sideBuy;
    private boolean mktOrder;

     Order(int id, int quantity, double price, boolean sideBuy) {
        validate(id, quantity, price);
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.sideBuy = sideBuy;
        mktOrder = (price == 0);
    }

     boolean isMktOrder() {
        return mktOrder;
    }

     int getId() {
        return id;
    }

     int getQuantity() {
        return quantity;
    }

     void setQuantity(int quantity) {
        this.quantity = quantity;
    }

     double getPrice() {
        return price;
    }

     void setPrice(double price) {
        this.price = price;
    }

     boolean isSideBuy() {
        return sideBuy;
    }

    private void validate(int id, int quantity, double price) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity cannot be less than or equal to 0");

        if ((price < 0 || id < 0)) {
            throw new IllegalArgumentException("Price and id cannot be less than 0. 0 price represents a market order while a 0 id represents orders from other participants in the market");
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return getId() == order.getId() &&
                getQuantity() == order.getQuantity() &&
                Double.compare(order.getPrice(), getPrice()) == 0 &&
                isSideBuy() == order.isSideBuy();
    }

    @Override
     public int hashCode() {
        return Objects.hash(getId(), getQuantity(), getPrice(), isSideBuy());
    }

    @Override
     public String toString() {
        return "Order{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", price=" + price +
                ", sideBuy=" + sideBuy +
                '}';
    }
}
