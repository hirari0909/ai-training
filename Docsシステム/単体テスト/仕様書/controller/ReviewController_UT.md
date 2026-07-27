# 単体テスト仕様書: ReviewController（メソッド単位）

## 対象クラス
- クラス名: `jp.co.skig.training.bookshelf.controller.ReviewController`
- 対象ファイル: `src/main/java/jp/co/skig/training/bookshelf/controller/ReviewController.java`

## モック対象
| コンポーネント | クラス | 種別 |
|---|---|---|
| bookService | `BookService` | @Service |
| reviewService | `ReviewService` | @Service |

---

# BK11: レビュー投稿入力画面

## 入力画面表示 - newForm()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK11-001 | 初期表示（セッションフォームなし） | 書籍あり | GET /books/1/reviews/new | view: `book/BK11_ReviewPostInput` | model.form が新規Form |
| BK11-002 | セッションフォーム復元（同一bookId） | セッションにform(bookId=1) | GET /books/1/reviews/new | セッション値を復元 | model.form がセッション値 |
| BK11-003 | 書籍が存在しない場合一覧へリダイレクト | bookService.findByIdがnull | GET /books/999/reviews/new | redirect:/book/list | - |

## 投稿確認画面表示 - confirmReview()
**設計書参照: BK11 バリデーション**

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK11-004 | 入力値正常で確認画面へ遷移 | 書籍あり、全項目正常 | reviewerName="山田", rating=5 | view: `book/BK12_ReviewPostConfirm` | セッションにフォーム保存 |
| BK11-005 | 書籍が存在しない場合一覧へリダイレクト | bookService.findByIdがnull | POST /books/999/reviews/confirm | redirect:/book/list | - |
| BK11-006 | レビュアー名未入力 | reviewerName="" | POST | view: `book/BK11_ReviewPostInput` | errors.reviewerName="レビュアー名は必須です" |
| BK11-007 | レビュアー名文字数超過(51文字) | reviewerName=51文字 | POST | 文字数エラー | errors.reviewerName |
| BK11-008 | 評価未入力 | rating="" | POST | 必須エラー | errors.rating="評価は必須です" |
| BK11-009 | 評価が数値でない | rating="abc" | POST | 範囲エラー | errors.rating="評価は1〜5の範囲で入力してください" |
| BK11-010 | 評価が範囲外(0または6) | rating="6" | POST | 範囲エラー | errors.rating |
| BK11-011 | コメント文字数超過(1001文字) | comment=1001文字 | POST | 文字数エラー | errors.comment |
| BK11-012 | コメントは任意項目のため未入力でも正常 | comment=null | POST | エラーなし | errors.comment=null |

---

# BK12→BK13: レビュー投稿処理

## 投稿実行 - postReview()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK12-001 | セッション切れ（フォームなし） | セッション未設定 | POST /books/1/reviews | redirect:/books/1/reviews/new | flash.errorMessage="セッションが切れました。最初からやり直してください。" |
| BK12-002 | セッションのbookIdと不一致 | セッションform(bookId=2) | POST /books/1/reviews | redirect:/books/1/reviews/new | 同上メッセージ |
| BK12-003 | 正常投稿成功 | 全項目正常 | POST /books/1/reviews | redirect:/books/1/reviews/complete | reviewService.register呼び出し、セッションにcomplete情報保存 |
| BK12-004 | DB例外発生時 | reviewService.register が例外throw | POST /books/1/reviews | view: `book/BK12_ReviewPostConfirm` | model.errorMessage="データの登録に失敗しました" |

## 投稿完了画面表示 - reviewComplete()
| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK13-001 | 完了画面表示（完了情報あり） | セッションにcompleteInfoあり | GET /books/1/reviews/complete | view: `book/BK13_ReviewPostComplete` | model.reviewerName, model.rating設定、セッション削除 |
| BK13-002 | 完了情報なしでも表示可能 | セッションにcompleteInfoなし | GET /books/1/reviews/complete | view: `book/BK13_ReviewPostComplete` | model.book設定、reviewerName属性は追加されない |
