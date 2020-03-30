import java.util.Iterator;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;

class TestUtil {

    static void assertOrderBookSize(UnifiedOrderBook orderBook, double price, int buyBook, int sellBook) {
        requireNonNull(orderBook.getOrderBook());
        assertEquals(buyBook, orderBook.getOrderBook().get(price).bookSize(true));
        assertEquals(sellBook, orderBook.getOrderBook().get(price).bookSize(false));
    }

    static void assertOtherSizes(UnifiedOrderBook orderBook, int minPrices, int maxPrices, int lookBook) {
        assertEquals(minPrices, orderBook.getMinPrices().size());
        assertEquals(maxPrices, orderBook.getMaxPrices().size());
        assertEquals(lookBook, orderBook.getLookBook().size());
    }

    static void printOrderBook(UnifiedOrderBook orderBook) {
        System.out.println("---ORDER BOOK---");
        Iterator it = orderBook.getOrderBook().entrySet().iterator();
        System.out.println("Buy Book:");
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey());
            OrderQueue queue = (OrderQueue) pair.getValue();
            queue.printBook(true);
        }

        System.out.println("\nSell Book:");
        it = orderBook.getOrderBook().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey());
            OrderQueue queue = (OrderQueue) pair.getValue();
            queue.printBook(false);
        }

        System.out.println("\nLook Book:");
        it = orderBook.getLookBook().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }

        int size = orderBook.getMinPrices().size();
        System.out.println("\nMinHeap: size: " + size);
        for (int i = 0; i < size; i++) {
            System.out.print(orderBook.getMinPrices().poll() + " ");
        }

        size = orderBook.getMaxPrices().size();
        System.out.println("\nMaxHeap: size: " + size);
        for (int i = 0; i < size; i++) {
            System.out.print(orderBook.getMaxPrices().poll() + " ");
        }

    }
}
