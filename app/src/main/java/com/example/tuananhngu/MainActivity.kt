package com.example.tuananhngu

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import com.example.tuananhngu.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val listLotoWeb = mutableListOf<String>()
    private val listLotoPlayer = mutableListOf<String>()
    private val listLotoPlayerEat = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setObserver()
        setOnClick()
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun setOnClick() {
        binding.btClear.setOnClickListener {
            binding.edtMyLoto.text.clear()
            binding.edtMyDiemPlay.text.clear()
            binding.edtDiemTong.text.clear()
        }

        binding.btResult.setOnClickListener {
            if (!binding.edtDiemTong.text.isNullOrEmpty() && !binding.edtMyDiemPlay.text.isNullOrEmpty() && !binding.edtMyLoto.text.isNullOrEmpty()) {
                val listLotoKhachEat = handleGetResult()
                Log.e("list khách ăn: ", listLotoKhachEat.joinToString())
                binding.tvDiemKHAn.text =
                    "- Xác suất: " + listLotoKhachEat.size + "\n- Gồm những con: " + listLotoKhachEat.joinToString()
                calculatingLoLai(listLotoKhachEat)
            }
        }

        binding.constraintLayout.setOnTouchListener { _, _ ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.constraintLayout.windowToken, 0)
            false
        }
    }

    private fun calculatingLoLai(listLotoKhachEat: List<String>) {
        val tongDiem = Integer.parseInt(binding.edtDiemTong.text.toString())
        val diemMinhDanh = Integer.parseInt(binding.edtMyDiemPlay.text.toString())
        val tienMinhNhan = tongDiem * diemMinhDanh * TIEN_MUA
        val tienKhachAn = listLotoKhachEat.size * TIEN_AN * diemMinhDanh
        val result = tienMinhNhan - tienKhachAn

        binding.tvResult.text = if (result > 0) {
            "Tổng số tiền ăn được: $result đồng"
        } else if (result == 0) "Éo ăn được gì!!!!, đm Tuấn Anh"
        else {
            "Tổng số tiền mất: " + abs(result) + " đồng"
        }
    }

    private fun handleGetResult(): List<String> {
        val result = convertStringToList(binding.edtMyLoto.text.toString())
        listLotoPlayer.clear()
        listLotoPlayerEat.clear()
        listLotoPlayer.addAll(result)
        for (lotoPlayer in listLotoPlayer) {
            for (lotoWeb in listLotoWeb) {
                if (lotoPlayer == lotoWeb) {
                    listLotoPlayerEat.add(lotoWeb)
                }
            }
        }
        return listLotoPlayerEat
    }

    @SuppressLint("SetTextI18n")
    private fun setObserver() {
        lifecycleScope.launch {
            val result = loadDataFromUrl("https://xosodaiphat.com")
            binding.lotoWeb.text = "- Những con về: $result"
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun loadDataFromUrl(url: String): String? {
        return withContext(Dispatchers.IO) {
            listLotoWeb.clear()
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val html = response.body()?.string()
                val doc = Jsoup.parse(html)
                val lotoTable = doc.select("table.table-loto")
                val lotoRows = lotoTable.select("tr")
                val lotoTds = doc.select("td[id^=loto_mb_]")
                for (td in lotoTds) {
                    val lotoNumber = td.text()
                    Log.e("here", lotoNumber)
                    val output = convertStringToList(lotoNumber)
                    listLotoWeb.addAll(output)
                }
                Log.e("here", "lấy về thành công!")
                listLotoWeb.joinToString()
            } else {
                Log.e("here", "lỗi lấy data trên web về!")
                null
            }
        }
    }

    private fun convertStringToList(input: String): List<String> {
        return try {
            val regex = Regex("[^\\d]+")
            val result = input.split(",").map { it.replace(regex, "") }
            val result1 = result.map { it.trim() }.filter { it.isNotBlank() }
            result1.filter { it.trim().isNotEmpty() }.map { it.replace("\n", "") }
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    companion object {
        const val TIEN_MUA = 21500
        const val TIEN_AN = 80000
    }
}