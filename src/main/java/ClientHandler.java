import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private BufferedReader inFromClient;
    private UnifiedOrderBook orderBook;
    private MessageParser messageParser;

    public ClientHandler(Socket client, UnifiedOrderBook book) throws IOException {
        this.inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
        orderBook = book;
        messageParser = new MessageParser();
    }

    @Override
    public void run() {
        try {
            String clientRequest = inFromClient.readLine();
            processRequest(clientRequest);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inFromClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processRequest(String request) {
        String type = messageParser.orderType(request);
        switch (type) {
            case MessageParser.NEW:
                Order order = messageParser.newOrder(request);
                orderBook.fillAndInsert(order);
                return;
            case MessageParser.AMEND:
                Amend amend = messageParser.makeAmend(request);
                orderBook.amend(amend);
                return;
            case MessageParser.CANCEL:
                orderBook.cancel(messageParser.cancelId(request));
                return;
            case MessageParser.MARKET:
                List<Order> marketUpdate = messageParser.marketUpdates(request);
                for (Order o : marketUpdate) {
                    orderBook.fillAndInsert(o);
                }
                return;
        }
    }
}
