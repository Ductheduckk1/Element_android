package im.vector.app.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.databinding.VectorSettingsRootBinding
import im.vector.app.features.navigation.Navigator
import im.vector.app.features.settings.legals.LegalsFragment
import im.vector.app.features.settings.notifications.VectorSettingsNotificationFragment
import javax.inject.Inject

@AndroidEntryPoint
class VectorSettingsRootFragment : Fragment() {

    private var _binding: VectorSettingsRootBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var navigator: Navigator

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSettingsRootBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Cài đặt"

        binding.itemNotifications.setOnClickListener {
            navigateTo(VectorSettingsNotificationFragment::class.java)
        }

        binding.itemMessage.setOnClickListener {
            navigateTo(VectorSettingsMessageFragment::class.java)
        }

        binding.itemCall.setOnClickListener {
            navigateTo(VectorSettingsVoiceVideoFragment::class.java)
        }

        binding.itemContact.setOnClickListener {
            navigateTo(VectorSettingsContactFragment::class.java)
        }

        binding.itemUiLanguage.setOnClickListener {
            navigateTo(VectorSettingsPreferencesFragment::class.java)
        }

        binding.itemSecurity.setOnClickListener {
            navigateTo(VectorSettingsSecurityFragment::class.java)
        }

        binding.itemOther.setOnClickListener {
            navigateTo(VectorSettingsOtherSettingsFragment::class.java)
        }

        binding.itemHelp.setOnClickListener {
            navigateTo(VectorSettingsHelpAboutFragment::class.java)
        }

        binding.itemPrivacy.setOnClickListener {
            navigateTo(LegalsFragment::class.java)
        }
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
