package im.vector.app.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.core.utils.copyToClipboard
import im.vector.app.databinding.VectorSettingsAdvancedSettingsBinding
import im.vector.app.features.analytics.plan.MobileScreen
import im.vector.app.features.home.NightlyProxy
import im.vector.app.features.rageshake.RageShake
import im.vector.lib.strings.CommonStrings
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.Session
import javax.inject.Inject

@AndroidEntryPoint
class VectorSettingsAdvancedSettingsFragment : Fragment() {

    @Inject lateinit var nightlyProxy: NightlyProxy
    @Inject lateinit var vectorPreferences: VectorPreferences
    @Inject lateinit var session: Session

    private var _binding: VectorSettingsAdvancedSettingsBinding? = null
    private val binding get() = _binding!!

    private var rageshake: RageShake? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Analytics
        (activity as? VectorBaseActivity<*>)?.analyticsScreenName = MobileScreen.ScreenName.SettingsAdvanced
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSettingsAdvancedSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Cài đặt nâng cao"
        setupRageShakeSection()
        setupDevToolsSection()
    }

    override fun onResume() {
        super.onResume()
        rageshake = (activity as? VectorBaseActivity<*>)?.rageShake
        rageshake?.interceptor = {
            (activity as? VectorBaseActivity<*>)?.showSnackbar(getString(CommonStrings.rageshake_detected))
        }
    }

    override fun onPause() {
        super.onPause()
        rageshake?.interceptor = null
        rageshake = null
    }

    private fun setupDevToolsSection() {
        binding.itemAccessToken.setOnClickListener {
            copyToClipboard(requireContext(), session.sessionParams.credentials.accessToken)
        }

        if (session.cryptoService().supportKeyRequestInspection()) {
            binding.itemKeyRequests.visibility = View.VISIBLE
            binding.itemKeyRequests.setOnClickListener {
                // TODO: navigate to KeyRequestsFragment
            }
        } else {
            binding.itemKeyRequests.visibility = View.GONE
        }
    }

    private fun setupRageShakeSection() {
        val isAvailable = RageShake.isAvailable(requireContext())
        if (!isAvailable) {
            binding.groupRageshake.visibility = View.GONE
            return
        }

        binding.switchRageshake.isChecked = vectorPreferences.isUseRageShakeEnabled()

        binding.switchRageshake.setOnCheckedChangeListener { _, isChecked ->
            vectorPreferences.setUseRageShake(isChecked)
            if (isChecked) rageshake?.start() else rageshake?.stop()
        }

        val threshold = vectorPreferences.getRageShakeDetectionThreshold()
        binding.seekbarRageshake.progress = threshold

        binding.seekbarRageshake.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                vectorPreferences.setRageShakeDetectionThreshold(progress)
                rageshake?.setSensitivity(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
