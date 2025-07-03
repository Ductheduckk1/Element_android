package im.vector.app.features.settings.notifications.other

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.databinding.VectorSettingsNotificationOtherBinding
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.pushrules.RuleIds
import org.matrix.android.sdk.api.session.pushrules.RuleKind
import javax.inject.Inject

@AndroidEntryPoint
class VectorSettingsOtherNotificationFragment : Fragment() {

    private var _binding: VectorSettingsNotificationOtherBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var session: Session

    private val pushRuleService get() = session.pushRuleService()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSettingsNotificationOtherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Khác"
        setupSwitches()
        loadStates()
    }

    private fun setupSwitches() {
        setupSwitch(binding.switchRoomInvite, RuleIds.RULE_ID_INVITE_ME)
        setupSwitch(binding.switchCallInvite, RuleIds.RULE_ID_CALL)
        setupSwitch(binding.switchBotMessages, RuleIds.RULE_ID_SUPPRESS_BOTS_NOTIFICATIONS)
        setupSwitch(binding.switchRoomUpgrade, RuleIds.RULE_ID_TOMBSTONE)
    }

    private fun setupSwitch(switch: View, ruleId: String) {
        if (switch !is androidx.appcompat.widget.SwitchCompat && switch !is android.widget.Switch) return

        (switch as? CompoundButton)?.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                val rule = pushRuleService.getPushRules().content?.firstOrNull { it.ruleId == ruleId }
                if (rule != null) {
                    pushRuleService.updatePushRuleEnableStatus(RuleKind.OVERRIDE, rule, isChecked)
                }
            }
        }
    }

    private fun loadStates() {
        lifecycleScope.launch {
            val rules = pushRuleService.getPushRules().override.orEmpty()
            binding.switchRoomInvite.isChecked = rules.firstOrNull { it.ruleId == RuleIds.RULE_ID_INVITE_ME }?.enabled ?: false
            binding.switchCallInvite.isChecked = rules.firstOrNull { it.ruleId == RuleIds.RULE_ID_CALL }?.enabled ?: false
            binding.switchBotMessages.isChecked = rules.firstOrNull { it.ruleId == RuleIds.RULE_ID_SUPPRESS_BOTS_NOTIFICATIONS }?.enabled ?: false
            binding.switchRoomUpgrade.isChecked = rules.firstOrNull { it.ruleId == RuleIds.RULE_ID_TOMBSTONE }?.enabled ?: false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
