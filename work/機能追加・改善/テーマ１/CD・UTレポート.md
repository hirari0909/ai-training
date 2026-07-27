# CD・UTレポート：お勧めフラグ（recommended）機能追加

## 1. 対象・目的

### 修正依頼内容
- 書籍に「お勧めフラグ」を追加し、一覧画面（BK01）でタイトル横に「おすすめ」バッジを表示する。
- 書籍登録（BK03/BK04）・書籍編集（BK06/BK07）画面で、お勧めフラグをチェックボックスとして入力・確認できるようにする。
- バリデーション対象外の任意項目とする。

### 参照ドキュメント（設計修正フェーズの成果物）
- [work/機能追加・改善/テーマ１/設計書修正レポート.md](./設計書修正レポート.md)（本CD・UTの元となる設計変更内容）
- [Docsシステム/外部設計/03ER図.md](../../../Docsシステム/外部設計/03ER図.md)
- [Docsシステム/外部設計/04TBL定義.md](../../../Docsシステム/外部設計/04TBL定義.md)
- [05個別画面設計_BK01_書籍一覧画面.md](../../../Docsシステム/外部設計/05個別画面設計/05個別画面設計_BK01_書籍一覧画面.md)
- [05個別画面設計_BK03_書籍登録入力画面.md](../../../Docsシステム/外部設計/05個別画面設計/05個別画面設計_BK03_書籍登録入力画面.md)
- [05個別画面設計_BK04_書籍登録確認画面.md](../../../Docsシステム/外部設計/05個別画面設計/05個別画面設計_BK04_書籍登録確認画面.md)
- [05個別画面設計_BK06_書籍編集入力画面.md](../../../Docsシステム/外部設計/05個別画面設計/05個別画面設計_BK06_書籍編集入力画面.md)
- [05個別画面設計_BK07_書籍編集確認画面.md](../../../Docsシステム/外部設計/05個別画面設計/05個別画面設計_BK07_書籍編集確認画面.md)

### 使用プロンプト
- CD: [`prompt/01.CD/CD_Executor.md`](../../../prompt/01.CD/CD_Executor.md)（+ [`CD_ルール.md`](../../../prompt/01.CD/CD_ルール.md)）
- UT: [`prompt/02.UT1/単体テスト_Executor.md`](../../../prompt/02.UT1/単体テスト_Executor.md)

---

## 2. CD（コーディング）実施内容

### 変更ファイル一覧
| ファイル | 種別 | 内容 |
|---|---|---|
| [schema.sql](../../../training-bookshelf/src/main/resources/schema.sql) | DDL | `books` テーブルに `is_recommended BOOLEAN NOT NULL DEFAULT FALSE` 列を追加 |
| [Book.java](../../../training-bookshelf/src/main/java/jp/co/skig/training/bookshelf/entity/Book.java) | Entity | `recommended` (Boolean) フィールド追加 |
| [BookRegisterForm.java](../../../training-bookshelf/src/main/java/jp/co/skig/training/bookshelf/form/BookRegisterForm.java) | Form | `recommended` フィールド追加 |
| [BookEditForm.java](../../../training-bookshelf/src/main/java/jp/co/skig/training/bookshelf/form/BookEditForm.java) | Form | `recommended` フィールド追加 |
| [BookMapper.java](../../../training-bookshelf/src/main/java/jp/co/skig/training/bookshelf/mapper/BookMapper.java) | Mapper | `findById`/`insert`/`update` に `is_recommended` 列を反映 |
| [BookMapper.xml](../../../training-bookshelf/src/main/resources/jp/co/skig/training/bookshelf/mapper/BookMapper.xml) | Mapper(XML) | `findAll` のSELECT/GROUP BYに `is_recommended AS recommended` を追加 |
| [BookRegisterController.java](../../../training-bookshelf/src/main/java/jp/co/skig/training/bookshelf/controller/BookRegisterController.java) | Controller | `confirmRegister()`/`register()` でお勧めフラグを受け取りForm/Entityへ反映 |
| [BookEditController.java](../../../training-bookshelf/src/main/java/jp/co/skig/training/bookshelf/controller/BookEditController.java) | Controller | `editForm()`(toEditForm)/`confirmEdit()`/`update()` でお勧めフラグを受け取り・復元・反映 |
| [BK01_BookList.html](../../../training-bookshelf/src/main/resources/templates/book/BK01_BookList.html) | View | タイトル横に「おすすめ」バッジを追加 |
| [BK03_BookRegisterInput.html](../../../training-bookshelf/src/main/resources/templates/book/BK03_BookRegisterInput.html) | View | お勧めフラグ チェックボックス追加 |
| [BK04_BookRegisterConfirm.html](../../../training-bookshelf/src/main/resources/templates/book/BK04_BookRegisterConfirm.html) | View | お勧めフラグ（する／しない）確認表示追加 |
| [BK06_BookEditInput.html](../../../training-bookshelf/src/main/resources/templates/book/BK06_BookEditInput.html) | View | お勧めフラグ チェックボックス追加（既存値プリセット） |
| [BK07_BookEditConfirm.html](../../../training-bookshelf/src/main/resources/templates/book/BK07_BookEditConfirm.html) | View | お勧めフラグ（する／しない）確認表示追加 |

### 主な差分

#### DBスキーマ（schema.sql）
```diff
     price INT NOT NULL,
     description TEXT,
+    is_recommended BOOLEAN NOT NULL DEFAULT FALSE,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     UNIQUE (isbn),
```
※既存の `data.sql` の書籍INSERT文はカラム指定型（`is_recommended` を含まない）のため、
　`DEFAULT FALSE` により既存の50件は全て `recommended=false` となる（データ変更不要）。

#### Entity（Book.java）
```diff
   private Integer price;
   private String description;
+
+  /** お勧めフラグ（ONの場合、一覧画面でタイトル横に「おすすめ」バッジを表示） */
+  private Boolean recommended;
+
   private LocalDateTime createdAt;
   private LocalDateTime updatedAt;
```

#### Mapper（BookMapper.java／BookMapper.xml）
- `findById`: `SELECT` 列に `b.is_recommended AS recommended` を追加
- `insert`: 列リスト・VALUES に `is_recommended` / `#{recommended}` を追加
- `update`: `SET` 句に `is_recommended = #{recommended}` を追加
- `findAll`（XML）: `SELECT` 列・`GROUP BY` 句に `is_recommended`（`AS recommended`）を追加
  - `application.properties` の `mybatis.configuration.map-underscore-to-camel-case=true` により
    アンダースコア→キャメルケースの自動変換は行われるが、`is_recommended` → `isRecommended` に
    変換されるため、Entityのフィールド名 `recommended` に合わせて明示的に `AS recommended` の
    エイリアスを付与した（既存の `avg_rating`/`review_count` と同じ方式）。

#### Controller（BookRegisterController.java）
```diff
       @RequestParam(required = false) String price,
       @RequestParam(required = false) String description,
+      @RequestParam(required = false) String recommended,
       HttpSession session,
       Model model) {
     ...
     form.setDescription(description);
+    form.setRecommended(recommended != null);
     trySetPublishedDate(form, publishedDate);
```
```diff
       book.setPrice(form.getPrice());
       book.setDescription(form.getDescription());
+      book.setRecommended(form.getRecommended());
       bookService.register(book);
```
※チェックボックスは `value` 属性未指定のため、チェック時のみ `recommended` パラメータが
　送信される（値は `on`）。未チェック時はパラメータ自体が送信されないため、
　`recommended != null` でON/OFFを判定する。

#### Controller（BookEditController.java）
```diff
       @RequestParam(required = false) String price,
       @RequestParam(required = false) String description,
+      @RequestParam(required = false) String recommended,
       @RequestParam String updatedAt,
     ...
     form.setDescription(description);
+    form.setRecommended(recommended != null);
     form.setUpdatedAt(parseUpdatedAt(updatedAt));
```
```diff
       book.setPrice(form.getPrice());
       book.setDescription(form.getDescription());
+      book.setRecommended(form.getRecommended());
       book.setUpdatedAt(form.getUpdatedAt());
```
```diff
     form.setCategoryId(book.getCategoryId());
     form.setPrice(book.getPrice());
     form.setDescription(book.getDescription());
+    form.setRecommended(book.getRecommended());
     form.setUpdatedAt(book.getUpdatedAt());
     return form;
```

#### View（BK01_BookList.html）
```diff
-<td><a th:href="@{/book/detail/{id}(id=${book.bookId})}" class="book-title-link" th:text="${book.title}"></a></td>
+<td><a th:href="@{/book/detail/{id}(id=${book.bookId})}" class="book-title-link" th:text="${book.title}"></a><span class="badge bg-danger ms-1" th:if="${book.recommended}">おすすめ</span></td>
```

#### View（BK03_BookRegisterInput.html／BK06_BookEditInput.html）
```diff
+<div class="row mb-3">
+    <div class="col-md-12">
+        <div class="form-check">
+            <input type="checkbox" class="form-check-input" id="recommended" name="recommended" th:checked="${form.recommended}">
+            <label for="recommended" class="form-check-label">お勧めフラグ（一覧画面でタイトル横に「おすすめ」バッジを表示）</label>
+        </div>
+    </div>
+</div>
```

#### View（BK04_BookRegisterConfirm.html／BK07_BookEditConfirm.html）
```diff
+<div class="row info-row">
+    <div class="col-md-6">
+        <div class="info-label">お勧めフラグ</div>
+        <div class="info-value" th:text="${form.recommended} ? 'する' : 'しない'"></div>
+    </div>
+</div>
```

### ビルド確認
```
$ ./mvnw compile
BUILD SUCCESS
```
コンパイルエラーなし。

---

## 3. UT（単体テスト）実施内容

### 影響範囲の確認
- `BookListController` はModelに `books`（`Book`エンティティのリスト）を渡すのみで、
  `recommended` の有無に関わらずロジック変更が不要なため、テスト仕様書・テストコードの
  変更は不要と判断。
- `BookRegisterController`・`BookEditController` はお勧めフラグの受け取り・Form/Entityへの
  反映というロジック変更があるため、UT仕様書・テストコードを追加した。
- `BookMapper`（Mapper）はプロジェクトの単体テスト方針上、対象外（結合テストの範囲）。

### UT仕様書の更新
| ファイル | 追加したテストケース |
|---|---|
| [BookRegisterController_UT.md](../../../Docsシステム/単体テスト/仕様書/controller/BookRegisterController_UT.md) | BK03-009（お勧めフラグON）、BK03-010（お勧めフラグ未指定）、BK04-005（Bookエンティティへの反映） |
| [BookEditController_UT.md](../../../Docsシステム/単体テスト/仕様書/controller/BookEditController_UT.md) | BK06-012（お勧めフラグON）、BK06-013（お勧めフラグ未指定）、BK07-007（Bookエンティティへの反映） |

### テストコードの追加
| ファイル | 追加したテストメソッド |
|---|---|
| [BookRegisterControllerTest.java](../../../training-bookshelf/src/test/java/jp/co/skig/training/bookshelf/controller/BookRegisterControllerTest.java) | `BK03_009_お勧めフラグON…`、`BK03_010_お勧めフラグ未指定…`、`BK04_005_お勧めフラグがBookエンティティへ反映される` |
| [BookEditControllerTest.java](../../../training-bookshelf/src/test/java/jp/co/skig/training/bookshelf/controller/BookEditControllerTest.java) | `BK06_012_お勧めフラグON…`、`BK06_013_お勧めフラグ未指定…`、`BK07_007_お勧めフラグがBookエンティティへ反映される` |

Book/Formへの反映確認は `ArgumentCaptor` を用いて `bookService.register()`／`update()` に
渡される `Book` エンティティの `recommended` 値を直接検証した。

### テスト実行結果
```
$ ./mvnw test
[INFO] Tests run: 123, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
既存117件 + 新規追加16件（BookRegisterControllerTest +3、BookEditControllerTest +3、
合計6件の想定に対し、実測ではBookRegisterControllerTestが13→16件、BookEditControllerTestが18→21件、
それぞれ+3件ずつで整合）で全件成功。

### カバレッジ（JaCoCo）
| クラス | 命令 | 分岐 | 行 | メソッド |
|---|---|---|---|---|
| BookRegisterController | 306/306 (100%) | 10/10 (100%) | 76/76 (100%) | 11/11 (100%) |
| BookEditController | 434/434 (100%) | 18/18 (100%) | 109/109 (100%) | 13/13 (100%) |
| BookListController | 208/208 (100%) | 20/20 (100%) | 42/42 (100%) | 3/3 (100%) |

いずれもカバレッジ目標（Instruction≥95%, Branch≥90%, Line≥95%, Method 100%）を達成。

JaCoCo HTMLレポートは [Docsシステム/単体テスト/テスト結果/jacoco/](../../../Docsシステム/単体テスト/テスト結果/jacoco/) に反映済み。

### 単体テストレポートの更新
- [BookRegisterController_Report.md](../../../Docsシステム/単体テスト/テスト結果/BookRegisterController_Report.md)：テスト件数13→16件、カバレッジ数値を更新
- [BookEditController_Report.md](../../../Docsシステム/単体テスト/テスト結果/BookEditController_Report.md)：テスト件数18→21件、カバレッジ数値を更新

### UT時に発見・修正した内容
- 製品コード・テストコード・設計書との乖離は発見されなかった。

---

## 4. 今後の課題（スコープ外）
- 結合テスト（SC01/SC02の書籍登録・編集フロー）でのお勧めフラグ確認は本レポートの対象外。
  必要に応じて `Docsシステム/結合テスト/ケース/` の該当シナリオへの追記を検討。
- BK02（書籍詳細画面）へのお勧めフラグ表示は設計スコープ外のため未実装。
