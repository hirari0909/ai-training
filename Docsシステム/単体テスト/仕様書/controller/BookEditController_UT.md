# 単体テスト仕様書: BookEditController（メソッド単位）

## 対象クラス
- クラス名: `jp.co.skig.training.bookshelf.controller.BookEditController`
- 対象ファイル: `src/main/java/jp/co/skig/training/bookshelf/controller/BookEditController.java`

## モック対象
| コンポーネント | クラス | 種別 |
|---|---|---|
| bookService | `BookService` | @Service |
| categoryService | `CategoryService` | @Service |

---

# BK06: 書籍編集入力画面

## 編集入力画面表示 - editForm()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK06-001 | 書籍DBから取得して初期表示 | セッションフォームなし、書籍あり | GET /book/edit/1 | view: `book/BK06_BookEditInput` | model.form がDB値と一致 |
| BK06-002 | セッションフォームが同一bookIdなら復元 | セッションにform(bookId=1)あり | GET /book/edit/1 | セッション値を使用 | model.form がセッション値と一致（DB再取得なし） |
| BK06-003 | セッションフォームが別bookIdなら再取得 | セッションにform(bookId=2)あり | GET /book/edit/1 | DBから再取得 | model.form がDB値(bookId=1) |
| BK06-004 | 書籍が存在しない場合エラー画面 | bookService.findByIdがnull | GET /book/edit/999 | view: `book/error` | model.errorMessage="指定された書籍が見つかりません" |

## 編集確認画面表示 - confirmEdit()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK06-005 | 入力値正常で確認画面へ遷移 | 全項目正常値 | POST /book/edit/1/confirm | view: `book/BK07_BookEditConfirm` | セッションにフォーム保存 |
| BK06-006 | バリデーションエラーで入力画面再表示 | isbn不正 | POST /book/edit/1/confirm | view: `book/BK06_BookEditInput` | model.errors にisbnエラー |
| BK06-007 | updatedAt不正値はnullとなる | updatedAt="invalid" | POST /book/edit/1/confirm | パース失敗 | form.updatedAt=null |

## 編集キャンセル - cancelEdit()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK06-008 | キャンセルでセッション削除し詳細へ | セッションにフォームあり | GET /book/edit/1/cancel | redirect:/book/detail/1 | セッション属性削除 |

---

# BK07→BK08: 書籍更新処理

## 更新実行 - update()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK07-001 | セッション切れ（フォームなし） | セッション未設定 | POST /book/edit/1 | redirect:/book/edit/1 | flash.errorMessage="セッションが切れました。最初からやり直してください。" |
| BK07-002 | セッションのbookIdと不一致 | セッションform(bookId=2) | POST /book/edit/1 | redirect:/book/edit/1 | 同上メッセージ |
| BK07-003 | ISBN重複エラー | isDuplicateIsbn=true | POST /book/edit/1 | view: `book/BK07_BookEditConfirm` | model.errorMessage="このISBNは既に登録されています" |
| BK07-004 | 楽観ロック失敗（updateCount=0） | bookService.update が0を返す | POST /book/edit/1 | redirect:/book/detail/1 | flash.errorMessage="他のユーザーによって更新されています。最新のデータを取得してください。"、セッション削除 |
| BK07-005 | 正常更新成功 | updateCount=1 | POST /book/edit/1 | redirect:/book/edit/complete?bookId=1 | セッションのフォーム削除 |
| BK07-006 | DB例外発生時 | bookService.update が例外throw | POST /book/edit/1 | view: `book/BK07_BookEditConfirm` | model.errorMessage="データの更新に失敗しました" |

## 編集完了画面表示 - editComplete()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK08-001 | 完了画面表示 | bookId=1 | GET /book/edit/complete?bookId=1 | view: `book/BK08_BookEditComplete` | model.bookId=1 |
