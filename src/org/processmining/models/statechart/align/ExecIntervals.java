package org.processmining.models.statechart.align;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ExecIntervals implements Iterable<ExecInterval> {

    private final TIntObjectMap<List<ExecInterval>> executions;
    private int size;
    
    public ExecIntervals() {
        executions = new TIntObjectHashMap<>();
        size = 0;
    }
    
    public ExecIntervals(ExecIntervals copyFrom) {
        executions = new TIntObjectHashMap<>();
        for (int traceIndex : copyFrom.executions.keys()) {
            List<ExecInterval> execs = new ArrayList<>();
            for (ExecInterval exec : copyFrom.executions.get(traceIndex)) {
                execs.add(new ExecInterval(exec));
                size++;
            }
            executions.put(traceIndex, execs);
        }
    }
    
    public ExecInterval getLastInterval(int traceIndex) {
        List<ExecInterval> execs = executions.get(traceIndex);
        if (execs != null && !execs.isEmpty()) {
            return execs.get(execs.size() - 1);
        }
        return null;
    }

    public ExecInterval getLastOpenInterval(int traceIndex) {
        List<ExecInterval> execs = executions.get(traceIndex);
        if (execs != null) {
            for (int i = execs.size() - 1; i >= 0; i--) {
                ExecInterval ival = execs.get(i);
                if (ival.getComplete() == null) {
                    return ival;
                }
            }
        }
        return null;
    }

    public void addInterval(int traceIndex, ExecInterval exec) {
        List<ExecInterval> execs = executions.get(traceIndex);
        if (execs == null) {
            execs = new ArrayList<>();
            executions.put(traceIndex, execs);
        }
        execs.add(exec);
        size++;
    }

    @Override
    public Iterator<ExecInterval> iterator() {
        return new FlatIterator();
    }
    
    public int[] getTraceIndices() {
        return executions.keys();
    }
    
    public List<ExecInterval> getIntervalsForTrace(int traceIndex) {
        return executions.get(traceIndex);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
    
    private class FlatIterator implements Iterator<ExecInterval> {
        
        private Iterator<List<ExecInterval>> baseIt;
        private Iterator<ExecInterval> listIt;

        protected FlatIterator() {
            baseIt = executions.valueCollection().iterator();
            listIt = null;
        }
        
        @Override
        public boolean hasNext() {
            while ((listIt == null || !listIt.hasNext()) && baseIt.hasNext()) {
                listIt = baseIt.next().iterator();
            }
            
            return (listIt != null && listIt.hasNext());
        }

        @Override
        public ExecInterval next() {
            if (hasNext()) {
                return listIt.next();
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        bld.append("{");
        String split1 = "";
        for (int traceIndex : getTraceIndices()) {
            bld.append(traceIndex + " => [");
            String split2 = "";
            for (ExecInterval interval : executions.get(traceIndex)) {
                bld.append(interval);
                bld.append(split2);
                split2 = ", ";
            }
            bld.append("]");
            bld.append(split1);   
            split1 = ", \n";
        }
        bld.append("}");
        return bld.toString();
    }
}
