# 単体テスト仕様書: BookValidationSupport（メソッド単位）

## 対象クラス
- クラス名: `jp.co.skig.training.bookshelf.controller.BookValidationSupport`
- 対象ファイル: `src/main/java/jp/co/skig/training/bookshelf/controller/BookValidationSupport.java`

## モック対象
なし（外部コンポーネント呼び出しなし。`MessageUtil` は実呼び出し）

---

# BK03/BK06: 書籍登録・編集入力共通バリデーション

## 入力チェック - validate()
**設計書参照: 01共通仕様 2.1、BK03/BK06 画面項目定義**

| No | テストケース | 入力 | 期待結果 | 確認項目 |
|----|------------|------|---------|---------|
| VAL-001 | 全項目正常 | 全項目正しい値 | エラーなし | errors.isEmpty()=true |
| VAL-002 | タイトル未入力 | title="" | 必須エラー | errors.get("title") = "タイトルは必須です" |
| VAL-003 | タイトル文字数超過(101文字) | title=101文字 | 文字数エラー | errors.get("title") にメッセージ |
| VAL-004 | 著者未入力 | author=null | 必須エラー | errors.get("author") |
| VAL-005 | 著者文字数超過(51文字) | author=51文字 | 文字数エラー | errors.get("author") |
| VAL-006 | 出版社未入力 | publisher="" | 必須エラー | errors.get("publisher") |
| VAL-007 | 出版社文字数超過(51文字) | publisher=51文字 | 文字数エラー | errors.get("publisher") |
| VAL-008 | 出版日未入力 | publishedDate=null | 必須エラー | errors.get("publishedDate") |
| VAL-009 | 出版日フォーマット不正 | publishedDate="2024/13/40" | フォーマットエラー | errors.get("publishedDate") |
| VAL-010 | 出版日が未来日付 | publishedDate=翌日 | 未来日エラー | errors.get("publishedDate") |
| VAL-011 | ISBN未入力 | isbn="" | 必須エラー | errors.get("isbn") |
| VAL-012 | ISBN形式不正(9桁) | isbn="123456789" | 形式エラー | errors.get("isbn") |
| VAL-013 | ISBN10桁は正常 | isbn=10桁数字 | エラーなし | errors.get("isbn")=null |
| VAL-014 | ISBN13桁は正常 | isbn=13桁数字 | エラーなし | errors.get("isbn")=null |
| VAL-015 | カテゴリ未選択 | category="" | 必須エラー | errors.get("category") |
| VAL-016 | 価格未入力 | price="" | 必須エラー | errors.get("price") |
| VAL-017 | 価格が数値でない | price="abc" | 数値エラー | errors.get("price") |
| VAL-018 | 価格が負数 | price="-1" | 最小値エラー | errors.get("price") |
| VAL-019 | 価格0は境界値として正常 | price="0" | エラーなし | errors.get("price")=null |
| VAL-020 | 概要文字数超過(1001文字) | description=1001文字 | 文字数エラー | errors.get("description") |
| VAL-021 | 概要は任意項目のため未入力でも正常 | description=null | エラーなし | errors.get("description")=null |
| VAL-022 | 複数項目で同時にエラー発生 | title, isbn 共に不正 | 複数エラー | errors.size()>=2 |
