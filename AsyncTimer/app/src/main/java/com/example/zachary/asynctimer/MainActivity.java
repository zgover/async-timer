// Zachary Gover
// JAV1 - 1608
// MainActivity

package com.example.zachary.asynctimer;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

	/**
	 * MARK: Global Properties
	 */

	public int min;
	public int sec;
	public Long totalTime;
	public Long timeElapsed;
	public Timer timer;
	public Long preTime;
	public boolean running;

	/**
	 * MARK: View Connections
	 */

	public TextView minField;
	public TextView secField;
	public Button startBtn;
	public Button stopBtn;

	/**
	 * MARK: Default Methods
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Setup default properties and view connections
		minField = (EditText) findViewById(R.id.minField);
		secField = (EditText) findViewById(R.id.secField);
		startBtn = (Button) findViewById(R.id.startBtn);
		stopBtn = (Button) findViewById(R.id.stopBtn);
	}

	/**
	 * MARK: Event Listeners
	 */

	public void stopTimer(View view) {
		// Stop the timer
		if (running) {
			timer.cancel();
			preTime = null;
			timer = null;
			running = false;
		}
	}

	public void startTimer(View view) {
		if (running) { return; }

		// Set the min / sec
		this.min = Integer.parseInt(minField.getText().toString());
		this.sec = Integer.parseInt(secField.getText().toString());
		this.totalTime = new Long(
			(TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec))
		);

		// Start the timer
		preTime = System.currentTimeMillis();
		timeElapsed = new Long(0);
		timer = new Timer();
		running = true;

		timer.schedule(new TimeTask(), 0, 500);
	}

	/**
	 * MARK: Custom Methods
	 */

	public void updateTimer(String min, String sec) {
		// Set text for min and sec text fields
		this.minField.setText(min);
		this.secField.setText(sec);
	}

	/**
	 * MARK: Nested Classes
	 */

	private class AsyncThread extends AsyncTask<String, String, HashMap<String, String>> {

		@Override
		protected HashMap<String, String> doInBackground(String... strings) {

			HashMap<String, String> returnTime;
			Long postTime = System.currentTimeMillis();
			Long difference = postTime - preTime;
			String min;
			String sec;

			System.out.println(preTime);
			System.out.println(postTime);
			System.out.println(timeElapsed);
			System.out.println(difference);
			System.out.println(timeElapsed + difference);
			System.out.println(totalTime);

			timeElapsed += difference;

			// Determine the time difference
			if (timeElapsed >= totalTime) {
				min = "00";
				sec = "00";

				timer.cancel();
			} else {
				// Set the values from the time elapsed
				min = String.format(
					"%02d", TimeUnit.MILLISECONDS.toMinutes(totalTime - timeElapsed)
				);

				sec = String.format(
					"%02d", TimeUnit.MILLISECONDS.toSeconds(totalTime - timeElapsed) -
						TimeUnit.MINUTES.toSeconds(
							TimeUnit.MILLISECONDS.toMinutes(totalTime - timeElapsed)
						)
				);
			}

			// Build the return time and return it
			returnTime = new HashMap<>();
			returnTime.put("min", min);
			returnTime.put("sec", sec);

			preTime = System.currentTimeMillis();

			return returnTime;
		}

		@Override
		protected void onPostExecute(HashMap<String, String> s) {
			super.onPostExecute(s);
			updateTimer(s.get("min"), s.get("sec"));
		}
	}

	private class TimeTask extends TimerTask {

		@Override
		public void run() {
			AsyncThread task = new AsyncThread();
			task.execute();
		}
	}
}
