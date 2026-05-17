package com.estudiante.strennus_proyweb.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.estudiante.strennus_proyweb.databinding.FragmentRankingBinding
import com.estudiante.strennus_proyweb.databinding.ItemRankingBinding
import com.estudiante.strennus_proyweb.R

class RankingFragment : Fragment() {

    private var _binding: FragmentRankingBinding? = null
    private val binding get() = _binding!!


    data class RankingUser(val position: Int, val name: String, val points: Int)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRankingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usuarios = listOf(
            RankingUser(1, "Alex Johnson", 2840),
            RankingUser(2, "Maria Garcia", 2735),
            RankingUser(3, "Chris Lee", 2680),
            RankingUser(4, "Sarah Smith", 2590),
            RankingUser(5, "Mike Wilson", 2510),
            RankingUser(6, "Persona 6", 2310),
            RankingUser(7, "Persona 7", 2200),
            RankingUser(8, "Persona 8", 2180),
            RankingUser(9, "Persona 9", 1890),
            RankingUser(10, "Persona 10", 1610)
        )

        binding.ListaRanking.adapter = RankingAdapter(usuarios)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class RankingAdapter(private val lista: List<RankingUser>) :
        RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

        inner class ViewHolder(val b: ItemRankingBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(ItemRankingBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = lista.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = lista[position]
            holder.b.tvRankNumber.text = user.position.toString()
            holder.b.tvUserName.text = user.name
            holder.b.tvUserPoints.text = "${user.points} puntos"

            if (user.position <= 3) {
                holder.b.circleBackground.setBackgroundResource(R.drawable.bg_circle_red)
            } else {
                holder.b.circleBackground.setBackgroundResource(R.drawable.bg_circle_grey)
            }
        }
    }
}