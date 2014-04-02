package zdd.test;

import java.util.HashSet;
import java.util.Set;

import zdd.ValueState;
import zdd.ZNode;
import zdd.ValueState.State;
import zdd.graphic.ZDDGraph;

public class Test {
	public static void main(String args[]) {
		ZDDGraph zddg = new ZDDGraph(1500, 900);

		ZNode tmpNode1 = MUT(1);
		ZNode tmpNode2 = MUT(2);

		ZNode tmpNode4 = MDT(1);
		ZNode tmpNode5 = MDT(2);

		System.out.println(tmpNode1.onePathCount());
		ZNode result = tmpNode1.intersection(tmpNode2).intersection(tmpNode4)
				.intersection(tmpNode5).subset1(1);
		zddg.showGraph(result);

		System.out.printf("メモリー：%d/%d\n",
				(Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
						.freeMemory()) / (1024 * 1024), Runtime.getRuntime()
						.maxMemory() / (1024 * 1024));
		System.out.println("キャッシュサイズ:" + ZNode.cacheSize());

		// ZNode.gc();
		// System.out.println("GC DONE");
		//
		// System.out.printf("メモリー：%d/%d\n",
		// (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
		// .freeMemory()) / (1024 * 1024), Runtime.getRuntime()
		// .maxMemory() / (1024 * 1024));
		// System.out.println("キャッシュサイズ:" + ZNode.cacheSize());

		tmpNode1 = null;
		tmpNode2 = null;
		tmpNode4 = null;
		tmpNode5 = null;
		result = null;

		while (ZNode.cacheSize() != 0) {
			ZNode.gc();
			System.out.println("GC DONE");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.printf("メモリー：%d/%d\n", (Runtime.getRuntime()
					.totalMemory() - Runtime.getRuntime().freeMemory())
					/ (1024 * 1024), Runtime.getRuntime().maxMemory()
					/ (1024 * 1024));
			System.out.println("キャッシュサイズ:" + ZNode.cacheSize());
		}

	}

	public static ZNode MUT(int kmax) {
		Set<ValueState> fullSet = new HashSet<>();
		for (int i = 1; i <= 24; i++) {
			fullSet.add(new ValueState(i, State.ANY));
		}
		ZNode tmpNode = ZNode.genChainZDD(fullSet);
		System.out.println(tmpNode.onePathCount());

		OUTER: for (int i = 1; i <= 24; i++) {
			Set<ValueState> set = new HashSet<>();
			for (int t = 1; t <= 24; t++) {
				if (t == i) {
					if (24 < t + 2 + kmax)
						break OUTER;
					ValueState vs1 = new ValueState(t++, State.ZERO);
					for (int k = 0; k < kmax; k++) {
						ValueState vs2 = new ValueState(t++, State.ONE);
						set.add(vs2);
					}
					ValueState vs3 = new ValueState(t, State.ZERO);
					set.add(vs1);
					set.add(vs3);
					continue;
				} else {
					set.add(new ValueState(t, State.ANY));
				}
			}
			tmpNode = tmpNode.diff(ZNode.genChainZDD(set));
		}
		return tmpNode;
	}

	public static ZNode MDT(int kmax) {
		Set<ValueState> fullSet = new HashSet<>();
		for (int i = 1; i <= 24; i++) {
			fullSet.add(new ValueState(i, State.ANY));
		}
		ZNode tmpNode = ZNode.genChainZDD(fullSet);
		System.out.println(tmpNode.onePathCount());

		OUTER: for (int i = 1; i <= 24; i++) {
			Set<ValueState> set = new HashSet<>();
			for (int t = 1; t <= 24; t++) {
				if (t == i) {
					if (24 < t + 2 + kmax)
						break OUTER;
					ValueState vs1 = new ValueState(t++, State.ONE);
					for (int k = 0; k < kmax; k++) {
						ValueState vs2 = new ValueState(t++, State.ZERO);
						set.add(vs2);
					}
					ValueState vs3 = new ValueState(t, State.ONE);
					set.add(vs1);
					set.add(vs3);
					continue;
				} else {
					set.add(new ValueState(t, State.ANY));
				}
			}
			tmpNode = tmpNode.diff(ZNode.genChainZDD(set));
		}
		return tmpNode;
	}
}
