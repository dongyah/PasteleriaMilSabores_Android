📱 Appium Android Automation – Java + TestNG + Cucumber

Automatización de pruebas para Android utilizando Appium, Java, TestNG, Cucumber y UIAutomator2.
El proyecto está diseñado para ejecutarse en IntelliJ IDEA Community, con soporte para pruebas BDD y reportes automáticos.

🚀 Tecnologías utilizadas

Java 17+

Maven

Appium Server

UIAutomator2 Driver

TestNG

Cucumber + Gherkin

Android Studio (Emulador o dispositivo físico)

📁 Estructura del proyecto
src
└── test
    ├── java
    │   ├── Runner
    │   │   └── TestRunner.java
    │   └── StepDefinitions
    │       ├── LoginSteps.java
    │       └── SampleTest.java
    └── resources
        └── features
            └── login.feature

🧰 Instalación de Appium y herramientas necesarias
1️⃣ Instalar Node.js
node -v


Si no lo tienes:

sudo apt install nodejs npm -y

2️⃣ Instalar Appium
npm install -g appium


Verificar instalación:

appium

3️⃣ Instalar driver de Android UIAutomator2
appium driver install uiautomator2

4️⃣ Instalar Appium Inspector (opcional)

Descargar desde:
https://github.com/appium/appium-inspector/releases

5️⃣ Plugins recomendados para IntelliJ IDEA Community

En File → Settings → Plugins:

Plugin	Uso
Cucumber for Java	Ejecutar escenarios Gherkin
Gherkin	Colorear sintaxis Gherkin
TestNG	Framework de pruebas
Lombok (opcional)	Helpers para POJOs
⚙️ Desired Capabilities (Java)
@BeforeClass
public void setUp() throws Exception {

    UiAutomator2Options options = new UiAutomator2Options()
            .setPlatformName("Android")
            .setDeviceName("Android Emulator")
            .setAutomationName("UiAutomator2")
            .setPlatformVersion("14")
            .setApp("/ruta/a/app-debug.apk")
            .setAppPackage("com.example.proyectobase")
            .setAppActivity("com.example.proyectobase.MainActivity")
            .setAutoGrantPermissions(true)
            .setNewCommandTimeout(Duration.ofSeconds(360));

    driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);

    DriverManager.setDriver(driver);
}

🐍 Desired Capabilities (Python)
capabilities = {
    "platformName": "Android",
    "appium:platformVersion": "14",
    "appium:deviceName": "Google Pixel 7 Pro",
    "appium:automationName": "uiautomator2",
    "appium:app": "/path/app-debug.apk",
    "appium:autoGrantPermissions": True,
    "sauce:options": {
        "appiumVersion": "latest"
    }
}

📜 Ejecución de pruebas
✔ Desde Maven
mvn clean test

✔ Desde IntelliJ

Abrir TestRunner.java

Clic derecho → Run TestRunner

Reportes generados en:

target/cucumber-report.html

🧩 Runner de Cucumber
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"StepDefinitions"},
        plugin = {
                "pretty",
                "html:target/cucumber-report.html",
                "json:target/cucumber.json"
        },
        monochrome = true
)
public class TestRunner extends AbstractTestNGCucumberTests {
}

🧪 Ejemplo de feature en Gherkin

login.feature

Feature: Login en la aplicación

  Scenario: Ingreso exitoso
    Given la app está abierta
    When ingreso usuario "admin" y contraseña "1234"
    Then debo ver el mensaje "Bienvenido"

📌 Ejemplo de Step Definition
@Given("la app está abierta")
public void laAppEstaAbierta() {
    driver = DriverManager.getDriver();
}

🧱 Requisitos previos

Java 17+

Android SDK + Emulador

Appium Server corriendo:

appium


APK compilado (app-debug.apk)

🔧 TODO (Mejoras futuras)

 Integrar Allure Reports

 Implementar Page Object Model (POM)

 Añadir pruebas para gestos: scroll, tap, swipe

 Pipeline CI/CD con GitHub Actions

⭐ Contribuciones

¡Las contribuciones son bienvenidas!
Si deseas agregar mejoras, abre un Pull Request o crea un Issue.
