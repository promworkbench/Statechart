package org.processmining.algorithms.statechart.l2l.list;

import gnu.trove.map.hash.THashMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.algorithms.statechart.l2l.HandleActivityUtil;
import org.processmining.algorithms.statechart.l2l.L2LNestedCalls;
import org.processmining.models.statechart.log.LogFactory;
import org.processmining.xes.statechart.classification.XEventListLabelClassifier;
import org.processmining.xes.statechart.extension.XListLabelExtension;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class L2LListNestedCalls extends L2LNestedCalls {

    // TODO: legacy, remove this
    private final String DefaultInstance = "DefaultInstance";
    
    private static final XListLabelExtension extListLabel = XListLabelExtension.instance();

    public L2LListNestedCalls() {
        this(new Parameters());
    }

    public L2LListNestedCalls(Parameters params) {
        super(params);
    }

    /**
     * Transform log like [ <a_s, b_s, c_s, c_c, b_c, d_s, d_c, e_s, e_c> ]
     * where _s, _c refer to start complete (e.g. via lifecycle info) into log
     * like [ < a.b.c, a.d, e > ] where a.b.c is an event with collection <a, b,
     * c> as label based on list-label extension and classifier
     * 
     * @param input
     * @return
     */
    @Override
    public XLog transform(XLog input) {
        Preconditions.checkNotNull(input);

        Preconditions.checkNotNull(params.clsLabel, "No label classifier set");
        Preconditions.checkNotNull(params.clsSR,
                "No start-return classifier set");
        Preconditions.checkNotNull(params.startSymbol, "No start symbol set");
        Preconditions.checkNotNull(params.returnSymbol, "No return symbol set");

        params.startSymbol = params.startSymbol.toLowerCase();
        params.returnSymbol = params.returnSymbol.toLowerCase();
        if (params.handleSymbol.isPresent()) {
            params.handleSymbol = Optional.of(params.handleSymbol.get().toLowerCase());
        }
        
        XFactory f = LogFactory.getFactory();
        XLog log = f.createLog();
        log.getExtensions().add(extListLabel);
        log.getExtensions().add(XConceptExtension.instance());
        log.getClassifiers().add(new XEventNameClassifier());
        log.getClassifiers().add(new XEventListLabelClassifier());

        for (XTrace trace : input) {
            log.add(_transformTrace(f, trace));
        }

        return log;
    }

    @Override
    protected XTrace _transformTrace(XFactory f, XTrace input) {
        XTrace result = f.createTrace();

        // per instance key a list of prefixes to use
        Map<String, Deque<String>> prefixes = new THashMap<>();
        Map<String, Deque<XAttributeMap>> dataPres = new THashMap<>();
        //Map<String, Deque<XAttributeMap>> dataPosts = new THashMap<>();
//        TObjectIntMap<String[]> instMap = new TObjectIntHashMap<String[]>(
//                Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR,
//                NoIntValue);
        Map<String, Multiset<List<String>>> instMaps = new THashMap<>();
        Map<String, Deque<String>> instStacks = new THashMap<>();
        
        // the last visited symbol (never in the prefix)
        String symbol = null;

        for (XEvent event : input) {
            String eventLabel = params.clsLabel.getClassIdentity(event);
            String eventSR = params.clsSR.getClassIdentity(event).toLowerCase();
            Deque<String> prefix = _getPrefixList(prefixes, event);
            Deque<XAttributeMap> dataPre = _getPrefixList(dataPres, event);
            //Deque<XAttributeMap> dataPost = _getPrefixList(dataPosts, event);
            Multiset<List<String>> instMap = _getMultiset(instMaps, event);
            Deque<String> instStack = _getPrefixList(instStacks, event);
            
            XAttributeMap map = event.getAttributes();

            if (params.handleSymbol.isPresent() && eventSR.equals(params.handleSymbol.get())) {
                // a handle exception happens in the middle of an interval
                // and should provide "1 extra level" with a special symbol annotation

                String label = HandleActivityUtil.createHandleActivity(eventLabel);
                
                // instance counting
                List<String> instKey = _constructKey(prefix, label);
                int inst = 1 + instMap.add(instKey, 1);
                instStack.addLast(Integer.toString(inst));

                // Create handle name
                prefix.addLast(label);
                dataPre.addLast(map);
                
                // add event
                result.add(_createEvent(f, event, prefix, dataPre, instStack));//, dataPost));
                
                // cleanup
                prefix.removeLast();
                dataPre.removeLast();
                instStack.removeLast();
                
            } else if (eventSR.equals(params.startSymbol)) {
                // interval start
                if (symbol != null) {
                    prefix.addLast(symbol);
                }
                symbol = eventLabel;
                dataPre.addLast(map);
                
                // instance counting
                List<String> instKey = _constructKey(prefix, symbol);
                int inst = 1 + instMap.add(instKey, 1);
                instStack.addLast(Integer.toString(inst));
                
            } else if (eventSR.equals(params.returnSymbol)) {
                // interval end
                
                // in case we only have a complete, and no matching start: 
                // be nice and consider it a leaf case
                // TODO: optimize contain?
                // TODO: consider multiple start-complete with the same name
                if ((symbol == null || !symbol.equals(eventLabel)) && !prefix.contains(eventLabel)) {
                    if (symbol != null) {
                        prefix.addLast(symbol);
                    }
                    symbol = eventLabel;
                    dataPre.addLast(map);
                    
                    // instance counting
                    List<String> instKey = _constructKey(prefix, symbol);
                    int inst = 1 + instMap.add(instKey, 1);
                    instStack.addLast(Integer.toString(inst));
                }
                
                // check leaf or intermediate complete
//                if ((symbol != null && symbol.equals(eventLabel))) {
                if (symbol != null) {
                    // leaf-type
                    prefix.addLast(symbol);
                    result.add(_createEvent(f, event, prefix, dataPre, instStack));//, dataPost));
                    
                    String lastLabel = null;
                    while (!prefix.isEmpty() && (lastLabel == null || !lastLabel.equals(eventLabel))) {
                        lastLabel = prefix.removeLast();
                        dataPre.removeLast();
                        instStack.removeLast();
                    }
                    symbol = null; // mark as non-leaf
                } else {
                    // non-leaf type
                    String lastLabel = null;
                    while (!prefix.isEmpty() && (lastLabel == null || !lastLabel.equals(eventLabel))) {
                        // unwind till the return-event symbol is removed
                        lastLabel = prefix.removeLast();
                        if (!dataPre.isEmpty()) {
                            dataPre.removeLast();
                        }
                        if (!instStack.isEmpty()) {
                            instStack.removeLast();
                        }
                    }
                }
            }
        }

        return result;
    }

    private List<String> _constructKey(Deque<String> prefix, String symbol) {
        List<String> key = new ArrayList<String>(prefix.size() + 1);
        key.addAll(prefix);
        key.add(symbol);
        return key;
    }

    private XEvent _createEvent(XFactory f, XEvent eReturn, Deque<String> prefix, 
            Deque<XAttributeMap> dataPre, Deque<String> instStack) {//, Deque<XAttributeMap> dataPost) {
        XEvent event = f.createEvent();
        XAttributeMap eMap = event.getAttributes();

        // copy basic attributes
        XAttributeMap returnMap = eReturn.getAttributes();
        for (String key : returnMap.keySet()) {
            if (!key.equals(XLifecycleExtension.KEY_TRANSITION)
                    && !key.equals(XConceptExtension.KEY_NAME)) {
                eMap.put(key, returnMap.get(key));
            }
        }
        
        // list dataPre
        extListLabel.assignData(event, dataPre);
        
        // override label
        extListLabel.assignName(event, prefix);
        extListLabel.assignInstance(event, instStack);

        return event;
    }

    private <T> Deque<T> _getPrefixList(Map<String, Deque<T>> prefixes,
            XEvent event) {
        // TODO: legacy, remove this
        String key = DefaultInstance;
//        if (params.clsInst != null) {
//            key = params.clsInst.getClassIdentity(event);
//        }

        Deque<T> result = prefixes.get(key);
        if (result == null) {
            result = new ArrayDeque<T>();
            prefixes.put(key, result);
        }

        return result;
    }

    private <T> Multiset<T> _getMultiset(Map<String, Multiset<T>> maps, 
            XEvent event) {
        // TODO: legacy, remove this
        String key = DefaultInstance;
//        if (params.clsInst != null) {
//            key = params.clsInst.getClassIdentity(event);
//        }

        Multiset<T> result = maps.get(key);
        if (result == null) {
            result = HashMultiset.create();
            maps.put(key, result);
        }

        return result;
    }
}
