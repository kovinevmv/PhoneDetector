package com.leti.phonedetector

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.anychart.APIlib
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import com.leti.phonedetector.database.PhoneLogDBHelper
import kotlinx.android.synthetic.main.activity_statistics.*
import java.util.*


class StatisticsActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        title = "Statistics"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val spamView: AnyChartView = findViewById(R.id.any_chart_spam)
        APIlib.getInstance().setActiveAnyChartView(any_chart_spam)
        // anyChartView.setProgressBar(findViewById(R.id.progress_bar))

        val spam = AnyChart.column()

        var db = PhoneLogDBHelper(this)
        var phones = db.getStatisticsPhoneLog()
        val calendar: Calendar = Calendar.getInstance()
        val currentYear: Int = calendar.get(Calendar.YEAR)
        val currentMonth: Int = calendar.get(Calendar.MONTH)

        var phonesMap = mutableMapOf<String, Int>()

        if (currentMonth == 11) {
            for (i in 1..12) {
                phonesMap[currentYear.toString() + '.' + i.toString().padStart(2, '0')] = 0
            }
        } else {
            for (i in (currentMonth + 2)..12) {
                phonesMap[(currentYear - 1).toString() + '.' + i.toString().padStart(2, '0')] = 0
            }
            for (i in 1..(currentMonth + 1)) {
                phonesMap[currentYear.toString() + '.' + i.toString().padStart(2, '0')] = 0
            }
        }
        phones.forEach{
            Log.d("PHONES NUM", it.date.substring(0, 7))
            Log.d("PHONES NUM EQ", phonesMap.containsKey(it.date.substring(0, 7)).toString())
            if (phonesMap.containsKey(it.date.substring(0, 7))) {
                phonesMap.merge(it.date.substring(0, 7),1, Int::plus)
            }
        }

        val spamData: MutableList<DataEntry> = ArrayList()

        for ((k, v) in phonesMap) {
            Log.d("PHONES MAP", "$k = $v")
            spamData.add(ValueDataEntry(k.substring(5,7) + '.' + k.substring(2, 4), v))
        }

        var column = spam.column(spamData)

        column.tooltip()
            .titleFormat("{%X}")
            .position(Position.CENTER_BOTTOM)
            .anchor(Anchor.CENTER_BOTTOM)
            .offsetX(0.0)
            .offsetY(10.0)
            .format("{%Value} calls")

        spam.animation(true)
        spam.title("Spam calls for 12 latest months")

        spam.yScale().minimum(0.0)
        spam.yScale().ticks().allowFractional(false)

        spam.yAxis(0).labels().format("{%Value}{groupsSeparator: }")
        spam.xAxis(0).labels().format("{%Value}{groupsSeparator: }")

        var xAxisLabels = spam.xAxis(0).labels()
        xAxisLabels.rotation(270)

        spam.tooltip().positionMode(TooltipPositionMode.POINT)
        spam.interactivity().hoverMode(HoverMode.BY_X)

        spam.xAxis(0).title("Month")
        spam.yAxis(0).title("Number of calls")

        spam.yGrid(0).enabled(true)

        spamView.setChart(spam)

        val numberView: AnyChartView = findViewById(R.id.any_chart_number)
        APIlib.getInstance().setActiveAnyChartView(any_chart_number)
        // anyChartView.setProgressBar(findViewById(R.id.progress_bar))

        val number = AnyChart.column()

        db = PhoneLogDBHelper(this)
        phones = db.getStatisticsPhoneLog()

        phonesMap = mutableMapOf<String, Int>()

        if (currentMonth == 11) {
            for (i in 1..12) {
                phonesMap[currentYear.toString() + '.' + i.toString().padStart(2, '0')] = 0
            }
        } else {
            for (i in (currentMonth + 2)..12) {
                phonesMap[(currentYear - 1).toString() + '.' + i.toString().padStart(2, '0')] = 0
            }
            for (i in 1..(currentMonth + 1)) {
                phonesMap[currentYear.toString() + '.' + i.toString().padStart(2, '0')] = 0
            }
        }
        phones.forEach{
            Log.d("PHONES NUM", it.date.substring(0, 7))
            Log.d("PHONES NUM EQ", phonesMap.containsKey(it.date.substring(0, 7)).toString())
            if (phonesMap.containsKey(it.date.substring(0, 7))) {
                phonesMap.merge(it.date.substring(0, 7),1, Int::plus)
            }
        }

        val numberData: MutableList<DataEntry> = ArrayList()

        for ((k, v) in phonesMap) {
            Log.d("PHONES MAP", "$k = $v")
            numberData.add(ValueDataEntry(k.substring(5,7) + '.' + k.substring(2, 4), v))
        }

        column = number.column(numberData)

        column.tooltip()
            .titleFormat("{%X}")
            .position(Position.CENTER_BOTTOM)
            .anchor(Anchor.CENTER_BOTTOM)
            .offsetX(0.0)
            .offsetY(10.0)
            .format("{%Value} calls")

        number.animation(true)
        number.title("Spam calls for 12 latest months")

        number.yScale().minimum(0.0)
        number.yScale().ticks().allowFractional(false)

        number.yAxis(0).labels().format("{%Value}{groupsSeparator: }")
        number.xAxis(0).labels().format("{%Value}{groupsSeparator: }")

        xAxisLabels = number.xAxis(0).labels()
        xAxisLabels.rotation(270)

        number.tooltip().positionMode(TooltipPositionMode.POINT)
        number.interactivity().hoverMode(HoverMode.BY_X)

        number.xAxis(0).title("Month")
        number.yAxis(0).title("Number of calls")

        number.yGrid(0).enabled(true)

        numberView.setChart(number)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
