# 単体テスト仕様書: CategoryService（メソッド単位）

## 対象クラス
- クラス名: `jp.co.skig.training.bookshelf.service.CategoryService`
- 対象ファイル: `src/main/java/jp/co/skig/training/bookshelf/service/CategoryService.java`

## モック対象
| コンポーネント | クラス | 種別 |
|---|---|---|
| categoryMapper | `CategoryMapper` | @Mapper |

---

## カテゴリ全件取得 - findAll()

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| SVC-CAT-001 | 正常取得（複数件） | mapperが3件返却 | - | 3件を返す | 各カテゴリの内容・順序一致 |
| SVC-CAT-002 | 該当なし（0件） | mapperが空リスト返却 | - | 空リストを返す | size()=0 |
