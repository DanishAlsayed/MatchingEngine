import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class UnifiedOrderBookTest {
    UnifiedOrderBook orderBook;

    @Before
    public void setup() {
        orderBook = new UnifiedOrderBook();
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
        assertEquals(orderBook.getOrderBook().get(10.0).bookSize(true), 1);
        assertEquals(orderBook.getOrderBook().get(10.1).bookSize(false), 1);
        Order existing = new Order(2, 100, 10.1, false);
        assertFalse(orderBook.fillAndInsert(existing));
        assertEquals(orderBook.getOrderBook().get(10.0).bookSize(true), 1);
        assertEquals(orderBook.getOrderBook().get(10.1).bookSize(false), 1);
        Order order3 = new Order(3, 100, 10.1, true);
        assertTrue(orderBook.fillAndInsert(order3));
        assertEquals(orderBook.getOrderBook().get(10.0).bookSize(true), 1);
        assertEquals(orderBook.getOrderBook().get(10.0).bookSize(false), 0);
        Order order4 = new Order(4, 100, 10.1, false);
        assertTrue(orderBook.fillAndInsert(order4));
        assertEquals(orderBook.getOrderBook().get(10.0).bookSize(true), 1);
        assertEquals(orderBook.getOrderBook().get(10.1).bookSize(false), 1);
        assertTrue(orderBook.cancel(4));
        assertEquals(orderBook.getOrderBook().get(10.0).bookSize(true), 1);
        assertEquals(orderBook.getOrderBook().get(10.1).bookSize(false), 0);

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
        printOrderBook();
        assertEquals(orderBook.getOrderBook().get(10.1).bookSize(false), 3);
        assertNull(orderBook.getOrderBook().get(10.0));
        assertTrue(orderBook.fillAndInsert(order4));
        assertEquals(orderBook.getOrderBook().get(10.1).bookSize(false), 0);
        assertEquals(orderBook.getOrderBook().get(10.1).bookSize(true), 1);
    }

    @Test
    public void amendOrders() {
        Order order1 = new Order(1, 100, 10.1, false);
        Order order2 = new Order(2, 100, 10.0, true);
        Order order3 = new Order(3, 100, 10.0, true);
        assertTrue(orderBook.fillAndInsert(order1));
        assertTrue(orderBook.fillAndInsert(order2));
        assertTrue(orderBook.fillAndInsert(order3));
        printOrderBook();
        Amend amend = new Amend(order1.getId(),9.9,Amend.PRICE_AMEND);
        assertTrue(orderBook.amend(amend));
        amend = new Amend(order3.getId(),90,Amend.QUANTITY_AMEND);
        assertTrue(orderBook.amend(amend));
        amend = new Amend(order2.getId(),110,Amend.QUANTITY_AMEND);
        assertTrue(orderBook.amend(amend));
    }

    private void printOrderBook() {
        System.out.println("---ORDER BOOK---");
        Iterator it = orderBook.getOrderBook().entrySet().iterator();
        System.out.println("Buy Book:");
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey());
            Book queue = (Book) pair.getValue();
            queue.printBook(true);
        }

        System.out.println("\nSell Book:");
        it = orderBook.getOrderBook().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey());
            Book queue = (Book) pair.getValue();
            queue.printBook(false);
        }

        System.out.println("\nLook Book:");
        it = orderBook.getLookBook().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }

}
