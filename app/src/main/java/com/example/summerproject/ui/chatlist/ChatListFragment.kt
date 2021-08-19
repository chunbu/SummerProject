package com.example.summerproject.ui.chatlist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.summerproject.DBKey.Companion.CHILD_CHAT
import com.example.summerproject.DBKey.Companion.DB_USERS
import com.example.summerproject.R
import com.example.summerproject.ui.chatdetail.ChatRoomActivity
import com.example.summerproject.databinding.FragmentChatlistBinding


class ChatListFragment : Fragment(R.layout.fragment_chatlist) {
    private lateinit var binding: FragmentChatlistBinding
    private lateinit var chatListAdapter: ChatListAdapter
    private val chatRoomList = mutableListOf<ChatListItem>()
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentChatlistBinding = FragmentChatlistBinding.bind(view)

        binding = fragmentChatlistBinding

        initchartListAdapter()

        chatRoomList.clear()

        initChartRecyclerView()

        initChatDB()
    }

    private fun initchartListAdapter() {
        chatListAdapter = ChatListAdapter(onItemClicked = { chatRoom ->
            context?.let {
                val intent = Intent(it, ChatRoomActivity::class.java)
                intent.putExtra("chatKey", chatRoom.key) // 인텐트로 키를 전달해서 start
                startActivity(intent)
            }
        })
    }

    private fun initChartRecyclerView() {
        binding.chatListRecyclerView.adapter = chatListAdapter
        binding.chatListRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun initChatDB() {
        val firebaseUser = auth.currentUser ?: return
        val chatDB =
            Firebase.database.reference.child(DB_USERS).child(firebaseUser.uid).child(CHILD_CHAT)

        // db에 있는 채팅 리스트를 불러와 각각 리스트에 더해준다.
        chatDB.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val model = it.getValue(ChatListItem::class.java)
                    model ?: return
                    chatRoomList.add(model)
                }
                chatListAdapter.submitList(chatRoomList)
                chatListAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // view가 새로 그려졌을 때;
    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        chatListAdapter.notifyDataSetChanged()
        val recyclerView = requireView().findViewById(R.id.chatListRecyclerView) as RecyclerView
        recyclerView.addItemDecoration(DividerItemDecoration(requireView().context, 1))
        chatListAdapter.notifyDataSetChanged()// view 를 다시 그림;

    }

}