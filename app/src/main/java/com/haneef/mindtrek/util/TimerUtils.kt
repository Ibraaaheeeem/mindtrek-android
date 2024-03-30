import android.os.CountDownTimer
import android.widget.TextView
import java.util.concurrent.TimeUnit

class TimerUtils(private val textView: TextView, private val seconds: Long) {
    private var countDownTimer: CountDownTimer? = null
    private var timerCallback: TimerCallback? = null

    fun setCallback(callback: TimerCallback) {
        timerCallback = callback
    }

    fun start() {
        countDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingSeconds = millisUntilFinished / 1000
                val formattedTime = formatTime(remainingSeconds)
                updateView(formattedTime)
            }

            override fun onFinish() {
                val formattedTime = formatTime(0)
                updateView(formattedTime)
                timerCallback?.onTimerFinished()
            }
        }
        countDownTimer?.start()
    }

    fun cancel() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun formatTime(remainingSeconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(remainingSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(remainingSeconds - TimeUnit.HOURS.toSeconds(hours))
        val seconds = remainingSeconds - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun updateView(formattedTime: String) {
        textView.text = "$formattedTime"
    }
    interface TimerCallback {
        fun onTimerFinished()
    }

}
