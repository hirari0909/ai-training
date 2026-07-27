# 単体テスト仕様書: BookDetailController（メソッド単位）

## 対象クラス
- クラス名: `jp.co.skig.training.bookshelf.controller.BookDetailController`
- 対象ファイル: `src/main/java/jp/co/skig/training/bookshelf/controller/BookDetailController.java`

## モック対象
| コンポーネント | クラス | 種別 |
|---|---|---|
| bookService | `BookService` | @Service |
| reviewService | `ReviewService` | @Service |

---

# BK02: 書籍詳細画面

## 詳細表示 - detail()
**設計書参照: BK02**

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK02-001 | 正常表示（レビューあり） | 書籍1件、レビュー2件 | bookId=1 | view: `book/BK02_BookDetail` | model.book, model.reviews に2件、avgRating算出値 |
| BK02-002 | 書籍が存在しない場合エラー画面 | bookService.findById が null | bookId=999 | view: `book/error` | model.errorMessage="指定された書籍が見つかりません"、redirectUrl=/book/list |
