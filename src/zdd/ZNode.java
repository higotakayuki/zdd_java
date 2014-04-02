package zdd;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

public class ZNode implements Comparable<ZNode> {

	private static final ZNode top = new ZNode(Integer.MIN_VALUE, null, null,
			ZNodeType.Top);
	private static final ZNode bottom = new ZNode(Integer.MIN_VALUE, null,
			null, ZNodeType.Bottom);

	private static final UTable utable = new UTable();

	public static void gc() {
		System.gc();
		utable.gc();
	}

	public static int cacheSize() {
		return utable.size();
	}

	public enum ZNodeType {
		Bottom, Top, NonTerminal
	}

	private final ZNodeType type;
	// level‚ÍInteger.MIN_VALUE‚æ‚è‘å‚«‚¢’l
	public final int level;
	public final ZNode low;
	public final ZNode high;


	private ZNode(int level, ZNode low, ZNode high) {
		this.level = level;
		this.low = low;
		this.high = high;

		type = ZNodeType.NonTerminal;
	}

	private ZNode(int level, ZNode low, ZNode high, ZNodeType type) {
		this.level = level;
		this.low = low;
		this.high = high;
		this.type = type;
	}

	public static ZNode getNode(int level, ZNode low, ZNode high) {
		// node elimination
		if (high == bottom)
			return low;
		Optional<ZNode> node = utable.getNode(level, low, high);
		if (node.isPresent())
			return node.get();
		else {
			ZNode newNode = new ZNode(level, low, high);
			utable.put(newNode);
			return newNode;
		}
		// cache check
		// ZNode node = utable.getNode(level, low, high);
		// if (node == null) {
		// node = new ZNode(level, low, high);
		// utable.put(node);
		// }
		// return node;
	}

	public static ZNode getEmpty() {
		return bottom;
	}

	public static ZNode getBase() {
		return top;
	}

	public static ZNode getBase(List<Integer> levels) {
		if (levels.size() == 0) {
			return top;
		}

		Collections.sort(levels);
		int v = levels.get(0);
		ZNode tmpNode = getNode(v, top, top);
		for (int i = 1; i < levels.size(); i++) {
			v = levels.get(i);
			tmpNode = getNode(v, tmpNode, tmpNode);
		}
		return tmpNode;
	}

	public static ZNode genSinglePathZDD(Set<Integer> set) {
		if (set.size() == 0)
			return bottom;
		ArrayList<Integer> list = new ArrayList<>(set);
		Collections.sort(list);
		ZNode tmp = getNode(list.get(0), bottom, top);
		for (int i = 1; i < list.size(); i++) {
			tmp = getNode(list.get(i), bottom, tmp);
		}
		return tmp;
	}

	public static ZNode genChainZDD(Set<ValueState> values) {
		if (values.size() == 0)
			return bottom;
		ArrayList<ValueState> list = new ArrayList<>(values);
		Collections.sort(list, new Comparator<ValueState>() {
			@Override
			public int compare(ValueState o1, ValueState o2) {
				return o1.value - o2.value;
			}
		});
		ZNode tmp = top;
		for (ValueState vs : list) {
			switch (vs.state) {
			case ZERO:
				tmp = getNode(vs.value, tmp, bottom);
				break;
			case ONE:
				tmp = getNode(vs.value, bottom, tmp);
				break;
			case ANY:
				tmp = getNode(vs.value, tmp, tmp);
				break;
			}
		}
		return tmp;
	}

	public boolean isBottom() {
		return this.type == ZNodeType.Bottom;
	}

	public boolean isTop() {
		return this.type == ZNodeType.Top;
	}

	public boolean isNonTerminal() {
		return this.type == ZNodeType.NonTerminal;
	}

	@Override
	public int compareTo(ZNode o) {
		int diff = this.type.compareTo(o.type);
		if (diff == 0 && this.type == ZNodeType.NonTerminal) {
			int diff2 = this.level - o.level;
			if (diff2 == 0) {
				int diff3 = this.high.compareTo(o.high);
				if (diff3 == 0)
					return this.low.compareTo(o.low);
				else
					return diff3;
			} else {
				return diff2;
			}
		} else {
			return diff;
		}
	}

	@Override
	public String toString() {
		if (type == ZNodeType.Bottom)
			return "Bottom";
		else if (type == ZNodeType.Top)
			return "Top";
		else
			return level + "|" + low.level + "," + high.level;
	}

	public Set<ZNode> enumNodes() {
		Set<ZNode> set = new LinkedHashSet<ZNode>();
		this.enumNodes(set);
		return set;
	}

	private void enumNodes(Set<ZNode> set) {
		set.add(this);
		if (low != null && !set.contains(low))
			low.enumNodes(set);
		if (high != null && !set.contains(high))
			high.enumNodes(set);
	}

	public List<List<Integer>> enumPaths() {
		Map<ZNode, List<List<Integer>>> cache = new HashMap<>();
		return this.enumPaths(cache);
	}

	private List<List<Integer>> enumPaths(Map<ZNode, List<List<Integer>>> cache) {
		List<List<Integer>> result = new ArrayList<>();
		if (isBottom()) {
			// nop;
			return result;
		} else if (isTop()) {
			result.add(new LinkedList<Integer>());
			return result;
		} else {
			if (cache.containsKey(this))
				return cache.get(this);

			result.addAll(low.enumPaths());
			List<List<Integer>> tmpResult1 = high.enumPaths(cache);
			for (int i = 0; i < tmpResult1.size(); i++) {
				List<Integer> list = new ArrayList<Integer>(tmpResult1.get(i));
				list.add(level);
				result.add(list);
			}
		}
		return result;

	}

	public void printAllSets() {
		List<List<Integer>> result = enumPaths();
		Collections.sort(result, (List<Integer> o1, List<Integer> o2) -> {
			int diff = o1.size() - o2.size();
			if (diff != 0)
				return diff;
			Iterator<Integer> it1 = o1.iterator();
			Iterator<Integer> it2 = o2.iterator();
			while (it1.hasNext()) {
				int v1 = it1.next();
				int v2 = it2.next();
				diff = v1 - v2;
				if (diff != 0)
					return diff;
			}
			return 0;
		});
		System.out.print("{");
		for (List<Integer> list : result) {
			System.out.print("{");
			for (int i = 0; i < list.size(); i++) {
				if (i != 0)
					System.out.print(",");
				System.out.print(list.get(i));
			}
			System.out.print("}");
		}
		System.out.println("}");
	}

	public int nodeCount() {
		int counter = 0;
		HashSet<ZNode> finish = new HashSet<>();
		Stack<ZNode> stack = new Stack<>();
		DFS: for (ZNode tNode = this;;) {
			counter++;
			finish.add(tNode);
			if (tNode.low != null)
				stack.push(tNode.low);
			if (tNode.high != null)
				stack.push(tNode.high);
			do {
				if (stack.isEmpty())
					break DFS;
				tNode = stack.pop();
			} while (finish.contains(tNode));
		}
		return counter;
	}

	public BigInteger onePathCount() {
		Map<ZNode, BigInteger> cache = new HashMap<>();
		return onePathCount(cache);
	}

	private BigInteger onePathCount(Map<ZNode, BigInteger> cache) {
		if (this.isBottom())
			return BigInteger.ZERO;
		if (this.isTop())
			return BigInteger.ONE;

		if (cache.containsKey(this))
			return cache.get(this);
		BigInteger result = null;
		if (low == high)
			result = low.onePathCount(cache).multiply(BigInteger.valueOf(2));
		else
			result = low.onePathCount(cache).add(high.onePathCount(cache));
		cache.put(this, result);
		return result;
	}

	public ZNode subset0(int value) {
		Map<CacheKeyZNode1, ZNode> cache = new HashMap<>();
		return subset0(value, cache);
	}

	private ZNode subset0(int value, Map<CacheKeyZNode1, ZNode> cache) {
		if (this.level < value)
			return this;
		if (this.level == value)
			return this.low;
		CacheKeyZNode1 key = new CacheKeyZNode1(this);
		if (cache.containsKey(key))
			return cache.get(key);
		ZNode result = getNode(this.level, this.low.subset0(value, cache),
				this.high.subset0(value, cache));
		cache.put(key, result);
		return result;
	}

	public ZNode subset1(int value) {
		Map<CacheKeyZNode1, ZNode> cache = new HashMap<>();
		return subset1(value, cache);
	}

	private ZNode subset1(int value, Map<CacheKeyZNode1, ZNode> cache) {
		if (this.level < value)
			return bottom;
		else if (this.level == value)
			return this.high;
		CacheKeyZNode1 key = new CacheKeyZNode1(this);
		if (cache.containsKey(key))
			return cache.get(key);
		ZNode result = getNode(this.level, this.low.subset1(value, cache),
				this.high.subset1(value, cache));
		cache.put(key, result);
		return result;
	}

	public ZNode change(int value) {
		Map<CacheKeyZNode1, ZNode> cache = new HashMap<>();
		return change(value, cache);
	}

	private ZNode change(int value, Map<CacheKeyZNode1, ZNode> cache) {
		if (this.level < value)
			return getNode(value, bottom, this);
		if (this.level == value)
			return getNode(value, this.high, this.low);
		CacheKeyZNode1 key = new CacheKeyZNode1(this);
		if (cache.containsKey(key))
			return cache.get(key);
		ZNode result = getNode(this.level, low.change(value),
				high.change(value));
		return result;
	}

	// “ñ€‰‰ŽZ
	public ZNode union(ZNode q) {
		Map<CacheKeyZNode2Exchangable, ZNode> cache = new HashMap<>();
		return union(this, q, cache);
	}

	private static ZNode union(ZNode p, ZNode q,
			Map<CacheKeyZNode2Exchangable, ZNode> cache) {
		if (p == bottom)
			return q;
		if (q == bottom)
			return p;
		if (p == q)
			return p;

		CacheKeyZNode2Exchangable key = new CacheKeyZNode2Exchangable(p, q);
		if (cache.containsKey(key))
			return cache.get(key);
		ZNode result = null;
		if (p.level > q.level)
			result = getNode(p.level, union(p.low, q, cache), p.high);
		else if (p.level < q.level)
			result = getNode(q.level, union(p, q.low, cache), q.high);
		else
			result = getNode(p.level, union(p.low, q.low, cache),
					union(p.high, q.high, cache));// if(p.value==q.value)
		cache.put(key, result);
		return result;

	}

	public ZNode intersection(ZNode q) {
		Map<CacheKeyZNode2Exchangable, ZNode> cache = new HashMap<>();
		return intersection(this, q, cache);
	}

	private static ZNode intersection(ZNode p, ZNode q,
			Map<CacheKeyZNode2Exchangable, ZNode> cache) {
		if (p == bottom)
			return bottom;
		if (q == bottom)
			return bottom;
		if (p == q)
			return p;

		CacheKeyZNode2Exchangable key = new CacheKeyZNode2Exchangable(p, q);
		if (cache.containsKey(key))
			return cache.get(key);
		ZNode result = null;
		if (p.level > q.level)
			result = intersection(p.low, q, cache);
		else if (p.level < q.level)
			result = intersection(p, q.low, cache);
		else
			result = getNode(p.level, intersection(p.low, q.low, cache),
					intersection(p.high, q.high, cache));// if(p.value==q.value)
		cache.put(key, result);
		return result;

	}

	public ZNode dotProduct(ZNode q) {
		Map<CacheKeyZNode2Exchangable, ZNode> cache = new HashMap<>();
		return dotProduct(this, q, cache);
	}

	private static ZNode dotProduct(ZNode p, ZNode q,
			Map<CacheKeyZNode2Exchangable, ZNode> cache) {
		if (p == top)
			return q;
		if (q == top)
			return p;
		if (p == bottom && q == bottom)
			return bottom;
		CacheKeyZNode2Exchangable key = new CacheKeyZNode2Exchangable(p, q);
		if (cache.containsKey(key))
			return cache.get(key);
		ZNode result = null;
		if (p.level > q.level)
			result = getNode(p.level, dotProduct(p.low, q, cache),
					dotProduct(p.high, q, cache));
		else if (p.level < q.level)
			result = getNode(q.level, dotProduct(p, q.low, cache),
					dotProduct(p, q.high, cache));
		else if (p.level == q.level) {
			ZNode oneNode = dotProduct(p.high, q.high, cache).union(
					dotProduct(p.high, q.low, cache)).union(
					dotProduct(p.low, q.high, cache));
			result = getNode(p.level, dotProduct(p.low, q.low, cache), oneNode);
		}
		return result;
	}

	public ZNode diff(ZNode q) {
		Map<CacheKeyZNode2, ZNode> cache = new HashMap<>();
		return diff(this, q, cache);
	}

	private static ZNode diff(ZNode p, ZNode q, Map<CacheKeyZNode2, ZNode> cache) {
		if (p == bottom)
			return bottom;
		if (q == bottom)
			return p;
		if (p == q)
			return bottom;

		CacheKeyZNode2 key = new CacheKeyZNode2(p, q);
		if (cache.containsKey(key))
			return cache.get(key);
		ZNode result = null;
		if (p.level > q.level)
			result = getNode(p.level, diff(p.low, q, cache), p.high);
		else if (p.level < q.level)
			result = diff(p, q.low, cache);
		else
			result = getNode(p.level, diff(p.low, q.low, cache),
					diff(p.high, q.high, cache));// if(p.value==q.value)
		cache.put(key, result);
		return result;
	}
}
