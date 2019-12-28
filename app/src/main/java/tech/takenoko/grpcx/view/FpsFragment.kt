package tech.takenoko.grpcx.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.fps_fragment.*
import kotlinx.android.synthetic.main.main_fragment.recycler
import tech.takenoko.grpcx.R
import tech.takenoko.grpcx.databinding.FpsFragmentBinding
import tech.takenoko.grpcx.viewmodel.FpsViewModel
import java.text.SimpleDateFormat
import java.util.*


class FpsFragment : Fragment() {

    companion object {
        fun newInstance() = FpsFragment()
    }

    private lateinit var viewModel: FpsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProviders.of(this).get(FpsViewModel::class.java)
        val binding = DataBindingUtil.inflate<FpsFragmentBinding>(inflater, R.layout.fps_fragment, container, false).apply {
            lifecycleOwner = this@FpsFragment
            viewmodel = viewModel
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
        setupGraph()
    }

    private fun setupRecyclerView() {
        val adapter = RecyclerViewAdapter()
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter
        recycler.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager(activity).orientation))
        viewModel.listLiveData.observe(this, Observer { list -> adapter.setItem(list) })

        viewModel.fpsLiveData.observe(this, Observer { _ ->

            val list1 = viewModel.pollingUsecase.fpsLiveData.value ?: listOf()
            val values1 = list1.mapIndexed { index, i -> Entry(index.toFloat(), i.toFloat(), null, null) }
            val set1 = LineDataSet(values1, "Data").apply { setCircleColor(Color.rgb(96, 96, 255)) }

            val list2 = viewModel.restUsecase.fpsLiveData.value ?: listOf()
            val values2 = list2.mapIndexed { index, i -> Entry(index.toFloat(), i.toFloat(), null, null) }
            val set2 = LineDataSet(values2, "Data").apply { setCircleColor(Color.rgb(255, 96, 96)) }

            chart.data = LineData(mutableListOf<ILineDataSet>().apply {
                add(set1)
                add(set2)
            })
            chart.notifyDataSetChanged()
            chart.invalidate()
        })
    }

    private fun setupGraph() {
        chart.apply {
            isDragEnabled = true
            setTouchEnabled(true)
            setScaleEnabled(true)
            setDrawGridBackground(false)
            setPinchZoom(true)
            setBackgroundColor(Color.WHITE)
        }
        chart.legend.apply {
            form = Legend.LegendForm.NONE
            // form = Legend.LegendForm.LINE
            textColor = Color.BLACK
        }
        chart.xAxis.apply {
            textColor = Color.BLACK
        }
        chart.axisLeft.apply {
            textColor = Color.BLACK
            setDrawGridLines(false)
        }
        chart.axisRight.apply {
            isEnabled = false
        }
    }
}
