package com.gratex.perconik.useractivity.app.watchers;

import java.util.ArrayList;

import com.gratex.perconik.useractivity.app.AppSvc;
import com.gratex.perconik.useractivity.app.AppTracer;
import com.gratex.perconik.useractivity.app.EventCache;
import com.gratex.perconik.useractivity.app.Settings;

/**
 * Manages all watcher instances.
 */
public class WatcherManager {
  private EventCache eventCache;
  private ArrayList<Watcher> watchers;
  private WatcherServer watcherServer;

  public WatcherManager(EventCache eventCache) {
    this.eventCache = eventCache;

    this.watchers = new ArrayList<>();

    try {
      this.watchers.add(new ProcessWatcher());
    } catch (Exception ex) {
      AppTracer.getInstance().writeError("Failed to create an instance of a watcher.", ex);
    }

    try {
      this.watcherServer = new WatcherServer(Settings.getInstance().getLocalSvcPort());
      this.watcherServer.setServiceClasses(IdeWatcherSvc.class, WebWatcherSvc.class, BashCommandWatcherSvc.class, AppSvc.class, //TODO:AppSvc is not a watcher - add this whole code somewhere else
          GenericEventWatcherSvc.class);
      this.watcherServer.start(); //TODO: stop at app shutdown
    } catch (Exception ex) {
      AppTracer.getInstance().writeError(String.format("Failed to start the '%s'.", WatcherServer.class.getName()), ex);
    }
  }

  public void close() throws Exception {
    this.stopWatchers();
    this.watcherServer.stop();
  }

  public ArrayList<Watcher> getWatchers() {
    return this.watchers;
  }

  public void startWatchers() {
    for (Watcher watcher: this.watchers) {
      try {
        watcher.start(this.eventCache);
      } catch (Throwable ex) {
        AppTracer.getInstance().writeError(String.format("Failed to start the watcher '%s'.", watcher.getDisplayName()), ex);
      }
    }
  }

  public void stopWatchers() {
    for (Watcher watcher: this.watchers) {
      try {
        watcher.stop();
      } catch (Throwable ex) {
        AppTracer.getInstance().writeError(String.format("Failed to stop the watcher '%s'.", watcher.getDisplayName()), ex);
      }
    }
  }
}
