import org.junit.Test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class MatchingEngineTest implements Runnable {
    private MatchingEngine matchingEngine;

    public MatchingEngineTest() {
        matchingEngine = new MatchingEngine();
    }

    static class TestClient implements Runnable {
        private String input;
        Socket clientSocket;
        DataOutputStream outToServer;

        TestClient(String input) throws IOException {
            this.input = input;
            clientSocket = new Socket("localhost", MatchingEngine.PORT);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
        }

        void send() throws Exception {
            outToServer.writeBytes(input + '\n');
        }

        @Override
        public void run() {
            try {
                send();
                sleep(50);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Test
    public void newOrders() throws Exception {
        Thread serverThread = new Thread(new MatchingEngineTest());
        serverThread.start();
        List<String> inputs = new ArrayList<>();
        inputs.add("N:1,100,9.5,1");
        inputs.add("N:2,100,9.7,0");
        inputs.add("N:3,100,99.7,0");
        inputs.add("M:99.6,500|99.7,400|99.8,300|99.9,300|100,100|100.1,500|100.2,1000|100.3,2000|100.4,2500|100.5,3000|");
        inputs.add("A:1,1,90");
        inputs.add("A:1,1,110");
        inputs.add("A:1,0,100.5");
        inputs.add("X:2");
        inputs.add("N:4,100,0,1");
        for (String input : inputs) {
            Thread clientThread = new Thread(new TestClient(input));
            clientThread.start();
            clientThread.join();
        }
        matchingEngine.stopServer();
    }

    @Override
    public void run() {
        try {
            matchingEngine.startServer();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
