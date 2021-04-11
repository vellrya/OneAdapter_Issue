package a.b.c

import a.b.c.databinding.ActivityMainBinding
import a.b.c.databinding.StickerSetHeaderBinding
import a.b.c.databinding.StickerViewBinding
import android.animation.ObjectAnimator
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.idanatz.oneadapter.OneAdapter
import com.idanatz.oneadapter.external.event_hooks.ClickEventHook
import com.idanatz.oneadapter.external.interfaces.Diffable
import com.idanatz.oneadapter.external.modules.ItemModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    val SPAN_COUNT = 5

    inner class StickerModule : ItemModule<StickerModel>() {
        //sticker in main vertical panel
        init {
            config {
                layoutResource = R.layout.sticker_view
//                firstBindAnimation = AnimatorInflater.loadAnimator(this@ChatActivity, R.animator.sticker_first_bind)
                firstBindAnimation = ObjectAnimator().apply {
                    setPropertyName("alpha")
                    setFloatValues(0f, 1f)
                    duration = 200
                }
            }
            onBind { model, viewBinder, metadata ->
//                val lav = viewBinder.findViewById<ImageView>(R.id.stickerLAV)


                viewBinder.bindings(StickerViewBinding::bind).run {

                    val size = binding.root.width / SPAN_COUNT
                    stickerLAV.layoutParams = LinearLayout.LayoutParams(size, size)
                    stickerLAV.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, model.color))
                }


            }
            onUnbind { model, viewBinder, metadata ->
            }

            eventHooks += ClickEventHook<StickerModel>().apply {
                onClick { model, viewBinder, metadata ->
                }
            }


        }
    }

    inner class StickerSetHeaderModule : ItemModule<StickerSetModel>() {
        //sticker set text (header) in main vertical panel
        init {
            config {
                layoutResource = R.layout.sticker_set_header
            }
            onBind { model, viewBinder, metadata ->

                viewBinder.bindings(StickerSetHeaderBinding::bind).run {
                    stickerTitleTv.text = model.name
                    (viewBinder.rootView.layoutParams as StaggeredGridLayoutManager.LayoutParams).apply {
                        isFullSpan = true
                    }
                }
            }
            onUnbind { model, viewBinder, metadata ->
            }
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.stickerRV.setHasFixedSize(true)

        val stickerLayoutManager = object : StaggeredGridLayoutManager(
                SPAN_COUNT,
                StaggeredGridLayoutManager.VERTICAL
        ) {
            override fun canScrollVertically(): Boolean {
                return true
            }
        }

//        val stickerLayoutManager = StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL)
//        stickerLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        binding.stickerRV.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
            ) {
                outRect.left = 0
                outRect.right = 0
                outRect.bottom = 0
                outRect.top = 0
            }
        })
        binding.stickerRV.layoutManager = stickerLayoutManager
        binding.stickerRV.isVerticalScrollBarEnabled = false

        val stickerAdapter = OneAdapter(binding.stickerRV) {

            itemModules += StickerModule()
            itemModules += StickerSetHeaderModule()

        }

        stickerAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                binding.stickerRV.postDelayed({ binding.stickerRV.scrollToPosition(0)}, 100)
            }

        })

        val fakeItems = mutableListOf<Diffable>()

        for (i in 0..10) {
            fakeItems.add(StickerSetModel(i.toLong(), randomString(10)))
            for (k in 0..listOf(10, 15, 20, 25, 30).random()) {
                fakeItems.add(StickerModel((i*1000+k).toLong(), randomString(3), listOf(android.R.color.black, android.R.color.holo_purple,  android.R.color.holo_blue_bright,  android.R.color.holo_red_dark,  android.R.color.holo_green_dark, ).random()))
            }
        }

        stickerAdapter.setItems(fakeItems)

        binding.searchEt.doOnTextChanged { text, start, before, count ->
            lifecycleScope.launch(Dispatchers.IO) {
                if (text.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        stickerAdapter.setItems(fakeItems)
                    }
                    return@launch
                }
//                val wait = kotlin.random.Random.nextLong(100, 200)
//                val sTs = System.currentTimeMillis()
//                while (System.currentTimeMillis()-sTs<wait) continue
//                delay() //simulate some processing
                val newSet = mutableListOf<Diffable>()
                for (el in fakeItems) {
//                    if ((el is StickerSetModel && kotlin.random.Random.nextInt(1, 5)>count) || el is StickerModel && el.fakeDesc.contains(text, true)) newSet.add(el)
                    if ((el is StickerSetModel && el.name.contains(text, true)) || el is StickerModel && el.fakeDesc.contains(text, true)) newSet.add(el)
                }
                withContext(Dispatchers.Main) {
                    //crash from background :)
                    println("Set: "+newSet)
                    stickerAdapter.setItems(newSet)
                }
            }
        }



    }


    fun randomString(
            len: Int,
            onlyDigit: Boolean = false,
            onlySmallAndDigit: Boolean = false,
            hexId: Boolean = false
    ): String {
        val rndStr = if (onlyDigit) "0123456789" else if (onlySmallAndDigit) "0123456789abcdefghijklmnopqrstuvwxyz" else if (hexId) "0123456789abcdef" else "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val random = Random()
        val sb = StringBuilder(len)
        for (i in 0 until len)
            sb.append(rndStr[random.nextInt(rndStr.length)])
        return sb.toString()
    }
}