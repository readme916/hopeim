package com.tianyoukeji.parent.common;

public class HttpPostReturnUuid {

	private long uuid;
	
	public HttpPostReturnUuid() {
		this.uuid = 0;
	}
	public HttpPostReturnUuid(long uuid) {
		this.uuid = uuid;
	}
	public long getUuid() {
		return uuid;
	}
	public void setUuid(long uuid) {
		this.uuid = uuid;
	}
	
}
