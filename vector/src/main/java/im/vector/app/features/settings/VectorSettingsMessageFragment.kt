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
import im.vector.app.databinding.VectorSettingsMessageBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VectorSettingsMessageFragment : Fragment() {

    private var _binding: VectorSettingsMessageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSettingsMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Message"
        setupSwitches()
    }

    private fun setupSwitches() {
        with(binding) {
            setupSwitch(switchMediaConfirm)
            setupSwitch(switchMessageBubbles)
            setupSwitch(switchReadReceipts)
            setupSwitch(switchTimestamps)
            setupSwitch(switchRemovedMessages)
            setupSwitch(switchEmojiKeyboard)
            setupSwitch(switchChatEffects)
            setupSwitch(switchLastestUserInfo)
            setupSwitch(switchJoinLeave)
            setupSwitch(switchAccountEvents)
            setupSwitch(switchTypingNotify)
            setupSwitch(switchSendEnter)
            setupSwitch(switchInlinePreview)
            setupSwitch(switchAutoplay)
            setupSwitch(switchMarkdown)
            setupSwitch(switchDirectShare)
        }
    }

    private fun setupSwitch(switch: CompoundButton) {
        switch.setOnCheckedChangeListener { _, _ ->
            lifecycleScope.launch {
                // TODO: Lưu trạng thái nếu cần thiết
                // Ví dụ: settings.set(key, isChecked)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
