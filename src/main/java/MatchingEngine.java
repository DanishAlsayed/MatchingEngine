import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class MatchingEngine {
    static final int PORT = 22000;
    private UnifiedOrderBook orderBook;
    private List<ClientHandler> clientHandlers;
    private ExecutorService pool;
    private volatile boolean exit = false;

    MatchingEngine() {
        this.orderBook = new UnifiedOrderBook();
        clientHandlers = new ArrayList<>();
        pool = Executors.newCachedThreadPool();
    }

    void startServer() throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("MatchingEngine server started");
        while (!exit) {
            Socket connectionSocket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(connectionSocket, orderBook);
            clientHandlers.add(clientHandler);
            pool.execute(clientHandler);
        }
    }

    void stopServer() {
        exit = true;
    }
}
