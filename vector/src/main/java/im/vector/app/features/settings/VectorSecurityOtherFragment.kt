package im.vector.app.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.databinding.VectorSecurityOtherFragmentBinding
import javax.inject.Inject
import org.matrix.android.sdk.api.session.Session

@AndroidEntryPoint
class VectorSecurityOtherFragment : Fragment() {

    @Inject lateinit var session: Session
    @Inject lateinit var vectorPreferences: VectorPreferences


    private var _binding: VectorSecurityOtherFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSecurityOtherFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Khác"

        // Switch logic
        binding.switchIncognitoKeyboard.setOnCheckedChangeListener { _, _ ->

        }

        binding.switchPreventScreenshot.setOnCheckedChangeListener { _,_ ->

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
