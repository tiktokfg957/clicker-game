package com.example.clicker   // если меняли package, исправьте здесь

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // UI элементы
    private lateinit var tvCoins: TextView
    private lateinit var btnClick: Button
    private lateinit var btnUpgradeClick: Button
    private lateinit var btnUpgradeAuto: Button
    private lateinit var tvStats: TextView

    // Игровые переменные
    private var coins = 0L               // текущее количество монет
    private var clickPower = 1            // сколько монет даёт один клик
    private var autoClickerLevel = 0      // уровень автокликера (сколько раз в секунду)
    private var autoClickerPrice = 100    // цена за первый уровень автокликера

    // Для автокликера
    private val handler = Handler(Looper.getMainLooper())
    private var autoClickRunnable: Runnable? = null

    // Для сохранения
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Находим View по ID
        tvCoins = findViewById(R.id.tv_coins)
        btnClick = findViewById(R.id.btn_click)
        btnUpgradeClick = findViewById(R.id.btn_upgrade_click)
        btnUpgradeAuto = findViewById(R.id.btn_upgrade_auto)
        tvStats = findViewById(R.id.tv_stats)

        // Загружаем сохранённые данные
        prefs = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        loadData()

        // Обновляем интерфейс
        updateUI()

        // Обработчик клика по большой кнопке
        btnClick.setOnClickListener {
            addCoins(clickPower.toLong())
        }

        // Улучшение силы клика
        btnUpgradeClick.setOnClickListener {
            val price = clickPower * 10 // цена улучшения = сила клика * 10
            if (coins >= price) {
                coins -= price
                clickPower++
                updateUI()
                saveData()
                Toast.makeText(this, "Сила клика увеличена!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Не хватает монет!", Toast.LENGTH_SHORT).show()
            }
        }

        // Улучшение автокликера
        btnUpgradeAuto.setOnClickListener {
            if (coins >= autoClickerPrice) {
                coins -= autoClickerPrice
                autoClickerLevel++
                autoClickerPrice = (autoClickerPrice * 1.5).toInt() // цена растёт
                updateUI()
                saveData()
                restartAutoClicker() // перезапускаем автокликер с новым уровнем
                Toast.makeText(this, "Автокликер улучшен!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Не хватает монет!", Toast.LENGTH_SHORT).show()
            }
        }

        // Запускаем автокликер
        startAutoClicker()
    }

    // Добавление монет и обновление UI
    private fun addCoins(amount: Long) {
        coins += amount
        updateUI()
        saveData()
    }

    // Обновление текстов
    private fun updateUI() {
        tvCoins.text = String.format(Locale.getDefault(), "Монеты: %d", coins)
        tvStats.text = String.format(Locale.getDefault(),
            "За клик: +%d | Авто: +%d/сек",
            clickPower,
            autoClickerLevel
        )
        btnUpgradeClick.text = String.format(Locale.getDefault(),
            "Улучшить клик (сила +1)\nЦена: %d монет",
            clickPower * 10
        )
        btnUpgradeAuto.text = String.format(Locale.getDefault(),
            "Автокликер +1/сек\nЦена: %d монет",
            autoClickerPrice
        )
    }

    // Запуск цикла автокликера
    private fun startAutoClicker() {
        autoClickRunnable = object : Runnable {
            override fun run() {
                if (autoClickerLevel > 0) {
                    addCoins(autoClickerLevel.toLong())
                }
                handler.postDelayed(this, 1000) // каждую секунду
            }
        }
        handler.post(autoClickRunnable!!)
    }

    // Перезапуск автокликера после изменения уровня
    private fun restartAutoClicker() {
        autoClickRunnable?.let { handler.removeCallbacks(it) }
        startAutoClicker()
    }

    // Сохранение данных
    private fun saveData() {
        val editor = prefs.edit()
        editor.putLong("coins", coins)
        editor.putInt("clickPower", clickPower)
        editor.putInt("autoClickerLevel", autoClickerLevel)
        editor.putInt("autoClickerPrice", autoClickerPrice)
        editor.apply()
    }

    // Загрузка данных
    private fun loadData() {
        coins = prefs.getLong("coins", 0L)
        clickPower = prefs.getInt("clickPower", 1)
        autoClickerLevel = prefs.getInt("autoClickerLevel", 0)
        autoClickerPrice = prefs.getInt("autoClickerPrice", 100)
    }

    // Остановка автокликера при закрытии приложения
    override fun onDestroy() {
        super.onDestroy()
        autoClickRunnable?.let { handler.removeCallbacks(it) }
    }
}
