import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class UnifiedOrderBookTest {
    private UnifiedOrderBook orderBook;

    @Before
    public void setup() {
        orderBook = new UnifiedOrderBook();
    }

    @After
    public void print() {
        TestUtil.printOrderBook(orderBook);
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
        TestUtil.printOrderBook(orderBook);
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
        TestUtil.printOrderBook(orderBook);
        Amend amend = new Amend(order1.getId(), 9.9, Amend.PRICE_AMEND);
        assertTrue(orderBook.amend(amend));
        amend = new Amend(order3.getId(), 90, Amend.QUANTITY_AMEND);
        assertTrue(orderBook.amend(amend));
        amend = new Amend(order2.getId(), 110, Amend.QUANTITY_AMEND);
        assertTrue(orderBook.amend(amend));
    }

    @Test
    public void marketOrder() {
        Order mktOrder = new Order(9,125,0,false);
        assertFalse(orderBook.fillAndInsert(mktOrder));
        Order order1 = new Order(1, 100, 10.1, false);
        assertFalse(orderBook.fillAndInsert(mktOrder));
        Order order2 = new Order(2, 100, 10.2, true);
        Order order3 = new Order(3, 100, 10.3, true);
        Order order4 = new Order(4, 250, 10.4, true);
        Order order5 = new Order(5, 100, 10.5, false);
        Order order6 = new Order(6, 100, 10.6, true);
        Order order7 = new Order(7, 100, 10.7, true);
        Order order8 = new Order(8, 275, 10.7, true);
        assertTrue(orderBook.fillAndInsert(order1));
        assertTrue(orderBook.fillAndInsert(order2));
        assertTrue(orderBook.fillAndInsert(order3));
        assertTrue(orderBook.fillAndInsert(order4));
        assertTrue(orderBook.fillAndInsert(order5));
        assertTrue(orderBook.fillAndInsert(order6));
        assertTrue(orderBook.fillAndInsert(order7));
        assertTrue(orderBook.fillAndInsert(order8));
        assertTrue(orderBook.fillAndInsert(mktOrder));
    }

    @Test
    public void priceHeapsInsertions() {
        Order order1 = new Order(1, 100, 10.1, false);
        Order order2 = new Order(2, 100, 10.2, true);
        Order order3 = new Order(3, 100, 10.3, true);
        Order order4 = new Order(4, 275, 10.4, true);
        Order order5 = new Order(5, 100, 10.5, false);
        Order order6 = new Order(6, 100, 10.6, true);
        Order order7 = new Order(7, 100, 10.7, true);
        Order order8 = new Order(8, 275, 10.7, true);
        assertTrue(orderBook.fillAndInsert(order1));
        assertTrue(orderBook.fillAndInsert(order2));
        assertTrue(orderBook.fillAndInsert(order3));
        assertTrue(orderBook.fillAndInsert(order4));
        assertTrue(orderBook.fillAndInsert(order5));
        assertTrue(orderBook.fillAndInsert(order6));
        assertTrue(orderBook.fillAndInsert(order7));
        assertTrue(orderBook.fillAndInsert(order8));
    }

}
