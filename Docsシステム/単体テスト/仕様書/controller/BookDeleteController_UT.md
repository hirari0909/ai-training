# 単体テスト仕様書: BookDeleteController（メソッド単位）

## 対象クラス
- クラス名: `jp.co.skig.training.bookshelf.controller.BookDeleteController`
- 対象ファイル: `src/main/java/jp/co/skig/training/bookshelf/controller/BookDeleteController.java`

## モック対象
| コンポーネント | クラス | 種別 |
|---|---|---|
| bookService | `BookService` | @Service |

---

# BK09: 書籍削除確認画面

## 削除確認画面表示 - confirmDelete()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK09-001 | 正常表示 | 書籍あり | GET /book/delete/confirm/1 | view: `book/BK09_BookDeleteConfirm` | model.book が対象書籍 |
| BK09-002 | 書籍が存在しない場合エラー画面 | bookService.findByIdがnull | GET /book/delete/confirm/999 | view: `book/error` | model.errorMessage="指定された書籍が見つかりません" |

## 削除実行 - delete()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK09-003 | 正常削除成功 | deleteCount=1 | POST /book/delete/1 | redirect:/book/delete/complete | - |
| BK09-004 | 対象が既に存在しない（deleteCount=0） | deleteCount=0 | POST /book/delete/1 | view: `book/error` | model.errorMessage="指定された書籍が見つかりません" |
| BK09-005 | DB例外発生時 | bookService.delete が例外throw | POST /book/delete/1 | view: `book/BK09_BookDeleteConfirm` | model.errorMessage="データの削除に失敗しました"、book再取得 |

---

# BK10: 書籍削除完了画面

## 削除完了画面表示 - deleteComplete()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK10-001 | 完了画面表示 | - | GET /book/delete/complete | view: `book/BK10_BookDeleteComplete` | - |
