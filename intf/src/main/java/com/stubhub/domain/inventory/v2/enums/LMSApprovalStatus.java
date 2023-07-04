package com.stubhub.domain.inventory.v2.enums;

public enum LMSApprovalStatus {

	PENDING_APROVAL(1, "Pending Approval"), 
	
	APPROVED(2, "Approved"),

	REJECTED(3, "Rejected"),

	APROVAL_REMOVED(4, "Approval Removed");

	private Integer id;
	private String desc;

	private LMSApprovalStatus(Integer id, String desc) {
		this.id = id;
		this.desc = desc;
	}

	public Integer getId() {
		return this.id;
	}

	public String getDescription() {
		return this.desc;
	}

	public static LMSApprovalStatus getById(Integer id) {
		if (id != null) {
			LMSApprovalStatus[] values = LMSApprovalStatus.values();
			for (LMSApprovalStatus value : values) {
				if (value.id.compareTo(id) == 0)
					return value;
			}
		}

		return null;
	}
}
