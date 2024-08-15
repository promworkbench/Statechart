package org.processmining.protocols.statechart.saw;

import org.processmining.protocols.statechart.saw.api.SawApi;
import org.processmining.protocols.statechart.saw.api.SawApiJson;
import org.processmining.utils.statechart.signals.Action1;
import org.processmining.utils.statechart.signals.Signal1;
import org.processmining.utils.statechart.signals.SignalRegistration;

/**
 * SAW Server thread and API
 * @author mleemans
 *
 * SAW - Software Analysis Workbench 
 * Hosts API for SAW commands
 * Used by Eclipse SAW plugin for Java IDE interactions
 */
public class SawServer {

    public final Signal1<SawServerStatus> SignalStatus = new Signal1<>();

    private SawBus bus;
    private SawApi api;
    
    private boolean running;
    private ServerRunner serverRunner;
    private Thread serverThread;

    private SawServerStatus runStatus;
    private SignalRegistration<Action1<SawServerStatus>> sigReg;

    private static SawServer instance;

    public static SawServer instance() {
        if (instance == null) {
            instance = new SawServer();
        }
        return instance;
    }
    
    private SawServer() {
        bus = new SawBus();
        api = new SawApiJson(bus);
        
        running = false;
        runStatus = SawServerStatus.Offline;
    }
    
    public void start() {
        if (!running) {
            serverRunner = new ServerRunner(bus);
            sigReg = serverRunner.SignalStatus.register(new Action1<SawServerStatus>() {
                @Override
                public void call(SawServerStatus t) {
                    runStatus = t;
                    SignalStatus.dispatch(t);
                }
            });
            
            serverThread = new Thread(serverRunner);
            serverThread.setDaemon(true);
            serverThread.start();
            running = true;
        }
    }
    
    public void stop() {
        if (running) {
            serverRunner.stop();
            try {
                serverThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (sigReg != null) {
                    sigReg.unregister();
                }
                sigReg = null;
                
                serverThread = null;
                serverRunner = null;
                running = false;
            }
        }
    }
    
    public SawApi getApi() {
        return api;
    }
    
    public boolean isRunning() {
        return running;
    }

    public SawServerStatus getStatus() {
        return runStatus;
    }
}
