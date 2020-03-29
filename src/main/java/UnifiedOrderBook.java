import com.google.common.collect.HashMultimap;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

class UnifiedOrderBook {
    private final Map<Integer, Order> lookBook;
    private final Map<Double, Book> orderBook;
    private PriorityQueue<Double> minPrices;
    private PriorityQueue<Double> maxPrices;
    private ReentrantLock lock;

    UnifiedOrderBook() {
        lookBook = new ConcurrentHashMap<>();
        orderBook = new ConcurrentHashMap<>();
        minPrices = new PriorityQueue<>();
        maxPrices = new PriorityQueue<>((o1, o2) -> -Double.compare(o1, o2));
        lock = new ReentrantLock(true);
    }

    boolean fillAndInsert(Order order) {
        lock.lock();
        try {
            int id = order.getId();
            if (id != 0 && getOrder(id) != null) {
                System.out.println("Order with id:" + id + " already exists, fillAndInsert failed");
                return false;
            }

            if (order.getPrice() == 0 && !setMarketPrice(order)) {
                return false;
            }

            Book queues = orderBook.get(order.getPrice());
            if (queues != null) {
                queues.fillAndInsert(order, lookBook, maxPrices, minPrices);
            } else if (!order.isMktOrder()) {
                queues = new Book();
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
            System.out.println("Market order " + order + " will be treated as immediate or cancel i.e. it will be filled as " +
                    "much as possible, any residual quantity will not be inserted in the book");
            if (order.isSideBuy() && !minPrices.isEmpty()) {
                order.setPrice(minPrices.poll());
                return true;
            } else if (!order.isSideBuy() && !maxPrices.isEmpty()) {
                order.setPrice(maxPrices.poll());
                return true;
            }
            System.out.println("No corresponding limit order to match " + order + " against. This order will be not be entertained.");
            return false;
        } finally {
            lock.unlock();
        }
    }

    boolean cancel(int id) {
        lock.lock();
        try {
            Order order = getOrder(id);
            if (order == null) {
                System.out.println("Order with id " + id + " not found, cancellation failed.");
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
        } finally {
            lock.unlock();
        }
    }

    boolean amend(Amend amend) {
        lock.lock();
        try {
            int id = amend.getId();
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

    private Order getOrder(int id) {
        return lookBook.get(id);
    }

    Map<Double, Book> getOrderBook() {
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
