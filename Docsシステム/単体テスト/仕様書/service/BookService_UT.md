# 単体テスト仕様書: BookService（メソッド単位）

## 対象クラス
- クラス名: `jp.co.skig.training.bookshelf.service.BookService`
- 対象ファイル: `src/main/java/jp/co/skig/training/bookshelf/service/BookService.java`

## モック対象
| コンポーネント | クラス | 種別 |
|---|---|---|
| bookMapper | `BookMapper` | @Mapper |

---

## 書籍一覧取得 - findAll()

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| SVC-BK-001 | 正常取得・offset計算 | mapperが書籍2件返却 | page=2, pageSize=20 | offset=40でmapper呼び出し | mapper.findAll(..., 20, 40) を検証、結果の値・件数一致 |

## 件数取得 - count()

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| SVC-BK-002 | 正常取得 | mapperが5を返却 | 検索条件あり | count=5 | mapper.countの引数と戻り値一致 |

## 1件取得 - findById()

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| SVC-BK-003 | 存在する書籍を取得 | mapperがBookを返却 | bookId=1 | Bookを返す | 取得値の各項目一致 |
| SVC-BK-004 | 存在しない場合はnull | mapperがnullを返却 | bookId=999 | nullを返す | 戻り値がnull |

## ISBN重複チェック（自身除外なし） - isDuplicateIsbn(String)

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| SVC-BK-005 | 重複あり | mapper.findByIsbnがBookを返却 | isbn="1234567890" | true | 戻り値がtrue |
| SVC-BK-006 | 重複なし | mapper.findByIsbnがnull | isbn="1234567890" | false | 戻り値がfalse |

## ISBN重複チェック（自身除外あり） - isDuplicateIsbn(String, Integer)

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| SVC-BK-007 | 重複だが自分自身は除外 | 既存BookのbookId=1と一致 | isbn, bookId=1 | false | 戻り値がfalse |
| SVC-BK-008 | 重複かつ別の書籍 | 既存BookのbookId=2 | isbn, bookId=1 | true | 戻り値がtrue |
| SVC-BK-009 | 重複なし | mapper.findByIsbnがnull | isbn, bookId=1 | false | 戻り値がfalse |

## 登録 - register()

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| SVC-BK-010 | 正常登録 | - | Book | mapper.insert呼び出し | verify(mapper).insert(book) |

## 更新 - update()

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| SVC-BK-011 | 正常更新（1件更新） | mapper.updateが1を返却 | Book | 戻り値1 | 戻り値が1 |
| SVC-BK-012 | 楽観ロック失敗（0件更新） | mapper.updateが0を返却 | Book | 戻り値0 | 戻り値が0 |

## 削除 - delete()

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| SVC-BK-013 | 正常削除 | mapper.deleteが1を返却 | bookId=1 | 戻り値1 | 戻り値が1 |
| SVC-BK-014 | 対象なし | mapper.deleteが0を返却 | bookId=999 | 戻り値0 | 戻り値が0 |
