# Project_SOEN342
## Team Members

| Name | Student ID |
|------|------------|
| Roshni Karwal | 40290739 |
| Anastasia Bondarenko | 40317398 |
| Chahd Hamada | 40274678 |

## Run the application

Requirements: a Java Development Kit (JDK) with `javac` and `java` available in PowerShell.

Open PowerShell in the project root (`Project_SOEN342`) and compile the application:

```powershell
$build = Join-Path $env:TEMP "soen342-build"
New-Item -ItemType Directory -Force $build | Out-Null
javac -d $build app\Main.java AppointmentAndAvailability\*.java data\*.java
```

Start the command-line application from the same project-root directory:

```powershell
java -cp $build app.Main
```

The application starts with an empty in-memory store. To search the sample availability, choose **4. Import availability from CSV** and enter `csv/availability.csv`. Then choose **2. Search availability**. You can also choose **3. Make availability** to create slots during the current run. The store is reset when the application exits, so export any availability you want to keep using **5. Export availability to CSV**.
