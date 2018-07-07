package io.mindhouse.idee.ui.idea.list

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.mindhouse.idee.R
import io.mindhouse.idee.data.model.Board
import io.mindhouse.idee.data.model.Idea
import io.mindhouse.idee.ui.base.MvvmFragment
import io.mindhouse.idee.ui.idea.IdeaActivity
import kotlinx.android.synthetic.main.fragment_idea_list.*

/**
 * Created by kmisztal on 29/06/2018.
 *
 * @author Krzysztof Misztal
 */
class IdeaListFragment : MvvmFragment<IdeaListViewState, IdeaListViewModel>() {

    companion object {
        private const val KEY_BOARD = "board"

        //todo change to board id and observe changes!!
        fun newInstance(board: Board? = null): IdeaListFragment {
            val fragment = IdeaListFragment()
            val args = Bundle()
            args.putParcelable(KEY_BOARD, board)

            fragment.arguments = args
            return fragment
        }
    }

    private val adapter = IdeaRecyclerAdapter()
    var board: Board? = null
        set(value) {
            if (field != value) {
                field = value
                viewModel.board = board
            }
        }

    var fragmentCallbacks: FragmentCallbacks? = null

    //==========================================================================

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        viewModel.board = board
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_idea_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(view.context)

        val decoratorDrawable = ContextCompat.getDrawable(view.context, R.drawable.separator_idea_list)
        if (decoratorDrawable != null) {
            val decorator = DividerItemDecoration(view.context, LinearLayoutManager.VERTICAL)
            decorator.setDrawable(decoratorDrawable)
            recyclerView.addItemDecoration(decorator)
        }

        adapter.onItemClickedListener = { idea, _ ->
            fragmentCallbacks?.onIdeaSelected(idea)
        }

        addIdeaButton.setOnClickListener {
            val boardId = board?.id
            if (boardId != null) {
                val intent = IdeaActivity.newIntent(it.context, boardId)
                startActivity(intent)
            }
        }

        sortOrderButton.setOnClickListener {
            val ascending = !adapter.comparator.ascending
            adapter.comparator = adapter.comparator.copy(ascending = ascending)
        }

        sortButton.setOnClickListener {
            showSortDialog()
        }
    }

    override fun render(state: IdeaListViewState) {
        val context = context ?: return
        val name = state.board?.name ?: getString(R.string.app_name)
        activity?.title = name
        adapter.setItems(state.ideas)

        shareStatus.setText(state.shareStatus)
        val color = if (state.shareStatus == R.string.not_shared) {
            shareStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_share_gray, 0)
            ContextCompat.getColor(context, R.color.gray)
        } else {
            shareStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_share, 0)
            ContextCompat.getColor(context, R.color.blue)
        }

        shareStatus.setTextColor(color)

        if (state.isLoading) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }

        if (state.board == null) {
            addIdeaButton.hide()
        } else {
            addIdeaButton.show()
        }
    }

    //==========================================================================
    // private
    //==========================================================================

    private fun showSortDialog() {
        val context = context ?: return
        AlertDialog.Builder(context)
                .setTitle(R.string.sort_by)
                .setItems(R.array.sorting_options) { _, which ->
                    onSortingSelected(which)
                }
                .show()
    }

    private fun onSortingSelected(which: Int) {
        val sorting = when (which) {
            0 -> IdeaComparator.Mode.AVERAGE
            1 -> IdeaComparator.Mode.EASE
            2 -> IdeaComparator.Mode.CONFIDENCE
            3 -> IdeaComparator.Mode.IMPACT
            else -> throw IllegalArgumentException("Wrong sorting index: $which")
        }

        val comparator = adapter.comparator.copy(mode = sorting)
        adapter.comparator = comparator
    }

    //==========================================================================

    override fun createViewModel() =
            ViewModelProviders.of(this, viewModelFactory)[IdeaListViewModel::class.java]

    interface FragmentCallbacks {
        fun onIdeaSelected(idea: Idea)
    }
}