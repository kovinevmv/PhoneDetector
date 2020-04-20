package com.leti.phonedetector

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anychart.APIlib
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.CategoryValueDataEntry
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.chart.common.listener.Event
import com.anychart.chart.common.listener.ListenersInterface
import com.anychart.enums.*
import com.anychart.scales.OrdinalColor
import com.leti.phonedetector.database.PhoneLogDBHelper
import com.leti.phonedetector.model.PhoneLogInfo
import kotlinx.android.synthetic.main.activity_statistics.*
import java.util.*


class StatisticsActivity : AppCompatActivity() {

    private lateinit var phones: ArrayList<PhoneLogInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        title = "Statistics"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        readDataBase()

        createSpamChart()
        createPieChart()
        createWordCloud()
    }

    private fun readDataBase() {
        val db = PhoneLogDBHelper(this)
        phones = db.readPhoneLog()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun dateToMapKey(month : Int, year : Int) : String{
        val monthString = month.toString().padStart(2, '0')
        val yearString = year.toString()

        return "${yearString}.${monthString}"
    }

    private fun createSpamChart() {
        val spamView: AnyChartView = findViewById(R.id.chart_spam)
        APIlib.getInstance().setActiveAnyChartView(chart_spam)
        spamView.setProgressBar(findViewById(R.id.progress_bar_chart_spam))

        val spam = AnyChart.column()


        val calendar: Calendar = Calendar.getInstance()
        val currentYear: Int = calendar.get(Calendar.YEAR)
        val currentMonth: Int = calendar.get(Calendar.MONTH) + 1

        val phonesMap = mutableMapOf<String, Int>()

        if (currentMonth == 12) {
            // Full year
            for (month in 1..12) {
                phonesMap[dateToMapKey(month, currentYear)] = 0
            }
        } else {
            // Past year
            for (month in (currentMonth + 1)..12) {
                phonesMap[dateToMapKey(month, currentYear - 1)] = 0
            }
            // Current year
            for (month in 1..(currentMonth)) {
                phonesMap[dateToMapKey(month, currentYear)] = 0
            }
        }

        val innerPhones = phones.filter { p -> p.isSpam }
        for (phone in innerPhones) {
            val date = phone.date.substring(0, 7)

            if (phonesMap.containsKey(date))
                phonesMap[date] = phonesMap.getOrPut(date) { 1 } + 1
        }

        val spamData: MutableList<DataEntry> = ArrayList()

        for ((k, v) in phonesMap) {
            spamData.add(ValueDataEntry(k.substring(5, 7) + '.' + k.substring(2, 4), v))
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
        spam.title("Spam calls for 12 latest months").enabled(true)

        spam.yScale().minimum(0.0)
        spam.yScale().ticks().allowFractional(false)

        spam.yAxis(0).labels().format("{%Value}{groupsSeparator: }")
        spam.xAxis(0).labels().format("{%Value}{groupsSeparator: }")

        val xAxisLabels = spam.xAxis(0).labels()
        xAxisLabels.rotation(270)

        spam.tooltip().positionMode(TooltipPositionMode.POINT)
        spam.interactivity().hoverMode(HoverMode.BY_X)

        spam.xAxis(0).title("Month")
        spam.yAxis(0).title("Number of calls")

        spam.yGrid(0).enabled(true)

        spamView.setChart(spam)
    }

    private fun createPieChart() {
        val chart: AnyChartView = findViewById(R.id.chart_pie_top)
        APIlib.getInstance().setActiveAnyChartView(chart_pie_top)

        chart.setProgressBar(findViewById(R.id.progress_bar_chart_pie_top));

        val pie = AnyChart.pie()

        pie.setOnClickListener(object :
            ListenersInterface.OnClickListener(arrayOf("x", "value")) {
            override fun onClick(event: Event) {}
        })

        val data: MutableList<DataEntry> = ArrayList()
        data.add(
            ValueDataEntry(
                "Spam incoming calls",
                phones.filter { phone -> phone.isSpam }.size
            )
        )
        data.add(
            ValueDataEntry(
                "Not spam incoming calls",
                phones.filter { phone -> !phone.isSpam }.size
            )
        )


        pie.data(data)
        pie.title("Spam Call Pie Chart")
        pie.labels().position("outside")


        pie.legend()
            .position("center-bottom")
            .itemsLayout(LegendLayout.HORIZONTAL)
            .align(Align.CENTER)


        chart.setChart(pie)
    }

    private fun createWordCloud() {
        val anyChartView: AnyChartView = findViewById(R.id.chart_word_cloud)
        APIlib.getInstance().setActiveAnyChartView(chart_word_cloud)

        anyChartView.setProgressBar(findViewById(R.id.progress_bar_chart_word_cloud))

        val tagCloud = AnyChart.tagCloud()

        tagCloud.title("World Population")

        val ordinalColor = OrdinalColor.instantiate()
        ordinalColor.colors(
            arrayOf(
                "#f14526", "#26959f", "#3b8ad8", "#60727b", "#e24b26"
            )
        )
        tagCloud.colorScale(ordinalColor)
        tagCloud.angles(arrayOf(-90.0, 0.0, 90.0))

        tagCloud.colorRange().enabled(true)
        tagCloud.colorRange().colorLineSize(15.0)


        val data: MutableList<DataEntry> = ArrayList()

        val phonesMap = mutableMapOf<Boolean, MutableMap<String, Int>>()
        phonesMap[true] = mutableMapOf()
        phonesMap[false] = mutableMapOf()

        for (phone in phones) {
            phonesMap[phone.isSpam]?.set(phone.name,
                (phonesMap[phone.isSpam]?.getOrPut(phone.name) { 0 } ?: 0) + 1)
        }

        for ((isSpam, v) in phonesMap) {
            for ((name, count) in v) {
                data.add(CategoryValueDataEntry(name, if (isSpam) "Spam" else "Not spam", count))
            }
        }


        tagCloud.data(data)

        anyChartView.setChart(tagCloud)
    }
}
