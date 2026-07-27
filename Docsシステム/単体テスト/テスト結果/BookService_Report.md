# 単体テストレポート: BookService

## 基本情報
| 項目 | 内容 |
|------|------|
| クラス名 | `jp.co.skig.training.bookshelf.service.BookService` |
| テストクラス | `BookServiceTest` |
| テスト件数 | 15件 |
| テスト結果 | ALL GREEN |

## カバレッジ
| 指標 | カバー | 未カバー | カバレッジ率 |
|------|--------|----------|-------------|
| 命令(Instruction) | 73 | 0 | 100% |
| 分岐(Branch) | 6 | 0 | 100% |
| 行(Line) | 13 | 0 | 100% |
| メソッド(Method) | 9 | 0 | 100% |

## UT時に修正した内容
| No | 修正内容 | 修正箇所 | 理由 |
|----|---------|---------|------|
| 1 | `findAll_001`/`count_002` に `searchPublisher` 引数を追加 | `BookServiceTest.java` | `BookMapper`/`BookService` のメソッドシグネチャ変更（出版社検索追加）への追随 |
| 2 | `findAllPublishers_010` を新規追加 | `BookServiceTest.java` | 新規メソッド `findAllPublishers()` のカバレッジ確保 |

## 設計書との乖離
| No | 設計書 | 乖離内容 | 対応（設計書修正 or 実装維持） |
|----|--------|---------|------|
| - | なし | - | - |

### 備考
- テーマ３（出版社検索）対応: `findAllPublishers()` 追加に伴い新規テストを追加し、既存2テストを更新。カバレッジ100%を維持。
- 特記事項なし
