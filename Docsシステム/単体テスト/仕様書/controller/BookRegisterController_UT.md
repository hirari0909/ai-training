# 単体テスト仕様書: BookRegisterController（メソッド単位）

## 対象クラス
- クラス名: `jp.co.skig.training.bookshelf.controller.BookRegisterController`
- 対象ファイル: `src/main/java/jp/co/skig/training/bookshelf/controller/BookRegisterController.java`

## モック対象
| コンポーネント | クラス | 種別 |
|---|---|---|
| bookService | `BookService` | @Service |
| categoryService | `CategoryService` | @Service |

---

# BK03: 書籍登録入力画面

## 入力画面表示 - newForm()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK03-001 | 初期表示（セッションフォームなし） | セッション未設定 | GET /book/create | view: `book/BK03_BookRegisterInput` | model.form が新規の空Form |
| BK03-002 | セッションフォーム復元 | セッションにフォーム保存済み | GET /book/create | セッション値を復元 | model.form がセッション値と一致 |

## 登録確認画面表示 - confirmRegister()
**設計書参照: BK03→BK04**

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK03-003 | 入力値正常で確認画面へ遷移 | 全項目正常値 | POST /book/create/confirm | view: `book/BK04_BookRegisterConfirm` | セッションにフォーム保存、model.categoryName設定 |
| BK03-004 | バリデーションエラーで入力画面再表示 | title未入力 | POST /book/create/confirm | view: `book/BK03_BookRegisterInput` | model.errors にtitleエラー、入力値保持 |
| BK03-005 | 出版日不正値は保存時にnullとなる | publishedDate="invalid" | POST /book/create/confirm | フォーム変換失敗 | form.publishedDate=null（エラー表示） |
| BK03-006 | カテゴリ不正値は保存時にnullとなる | category="abc" | POST /book/create/confirm | フォーム変換失敗 | form.categoryId=null |
| BK03-007 | 価格不正値は保存時にnullとなる | price="abc" | POST /book/create/confirm | フォーム変換失敗 | form.price=null |

## 登録キャンセル - cancelRegister()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK03-008 | キャンセルでセッション削除し一覧へ | セッションにフォームあり | GET /book/create/cancel | redirect:/book/list | セッション属性削除 |

---

# BK04→BK05: 書籍登録処理

## 登録実行 - register()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK04-001 | セッション切れ（フォームなし） | セッション未設定 | POST /book/create | redirect:/book/create | flash.errorMessage="セッションが切れました。最初からやり直してください。" |
| BK04-002 | ISBN重複エラー | isDuplicateIsbn=true | POST /book/create | view: `book/BK04_BookRegisterConfirm` | model.errorMessage="このISBNは既に登録されています" |
| BK04-003 | 正常登録成功 | isDuplicateIsbn=false、登録成功 | POST /book/create | redirect:/book/create/complete?bookId=1 | セッションのフォーム削除、bookService.register呼び出し |
| BK04-004 | DB例外発生時 | bookService.register が例外throw | POST /book/create | view: `book/BK04_BookRegisterConfirm` | model.errorMessage="データの登録に失敗しました" |

## 登録完了画面表示 - registerComplete()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK05-001 | 完了画面表示 | bookId=1 | GET /book/create/complete?bookId=1 | view: `book/BK05_BookRegisterComplete` | model.bookId=1 |
