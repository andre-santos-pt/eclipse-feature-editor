package pt.iscte.eclipse.featureeditor.model;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import org.eclipse.core.runtime.Assert;


public class FeatureSelection extends Observable {

	private FeatureSet set;
	private Set<String> selection;
	
	public FeatureSelection(FeatureSet set) {
		Assert.isNotNull(set);
		this.set = set;
		selection = new HashSet<String>();
	}
	
	public boolean isPluginSelected(String pluginId) {
		Assert.isNotNull(pluginId);
		return selection.contains(pluginId);
	}
	
	public boolean isFeatureSelected(Feature f) {
		Assert.isNotNull(f);
		return isPluginSelected(f.getPlugin());
	}
	
	public void selectPlugin(String pluginId) {
		Assert.isNotNull(pluginId);
		Assert.isTrue(set.getPlugins().contains(pluginId));
		
		selection.add(pluginId);
		setChanged();
		notifyObservers(pluginId);
	}
	
	public void unselectPlugin(String pluginId) {
		Assert.isNotNull(pluginId);
		Assert.isTrue(set.getPlugins().contains(pluginId));
		
		selection.remove(pluginId);
		setChanged();
		notifyObservers(pluginId);
	}
	
}
