import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

class UnifiedOrderBook implements OrderBook {
    //Does not keep track of market updates
    private final Map<Integer, Order> lookBook;
    private final Map<Double, OrderQueue> orderBook;
    private PriorityQueue<Double> minPrices;
    private PriorityQueue<Double> maxPrices;
    private ReentrantLock lock;

    private final static int MARKET_UPDATE_ID = 0;

    UnifiedOrderBook() {
        lookBook = new ConcurrentHashMap<>();
        orderBook = new ConcurrentHashMap<>();
        minPrices = new PriorityQueue<>();
        maxPrices = new PriorityQueue<>((o1, o2) -> -Double.compare(o1, o2));
        lock = new ReentrantLock(true);
    }

    @Override
    public boolean fillAndInsert(Order order) {
        lock.lock();
        try {
            int id = order.getId();
            if (id != MARKET_UPDATE_ID && getOrder(id) != null) {
                System.out.println("Order with id:" + id + " already exists, fillAndInsert failed");
                return false;
            }

            if (order.getPrice() == 0 && !setMarketPrice(order)) {
                return false;
            }

            OrderQueue queues = orderBook.get(order.getPrice());
            if (queues != null) {
                queues.fillAndInsert(order, lookBook, maxPrices, minPrices);
            } else {
                queues = new OrderQueue();
                queues.fillAndInsert(order, lookBook, maxPrices, minPrices);
                orderBook.put(order.getPrice(), queues);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    private boolean setMarketPrice(Order order) {
        lock.lock();
        try {
            System.out.println("Market order " + order + " will be given a price of the fartouch and any residual quantity will be inserted in the queue at that price.");
            if (order.isSideBuy() && !minPrices.isEmpty()) {
                order.setPrice(minPrices.peek());
                return true;
            } else if (!order.isSideBuy() && !maxPrices.isEmpty()) {
                order.setPrice(maxPrices.peek());
                return true;
            }
            System.out.println("No limit order to match market " + order + " against. This order will be not be entertained.");
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean cancel(int id) {
        lock.lock();
        if (getOrder(id) == null) {
            return false;
        }
        try {
            Order order = getOrder(id);
            if (order == null) {
                System.out.println("Order with id " + id + " not found, cancellation failed.");
                return false;
            }
            OrderQueue queues = orderBook.get(order.getPrice());
            if (queues != null) {
                if (order.isSideBuy()) {
                    queues.remove(order, lookBook, maxPrices);
                } else {
                    queues.remove(order, lookBook, minPrices);
                }
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean amend(Amend amend) {
        lock.lock();
        try {
            int id = amend.getId();
            if (getOrder(id) == null) {
                return false;
            }
            Order order = getOrder(id);
            if (order == null) {
                System.out.println("Order with id " + id + " not found, amend failed.");
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
        } finally {
            lock.unlock();
        }
    }

    void clear() {
        lock.lock();
        try {
            orderBook.clear();
            lookBook.clear();
        } finally {
            lock.unlock();
        }
    }

    static boolean isMarketUpdate(int id) {
        return id == MARKET_UPDATE_ID;
    }

    private Order getOrder(int id) {
        if (id == MARKET_UPDATE_ID)
            return null;
        return lookBook.get(id);
    }

    Map<Double, OrderQueue> getOrderBook() {
        return orderBook;
    }

    Map<Integer, Order> getLookBook() {
        return lookBook;
    }

    PriorityQueue<Double> getMinPrices() {
        return minPrices;
    }

    PriorityQueue<Double> getMaxPrices() {
        return maxPrices;
    }
}
