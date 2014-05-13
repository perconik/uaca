package com.gratex.perconik.useractivity.app.dto.web;

import javax.ws.rs.core.UriBuilder;

public class WebBookmarkEventDto extends WebEventDto {
	private String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	protected UriBuilder getDefaultEventTypeUri() {
		return super.getDefaultEventTypeUri().path("bookmark");
	}
}