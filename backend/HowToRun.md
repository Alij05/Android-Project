# Run Sickimfy

## 1. Pull the latest changes

```powershell
git pull origin main
```

## 2. Configure the API

Add one of these lines to `local.properties`:

```properties
# Android Emulator
sickimfy.apiBaseUrl=http\://10.0.2.2\:8080/

# Physical Phone
sickimfy.apiBaseUrl=http\://LAPTOP_IP\:8080/
```

For a physical phone, replace `LAPTOP_IP` with the laptop’s Wi-Fi IPv4 address.

## 3. Physical Phone Only

Run once in Administrator PowerShell:

```powershell
New-NetFirewallRule -DisplayName "Sickimfy Backend TCP 8080" -Direction Inbound -Action Allow -Protocol TCP -LocalPort 8080 -Profile Any -RemoteAddress LocalSubnet
```

## 4. Run the Backend

```powershell
cd backend
.\gradlew.bat run
```

Keep the terminal open. Staying at `83% EXECUTING` is normal.

## 5. Run the App

Run the Android app normally from Android Studio.

If `Address already in use` appears, the Backend is already running.