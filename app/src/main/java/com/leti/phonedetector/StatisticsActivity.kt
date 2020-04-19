package com.leti.phonedetector

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import com.leti.phonedetector.database.PhoneLogDBHelper
import java.util.*


class StatisticsActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        title = "Statistics"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val anyChartView: AnyChartView = findViewById(R.id.any_chart_view)
        anyChartView.setProgressBar(findViewById(R.id.progress_bar))

        val cartesian = AnyChart.column()

        val db = PhoneLogDBHelper(this)
        val phones = db.getStatisticsPhoneLog()

        val calendar: Calendar = Calendar.getInstance()
        val currentYear: Int = calendar.get(Calendar.YEAR)
        val currentMonth: Int = calendar.get(Calendar.MONTH)

        val phonesMap = mutableMapOf<String, Int>()

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

        val data: MutableList<DataEntry> = ArrayList()

        for ((k, v) in phonesMap) {
            Log.d("PHONES MAP", "$k = $v")
            data.add(ValueDataEntry(k.substring(5,7) + '.' + k.substring(2, 4), v))
        }

        val column = cartesian.column(data)

        column.tooltip()
            .titleFormat("{%X}")
            .position(Position.CENTER_BOTTOM)
            .anchor(Anchor.CENTER_BOTTOM)
            .offsetX(0.0)
            .offsetY(10.0)
            .format("{%Value} calls")

        cartesian.animation(true)
        cartesian.title("Spam calls for 12 latest months")

        cartesian.yScale().minimum(0.0)
        cartesian.yScale().ticks().allowFractional(false)

        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }")
        cartesian.xAxis(0).labels().format("{%Value}{groupsSeparator: }")

        val xAxisLabels = cartesian.xAxis(0).labels()
        xAxisLabels.rotation(270)

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.interactivity().hoverMode(HoverMode.BY_X)

        cartesian.xAxis(0).title("Month")
        cartesian.yAxis(0).title("Number of calls")

        cartesian.yGrid(0).enabled(true)

        anyChartView.setChart(cartesian)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
