import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

class Book {
    private ConcurrentLinkedQueue<Order> buyQueue;
    private ConcurrentLinkedQueue<Order> sellQueue;

    Book() {
        buyQueue = new ConcurrentLinkedQueue<>();
        sellQueue = new ConcurrentLinkedQueue<>();
    }

    int bookSize(boolean sideBuy) {
        return sideBuy ? buyQueue.size() : sellQueue.size();
    }

    void printBook(boolean sideBuy) {
        Iterator<Order> it = getSideQueue(sideBuy).iterator();
        while (it.hasNext()) {
            Order order = it.next();
            System.out.println(order);
        }
    }

    void fillAndInsert(Order order, Map<Integer, Order> lookBook, PriorityQueue<Double> maxPrices, PriorityQueue<Double> minPrices) {
        boolean result;
        boolean sideBuy = order.isSideBuy();
        result = fill(order, lookBook, sideBuy ? minPrices : maxPrices);
        if (!result && !order.isMktOrder()) {
            insert(order, lookBook);
            if (sideBuy)
                maxPrices.add(order.getPrice());
            else
                minPrices.add(order.getPrice());
        }
    }

    boolean amendQuantity(Amend amend, Order order, Map<Integer, Order> lookBook) {

        Iterator<Order> it = getSideQueue(order.isSideBuy()).iterator();
        while (it.hasNext()) {
            Order restingOrder = it.next();
            if (restingOrder.getId() == amend.getId()) {
                if (amend.getQuantity() < restingOrder.getQuantity()) {
                    restingOrder.setQuantity(amend.getQuantity());
                    System.out.println(restingOrder + " quantity-down amended");
                    lookBook.replace(restingOrder.getId(), restingOrder);
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

        return true;
    }

    void remove(Order order, Map<Integer, Order> lookBook, PriorityQueue<Double> priorityQueue) {
        Iterator<Order> it = getSideQueue(order.isSideBuy()).iterator();
        int id = order.getId();
        while (it.hasNext()) {
            Order restingOrder = it.next();
            double price = restingOrder.getPrice();
            if (restingOrder.getId() == id) {
                System.out.println(restingOrder + " cancelled");
                it.remove();
                lookBook.remove(id);
                priorityQueue.remove(price);
                break;
            }
        }
    }

    private boolean fill(Order order, Map<Integer, Order> lookBook, PriorityQueue<Double> priorityQueue) {
        boolean result = false;
        boolean partialFill = false;
        Iterator<Order> it = getSideQueue(!order.isSideBuy()).iterator();
        while (it.hasNext()) {
            Order restingOrder = it.next();
            int orderQty = order.getQuantity();
            int restingQty = restingOrder.getQuantity();
            int diff = orderQty - restingQty;
            if (diff < 0) {
                order.setQuantity(0);
                int newQty = diff * -1;
                restingOrder.setQuantity(newQty);
                lookBook.get(restingOrder.getId()).setQuantity(newQty);
                result = true;
                System.out.println("Order: " + order + " fully filled");
                System.out.println("restingOrder: " + restingOrder + " partially filled, quantity left:" + restingOrder.getQuantity());
                break;
            } else if (diff > 0) {
                order.setQuantity(diff);
                System.out.println("restingOrder: " + restingOrder + " fully filled");
                System.out.println("order: " + order + " partially filled, quantity left:" + order.getQuantity());
                priorityQueue.remove(restingOrder.getPrice());
                it.remove();
                lookBook.remove(restingOrder.getId());
                partialFill = true;
            } else {
                order.setQuantity(0);
                System.out.println("orders: " + restingOrder + " and " + order + " fully filled");
                priorityQueue.remove(restingOrder.getPrice());
                it.remove();
                lookBook.remove(restingOrder.getId());
                result = true;
                break;
            }
        }

        if (!result && !partialFill) {
            System.out.println("Order " + order + " got no fills");
        }

        return result;
    }

    private void insert(Order order, Map<Integer, Order> lookBook) {
        getSideQueue(order.isSideBuy()).add(order);
        lookBook.put(order.getId(), order);
        System.out.println(order + " inserted in the book");
    }

    private ConcurrentLinkedQueue<Order> getSideQueue(boolean sideBuy) {
        return sideBuy ? buyQueue : sellQueue;

    }
}
