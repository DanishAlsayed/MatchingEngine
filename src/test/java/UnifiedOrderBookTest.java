import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
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
        TestUtil.assertOrderBookSize(orderBook,10.0, 1, 0);
        TestUtil.assertOrderBookSize(orderBook,10.1, 0, 1);
        TestUtil.assertOtherSizes(orderBook,1, 1, 2);
        Order existing = new Order(2, 100, 10.1, false);
        assertFalse(orderBook.fillAndInsert(existing));
        TestUtil.assertOrderBookSize(orderBook,10.0, 1, 0);
        TestUtil.assertOrderBookSize(orderBook,10.1, 0, 1);
        TestUtil.assertOtherSizes(orderBook,1, 1, 2);
        Order order3 = new Order(3, 100, 10.1, true);
        assertTrue(orderBook.fillAndInsert(order3));
        TestUtil.assertOrderBookSize(orderBook,10.0, 1, 0);
        TestUtil.assertOrderBookSize(orderBook,10.1, 0, 0);
        TestUtil.assertOtherSizes(orderBook,0, 1, 1);
        Order order4 = new Order(4, 100, 10.1, false);
        assertTrue(orderBook.fillAndInsert(order4));
        TestUtil.assertOrderBookSize(orderBook,10.0, 1, 0);
        TestUtil.assertOrderBookSize(orderBook,10.1, 0, 1);
        TestUtil.assertOtherSizes(orderBook,1, 1, 2);
        assertTrue(orderBook.cancel(4));
        TestUtil.assertOrderBookSize(orderBook,10.0, 1, 0);
        TestUtil.assertOrderBookSize(orderBook,10.1, 0, 0);
        TestUtil.assertOtherSizes(orderBook,0, 1, 1);

    }

    @Test
    public void multipleMatches() {
        Order order1 = new Order(1, 100, 10.1, false);
        Order order2 = new Order(2, 100, 10.1, false);
        Order order3 = new Order(3, 50, 10.1, false);
        assertTrue(orderBook.fillAndInsert(order1));
        assertTrue(orderBook.fillAndInsert(order2));
        assertTrue(orderBook.fillAndInsert(order3));
        TestUtil.assertOrderBookSize(orderBook,10.1, 0, 3);
        TestUtil.assertOtherSizes(orderBook,3, 0, 3);
        Order order4 = new Order(4, 275, 10.1, true);
        assertTrue(orderBook.fillAndInsert(order4));
        TestUtil.assertOrderBookSize(orderBook,10.1, 1, 0);
        assertNull(orderBook.getOrderBook().get(10.0)); //as there was never an order at 10.0
        TestUtil.assertOtherSizes(orderBook,0, 1, 1);
    }

    @Test
    public void amendOrders() {
        Order order1 = new Order(1, 100, 10.1, false);
        Order order2 = new Order(2, 100, 10.0, true);
        Order order3 = new Order(3, 50, 10.0, true);
        assertTrue(orderBook.fillAndInsert(order1));
        assertTrue(orderBook.fillAndInsert(order2));
        assertTrue(orderBook.fillAndInsert(order3));
        TestUtil.assertOrderBookSize(orderBook,10.0, 2, 0);
        TestUtil.assertOrderBookSize(orderBook,10.1, 0, 1);
        TestUtil.assertOtherSizes(orderBook,1, 2, 3);
        Amend amend = new Amend(order1.getId(), 9.9, Amend.PRICE_AMEND);
        assertTrue(orderBook.amend(amend));
        TestUtil.assertOrderBookSize(orderBook,10.0, 2, 0);
        TestUtil.assertOrderBookSize(orderBook,9.9, 0, 1);
        TestUtil.assertOrderBookSize(orderBook,10.1, 0, 0);
        TestUtil.assertOtherSizes(orderBook,1, 2, 3);
        TestCase.assertEquals(9.9, orderBook.getMinPrices().peek());
        amend = new Amend(order3.getId(), 40, Amend.QUANTITY_AMEND);
        assertTrue(orderBook.amend(amend));
        //Quantity amends do not change any sizes
        TestUtil.assertOrderBookSize(orderBook,10.0, 2, 0);
        TestUtil.assertOrderBookSize(orderBook,9.9, 0, 1);
        TestUtil.assertOtherSizes(orderBook,1, 2, 3);
        amend = new Amend(order2.getId(), 110, Amend.QUANTITY_AMEND);
        assertTrue(orderBook.amend(amend));
        TestUtil.assertOrderBookSize(orderBook,10.0, 2, 0);
        TestUtil.assertOrderBookSize(orderBook,9.9, 0, 1);
        TestUtil.assertOrderBookSize(orderBook,10.1, 0, 0);
        TestUtil.assertOtherSizes(orderBook,1, 2, 3);
    }

    @Test
    public void marketOrder() {
        Order mktOrder = new Order(9, 380, 0.0, false);
        assertFalse(orderBook.fillAndInsert(mktOrder));
        //Market order not entertained as there is no opposite (buy) limit order to pick price from
        assertNull(orderBook.getOrderBook().get(0.0));
        TestUtil.assertOtherSizes(orderBook,0, 0, 0);
        Order order1 = new Order(1, 100, 10.1, false);
        assertTrue(orderBook.fillAndInsert(order1));
        //Market order not entertained as there is no opposite (buy) limit order to pick price from
        assertFalse(orderBook.fillAndInsert(mktOrder));
        TestUtil.assertOrderBookSize(orderBook,10.1, 0, 1);
        TestUtil.assertOtherSizes(orderBook,1, 0, 1);
        Order order2 = new Order(2, 100, 10.2, true);
        Order order3 = new Order(3, 100, 10.3, true);
        Order order4 = new Order(4, 250, 10.4, true);
        Order order5 = new Order(5, 100, 10.5, false);
        Order order6 = new Order(6, 100, 10.6, true);
        Order order7 = new Order(7, 100, 10.7, true);
        Order order8 = new Order(8, 275, 10.7, true);
        assertTrue(orderBook.fillAndInsert(order2));
        assertTrue(orderBook.fillAndInsert(order3));
        assertTrue(orderBook.fillAndInsert(order4));
        assertTrue(orderBook.fillAndInsert(order5));
        assertTrue(orderBook.fillAndInsert(order6));
        assertTrue(orderBook.fillAndInsert(order7));
        assertTrue(orderBook.fillAndInsert(order8));
        TestUtil.assertOrderBookSize(orderBook,10.7, 2, 0);
        TestUtil.assertOtherSizes(orderBook,2, 6, 8);
        assertTrue(orderBook.fillAndInsert(mktOrder));
        TestUtil.assertOrderBookSize(orderBook,10.7, 0, 1);
        TestUtil.assertOtherSizes(orderBook,3, 4, 7);
    }

    @Test
    public void marketUpdate() {
        Order m1 = new Order(0, 100, 9.9, true);
        Order order1 = new Order(1, 100, 10.1, false);
        Order order2 = new Order(2, 100, 10.0, true);
        Order order3 = new Order(3, 50, 10.0, true);
        assertTrue(orderBook.fillAndInsert(m1));
        assertTrue(orderBook.fillAndInsert(order1));
        assertTrue(orderBook.fillAndInsert(order2));
        assertTrue(orderBook.fillAndInsert(order3));
        TestUtil.assertOrderBookSize(orderBook,10.0, 2, 0);
        TestUtil.assertOrderBookSize(orderBook,10.1, 0, 1);
        TestUtil.assertOrderBookSize(orderBook,9.9, 1, 0);
        //lookbook != minPricesSize + maxPricesSize when market updates are inserted
        TestUtil.assertOtherSizes(orderBook,1, 3, 3);
        Order m2 = new Order(0, 150, 10.0, false);
        assertTrue(orderBook.fillAndInsert(m2));
        TestUtil.assertOrderBookSize(orderBook,10.0, 0, 0);
        TestUtil.assertOrderBookSize(orderBook,10.1, 0, 1);
        TestUtil.assertOrderBookSize(orderBook,9.9, 1, 0);
        TestUtil.assertOtherSizes(orderBook,1, 1, 1);
        TestCase.assertEquals(9.9, orderBook.getMaxPrices().peek());
        TestCase.assertEquals(10.1, orderBook.getMinPrices().peek());
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
        TestUtil.assertOtherSizes(orderBook,2, 6, 8);
    }
}
