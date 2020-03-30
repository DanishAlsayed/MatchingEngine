import com.google.common.collect.ImmutableSortedSet;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

class MessageParser {
    static final String NEW = "N:";
    static final String AMEND = "A:";
     static final String CANCEL = "X:";
     static final String MARKET = "M:";

    private final char COLON = ':';
    private final char BUY = '1';
    private final char SELL = '0';
    private final char COMMA = ',';
    private final int MESSAGE_INDEX = 2;
    private final int MARKET_BOOK_DEPTH = 5;
    private final SortedSet<String> PREFIXES;

     MessageParser() {
        PREFIXES = ImmutableSortedSet.of(NEW, AMEND, CANCEL, MARKET);
    }

     String orderType(String message) {
        StringBuilder prefix = new StringBuilder();
        prefix.append(message.charAt(0));
        prefix.append(message.charAt(1));
        if (!PREFIXES.contains(prefix.toString())) {
            throw new IllegalArgumentException("No appropriate prefix found");
        }
        return prefix.toString();
    }

     Order newOrder(String message) {
        if (!message.matches("^N:\\d+,\\d+,\\d+(\\.\\d+)?,[0|1]$")) {
            throw new RuntimeException("Ill formed message for new order");
        }
        String[] properties = message.substring(MESSAGE_INDEX).split(",");
        return new Order(Integer.parseInt(properties[0]), Integer.parseInt(properties[1]),
                Double.parseDouble(properties[2]), sideBuy(properties[3]));
    }

     Amend makeAmend(String message) {
        if (!message.matches("^A:\\d+,[0|1],\\d+(\\.\\d+)?$")) {
            throw new RuntimeException("Ill formed message for order amend");
        }
        String[] properties = message.substring(MESSAGE_INDEX).split(String.valueOf(COMMA));
        int amendType = Integer.parseInt(properties[1]);
        if (amendType == Amend.PRICE_AMEND) {
            return new Amend(Integer.parseInt(properties[0]), Double.parseDouble(properties[2]), amendType);
        } else if (amendType == Amend.QUANTITY_AMEND) {
            return new Amend(Integer.parseInt(properties[0]), Integer.parseInt(properties[2]), amendType);
        } else {
            throw new RuntimeException("Unknown amend type.");
        }
    }

     int cancelId(String message) {
        if (!message.matches("^X:\\d+$"))
            throw new RuntimeException("Ill formed message for cancel");
        return Integer.parseInt(message.split(String.valueOf(COLON))[1]);
    }

     List<Order> marketUpdates(String message) {
        if (!message.matches("^M:(\\d+(\\.\\d+)?,\\d+\\|){10}")) {
            throw new RuntimeException("Ill formed message for market update");
        }
        String[] ordersStr = message.substring(MESSAGE_INDEX).split("\\|");
        int updateSize = MARKET_BOOK_DEPTH * 2;
        if (ordersStr.length != updateSize) {
            throw new RuntimeException("Expected market update size " + updateSize + " received " + ordersStr.length);
        }
        int bidIndex = MARKET_BOOK_DEPTH - 1;
        List<Order> orders = new ArrayList<>();
        int i = bidIndex;
        int j = MARKET_BOOK_DEPTH;
        while (i >= 0 && j <= (MARKET_BOOK_DEPTH * 2) - 1) {
            String[] properties = ordersStr[i].split(String.valueOf(COMMA));
            orders.add(new Order(0, Integer.parseInt(properties[1]), Double.parseDouble(properties[0]), true));
            properties = ordersStr[j].split(String.valueOf(COMMA));
            orders.add(new Order(0, Integer.parseInt(properties[1]), Double.parseDouble(properties[0]), false));
            i--;
            j++;
        }
        return orders;
    }

    private boolean sideBuy(String side) {
        if (side.length() != 1) {
            throw new RuntimeException("Side should be a 1 character message");
        }
        if (side.charAt(0) == BUY)
            return true;
        else if (side.charAt(0) == SELL)
            return false;
        else
            throw new RuntimeException("Unsupported order side");
    }

}
