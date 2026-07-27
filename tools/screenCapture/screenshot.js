/**
 * HTMLモック スクリーンショットツール
 *
 * 対象: Docsシステム/外部設計/05個別画面設計/html/*.html
 * 出力: Docsシステム/外部設計/05個別画面設計/img/*.png
 *
 * 使用方法:
 *   npm install
 *   npx playwright install chromium
 *   npm run screenshot
 */

const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

// ディレクトリパス（このスクリプトからの相対パス）
const HTML_DIR = path.resolve(__dirname, '../../Docsシステム/外部設計/05個別画面設計/html');
const IMG_DIR = path.resolve(__dirname, '../../Docsシステム/外部設計/05個別画面設計/img');

async function takeScreenshots() {
  // imgディレクトリが存在しない場合は作成
  if (!fs.existsSync(IMG_DIR)) {
    fs.mkdirSync(IMG_DIR, { recursive: true });
    console.log(`imgディレクトリを作成しました: ${IMG_DIR}`);
  }

  // HTMLファイル一覧を取得
  const htmlFiles = fs.readdirSync(HTML_DIR).filter(f => f.endsWith('.html'));
  if (htmlFiles.length === 0) {
    console.log('HTMLファイルが見つかりませんでした。');
    return;
  }

  console.log(`Chromium (headless) を起動します...`);
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();

  // ビューポートサイズ（画面設計に合わせて調整可能）
  await page.setViewportSize({ width: 1280, height: 900 });

  console.log(`\n${htmlFiles.length} 件のHTMLファイルを処理します...\n`);

  let successCount = 0;
  let errorCount = 0;

  for (const htmlFile of htmlFiles) {
    const htmlPath = path.join(HTML_DIR, htmlFile);
    const baseName = path.basename(htmlFile, '.html');
    const pngPath = path.join(IMG_DIR, `${baseName}.png`);

    // file:// URLに変換（Windowsのバックスラッシュも考慮）
    const fileUrl = 'file:///' + htmlPath.replace(/\\/g, '/');

    try {
      await page.goto(fileUrl, { waitUntil: 'networkidle' });

      // ページ全体（スクロール含む）をキャプチャ
      await page.screenshot({
        path: pngPath,
        fullPage: true,
      });

      console.log(`  ✔ ${htmlFile}  →  ${baseName}.png`);
      successCount++;
    } catch (err) {
      console.error(`  ✖ ${htmlFile}  エラー: ${err.message}`);
      errorCount++;
    }
  }

  await browser.close();

  console.log(`\n完了: 成功 ${successCount} 件 / 失敗 ${errorCount} 件`);
  console.log(`出力先: ${IMG_DIR}`);
}

takeScreenshots().catch(err => {
  console.error('予期しないエラーが発生しました:', err);
  process.exit(1);
});
