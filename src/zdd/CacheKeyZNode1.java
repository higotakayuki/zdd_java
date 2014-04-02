package zdd;

public class CacheKeyZNode1 {
	private final ZNode n1;

	public CacheKeyZNode1(ZNode n1) {
		this.n1 = n1;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public int hashCode() {
		return n1.hashCode();
	}

}
