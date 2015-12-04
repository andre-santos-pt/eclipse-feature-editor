package pt.iscte.eclipse.featureeditor.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class Feature implements Comparable<Feature> {

	private String plugin;
	private String name;

	private Set<Feature> children;
	private Feature parent;

	private boolean abstractFeature;
	private String extensionPointId;


	private Feature(Feature parent, FeatureSet set) {
		this.parent = parent;
		children = new HashSet<Feature>();
		abstractFeature = false;
		set.newFeature(this);
	}

	public Feature(String name, FeatureSet set) {
		this((Feature) null, set);
		plugin = "NA";
		this.name = name;
	}

	public Feature(Bundle bundle, FeatureSet set) {
		this((Feature) null, set);
		plugin = bundle.getSymbolicName();
		name = bundle.getHeaders().get("Bundle-Name");
	}


	public Feature(Feature parent, IExtensionPoint extensionPoint, FeatureSet set) {
		this(parent, set);
		plugin = extensionPoint.getContributor().getName();
		name = extensionPoint.getLabel();
		parent.children.add(this);
		abstractFeature = true;
		extensionPointId = extensionPoint.getUniqueIdentifier();
		if(name.isEmpty())
			name = plugin;
	}

	public Feature(Feature parent, IExtension extension, FeatureSet set) {
		this(parent, set);
		plugin = extension.getContributor().getName();
		name = extension.getLabel();
		
		if(name.isEmpty()) {
			Bundle bundle = Platform.getBundle(plugin);
			name = bundle.getHeaders().get("Bundle-Name");
		}
		parent.children.add(this);
	}

	public String getName() {
		return name;
	}

	public String getPlugin() {
		return plugin;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public boolean isMandatory() {
		return isRoot() || parent.getPlugin().equals(plugin);
	}

	public boolean isAbstract() {
		return abstractFeature;
	}

	public String getExtensionPointId() {
		return extensionPointId;
	}


	public Feature getParent() {
		return parent;
	}

	public Set<Feature> getChildren() {
		return Collections.unmodifiableSet(children);
	}
	
	public boolean isLeaf() {
		return children.isEmpty();
	}

	public int getDepth() {
		if(isRoot())
			return 1;
		else
			return 1 + parent.getDepth();
	}

	public boolean samePlugin(Feature f) {
		return plugin.equals(f.plugin);
	}

	@Override
	public int compareTo(Feature f) {
		return plugin.compareTo(f.plugin);
	}

	@Override
	public String toString() {
		return name;
	}
}
