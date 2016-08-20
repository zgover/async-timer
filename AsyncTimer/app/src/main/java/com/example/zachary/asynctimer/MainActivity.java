// Zachary Gover
// JAV1 - 1608
// MainActivity

package com.example.zachary.asynctimer;

import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

	/**
	 * MARK: Global Properties
	 */

	public int min;
	public int sec;
	public Long totalTime;
	public Long timeElapsed;
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
			running = false;

			// Reset fields
			minField.setText("00");
			secField.setText("00");
		}
	}

	public void startTimer(View view) {
		if (running) {
			showToast("Timer is already running");
			return;
		}
		// All empty or zeros
		else if ((minField.equals("") || minField.equals("00") || minField.equals("0"))
			&& (secField.equals("0") || secField.equals("00") || secField.equals(""))) {
			showToast("Please enter a valid time");
			return;
		}

		// Set the min / sec
		this.min = Integer.parseInt(minField.getText().toString());
		this.sec = Integer.parseInt(secField.getText().toString());
		this.totalTime = TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec);

		// Start the timer
		preTime = System.currentTimeMillis();
		timeElapsed = new Long(0);
		running = true;

		AsyncThread task = new AsyncThread();
		task.execute(totalTime);
	}

	/**
	 * MARK: Custom Methods
	 */

	public void updateTimer(Long time) {
		HashMap<String, String> parsedTime = milToReal(time);

		// Set text for min and sec text fields
		this.minField.setText(parsedTime.get("min"));
		this.secField.setText(parsedTime.get("sec"));
	}

	public void showToast(String message) {
		Toast toast = Toast.makeText(
			getApplicationContext(), message, Toast.LENGTH_SHORT
		);
		toast.show();
	}

	public void showAlert(String title, String message) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(title);
		adb.setMessage(message);
		adb.setPositiveButton("Continue", null);
		adb.show();
	}

	public HashMap<String, String> milToReal(Long time) {
		HashMap<String, String> returnTime = new HashMap<>();
		String min;
		String sec;

		// Set the values from the time elapsed
		min = String.format(
			"%02d", TimeUnit.MILLISECONDS.toMinutes(time)
		);

		sec = String.format(
			"%02d", TimeUnit.MILLISECONDS.toSeconds(time) -
				TimeUnit.MINUTES.toSeconds(
					TimeUnit.MILLISECONDS.toMinutes(time)
				)
		);

		returnTime.put("min", min);
		returnTime.put("sec", sec);

		return returnTime;
	}

	/**
	 * MARK: Nested Classes
	 */

	private class AsyncThread extends AsyncTask<Long, Long, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			// Show toast for initial run
			showToast("Timer has been started");
		}

		@Override
		protected String doInBackground(Long... time) {
			Long totalTime = time[0];
			String returnType;

			do {
				// Sleep for 500 milliseconds
				try { Thread.sleep(500); } catch (Exception e) { e.printStackTrace(); }

				// Grab the time after sleep and compare.
				Long postTime = System.currentTimeMillis();
				Long difference = postTime - preTime;

				timeElapsed += difference;

				// Determine the result or if we cancelled the timer
				if (!running) {
					break;
				} else if (timeElapsed >= totalTime) {
					running = false;
					System.out.println("yes it did still run");
				} else {
					System.out.println("is running: " + running);
					// Update timer
					publishProgress(totalTime - timeElapsed);
					preTime = postTime;
				}

			} while(running);

			if (running) {
				returnType = "timer-expired";
			} else {
				returnType = "timer-cancelled";
			}

			return returnType;
		}

		@Override
		protected void onProgressUpdate(Long... time) {
			super.onProgressUpdate(time);

			// Update timer on main thread
			updateTimer(time[0]);
		}

		@Override
		protected void onPostExecute(String type) {
			super.onPostExecute(type);

			HashMap<String, String> time = milToReal(timeElapsed);
			String showTime  = "Min: " + time.get("min") + " Sec: " + time.get("sec");

			if (type.equals("timer-expired")) {
				showAlert("Timer Expired", "Total Time: " + showTime);
			} else if (type.equals("timer-cancelled")) {
				showAlert("Timer Cancelled", "Total Time: " + showTime);
			}
		}
	}
}
