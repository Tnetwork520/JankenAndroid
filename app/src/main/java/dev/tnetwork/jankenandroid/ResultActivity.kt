package dev.tnetwork.jankenandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_result.*

//定数宣言
val gu = 0
val choki = 1
val pa = 2

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        /*
           格納されたデータを取り出す
           受け取ったインテントの取得はgetIntentメソッドで行うが、
           Kotlinではintentプロパティが利用できる
           intentは「this.intent」の略
           インテントに格納された情報はgetXXExtraメソッドで取り出せる

           getIntExtraメソッド
           機能 インテントから追加情報を取り出す
           書式 getIntExtra(name:String!,defalutValue:Int):Int
           引数 name:取り出したい情報のキー
               　　　value:追加する値
        */
        val id = intent.getIntExtra("MY_HAND", 0)
        /*
           when式による処理の分岐
           Int型のmyHandを用意
           Kotlin Android拡張プラグインを使ってビューの取得のため、
           kotlinx.android.synthetic.main.activity_result.*インポート必須
           このwhen式で前画面でタップされたボタンidに合わせて
           グー、チョキ、パーの画像を切り替えている
           イメージビューに画像を設定するにはsetImageResourceメソッドを使う
           引数に表示したい画像のリソースid(画像ファイル名の拡張子を除いたもの)を渡して画像を表示する

           setImageResourceメソッド
           機能 ImageViewのコンテンツに画像リソースを指定する
           書式 setImageResource(resId:Int):Unit
           引数 resID:画像のリソースID

           画像のリソースIDは「R.drawable.Id名」
           when内部に戻り値を設定（戻り値がnullにならないようにelseも記述）
        */
        val myHand: Int
        myHand = when (id) {
            R.id.gu -> {
                myHandImage.setImageResource(R.drawable.gu)
                gu
            }
            R.id.choki -> {
                myHandImage.setImageResource(R.drawable.choki)
                choki
            }
            R.id.pa -> {
                myHandImage.setImageResource(R.drawable.pa)
                pa
            }
            else -> gu
        }
        /*
           コンピュータの手を決める
           randomメソッドで0以上1未満の乱数を返し、
           3を掛けて0〜2のランダムな値を取得する
           KotlinではDouble型からInt型へはtoIntメソッドを使う
           when式でその手に合わせた画像をイメージビューに表示する
        */
        val comHand = getHand()//(Math.random()*3).toInt()
        when (comHand) {
            gu -> comHandImage.setImageResource(R.drawable.com_gu)
            choki -> comHandImage.setImageResource(R.drawable.com_choki)
            pa -> comHandImage.setImageResource(R.drawable.com_pa)
        }

        /*
           プレイヤとコンピュータの手を比較し、勝敗を判定する
           コンピュータの手からプレイヤの手を引いた値が0ならあいこ
           コンピュータの手からプレイヤの手を引いた値が1または-2ならプレイヤの勝ち
           コンピュータの手からプレイヤの手を引いた値が2または-1ならコンピュータの勝ち
           処理を簡略化するために3を足して正の数に揃えた後に3で割った余りを計算することで判断するロジック
           setTextメソッドにより、勝敗をテキストビューに表示する
        */
        val gameResult = (comHand - myHand + 3) % 3
        when (gameResult) {
            0 -> resultLabel.setText(R.string.result_draw) //引き分け
            1 -> resultLabel.setText(R.string.result_win) //勝った場合
            2 -> resultLabel.setText(R.string.result_lose) //負けた場合
        }
        backButton.setOnClickListener { finish() }

        //じゃんけんの結果を保存する
        saveData(myHand, comHand, gameResult)
    }

    /* saveDataメソッドを追加する
       このメソッドでは、既に定義済の変数myHand、comHand、gameResultを引数として指定して使用する
       最初にPreferenceManagerクラス(Android 10 (API 29) 以降非推奨)の
       getDefaultSharedPreferencesクラスメソッドを使ってデフォルトの共有プリファレンスを取得する

       getDefaultSharedPreferencesメソッド
       機能 デフォルトの共有プリファレンスを取得する
       書式 PreferenceManager.getDefaultSharedPreferences(context:Context!):SharedPreferences!
       引数 context:デフォルトの共有プリファレンスを取得するオブジェクト

       以降の4行では共有プリファレンスから値を取得して、各種変数に代入している

       getIntメソッド
       機能 共有プリファレンスの設定項目をInt型で取得する
       書式 getInt(key:String!,defValue:Int):Int
       引数 key:取り出したい設定項目の名前
           defValue:設定項目が未設定の場合にはこの値を返す

       ※文字列で取り出すときはgetString、Boolean型で取り出すgetBooleanなどもある

       ediWinningStreakCountは今回の勝負後の連勝回数を保持する変数
       when式を使ってコンピュータが勝敗したかを調べて連勝した場合は変数winningStreakCountの
       値に+1を代入し、そうでない場合は0を代入する
    */
    private fun saveData(myHand: Int, comHand: Int, gameResult: Int) {

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val gameCount = pref.getInt("GAME_COUNT", 0)
        val winningStreakCount = pref.getInt("WINNING_STREAK_COUNT", 0)
        val lastComHand = pref.getInt("LAST_COM_HAND", 0)
        val lastGameResult = pref.getInt("GAME_RESULT", -1)

        val ediWinningStreakCount =
            when {
                lastGameResult == 2 && gameResult == 2 ->
                    winningStreakCount + 1
                else ->
                    0
            }
        /*
           共有プリファレンスに書き込みを行うためにはSharedPreferences.Editorインターフェイスの
           インターフェイスを取得する必要がある
           次の行で勝負した回数をputIntメソッドにより+1して保持している
           putIntメソッドは自分自身（editor）を返すため、このようにチェーンのように繋げて記述することが可能

           etIntメソッド
           機能 共有プリファレンスの設定項目にInt型の値を書き込む
           書式 putInt(key:String!,defValue:Int):SharedPreferences.Editor!
           引数 key:書き込みたい設定項目の名前
               value:書き込む値

           ※文字列を書き込むputStringやBoolean型の値を書き込むputBooleanなどがある
           以降の5行で上記のwhen式で取得した連勝回数と引数で受け取った今回の結果、
           及び前回のコンピュータの手を前回（一部は前々回）の結果として共有プリファレンスに書き込む
           Editorインターフェイスのインターフェイスを通じて行った変更はapplyメソッド、またはcommitメソッドで保存する
           ※これを行わないと保存されない
        */

        val editor = pref.edit()
        editor.putInt("GAME_COUNT", gameCount + 1)
            .putInt("WINNING_STREAK_COUNT", ediWinningStreakCount)
            .putInt("LAST_MY_HAND", myHand)
            .putInt("LAST_COM_HAND", comHand)
            .putInt("BEFORE_LAST_COM_HAND", lastComHand)
            .putInt("GAME_RESULT", gameResult)
            .apply()
    }
    /*
       じゃんけんロジックを実装する
       コンピュータの手を決める
       ・1回目で負けた場合、相手の出した手に勝つ手を出す
       ・1回目に勝った場合、次に出す手を変える
       ・同じ手で連勝した場合は手を変える
       ・上記以外はランダム

    */
    private fun getHand(): Int {
        var hand = (Math.random() * 3).toInt()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val gameCount = pref.getInt("GAME_COUNT", 0)
        val winningStreakCount = pref.getInt("WINNING_STREAK_COUNT", 0)
        val lastMyHand = pref.getInt("LAST_MY_HAND", 0)
        val lastComHand = pref.getInt("LAST_COM_HAND", 0)
        val beforeLastComHand = pref.getInt("BEFORE_LAST_COM_HAND", 0)
        val gameResult = pref.getInt("GAME_RESULT", -1)

        if (gameCount == 1){
            if (gameCount == 2){
                // 前回の勝負が1回目でコンピュータに勝った場合
                // コンピュータは次に出す手を変える
                while (lastComHand == hand){
                    hand = (Math.random() * 3).toInt()
                }
            }else if (gameCount == 1){
                hand = (lastMyHand - 1 + 3) % 3
            }
        }else if(winningStreakCount > 0){
            if (beforeLastComHand == lastComHand) {
                //
                while (lastComHand == hand) {
                    hand = (Math.random() * 3).toInt()
                }
            }
        }
        return hand
    }

}