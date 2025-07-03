package im.vector.app.features.settings.labs

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.databinding.VectorSettingsLabsBinding
import im.vector.app.features.MainActivity
import im.vector.app.features.MainActivityArgs
import im.vector.app.features.VectorFeatures
import im.vector.app.features.home.room.threads.ThreadsManager
import im.vector.app.features.settings.VectorPreferences
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.settings.LightweightSettingsStorage
import javax.inject.Inject

@AndroidEntryPoint
class VectorSettingsLabsFragment : Fragment(), MavericksView {

    private var _binding: VectorSettingsLabsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VectorSettingsLabsViewModel by fragmentViewModel()


    @Inject lateinit var vectorPreferences: VectorPreferences
    @Inject lateinit var lightweightSettingsStorage: LightweightSettingsStorage
    @Inject lateinit var threadsManager: ThreadsManager
    @Inject lateinit var vectorFeatures: VectorFeatures
    @Inject lateinit var session: Session

    override fun invalidate() {
        // Không cần xử lý nếu chỉ dùng event
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?

    ): View {
        _binding = VectorSettingsLabsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Labs"
        bindSettings()
    }

    private fun bindSettings() {
        binding.apply {
            // 1. Swipe to reply
            switchSwipeReply.isChecked = vectorPreferences.swipeToReplyIsEnabled()
            switchSwipeReply.setOnCheckedChangeListener { _, isChecked ->
                vectorPreferences.setSwipeToReply(isChecked)
            }

            // 2. LaTeX
            switchLatex.isChecked = vectorPreferences.isLatexEnabled()
            switchLatex.setOnCheckedChangeListener { _, isChecked ->
                vectorPreferences.setLatexEnabled(isChecked)
                restartApp()
            }

            // 3. Threaded messages
            switchThreaded.isChecked = vectorPreferences.areThreadMessagesEnabled()
            switchThreaded.setOnCheckedChangeListener { _, isChecked ->
                onThreadsPreferenceClickedInterceptor(isChecked)
            }

            // 4. Auto report decryption errors
            switchAutoReport.isChecked = vectorPreferences.labsAutoReportUISI()
            switchAutoReport.setOnCheckedChangeListener { _, isChecked ->
                vectorPreferences.setLabsAutoReportUISI(isChecked)
            }

            // 5. Live location sharing
            switchLocation.isChecked = vectorPreferences.isLiveLocationEnabled()
            switchLocation.setOnCheckedChangeListener { _, isChecked ->
                vectorPreferences.setLiveLocationSharingEnabled(isChecked)
            }

            // 6. LaoApp call permission
            switchLaoapp.isChecked = vectorPreferences.isLaoAppCallShortcutEnabled()
            switchLaoapp.setOnCheckedChangeListener { _, isChecked ->
                vectorPreferences.setLaoAppCallShortcutEnabled(isChecked)
            }

            // 7. Deferred DMs
            switchDeferredDm.isChecked = vectorPreferences.isDeferredDmEnabled()
            switchDeferredDm.setOnCheckedChangeListener { _, isChecked ->
                vectorPreferences.setDeferredDmEnabled(isChecked)
            }

            // 8. Rich text editor
            switchRichText.isChecked = vectorPreferences.isRichTextEditorEnabled()
            switchRichText.setOnCheckedChangeListener { _, isChecked ->
                vectorPreferences.setRichTextEditorEnabled(isChecked)
            }

            // 9. New session manager
            switchSessionManager.isChecked = vectorPreferences.isNewSessionManagerEnabled()
            switchSessionManager.setOnCheckedChangeListener { _, isChecked ->
                vectorPreferences.setNewSessionManagerEnabled(isChecked)
            }

            // 10. Client info recording
            switchClientInfo.isChecked = vectorPreferences.isClientInfoRecordingEnabled()
            switchClientInfo.setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    if (isChecked) {
                        viewModel.handle(VectorSettingsLabsAction.UpdateClientInfo)
                    } else {
                        viewModel.handle(VectorSettingsLabsAction.DeleteRecordedClientInfo)
                    }
                }
            }
        }
    }

    private fun onThreadsPreferenceClickedInterceptor(userChecked: Boolean) {
        if (!isThreadsSupported() && userChecked)
        {
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Enable Threads")
                    .setMessage(threadsManager.getLabsEnableThreadsMessage())
                    .setPositiveButton("Try it out") { _, _ -> onThreadsPreferenceClicked() }
                    .setNegativeButton("Not now") { _, _ -> binding.switchThreaded.isChecked = false }
                    .show()
                    .findViewById<TextView>(android.R.id.message)
                    ?.apply {
                        linksClickable = true
                    }
        } else {
            onThreadsPreferenceClicked()
        }
    }

    private fun onThreadsPreferenceClicked() {
        vectorPreferences.setThreadFlagChangedManually()
        vectorPreferences.setShouldMigrateThreads(!vectorPreferences.areThreadMessagesEnabled())
        lightweightSettingsStorage.setThreadMessagesEnabled(vectorPreferences.areThreadMessagesEnabled())
        restartApp()
    }

    private fun restartApp() {
        MainActivity.restartApp(requireActivity(), MainActivityArgs(clearCache = true))
    }
    private fun isThreadsSupported(): Boolean {
        return session.homeServerCapabilitiesService().getHomeServerCapabilities().canUseThreading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
