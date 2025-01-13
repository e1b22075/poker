# 1. セットアップマニュアル
 ## はじめに
 このセットアップマニュアルは、JDKがインストールされているサーバーにセットアップするためのマニュアルです。JDKのインストールがまだな場合は先にJDKのインストールをお願いします。
## セットアップ方法
 ### 1.	cloneを行う
  JDKがインストールされているサーバーでgit内にあるポーカーゲームのリポジトリをcloneします。cloneの際、HTTPSでのcloneを行う場合は以下のコマンドを入力してください。
  ```
  git clone https://github.com/e1b22075/poker.git
  ```
  また、SSHでのcloneは以下のコマンドを入力してください
  ```
  git clone git@github.com:e1b22075/poker.git
  ```

 ### 2. ディレクトリの移動
 cloneが終了すると、pokerというディレクトリが作成されます。以下のコマンドを入力し、pokerディレクトリに移動してください。
  cd poker

 ### 3.  gradlewの実行
移動後、bashを利用してgradlewを実行します。以下のコマンドを入力してください。
```
  bash ./gradlew
  ```
 ### 4.	アプリの起動
  最後に、プロジェクトを実行するために以下のコマンドを入力してください
  ```
  bash ./gradlew bootrun
  ```

## 2.  poker-game:ユーザマニュアル

### ログイン方法
Webページにアクセスすると、ログインかユーザ登録のどちらかを選ぶことができる。
まだユーザ登録を行っていないユーザはここでユーザ登録をしてもらい、登録したユーザ名とパスワードでログインすることができる。

**注意！！！**
**ユーザー登録には、**

**ユーザー名**

**メールアドレス**

**パスワード**

**が必要になります。ここで間違っても普段使っているものを入力しないでください。**


### 遊び方
#### ログインから入室まで
 1.  以下のページ（授業用）にアクセスし、ユーザー登録を行う
 ```
 http://150.89.233.204
 ```
2.  ユーザー登録後、ログインする。
3.  ログイン後、対戦するルームを選択する画面に遷移するので、遊びたい部屋に入室する。
- **「CPUと対戦」の部屋に入室すると「POKERを開始」のボタンで試合が開始される。**
- **「ユーザと対戦」の部屋に入ると、「準備」というボタンがあるのでそれを押すとほかのユーザが入室してくるのを待機する状態となる。ほかのユーザが入室してきて、準備完了となれば試合が開始される。**

 ### ゲームの操作について
1. ゲーム画面に移ると、「カードを引く」ボタンを押して自分の手札を引く。そのときの手札に応じて、「コール」、「レイズ」、「ドロップ」を選択する。
2. この3つのいずれかを選び、カードの役をそろえていく作業を2ターン行う。最終的にそろった役で、自分と相手のどちらが強いかを判定して勝利した方に賭けていたコインを渡すことになる。
3. 	この一連の流れが1ラウンドとして試合が行われ、このやり取りを3回行われたときに、コインを多く所持していた方の勝利となる。

### 用語
- コイン : 賭け金のこと。なくなるとゲームに敗北する。
- コール : 賭け金をそのままの状態にしてゲームを続行する。coinを維持して不要なカードを選びなおすことができる。
- ドロップ : リタイアすること。コインを無条件で1枚相手に渡してゲームから降りる。
- レイズ : コインを上乗せすること。コインを上乗せして不要なカードを選びなおすことができる。

### ゲーム操作に困ったら
ゲームの詳細について各部屋に入った時に「HELPへのアクセス」というボタンがあるので、そこからヘルプページへ遷移することができる
