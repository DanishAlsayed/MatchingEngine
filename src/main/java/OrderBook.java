import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

 class OrderBook {
    private final Map<Double, ConcurrentLinkedQueue<Order>> buyBook;
    private final Map<Double, ConcurrentLinkedQueue<Order>> sellBook;
    private final Map<Integer, Order> lookBook;

     Map<Double, ConcurrentLinkedQueue<Order>> getBuyBook() {
        return buyBook;
    }

     Map<Double, ConcurrentLinkedQueue<Order>> getSellBook() {
        return sellBook;
    }

     Map<Integer, Order> getLookBook() {
        return lookBook;
    }

     OrderBook() {
        this.buyBook = new ConcurrentHashMap<>();
        this.sellBook = new ConcurrentHashMap<>();
        this.lookBook = new ConcurrentHashMap<>();
    }

     boolean fillAndInsert(Order order) {
        if (getOrder(order.getId()) != null) {
            return false;
        }

        ConcurrentLinkedQueue<Order> queue;
        double price = order.getPrice();
        queue = order.isSideBuy() ? sellBook.get(price) : buyBook.get(price);
        if (queue != null) {
            if (fill(order, queue)) {
                return true;
            }
        }
        return insert(order);
    }

     boolean remove(int id) {
        Order order = getOrder(id);
        if (order == null) {
            return false;
        }

        ConcurrentLinkedQueue<Order> queue;
        double price = order.getPrice();
        queue = order.isSideBuy() ? buyBook.get(price) : sellBook.get(price);
        if (queue != null) {
            Iterator<Order> it = queue.iterator();
            while (it.hasNext()) {
                Order restingOrder = it.next();
                if (restingOrder.getId() == id) {
                    it.remove();
                    lookBook.remove(id);
                    return true;
                }
            }
        }
        return false;
    }

     void clear() {
        buyBook.clear();
        sellBook.clear();
        lookBook.clear();
    }

    private boolean insert(Order order) {
        ConcurrentLinkedQueue<Order> queue;
        double price = order.getPrice();
        queue = order.isSideBuy() ? buyBook.get(price) : sellBook.get(price);
        if (queue != null) {
            queue.add(order);
        } else {
            queue = new ConcurrentLinkedQueue<>();
            queue.add(order);
            if (order.isSideBuy()) {
                buyBook.put(order.getPrice(), queue);
            } else {
                sellBook.put(order.getPrice(), queue);
            }
        }
        lookBook.put(order.getId(), order);
        return true;
    }

    private boolean fill(Order order, ConcurrentLinkedQueue<Order> queue) {
        Iterator<Order> it = queue.iterator();
        while (it.hasNext()) {
            Order restingOrder = it.next();

            int orderQty = order.getQuantity();
            int restingQty = restingOrder.getQuantity();
            int diff = orderQty - restingQty;
            if (diff < 0) {
                order.setQuantity(0);
                restingOrder.setQuantity(restingQty + diff);
                return true;
            } else if (diff > 0) {
                order.setQuantity(diff);
                it.remove();
                lookBook.remove(restingOrder.getId());
            } else {
                order.setQuantity(0);
                it.remove();
                lookBook.remove(restingOrder.getId());
                return true;
            }
        }
        return false;
    }

    private Order getOrder(int id) {
        return lookBook.get(id);
    }
}
