package com.gratex.perconik.useractivity.app.watchers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.SigarException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gratex.perconik.useractivity.app.AppTracer;
import com.gratex.perconik.useractivity.app.dto.ProcessDto;
import com.gratex.perconik.useractivity.app.dto.ProcessesChangedSinceCheckEventDto;

/**
 * Raises 'ProcessesChangedSinceCheckEvent'.
 */
public class ProcessWatcher extends TimerWatcherBase {
	private HashMap<Integer, ProcessDto> lastRunningProcesses = new HashMap<Integer, ProcessDto>();
	
	@Override
	public String getDisplayName() {
		return "Running Processes";
	}	
	
	@Override
	public void stop() {
		super.stop();
		lastRunningProcesses.clear(); //to report all running processes when the monitoring is resumed again
	}
	
	@Override
	protected void onTick()
	{
		try {
			HashSet<Integer> runningProcesses = getRunningProcesses();
			List<ProcessDto> addedProcesses = getAddedProcesses(runningProcesses);
			List<ProcessDto> killedProcesses = getKilledProcesses(runningProcesses);
			
			if(addedProcesses.size() != 0 || killedProcesses.size() != 0) {
				addEventToCache(addedProcesses, killedProcesses);
				updateLastRunningProcesses(addedProcesses, killedProcesses);
			}
		} catch(Throwable ex) {
			AppTracer.getInstance().writeError("Failed to update running processes.", ex);
		}
	}

	private HashSet<Integer> getRunningProcesses() throws SigarException {
		long[] pids = Humidor.getInstance().getSigar().getProcList();
		
		HashSet<Integer> pidsSet = new HashSet<Integer>(pids.length);
		for (long pid : pids) {
			pidsSet.add((int)pid);
		}
		return pidsSet;
	}
	
	private List<ProcessDto> getAddedProcesses(HashSet<Integer> runningProcesses) throws SigarException {
		ArrayList<ProcessDto> addedProcesses = new ArrayList<ProcessDto>();
		
		for (Integer runningProcessPid : runningProcesses) {
			if(!lastRunningProcesses.containsKey(runningProcessPid)) {
				ProcessDto dto = new ProcessDto();
				dto.setPid((int)(long)runningProcessPid);
				dto.setName(Humidor.getInstance().getSigar().getProcState(runningProcessPid).getName());
				addedProcesses.add(dto);
			}
		}		
		return addedProcesses;
	}
	
	private List<ProcessDto> getKilledProcesses(HashSet<Integer> runningProcesses) {
		ArrayList<ProcessDto> killedProcesses = new ArrayList<ProcessDto>();
		
		for (ProcessDto lastRunningProcessDto : lastRunningProcesses.values()) {
			if(!runningProcesses.contains(lastRunningProcessDto.getPid())) {
				killedProcesses.add(lastRunningProcessDto);
			}
		}
		return killedProcesses;
	}
	
	private void addEventToCache(List<ProcessDto> addedProcesses, List<ProcessDto> killedProcesses) throws JsonProcessingException, SQLException {
		ProcessesChangedSinceCheckEventDto dto = new ProcessesChangedSinceCheckEventDto();
		dto.setAddedProcesses(addedProcesses);
		dto.setKilledProcesses(killedProcesses);
		
		getEventCache().addEvent(dto);
	}
	
	private void updateLastRunningProcesses(List<ProcessDto> addedProcesses, List<ProcessDto> killedProcesses) {
		for (ProcessDto killedProcess : killedProcesses) {
			lastRunningProcesses.remove(killedProcess.getPid());
		}
		
		for (ProcessDto addedProcess : addedProcesses) {
			lastRunningProcesses.put(addedProcess.getPid(), addedProcess);
		}
	}
}