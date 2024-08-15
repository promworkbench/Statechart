package org.processmining.protocols.statechart.saw.api;

import java.util.Collection;

import org.processmining.protocols.statechart.saw.SawBus;
import org.processmining.protocols.statechart.saw.api.data.DataPackage;
import org.processmining.protocols.statechart.saw.api.data.Joinpoint;
import org.processmining.protocols.statechart.saw.api.data.JoinpointStat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SawApiJson extends SawApi {

    private SawBus bus;

    private ObjectMapper mapper = new ObjectMapper();

    private Collection<JoinpointStat> cache_joinpointStats;
    
    public SawApiJson(SawBus bus) {
        this.bus = bus;
    }

    private void _send(Object data) {
        try {
            String json = mapper.writeValueAsString(data);
            bus.bus.dispatch(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void _set(String key, Object data, boolean broadcastUpdate) {
        try {
            String json = mapper.writeValueAsString(data);
            bus.setStartupMessage(key, json, broadcastUpdate);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void selectJoinpoint(Joinpoint joinpoint) {
        _send(DataPackage.createSelectJoinpoint(joinpoint));
    }

    @Override
    public void setJoinpointStats(Collection<JoinpointStat> joinpointStats) {
        if (cache_joinpointStats != joinpointStats) {
            cache_joinpointStats = joinpointStats;
            _set(SawApi.KeyJoinpointStats,
                    DataPackage.createJoinpointStats(joinpointStats), true);
        }
    }

}
