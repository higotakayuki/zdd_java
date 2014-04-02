package zdd;

public class CacheKeyZNode2Exchangable {
	private final ZNode n1;
	private final ZNode n2;

	public CacheKeyZNode2Exchangable(ZNode n1, ZNode n2) {
		this.n1 = n1;
		this.n2 = n2;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof CacheKeyZNode2Exchangable)) return false;
		CacheKeyZNode2Exchangable o2 =
				(CacheKeyZNode2Exchangable) obj;
		if ((n1 == o2.n1 && n2 == o2.n2)
				|| (n1 == o2.n2 && n2 == o2.n1)) return true;
		return false;
	}

	@Override
	public int hashCode() {
		n1.hashCode();
		n2.hashCode();
		return n1.hashCode() ^ n2.hashCode();
	}

}
