import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UnifiedOrderBook {
    private final Map<Integer, Order> lookBook;
    private final Map<Double, Book> orderBook;

    public UnifiedOrderBook() {
        lookBook = new ConcurrentHashMap<>();
        orderBook = new ConcurrentHashMap<Double, Book>();
    }

    public boolean fillAndInsert(Order order) {
        if (getOrder(order.getId()) != null) {
            return false;
        }
        Book queues = orderBook.get(order.getPrice());
        if (queues != null) {
            orderBook.get(order.getPrice()).fillAndInsert(order, lookBook);
        } else {
            queues = new Book();
            queues.fillAndInsert(order, lookBook);
            orderBook.put(order.getPrice(), queues);
        }
        return true;
    }

    public boolean cancel(int id) {
        Order order = getOrder(id);
        if (order == null) {
            return false;
        }
        Book queues = orderBook.get(order.getPrice());
        if (queues != null) {
            return queues.remove(order, lookBook);
        }
        return false;
    }

    public boolean amend(Amend amend) {
        int id = amend.getId();
        Order order = getOrder(id);
        if (order == null) {
            return false;
        }
        if (amend.getAmendType() == Amend.PRICE_AMEND && amend.getPrice() != order.getPrice()) {
            cancel(id);
            order.setPrice(amend.getPrice());
            return fillAndInsert(order);
        } else if (amend.getAmendType() == Amend.QUANTITY_AMEND && amend.getQuantity() != order.getQuantity()) {
            return orderBook.get(order.getPrice()).amendQuantity(amend, order, lookBook);
        }
        return false;
    }

    public void clear() {
        orderBook.clear();
        lookBook.clear();
    }

    private Order getOrder(int id) {
        return lookBook.get(id);
    }

    public Map<Double, Book> getOrderBook() {
        return orderBook;
    }

    public Map<Integer, Order> getLookBook() {
        return lookBook;
    }
}
