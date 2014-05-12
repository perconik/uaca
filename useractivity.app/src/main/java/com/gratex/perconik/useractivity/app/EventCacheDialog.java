package com.gratex.perconik.useractivity.app;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.gratex.perconik.useractivity.app.dto.CachedEvent;

public class EventCacheDialog extends JDialog {
	private static final long serialVersionUID = 3565081061317049889L;
	private EventCache eventCache;
	private JTable eventsTable;
	private ArrayList<CachedEvent> displayedEvents; //events currently displayed to the user
	
	public EventCacheDialog(JFrame parent, EventCache eventCache) {
		super(parent, true);
		
		this.eventCache = eventCache;
		
		setTitle("Event Cache");
		setIconImage(ResourcesHelper.getUserActivityIcon16().getImage());
		setSize(500, 500);		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addControls();
		setLocationRelativeTo(null);
		
		refreshEvents();
	}
	
	private void addControls() {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		add(topPanel);
		
		addEventsTable(topPanel);
		addButtons(topPanel);
	}
	
	private void addEventsTable(JPanel parent) {
		this.eventsTable = new JTable(new DefaultTableModel() {
			private static final long serialVersionUID = -4700723820298918429L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		this.eventsTable.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					openSelectedEventRowDetail();
				}
			}
		});
		parent.add(new JScrollPane(this.eventsTable));
	}
	
	private void refreshEvents() {
		try {
			this.displayedEvents = this.eventCache.getEvents();
			setEventsTableData();
		} catch (SQLException ex) {
			MessageBox.showError(this, "Failed to retrieve events from the cache.", ex, "Failed retrieve events.");
		}
	}
	
	private void setEventsTableData() {
		//set data
		Object[][] rows = new Object[this.displayedEvents.size()][2];
		for(int i = 0; i < this.displayedEvents.size(); i++) {
			CachedEvent cachedEvent = this.displayedEvents.get(i);
			rows[i] = new Object[] { SimpleDateFormat.getInstance().format(cachedEvent.getTime()), cachedEvent.getEventId() };
		}
		((DefaultTableModel)this.eventsTable.getModel()).setDataVector(rows, new String[] { "Time", "ID"});
		
		//resize columns
		TableColumn timeColumn = this.eventsTable.getColumnModel().getColumn(0);
		timeColumn.setPreferredWidth(150);
		timeColumn.setMinWidth(0);
		timeColumn.setMaxWidth(1000);
	}
	
	private void openSelectedEventRowDetail() {
		int selectedRowIndex = this.eventsTable.getSelectedRow();
		if(selectedRowIndex != -1) {
			new CachedEventDetailDialog(this, this.displayedEvents.get(selectedRowIndex)).setVisible(true);
		}
	}
	
	private void addButtons(JPanel panel) {
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
		panel.add(buttonsPanel);
		
		//'refresh' button
		addButton(buttonsPanel, "Refresh", "Reload events from the cache", true, new ActionListener() {			
			public void actionPerformed(ActionEvent arg0) {
				refreshEvents();
			}
		});
		
		//'delete selection' button
		addButton(buttonsPanel, "Delete Selection", "Remove the selected events from the cache", true, new ActionListener() {			
			public void actionPerformed(ActionEvent arg0) {
				try {
					ArrayList<UUID> selectedEventIds = new ArrayList<UUID>();
					for (int eventIndex : EventCacheDialog.this.eventsTable.getSelectedRows()) {
						selectedEventIds.add(EventCacheDialog.this.displayedEvents.get(eventIndex).getEventId());
					}
					EventCacheDialog.this.eventCache.removeEvents(selectedEventIds);
					refreshEvents();
				} catch(SQLException ex) {
					MessageBox.showError(EventCacheDialog.this, "Failed to delete all of the selected events.", ex, "Delete selection failed");
				}
			}
		});
		
		//'delete all' button
		addButton(buttonsPanel, "Delete All", "Remove all events from the cache", true, new ActionListener() {			
			public void actionPerformed(ActionEvent arg0) {
				try {
					EventCacheDialog.this.eventCache.removeAllEvents();
					refreshEvents();
				} catch(SQLException ex) {
					MessageBox.showError(EventCacheDialog.this, "Failed to delete all events.", ex, "Delete all failed");
				}
			}
		});
		
		//'close' button
		addButton(buttonsPanel, "Close", "Close the dialog", false, new ActionListener() {			
			public void actionPerformed(ActionEvent arg0) {
				EventCacheDialog.this.setVisible(false);
			}
		});
	}
	
	private void addButton(JPanel panel, String text, String toolTipText, boolean addMargin, ActionListener actionListener) {		
		JButton button = new JButton(text);
		button.setToolTipText(toolTipText);
		button.setAlignmentY(CENTER_ALIGNMENT);
		button.setAlignmentX(CENTER_ALIGNMENT);
		//button.setMinimumSize(new Dimension(75, 0));
		//button.setMaximumSize(new Dimension(75, 500));
		button.addActionListener(actionListener);
		
		panel.add(button);
		if(addMargin) {
			panel.add(Box.createRigidArea(new Dimension(5, 0)));
		}
	}
}