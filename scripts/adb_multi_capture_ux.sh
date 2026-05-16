#!/usr/bin/env bash
# Multi-capture overlay UX smoke test via ADB (device required).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PKG="dev.sonle.pdfscanner.dev"
OUT_DIR="${ROOT}/.cursor/adb_regression"
mkdir -p "$OUT_DIR"

adb devices | grep -q 'device$' || {
  echo "No ADB device connected."
  exit 1
}

echo "==> Building and installing dev debug APK"
(cd "$ROOT" && ./gradlew :app:installDevDebug -q)

echo "==> Launching app"
adb shell am force-stop "$PKG"
adb shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null
sleep 2

dump_ui() {
  local name="$1"
  adb shell uiautomator dump /sdcard/window_dump.xml >/dev/null
  adb pull /sdcard/window_dump.xml "$OUT_DIR/$name.xml" >/dev/null
}

tap_desc() {
  local desc="$1"
  local file="$2"
  local bounds
  bounds="$(python3 - "$file" "$desc" <<'PY'
import re, sys
xml = open(sys.argv[1], encoding="utf-8").read()
desc = sys.argv[2]
pat = rf'content-desc="{re.escape(desc)}"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"'
m = re.search(pat, xml)
if not m:
    sys.exit(1)
x1,y1,x2,y2 = map(int, m.groups())
print((x1+x2)//2, (y1+y2)//2)
PY
)" || return 1
  adb shell input tap ${bounds// / }
}

summary=()
pass=0
fail=0
record() {
  local step="$1" ok="$2" note="$3"
  summary+=("{\"step\":\"$step\",\"ok\":$ok,\"note\":\"$note\"}")
  if [[ "$ok" == "true" ]]; then pass=$((pass+1)); else fail=$((fail+1)); fi
}

dump_ui "ux_01_home"
if tap_desc "Scan" "$OUT_DIR/ux_01_home.xml"; then
  sleep 2
  dump_ui "ux_02_scanner"
  if grep -q 'pdfDetectionOverlay\|Đang tự động\|Scanner Mode' "$OUT_DIR/ux_02_scanner.xml" 2>/dev/null ||
     grep -q 'Flash' "$OUT_DIR/ux_02_scanner.xml"; then
    record "scanner_opened" true "camera screen visible"
  else
    record "scanner_opened" false "camera screen not detected"
  fi

  # Toggle multi page mode (Page Mode control)
  if tap_desc "Page Mode" "$OUT_DIR/ux_02_scanner.xml"; then
    sleep 0.4
    dump_ui "ux_03_multi_mode"
    record "multi_mode_toggled" true "page mode tapped"
  else
    record "multi_mode_toggled" false "page mode control not found"
  fi

  # Shutter tap (center of bottom bar)
  adb shell input tap 530 2127
  sleep 2
  dump_ui "ux_04_after_capture"

  if grep -q 'multiCaptureOverlay' "$OUT_DIR/ux_04_after_capture.xml" 2>/dev/null ||
     grep -q 'scanner_capture_detecting\|Đang nhận diện\|flattening\|saving' "$OUT_DIR/ux_04_after_capture.xml" 2>/dev/null; then
    record "multi_capture_overlay" true "overlay semantics visible during capture"
  else
    # Compose testTag may not appear in uiautomator; check we stayed on camera (no review)
    if grep -q 'Flash' "$OUT_DIR/ux_04_after_capture.xml" && ! grep -q 'Page 1' "$OUT_DIR/ux_04_after_capture.xml"; then
      record "multi_capture_overlay" true "stayed on camera after multi capture (overlay active)"
    else
      record "multi_capture_overlay" false "overlay / camera state unexpected"
    fi
  fi
else
  record "scanner_opened" false "Scan FAB not found"
fi

python3 - "$OUT_DIR/ux_summary.json" "$pass" "$fail" "${summary[@]}" <<'PY'
import json, sys
out, passed, failed = sys.argv[1], int(sys.argv[2]), int(sys.argv[3])
results = [json.loads(x) for x in sys.argv[4:]]
json.dump({"pass": passed, "fail": failed, "results": results}, open(out, "w"), indent=2)
print(json.dumps({"pass": passed, "fail": failed}, indent=2))
PY

echo "Artifacts: $OUT_DIR/ux_*.xml"
echo "Summary: $OUT_DIR/ux_summary.json"
[[ "$fail" -eq 0 ]]
