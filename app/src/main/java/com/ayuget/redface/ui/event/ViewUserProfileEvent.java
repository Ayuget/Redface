package com.ayuget.redface.ui.event;

public class ViewUserProfileEvent {
	private final int userId;

	public ViewUserProfileEvent(int userId) {
		this.userId = userId;
	}

	public int getUserId() {
		return userId;
	}
}
