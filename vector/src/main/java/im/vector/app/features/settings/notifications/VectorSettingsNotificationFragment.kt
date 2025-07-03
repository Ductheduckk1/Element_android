package im.vector.app.features.settings.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.databinding.VectorSettingsNotificationsBinding
import im.vector.app.features.settings.VectorPreferences
import im.vector.app.features.settings.notifications.VectorSettingsNotificationViewAction
import im.vector.app.features.settings.notifications.VectorSettingsNotificationViewModel
import im.vector.app.features.settings.notifications.defaults.VectorSettingsDefaultNotificationFragment
import im.vector.app.features.settings.notifications.keywordandmentions.VectorSettingsKeywordAndMentionsNotificationFragment
import im.vector.app.features.settings.notifications.other.VectorSettingsOtherNotificationFragment
import javax.inject.Inject

@AndroidEntryPoint
class VectorSettingsNotificationFragment : Fragment(), MavericksView{

    private var _binding: VectorSettingsNotificationsBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var vectorPreferences: VectorPreferences
    private val viewModel: VectorSettingsNotificationViewModel by fragmentViewModel()
    override fun invalidate() {
        // Không cần xử lý nếu chỉ dùng event
    }
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSettingsNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Thông báo"
        setupSwitches()
        setupClicks()
        restoreStates()
    }

    private fun setupSwitches() {
        binding.switchEnableDeviceNotif.setOnCheckedChangeListener { _, isChecked ->
            val action = if (isChecked) {
                VectorSettingsNotificationViewAction.EnableNotificationsForDevice("")
            } else {
                VectorSettingsNotificationViewAction.DisableNotificationsForDevice
            }
            viewModel.handle(action)
        }

        binding.switchEnableInAppNotif.setOnCheckedChangeListener { _, isChecked ->
            vectorPreferences.setInAppNotificationEnabled(isChecked)
        }

        binding.switchShowDecrypted.setOnCheckedChangeListener { _, isChecked ->
            vectorPreferences.setShowDecryptedContentInNotifications(isChecked)
        }

        binding.switchPinMissedRooms.setOnCheckedChangeListener { _, isChecked ->
            vectorPreferences.setPinMissedNotificationsRooms(isChecked)
        }

        binding.switchPinUnreadRooms.setOnCheckedChangeListener { _, isChecked ->
            vectorPreferences.setPinUnreadRooms(isChecked)
        }
    }

    private fun setupClicks() {
//        binding.itemDeviceNotifDetails.setOnClickListener {
//            navigateTo(DeviceNotificationsSettingsFragment::class.java)
//        }
        binding.itemNotificationDefault.setOnClickListener {
            navigateTo(VectorSettingsDefaultNotificationFragment::class.java)
        }
        binding.itemKeywordsMentions.setOnClickListener {
            navigateTo(VectorSettingsKeywordAndMentionsNotificationFragment::class.java)
        }
        binding.itemNotificationOther.setOnClickListener {
            navigateTo(VectorSettingsOtherNotificationFragment::class.java)
        }
    }

    private fun restoreStates() {
        binding.switchEnableInAppNotif.isChecked = vectorPreferences.areInAppNotificationsEnabled()
        binding.switchShowDecrypted.isChecked = vectorPreferences.showDecryptedContentInNotifications()
        binding.switchPinMissedRooms.isChecked = vectorPreferences.pinMissedNotificationsRooms()
        binding.switchPinUnreadRooms.isChecked = vectorPreferences.pinUnreadRooms()
    }

    private fun navigateTo(fragmentClass: Class<out Fragment>) {
        parentFragmentManager.beginTransaction()
                .replace((view?.parent as ViewGroup).id, fragmentClass, null)
                .addToBackStack(null)
                .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
