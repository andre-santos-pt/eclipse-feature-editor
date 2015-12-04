package pt.iscte.eclipse.featureeditor;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IEntityConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IEntityStyleProvider;
import org.eclipse.zest.core.viewers.IFigureProvider;
import org.eclipse.zest.core.viewers.ISelfStyleProvider;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;

class StyleProvider extends LabelProvider implements IEntityStyleProvider, IEntityConnectionStyleProvider, ISelfStyleProvider, IFigureProvider  {
		final Color BLACK = new Color(null, 0, 0, 0);
		final Color WHITE = new Color(null, 255, 255, 255);
		final Color ABSTRACT = new Color(null, 255, 0, 0);
		final Color SELECTED = new Color(null, 230, 230, 230);
		final Color HIGHLIGHT = new Color(null, 0, 120, 230);
	
		
		final Font NORMAL_FONT = new Font(null, "Arial", 12, SWT.NORMAL);
		final Font ITALIC_FONT = new Font(null, "Arial", 12, SWT.ITALIC);
		final Font BOLD_FONT = new Font(null, "Arial", 14, SWT.BOLD);
		
		private GraphViewer viewer;
		
		public StyleProvider(GraphViewer viewer) {
			this.viewer = viewer;
		}
		
		@Override
		public String getText(Object element) {
			if(element instanceof Feature)
				return ((Feature) element).getName();
			else
				return null;
		}

		@Override
		public Color getNodeHighlightColor(Object entity) {
			return HIGHLIGHT;
		}

		@Override
		public Color getBorderColor(Object entity) {
			return BLACK;
		}

		@Override
		public Color getBorderHighlightColor(Object entity) {
			return BLACK;
		}

		@Override
		public int getBorderWidth(Object entity) {
			return 1;
		}

		@Override
		public Color getBackgroundColour(Object entity) {
			Feature f = (Feature) entity;
			if(f.isSelected())
				return SELECTED;
			else
				return WHITE;
		}

		@Override
		public Color getForegroundColour(Object entity) {
//			Feature f = (Feature) entity;
//			if(f.isAbstract())
//				return ABSTRACT;
//			else
				return BLACK;
		}

		@Override
		public IFigure getTooltip(Object entity) {
			if(entity instanceof Feature)
				return new Label(((Feature) entity).getPlugin());
			else
				return new Label("optional/mandatory");
		}

		@Override
		public boolean fisheyeNode(Object entity) {
			return false;
		}

		
		
		
		//IConnectionStyleProvider
//		@Override
//		public int getConnectionStyle(Object rel) {
//			return ZestStyles.CONNECTIONS_SOLID;
//		}
//
//		@Override
//		public Color getColor(Object rel) {
//			return new Color(null, 0, 0, 0);
//		}
//
//		@Override
//		public Color getHighlightColor(Object rel) {
//			return new Color(null, 200, 200, 200);
//		}
//
//		@Override
//		public int getLineWidth(Object rel) {
//			return 1;
//		}

		//IFigureProvider
//		@Override
//		public IFigure getFigure(Object element) {
//			Feature f = (Feature) element;
//			RectangleFigure r = new RectangleFigure();
//			r.add(new Label(f.getName()));
//			return r;
//		}

		
		
		@Override
		public int getConnectionStyle(Object src, Object dest) {
//			if(((Feature) dest).isMandatory())
				return ZestStyles.CONNECTIONS_SOLID;
//			else
//				return ZestStyles.CONNECTIONS_DASH;
		}

		@Override
		public Color getColor(Object src, Object dest) {
			return BLACK;
		}

		@Override
		public Color getHighlightColor(Object src, Object dest) {
			return HIGHLIGHT;
		}

		@Override
		public int getLineWidth(Object src, Object dest) {
			return 1;
		}

//		@Override
		public IFigure getFigure(Object element) {
			final Feature feature = (Feature) element;
			final RectangleFigure r = new RectangleFigure();
			r.setSize(feature.isRoot() ? 200 : 150, feature.isRoot() ? 30 : 25);
			r.setLineWidth(feature.isRoot() ? 3 : 1);
			
			Label l = new Label(feature.getName());
			l.setSize(feature.isRoot() ? 200: 150, feature.isRoot() ? 30 : 25);
			l.setLocation(new Point(3, 1));
			l.setBackgroundColor(ABSTRACT);			
			l.setFont(feature.isAbstract() ? ITALIC_FONT : feature.isRoot() ? BOLD_FONT : NORMAL_FONT);
			r.add(l);
			if(feature.isAbstract()) {
				r.setLineStyle(SWT.LINE_DASH);
				r.setLineWidth(2);
			}
			String text = feature.getName() + "\nOwner: " + feature.getPlugin();
			if(feature.isAbstract())
				text += "\n" + "Extension point: " + feature.getExtensionPointId();
			
			r.setToolTip(new Label(text));
		
			viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection s = (IStructuredSelection) event.getSelection();
					if(s.toList().contains(feature)) {
						r.setBackgroundColor(HIGHLIGHT);
						r.setForegroundColor(WHITE);
					}
					else {
						if(feature.isSelected())
							r.setBackgroundColor(SELECTED);
						else
							r.setBackgroundColor(WHITE);
						
						r.setForegroundColor(BLACK);
					}
}
			});
//			Ellipse e = new Ellipse();
//			e.setSize(20, 20);
//			e.setLocation(new Point(-10,0));
//			r.add(e);
//			r.set
//			new Anchor(r);
			return r;
		}

		
		
		
		@Override
		public void selfStyleConnection(Object element, GraphConnection connection) {
			Ellipse e = new Ellipse();
			e.setSize(30, 30);
			e.setBackgroundColor(ABSTRACT);
			EntityConnectionData eConn = (EntityConnectionData) element;
			Feature dest = (Feature) eConn.dest;
			Connection fig = connection.getConnectionFigure();
			PolylineConnection conn = (PolylineConnection) fig;
//			PolylineDecoration dec = new PolylineDecoration();
			conn.setSourceDecoration(new FeatureCircle(dest, fig));
			fig.setToolTip(new Label("??"));
		}

		@Override
		public void selfStyleNode(Object element, GraphNode node) {
//			Ellipse e = new Ellipse();
//			e.setSize(40, 40);
//			e.setBackgroundColor(BLACK);
//			e.setLocation(new Point(50, 0));
			
			Feature feature = (Feature) element;
			String text = "Owner: " + feature.getPlugin();
			if(feature.isAbstract())
				text += "\n" + "Extension point: " + feature.getExtensionPointId();
			
//			Label label = (Label) node.getNodeFigure();
//			label.setBackgroundColor(WHITE);
//			node.setTooltip(new Label(text));
////			System.out.println(getClass());
//			if(feature.isAbstract())
//				label.setFont(ITALIC_FONT);
//			
////				label.setLineStyle(SWT.LINE_DASH);
		}
		
		class FeatureCircle extends Ellipse implements RotatableDecoration {

			Connection c;
			
			FeatureCircle(Feature feature, Connection c) {
				this.c = c;
				setSize(10, 10);
				setBackgroundColor(feature.isMandatory() ? BLACK : WHITE);
			}
			
			@Override
			public void setReferencePoint(Point p) {
			
//				Point s = c.getSourceAnchor().getLocation(p).getCopy();
//				s.setX(s.x - c.getTargetAnchor().getLocation(p).x);
//				s.setY(s.y - c.getTargetAnchor().getLocation(p).y);
//				
//				double d = Math.atan2(p.y, p.x);
//				int angle = (int) Math.round(Math.toDegrees(d));
//				System.out.println((angle/10)-5);
//				Point t = c.getTargetAnchor().getLocation(p);
//				t.setX(t.x-5);
//				t.setY(t.y-5);
//				setLocation(t);
				
				Point p1 = c.getTargetAnchor().getLocation(p).getCopy();
//				p1.x -= 5;
				
				Point p2 = c.getSourceAnchor().getLocation(p).getCopy();
//				p2.y -= 5;
				
				double deltaX = p2.x - p1.x;
				double deltaY = p2.y - p1.y;

				// now you know how much far they are
				double coeff = 5 / Math.sqrt(deltaX*deltaX + deltaY*deltaY); 
				//this coefficient can be tweaked to decice how much near the two points will be after the update.. 0.5 = 50% of the previous distance

				setLocation(new Point((int) (p1.x + coeff*deltaX)-4, (int) (p1.y + coeff*deltaY) -4));
				
				//				setLocation(new Point(p.x+(angle/10)-5,p.y-9));
				
//				Point pt = Point.SINGLETON;
//				pt.setLocation(ref);
//				pt.negate().translate(location);
//				setRotation(Math.atan2(pt.y, pt.x));
			}
			
		}
		
//		class Anchor extends AbstractConnectionAnchor {
//
//			Anchor(IFigure f) {
//				super(f);
//			}
//			
//			@Override
//			public Point getLocation(Point reference) {
//				return new Point(10, 0);
//			}
//
//
//			@Override
//			public Point getReferencePoint() {
//				return new Point(0, 0);
//			}
//			
//		}
	}