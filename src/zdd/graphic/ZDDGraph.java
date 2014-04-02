package zdd.graphic;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import zdd.ZNode;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class ZDDGraph {

	public static void main(String[] args) throws IOException, InterruptedException {
		Integer[] array = { 1, 2, 3, 4, 5 };
		List<Integer> list = Arrays.asList(array);
		ZNode t0 = ZNode.getBase(list);
		ZNode t1 = ZNode.getBase();
		t1 = t1.change(3);
		t0 = t0.diff(t1);
		t1 = t1.change(2);
		t0 = t0.diff(t1);
		ZDDGraph zddg = new ZDDGraph(300, 300);
		zddg.showGraph(t0);
		//Thread.sleep(1000);
		//zddg.showGraph(t1);
		t0.printAllSets();
		System.out.println(t0.onePathCount());
	}

	private Graph<ZNode, Edge> g;
	private ZNode root;
	private final Dimension size;
	private final JFrame frame = new JFrame("ZDD Graph");
	private VisualizationViewer<ZNode, Edge> panel = null;

	private float dash[] = { 5f };
	private final Stroke normalStroke = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
	private final Stroke dotStroke = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
			dash, 0.0f);
	private final Stroke wstroke = new WStroke(3.0f, 1.0f);

	public ZDDGraph(int width, int height) {
		this.size = new Dimension(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	private void constructGraph() {
		g = new DirectedSparseMultigraph<ZNode, Edge>();
		HashSet<ZNode> finish = new HashSet<>();
		Stack<ZNode> stack = new Stack<>();
		DFS: for (ZNode tNode = root;;) {
			g.addVertex(tNode);
			if (tNode.low == tNode.high && tNode.low != null) {
				g.addEdge(new Edge(EdgeType.both), tNode, tNode.low);
				stack.push(tNode.low);
			} else {
				if (tNode.low != null && !tNode.low.isBottom()) {
					g.addEdge(new Edge(EdgeType.low), tNode,
							tNode.low);
					stack.push(tNode.low);
				}
				if (tNode.high != null && !tNode.high.isBottom()) {
					g.addEdge(new Edge(EdgeType.high), tNode,
							tNode.high);
					stack.push(tNode.high);
				}
			}

			finish.add(tNode);
			do {
				if (stack.isEmpty()) break DFS;
				tNode = stack.pop();
			} while (finish.contains(tNode));
		}
	}

	private TreeMap<Integer, List<ZNode>> getLayers() {
		Set<ZNode> nodes = root.enumNodes();
		TreeMap<Integer, List<ZNode>> layers = new TreeMap<>();
		for (ZNode n : nodes) {
			if (n.isBottom()) continue;
			if (!layers.containsKey(n.level)) layers.put(n.level,
					new ArrayList<ZNode>());
			List<ZNode> list = layers.get(n.level);
			list.add(n);
		}
		return layers;
	}

	public void showGraph(ZNode root) {
		this.root=root;
		constructGraph();
		// ëSóÒãì
		TreeMap<Integer, List<ZNode>> layers = getLayers();
		NavigableSet<Integer> keys = layers.navigableKeySet();

		// èÄîı
		// Layout<ZNode, Edge> layout = new DAGLayout<>(g);
		Layout<ZNode, Edge> layout = new StaticLayout<>(g);

		layout.setSize(size);

		// à íuåàÇﬂ
		int deltaY = size.height / (keys.size() + 1);
		Iterator<Integer> it = keys.descendingIterator();
		for (int i = 0; i < keys.size(); i++) {
			Integer key = it.next();
			List<ZNode> list = layers.get(key);
			int deltaX = size.width / (list.size() + 1);
			for (int j = 0; j < list.size(); j++) {
				layout.setLocation(list.get(j), new Point2D.Double(
						deltaX * (j + 1), deltaY * (i + 1)));
				// layout.setLocation(list.get(j), new Point2D.Double(200,200));
			}
		}

		if (panel != null) frame.getContentPane().remove(panel);
		panel = new VisualizationViewer<>(layout, size);

		panel.getRenderContext().setEdgeShapeTransformer(
				new EdgeShape.Line<ZNode, Edge>());
		Transformer<ZNode, String> labeller =
				new Transformer<ZNode, String>() {
					@Override
					public String transform(ZNode n) {
						if (n.isNonTerminal()) return "" + n.level;
						// else if (n.type == Type.Bottom) return "F";
						else if (n.isTop()) return "T";
						else return "";
					}
				};
		panel.getRenderContext().setVertexLabelTransformer(labeller);
		panel.getRenderer().getVertexLabelRenderer()
				.setPosition(Position.CNTR);

		Transformer<ZNode, Shape> nodeShapeTransformer =
				new Transformer<ZNode, Shape>() {
					@Override
					public Shape transform(ZNode n) {
						if (n.isNonTerminal()) return new Rectangle(
								-12, -12, 24, 24);
						else return new Ellipse2D.Double(-12, -12,
								24, 24);
					}
				};
		panel.getRenderContext().setVertexShapeTransformer(
				nodeShapeTransformer);

		Transformer<Edge, Stroke> edgeStrokeTransformer =
				new Transformer<Edge, Stroke>() {
					@Override
					public Stroke transform(Edge e) {
						if (e.type == EdgeType.low) return dotStroke;
						else if (e.type == EdgeType.high) return normalStroke;
						else if (e.type == EdgeType.both) return wstroke;
						else return null;
					}
				};
		panel.getRenderContext().setEdgeStrokeTransformer(
				edgeStrokeTransformer);

		panel.getRenderContext().setEdgeShapeTransformer(
				new EdgeShape.Line<ZNode, Edge>());

		DefaultModalGraphMouse<ZNode, String> gm =
				new DefaultModalGraphMouse<ZNode, String>();
		gm.setMode(ModalGraphMouse.Mode.PICKING);
		panel.setGraphMouse(gm);

		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
	}

	public enum EdgeType {
		low, high, both,
	};

	private class Edge {
		public EdgeType type;

		public Edge(EdgeType type) {
			this.type = type;
		}
	}

	public class WStroke implements Stroke {

		private float width1;
		private float width2;

		private Stroke createStroke(float width) {
			return new BasicStroke(width);
		}

		public WStroke(float width1, float width2) {
			this.width1 = width1;
			this.width2 = width2;
		}

		public Shape createStrokedShape(Shape s) {
			Shape out = createStroke(width1).createStrokedShape(s);
			return createStroke(width2).createStrokedShape(out);
		}
	}

}
