# screenCapture ツール

HTMLモックを修正した後、Playwright（headless Chromium）を使ってスクリーンショットをPNG形式で自動保存するツールです。

## 対象・出力

| 種別 | パス |
|---|---|
| 入力（HTMLモック） | `Docsシステム/外部設計/05個別画面設計/html/*.html` |
| 出力（PNGファイル） | `Docsシステム/外部設計/05個別画面設計/img/*.png` |

## セットアップ（初回のみ）

```bash
cd tools/screenCapture
npm install
npx playwright install chromium
```

> GitHub Codespaces でも同様の手順で動作します。

## 使い方

HTMLモックを修正したら以下を実行します。

```bash
npm run screenshot
```

`html/` 配下のすべての `.html` ファイルが処理され、同名の `.png` が `img/` に保存されます。

## 設定のカスタマイズ

`screenshot.js` 内の以下の値を変更することで動作を調整できます。

```js
// ビューポートサイズ（px）
await page.setViewportSize({ width: 1280, height: 900 });

// ページ全体をキャプチャしない場合は false に変更
await page.screenshot({ path: pngPath, fullPage: true });
```

## ファイル構成

```
tools/screenCapture/
├── screenshot.js   # メインスクリプト
├── package.json    # 依存関係定義
└── README.md       # このファイル
```
