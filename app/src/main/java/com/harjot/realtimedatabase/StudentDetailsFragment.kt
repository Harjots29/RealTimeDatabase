package com.harjot.realtimedatabase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.harjot.realtimedatabase.databinding.FragmentStudentBinding
import com.harjot.realtimedatabase.databinding.FragmentStudentDetailsBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudentDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudentDetailsFragment : Fragment() {
    lateinit var binding: FragmentStudentDetailsBinding
    lateinit var mainActivity: MainActivity
    var name = ""
    var department = ""
    var rollNo = 0
    var image = ""
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = activity as MainActivity
        arguments?.let {
            name = it.getString("name","")
            department = it.getString("department","")
            rollNo = it.getInt("rollNo",0)
            image = it.getString("image","")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentDetailsBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvName.setText(name)
        binding.tvDepartment.setText(department)
        binding.tvRollNo.setText(rollNo.toString())
//        binding.img.setImageURI(image.toUri())
        Glide.with(mainActivity)
            .load(image)
            .placeholder(R.drawable.ic_img)
            .into(binding.img)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StudentDetailsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StudentDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}