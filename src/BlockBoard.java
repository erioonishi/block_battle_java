import java.awt.Color;//色を扱う
import java.awt.Dimension;//コンポーネントの大きさ（幅や高さ）を設定
import java.awt.Font;//文字のフォントを設定
import java.awt.FontMetrics;//文字の幅や高さを計測
import java.awt.Graphics;//描画を行うため
import java.awt.event.KeyEvent;//キーボードのキー操作

import javax.swing.JPanel;//GUIのパネル（表示領域）を作るためのクラス

class BlockBoard extends JPanel {//JPanelを継承して、盤面を表示・操作するためのクラス
    private final int ROWS = 20, COLS = 10, BLOCK_SIZE = 30;//盤面の行数が20、列数が10、ブロックのサイズが30ピクセル
    private final Color[] COLORS = {//7種類の色
        new Color(255, 182, 193), // Light Pink
        new Color(173, 216, 230), // Light Blue
        new Color(221, 160, 221), // Plum
        new Color(255, 200, 124), // Light Orange
        new Color(152, 251, 152), // Pale Green
        new Color(255, 255, 153), // Light Yellow
        new Color(211, 211, 211)  // Light Gray
    };
    private final int[][][] TETROMINOS = {//今動いているテトリミノの形を保存する配列
        {{1, 1, 1, 1}},                     // I
        {{1, 1}, {1, 1}},                   // O
        {{0, 1, 0}, {1, 1, 1}},             // T
        {{1, 0, 0}, {1, 1, 1}},             // J
        {{0, 0, 1}, {1, 1, 1}},             // L
        {{1, 1, 0}, {0, 1, 1}},             // S
        {{0, 1, 1}, {1, 1, 0}},             // Z
    };

    private int[][] board = new int[ROWS][COLS];
    private int[][] currentShape;
    private int currentRow, currentCol, currentType;//今のテトリミノの位置（行と列）と種類（0〜6）を保持
    private boolean gameOver = false;//ゲームオーバーかどうかのフラグ（最初はゲーム続行中）
    private boolean win = false;//勝利したかどうかのフラグ
    private final boolean isLeftPlayer;//左側プレイヤーかどうかを示すフラグ（2人対戦用）
    private final TwoPlayerBlock parent;//親クラス（ゲーム全体の管理クラス）への参照。ゲームオーバー通知などに使う

    public BlockBoard(boolean isLeftPlayer, TwoPlayerBlock parent) {//コンストラクタ。左側プレイヤーか、親クラスの参照を受け取る
        this.isLeftPlayer = isLeftPlayer;//受け取った「左プレイヤーかどうか」をメンバ変数に保存
        this.parent = parent;//受け取った親クラスの参照を保存
        setPreferredSize(new Dimension(COLS * BLOCK_SIZE, ROWS * BLOCK_SIZE));//JPanelのサイズをブロックの数×ブロックサイズに設定（300×600ピクセル）
        spawnNewTetromino();//最初のテトリミノを画面に出現させる
    }

    ////新しいテトリミノを生成する処理を定義////
    private void spawnNewTetromino() {
        currentType = (int)(Math.random() * TETROMINOS.length);//7種類の中からランダムに1種類選び
        currentShape = deepCopy(TETROMINOS[currentType]);//選んだテトリミノの形をコピーして、動かせる状態にする
        currentRow = 0;//テトリミノの初期位置を盤面の一番上の行に設定
        currentCol = COLS / 2 - 1;//テトリミノの初期位置を横の中央あたりに設定

        if (!canPlace(currentRow, currentCol, currentShape)) {//その位置に置けるかチェックして置けなければゲームオーバー
            gameOver = true;//ゲームオーバーフラグを立てる
            parent.notifyGameOver(isLeftPlayer);//親クラスにゲームオーバーを通知
        }
    }

    ////ゲーム状態を1ステップ進める処理（テトリミノを1マス下に動かすなど）////
    public void update() {
        if (!gameOver && !win) {//ゲームがまだ終わっていなければ処理を続ける
            if (canPlace(currentRow + 1, currentCol)) {//1つ下にテトリミノを置けるかチェック
                currentRow++;//置ければ1行下に移動
            } else {//置けなければ今の位置で固定し、ライン消しや新テトリミノ出現処理へ
                fixToBoard();//今のテトリミノを盤面に固定（盤面配列に書き込む）
                clearLines();//ラインが揃っていれば消す
                spawnNewTetromino();//新しいテトリミノを出現させる
            }
            repaint();//画面を再描画
        }
    }

    ////キー入力を受けてテトリミノを動かす処理////
    public void handleKey(int keyCode) {
        if (gameOver || win) return;//ゲーム終了・勝利時は何もしない

        if (isLeftPlayer) {//左プレイヤーなら下のキー操作
            switch (keyCode) {//押されたキーで分岐
                case KeyEvent.VK_A: if (canPlace(currentRow, currentCol - 1)) currentCol--; break;//‘A’キーで左に移動可能なら左に動かす
                case KeyEvent.VK_D: if (canPlace(currentRow, currentCol + 1)) currentCol++; break;//‘D’キーで右に移動可能なら右に動かす
                case KeyEvent.VK_S: if (canPlace(currentRow + 1, currentCol)) currentRow++; break;//‘S’キーで下に移動可能なら下に動かす
                case KeyEvent.VK_W:
                    int[][] rotatedL = rotateRight(currentShape);
                    if (canPlace(currentRow, currentCol, rotatedL)) currentShape = rotatedL;
                    break;//‘W’キーでテトリミノを右回転し、置ければ回転させる
            }
        } else {//右プレイヤーのキー操作
            switch (keyCode) {//押されたキーで分岐
                case KeyEvent.VK_LEFT: if (canPlace(currentRow, currentCol - 1)) currentCol--; break;// ←キーで左移動
                case KeyEvent.VK_RIGHT: if (canPlace(currentRow, currentCol + 1)) currentCol++; break;// →キーで右移動
                case KeyEvent.VK_DOWN: if (canPlace(currentRow + 1, currentCol)) currentRow++; break;// ↓キーで下移動
                case KeyEvent.VK_UP:
                    int[][] rotatedR = rotateRight(currentShape);
                    if (canPlace(currentRow, currentCol, rotatedR)) currentShape = rotatedR;
                    break;//↑キーで回転
            }
        }
        repaint();//キー操作後に画面を再描画
    }

    ////rotateRight メソッド（テトリミノを右に回転）////
    private int[][] rotateRight(int[][] shape) {//2次元配列（テトリミノの形）を右に90度回転させるメソッド
        int rows = shape.length, cols = shape[0].length;//元の行数と列数を取得
        int[][] rotated = new int[cols][rows];//回転後の形を入れるための新しい2次元配列を作成（行列が逆になる）
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                rotated[c][rows - 1 - r] = shape[r][c];//元の (r, c) を新しい配列の (c, rows-1-r) に変換してコピー（右回転の基本）
        return rotated;//回転後の形を返す
    }

    ////deepCopy メソッド（2次元配列のコピー）////
    private int[][] deepCopy(int[][] shape) {//テトリミノの形をそのまま複製するメソッド
        int[][] copy = new int[shape.length][];//行数だけ確保した新しい配列を作成
        for (int i = 0; i < shape.length; i++)
            copy[i] = shape[i].clone();//各行（1次元配列）をコピー（参照でなく中身をコピー）
        return copy;//コピーした配列を返す
    }

    ////canPlace メソッド（その場所にテトリミノが置けるか？）////
    private boolean canPlace(int row, int col) {//現在のテトリミノ currentShape が row, col に置けるかを判定
        return canPlace(row, col, currentShape);//実体はオーバーロードされた次のメソッドで処理
    }// 今の形のテトリミノを、指定の位置に置けるか判定（オーバーロード）

    ////canPlace（オーバーロード）////
    private boolean canPlace(int row, int col, int[][] shape) {//指定の形を指定の位置に置けるか判定
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {//各ブロックの位置をチェック
                if (shape[r][c] == 1) {//ブロックがある部分のみ判定対象
                    int br = row + r, bc = col + c;//実際の盤面での位置を計算
                    if (br < 0 || br >= ROWS || bc < 0 || bc >= COLS || board[br][bc] != 0)
                        return false;//範囲外または既にブロックがある場所なら置けない（false）
                }
            }
        }
        return true;//全部OKなら true（置ける）
    }

    ////fixToBoard（テトリミノを盤面に固定）////
    private void fixToBoard() {//落下完了したテトリミノを盤面に「固定」
        for (int r = 0; r < currentShape.length; r++) {
            for (int c = 0; c < currentShape[r].length; c++) {
                if (currentShape[r][c] == 1) {//ブロックがある場所だけ固定
                    board[currentRow + r][currentCol + c] = currentType + 1;//盤面にブロックの種類番号を書き込む
                }
            }
        }
    }

    ////clearLines（行が揃ったら消す）////
    private void clearLines() {// 揃った行を消し、上の行を1段下にずらす処理
        for (int r = ROWS - 1; r >= 0; r--) {
            boolean full = true;// 下から上にチェック。行が満杯か確認。
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] == 0) {
                    full = false;
                    break;// 1つでも空きがあればその行は満杯ではない
                }
            }
            if (full) {
                for (int row = r; row > 0; row--) {
                    board[row] = board[row - 1].clone();
                }
                board[0] = new int[COLS];
                r++; // 再チェックするもう一度同じ行を確認（連続で消える場合）
            }
        }
    }

    ////setWin（勝利処理）////
    public void setWin() {
        win = true;
        repaint();//勝利状態にして、画面を再描画
    }

    ////paintComponent（画面の描画）////
    public void paintComponent(Graphics g) {
        super.paintComponent(g);//JPanelの描画処理を上書き。背景などを描く
     // 背景を黒で塗る
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());//黒く塗りつぶす
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c] != 0) {//board[r][c] が 0 でない⇒ブロックがあるマス
                    g.setColor(COLORS[board[r][c] - 1]);//ブロックの色を設定
                    g.fillRect(c * BLOCK_SIZE, r * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);//カラフルなブロックの四角描画
                    g.setColor(Color.WHITE);
                    g.drawRect(c * BLOCK_SIZE, r * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                }//盤面上のブロックを描画（色つき＋白枠）
            }
        }

        if (!gameOver && !win) {// ゲームオーバーまたは勝利状態でなければテトリミノを描画
            g.setColor(COLORS[currentType]);// currentType は今落ちているテトリミノの種類
            for (int r = 0; r < currentShape.length; r++) {// 今のテトリミノの形 currentShape（2次元配列）を1マスずつチェック
                for (int c = 0; c < currentShape[r].length; c++) {
                    if (currentShape[r][c] == 1) {
                        int x = (currentCol + c) * BLOCK_SIZE;
                        int y = (currentRow + r) * BLOCK_SIZE;
                        g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);// 色つき四角
                        g.setColor(Color.WHITE);// 枠線の色を白に
                        g.drawRect(x, y, BLOCK_SIZE, BLOCK_SIZE);// 枠線
                        g.setColor(COLORS[currentType]);// 元の色に戻す
                    }// 現在落ちているテトリミノを描画
                }
            }
        }
        ////ゲームオーバー表示////
        if (gameOver) {
            g.setColor(new Color(103, 141, 176)); // 淡い青
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String loseText = "LOSE";
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(loseText);
            int textHeight = fm.getHeight();
            int x = (getWidth() - textWidth) / 2;// 中央に配置する
            int y = (getHeight() - textHeight) / 2 + fm.getAscent();
            g.drawString(loseText, x, y);
        ////勝利表示////
        } else if (win) {
            g.setColor(new Color(176, 103, 120)); // 淡い赤
            g.setFont(new Font("Arial", Font.BOLD, 36));
            String winText = "WIN!";
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(winText);
            int textHeight = fm.getHeight();
            int x = (getWidth() - textWidth) / 2;
            int y = (getHeight() - textHeight) / 2 + fm.getAscent();
            g.drawString(winText, x, y);
        }
        

    }
}


//r	テトリミノ内の行（相対位置）
//c	テトリミノ内の列（相対位置）
//row	テトリミノの左上が置かれる盤面上の行
//col	テトリミノの左上が置かれる盤面上の列
//br = row + r	実際の盤面上の行位置
//bc = col + c	実際の盤面上の列位置
