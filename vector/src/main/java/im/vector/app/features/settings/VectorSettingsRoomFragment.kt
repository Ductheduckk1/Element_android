package im.vector.app.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.databinding.VectorSettingsRoomDirectoryBinding
import javax.inject.Inject

@AndroidEntryPoint
class VectorSettingsRoomFragment : Fragment() {

    private var _binding: VectorSettingsRoomDirectoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSettingsRoomDirectoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Room Directory"
        setupSwitch()
    }

    private fun setupSwitch() {
        binding.switchShowExplicitRooms.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
            // TODO: Lưu trạng thái vào SharedPreferences hoặc ViewModel nếu cần
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
