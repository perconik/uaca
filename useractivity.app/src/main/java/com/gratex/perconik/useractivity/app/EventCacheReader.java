package com.gratex.perconik.useractivity.app;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.gratex.perconik.useractivity.app.dto.CachedEvent;

/**
 * Represents a data reader for EventCache query result sets. Reads CachedEvents from a result set.
 */
public class EventCacheReader {
	private Connection connection;
	private ResultSet resultSet;
	private CachedEvent current;

	public EventCacheReader(Connection connection, ResultSet resultSet) {
		ValidationHelper.checkArgNotNull(connection, "connection");
		ValidationHelper.checkArgNotNull(resultSet, "resultSet");
		
		this.connection = connection;
		this.resultSet = resultSet;		
	}
	
	/**
	 * Reads the next CachedEvent from the result set.
	 * Thread safe.
	 * @throws SQLException
	 */
	public boolean next() throws SQLException {
	  if(resultSet.next()) {
	    current = new CachedEvent(resultSet.getInt("ID"),
	                              resultSet.getString("EVENTID"), 
	                              XMLGregorianCalendarHelper.createUtc(resultSet.getLong("TIMESTAMP")), 
	                              resultSet.getString("DATA"));
	    return true;
	  }
	  current = null;
	  return false;
	}
	
	public CachedEvent getCurrent() {
		return current;
	}

	public void closeOrTrace() {
		try {
			close();
		} catch (SQLException ex) {
			AppTracer.getInstance().writeError("Failed to close an EventCacheReader.", ex);
		}
	}
	
	public void close() throws SQLException
	{
		resultSet.close();
		connection.close();
	}
}