# 単体テスト仕様書: BookListController（メソッド単位）

## 対象クラス
- クラス名: `jp.co.skig.training.bookshelf.controller.BookListController`
- 対象ファイル: `src/main/java/jp/co/skig/training/bookshelf/controller/BookListController.java`

## モック対象
| コンポーネント | クラス | 種別 |
|---|---|---|
| bookService | `BookService` | @Service |
| categoryService | `CategoryService` | @Service |

---

# BK01: 書籍一覧画面

## 一覧表示 - list()
**設計書参照: BK01 3.1/3.2/3.3**

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK01-001 | 初期表示成功（検索条件なし） | 書籍3件、カテゴリ2件、セッション未設定 | パラメータなし | view: `book/BK01_BookList` | model.books に3件、totalCount=3、currentPage=1 |
| BK01-002 | 検索条件指定で検索しセッション保存 | 書籍1件ヒット | searchTitle="Java" | 検索実行 | model.searchTitle="Java"、セッションに条件保存 |
| BK01-003 | セッションから検索条件復元 | セッションに検索条件あり、パラメータなし | パラメータなし | セッション値を検索条件に使用 | model.searchTitle がセッション値と一致 |
| BK01-004 | ソート指定あり | sortColumn=title, sortOrder=ASC | 上記パラメータ | ソート指定を反映 | model.sortColumn="title", sortOrder="ASC" |
| BK01-005 | ソート未指定時デフォルト適用 | パラメータなし | パラメータなし | デフォルトソート | model.sortColumn="bookId", sortOrder="DESC" |
| BK01-006 | ページ情報算出（端数あり） | 該当書籍41件 | page=1 | 3ページに分割 | totalPages=3、totalCount=41 |
| BK01-007 | page指定が下限未満は1に補正 | 総件数20件 | page=0 | ページ補正 | currentPage=1 |
| BK01-008 | page指定が上限超過は最終ページに補正 | 総件数20件（1ページ） | page=99 | ページ補正 | currentPage=1（totalPages） |
| BK01-009 | 該当書籍0件で未登録メッセージ表示 | totalCount=0 | パラメータなし | noDataMessage表示 | model.noDataMessage が設定される |
| BK01-010 | 検索条件ありで検索結果メッセージ表示 | totalCount=5、検索条件あり | searchAuthor="夏目" | searchResultMessage表示 | model.searchResultMessage に件数含む |

## 検索条件クリア - clearSearch()
**設計書参照: BK01 3.3**

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| BK01-011 | クリア押下でセッション削除しリダイレクト | セッションに検索条件あり | `/book/list/clear` | リダイレクト | redirect:/book/list、セッション属性3件削除 |
