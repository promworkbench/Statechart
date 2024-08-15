package org.processmining.protocols.statechart.saw;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.SignalRegistration;

public class ClientHandler implements Runnable {

    private static final Logger logger = LogManager
            .getLogger(ClientHandler.class.getName());
    
    private Socket socket;
    
    private BlockingQueue<String> queue = new ArrayBlockingQueue<String>(20);

    private PrintStream out;

    private SignalRegistration<Action1<String>> reg;

    public ClientHandler(Socket incoming, SawBus bus) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Setup incoming socket at " + incoming.getRemoteSocketAddress());
        }
        
        socket = incoming;
        out = new PrintStream(socket.getOutputStream());

        try {
            reg = bus.bus.register(new Action1<String>() {
                @Override
                public void call(String t) {
                    try {
                        queue.put(t);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            
            for (String msg : bus.getStartupMessages()) {
                queue.put(msg);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while(true) {
                String msg = queue.take();
                if (logger.isDebugEnabled()) {
                    logger.debug("Broadcast '" + msg + "' at " + socket.getRemoteSocketAddress());
                }
                out.print(msg);
                out.print("\n");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public void stop() {
        reg.unregister();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
