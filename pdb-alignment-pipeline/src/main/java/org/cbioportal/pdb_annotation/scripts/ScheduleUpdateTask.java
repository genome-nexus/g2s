package org.cbioportal.pdb_annotation.scripts;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cron work on Weekly Update
 * @author wangjue
 *
 */
public class ScheduleUpdateTask extends TimerTask {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// Constructor
	public ScheduleUpdateTask() {

	}

	public void run() {
		try {
			logger.info("[UPDATE] CronJob Start ...");
			PdbScriptsPipelineRunCommand app = new PdbScriptsPipelineRunCommand();
			app.runUpdatePDB();
			logger.info("[UPDATE] CronJob End ...");

		} catch (Exception ex) {

			System.out.println("[UPDATE] Error running thread " + ex.getMessage());
		}
	}

}
