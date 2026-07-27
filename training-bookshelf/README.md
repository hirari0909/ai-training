# Training Bookshelf

書籍情報の登録・閲覧・編集・削除を行う管理システムです。Spring Boot/Javaの学習用教材として使用します。

## システム概要

本システムは、書籍情報の登録・閲覧・編集・削除を行う管理システムです。
また、各書籍に対してレビューを投稿・閲覧する機能を提供します。

### 主な機能

- 書籍一覧表示
- 書籍詳細表示
- 書籍の新規登録
- 書籍情報の編集
- 書籍の削除
- レビューの投稿・閲覧

## 使用技術

- **バックエンド**: Spring Boot 3.5.8 (Java 21)
- **テンプレートエンジン**: Thymeleaf
- **データアクセス**: MyBatis
- **データベース**: H2 Database (開発環境)、MySQL 8.x (本番環境)
- **ビルドツール**: Maven
- **その他**: Lombok, Spring Boot DevTools

## 必要な環境

- Java 21以上
- Maven 3.6以上（Maven Wrapperを使用する場合は不要）

## セットアップ方法

### 1. リポジトリのクローン

```bash
git clone <repository-url>
cd training-bookshelf
```

### 2. プロジェクトのビルド

Maven Wrapperを使用する場合（推奨）：

**Windows:**
```powershell
.\mvnw.cmd clean install
```

**Mac/Linux:**
```bash
./mvnw clean install
```

Maven がインストールされている場合：
```bash
mvn clean install
```

## アプリケーションの起動方法

### 方法1: Maven Wrapperを使用（推奨）

**Windows:**
```powershell
.\mvnw.cmd spring-boot:run
```

**Mac/Linux:**
```bash
./mvnw spring-boot:run
```

### 方法2: Maven を直接使用

```bash
mvn spring-boot:run
```

### 方法3: jar ファイルから起動

```bash
# ビルド
.\mvnw.cmd clean package

# 起動
java -jar target/training-bookshelf-0.0.1-SNAPSHOT.jar
```

## アクセス方法

アプリケーション起動後、以下のURLにアクセスしてください：

- **Hello World 画面**: http://localhost:8080/hello
- **アプリケーション**: http://localhost:8080
- **H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - User Name: `sa`
  - Password: （空欄）

## 開発環境

### データベース設定

デフォルトでH2インメモリデータベースを使用しています。

**H2 Database Console**: http://localhost:8080/h2-console

接続情報：
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **User Name**: `sa`
- **Password**: （空欄）

### Hot Reload（ホットリロード）

Spring Boot DevToolsを使用しているため、ソースコードの変更時に自動的にアプリケーションが再起動します。

### IDEでの実行

1. プロジェクトをIDEにインポート（IntelliJ IDEA、Eclipse、VS Code など）
2. `TrainingBookshelfApplication.java` を開く
3. main メソッドを実行

## プロジェクト構造

```
training-bookshelf/
├── docs/                              # 設計ドキュメント
│   ├── 外部設計/                      # 外部設計書
│   │   ├── 01共通仕様.md
│   │   ├── 02画面遷移図.md
│   │   ├── 03ER図.md
│   │   ├── 04TBL定義.md
│   │   ├── 05メッセージ一覧.md
│   │   └── 05個別画面設計/            # 画面ごとの詳細設計（html/img含む）
│   ├── 詳細設計/                      # 詳細設計書
│   │   ├── ネーミングルール.md
│   │   └── モジュール一覧.md
│   └── 単体テスト/                    # 単体テスト
│       ├── 仕様書/                    # テスト仕様書
│       │   ├── controller/            # コントローラーテスト仕様
│       │   ├── service/               # サービステスト仕様
│       │   └── util/                  # ユーティリティテスト仕様
│       └── テスト結果/                # テストレポート・JaCoCoカバレッジ
├── kenshu-docs/                       # 研修資料
├── prompt/                            # AI プロンプト定義
│   ├── CD/                            # コーディング用プロンプト
│   └── UT1/                           # 単体テスト用プロンプト
├── src/
│   ├── main/
│   │   ├── java/.../bookshelf/        # Javaソースコード
│   │   │   ├── controller/            # コントローラー（Book, Hello, Review）
│   │   │   ├── entity/                # エンティティ（Book, Category, Review, Sample）
│   │   │   ├── form/                  # フォーム（BookRegister, BookEdit, Review）
│   │   │   ├── mapper/                # MyBatisマッパー
│   │   │   ├── service/               # サービス層
│   │   │   └── util/                  # ユーティリティ（MessageUtil）
│   │   └── resources/
│   │       ├── application.properties # アプリケーション設定
│   │       ├── messages.properties    # メッセージ定義
│   │       ├── schema.sql             # DDL定義
│   │       ├── data.sql               # 初期データ
│   │       ├── static/css/            # スタイルシート
│   │       └── templates/             # Thymeleafテンプレート
│   │           ├── book/              # 書籍関連画面（BK01〜BK13, error）
│   │           └── hello.html
│   └── test/                          # テストコード
│       └── java/.../bookshelf/
│           ├── controller/            # コントローラーテスト
│           ├── mapper/                # マッパーテスト
│           ├── service/               # サービステスト
│           └── util/                  # ユーティリティテスト
├── pom.xml                            # Maven設定ファイル
└── README.md                          # このファイル
```

## ドキュメント

詳細な仕様については、`docs/` ディレクトリ内の各ドキュメントを参照してください：

### 外部設計

- [共通仕様](docs/外部設計/01共通仕様.md)
- [画面遷移図](docs/外部設計/02画面遷移図.md)
- [ER図](docs/外部設計/03ER図.md)
- [テーブル定義](docs/外部設計/04TBL定義.md)
- [メッセージ一覧](docs/外部設計/05メッセージ一覧.md)
- [個別画面設計](docs/外部設計/05個別画面設計/)

### 詳細設計

- [ネーミングルール](docs/詳細設計/ネーミングルール.md)
- [モジュール一覧](docs/詳細設計/モジュール一覧.md)

### 単体テスト

- [テスト仕様書](docs/単体テスト/仕様書/)
- [テスト結果・カバレッジレポート](docs/単体テスト/テスト結果/)

## テスト

### テストの実行

全てのテストを実行：

**Windows:**
```powershell
.\mvnw.cmd test
start target/site/jacoco/index.html
```

**Mac/Linux:**
```bash
./mvnw test
```

### 特定のテストクラスを実行

```powershell
# 単一のテストクラス
.\mvnw.cmd test -Dtest=SampleMapperTest

# 複数のテストクラス
.\mvnw.cmd test -Dtest=SampleServiceTest,HelloControllerTest
```

### カバレッジレポート

テスト実行後、JaCoCoによるカバレッジレポートが自動生成されます。

**レポートの確認方法：**

1. テストを実行
   ```powershell
   .\mvnw.cmd test
   ```

2. 以下のファイルをブラウザで開く
   ```
   target/site/jacoco/index.html
   ```

**カバレッジレポートには以下の情報が含まれます：**
- クラス別のカバレッジ率
- メソッド別のカバレッジ率
- 行単位のカバレッジ
- 分岐のカバレッジ

## トラブルシューティング

### ポート8080が既に使用されている場合

`application.properties` に以下を追加してポートを変更できます：

```properties
server.port=8081
```

### ビルドエラーが発生する場合

キャッシュをクリアしてビルドし直してください：

```powershell
.\mvnw.cmd clean
.\mvnw.cmd install
```

### Java バージョンの確認

Java 21が正しくインストールされているか確認してください：

```bash
java -version
```

## ライセンス

このプロジェクトは研修用教材です。

## お問い合わせ

問題が発生した場合は、プロジェクト管理者にお問い合わせください。
