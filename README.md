# MatchingEngine
This is a single stock matching engine implementing price/time priority matching algorithm that is meant to be part of an exchange simulator used to test Algorithmic Trading orders. It supports the following types of orders
- Limit Orders
- Market Orders

As well as market updates i.e. orders from other participants in the market so that the orders sent to it from our algorithms do not just match against each other thus help in simulating realistic scenarios.

### Classes & Interfaces
 - Order:
 Represents an order object and has properties like id, quantity, price etc.
 - Amend:
 Represents an amend on an order and has properties like (corresponding order) id, amendType, price and quantity.
 - OrderQueue:
 Represents the buy and sell order queue at a certain price level.
 - UnifiedOrderBook:
 Represents the order book. Implements the OrderBook Interface (below) and is based on a hashmap where the key is the price and the value is an OrderQueue.
 - MessageParser:
 Handles parsing of messages sent by clients. Below is the format of messages:
1. New order: "N:1,100,9.5,1". Where "N" is the tag, 1 is the order id, 100 is the quantity, 9.5 is the price and 1 is the order type (1 = Buy and 0 = Sell).
2. Amend: "A:1,0,5.0". Where "A" is the tag, 1 is the order to be amended, 0 is amend type (0 = Price amend and 1 = quantity amend), 5.0 is the field to be amended, in this case price.
3. Cancel: "X:1". Where "X" is the tag, and 1 is the id of the order to be canceled.
4. Market update: "M:99.6,500|99.7,400|99.8,300|99.99,300|100,100|100.1,500|100.2,1000|100.3,2000|100.4,2500|100.5,3000|". Where "M" is the tag, followed by comma separated price and quantity. The first 5 are bid5 to bid1 and the last 5 are ask1 to ask5. Exactly a 5 level depth update is expected, each order is separated by "|".
 - OrderBook Interface:
 The interface to be implemented by the order book, it has the following functions:
```sh
    boolean fillAndInsert(Order order);
    boolean cancel(int id);
    boolean amend(Amend amend);
```
 - MatchingEngine:
This is a server class that receives client requests and gets them processed on a separate thread via ClientHandler managed via an ExecutorService.
 - ClientHandler:
A runnable object that handles requests received.

### Mechanism
1. Market orders are matched at the current fartouch and any residual quantity is put in it's queue at that fartouch.
2. Quantity up amends result in a queue priority loss
3. Amends and cancels on orders that do not exist are not entertained.
4. Order id must be unique positive integers except for orders coming in from market update which are automatically assigned an id of 0 and thus amends and cancels are not handled for them.
5. Actions taken on an order are printed in the console to help understand the code flow and the decisions made.
6. The order book is thread-safe.
7. a lookbook is maintained to check if an order with a certain id exists, used to verify that validity of amends and cancels received as well as the uniqueness of order ids. It does not keep track of orders in market udpates.
8. A priority queue is maintained for bids and one for asks to pull the current fartouch to price market orders.

### Dependencies
- Maven
- junit
- Google Guava
- Apache Commons 
- Java 8