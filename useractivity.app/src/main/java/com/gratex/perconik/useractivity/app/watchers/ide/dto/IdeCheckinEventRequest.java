package com.gratex.perconik.useractivity.app.watchers.ide.dto;

public class IdeCheckinEventRequest extends BaseIdeEventRequest{
	/**
	 * Changeset id as specified in a RCS
	 */
	private String changesetIdInRcs;

	/**
	 * Target rcs server or remote repository 
	 */
	private RcsServerDto rcsServer;

	/**
	 * @return the {@link #changesetIdInRcs}
	 */
	public String getChangesetIdInRcs() {
		return changesetIdInRcs;
	}

	/**
	 * @param {@link #changesetIdInRcs}
	 */
	public void setChangesetIdInRcs(String changesetIdInRcs) {
		this.changesetIdInRcs = changesetIdInRcs;
	}

	/**
	 * @return the {@link #rcsServer}
	 */
	public RcsServerDto getRcsServer() {
		return rcsServer;
	}

	/**
	 * @param {@link #rcsServer}
	 */
	public void setRcsServer(RcsServerDto rcsServer) {
		this.rcsServer = rcsServer;
	}
}