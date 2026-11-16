#!/usr/bin/env sh
# Installs the app on this machine (Linux): the app's jars in one folder with the engine library beside them, a
# launcher in ~/.local/bin and an entry in the applications menu. Run it again to update.
#   scripts/install-desktop.sh
set -eu
cd "$(dirname "$0")/.."
SLUG="$(basename "$PWD")"
NAME="$(sed -n 's/.*APP_NAME *= *"\(.*\)".*/\1/p' app/src/commonMain/kotlin/app/loki/"$SLUG"/App.kt | head -1)"
NAME="${NAME:-$SLUG}"
cargo build --release -p loki-ffi
if [ -x ./gradlew ]; then GRADLE=./gradlew; else GRADLE=gradle; fi
$GRADLE :app:installDesktopLibs
MAIN="$(sed -n 's/.*mainClass = "\(.*\)".*/\1/p' app/build.gradle.kts | head -1)"
# The Java that built it (a JDK 21 in JAVA_HOME), else the one on the path.
JAVA_CMD="${JAVA_HOME:+$JAVA_HOME/bin/}java"
DEST="$HOME/.local/share/$SLUG"
rm -rf "$DEST/lib"
mkdir -p "$DEST" "$HOME/.local/bin" "$HOME/.local/share/applications"
cp -r app/build/install/lib "$DEST/lib"
cp target/release/libloki_ffi.so "$DEST/"
cat > "$HOME/.local/bin/$SLUG" <<LAUNCH
#!/usr/bin/env sh
# Skia's CPU renderer unless asked otherwise: it works on every machine.
export SKIKO_RENDER_API="\${SKIKO_RENDER_API:-SOFTWARE_FAST}"
exec "$JAVA_CMD" -Dloki.lib="$DEST" -cp "$DEST/lib/*" $MAIN "\$@"
LAUNCH
chmod +x "$HOME/.local/bin/$SLUG"
cat > "$HOME/.local/share/applications/$SLUG.desktop" <<ENTRY
[Desktop Entry]
Type=Application
Name=$NAME
Exec=$HOME/.local/bin/$SLUG
Terminal=false
Categories=Graphics;
ENTRY
echo "Installed $NAME: run $SLUG, or find it in the applications menu."
