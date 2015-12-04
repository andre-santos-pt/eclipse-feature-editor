package pt.iscte.eclipse.featureeditor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.zest.core.viewers.IGraphEntityContentProvider;
import org.osgi.framework.Bundle;

import pt.iscte.eclipse.featureeditor.model.Feature;
import pt.iscte.eclipse.featureeditor.model.FeatureSet;


public class NodeProvider extends ArrayContentProvider implements IGraphEntityContentProvider {
	private FeatureSet set;

	NodeProvider(Bundle root, Set<String> excludeExtensionPoints) {
		set = new FeatureSet();
		Feature rootFeature = new Feature(root, set);
		set.setRoot(rootFeature);

		buildTree(set.getRoot(), set, excludeExtensionPoints, new HashSet<String>());
	}

	NodeProvider(String rootName, Bundle[] roots, Set<String> excludeExtensionPoints) {
		set = new FeatureSet();
		Feature rootFeature = new Feature(rootName, set);
		set.setRoot(rootFeature);

		for(Bundle b : roots) {
			Feature subRoot = new Feature(b, set);
			buildTree(subRoot, set, excludeExtensionPoints, new HashSet<String>());
		}
	}

	private void buildTree(Feature f, FeatureSet set, Collection<String> excludeExtensionPoints, Set<String> handled)  {
		if(handled.contains(f.getPlugin()))
			return;

		handled.add(f.getPlugin());

		IExtensionRegistry reg = Platform.getExtensionRegistry();
		
		for(IExtensionPoint ep : reg.getExtensionPoints()) {
			if(!excludeExtensionPoints.contains(ep.getUniqueIdentifier()) &&
					ep.getContributor().getName().equals(f.getPlugin())) {
				
				Feature absFeat = new Feature(f, ep, set);
				
				for(IExtension e : ep.getExtensions()) {
					Feature child = new Feature(absFeat, e, set);	
					IExtension[] exts = reg.getExtensions(e.getContributor().getName());
					IExtensionPoint[] extensionPoints = reg.getExtensionPoints(e.getContributor());
					System.out.println(child + ": " + extensionPoints.length);
					
					if(exts.length > 0 && exts[0].equals(e)) {
						buildTree(child, set, excludeExtensionPoints, handled);
					}
				}
			}
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		Object[] elements = set.getAllFeatures().toArray();
		return elements;
	}

	@Override
	public Object[] getConnectedTo(Object entity) {
		Feature feature = (Feature) entity;
		return feature.getChildren().toArray();
	}

	public FeatureSet getFeatureSet() {
		return set;
	}

}