# 単体テスト仕様書: ReviewService（メソッド単位）

## 対象クラス
- クラス名: `jp.co.skig.training.bookshelf.service.ReviewService`
- 対象ファイル: `src/main/java/jp/co/skig/training/bookshelf/service/ReviewService.java`

## モック対象
| コンポーネント | クラス | 種別 |
|---|---|---|
| reviewMapper | `ReviewMapper` | @Mapper |

---

## 書籍IDに紐づくレビュー取得 - findByBookId()

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| SVC-REV-001 | 正常取得（複数件） | mapperが2件返却 | bookId=1 | 2件を返す | 各レビューの内容一致 |
| SVC-REV-002 | 該当なし（0件） | mapperが空リスト返却 | bookId=999 | 空リストを返す | size()=0 |

## レビュー登録 - register()

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| SVC-REV-003 | 正常登録 | - | Review | mapper.insert呼び出し | verify(mapper).insert(review) |

## 平均評価算出 - calculateAverageRating()

| No | テストケース | 前提条件 | 入力 | 期待結果 | 確認項目 |
|----|------------|---------|------|---------|---------|
| SVC-REV-004 | 複数レビューの平均を算出 | 評価[5,4,3] | reviews | 平均4.0 | 戻り値が4.0 |
| SVC-REV-005 | 小数点1位で四捨五入 | 評価[5,4,4] | reviews | 平均4.3（4.333→四捨五入） | 戻り値が4.3 |
| SVC-REV-006 | レビューが空リストの場合0.0 | reviews=空リスト | reviews | 0.0 | 戻り値が0.0 |
| SVC-REV-007 | レビューがnullの場合0.0 | reviews=null | null | 0.0 | 戻り値が0.0 |
