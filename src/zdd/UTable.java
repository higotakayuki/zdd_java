package zdd;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import zdd.ZNode.ZNodeType;

public class UTable {

	// 一意の検索キーを作るのは難しいので、キーが衝突した場合に備えてリストに保存
	private final Map<Key, Entry> table = new HashMap<>();

	private class Key {
		public final int level;
		public final ZNode low;
		public final ZNode high;

		public Key(int level, ZNode low, ZNode high) {
			this.level = level;
			this.low = low;
			this.high = high;
		}

		@Override
		public int hashCode() {
			return level ^ low.level ^ high.level;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Key) {
				Key tar = (Key) obj;
				return tar.level == this.level && tar.low == this.low
						&& tar.high == this.high;
			}
			return false;
		}

		@Override
		public String toString() {
			return level + "|" + low.level + "," + high.level;
		}
	}

	// get()を呼んでnullが返ってきたら、utableからWeakReferenceを除外
	private class Entry extends WeakReference<ZNode> {
		public final Key key;

		public Entry(ZNode node) {
			super(node, queue);
			this.key = new Key(node.level, node.low, node.high);
		}
	}

	private ReferenceQueue<ZNode> queue = new ReferenceQueue<>();

	public void put(ZNode node) {
		expungeStaleEntries();
		if (!table.containsKey(node)) {
			Entry ref = new Entry(node);
			table.put(new Key(node.level, node.low, node.high), ref);
		}
	}

	public Optional<ZNode> getNode(int level, ZNode low, ZNode high) {
		Entry ent = table.get(new Key(level, low, high));
		if (ent == null)
			return Optional.empty();

		ZNode node = ent.get();
		if (node == null) {
			table.remove(ent);
			return Optional.empty();
		} else {
			return Optional.of(node);
		}
	}

	private void expungeStaleEntries() {
		Entry e = null;
		while ((e = (Entry) queue.poll()) != null) {
			table.remove(e.key);
		}
	}

	public void gc() {
		expungeStaleEntries();
	}

	public int size() {
		return table.size();
	}
}
