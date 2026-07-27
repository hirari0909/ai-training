# 単体テストレポート: BookValidationSupport

## 基本情報
| 項目 | 内容 |
|------|------|
| クラス名 | `jp.co.skig.training.bookshelf.controller.BookValidationSupport` |
| テストクラス | `BookValidationSupportTest` |
| テスト件数 | 22件 |
| テスト結果 | ALL GREEN |

## カバレッジ
| 指標 | カバー | 未カバー | カバレッジ率 |
|------|--------|----------|-------------|
| 命令(Instruction) | 285 | 0 | 100% |
| 分岐(Branch) | 30 | 0 | 100% |
| 行(Line) | 42 | 0 | 100% |
| メソッド(Method) | 1 | 0 | 100% |

## UT時に修正した内容
| No | 修正内容 | 修正箇所 | 理由 |
|----|---------|---------|------|
| 1 | テストコードの期待値を「概要は1000文字以内で入力してください」から「概要は1,000文字以内で入力してください」に修正 | `BookValidationSupportTest.validate_020` | `MessageFormat` によるメッセージ整形で数値がロケール標準の桁区切り（カンマ）付きで出力される仕様のため、テスト側の期待値誤りを修正した |

## 設計書との乖離
| No | 設計書 | 乖離内容 | 対応（設計書修正 or 実装維持） |
|----|--------|---------|------|

### 備考
- Spring コンテキストを起動しない単体テストのため、`MessageUtil` の静的フィールドへ実際の `MessageSource`（`ResourceBundleMessageSource`）をテスト側で注入する `MessageUtilTestSupport` を用意し、実メッセージでの検証を行った。
- 特記事項なし
