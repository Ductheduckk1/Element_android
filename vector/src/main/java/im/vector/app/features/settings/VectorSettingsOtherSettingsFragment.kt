package im.vector.app.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.databinding.VectorSettingsOtherSettingsBinding
import im.vector.app.features.settings.labs.VectorSettingsLabsFragment

@AndroidEntryPoint
class VectorSettingsOtherSettingsFragment : Fragment() {

    private var _binding: VectorSettingsOtherSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSettingsOtherSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Cài đặt khác"

        binding.itemRoomDirectory.setOnClickListener {
            parentFragmentManager.beginTransaction()
                    .replace(id, VectorSettingsRoomFragment())
                    .addToBackStack(null)
                    .commit()
        }

        binding.itemAdvancedSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                    .replace(id, VectorSettingsAdvancedSettingsFragment())
                    .addToBackStack(null)
                    .commit()
        }

        binding.itemLab.setOnClickListener {
            parentFragmentManager.beginTransaction()
                    .replace(id, VectorSettingsLabsFragment())
                    .addToBackStack(null)
                    .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
