# 単体テストを行う

## 前提（アプリ／フレームワーク）
- **対象**: `training-bookshelf/src/main/java` 配下のJavaファイル
- **バックエンド**: Spring Boot 3.5.8 (Java 21)
- **テストフレームワーク**: JUnit 5 (Jupiter)
- **モック**: Mockito（Spring コンテキストを伴う場合は `@MockitoBean`、伴わない場合は `@Mock`/`@InjectMocks`）
- **MVC テスト**: `MockMvc` + `@WebMvcTest`
- **カバレッジ**: JaCoCo（Maven plugin）
- **ビルド**: Maven（`mvn test` / `mvn jacoco:report`）

---

## 1. 単体テスト仕様書を作成する

### 入力
- 設計書（**正**）: `Docsシステム/外部設計/05個別画面設計` 配下の MD
- 対象ソース: `training-bookshelf/src/main/java/**/*.java`

### 出力
- 配置先: `Docsシステム/単体テスト/仕様書/<パッケージ階層と同じDir>/`
- ファイル名: `{ClassName}_UT.md`（クラス単位で1ファイル）

### 作成ルール
- 仕様書は**クラス単位**で作成し、内訳は**メソッド軸**で構成する
- ロジックを含まないクラスは作成不要（例：`@Data` のみの Entity、フィールドのみの Form/DTO、定数クラス）
- ケースは設計書の機能要件・画面項目定義・バリデーション・状態遷移から導出する。設計書と実装に乖離がある場合は**設計書を正**として仕様書を作る（実装側の修正はフェーズ3でレポートに残す）
- 観点の網羅は最低限以下を意識する
  - **正常系**（基本フロー）
  - **異常系**（DB例外、未存在、楽観ロック失敗、セッション切れ 等）
  - **境界値**（桁数上下限、0/null/空文字、最終ページ、最大件数 等）
  - **状態遷移**（セッション保存／復元／削除、リダイレクト先）
- Mapper など外側コンポーネントの呼び出しは mock 化を前提とする

### 仕様書テンプレート（必須セクション）
```markdown
# 単体テスト仕様書: {ClassName}（メソッド単位）

## 対象クラス
- クラス名: `jp.co.skig.training.bookshelf.<package>.{ClassName}`
- 対象ファイル: `src/main/java/.../{ClassName}.java`

## モック対象
| コンポーネント | クラス | 種別 |
|---|---|---|
| xxxService | `XxxService` | @Service |

---

# {画面ID}: {画面名}        ← Controller の場合は画面単位、Service/Util は省略可

## {処理名} - {メソッド名}()
**設計書参照: {画面ID} {章番号}**

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK01-001 | 初期表示成功 | 書籍3件 | パラメータなし | view: `book/...` | model.books に3件 |
```

参考: `Docsシステム/単体テスト/仕様書/controller/SampleController_UT.md`、
      `Docsシステム/単体テスト/仕様書/service/CategoryService_UT.md`

---

## 2. 単体テストクラスを作成する

### 入力
- 仕様書: `Docsシステム/単体テスト/仕様書/` 配下の `{ClassName}_UT.md`
- 対象ソース: `training-bookshelf/src/main/java/...`

### 出力
- 配置先: `training-bookshelf/src/test/java/...`（プロダクションコードと同一パッケージ）
- ファイル名: `{ClassName}Test.java`

### 作成ルール
- 仕様書のテストID（例 `BK01-001`）と Java メソッド名を1対1で対応させる
  - Java の識別子に `-` は使えないため、メソッド名では `_` に置換する（例: `BK01_001_初期表示成功`）
  - 形式: `void {画面ID}_{連番}_{シナリオ概要}()`
- 各テストメソッドの先頭にテスト観点をコメントで記載する
- アノテーション選定（**レイヤ別**）
  - **Controller**: `@WebMvcTest({Controller}.class)` + `MockMvc` を inject、依存 Bean は `@MockitoBean` で差し替え
  - **Service / Util / その他**: `@ExtendWith(MockitoExtension.class)` + `@Mock`（依存）+ `@InjectMocks`（被テスト）。Spring コンテキストは起動しない
  - **Mapper**: 単体テストでは作成しない（結合テスト範囲）
- mock 化対象は「自分から呼び出す」`@Component` / `@Service` / `@Mapper`
  - `MessageUtil` のような pojo な static ユーティリティは mock 化せず、実呼び出しのままで良い（必要に応じて messages.properties を読ませる）
- 構造は **Given / When / Then** コメントで明示する
- アサートは**項目単位**で値検証を行う（`size()` だけで終わらせない。中身の値・順序まで確認）
- テストデータは生成ヘルパー（`createBooks(n)` 等）を用意して見通しを保つ

### サンプル（Controller / `@WebMvcTest`）
```java
@Test
void BK01_005_ページ情報算出_端数あり() throws Exception {
    // Given: 該当書籍41件
    when(bookService.count(isNull(), isNull(), isNull())).thenReturn(41);
    when(bookService.findAll(any(), any(), any(), any(), any(), anyInt(), anyInt()))
            .thenReturn(createBooks(20));
    when(categoryService.findAll()).thenReturn(Collections.emptyList());

    // When & Then: 1ページ20件 → 41件で 3ページに分割
    mockMvc.perform(get("/book/list").param("page", "0"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("totalPages", 3))
            .andExpect(model().attribute("totalCount", 41));
}
```

### サンプル（Service / Mockito 単体）
```java
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock private BookMapper bookMapper;
    @InjectMocks private BookService bookService;

    @Test
    void findById_001_正常取得() {
        // Given
        Book expected = new Book();
        expected.setBookId(1);
        when(bookMapper.findById(1)).thenReturn(expected);

        // When
        Book actual = bookService.findById(1);

        // Then
        assertThat(actual.getBookId()).isEqualTo(1);
        verify(bookMapper, times(1)).findById(1);
    }
}
```

---

## 3. テストを実行する

### 実行
- 実行コマンド: `mvn test`（または `mvn -pl training-bookshelf test`）
- カバレッジレポート生成: `mvn jacoco:report`
- エラーがあれば修正する。修正は次の優先順
  1. テストコードのバグ → テスト側を直す
  2. 仕様書とテストの乖離 → 仕様書を確認して整合させる
  3. プロダクトコードの不具合 → コードを直す（修正内容はレポートに記載）

### カバレッジ目標
| 指標 | 目標 |
|------|------|
| Instruction | 95% 以上 |
| Branch | 90% 以上 |
| Line | 95% 以上 |
| Method | 100%（除外理由がある場合のみ未到達可） |

未到達分は**理由をレポートに記載**する（到達不能な防御コード／フレームワーク要件で残しているコード 等）。

### 出力
- JaCoCo レポート（HTML 一式）: `Docsシステム/単体テスト/テスト結果/jacoco/` にコピー
- クラス単位レポート（MD）: `Docsシステム/単体テスト/テスト結果/{ClassName}_Report.md`

### レポートテンプレート（必須セクション）
```markdown
# 単体テストレポート: {ClassName}

## 基本情報
| 項目 | 内容 |
|------|------|
| クラス名 | `jp.co.skig.training.bookshelf.<package>.{ClassName}` |
| テストクラス | `{ClassName}Test` |
| テスト件数 | n件 |
| テスト結果 | ALL GREEN / 一部失敗 |

## カバレッジ
| 指標 | カバー | 未カバー | カバレッジ率 |
|------|--------|----------|-------------|
| 命令(Instruction) | - | - | -% |
| 分岐(Branch) | - | - | -% |
| 行(Line) | - | - | -% |
| メソッド(Method) | - | - | -% |

## UT時に修正した内容
| No | 修正内容 | 修正箇所 | 理由 |
|----|---------|---------|------|

## 設計書との乖離
| No | 設計書 | 乖離内容 | 対応（設計書修正 or 実装維持） |
|----|--------|---------|------|

### 備考
- カバレッジ未到達がある場合はその理由
- 特記事項
```

参考: `Docsシステム/単体テスト/テスト結果/BookService_Report.md`、
      `Docsシステム/単体テスト/テスト結果/BookController_Report.md`
