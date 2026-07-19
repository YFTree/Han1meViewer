package io.github.daisukikaffuchino.han1meviewer.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import cn.jzvd.JZUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.google.android.material.button.MaterialButton
import io.github.daisukikaffuchino.han1meviewer.R
import io.github.daisukikaffuchino.han1meviewer.logic.entity.HKeyframeEntity
import io.github.daisukikaffuchino.han1meviewer.ui.activity.MainActivity
import io.github.daisukikaffuchino.han1meviewer.ui.theme.HanimeTheme
import io.github.daisukikaffuchino.han1meviewer.util.createAlertDialog
import io.github.daisukikaffuchino.han1meviewer.util.showAlertDialog
import io.github.daisukikaffuchino.han1meviewer.util.showWithBlurEffect
import com.yenaly.yenaly_libs.utils.findActivityOrNull
import com.yenaly.yenaly_libs.utils.showShortToast

/**
 * @project Han1meViewer
 * @author Yenaly Liew
 * @time 2023/11/26 026 17:42
 */
class HKeyframeRvAdapter(
    private val videoCode: String,
    keyframe: HKeyframeEntity? = null,
) : BaseQuickAdapter<HKeyframeEntity.Keyframe, QuickViewHolder>(
    keyframe?.keyframes.orEmpty(),COMPARATOR
) {

    init {
        isStateViewEnable = true
    }

    /**
     * 是否是本地关键帧
     *
     * @return false if is shared, true otherwise.
     */
    var isLocal: Boolean = true

    var isShared: Boolean = false

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<HKeyframeEntity.Keyframe>() {
            override fun areItemsTheSame(
                oldItem: HKeyframeEntity.Keyframe,
                newItem: HKeyframeEntity.Keyframe,
            ) = oldItem.position == newItem.position

            override fun areContentsTheSame(
                oldItem: HKeyframeEntity.Keyframe,
                newItem: HKeyframeEntity.Keyframe,
            ) = oldItem == newItem
        }
    }

    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: HKeyframeEntity.Keyframe?,
    ) {
        item ?: return
        holder.setText(R.id.tv_keyframe, JZUtils.stringForTime(item.position))
        holder.setText(R.id.tv_index, "#${holder.bindingAdapterPosition + 1}")

        holder.setGone(R.id.btn_delete, !isLocal)
        holder.setGone(R.id.btn_edit, !isLocal)

        if (!item.prompt.isNullOrBlank()) {
            holder.setGone(R.id.tv_prompt, false)
            holder.setText(R.id.tv_prompt, "➥ " + item.prompt)
        } else {
            holder.setGone(R.id.tv_prompt, true)
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int,
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_h_keyframe, parent).also { viewHolder ->
            if (isShared) return@also
            viewHolder.getView<MaterialButton>(R.id.btn_edit).apply {
                setOnClickListener {
                    val position = viewHolder.bindingAdapterPosition
                    val item = getItem(position)
                    val promptState = mutableStateOf(item.prompt.orEmpty())
                    val positionState = mutableStateOf(item.position.toString())
                    val activity = context.findActivityOrNull<FragmentActivity>()
                    val lifecycleOwner = activity ?: return@setOnClickListener
                    val dialogContent = ComposeView(context).apply {
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                    }

                    val dialog = context.createAlertDialog {
                        setTitle(R.string.modify_h_keyframe)
                        setView(dialogContent)
                        setPositiveButton(R.string.confirm) { _, _ ->
                            val prompt = promptState.value
                            val pos = positionState.value.toLongOrNull()
                                ?: return@setPositiveButton showShortToast(R.string.unknown_error)
                            when (context) {
                                is MainActivity -> {
                                    context.viewModel.modifyHKeyframe(
                                        videoCode, item, HKeyframeEntity.Keyframe(
                                            position = pos,
                                            prompt = prompt
                                        )
                                    )
                                     showShortToast(R.string.modify_success)
                                }
                            }
                        }
                        setNegativeButton(R.string.cancel, null)
                    }
                    dialog.showWithBlurEffect()

                    val contentLifecycleOwner =
                        dialogContent.findViewTreeLifecycleOwner() ?: lifecycleOwner
                    val viewModelStoreOwner =
                        dialogContent.findViewTreeViewModelStoreOwner() ?: activity
                    val savedStateRegistryOwner =
                        dialogContent.findViewTreeSavedStateRegistryOwner() ?: activity
                    dialog.window?.decorView?.apply {
                        setViewTreeLifecycleOwner(contentLifecycleOwner)
                        setViewTreeViewModelStoreOwner(viewModelStoreOwner)
                        setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
                    }
                    dialogContent.apply {
                        setViewTreeLifecycleOwner(contentLifecycleOwner)
                        setViewTreeViewModelStoreOwner(viewModelStoreOwner)
                        setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
                        setContent {
                            HanimeTheme {
                                HKeyframeEditContent(
                                    promptState = promptState,
                                    positionState = positionState,
                                )
                            }
                        }
                    }
                }
            }
            viewHolder.getView<MaterialButton>(R.id.btn_delete).apply {
                setOnClickListener {
                    val position = viewHolder.bindingAdapterPosition
                    val item = getItem(position)
                    it.context.showAlertDialog {
                        setTitle(R.string.sure_to_delete)
                        setMessage(JZUtils.stringForTime(item.position))
                        setPositiveButton(R.string.confirm) { _, _ ->
                            when (context) {
                                is MainActivity -> {
                                    context.viewModel.removeHKeyframe(videoCode, item)
                                     showShortToast(R.string.delete_success)
                                }
                            }
                        }
                        setNegativeButton(R.string.cancel, null)
                    }
                }
            }
        }
    }
}

@Composable
private fun HKeyframeEditContent(
    promptState: MutableState<String>,
    positionState: MutableState<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = promptState.value,
            onValueChange = { promptState.value = it },
            label = { Text(stringResource(R.string.prompt)) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
        )
        OutlinedTextField(
            value = positionState.value,
            onValueChange = { positionState.value = it },
            label = { Text(stringResource(R.string.position_ms)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}
