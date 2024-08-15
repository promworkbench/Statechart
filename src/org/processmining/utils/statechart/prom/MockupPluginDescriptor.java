package org.processmining.utils.statechart.prom;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginDescriptorID;
import org.processmining.framework.plugin.impl.AbstractPluginDescriptor;

public class MockupPluginDescriptor extends AbstractPluginDescriptor {

	public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasAnnotation(Class<? extends Annotation> annotationClass, int methodIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass, int methodIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public PackageDescriptor getPackage() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumberOfMethods() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Class<?>> getReturnTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getReturnNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<List<Class<?>>> getParameterTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Class<?>> getParameterTypes(int methodIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getParameterNames(int methodIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public Class<?> getPluginParameterType(int methodIndex, int parameterIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPluginParameterName(int methodIndex, int parameterIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public PluginDescriptorID getID() {
		// TODO Auto-generated method stub
		return null;
	}

	public Class<? extends PluginContext> getContextType(int methodIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Class<?>> getTypesAtParameterIndex(int globalParameterIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getIndexInParameterNames(int methodIndex, int methodParameterIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getIndexInMethod(int methodIndex, int globalParameterIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getMethodLabel(int methodIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public int compareTo(PluginDescriptor plugin) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isUserAccessible() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean handlesCancel() {
		// TODO Auto-generated method stub
		return false;
	}

	public int getMostSignificantResult() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMethodHelp(int methodIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getKeywords() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getCategories() {
		// TODO Auto-generated method stub
		return null;
	}

	protected Object[] execute(PluginContext context, int methodIndex, Object... allArgs) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean meetsQualityThreshold() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean meetsLevelThreshold() {
		// TODO Auto-generated method stub
		return false;
	}

	public ImageIcon getIcon() {
		return null;
	}
	
	public URL getURL() {
		return null;
	}

}
