import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MessageParserTest {
    private MessageParser messageParser;

    @Before
    public void setup() {
        messageParser = new MessageParser();
    }

    @Test
    public void orderType() {
        assertEquals(messageParser.orderType("N:"), MessageParser.NEW);
        assertEquals(messageParser.orderType("A:"), MessageParser.AMEND);
        assertEquals(messageParser.orderType("X:"), MessageParser.CANCEL);
        assertEquals(messageParser.orderType("M:"), MessageParser.MARKET);
        try {
            messageParser.orderType("W:");
            fail();
        } catch (Exception e) {
            assertEquals("No appropriate prefix found", e.getMessage());
        }
    }

    @Test
    public void newOrder() {
        Order order = messageParser.newOrder("N:1,100,9.5,1");
        assertEquals(1, order.getId());
        assertEquals(100, order.getQuantity());
        TestCase.assertEquals(9.5, order.getPrice());
        assertTrue(order.isSideBuy());
        order = messageParser.newOrder("N:12,100,9,0");
        assertEquals(12, order.getId());
        assertEquals(100, order.getQuantity());
        TestCase.assertEquals(9.0, order.getPrice());
        assertFalse(order.isSideBuy());

        newOrderNegative("N:x,100,9.5,1");
        newOrderNegative("N:1, 100,9.5,1");
        newOrderNegative("N:1.1,100,9.5,1");
        newOrderNegative("N:1,100,9.5,3");
        newOrderNegative("N:1,10.0,9.5,0");
        newOrderNegative("N:1,10.0,w,0");
        newOrderNegative("A:1,100,9.5,1");
        newOrderNegative(" N:12,100,9,0");
        newOrderNegative("N:12,100,9,0 ");
    }

    private void newOrderNegative(String message) {
        try {
            messageParser.newOrder(message);
            fail();
        } catch (Exception e) {
            assertEquals("Ill formed message for new order", e.getMessage());
        }
    }

    @Test
    public void amend() {
        Amend amend = messageParser.makeAmend("A:20,1,50");
        assertEquals(amend.getQuantity(), 50);
        assertEquals(amend.getId(), 20);
        assertEquals(amend.getAmendType(), Amend.QUANTITY_AMEND);
        TestCase.assertEquals(amend.getPrice(), -1.0);
        amend = messageParser.makeAmend("A:2,0,5.0");
        assertEquals(amend.getQuantity(), -1);
        assertEquals(amend.getId(), 2);
        assertEquals(amend.getAmendType(), Amend.PRICE_AMEND);
        TestCase.assertEquals(amend.getPrice(), 5.0);

        amendNegative("N:2,0,5.0");
        amendNegative("A:2.1,0,5.0");
        amendNegative("A:2,2,5.0");
        amendNegative("A:2,0,5. 0");
        amendNegative("A:,0,5.0");
        amendNegative(" A:20,1,50");
        amendNegative("A:20,1,50 ");
    }

    private void amendNegative(String message) {
        try {
            messageParser.makeAmend(message);
            fail();
        } catch (Exception e) {
            assertEquals("Ill formed message for order amend", e.getMessage());
        }
    }

    @Test
    public void cancel() {
        assertEquals(1, messageParser.cancelId("X:1"));
        assertEquals(15, messageParser.cancelId("X:15"));

        cancelNegative("X:-15");
        cancelNegative("x:15");
        cancelNegative("X:5.0");
        cancelNegative("X: 15");
        cancelNegative(" X:15");
        cancelNegative("X:15 ");
    }

    private void cancelNegative(String message) {
        try {
            messageParser.cancelId(message);
            fail();
        } catch (Exception e) {
            assertEquals("Ill formed message for cancel", e.getMessage());
        }
    }

    @Test
    public void marketUpdate() {
        List<Order> orders = messageParser.marketUpdates("M:99.6,500|99.7,400|99.8,300|99.99,300|100,100|100.1,500|100.2,1000|100.3,2000|100.4,2500|100.5,3000|");
        int buys = 0;
        int sells = 0;
        for (Order o : orders) {
            assertEquals(0, o.getId());
            if (o.isSideBuy()) {
                buys++;
            } else {
                sells++;
            }
            System.out.println(o);
        }
        assertEquals(buys, sells);

        marketUpdateNegative("M:99.6,500|99.7,400|99.8,300|99.99,300|100,100|100.1,500|100.2,1000|100.3,2000|100.4,2500|100.5,3000");
        marketUpdateNegative("M:99.6,500|99.7,400|99.8,300|99.99,300|100,100|100.1,500|100.2,1000|100.3,2000|100.4,2500100.5,3000|");
        marketUpdateNegative("M:99.6,500|99.7,400|99.8,300|99.99,300|100,100|100.1,500|100.2,1000|100.3,2000|100.4,2500|100.5,3000.0|");

    }

    private void marketUpdateNegative(String message) {
        try {
            messageParser.marketUpdates(message);
            fail();
        } catch (Exception e) {
            assertEquals("Ill formed message for market update", e.getMessage());
        }
    }
}
