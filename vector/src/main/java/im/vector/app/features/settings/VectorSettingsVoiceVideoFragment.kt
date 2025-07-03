package im.vector.app.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.databinding.VectorSettingsVoiceVideoBinding
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.Session
import javax.inject.Inject
import androidx.core.content.edit

@AndroidEntryPoint
class VectorSettingsVoiceVideoFragment : Fragment() {

    private var _binding: VectorSettingsVoiceVideoBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var session: Session

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSettingsVoiceVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Âm thanh và hình ảnh"
        setupSwitches()
    }

    private fun setupSwitches() {
        setupSwitch(binding.switchFallbackCallSupport, "fallback_call_support")
        setupSwitch(binding.switchPreventAccidentalCall, "prevent_accidental_call")
    }

    private fun setupSwitch(switch: CompoundButton, key: String) {
        switch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                val prefs = requireContext().getSharedPreferences("voice_video_settings", 0)
                prefs.edit { putBoolean(key, isChecked) }
            }
        }

        // Gán giá trị ban đầu từ SharedPreferences
        val prefs = requireContext().getSharedPreferences("voice_video_settings", 0)
        switch.isChecked = prefs.getBoolean(key, false)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
