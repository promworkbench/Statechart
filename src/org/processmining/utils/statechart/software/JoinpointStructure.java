package org.processmining.utils.statechart.software;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoinpointStructure {
    private static final Pattern regJp = Pattern.compile(
            "^(.*?)\\((.*?)\\)(.*?)$");
    private static final Pattern regParam = Pattern.compile(
            "^([a-zA-Z0-9_.$]*?)(<\\?>)?(\\[\\])?(?:\\s+([a-zA-Z0-9_$]*?))?$");
    
    public static class Parameter {
        private final String typePackage;
        private final String typeClass;
        private final String typePrimitive;
        private final String paramName;
        private final boolean isArray;
        private final boolean isGeneric;

        public Parameter(String paramString) {
            Matcher regexMatcher = regParam.matcher(paramString);
            if (regexMatcher.find()) {
                String typeStr = regexMatcher.group(1);
                int pos = typeStr.lastIndexOf('.');
                if(pos == -1) {
                    typePackage = null;
                    typeClass = null;
                    typePrimitive = typeStr;
                } else {
                    typePackage = typeStr.substring(0, pos);
                    typeClass = typeStr.substring(pos + 1);
                    typePrimitive = null;
                }
                
                isGeneric = isPresent(regexMatcher.group(2));
                isArray = isPresent(regexMatcher.group(3));
                paramName = regexMatcher.group(4);
            } else {
                throw new IllegalArgumentException("Could not be parsed as a parameter");
            }
        }

        private boolean isPresent(String group) {
            return group != null && !group.isEmpty();
        }

        public String getTypePackage() {
            return typePackage;
        }

        public String getTypeClass() {
            return typeClass;
        }

        public String getTypePrimitive() {
            return typePrimitive;
        }

        public String getParamName() {
            return paramName;
        }

        public boolean isArray() {
            return isArray;
        }

        public boolean isGeneric() {
            return isGeneric;
        }
    }
    
    private final String jpPackage;
    private final String jpClass;
    private final String jpMethod;
    private final boolean isConstructor;
    private final Parameter[] jpParams;
    private final String postfix;
    private final String jpParamsStr;
    
    public JoinpointStructure(String joinpoint) {
        Matcher regexMatcher = regJp.matcher(joinpoint);
        if (regexMatcher.find()) {
            String callStr = regexMatcher.group(1);
            int pos = callStr.lastIndexOf('.');
            if(pos == -1) {
                throw new IllegalArgumentException("Could not be parsed as call type in Joinpoint ");
            } else {
                String typeStr = callStr.substring(0, pos);
                String method = callStr.substring(pos+1);

                int pos2 = typeStr.lastIndexOf('.');
                if ((!method.isEmpty() && Character.isUpperCase(method.charAt(0)))
                        || (pos2 == -1)) {
                    jpPackage = typeStr;
                    jpClass = method;
                    jpMethod = method;
                    isConstructor = true;
                } else {
                    jpPackage = typeStr.substring(0, pos2);
                    jpClass = typeStr.substring(pos2 + 1);
                    jpMethod = method;
                    isConstructor = false;
                }
            }
            
            List<Parameter> params = new ArrayList<>();
            jpParamsStr = regexMatcher.group(2);
            for (String param : jpParamsStr.split(",")) {
                try{
                    if (!param.isEmpty()) {
                        params.add(new Parameter(param));
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            jpParams = params.toArray(new Parameter[params.size()]);
            
            postfix = regexMatcher.group(3);
        } else {
            throw new IllegalArgumentException("Could not be parsed as a Joinpoint");
        }
    }

    public String getJpPackage() {
        return jpPackage;
    }

    public String getJpClass() {
        return jpClass;
    }

    public String getJpMethod() {
        return jpMethod;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public Parameter[] getJpParams() {
        return jpParams;
    }
    
    public String getJpParamStr() {
        return jpParamsStr;
    }
    
    public String getPostfix() {
        return postfix;
    }

}
