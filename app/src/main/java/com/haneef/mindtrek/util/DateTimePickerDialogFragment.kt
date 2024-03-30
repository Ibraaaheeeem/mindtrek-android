import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.haneef.mindtrek.R

class DateTimePickerDialogFragment : DialogFragment() {

    interface DateTimePickerListener {
        fun onDateTimeSelected(day: Int, month: Int, year: Int, hour: Int, minute: Int)
    }

    private var listener: DateTimePickerListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.date_time_picker, null)

        val datePicker = view.findViewById<DatePicker>(R.id.date_picker)
        val timePicker = view.findViewById<TimePicker>(R.id.time_picker)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)

        val builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle("Schedule Mock Date and Time")

        val dialog = builder.create()

        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            // Handle time change here
        }

        confirmButton.setOnClickListener {
            val year = datePicker.year
            val month = datePicker.month
            val day = datePicker.dayOfMonth

            // Get the selected time from the TimePicker
            val hour = timePicker.currentHour
            val minute = timePicker.currentMinute
            val selectedDateTime = "$day-${month}-$year $hour:$minute:00"

            listener?.onDateTimeSelected(day, month, year, hour, minute)
            dialog.dismiss()
        }

        return dialog
    }

    fun setListener(listener: DateTimePickerListener) {
        this.listener = listener
    }
    fun format2(number: Int): String {
        return String.format("%02d", number)
    }
}