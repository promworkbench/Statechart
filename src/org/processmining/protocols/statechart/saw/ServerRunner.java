package org.processmining.protocols.statechart.saw;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.processmining.utils.statechart.signals.Signal1;

public class ServerRunner implements Runnable {

    public final Signal1<SawServerStatus> SignalStatus = new Signal1<>();

    private static final Logger logger = LogManager
            .getLogger(ServerRunner.class.getName());
    
    private SawBus bus;
    private int port;

    private List<ClientHandler> handlers = new ArrayList<ClientHandler>();
    private List<Thread> threads = new ArrayList<Thread>();
    private ServerSocket serverSocket;
    
    public ServerRunner(SawBus bus) {
        this(bus, 9025);
    }
    
    public ServerRunner(SawBus bus, int port) {
        this.bus = bus;
        this.port = port;
    }
    
    @Override
    public void run() {
        SignalStatus.dispatch(SawServerStatus.Starting);
        logger.debug("Server startup");
        serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);

            SignalStatus.dispatch(SawServerStatus.Running);
            logger.debug("Server started");
            while(true) {
                Socket incoming = serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(incoming, bus);
                Thread clientThread = new Thread(clientHandler);
                clientThread.setDaemon(true);
                
                clientThread.start();
                handlers.add(clientHandler);
                threads.add(clientThread);
            }
        } catch(IOException e) {
            SignalStatus.dispatch(SawServerStatus.Error);
            e.printStackTrace();
        } finally {
            SignalStatus.dispatch(SawServerStatus.Shutdown);
            stop();
            SignalStatus.dispatch(SawServerStatus.Offline);
            logger.debug("Server stopped");
        }
    }

    public void stop() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            for (ClientHandler clientHandler : handlers) {
                clientHandler.stop();
            }
            handlers.clear();
            for (Thread clientThread : threads) {
                clientThread.join();
            }
            threads.clear();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
