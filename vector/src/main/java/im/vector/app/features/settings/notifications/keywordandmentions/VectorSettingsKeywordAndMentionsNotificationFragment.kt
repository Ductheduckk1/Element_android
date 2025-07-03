package im.vector.app.features.settings.notifications.keywordandmentions

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.databinding.VectorSettingsNotificationMentionsAndKeywordsBinding
import im.vector.app.features.settings.VectorPreferences
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.pushrules.RuleIds
import org.matrix.android.sdk.api.session.pushrules.RuleKind
import org.matrix.android.sdk.api.session.pushrules.rest.PushRule
import javax.inject.Inject

@AndroidEntryPoint
class VectorSettingsKeywordAndMentionsNotificationFragment : Fragment() {

    private var _binding: VectorSettingsNotificationMentionsAndKeywordsBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var session: Session
    @Inject lateinit var vectorPreferences: VectorPreferences

    private val pushRuleService get() = session.pushRuleService()

    private val keywords = mutableSetOf<String>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSettingsNotificationMentionsAndKeywordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Đề cập và từ khóa"
        setupSwitches()
        setupKeywordInput()
        loadStates()
    }

    private fun setupSwitches() {
        setupSwitch(binding.switchDisplayName, RuleIds.RULE_ID_CONTAIN_DISPLAY_NAME)
        setupSwitch(binding.switchUsername, RuleIds.RULE_ID_CONTAIN_USER_NAME)
        setupSwitch(binding.switchAtRoom, RuleIds.RULE_ID_ROOM_NOTIF)

        binding.switchKeywords.setOnCheckedChangeListener { _, isChecked ->
            binding.cardKeywords.isVisible = isChecked
            keywords.forEach {
                updatePushRule(it, isChecked)
            }
        }
    }

    private fun setupSwitch(switch: CompoundButton, ruleId: String) {
        switch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                val rule = pushRuleService.getPushRules().content?.firstOrNull { it.ruleId == ruleId }
                if (rule != null) {
                    pushRuleService.updatePushRuleEnableStatus(RuleKind.CONTENT, rule, isChecked)
                }
            }
        }
    }

    private fun setupKeywordInput() {
        binding.inputAddKeyword.setOnEditorActionListener { v, _, _ ->
            val keyword = v.text.toString().trim()
            if (keyword.isNotEmpty() && keywords.add(keyword)) {
                addKeywordChip(keyword)
                addKeywordRule(keyword)
                v.text = null
            }
            true
        }
    }

    private fun addKeywordChip(keyword: String) {
        val chip = Chip(requireContext()).apply {
            text = keyword
            isCloseIconVisible = true
            // Set chip background and text color based on the design
            chipBackgroundColor = ColorStateList.valueOf("#D8DEF8".toColorInt())
            setTextColor("#021B84".toColorInt())
            closeIconTint = ColorStateList.valueOf("#021B84".toColorInt())
            setOnCloseIconClickListener {
                keywords.remove(keyword)
                binding.chipGroupKeywords.removeView(this)
                removeKeywordRule(keyword)
            }
        }
        binding.chipGroupKeywords.addView(chip)
    }

    private fun loadStates() {
        lifecycleScope.launch {
            val rules = pushRuleService.getPushRules().content.orEmpty()
            binding.switchDisplayName.isChecked = rules.firstOrNull { it.ruleId == RuleIds.RULE_ID_CONTAIN_DISPLAY_NAME }?.enabled ?: false
            binding.switchUsername.isChecked = rules.firstOrNull { it.ruleId == RuleIds.RULE_ID_CONTAIN_USER_NAME }?.enabled ?: false
            binding.switchAtRoom.isChecked = rules.firstOrNull { it.ruleId == RuleIds.RULE_ID_ROOM_NOTIF }?.enabled ?: false
            binding.switchKeywords.isChecked = rules.any { !it.ruleId.startsWith(".") && it.enabled }
            val keywordRules = rules.filter { !it.ruleId.startsWith(".") }
            keywords.clear()
            keywordRules.forEach {
                keywords.add(it.ruleId)
                addKeywordChip(it.ruleId)
            }
            binding.cardKeywords.isVisible = binding.switchKeywords.isChecked
        }
    }

    private fun addKeywordRule(keyword: String) {
        lifecycleScope.launch {
            val rule = PushRule(
                    ruleId = keyword,
                    enabled = true,
                    pattern = keyword,
                    actions = listOf("notify")
            )
            pushRuleService.addPushRule(RuleKind.CONTENT, rule)
        }
    }

    private fun removeKeywordRule(keyword: String) {
        lifecycleScope.launch {
            pushRuleService.removePushRule(RuleKind.CONTENT, keyword)
        }
    }

    private fun updatePushRule(keyword: String, enabled: Boolean) {
        lifecycleScope.launch {
            val rule = pushRuleService.getPushRules().content?.firstOrNull { it.ruleId == keyword } ?: return@launch
            pushRuleService.updatePushRuleEnableStatus(RuleKind.CONTENT, rule, enabled)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
