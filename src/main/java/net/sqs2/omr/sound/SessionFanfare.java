/*

 SessionFanfare.java

 Copyright 2009 KUBO Hiroya (hiroya@cuc.ac.jp).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package net.sqs2.omr.sound;

import java.util.logging.Logger;

import net.sqs2.omr.model.AppConstants;
import net.sqs2.sound.SoundManager;

public class SessionFanfare {

	public void startFanfare() {
		Logger.getLogger("fanfare").info("*********** START ************");
		SoundManager.getInstance().play(AppConstants.SESSION_FANFARE_SOUND_FILENAME);
	}

	public void finishFanfare() {
		Logger.getLogger("fanfare").info("********* FINISHED ***********");
		for(int i = 0; i < 3; i++){
			SoundManager.getInstance().play(AppConstants.SESSION_FANFARE_SOUND_FILENAME);
			if(i+1 < 3){
				sleep(500);
			}
		}
	}

	public void failFanfare() {
		Logger.getLogger("fanfare").info("********* FAILED ***********");
		for(int i = 0; i < 3; i++){
			SoundManager.getInstance().play(AppConstants.TASK_EXECUTION_ERROR_SOUND_FILENAME);
			if(i+1 < 3){
				sleep(500);
			}
		}
	}

	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException ignore) {
		}
	}
}