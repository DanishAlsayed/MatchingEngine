import java.util.Objects;

public class Order {
    private int id;
    private int quantity;
    private double price;
    private boolean sideBuy;
    private boolean mktOrder;

    //TODO: add support for market orders, price will be -1
    public Order(int id, int quantity, double price, boolean sideBuy) {
        validate(id, quantity, price);
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.sideBuy = sideBuy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isSideBuy() {
        return sideBuy;
    }

    public void setSideBuy(boolean sideBuy) {
        this.sideBuy = sideBuy;
    }

    private void validate(int id, int quantity, double price) {
        //TODO: order id can be -1 for market data stream, for id check for -1 OR > 0
        if (id <= 0 || quantity <= 0 || price <= 0.0)
            throw new IllegalArgumentException("Order id, quantity and price must all be positive.");
    }

    //TODO: consider comparing only based on id?
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
