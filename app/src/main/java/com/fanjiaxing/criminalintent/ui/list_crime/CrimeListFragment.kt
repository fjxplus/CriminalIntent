package com.fanjiaxing.criminalintent.ui.list_crime

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fanjiaxing.criminalintent.R
import com.fanjiaxing.criminalintent.databinding.FragmentCrimeListBinding
import com.fanjiaxing.criminalintent.databinding.ListCrimeItemBinding
import com.fanjiaxing.criminalintent.logic.model.Crime
import java.util.*

private const val DATE_FORMAT = "yyyy-MM-dd kk:mm"

class CrimeListFragment : Fragment() {

    private val binding get() = _binding!!

    private var _binding: FragmentCrimeListBinding? = null

    private val viewModel by lazy { ViewModelProvider(this).get(CrimeListViewModel::class.java) }

    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrimeListBinding.inflate(inflater, container, false)
        val manager = LinearLayoutManager(context)
        binding.crimeRecycleView.layoutManager = manager
        binding.crimeRecycleView.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.crimeLiveData.observe(viewLifecycleOwner, { crimeList ->
            crimeList?.let {
                updateUI(crimeList)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.getCrimes()
        binding.newCrime.setOnClickListener{
            val crime = Crime()
            viewModel.addCrime(crime)
            callbacks?.onCrimeSelected(crime.id)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.new_crime -> {
            val crime = Crime()
            viewModel.addCrime(crime)
            callbacks?.onCrimeSelected(crime.id)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun updateUI(crimeList: List<Crime>) {
        if (crimeList.isNotEmpty()){
            binding.noCrimeLayout.visibility = View.GONE
            adapter = CrimeAdapter(crimeList)
            binding.crimeRecycleView.adapter = adapter
        }else {
            binding.noCrimeLayout.visibility = View.VISIBLE
        }
    }

    private inner class CrimeAdapter(private val crimeList: List<Crime>) :
        RecyclerView.Adapter<CrimeAdapter.ViewHolder>() {

        private inner class ViewHolder(binding: ListCrimeItemBinding) :
            RecyclerView.ViewHolder(binding.root), View.OnClickListener {
            val crimeTitle = binding.crimeTitle
            val crimeDate = binding.crimeDate
            val isSolved = binding.imageView

            private lateinit var crime: Crime

            init {
                itemView.setOnClickListener(this)
            }

            fun bindCrime(crime: Crime) {
                crimeTitle.text = crime.title

                val date = android.text.format.DateFormat.format(DATE_FORMAT, crime.date)
                crimeDate.text = date

                isSolved.visibility = when (crime.isSolved) {
                    true -> View.VISIBLE
                    false -> View.GONE
                }

                this.crime = crime
            }

            override fun onClick(v: View?) {
                callbacks?.onCrimeSelected(crime.id)

            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding =
                ListCrimeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val crime = crimeList[position]
            holder.bindCrime(crime)
        }

        override fun getItemCount(): Int {
            return crimeList.size
        }
    }
}