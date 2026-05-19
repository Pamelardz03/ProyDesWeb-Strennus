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
import androidx.recyclerview.widget.LinearLayoutManager
import com.estudiante.strennus_proyweb.data.AppDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        binding.ListaRanking.layoutManager = LinearLayoutManager(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDataBase.getInstance(requireContext())
            val usuarios = db.usuarioDao().getAllUsuarios()
                .sortedByDescending { it.puntos }

            withContext(Dispatchers.Main) {
                val items = usuarios.mapIndexed { index, u ->
                    RankingUser(index + 1, u.name, u.puntos)
                }
                binding.ListaRanking.adapter = RankingAdapter(items)
            }
        }
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