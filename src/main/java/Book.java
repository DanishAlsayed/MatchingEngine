import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class Book {
    private ConcurrentLinkedQueue<Order> buyQueue;
    private ConcurrentLinkedQueue<Order> sellQueue;
    private ReentrantLock lock;

    public Book() {
        buyQueue = new ConcurrentLinkedQueue<Order>();
        sellQueue = new ConcurrentLinkedQueue<Order>();
        //Fairness policy
        lock = new ReentrantLock(true);
    }

    public int bookSize(boolean sideBuy) {
        return sideBuy ? buyQueue.size() : sellQueue.size();
    }

    public void printBook(boolean sideBuy) {
        Iterator<Order> it = getSideQueue(sideBuy).iterator();
        while (it.hasNext()) {
            Order order = it.next();
            System.out.println(order);
        }
    }

    public boolean fillAndInsert(Order order, Map<Integer, Order> lookBook) {
        lock.lock();
        boolean result;
        try {
            result = fill(order, lookBook);
            if (!result) {
                insert(order, lookBook);
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    public boolean amendQuantity(Amend amend, Order order, Map<Integer, Order> lookBook) {
        lock.lock();
        try {
            Iterator<Order> it = getSideQueue(order.isSideBuy()).iterator();
            while (it.hasNext()) {
                Order restingOrder = it.next();
                if (restingOrder.getId() == amend.getId()) {
                    if (amend.getQuantity() < restingOrder.getQuantity()) {
                        restingOrder.setQuantity(amend.getQuantity());
                        //TODO: how is lookbook getting updated implicitly here?
                    } else {
                        Order newOrder = new Order(order.getId(), amend.getQuantity(), order.getPrice(), order.isSideBuy());
                        it.remove();
                        lookBook.remove(amend.getId());
                        //Queue priority is lost with a quantity-up amend
                        insert(newOrder, lookBook);
                    }
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    public boolean remove(Order order, Map<Integer, Order> lookBook) {
        lock.lock();
        boolean result = false;
        try {
            Iterator<Order> it = getSideQueue(order.isSideBuy()).iterator();
            int id = order.getId();
            while (it.hasNext()) {
                Order restingOrder = it.next();
                if (restingOrder.getId() == id) {
                    it.remove();
                    lookBook.remove(id);
                    result = true;
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    public void clear() {
        lock.lock();
        try {
            buyQueue.clear();
            sellQueue.clear();
        } finally {
            lock.unlock();
        }
    }

    private boolean fill(Order order, Map<Integer, Order> lookBook) {
        boolean result = false;
        Iterator<Order> it = getSideQueue(!order.isSideBuy()).iterator();
        while (it.hasNext()) {
            Order restingOrder = it.next();
            int orderQty = order.getQuantity();
            int restingQty = restingOrder.getQuantity();
            int diff = orderQty - restingQty;
            if (diff < 0) {
                order.setQuantity(0);
                int newQty = restingQty + diff;
                restingOrder.setQuantity(newQty);
                lookBook.get(restingOrder.getId()).setQuantity(newQty);
                result = true;
                break;
            } else if (diff > 0) {
                order.setQuantity(diff);
                it.remove();
                lookBook.remove(restingOrder.getId());
            } else {
                order.setQuantity(0);
                it.remove();
                lookBook.remove(restingOrder.getId());
                result = true;
                break;
            }
        }

        return result;
    }

    private boolean insert(Order order, Map<Integer, Order> lookBook) {
        getSideQueue(order.isSideBuy()).add(order);
        lookBook.put(order.getId(), order);
        return true;
    }

    private ConcurrentLinkedQueue<Order> getSideQueue(boolean sideBuy) {
        return sideBuy ? buyQueue : sellQueue;

    }
}
