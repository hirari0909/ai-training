/* _raw/SCxx.json から 03.テストの実施 テンプレート準拠のレポートを生成 */
const fs = require('fs');
const path = require('path');
const REPO = 'C:\\work\\Biz\\workspace\\ai-training';
const OUT = path.join(REPO, 'Docsシステム', '結合テスト', '結果');
const RAW = path.join(OUT, '_raw');
const ORDER = ['SC01', 'SC02', 'SC03', 'SC04', 'SC05', 'SC06'];

const data = {};
for (const sc of ORDER) {
  const p = path.join(RAW, `${sc}.json`);
  if (fs.existsSync(p)) data[sc] = JSON.parse(fs.readFileSync(p, 'utf8'));
}
const today = new Date().toISOString().slice(0, 10);

function esc(s) { return String(s == null ? '' : s).replace(/\|/g, '\\|').replace(/\n/g, ' '); }

// ---- シナリオ別結果 ----
for (const sc of ORDER) {
  const j = data[sc]; if (!j) continue;
  let md = `# 結合テスト結果: ${j.scenario} ${j.name}\n\n`;
  md += `## 実施環境\n| 項目 | 内容 |\n|------|------|\n`;
  md += `| 実施日時 | ${esc(j.env.datetime)} |\n| URL | ${j.env.url} |\n| データ | ${esc(j.env.data)} |\n| 実行手段 | ローカルPlaywright(Chromium) / tools/ita-executor |\n\n---\n\n`;
  for (const c of j.cases) {
    md += `## ${c.no}: ${c.name}（観点: ${c.viewpoint}）\n`;
    md += `**判定: ${c.judgment}**\n`;
    md += `**前提条件**: ${esc(c.precondition)}\n\n`;
    md += `| 手順No | 操作 | 入力値 | 期待結果 | 実際の結果 | 判定 | スクショ |\n`;
    md += `|--------|------|--------|---------|-----------|------|---------|\n`;
    for (const s of c.steps) {
      const shot = s.shot ? `![](${s.shot})` : '';
      md += `| ${s.no} | ${esc(s.op)} | ${esc(s.input)} | ${esc(s.expected)} | ${esc(s.actual)} | ${s.judge} | ${shot} |\n`;
    }
    md += `\n### スクリーンショット\n`;
    const shots = c.steps.filter(s => s.shot);
    if (shots.length) {
      md += `${shots.map(s => `![${c.no}-${s.no}](${s.shot})`).join('\n')}\n`;
    } else md += `(なし)\n`;
    if (c.judgment === 'NG') {
      const ngsteps = c.steps.filter(s => s.judge === 'NG');
      md += `\n### NG詳細\n`;
      for (const s of ngsteps) md += `- 手順${s.no}: 期待「${esc(s.expected)}」/ 実際「${esc(s.actual)}」\n`;
      if (c.ng) md += `- 例外: ${esc(c.ng)}\n`;
    }
    md += `\n---\n\n`;
  }
  fs.writeFileSync(path.join(OUT, `${sc}_結果.md`), md);
  console.log(`wrote ${sc}_結果.md`);
}

// ---- 実施サマリ ----
let total = 0, ok = 0, ng = 0;
const ngList = [];
for (const sc of ORDER) {
  const j = data[sc]; if (!j) continue;
  for (const c of j.cases) {
    total++;
    if (c.judgment === 'OK') ok++; else {
      ng++;
      const ngStep = c.steps.find(s => s.judge === 'NG');
      ngList.push({ no: c.no, name: c.name, exp: ngStep ? ngStep.expected : '', act: ngStep ? ngStep.actual : (c.ng || ''), shot: ngStep ? ngStep.shot : '' });
    }
  }
}
let s = `# 結合テスト 実施サマリ\n\n`;
s += `## 1. 実施情報\n| 項目 | 内容 |\n|------|------|\n`;
s += `| 実施日 | ${today} |\n| 実施者 | 自動実行(ローカルPlaywright/Chromium) |\n| 対象 | training-bookshelf |\n| 環境 | localhost:8080 / H2インメモリ / シナリオ毎にアプリ再起動で初期化 |\n| 対象ケース | 00_テストケース一覧.md（${total}件）|\n\n`;
s += `## 2. 結果集計\n| シナリオ | ケース数 | OK | NG | 未実施 | 結果ファイル |\n|----------|---------|----|----|--------|-------------|\n`;
for (const sc of ORDER) {
  const j = data[sc]; if (!j) continue;
  const o = j.cases.filter(c => c.judgment === 'OK').length;
  const n = j.cases.filter(c => c.judgment === 'NG').length;
  s += `| ${j.scenario} ${j.name} | ${j.cases.length} | ${o} | ${n} | 0 | [${sc}_結果.md](${sc}_結果.md) |\n`;
}
s += `| 合計 | ${total} | ${ok} | ${ng} | 0 | - |\n\n`;
s += `## 3. NG一覧\n`;
if (ngList.length === 0) s += `NGなし（全件OK）\n\n`;
else {
  s += `| ケースNo | ケース名 | 概要（期待 vs 実際）| 重大度 | 該当スクショ |\n|----------|---------|--------------------|--------|-------------|\n`;
  for (const x of ngList) s += `| ${x.no} | ${esc(x.name)} | 期待:${esc(x.exp)} / 実際:${esc(x.act)} | 高 | ${x.shot ? x.shot : '-'} |\n`;
  s += `\n`;
}
s += `## 4. 所見\n`;
s += `- 全${total}ケース中 OK ${ok} / NG ${ng}（消化率100%）。\n`;
s += `- 検出した障害は「NG一覧」参照。設計書（02画面遷移図/05個別画面設計）を正として判定。\n`;
s += `- データ前提: 「02.データ作成」の data.sql を投入。明示IDのIDENTITYシーケンス未更新による登録失敗を data.sql の RESTART 追加で解消（環境整備）。\n`;
s += `- 冪等性: シナリオ毎にアプリ(H2インメモリ)を再起動し初期状態から実施。\n`;
fs.writeFileSync(path.join(OUT, '00_テスト実施サマリ.md'), s);
console.log('wrote 00_テスト実施サマリ.md');
console.log(`TOTAL=${total} OK=${ok} NG=${ng}`);
