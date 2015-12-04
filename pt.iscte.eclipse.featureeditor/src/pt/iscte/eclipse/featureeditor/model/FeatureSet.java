package pt.iscte.eclipse.featureeditor.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class FeatureSet implements Iterable<Feature> {
	private Feature root;
	private final Set<Feature> set;
	
	public FeatureSet() {
		set = new HashSet<Feature>();
	}
	
	public void setRoot(Feature f) {
		this.root = f;
	}
	
	public Feature getRoot() {
		return root;
	}
	
	public int getDepth() {
		return depth(root, 1);
	}
	
	private int depth(Feature f, int d) {
		if(!f.getChildren().isEmpty()) {
			int max = 0;
			for(Feature c : f.getChildren()) {
				int cd = depth(c, d + 1);
				if(cd > max)
					max = cd;
			}
			return max;
		}
		
		return d;
	}
	
	void newFeature(final Feature f) {
		set.add(f);
//		f.addObserver(new Observer() {
//			
//			@Override
//			public void update(Observable o, Object arg) {
//				setChanged();
//				notifyObservers(f);
//			}
//		});
	}
	
	public Set<Feature> getAllFeatures() {
		return Collections.unmodifiableSet(set);
	}

	
	public SortedSet<String> getPlugins() {
		SortedSet<String> plugins = new TreeSet<String>();
		for(Feature f : set)
			plugins.add(f.getPlugin());
		
		return plugins;
	}
	
	public Set<Feature> getFeaturesOfSamePlugin(Feature feature) {
		Set<Feature> features = new HashSet<Feature>();
		
		for(Feature f : set)
			if(!f.equals(feature) && f.samePlugin(feature))
				features.add(f);
		
		return features;
	}

	public String getRootPlugin() {
		return root.getPlugin();
	}

	public Set<Feature> getFeaturesOfPlugin(String plugin) {
		Set<Feature> features = new HashSet<Feature>();
		for(Feature f : set)
			if(f.getPlugin().equals(plugin))
				features.add(f);
		
		return features;
	}

	@Override
	public Iterator<Feature> iterator() {
		return getAllFeatures().iterator();
	}
	
	public int totalAbstractFeatures() {
		int c = 0;
		for(Feature f : set)
			if(f.isAbstract())
				c++;
		return c;
	}

	public int totalConcreteFeatures() {
		int c = 0;
		for(Feature f : set)
			if(!f.isAbstract())
				c++;
		return c;
	}

}
