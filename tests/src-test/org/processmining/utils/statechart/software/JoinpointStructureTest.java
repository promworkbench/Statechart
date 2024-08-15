package org.processmining.utils.statechart.software;

import static org.junit.Assert.*;

import org.junit.Test;
import org.processmining.utils.statechart.software.JoinpointStructure;
import org.processmining.utils.statechart.software.JoinpointStructure.Parameter;

public class JoinpointStructureTest {

    @Test
    public void testParameterTypeNoName() {
        JoinpointStructure.Parameter param = new Parameter("java.lang.String");
        assertEquals("java.lang", param.getTypePackage());
        assertEquals("String", param.getTypeClass());
        assertNull(param.getTypePrimitive());
        assertFalse(param.isArray());
        assertFalse(param.isGeneric());
        assertNull(param.getParamName());
    }

    @Test
    public void testParameterTypeNoNameArray() {
        JoinpointStructure.Parameter param = new Parameter("java.lang.String[]");
        assertEquals("java.lang", param.getTypePackage());
        assertEquals("String", param.getTypeClass());
        assertNull(param.getTypePrimitive());
        assertTrue(param.isArray());
        assertFalse(param.isGeneric());
        assertNull(param.getParamName());
    }

    @Test
    public void testParameterTypeNoNameGeneric() {
        JoinpointStructure.Parameter param = new Parameter("java.lang.String<?>");
        assertEquals("java.lang", param.getTypePackage());
        assertEquals("String", param.getTypeClass());
        assertNull(param.getTypePrimitive());
        assertFalse(param.isArray());
        assertTrue(param.isGeneric());
        assertNull(param.getParamName());
    }
    
    @Test
    public void testParameterTypeNamed() {
        JoinpointStructure.Parameter param = new Parameter("java.lang.String input");
        assertEquals("java.lang", param.getTypePackage());
        assertEquals("String", param.getTypeClass());
        assertNull(param.getTypePrimitive());
        assertFalse(param.isArray());
        assertFalse(param.isGeneric());
        assertEquals("input", param.getParamName());
    }
    
    @Test
    public void testParameterPrimitiveNoName() {
        JoinpointStructure.Parameter param = new Parameter("int");
        assertNull(param.getTypePackage());
        assertNull(param.getTypeClass());
        assertEquals("int", param.getTypePrimitive());
        assertFalse(param.isArray());
        assertFalse(param.isGeneric());
        assertNull(param.getParamName());
    }
    
    @Test
    public void testParameterPrimitiveNamed() {
        JoinpointStructure.Parameter param = new Parameter("int input");
        assertNull(param.getTypePackage());
        assertNull(param.getTypeClass());
        assertEquals("int", param.getTypePrimitive());
        assertFalse(param.isArray());
        assertFalse(param.isGeneric());
        assertEquals("input", param.getParamName());
    }
    
    @Test
    public void testMethodJoinpoint() {
        JoinpointStructure jp = new JoinpointStructure("org.junit.runner.JUnitCore.main(java.lang.String[])");

        assertEquals("org.junit.runner", jp.getJpPackage());
        assertEquals("JUnitCore", jp.getJpClass());
        assertEquals("main", jp.getJpMethod());
        assertFalse(jp.isConstructor());
        assertNotNull(jp.getJpParams());
        assertEquals(1, jp.getJpParams().length);
    }
    
    @Test
    public void testConstructorJoinpoint() {
        JoinpointStructure jp = new JoinpointStructure("org.junit.runner.JUnitCore(java.lang.String[])");

        assertEquals("org.junit.runner", jp.getJpPackage());
        assertEquals("JUnitCore", jp.getJpClass());
        assertEquals("JUnitCore", jp.getJpMethod());
        assertTrue(jp.isConstructor());
        assertNotNull(jp.getJpParams());
        assertEquals(1, jp.getJpParams().length);
    }
    
    @Test
    public void testMethodJoinpointNoParam() {
        JoinpointStructure jp = new JoinpointStructure("org.junit.runner.JUnitCore.main()");

        assertEquals("org.junit.runner", jp.getJpPackage());
        assertEquals("JUnitCore", jp.getJpClass());
        assertEquals("main", jp.getJpMethod());
        assertFalse(jp.isConstructor());
        assertNotNull(jp.getJpParams());
        assertEquals(0, jp.getJpParams().length);
    }
    
    @Test
    public void testMethodJoinpointMultiParam() {
        JoinpointStructure jp = new JoinpointStructure("org.junit.runner.JUnitCore.main(org.junit.internal.JUnitSystem,java.lang.String[])");

        assertEquals("org.junit.runner", jp.getJpPackage());
        assertEquals("JUnitCore", jp.getJpClass());
        assertEquals("main", jp.getJpMethod());
        assertFalse(jp.isConstructor());
        assertNotNull(jp.getJpParams());
        assertEquals(2, jp.getJpParams().length);
    }
}
