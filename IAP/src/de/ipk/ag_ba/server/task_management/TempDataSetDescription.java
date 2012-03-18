package de.ipk.ag_ba.server.task_management;

public class TempDataSetDescription implements Comparable<TempDataSetDescription> {
	
	private final String bcn;
	private final String bpn;
	private final String bst;
	private final String originDBid;
	private final String release_iap;
	
	public TempDataSetDescription(String bcn, String bpn, String bst, String originDBid, String release_iap) {
		this.bcn = bcn;
		this.bpn = bpn;
		this.bst = bst;
		this.originDBid = originDBid;
		this.release_iap = release_iap;
	}
	
	@Override
	public int compareTo(TempDataSetDescription o) {
		return (bcn + "_" + bpn + "_" + bst).compareTo((o.bcn + "_" + o.bpn + "_" + o.bst));
	}
	
	@Override
	public int hashCode() {
		return (bcn + "_" + bpn + "_" + bst).hashCode();
	}
	
	@Override
	public String toString() {
		return bcn + "_" + bpn + "_" + bst;
	}
	
	public String getRemoteCapableAnalysisActionClassName() {
		return bcn;
	}
	
	public String getPartCnt() {
		return bpn;
	}
	
	public String getSubmissionTime() {
		return bst;
	}
	
	public int getPartCntI() {
		return Integer.parseInt(bpn);
	}
	
	public long getSubmissionTimeL() {
		return Long.parseLong(bst);
	}
	
	public String getOriginDBid() {
		return originDBid;
	}
	
	public String getReleaseIAP() {
		return release_iap;
	}
}
