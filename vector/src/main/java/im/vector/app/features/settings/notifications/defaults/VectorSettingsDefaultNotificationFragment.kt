package im.vector.app.features.settings.notifications.defaults

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.databinding.VectorSettingsNotificationDefaultBinding
import im.vector.app.features.analytics.plan.MobileScreen
import im.vector.app.features.settings.VectorPreferences
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.pushrules.rest.PushRule
import org.matrix.android.sdk.api.session.pushrules.RuleIds
import org.matrix.android.sdk.api.session.pushrules.RuleKind
import org.matrix.android.sdk.api.session.pushrules.rest.RuleSet
import javax.inject.Inject

@AndroidEntryPoint
class VectorSettingsDefaultNotificationFragment : Fragment() {

    private var _binding: VectorSettingsNotificationDefaultBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var session: Session
    @Inject lateinit var vectorPreferences: VectorPreferences

    private val pushRuleService get() = session.pushRuleService()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSettingsNotificationDefaultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Thông báo mặc định"
        setupSwitches()
        loadCurrentStates()
    }

    private fun setupSwitches() {
        setupSwitch(binding.switchDirectMessages, RuleIds.RULE_ID_ONE_TO_ONE_ROOM)
        setupSwitch(binding.switchGroupMessages, RuleIds.RULE_ID_ALL_OTHER_MESSAGES_ROOMS)
        setupSwitch(binding.switchEncryptedDirect, RuleIds.RULE_ID_ONE_TO_ONE_ENCRYPTED_ROOM)
        setupSwitch(binding.switchEncryptedGroup, RuleIds.RULE_ID_ENCRYPTED)
    }

    private fun setupSwitch(switch: CompoundButton, ruleId: String) {
        switch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                updatePushRuleEnabled(ruleId, isChecked)
            }
        }
    }

    private fun loadCurrentStates() {
        lifecycleScope.launch {
            binding.switchDirectMessages.isChecked = getPushRuleById(RuleIds.RULE_ID_ONE_TO_ONE_ROOM)?.second?.enabled ?: false
            binding.switchGroupMessages.isChecked = getPushRuleById(RuleIds.RULE_ID_ALL_OTHER_MESSAGES_ROOMS)?.second?.enabled ?: false
            binding.switchEncryptedDirect.isChecked = getPushRuleById(RuleIds.RULE_ID_ONE_TO_ONE_ENCRYPTED_ROOM)?.second?.enabled ?: false
            binding.switchEncryptedGroup.isChecked = getPushRuleById(RuleIds.RULE_ID_ENCRYPTED)?.second?.enabled ?: false
        }
    }

    private fun getPushRuleById(ruleId: String): Pair<RuleKind, PushRule>? {
        val allRules = pushRuleService.getPushRules()
        return allRules.getAllWithKind().firstOrNull { it.second.ruleId == ruleId }
    }

    private suspend fun updatePushRuleEnabled(ruleId: String, enabled: Boolean) {
        val (kind, rule) = getPushRuleById(ruleId) ?: return
        pushRuleService.updatePushRuleEnableStatus(kind, rule, enabled)
    }

    private fun RuleSet.getAllWithKind(): List<Pair<RuleKind, PushRule>> {
        val result = mutableListOf<Pair<RuleKind, PushRule>>()
        override?.forEach { result.add(RuleKind.OVERRIDE to it) }
        content?.forEach { result.add(RuleKind.CONTENT to it) }
        room?.forEach { result.add(RuleKind.ROOM to it) }
        sender?.forEach { result.add(RuleKind.SENDER to it) }
        underride?.forEach { result.add(RuleKind.UNDERRIDE to it) }
        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
