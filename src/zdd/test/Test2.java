package zdd.test;

import java.util.HashSet;
import java.util.Set;

import zdd.ZNode;
import zdd.graphic.ZDDGraph;

public class Test2 {
	@SuppressWarnings("serial")
	public static void main(String[] args) {
		Set<Integer> set1 = new HashSet<Integer>() {
			{
				add(5);
			}
		};

		Set<Integer> set2 = new HashSet<Integer>() {
			{
				add(5);
				add(4);
//				add(3);
			}
		};

		Set<Integer> set3 = new HashSet<Integer>() {
			{
				add(3);
			}
		};

		Set<Integer> set4 = new HashSet<Integer>() {
			{
				add(2);
				add(1);
			}
		};
		Set<Integer> set5 = new HashSet<Integer>() {
			{
				add(0);
			}
		};

		ZNode node1 = ZNode.genSinglePathZDD(set1);
		ZNode node2 = ZNode.genSinglePathZDD(set2);
		ZNode node3 = ZNode.genSinglePathZDD(set3);
		ZNode node4 = ZNode.genSinglePathZDD(set4);
		ZNode node5 = ZNode.genSinglePathZDD(set5);

		ZNode node12 = node1.union(node2).union(node5);
		ZNode node34 = node3.union(node4);
		node12.printAllSets();
		node34.printAllSets();
		ZNode tmp = node12.dotProduct(node34);
		tmp.printAllSets();
		System.out.println(tmp.onePathCount());
		tmp=tmp.dotProduct(tmp);
		tmp.printAllSets();
		System.out.println(tmp.onePathCount());
		ZDDGraph g=new ZDDGraph(500, 500);
		g.showGraph(tmp);
	}
}
