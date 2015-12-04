package pt.iscte.eclipse.featureeditor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.pde.core.target.NameVersionDescriptor;
import org.eclipse.pde.core.target.TargetFeature;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.osgi.framework.Bundle;

import pt.iscte.eclipse.featureeditor.PluginList.SelectionListener;
import pt.iscte.eclipse.featureeditor.model.Feature;
import pt.iscte.eclipse.featureeditor.model.FeatureSelection;
import pt.iscte.eclipse.featureeditor.model.FeatureSet;


public class FeatureModelEditor extends MultiPageEditorPart {
	private GraphViewer viewer;
	private String rootFeatureId;
	private FeatureSet set;
	private FeatureSelection selection;

	private TargetFeature featureXml;

	private boolean dirty;
	private org.eclipse.swt.widgets.List excludesList;

	private int pageIndex = 0;
	private PluginList pluginList;
	private StyleProvider styleProvider;

	// runs first when editor is created
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
		try {
			featureXml = new TargetFeature(((IFileEditorInput) input).getFile().getLocation().toFile());
			NameVersionDescriptor[] plugins = featureXml.getPlugins();
			if(plugins.length > 0) {
				rootFeatureId = plugins[0].getId();
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}


	@Override
	protected void createPages() {
		addPage(createDiagramArea(), "Feature Diagram");
		addPage(createXmlArea(), "feature.xml");
		addPage(createSettingsArea(), "Settings");
		reloadContent();
	}


	@Override
	public String getTitle() {
		String title = super.getTitle();
		if(dirty)
			title = "*" + title;

		return title;
	}



	private void addPage(Composite area, String title) {
		addPage(area);
		setPageText(pageIndex, title);
		pageIndex++;
	}

	private void reloadContent() {
		if(rootFeatureId == null) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), 
					"Root component could not be loaded", "see feature.xml, first plugin in the list");
			return;
		}
		Bundle rootBundle = Platform.getBundle(rootFeatureId);

		if(rootBundle == null) {	
			MessageDialog.openError(Display.getDefault().getActiveShell(), 
					"Root component not found", "not found: " + rootFeatureId);
			return;
		}

		Set<String> excludes = new HashSet<String>();
		for(String s : excludesList.getItems())
			excludes.add(s);

		NodeProvider provider = new NodeProvider(rootBundle, excludes);
		viewer.setContentProvider(provider);

		set = provider.getFeatureSet();
		selection = new FeatureSelection(set);
		selection.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				viewer.update(set.getFeaturesOfPlugin((String) arg).toArray(), null);
			}
		});

		styleProvider.setSelection(selection);
		pluginList.setInput(set, selection);
		viewer.setInput(null);

		//		pluginTable.setChecked(rootFeatureId, true);
		//		updatePanelFields();
	}


	private Composite createDiagramArea() {
		Composite diagramArea = new Composite(getContainer(), SWT.NONE);
		diagramArea.setLayout(new GridLayout(2, false));
		pluginList = new PluginList(diagramArea);
		createDiagramArea(diagramArea);

		pluginList.addSelectionListener(new SelectionListener() {
			@Override
			public void selectionChanged(List<String> selectedPlugins) {
				if(selectedPlugins.isEmpty())
					viewer.setSelection(StructuredSelection.EMPTY);
				else {
					List<Object> list = new ArrayList<Object>();
					for(String pluginId : selectedPlugins)
						list.addAll(set.getFeaturesOfPlugin(pluginId));
					
					viewer.setSelection(new StructuredSelection(list.toArray()));
				}
			}
		});
		return diagramArea;
	}


	private Composite createXmlArea() {
		Composite xmlArea = new Composite(getContainer(), SWT.NONE);
		xmlArea.setLayout(new FillLayout());
		Text text = new Text(xmlArea, SWT.MULTI | SWT.READ_ONLY);
		IFileEditorInput input = (IFileEditorInput) getEditorInput();
		try {
			InputStream contents = input.getFile().getContents();
			Scanner scanner = new Scanner(contents);
			while(scanner.hasNextLine()) {
				text.append(scanner.nextLine() + "\n");
			}
			scanner.close();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return xmlArea;
	}







	private void createDiagramArea(Composite parent) {

		viewer = new GraphViewer(parent, SWT.BORDER);
		styleProvider = new StyleProvider(viewer);
		viewer.setLabelProvider(styleProvider);
		viewer.setConnectionStyle(ZestStyles.CONNECTIONS_SOLID);
		//		viewer.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		viewer.setLayoutAlgorithm(new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);

		//		viewer.setInput(null);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));



		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if(!sel.isEmpty()) {
					Object obj = sel.getFirstElement();
					if(obj instanceof Feature) {
						Feature f = (Feature) obj;
						if(selection.isPluginSelected(f.getPlugin()))
							selection.unselectPlugin(f.getPlugin());
						else {
							selection.selectPlugin(f.getPlugin());
							Feature parent = f.getParent();
							while(parent != null) {
								selection.selectPlugin(parent.getPlugin());
								parent = parent.getParent();
							}
						}
						dirty = true;
					}
				}
			}
		});

		addDiagramMenu();
	}



	private void addDiagramMenu() {
		Menu menu = new Menu(viewer.getControl());

		MenuItem selectRoot = new MenuItem(menu, SWT.PUSH);
		selectRoot.setText("Select root plugin");
		selectRoot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//				IStructuredSelection selection = new StructuredSelection(set.getRootPlugin());
				//				pluginTable.setSelection(selection);
				pluginList.select(set.getRootPlugin());
			}
		});

		MenuItem setRoot = new MenuItem(menu, SWT.PUSH);
		setRoot.setText("Set as root plugin");
		setRoot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog dialog = new InputDialog(Display.getDefault().getActiveShell(), "Set root plugin", "Enter the plugin id", rootFeatureId, null);
				dialog.open();
				String id = dialog.getValue();
				rootFeatureId = id;	
				reloadContent();
			}
		});

		MenuItem relayout = new MenuItem(menu, SWT.PUSH);
		relayout.setText("Relayout");
		relayout.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.applyLayout();
			}
		});


		final MenuItem openPluginXml = new MenuItem(menu, SWT.PUSH);
		openPluginXml.setText("Open plugin.xml");
		openPluginXml.setEnabled(false);
		openPluginXml.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for(Feature feature : getSelectedFeatures()) {
					ManifestVisitor v = new ManifestVisitor(feature.getPlugin());
					try {
						ResourcesPlugin.getWorkspace().getRoot().accept(v);
					} catch (CoreException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		MenuItem select = new MenuItem(menu, SWT.PUSH);
		select.setText("Select plugin(s)");
		select.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Set<Feature> features = getSelectedFeatures();
				Set<String> plugins = new HashSet<String>();
				for(Feature f : features)
					plugins.add(f.getPlugin());

				//				IStructuredSelection selection = new StructuredSelection(plugins.toArray());
				//				pluginTable.setSelection(selection);

				pluginList.select(plugins);
			}
		});


		MenuItem exclude = new MenuItem(menu, SWT.PUSH);
		exclude.setText("Exclude");
		exclude.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Set<Feature> features = getSelectedFeatures();
				for(Feature f : features)
					if(f.isAbstract())
						excludesList.add(f.getExtensionPointId());
			}
		});

		MenuItem undo = new MenuItem(menu, SWT.PUSH);
		undo.setText("Undo");

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				openPluginXml.setEnabled(!viewer.getSelection().isEmpty());
			}
		});

		viewer.getControl().setMenu(menu);
	}

	private Set<Feature> getSelectedFeatures() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		Set<Feature> features = new HashSet<Feature>();
		Iterator iterator = selection.iterator();
		while(iterator.hasNext()) {
			Feature f = (Feature) iterator.next();
			features.add(f);
		}
		return features;
	}



	private Composite createSettingsArea() {
		Composite settingsArea = new Composite(getContainer(), SWT.NONE);
		settingsArea.setLayout(new GridLayout(2, false));


		new Label(settingsArea, SWT.NONE).setText("Feature id");
		Text featureId = new Text(settingsArea, SWT.BORDER);
		featureId.setText(featureXml.getId());

		new Label(settingsArea, SWT.NONE).setText("Feature name");
		Text featureName = new Text(settingsArea, SWT.BORDER);
		featureName.setText("??");

		new Label(settingsArea, SWT.NONE).setText("Feature version");
		Text featureVersion = new Text(settingsArea, SWT.BORDER);
		featureVersion.setText(featureXml.getVersion());

		new Label(settingsArea, SWT.NONE).setText("Root plugin");
		final Combo pluginsList = new Combo(settingsArea, SWT.NONE);
		for(String id : Platform.getExtensionRegistry().getNamespaces())
			pluginsList.add(id);
		new Label(settingsArea, SWT.NONE).setText("Exclude extension points");
		excludesList = new org.eclipse.swt.widgets.List(settingsArea, SWT.BORDER);
		excludesList.setLayoutData(new GridData(300,200));

		Button removeButton = new Button(settingsArea, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] selection = excludesList.getSelection();
				for(String s : selection)
					excludesList.remove(s);				
			}
		});


		new Label(settingsArea, SWT.NONE).setText("Layout");
		Combo layoutCombo = new Combo(settingsArea, SWT.NONE);
		layoutCombo.add("Tree");
		layoutCombo.add("Radial");

		return settingsArea;
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		System.out.println("save");
	}

	@Override
	public void doSaveAs() {

	}



	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}



	private final class ManifestVisitor implements IResourceVisitor {
		private static final String BUNDLE_ID_HEADER = "Bundle-SymbolicName:"; 

		private final String pluginId;
		private boolean found;

		private ManifestVisitor(String pluginId) {
			this.pluginId = pluginId;
			found = false;
		}

		@Override
		public boolean visit(IResource resource) throws CoreException {
			if(resource instanceof IFolder && !((IFolder) resource).getName().equals("META-INF"))
				return false;

			if(resource instanceof IFile && ((IFile) resource).getName().equals("MANIFEST.MF")) {
				IFile file = (IFile) resource;
				Scanner scanner = new Scanner(file.getContents());
				while(scanner.hasNextLine()) {
					String line = scanner.nextLine().trim();
					if(
							line.startsWith(BUNDLE_ID_HEADER) && 
							line.substring(BUNDLE_ID_HEADER.length()).trim().startsWith(pluginId)) {

						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						page.openEditor(new FileEditorInput(file), "org.eclipse.pde.ui.manifestEditor");
						found = true;
						break;
					}
				}
				scanner.close();
			}
			return !found;
		}
	}		
}
