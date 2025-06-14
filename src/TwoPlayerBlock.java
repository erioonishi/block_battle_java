import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;// グリッド状にコンポーネントを並べるレイアウトマネージャ
import java.awt.event.ActionEvent;// イベント（例：タイマーが動いた）を表すクラス
import java.awt.event.ActionListener;// イベントに反応するためのインターフェース
import java.awt.event.KeyEvent;// キーボードイベント（どのキーが押されたか等）を扱うクラス
import java.awt.event.KeyListener;// キーボード入力を検出するためのインターフェース

import javax.swing.JFrame;
import javax.swing.Timer;

public class TwoPlayerBlock extends JFrame implements ActionListener, KeyListener {//TwoPlayerBlockというクラスを定義してJFrameを継承して画面ウィンドウを作る
    private BlockBoard leftBoard, rightBoard;//leftBoard, rightBoard: 左右のプレイヤー用のゲーム盤（BlockBoardという別クラスのインスタンス）
    private Timer timer;//timer: 一定間隔でゲームを進めるためのタイマー

    public TwoPlayerBlock() {
        setTitle("Two Player Block");//ウィンドウのタイトルを設定
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//閉じるボタンを押したらプログラムも終了するように設定
        setLayout(new GridLayout(1, 2));//画面を 1行2列 のグリッドにする（左プレイヤーと右プレイヤー）

        leftBoard = new BlockBoard(true, this);//左がtrue（プレイヤー1）
        rightBoard = new BlockBoard(false, this);//右がfalse（プレイヤー2）

        add(leftBoard);
        add(rightBoard);//画面に左と右の盤面を追加

        addKeyListener(this);//キーボードの入力を受け取るように設定（このクラスがKeyListenerを実装している）
        setFocusable(true);//このウィンドウがキーボードの入力を受け取れるようにする
        pack();//すべての要素のサイズに合わせてウィンドウサイズを自動調整
        setResizable(false);//ウィンドウのサイズをユーザーが変更できないようにする
        setLocationRelativeTo(null);//画面を中央に表示する
        setVisible(true);//画面を表示する（最後に呼ばないとウィンドウが出てこない）

        timer = new Timer(500, this);//500ミリ秒（＝0.5秒）ごとに actionPerformed を呼び出すように設定
        timer.start();//タイマー開始でゲームが進行
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);//再描画が必要になったときに呼ばれる（たとえばウィンドウが更新されたとき）
        int midX = getWidth() / 2;//ウィンドウの横幅のちょうど真ん中のX座標を計算
        g.setColor(Color.WHITE);//描画する色を白に設定
        g.fillRect(midX - 2, 0, 4, getHeight());//中央に白い縦線（幅4ピクセル）を描く（2ピクセル左、2ピクセル右）
    }

    public void actionPerformed(ActionEvent e) {//タイマーで定期的に呼ばれる処理
        leftBoard.update();
        rightBoard.update();//タイマーが動くたびにleftBoardとrightBoardを更新
    }

    public void keyPressed(KeyEvent e) {//どのキーが押されたかを取得し、両方のプレイヤーに伝える
        leftBoard.handleKey(e.getKeyCode());
        rightBoard.handleKey(e.getKeyCode());//各プレイヤー側で自分用のキーでなければ無視する処理を行う
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public void notifyGameOver(boolean isLeft) {
        timer.stop();//ゲーム終了時にタイマーを止める（ブロックが落ちてこなくなる）
        if (isLeft) {
            rightBoard.setWin();
        } else {
            leftBoard.setWin();//左プレイヤーが負けたら右が勝ち、右が負けたら左が勝ち、と通知
        }
    }

    public static void main(String[] args) {
        new TwoPlayerBlock();
    }
}
