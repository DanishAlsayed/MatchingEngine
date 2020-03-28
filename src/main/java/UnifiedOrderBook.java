import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

public class UnifiedOrderBook {
    private final Map<Integer, Order> lookBook;
    private final Map<Double, Book> orderBook;
    private PriorityQueue<Double> minPrices;
    private PriorityQueue<Double> maxPrices;

    public UnifiedOrderBook() {
        lookBook = new ConcurrentHashMap<>();
        orderBook = new ConcurrentHashMap<Double, Book>();
        minPrices = new PriorityQueue<Double>();
        //TODO: verify
        maxPrices = new PriorityQueue<Double>((o1, o2) -> -Double.compare(o1, o2));
    }

    public boolean fillAndInsert(Order order) {
        if (getOrder(order.getId()) != null) {
            return false;
        }
        Book queues = orderBook.get(order.getPrice());
        //TODO: refactor these if/else blocks
        if (queues != null) {
            if (order.isSideBuy()) {
                queues.fillAndInsert(order, lookBook, maxPrices);
            } else {
                queues.fillAndInsert(order, lookBook, minPrices);
            }
        } else {
            queues = new Book();
            if (order.isSideBuy()) {
                queues.fillAndInsert(order, lookBook, maxPrices);
            } else {
                queues.fillAndInsert(order, lookBook, minPrices);
            }
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
            if (order.isSideBuy()) {
                queues.remove(order, lookBook, maxPrices);
            } else {
                queues.remove(order, lookBook, minPrices);
            }
            return true;
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

    public PriorityQueue<Double> getMinPrices() {
        return minPrices;
    }

    public PriorityQueue<Double> getMaxPrices() {
        return maxPrices;
    }
}
