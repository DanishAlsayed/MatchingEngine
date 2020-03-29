import java.util.Iterator;
import java.util.Map;

class TestUtil {

    static void printOrderBook(UnifiedOrderBook orderBook) {
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
