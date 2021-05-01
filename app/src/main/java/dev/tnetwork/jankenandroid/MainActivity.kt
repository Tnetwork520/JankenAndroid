package dev.tnetwork.jankenandroid

import android.content.DialogInterface
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*

private lateinit var player: MediaPlayer
private lateinit var soundPool: SoundPool
private var ActSound = 0

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
           ボタンを押した時に実行するメソッド
           引数はグー、チョキ、パーの3つのボタンを設定するので
           引数はviewを受け取るように指定する
           渡されるViewオブジェクトはnull安全が保証されていないため、
           データ型の後ろに「?」を付ける
        */
        fun onJankenButtonTapped(view: View?){
            /*
               アクティビティを開くには開きたいアクティビティを指定して
               Intentクラス（import android.content.Intent）の
               インスタンスを生成し、それをstartActivityメソッドの引数に渡す

               Intentクラスのコンストラクタ
               書式　Intent(context:Context!,class:Class<*>!)
               引数　context:呼び出し元のインスタンス
                    class:呼び出したいアクティビティクラス
            */
            val intent = Intent(this,ResultActivity::class.java)
            /*
　　　　　　　　　遷移元でインテントにデータを格納する
　　　　　　　　　putExtraメソッドでタップされたイメージボタンのIDをインテントに格納
　　　　　　　　　onJankenButtonTappedメソッドの引数viewには
               タップされたビューのインスタントが渡される
               そのビューのIDはgetIDメソッドで取得できるが、Kotlinではidプロパティが利用できる
               引数viewはnull許容型のため、null安全演算子?を使って処理する必要がある

               putExtraメソッド
               機能 インテントに追加情報を格納する
               書式 putExtra(name:String!,value:Int):Intent!
               引数 name:追加したい情報のキー
               　　　value:追加する値
            */
            intent.putExtra("MY_HAND",view?.id)
            /*
　　　　　　　　　startActivityメソッド
　　　　　　　　　機能　アクティビティを起動する
　　　　　　　　　書式　startActivity(intent:Intent!)
　　　　　　　　　引数　起動するアクティビティをセットしたインテント
            */
            startActivity(intent)
        }



        val audioAttributes = AudioAttributes.Builder()
                // USAGE_MEDIA
                // USAGE_GAME
                .setUsage(AudioAttributes.USAGE_GAME)
                // CONTENT_TYPE_MUSIC
                // CONTENT_TYPE_SPEECH, etc.
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()


        soundPool = SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                // ストリーム数に応じて
                .setMaxStreams(1)
                .build()

        // othr09.mp3 をロードしておく
        ActSound = soundPool.load(this, R.raw.othr09, 1)

        // load が終わったか確認する場合
        soundPool.setOnLoadCompleteListener{ soundPool, sampleId, status ->
            Log.d("debug", "sampleId=$sampleId")
            Log.d("debug", "status=$status")
        }

        /*
           イメージボタンとメソッドを紐づける
           下記コードはKotlinのラムダ式とSAM変換を使って
           クリック時のリスナーを設定している
           onJankenButtonTappedに渡しているのは暗黙の引数it。
           省略されているonClickにはタップされたViewオブジェクトが渡され、
           それをitに置き換えている
           また、kotlin Android拡張プラグインを使ってビューの取得を行っているので
           import kotlinx.android.synthetic.main.activity_main.*のインポートが必要
        */
        gu.setOnClickListener{
            onJankenButtonTapped(it)
            // othr09.mp3 の再生
            // play(ロードしたID, 左音量, 右音量, 優先度, ループ, 再生速度)
            soundPool.play(ActSound, 1.0f, 1.0f, 0, 0, 1.0f)
        }
        choki.setOnClickListener{
            onJankenButtonTapped(it)
            soundPool.play(ActSound, 1.0f, 1.0f, 0, 0, 1.0f)
        }
        pa.setOnClickListener{
            onJankenButtonTapped(it)
            soundPool.play(ActSound, 1.0f, 1.0f, 0, 0, 1.0f)
        }

        player = MediaPlayer.create(this,R.raw.getdown)
        player.isLooping = true


        //このアプリの終了ボタン
        endButton.setOnClickListener {
            // ダイアログを表示して再確認
            //※import androidx.appcompat.app.AlertDialogインポート必須
            AlertDialog.Builder(this).apply {
                setTitle("アプリ終了")
                setMessage("終了してもよろしいですか？")
                //終了の場合
                setPositiveButton("終了", DialogInterface.OnClickListener { _, _ ->
                    //
                    Toast.makeText(context, "アプリを終了します", Toast.LENGTH_LONG).show()
                    finish()
                })
                //キャンセルの場合
                setNegativeButton("キャンセル", null)
                show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        player.start()
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }
}