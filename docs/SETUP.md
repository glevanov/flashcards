# Local dev environment setup (Fedora)

Target machine: Fedora 44 Workstation. Editor: Zed (with the Kotlin extension).
No Android Studio required — everything is CLI-driven.

## 1. JDK 21

Gradle/AGP need a full JDK (17+; 21 recommended). The system may only have a
headless Java 25 runtime. Two options:

### Option A — system package (dnf, needs sudo)

```bash
sudo dnf install java-21-openjdk-devel
```

Point Gradle at JDK 21 regardless of the system default Java, via
`~/.gradle/gradle.properties` (machine-local, not committed):

```properties
org.gradle.java.home=/usr/lib/jvm/java-21-openjdk
```

### Option B — user-space tarball (no sudo)

Useful when `sudo` is unavailable (e.g. automated setup). Downloads the
Eclipse Temurin JDK 21 to `~/.local/opt/jdk-21`:

```bash
mkdir -p ~/.local/opt && cd /tmp
wget https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse -O jdk21.tar.gz
tar xzf jdk21.tar.gz
mv jdk-21.* ~/.local/opt/jdk-21
~/.local/opt/jdk-21/bin/javac -version   # must print 21.x
```

Then the same `~/.gradle/gradle.properties` line, pointing at the user-space
path instead:

```properties
org.gradle.java.home=/home/<you>/.local/opt/jdk-21
```

> Note: this machine currently uses Option B. The tarball is trivially
> replaceable with the dnf package later — just update the
> `org.gradle.java.home` path.

## 2. Android SDK command-line tools

```bash
mkdir -p ~/Android/Sdk/cmdline-tools
cd ~/Android/Sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-*.zip
mv cmdline-tools latest
```

## 3. SDK packages + licenses

```bash
export ANDROID_HOME=~/Android/Sdk
yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager \
  "platform-tools" "platforms;android-35" "build-tools;35.0.0"
```

## 4. Environment variables

Add to `~/.bashrc`:

```bash
export ANDROID_HOME=~/Android/Sdk
export PATH="$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin"
```

## 5. Phone over adb (no emulator needed)

1. On the phone: enable **Developer options** (tap build number 7×), then
   **USB debugging**.
2. Plug in via USB, then:

   ```bash
   adb devices        # accept the RSA prompt on the phone
   ```

3. Install/run builds:

   ```bash
   ./gradlew installDebug        # builds + installs on the connected phone
   adb install -r app/build/outputs/apk/debug/app-debug.apk   # manual alternative
   ```

Wireless debugging also works (`adb pair` / `adb connect`) once USB pairing is
done at least once.

## 6. Sanity check

```bash
java -version            # runtime (any 17+)
/usr/lib/jvm/java-21-openjdk/bin/javac -version   # JDK 21 compiler exists
adb devices              # phone listed as "device"
./gradlew --version      # Gradle wrapper resolves
```
