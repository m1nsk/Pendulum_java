package helloworld;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import devices.Protocol.Pi4SPIDevice;
import devices.sensorImplementations.MPU9250.MPU9250;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.github.dlopuch.apa102_java_rpi.Apa102Output;
import java.util.logging.Level;
import java.util.logging.Logger;

public class main {

    final static int NUM_LEDS = 144;

    public static void main(String[] args) throws IOException, InterruptedException {
        Thread serverThread = new Thread(() -> serverTask());
        serverThread.setDaemon(true);
        serverThread.start();

        try {
            MPU9250 mpu9250 = new MPU9250(
                    new Pi4SPIDevice(0, 100_000), // MPU9250 Protocol device
                    400,                                    // sample rate
                    100);                                   // sample size
      
        } catch (IOException e) {
            e.printStackTrace();
        }

        Apa102Output.initSpi();
// Could also init with non-defaults using #initSpi(SpiChannel spiChannel, int spiSpeed, SpiMode spiMode)
// Default speed is 7.8 Mhz

        Apa102Output strip = new Apa102Output(NUM_LEDS);

        byte[] ledRGBs = new byte[NUM_LEDS * 3];
        int counter = 0;
        while(true){
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 1000) {
                // <fill in your ledRGBs buffer with your pattern... eg examples/RainbowStrip.java>
                counter++;
                strip.writeStrip(ledRGBs);
                Thread.sleep(1);
            }
            System.out.println(counter + " counter");
            counter = 0;
        }
    }

    private static void serverTask() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/test", new MyHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
