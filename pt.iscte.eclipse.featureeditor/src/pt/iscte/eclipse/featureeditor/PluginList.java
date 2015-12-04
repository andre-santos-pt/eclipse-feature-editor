package pt.iscte.eclipse.featureeditor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;

import pt.iscte.eclipse.featureeditor.model.FeatureSelection;
import pt.iscte.eclipse.featureeditor.model.FeatureSet;

class PluginList extends Composite {

	private FeatureSet set;
	private FeatureSelection selection;

	private CheckboxTableViewer table;
	private Label totalLabel;
	private Label featLabel;
	private Label featAbsLabel;
	private Label depthLabel;


	PluginList(Composite parent) {
		super(parent, SWT.NONE);
		createPanelFields(this);
		createPanel(this);
	}

	void setInput(FeatureSet set, FeatureSelection selection) {
		Assert.isNotNull(set);
		Assert.isNotNull(selection);

		this.set = set;
		this.selection = selection;

		table.setInput(set.getPlugins());
		updatePanelFields();

		selection.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				boolean check = ((FeatureSelection) o).isPluginSelected((String) arg);
				table.setChecked((String) arg, check);
			}
		});
	}

	private ViewerFilter filter = new ViewerFilter() {
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return selection.isPluginSelected((String) element);
		}
	};
	
	void createPanel(Composite parent) {
		setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true));
		setLayout(new GridLayout(1, false));

		totalLabel = new Label(this, SWT.NONE);
		featLabel = new Label(this, SWT.NONE);
		featAbsLabel = new Label(this, SWT.NONE);
		depthLabel = new Label(this, SWT.NONE);

		final Button checkOnlySelected = new Button(this, SWT.CHECK);
		checkOnlySelected.setText("Show only selected plugins");
		checkOnlySelected.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(checkOnlySelected.getSelection())
					table.addFilter(filter);
				else
					table.removeFilter(filter);
			}
		});

		table = CheckboxTableViewer.newCheckList(this, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);

		table.getControl().setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
		table.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				System.out.println(event);	
			}
		});


		TableViewerColumn pluginColumn = new TableViewerColumn(table, SWT.NONE);
		{
			TableColumn c =	pluginColumn.getColumn();
			c.setWidth(250);
			c.setResizable(false);
			c.setMoveable(false);
			pluginColumn.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return element.toString();
				}
			});
		}

		table.setContentProvider(new ArrayContentProvider());
		
//		table.setContentProvider(new IStructuredContentProvider() {
//
//			//			FeatureSet set;
//			@Override
//			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
//				//				set = (FeatureSet) newInput;
//				//
//				//				if(set != null) {
//				//					set.addObserver(new Observer() {
//				//
//				//						@Override
//				//						public void update(Observable o, Object arg) {
//				//							boolean check = ((FeatureSet) o).isPluginSelected((String) arg);
//				//							pluginTable.setChecked((String) arg, check);
//				//						}
//				//					});
//				//				}
//			}
//
//			@Override
//			public Object[] getElements(Object inputElement) {
//				return set.getPlugins().toArray();
//			}
//
//			@Override
//			public void dispose() {
//
//			}
//		});

		table.addCheckStateListener(new ICheckStateListener() {

			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				String plugin = (String) event.getElement();
				if(event.getChecked())
					selection.selectPlugin(plugin);
				else
					selection.unselectPlugin(plugin);
			}
		});

		table.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();

				List<String> selection = new ArrayList<String>(sel.size());
				Iterator<String> iterator = sel.iterator();
				while(iterator.hasNext())
					selection.add(iterator.next());

				for(SelectionListener l : listeners)
					l.selectionChanged(selection);
			}

		});

	}

	private void createPanelFields(Composite panel) {
		totalLabel = new Label(panel, SWT.NONE);
		featLabel = new Label(panel, SWT.NONE);
		featAbsLabel = new Label(panel, SWT.NONE);
		depthLabel = new Label(panel, SWT.NONE);
	}

	private void updatePanelFields() {
		totalLabel.setText("Plugins: " + set.getPlugins().size());
		int concreteFeaturesCount = set.totalConcreteFeatures();
		featLabel.setText("Concrete Features: " + concreteFeaturesCount);
		int abstractFeaturesCount = set.totalAbstractFeatures();
		featAbsLabel.setText("Abstract Features: " + abstractFeaturesCount);
		depthLabel.setText("Depth: " + set.getDepth());
	}

	public void select(String pluginId) {
		Assert.isNotNull(pluginId);
		IStructuredSelection selection = new StructuredSelection(pluginId);
		table.setSelection(selection);
	}

	public void select(Set<String> pluginIds) {
		Assert.isNotNull(pluginIds);
		IStructuredSelection selection = new StructuredSelection(pluginIds.toArray());
		table.setSelection(selection);
	}

	public void addSelectionListener(SelectionListener listener) {
		Assert.isNotNull(listener);
		listeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		Assert.isNotNull(listener);
		listeners.remove(listener);
	}

	private List<SelectionListener> listeners = new ArrayList<PluginList.SelectionListener>();

	interface SelectionListener {
		void selectionChanged(List<String> selectedPlugins);
	}
}
