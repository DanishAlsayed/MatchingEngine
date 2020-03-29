import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class OrderBookTest {
    private OrderBook orderBook;

    @Before
    public void setup() {
        orderBook = new OrderBook();
    }

    @After
    public void print() {
        printOrderBook();
        orderBook.clear();
    }

    @Test
    public void FillAndInsert() {
        Order order1 = new Order(1, 100, 10.0, true);
        Order order2 = new Order(2, 100, 10.1, false);
        assertTrue(orderBook.fillAndInsert(order1));
        assertTrue(orderBook.fillAndInsert(order2));
        assertEquals(orderBook.getBuyBook().get(10.0).size(),1);
        assertEquals(orderBook.getSellBook().get(10.1).size(),1);
        Order existing = new Order(2, 100, 10.1, false);
        assertFalse(orderBook.fillAndInsert(existing));
        assertEquals(orderBook.getBuyBook().get(10.0).size(),1);
        assertEquals(orderBook.getSellBook().get(10.1).size(),1);
        Order order3 = new Order(3, 100, 10.1, true);
        assertTrue(orderBook.fillAndInsert(order3));
        assertEquals(orderBook.getBuyBook().get(10.0).size(),1);
        assertEquals(orderBook.getSellBook().get(10.1).size(),0);
        Order order4 = new Order(4, 100, 10.1, false);
        assertTrue(orderBook.fillAndInsert(order4));
        assertEquals(orderBook.getBuyBook().get(10.0).size(),1);
        assertEquals(orderBook.getSellBook().get(10.1).size(),1);
        assertTrue(orderBook.remove(4));
        assertEquals(orderBook.getBuyBook().get(10.0).size(),1);
        assertEquals(orderBook.getSellBook().get(10.1).size(),0);

    }

    @Test
    public void multipleMatches() {
        Order order1 = new Order(1, 100, 10.1, false);
        Order order2 = new Order(2, 100, 10.1, false);
        Order order3 = new Order(3, 50, 10.1, false);
        Order order4 = new Order(4, 275, 10.1, true);
        assertTrue(orderBook.fillAndInsert(order1));
        assertTrue(orderBook.fillAndInsert(order2));
        assertTrue(orderBook.fillAndInsert(order3));
        assertEquals(orderBook.getSellBook().get(10.1).size(),3);
        assertNull(orderBook.getBuyBook().get(10.1));
        assertTrue(orderBook.fillAndInsert(order4));
        assertEquals(orderBook.getSellBook().get(10.1).size(),0);
        assertEquals(orderBook.getBuyBook().get(10.1).size(),1);
    }

    private void printOrderBook() {
        System.out.println("---ORDER BOOK---");
        Iterator it = orderBook.getBuyBook().entrySet().iterator();
        Iterator queueIt;
        System.out.println("Buy Book:");
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey());
            ConcurrentLinkedQueue<Order> queue = (ConcurrentLinkedQueue<Order>) pair.getValue();
            queueIt = queue.iterator();
            while (queueIt.hasNext()) {
                Order order = (Order) queueIt.next();
                System.out.println(order);
            }
        }

        System.out.println("\nSell Book:");
        it = orderBook.getSellBook().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey());
            ConcurrentLinkedQueue<Order> queue = (ConcurrentLinkedQueue<Order>) pair.getValue();
            queueIt = queue.iterator();
            while (queueIt.hasNext()) {
                Order order = (Order) queueIt.next();
                System.out.println(order);
            }
        }

        System.out.println("\nLook Book:");
        it = orderBook.getLookBook().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }
}
