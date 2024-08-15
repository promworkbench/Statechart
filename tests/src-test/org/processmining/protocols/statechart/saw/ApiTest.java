package org.processmining.protocols.statechart.saw;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.processmining.protocols.statechart.saw.SawBus;
import org.processmining.protocols.statechart.saw.api.SawApiJson;
import org.processmining.protocols.statechart.saw.api.data.DataPackage;
import org.processmining.protocols.statechart.saw.api.data.Joinpoint;
import org.processmining.utils.statechart.signals.Action1;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiTest {

    @Test
    public void testSelectJoinpoint() {
        SawBus bus = new SawBus();
        DataComparer cmp = new DataComparer();
        bus.bus.register(cmp);
        
        SawApiJson api = new SawApiJson(bus);
        
        cmp.expected = DataPackage.createSelectJoinpoint(new Joinpoint("A", "B", 2));
        api.selectJoinpoint(new Joinpoint("A", "B", 2));
    }
}

class DataComparer implements Action1<String> {

    private ObjectMapper mapper;

    public DataPackage expected;
    
    public DataComparer() {
        mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
    
    @Override
    public void call(String t) {
        DataPackage actual;
        try {
            actual = mapper.readValue(t, DataPackage.class);
            assertEquals(expected, actual);
        } catch (JsonParseException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (JsonMappingException e) {
            e.printStackTrace();
            fail(e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
    
}